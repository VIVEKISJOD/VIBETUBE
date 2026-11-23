# In-app updater

Vybetube will ship with an in-app update centre backed by GitHub Releases for direct-download builds.

## User experience

- Settings → About Vybetube → Updates.
- Check now, last checked time, current version and available version.
- Release notes before downloading.
- Download progress, pause/cancel where the Android download API supports it, retry on failure.
- Verify the downloaded APK against the SHA-256 digest published for the release asset when available.
- Hand off installation to Android's Package Installer. Android must show its normal user confirmation; the app must never claim it can silently install an update.
- "Remind me later" and "Skip this version" preferences.
- Stable and beta channels, with stable selected by default.
- Automatic update checks can be disabled and should respect Wi-Fi-only preferences.

## Release publishing contract

Each stable GitHub Release must include:
- A versioned APK asset, e.g. `Vybetube-v0.2.0.apk`.
- A release tag matching the app version, e.g. `v0.2.0`.
- Release notes / changelog.
- A SHA-256 digest for the APK (GitHub release asset digest or a published checksum file).

The APK must be signed with the same application signing certificate as the installed app. A different signing key cannot update an existing installation. Never publish signing keys, passwords or keystore files in the repository.

## Distribution modes

- **Direct APK / GitHub Releases:** updater checks GitHub Releases, downloads the APK in-app and opens Android's package installer. Users may need to allow Vybetube to install unknown apps in Android settings, and must confirm installation.
- **Google Play:** use Play's supported in-app update API instead. Play-distributed builds should not download APKs from GitHub to replace themselves.

The build flavor must determine which update source is active; do not mix Play and sideload update paths.

## Security and failure handling

- Use HTTPS and a fixed, allowlisted repository owner/name; never trust an arbitrary URL from a remote manifest.
- Parse release JSON defensively; ignore drafts and prereleases on stable channel.
- Require a compatible APK package/application ID and a versionCode greater than the installed version.
- Verify SHA-256 when the release API supplies a digest; fail closed if a configured mandatory digest is missing or mismatched.
- Let Android verify the APK signature during installation. A mismatched signing certificate must fail installation.
- Never ask for Google credentials or download/install silently.
- Handle offline, rate limits, missing assets, insufficient storage, unsupported device ABI, download cancellation and installer errors.
- Do not update the app from an untrusted branch commit or arbitrary CI artifact; only published releases on the selected channel count.

## Implementation status

This document defines the required behaviour. The updater UI, release checker, verified downloader and Package Installer handoff are not yet implemented. Do not show a fake "up to date" state until a real release check succeeds.
