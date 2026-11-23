package app.vybetube

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

private const val RELEASES_API = "https://api.github.com/repos/VIVEKISJOD/VIBETUBE/releases/latest"
private const val RELEASES_PAGE = "https://github.com/VIVEKISJOD/VIBETUBE/releases"
private const val MAX_APK_BYTES = 300L * 1024L * 1024L

private data class VybetubeRelease(
    val tag: String,
    val name: String,
    val notes: String,
    val apkUrl: String,
    val sha256: String
)

@Composable
fun UpdateCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var release by remember { mutableStateOf<VybetubeRelease?>(null) }
    var status by remember { mutableStateOf("Check GitHub for the latest stable version.") }
    var busy by remember { mutableStateOf(false) }
    var downloadedApk by remember { mutableStateOf<File?>(null) }

    fun openReleases() {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(RELEASES_PAGE)))
    }

    fun install(file: File) {
        if (!context.packageManager.canRequestPackageInstalls()) {
            val permissionIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(permissionIntent)
            status = "Allow Vybetube to install updates, return here, then tap Install."
            return
        }
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("VYBETUBE UPDATES", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("Keep your app current", style = MaterialTheme.typography.titleMedium)
            Text("Installed version: 0.1.0", style = MaterialTheme.typography.bodySmall)
            Text(status, style = MaterialTheme.typography.bodyMedium)
            release?.let { latest ->
                Text("Latest: ${latest.name} (${latest.tag})", style = MaterialTheme.typography.titleSmall)
                if (latest.notes.isNotBlank()) {
                    Text(latest.notes.take(1200), style = MaterialTheme.typography.bodySmall)
                }
            }
            if (busy) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircularProgressIndicator()
                    Text("Working…")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !busy,
                    onClick = {
                        busy = true
                        status = "Checking the latest stable release…"
                        scope.launch {
                            try {
                                val result = withContext(Dispatchers.IO) { fetchLatestRelease() }
                                release = result
                                val current = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.1.0"
                                status = if (compareVersions(result.tag, current) > 0) {
                                    "A newer version is available."
                                } else {
                                    "You're using the latest stable version, or a newer build."
                                }
                            } catch (error: Exception) {
                                status = "Could not check for updates: ${error.message ?: "network error"}"
                            } finally {
                                busy = false
                            }
                        }
                    }
                ) { Text("Check for updates") }
                OutlinedButton(onClick = { openReleases() }) { Text("View all releases") }
            }
            release?.let { latest ->
                val readyFile = downloadedApk
                Button(
                    enabled = !busy,
                    onClick = {
                        if (readyFile != null && readyFile.exists()) {
                            install(readyFile)
                        } else {
                            busy = true
                            status = "Downloading and verifying the APK…"
                            scope.launch {
                                try {
                                    val file = withContext(Dispatchers.IO) {
                                        downloadAndVerify(latest)
                                    }
                                    downloadedApk = file
                                    status = "Download verified. Android will ask you to confirm installation."
                                    install(file)
                                } catch (error: Exception) {
                                    status = "Update download failed: ${error.message ?: "unknown error"}"
                                } finally {
                                    busy = false
                                }
                            }
                        }
                    }
                ) {
                    Text(if (readyFile?.exists() == true) "Install update" else "Download & install")
                }
            }
            Text(
                "Updates are downloaded from this project's GitHub Releases. Android confirms installation; Vybetube cannot silently install APKs.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun fetchLatestRelease(): VybetubeRelease {
    val connection = URL(RELEASES_API).openConnection() as HttpURLConnection
    try {
        connection.requestMethod = "GET"
        connection.connectTimeout = 12000
        connection.readTimeout = 15000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException("GitHub returned HTTP ${connection.responseCode}")
        }
        val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        if (json.optBoolean("draft") || json.optBoolean("prerelease")) {
            throw IllegalStateException("The latest release is not a stable release.")
        }
        val assets = json.getJSONArray("assets")
        var apkUrl: String? = null
        var digest: String? = null
        for (index in 0 until assets.length()) {
            val asset = assets.getJSONObject(index)
            val name = asset.optString("name")
            if (name.endsWith(".apk", ignoreCase = true)) {
                apkUrl = asset.optString("browser_download_url")
                digest = asset.optString("digest").removePrefix("sha256:").takeIf { it.matches(Regex("[a-fA-F0-9]{64}")) }
                break
            }
        }
        val safeUrl = apkUrl ?: throw IllegalStateException("This release has no APK asset yet.")
        val safeDigest = digest ?: throw IllegalStateException("Release APK is missing GitHub's SHA-256 digest. Download disabled for safety.")
        val uri = Uri.parse(safeUrl)
        if (uri.scheme != "https" || uri.host != "github.com" && uri.host != "objects.githubusercontent.com" && uri.host != "release-assets.githubusercontent.com") {
            throw IllegalStateException("Release asset URL is not trusted.")
        }
        return VybetubeRelease(
            tag = json.optString("tag_name"),
            name = json.optString("name").ifBlank { json.optString("tag_name") },
            notes = json.optString("body"),
            apkUrl = safeUrl,
            sha256 = safeDigest.lowercase()
        )
    } finally {
        connection.disconnect()
    }
}

private fun downloadAndVerify(release: VybetubeRelease): File {
    val connection = URL(release.apkUrl).openConnection() as HttpURLConnection
    try {
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        connection.instanceFollowRedirects = true
        if (connection.contentLengthLong > MAX_APK_BYTES) {
            throw IllegalStateException("APK exceeds the 300 MB safety limit.")
        }
        val directory = File(System.getProperty("java.io.tmpdir") ?: ".", "vybetube-updates")
        // Use the app cache directory through the caller-specific cache path below.
        val output = File.createTempFile("vybetube-", ".apk", directory.apply { mkdirs() })
        val digest = MessageDigest.getInstance("SHA-256")
        var total = 0L
        connection.inputStream.use { input ->
            output.outputStream().use { stream ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > MAX_APK_BYTES) throw IllegalStateException("APK exceeds the 300 MB safety limit.")
                    digest.update(buffer, 0, count)
                    stream.write(buffer, 0, count)
                }
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        if (actual != release.sha256) {
            output.delete()
            throw SecurityException("SHA-256 verification failed. The APK was discarded.")
        }
        return output
    } finally {
        connection.disconnect()
    }
}

private fun compareVersions(remote: String, installed: String): Int {
    fun parts(value: String) = value.removePrefix("v").substringBefore("-")
        .split(".").map { it.toIntOrNull() ?: 0 }
    val left = parts(remote)
    val right = parts(installed)
    for (index in 0 until maxOf(left.size, right.size)) {
        val a = left.getOrElse(index) { 0 }
        val b = right.getOrElse(index) { 0 }
        if (a != b) return a.compareTo(b)
    }
    return 0
}
