// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.content.Context
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.StreamingAead
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TinkSafeCipher
@Inject
constructor(@ApplicationContext private val context: Context) : SafeCipher {
    private val aead: StreamingAead by lazy {
        StreamingAeadConfig.register()
        AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREFERENCES_FILE)
            .withKeyTemplate(KeyTemplates.get(KEY_TEMPLATE))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), StreamingAead::class.java)
    }

    override fun encrypting(output: OutputStream, associatedData: ByteArray): OutputStream =
        aead.newEncryptingStream(output, associatedData)

    override fun decrypting(input: InputStream, associatedData: ByteArray): InputStream =
        aead.newDecryptingStream(input, associatedData)

    companion object {
        const val PREFERENCES_FILE = "safe_folder_keyset"
        private const val KEYSET_NAME = "keyset"
        private const val KEY_TEMPLATE = "AES256_GCM_HKDF_4KB"
        private const val MASTER_KEY_URI = "android-keystore://files_safe_folder"
    }
}
