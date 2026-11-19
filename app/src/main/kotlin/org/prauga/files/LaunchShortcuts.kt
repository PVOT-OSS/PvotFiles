// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

object LaunchShortcuts {
    fun publish(context: Context) {
        val shortcuts =
            listOf(
                shortcut(
                    context,
                    LaunchTarget.Downloads,
                    R.string.home_downloads,
                    R.drawable.ic_shortcut_downloads,
                ),
                shortcut(
                    context,
                    LaunchTarget.Clean,
                    R.string.tab_clean,
                    R.drawable.ic_shortcut_clean,
                ),
                shortcut(
                    context,
                    LaunchTarget.Search,
                    R.string.tab_search,
                    R.drawable.ic_shortcut_search,
                ),
                shortcut(
                    context,
                    LaunchTarget.SafeFolder,
                    R.string.safe_title,
                    R.drawable.ic_shortcut_safe,
                ),
            )
        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }

    fun reportUsed(context: Context, target: LaunchTarget) {
        ShortcutManagerCompat.reportShortcutUsed(context, target.name)
    }

    private fun shortcut(
        context: Context,
        target: LaunchTarget,
        @StringRes label: Int,
        @DrawableRes icon: Int,
    ): ShortcutInfoCompat =
        ShortcutInfoCompat.Builder(context, target.name)
            .setShortLabel(context.getString(label))
            .setIcon(IconCompat.createWithResource(context, icon))
            .setIntent(Intent(context, MainActivity::class.java).setAction(target.action))
            .build()
}
