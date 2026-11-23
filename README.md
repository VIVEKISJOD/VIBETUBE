# Vybetube

A native Android media experience for phones, tablets, Android TV and Google TV.

> **Project status:** early foundation. This repository is not yet a finished video client. Playback providers, account integration, downloads and TV support will be added as independently testable modules.

## Product direction

- Native Kotlin + Jetpack Compose interface; no WebView-based app shell.
- One adaptive Android app with touch-first phone/tablet screens and D-pad-first TV navigation.
- Media3 / ExoPlayer for local and permitted remote media playback.
- Rust shared core for provider-independent work such as cache policy, download scheduling, metadata models and filtering.
- Local-first preferences, history and playlists, with explicit controls for export and deletion.
- Accessible controls, predictable focus, responsive loading/error/empty states and configurable playback behaviour.

## Important YouTube limitations

Google OAuth does not grant unrestricted access to YouTube's media streams. The YouTube Data API supports specific account and metadata operations within its documented scopes and quotas; it is not a general-purpose video-stream or offline-download API. Vybetube will not collect Google passwords, promise unrestricted playback, or bypass access controls. Provider adapters must respect applicable terms, copyright and source permissions. Offline downloads will be offered only for sources and content that permit them.

## Repository layout

- `android/` — native Android application.
- `core-rust/` — provider-independent Rust library.
- `docs/` — architecture, settings and implementation notes.
- `.github/workflows/` — automated validation.

## Build status

The initial project foundation and CI configuration are being established. See [docs/ROADMAP.md](docs/ROADMAP.md) for what is implemented versus planned.

## Open-source references

Vybetube may learn from the interaction patterns and architecture of projects such as [NewPipe](https://github.com/TeamNewPipe/NewPipe), [LibreTube](https://github.com/libre-tube/LibreTube) and [SmartTube](https://github.com/yuliskov/SmartTube). No source code is copied into this repository at this stage. Any future code reuse must be reviewed for licence compatibility and accompanied by required notices and source offers.

## License

No project-wide license has been selected yet. Until one is added, all rights are reserved by the copyright holder.