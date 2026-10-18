package ie.suric.suricapp.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Copies selected photos into encrypted app-private storage; picker URIs are never persisted. */
class PhotoAttachmentStore(private val context: Context) {
    private val directory = File(context.filesDir, "personal_photos").apply { mkdirs() }

    fun copyFromPicker(uri: Uri): String {
        val type = context.contentResolver.getType(uri).orEmpty()
        require(type.startsWith("image/")) { "Choose an image file." }
        val name = "${UUID.randomUUID()}.simg"
        val target = file(name)
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, secretKey()) }
            target.outputStream().buffered().use { raw ->
                raw.write(cipher.iv)
                CipherOutputStream(raw, cipher).use { encrypted ->
                    val input = context.contentResolver.openInputStream(uri) ?: error("The selected image could not be opened.")
                    input.use { source ->
                        val buffer = ByteArray(16 * 1024)
                        var total = 0
                        while (true) {
                            val count = source.read(buffer)
                            if (count < 0) break
                            total += count
                            require(total <= MAX_IMAGE_BYTES) { "Choose an image smaller than 8 MB." }
                            encrypted.write(buffer, 0, count)
                        }
                        require(total > 0) { "The selected image is empty." }
                    }
                }
            }
            return name
        } catch (failure: Exception) {
            target.delete()
            throw failure
        }
    }

    fun delete(name: String?) {
        if (name != null && isSafeName(name)) file(name).delete()
    }

    /** Removes orphaned temporary or deleted attachments after the durable record snapshot is loaded. */
    fun removeOrphans(referencedNames: Set<String>) {
        directory.listFiles()?.forEach { candidate ->
            if (isSafeName(candidate.name) && candidate.name !in referencedNames) candidate.delete()
        }
    }

    fun thumbnail(name: String): Bitmap? {
        if (!isSafeName(name)) return null
        val bytes = runCatching {
            val target = file(name)
            target.inputStream().buffered().use { raw ->
                val iv = ByteArray(12)
                var offset = 0
                while (offset < iv.size) {
                    val read = raw.read(iv, offset, iv.size - offset)
                    if (read < 0) return null
                    offset += read
                }
                val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv)) }
                CipherInputStream(raw, cipher).use { input ->
                    val out = ByteArrayOutputStream()
                    val buffer = ByteArray(16 * 1024)
                    var total = 0
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > MAX_IMAGE_BYTES) error("Saved photo exceeds the allowed size.")
                        out.write(buffer, 0, count)
                    }
                    out.toByteArray()
                }
            }
        }.getOrNull() ?: return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / sample > 640 || bounds.outHeight / sample > 640) sample *= 2
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    private fun file(name: String): File {
        require(isSafeName(name)) { "Invalid saved photo reference." }
        return File(directory, name)
    }

    private fun isSafeName(name: String): Boolean = name.matches(Regex("[a-f0-9-]{36}\\.simg"))

    private fun secretKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build())
        }.generateKey()
    }

    companion object {
        private const val KEY_ALIAS = "suric_personal_photo_v1"
        private const val MAX_IMAGE_BYTES = 8 * 1024 * 1024
    }
}
