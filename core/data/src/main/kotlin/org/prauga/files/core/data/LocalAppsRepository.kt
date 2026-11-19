// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Process
import android.os.storage.StorageManager
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.time.Clock
import java.time.Duration
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.UnusedApp

internal class LocalAppsRepository
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val clock: Clock,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : AppsRepository {

    override fun hasUsageAccess(): Boolean {
        val ops = context.getSystemService(AppOpsManager::class.java)
        val mode =
            ops.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override suspend fun unusedApps(): List<UnusedApp>? =
        withContext(dispatcher) {
            if (!hasUsageAccess()) return@withContext null
            val packages = context.packageManager
            val now = clock.millis()
            val launchable =
                packages
                    .queryIntentActivities(
                        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
                        0,
                    )
                    .map { it.activityInfo.applicationInfo }
                    .distinctBy { it.packageName }
                    .filter {
                        it.flags and ApplicationInfo.FLAG_SYSTEM == 0 &&
                            it.packageName != context.packageName
                    }
            val installed =
                launchable.mapNotNull { app ->
                    try {
                        UnusedApps.Installed(
                            app.packageName,
                            packages.getPackageInfo(app.packageName, 0).firstInstallTime,
                        )
                    } catch (e: PackageManager.NameNotFoundException) {
                        null
                    }
                }
            val usage = context.getSystemService(UsageStatsManager::class.java)
            val lastUsed =
                usage.queryAndAggregateUsageStats(now - HISTORY.toMillis(), now).mapValues {
                    it.value.lastTimeUsed
                }
            val unused = UnusedApps.select(installed, lastUsed, now, UNUSED_FOR.toMillis())
            val storage = context.getSystemService(StorageStatsManager::class.java)
            launchable
                .filter { it.packageName in unused }
                .map { app ->
                    UnusedApp(
                        packageName = app.packageName,
                        label = app.loadLabel(packages).toString(),
                        lastUsedMillis = unused[app.packageName],
                        sizeBytes = sizeOf(storage, app),
                    )
                }
                .sortedByDescending { it.sizeBytes }
        }

    override suspend fun icon(packageName: String, sizePx: Int): Bitmap? =
        withContext(dispatcher) {
            try {
                context.packageManager.getApplicationIcon(packageName).toBitmap(sizePx, sizePx)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }

    private fun sizeOf(storage: StorageStatsManager, app: ApplicationInfo): Long =
        try {
            val stats =
                storage.queryStatsForPackage(
                    StorageManager.UUID_DEFAULT,
                    app.packageName,
                    Process.myUserHandle(),
                )
            stats.appBytes + stats.dataBytes + stats.cacheBytes
        } catch (e: IOException) {
            0L
        } catch (e: PackageManager.NameNotFoundException) {
            0L
        } catch (e: SecurityException) {
            0L
        }

    private companion object {
        val UNUSED_FOR: Duration = Duration.ofDays(30)
        val HISTORY: Duration = Duration.ofDays(365)
    }
}
