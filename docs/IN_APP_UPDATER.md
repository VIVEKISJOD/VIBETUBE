# In-app updater

## Current implementation

The direct-download updater is now wired into the Settings screen on the native Android app.

- Checks the fixed `VIVEKISJOD/VIBETUBE` GitHub stable-release endpoint.
- Displays the installed version, latest release name/tag and release notes.
- Shows an update action only when the release tag is newer than the installed version.
- Includes **View all releases**, which opens the repository's complete GitHub Releases page.
- Downloads the APK into the app's private cache directory.
- Requires GitHub's SHA-256 asset digest and fails closed when it is missing or mismatched.
- Restricts release asset URLs to HTTPS and GitHub's release asset hosts.
- Checks the APK package ID and requires its versionCode to be higher before handing it to Android.
- Uses Android FileProvider and the system package installer. Android's unknown-apps permission and installation confirmation remain under the user's control.

The updater is implemented for direct GitHub APK distribution. It has not yet been tested end-to-end with a published signed release APK. The current repository has no production release APK or app signing key configured.

## Release publishing contract

Every stable GitHub Release must include:
- A versioned APK asset, e.g. `Vybetube-v0.2.0.apk`.
- A release tag matching the app version, e.g. `v0.2.0`.
- A release APK built with application ID `app.vybetube`.
- A versionCode higher than the previous release.
- Release notes / changelog.
- GitHub's SHA-256 asset digest.

The APK must be signed with the same application signing certificate as the installed app. A different signing key cannot update an existing installation. Never publish signing keys, passwords or keystore files in the repository.

## Distribution modes

- **Direct APK / GitHub Releases:** the updater checks GitHub Releases, downloads and verifies the APK, then opens Android's package installer. Users may need to allow Vybetube to install unknown apps in Android settings, and must confirm installation.
- **Google Play:** use Play's supported in-app update API instead. Play-distributed builds should not download APKs from GitHub to replace themselves.

The build flavor must determine which update source is active; do not mix Play and sideload update paths.

## Remaining updater work

- End-to-end test with a real signed GitHub release and a second higher-version APK.
- Add beta channel selection.
- Add automatic checks, last-checked timestamp, reminder/skip preferences and download progress/cancel.
- Add a Play distribution flavor and Play in-app updates only if the app is later published there.

## Signing and safety

- Keep the signing keystore outside GitHub and back it up securely.
- Never install silently or accept arbitrary update URLs.
- Let Android verify the APK signing certificate during installation.
- Do not publish signing keys, passwords or keystore files in the repository.
