<!--
SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: Apache-2.0
-->

# Privacy Policy — Files

_Effective 20 November 2026_

Files (package `org.prauga.files`) is an app published by Saalim Quadri for PVOT-OSS. This
policy explains what the app does with your data.

**In short: the app has no accounts, no ads, no analytics and no tracking. It has no internet
permission, so nothing you do in it leaves your phone.**

## What the app stores

All of this is kept on your device:

- Your settings: sort order, list or grid view, whether hidden files are shown, theme and dynamic
  colour.
- The files and folders you mark as favourites, the tags you make and the items you put them on.
- Your last 10 searches, until you clear them.
- The locations from other apps' storage you add, with the access Android granted for them.

The app reads your files to show, open, play, compress and clean them; it keeps no list of what
you open. To find duplicates it reads the files' contents on your phone and remembers their
fingerprints only while the app is running.

Files you delete go to Recently deleted, a hidden `.trash-files` folder on your storage, with a
note of where each came from and when. They stay there for 30 days, or until you restore them or
delete them forever.

Files you move to the Safe folder are encrypted and kept in the app's private storage. The key is
held by Android's keystore on this device and never leaves it; your PIN is stored only as a salted
hash. If you forget the PIN, clear the app's data or uninstall the app, these files can't be
recovered. When you open one, a temporary decrypted copy is made in the app's cache and removed
when the Safe folder locks.

When another app asks Files to open a file, Files may keep a temporary copy in its cache to show
it. Files you share to Files are saved in your Downloads folder.

## Permissions

- **All files access** (`MANAGE_EXTERNAL_STORAGE`): lets the app show and manage the files on your
  device's shared storage. You grant it in Android's settings and can withdraw it there at any
  time. The app reads and changes files only when you ask it to.
- **Install apps** (`REQUEST_INSTALL_PACKAGES`): when you open an APK file, Files hands it to
  Android's installer, which asks you to confirm. Nothing is installed without your confirmation.
- **Find unused apps** (`PACKAGE_USAGE_STATS`, only if you allow usage access in Settings): reads
  when your apps were last used and how much space they take, to list apps you haven't opened for
  30 days in Clean. Files sees only apps with a launcher icon.
- **Uninstall apps** (`REQUEST_DELETE_PACKAGES`): from the unused apps list; Android asks you to
  confirm every uninstall.
- **Read photo locations** (`ACCESS_MEDIA_LOCATION`, only if you allow it from a photo's Info):
  shows where a photo was taken in its Info. Tapping the place opens your maps app.
- **Keep working in the background** (`FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC`,
  `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`): a copy, move, compress or extract you started keeps going
  after you leave the app, with its progress in a notification, and resumes after a restart.
- **Keep playing audio** (`FOREGROUND_SERVICE_MEDIA_PLAYBACK`): audio you open keeps playing after
  you leave the player, with controls in the notification and on the lock screen.
- **Show notifications** (`POST_NOTIFICATIONS`): for that progress and when an SD card or USB drive
  is ready. Android asks you first; if you say no, everything else still works.

## Backups

Android may include the app's data in your device backup and in a transfer to a new device. The
Safe folder, its key and its PIN are left out of both.

## Changes

Changes to this policy are published with the app's source and take effect with the release that
ships them.

## Contact

Open an issue at <https://github.com/PVOT-OSS/Files/issues>.
