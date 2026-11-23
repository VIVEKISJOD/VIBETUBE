# Architecture

## Layers

1. **Compose presentation** — native screens, navigation, adaptive layouts, accessibility and D-pad focus.
2. **Application domain** — playback use cases, queue, local library, preferences and download orchestration.
3. **Media adapter** — Android Media3 player, media session, audio focus, PiP and subtitle tracks.
4. **Provider adapters** — capability-described sources for search, metadata and permitted playback URLs.
5. **Rust core** — provider-independent models and policies: cache decisions, filtering, scheduling and validation.
6. **Persistence** — DataStore for preferences; a versioned local database for history, queue and playlists when that module is introduced.

## Device adaptation

Do not infer TV mode from screen size alone. Combine Android's television UI mode, available input features and window size. TV screens should use a 10-foot layout, clear focus states and predictable D-pad traversal. Tablets should use a navigation rail at wide breakpoints. Phones should use compact navigation and touch-friendly targets.

## Media playback

Media3 is the Android playback engine. The app should request only source formats actually offered by a provider and supported by the device. Hardware decoding is selected by Android/Media3 according to device capabilities; the app should not claim that every codec or resolution is available on every device.

## Rust boundary

The Rust crate must remain independent of Android UI and provider-specific extraction. Introduce JNI or UniFFI only after the Kotlin domain interfaces and Rust API are stable. Keep a Kotlin fallback for platform services that belong in Android.

## Security

- Never store Google passwords.
- Keep OAuth tokens out of logs and source control; use the platform-supported secure token storage path.
- Request only the minimum OAuth scopes and explain each scope.
- Never embed API keys or signing secrets in the APK.
- Treat provider metadata and URLs as untrusted input.
