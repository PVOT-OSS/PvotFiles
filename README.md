<!--
SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: Apache-2.0
-->

# Files

A file manager for Android, part of PVOT-OSS. It shows how your storage is used, lets you browse,
open, share, create, rename, copy, move and delete files (with a 30-day Recently deleted), search by
name, keep favourite folders, tag files and see previews of photos and videos. It has no internet
permission.

Built with Kotlin, Jetpack Compose, [PvotLib](https://github.com/PVOT-OSS/PvotLib) and Hilt;
Android 11 or newer. Files needs *All files access*, which you grant in Android's settings on first
start.

## Build

```sh
./gradlew assembleDebug assembleRelease testDebugUnitTest lintDebug
```

## Licence

Apache License 2.0 — see [LICENSE.md](LICENSE.md).
