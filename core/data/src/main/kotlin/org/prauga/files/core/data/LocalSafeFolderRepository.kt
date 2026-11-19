// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import java.time.Clock
import java.util.Base64
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.SafeItem
import org.prauga.files.core.model.UnlockResult

@Singleton
internal class LocalSafeFolderRepository
@Inject
constructor(
    @ApplicationContext private val context: Context,
    @SafeFolderStore private val store: DataStore<Preferences>,
    private val cipher: SafeCipher,
    private val mediaIndex: MediaIndex,
    private val clock: Clock,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : SafeFolderRepository {
    private val lock = Mutex()
    private val folder: File
        get() = File(context.filesDir, FOLDER)

    private val openedFolder: File
        get() = File(context.cacheDir, OPENED_FOLDER)

    override val hasPin: Flow<Boolean> = store.data.map { it[KEY_HASH] != null }

    override suspend fun setPin(pin: String): Boolean {
        if (!PinPolicy.isAcceptable(pin)) return false
        val salt = PinHasher.newSalt()
        val hash = withContext(dispatcher) { PinHasher.hash(pin, salt) }
        store.edit {
            it[KEY_SALT] = encoder.encodeToString(salt)
            it[KEY_HASH] = encoder.encodeToString(hash)
            it[KEY_FAILURES] = 0
            it[KEY_LOCKED_UNTIL] = 0L
        }
        return true
    }

    override suspend fun unlock(pin: String): UnlockResult {
        val preferences = store.data.first()
        val now = clock.millis()
        val lockedUntil = preferences[KEY_LOCKED_UNTIL] ?: 0L
        if (lockedUntil > now) return UnlockResult.LockedOut(lockedUntil)
        val salt = preferences[KEY_SALT]?.let(decoder::decode) ?: return UnlockResult.Wrong(0)
        val hash = preferences[KEY_HASH]?.let(decoder::decode) ?: return UnlockResult.Wrong(0)
        if (withContext(dispatcher) { PinHasher.matches(pin, salt, hash) }) {
            store.edit { it[KEY_FAILURES] = 0 }
            return UnlockResult.Unlocked
        }
        val failures = (preferences[KEY_FAILURES] ?: 0) + 1
        val lockout = PinPolicy.lockoutMillis(failures)
        store.edit {
            it[KEY_FAILURES] = failures
            it[KEY_LOCKED_UNTIL] = if (lockout > 0) now + lockout else 0L
        }
        return if (lockout > 0) UnlockResult.LockedOut(now + lockout)
        else UnlockResult.Wrong(PinPolicy.FREE_ATTEMPTS - failures)
    }

    override suspend fun items(): List<SafeItem>? =
        withContext(dispatcher) { lock.withLock { readIndex() } }

    override suspend fun add(paths: List<String>): FileOperationResult =
        withContext(dispatcher) {
            lock.withLock {
                val items = readIndex() ?: return@withLock FileOperationResult.Failed
                val sources = paths.map(::File).filter { it.isFile }
                val added = mutableListOf<Pair<File, SafeItem>>()
                for (source in sources) {
                    val item =
                        SafeItem(
                            UUID.randomUUID().toString(),
                            source.name,
                            source.length(),
                            clock.millis(),
                        )
                    val target = dataFile(item.id)
                    try {
                        folder.mkdirs()
                        source.inputStream().use { input ->
                            cipher.encrypting(target.outputStream(), item.id.toByteArray()).use {
                                input.copyTo(it)
                            }
                        }
                    } catch (e: IOException) {
                        target.delete()
                        continue
                    } catch (e: GeneralSecurityException) {
                        target.delete()
                        continue
                    }
                    added += source to item
                }
                if (added.isEmpty()) return@withLock FileOperationResult.Failed
                if (!writeIndex(items + added.map { it.second })) {
                    added.forEach { dataFile(it.second.id).delete() }
                    return@withLock FileOperationResult.Failed
                }
                val moved = added.map { it.first }.filter { it.delete() }
                mediaIndex.update(moved.map { it.path })
                if (added.size == paths.size) FileOperationResult.Done
                else FileOperationResult.Failed
            }
        }

    override suspend fun moveOut(id: String, folder: String): FileOperationResult =
        withContext(dispatcher) {
            lock.withLock {
                val items = readIndex() ?: return@withLock FileOperationResult.Failed
                val item =
                    items.firstOrNull { it.id == id } ?: return@withLock FileOperationResult.Failed
                val parent = File(folder)
                val target =
                    File(parent, TransferEngine.uniqueName(parent, item.name, isDirectory = false))
                if (!decryptTo(item, target)) return@withLock FileOperationResult.Failed
                if (!writeIndex(items - item)) {
                    target.delete()
                    return@withLock FileOperationResult.Failed
                }
                dataFile(item.id).delete()
                mediaIndex.update(listOf(target.path))
                FileOperationResult.Done
            }
        }

    override suspend fun open(id: String): String? =
        withContext(dispatcher) {
            lock.withLock {
                val item = readIndex()?.firstOrNull { it.id == id } ?: return@withLock null
                val target = File(File(openedFolder, item.id), item.name)
                target.parentFile?.mkdirs()
                if (decryptTo(item, target)) target.path else null
            }
        }

    override suspend fun delete(id: String): FileOperationResult =
        withContext(dispatcher) {
            lock.withLock {
                val items = readIndex() ?: return@withLock FileOperationResult.Failed
                val item =
                    items.firstOrNull { it.id == id } ?: return@withLock FileOperationResult.Failed
                if (!writeIndex(items - item)) return@withLock FileOperationResult.Failed
                dataFile(item.id).delete()
                FileOperationResult.Done
            }
        }

    override suspend fun clearOpened() {
        withContext(dispatcher) { openedFolder.deleteRecursively() }
    }

    private fun decryptTo(item: SafeItem, target: File): Boolean =
        try {
            cipher.decrypting(dataFile(item.id).inputStream(), item.id.toByteArray()).use { input ->
                target.outputStream().use { input.copyTo(it) }
            }
            true
        } catch (e: IOException) {
            target.delete()
            false
        } catch (e: GeneralSecurityException) {
            target.delete()
            false
        }

    private fun readIndex(): List<SafeItem>? {
        val index = File(folder, INDEX_FILE)
        if (!index.exists()) return emptyList()
        return try {
            cipher.decrypting(index.inputStream(), INDEX_DATA).use {
                SafeIndexCodec.decode(it.readBytes().decodeToString())
            }
        } catch (e: IOException) {
            null
        } catch (e: GeneralSecurityException) {
            null
        }
    }

    private fun writeIndex(items: List<SafeItem>): Boolean {
        folder.mkdirs()
        val temporary = File(folder, "$INDEX_FILE.tmp")
        return try {
            cipher.encrypting(temporary.outputStream(), INDEX_DATA).use {
                it.write(SafeIndexCodec.encode(items).encodeToByteArray())
            }
            temporary.renameTo(File(folder, INDEX_FILE))
        } catch (e: IOException) {
            temporary.delete()
            false
        } catch (e: GeneralSecurityException) {
            temporary.delete()
            false
        }
    }

    private fun dataFile(id: String): File = File(folder, "$id$DATA_SUFFIX")

    private companion object {
        const val FOLDER = "safe"
        const val OPENED_FOLDER = "safe-open"
        const val INDEX_FILE = "index"
        const val DATA_SUFFIX = ".bin"
        val INDEX_DATA = "index".toByteArray()
        val KEY_HASH = stringPreferencesKey("pin_hash")
        val KEY_SALT = stringPreferencesKey("pin_salt")
        val KEY_FAILURES = intPreferencesKey("failures")
        val KEY_LOCKED_UNTIL = longPreferencesKey("locked_until")
        val encoder: Base64.Encoder = Base64.getEncoder()
        val decoder: Base64.Decoder = Base64.getDecoder()
    }
}
