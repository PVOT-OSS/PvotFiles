<!--
SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: Apache-2.0
-->

# Privacy Policy — Files

_Effective 18 November 2026_

Files (package `org.prauga.files`) is an app published by Saalim Quadri for PVOT-OSS. This
policy explains what the app does with your data.

**In short: the app has no accounts, no ads, no analytics and no tracking. It has no internet
permission, so nothing you do in it leaves your phone.**

## What the app stores

How you like to see your files (the sort order, list or grid view, and whether hidden files
are shown), the folders you mark as favourites, and the tags you make with the files and folders
you put them on. It is kept on your device. The app reads your files to show them; it keeps no copy of
them and no list of what you open.

Files you delete go to Recently deleted, a hidden `.trash-files` folder on your storage, with a
note of where each came from and when. They stay there for 30 days, or until you restore them or
delete them forever.

## Permissions

- **All files access** (`MANAGE_EXTERNAL_STORAGE`): lets the app show and manage the files on
  your device's shared storage. You grant it in Android's settings and can withdraw it there at
  any time. The app reads and changes files only when you ask it to, and sends nothing anywhere.
- **Install apps** (`REQUEST_INSTALL_PACKAGES`): when you open an APK file, Files hands it to
  Android's installer, which asks you to confirm. Nothing is installed without your confirmation.
- **Find unused apps** (`PACKAGE_USAGE_STATS`, only if you allow usage access in Settings): reads
  when your apps were last used and how much space they take, to list apps you haven't opened for
  30 days in Clean. Files sees only apps with a launcher icon. This stays on your phone.
- **Uninstall apps** (`REQUEST_DELETE_PACKAGES`): from the unused apps list; Android asks you to
  confirm every uninstall.
- **Read photo locations** (`ACCESS_MEDIA_LOCATION`, only if you allow it from a photo's Info):
  shows where a photo was taken in its Info. The place stays on your phone; tapping it opens your
  maps app.
- **Run in the foreground and show notifications** (`FOREGROUND_SERVICE`,
  `FOREGROUND_SERVICE_DATA_SYNC`, `POST_NOTIFICATIONS`): lets a copy or move you started keep going
  after you leave the app, with its progress in a notification. Android asks you before the app may
  show notifications; if you say no, copies and moves still work.

Your recent searches (the last 10) are kept on your device until you clear them.

Files you move to the Safe folder are encrypted and kept in the app's private storage. The key
is held by Android's keystore on this device and never leaves it; your PIN is stored only as a
salted hash. If you forget the PIN or uninstall the app, these files can't be recovered. When you
open one, a temporary decrypted copy is made in the app's cache and removed when the Safe folder
locks.

## Backups

Android may include the app's data in your device backup and in a transfer to a new device. The
Safe folder, its key and its PIN are left out of both.

## Changes

Changes to this policy are published with the app's source and take effect with the release that
ships them.

## Contact

Open an issue at <https://github.com/PVOT-OSS/Files/issues>.
