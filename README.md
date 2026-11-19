<!--
SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: Apache-2.0
-->

# Files

A file manager for Android, part of PVOT-OSS. It shows how your storage is used and lets you:

- browse internal storage, SD cards, USB drives and folders other apps share;
- select several files to share, delete, copy, move, tag or compress;
- view photos, PDFs and text, and play video and music, in the app;
- compress to ZIP and extract archives, with a preview first;
- clean up large files, duplicates, old screenshots and downloads, empty folders and unused apps;
- search by name with type, date and size filters, or inside one folder;
- keep private files in an encrypted, PIN-locked Safe folder;
- restore deleted items for 30 days, keep favourites and coloured tags.

It has no internet permission, no accounts, no ads and no analytics.

Built with Kotlin, Jetpack Compose, [PvotLib](https://github.com/PVOT-OSS/PvotLib), Hilt, Media3 and
Tink; Android 11 or newer. Files needs *All files access*, which you grant in Android's settings on
first start.

## Build

```sh
./gradlew assembleDebug assembleRelease testDebugUnitTest lintDebug
```

Release builds are signed with Android Studio's *Generate Signed Bundle / APK*. To build PvotLib from
source instead of its Maven repository, pass `-PpvotLibDir=/path/to/PvotLib`.

Store listing text and graphics are in `fastlane/metadata/android`, the F-Droid recipe in
`fdroid/metadata`, and the privacy policy in [PrivacyPolicy.md](PrivacyPolicy.md).

## Licence

Apache License 2.0 — see [LICENSE.md](LICENSE.md).
