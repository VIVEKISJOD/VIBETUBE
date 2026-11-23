package app.vybetube

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val android.content.Context.settingsStore by preferencesDataStore(name = "vybetube_settings")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VybetubeApp() }
    }
}

private val canvas = Color(0xFF090B10)
private val surface = Color(0xFF151923)
private val accent = Color(0xFFB6F36A)
private val muted = Color(0xFF9299A8)

@Composable
private fun VybetubeApp() {
    val configuration = LocalConfiguration.current
    val isTv = (configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION
    var selected by remember { mutableStateOf("Settings") }
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = canvas, surface = surface, primary = accent,
            onPrimary = Color(0xFF10150A), onBackground = Color.White,
            onSurface = Color.White, secondary = muted
        )
    ) {
        Row(Modifier.fillMaxSize().background(canvas)) {
            if (isTv) {
                NavigationRail(selected, isTv) { selected = it }
            }
            Column(Modifier.fillMaxSize().weight(1f)) {
                if (!isTv) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("VYBE", color = accent, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Text("TUBE", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Rounded.Cast, contentDescription = "Cast", tint = muted)
                    }
                }
                when (selected) {
                    "Settings" -> SettingsScreen()
                    else -> PlaceholderScreen(selected)
                }
                if (!isTv) {
                    NavigationBar(containerColor = surface) {
                        listOf("Home", "Search", "Library", "Settings").forEach { item ->
                            NavigationBarItem(
                                selected = selected == item,
                                onClick = { selected = item },
                                icon = {
                                    Icon(
                                        when (item) {
                                            "Home" -> Icons.Rounded.Home
                                            "Search" -> Icons.Rounded.Search
                                            "Library" -> Icons.Rounded.VideoLibrary
                                            else -> Icons.Rounded.Tune
                                        }, contentDescription = item
                                    )
                                },
                                label = { Text(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationRail(selected: String, isTv: Boolean, onSelect: (String) -> Unit) {
    NavigationRail(containerColor = surface) {
        listOf("Home", "Search", "Library", "Settings").forEach { item ->
            NavigationRailItem(
                selected = selected == item, onClick = { onSelect(item) },
                icon = {
                    Icon(when (item) {
                        "Home" -> Icons.Rounded.Home
                        "Search" -> Icons.Rounded.Search
                        "Library" -> Icons.Rounded.VideoLibrary
                        else -> Icons.Rounded.Tune
                    }, contentDescription = item)
                },
                label = { Text(item) }
            )
        }
    }
}

@Composable
private fun SettingsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by context.settingsStore.data.collectAsState(initial = androidx.datastore.preferences.core.emptyPreferences())
    val groups = listOf(
        SettingGroup("PLAYBACK", listOf(
            SettingItem("Autoplay next", "Continue to the next video automatically", "autoplay", true),
            SettingItem("Background audio", "Keep audio playing when the app is minimized", "background_audio", true),
            SettingItem("Picture in picture", "Allow a floating mini-player", "pip", true),
            SettingItem("Remember playback speed", "Reuse your last selected speed", "remember_speed", true)
        )),
        SettingGroup("DATA & NETWORK", listOf(
            SettingItem("Data saver", "Prefer lower quality on mobile data", "data_saver", false),
            SettingItem("Wi-Fi only downloads", "Prevent downloads over mobile data", "wifi_downloads", true),
            SettingItem("Preload next video", "Prepare the next item in your queue", "preload_next", false)
        )),
        SettingGroup("PRIVACY", listOf(
            SettingItem("Pause watch history", "Do not add new items to local history", "pause_history", false),
            SettingItem("Incognito session", "Keep this session out of local history", "incognito", false)
        )),
        SettingGroup("EXPERIENCE", listOf(
            SettingItem("Haptic feedback", "Use subtle feedback for supported actions", "haptics", true),
            SettingItem("Reduce motion", "Limit non-essential interface animations", "reduce_motion", false)
        ))
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Your experience.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Make Vybetube work the way you do.", color = muted, modifier = Modifier.padding(top = 5.dp, bottom = 10.dp))
        }
        item {
            Surface(color = surface, shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Tune, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Playback & app settings", fontWeight = FontWeight.SemiBold)
                        Text("Vybetube 0.1.0 · Local preferences", color = muted, style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = muted)
                }
            }
        }
        items(groups) { group ->
            Text(group.title, color = accent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, start = 4.dp))
            Surface(color = surface, shape = RoundedCornerShape(18.dp)) {
                Column {
                    group.items.forEachIndexed { index, item ->
                        val key = booleanPreferencesKey(item.key)
                        val enabled = settings[key] ?: item.default
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(item.title, fontWeight = FontWeight.Medium)
                                Text(item.description, color = muted, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = enabled,
                                onCheckedChange = { value ->
                                    scope.launch { context.settingsStore.edit { it[key] = value } }
                                }
                            )
                        }
                        if (index != group.items.lastIndex) HorizontalDivider(color = Color.White.copy(alpha = 0.06f), modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
        item {
            Text("More controls are being added as their underlying features land. A setting will never be shown as available until it has a working implementation.", color = muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))
        }
    }
}

private data class SettingItem(val title: String, val description: String, val key: String, val default: Boolean)
private data class SettingGroup(val title: String, val items: List<SettingItem>)

@Composable
private fun PlaceholderScreen(name: String) {
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center) {
        Text(name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("This area is reserved for the native Vybetube experience. Its provider and playback integrations are not wired yet.", color = muted, modifier = Modifier.padding(top = 8.dp))
    }
}
