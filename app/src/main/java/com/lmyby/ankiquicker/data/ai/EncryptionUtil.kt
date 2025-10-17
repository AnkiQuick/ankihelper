package com.lmyby.ankiquicker.data.ai

import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
object EncryptionUtil {
    private const val ALGORITHM = "AES"
    private const val KEY = "AnkiHelperAIKey1" // 16 characters for 128-bit key

    @JvmStatic
    fun encrypt(value: String): String? {
        return try {
            val key: Key = SecretKeySpec(KEY.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedValue = cipher.doFinal(value.toByteArray())
            Base64.encodeToString(encryptedValue, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun decrypt(encryptedValue: String): String? {
        return try {
            val key: Key = SecretKeySpec(KEY.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decryptedValue = cipher.doFinal(Base64.decode(encryptedValue, Base64.DEFAULT))
            String(decryptedValue)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
