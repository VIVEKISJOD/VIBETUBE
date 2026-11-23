# Vybetube settings blueprint

Settings are grouped by user intent rather than by implementation module. Every row in the UI must have a real effect before it is marked as supported. Preferences are local-first and should remain usable without signing in.

## Playback

- Default video quality: Auto / Best available / Data saver / Ask each time.
- Preferred codec: Auto / AV1 / VP9 / AVC, filtered by actual device and source support.
- Playback speed, remember speed, autoplay next and autoplay previews.
- Background audio, picture-in-picture and lock-screen / notification controls.
- Subtitle language, subtitle size, caption style and automatic captions when supplied.
- Gesture controls: brightness, volume, seek and double-tap seek distance.
- Resume position, skip intro/outro where source metadata permits, and audio-only mode.

## Downloads

- Download only when the source and content permit offline copies.
- Available quality choices must come from the source; never upscale a lower-quality source.
- Wi-Fi-only, charging-only, concurrent task limit, retry policy and pause/resume.
- Storage destination, per-item actions, queue ordering, free-space warning and cleanup.
- Separate completed, queued, active, failed and expired items.

## Data and performance

- Data saver, metered-network policy, thumbnail loading, next-item preload.
- Configurable cache budget, cache breakdown and clear-cache action.
- Background refresh policy and diagnostics for playback/network failures.

## Appearance and accessibility

- AMOLED black, dark, light and system themes.
- Accent colour, compact/comfortable density, artwork treatment and reduced motion.
- Text scaling, contrast, captions, screen-reader labels and focus indicators.
- Phone bottom navigation, tablet/TV rail navigation and overscan-safe TV spacing.

## Privacy and account

- Pause/clear local watch history and search history.
- Incognito session, clear local data and export/import local library.
- Account connection status, granted scopes, revoke/disconnect action and privacy explanation.
- No password fields for Google sign-in. Use a supported OAuth browser flow.

## TV and remote

- D-pad focus order, visible focus ring, predictable Back handling and remote shortcuts.
- Optional autoplay previews, preview sound and ambient artwork.
- Search keyboard behaviour and touch/remote interaction parity.

## Advanced

- Playback engine / codec diagnostics, app version, open-source notices and logs export.
- Feature flags only for genuinely experimental functionality.
- Provider health and capability details without exposing tokens or secrets.

## Current implementation status

The first Compose settings screen persists its initial boolean preferences using Android DataStore. The switches are a preference foundation; playback, downloads, PiP, background service, account and cache engines are not yet implemented. Do not interpret a saved preference as proof that its corresponding feature is active.
