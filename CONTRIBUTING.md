# Contributing to LocalWatch

Thanks for improving LocalWatch.

## Before opening a change

- Search existing issues and pull requests.
- Keep the app offline-first: viewer assets must not depend on CDNs or remote fonts.
- Keep every visible control functional and every state honest.
- Preserve scoped storage; never expose document URIs or raw filesystem paths.
- Treat correct HTTP range behavior and release signing as compatibility requirements.

## Development

Requirements:

- JDK 17
- Android SDK 35

Run the checks:

```bash
./gradlew testDebugUnitTest assembleDebug
```

Windows contributors may use:

```powershell
.\setup-toolchain-on-f.ps1
.\build-on-f.ps1 -Target debug
```

## Pull requests

Keep changes focused, describe the user-visible result, and include the verification you performed. Add or update tests for range parsing, server behavior, or generated web UI when those areas change.

Do not commit build outputs, local configuration, credentials, signing keys, or signing passwords.
