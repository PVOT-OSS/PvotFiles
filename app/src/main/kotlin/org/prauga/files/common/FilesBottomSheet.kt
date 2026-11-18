// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.prauga.pvot.designsystem.components.PvotBottomSheet

@Composable
fun FilesBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String?,
    content: @Composable ColumnScope.() -> Unit,
) {
    var generation by remember { mutableIntStateOf(0) }
    // PvotBottomSheet 1.0.0 leaves an invisible window if its hide is interrupted, so remount it.
    key(generation) {
        PvotBottomSheet(
            visible = visible,
            onDismissRequest = { if (visible) onDismissRequest() else generation++ },
            title = title,
            content = content,
        )
    }
}
