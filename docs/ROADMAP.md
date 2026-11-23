# Vybetube roadmap

## Foundation in this branch

- [x] Native Kotlin / Jetpack Compose Android application shell.
- [x] Optional Leanback launcher category and touch-screen optional feature declaration.
- [x] Local DataStore-backed preference switches for the initial settings.
- [x] Rust workspace with provider-independent cache and download-state primitives.
- [x] Clear product boundary: YouTube account APIs are not unrestricted stream/download APIs.

## Next implementation milestones

1. **Device experience:** refine TV detection, D-pad focus order, overscan-safe spacing, remote back behaviour, tablet navigation and accessibility.
2. **Playback:** integrate Media3 session/service, audio focus, notification controls, PiP, subtitle tracks, playback speed and resume position using permitted media sources.
3. **Library:** local playlists, queue persistence, history search, bookmarks and export/import.
4. **Downloads:** permission-aware source capability checks, quality choices from actual source formats, pause/resume, storage selection and retry policy.
5. **Providers:** define a provider interface and implement only integrations whose access methods and terms permit the intended use.
6. **Rust bridge:** define a stable JNI/UniFFI boundary and move provider-independent cache, filtering and scheduling logic behind it.
7. **Quality:** unit/UI tests, accessibility checks, performance profiling, crash-safe migrations and signed release workflow.

## Settings policy

A preference must be connected to a real feature before it is presented as an active control. The first settings screen currently persists its switches locally; several are preparatory preferences and do not yet alter playback because playback services are not implemented.

## Reference projects

- NewPipe: queue, local playlists, channel groups, history and background audio patterns. GPL-3.0.
- LibreTube: privacy-first UX, Material 3 patterns and optional external integrations. GPL-3.0-or-later.
- SmartTube: TV-oriented focus/navigation and remote interaction patterns. MIT.

Study patterns and APIs; do not copy source files without checking compatibility, attribution and redistribution obligations.
