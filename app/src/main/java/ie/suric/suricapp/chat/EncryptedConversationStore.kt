package ie.suric.suricapp.chat

import android.content.Context
import android.util.Base64
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.json.JSONArray
import org.json.JSONObject

data class SavedConversation(val messages: List<ChatMessage>, val draft: String)

/** Encrypted, app-private conversation history, separate from user-authored personal entries. */
class EncryptedConversationStore(context: Context) {
    private val preferences = context.getSharedPreferences("suric_chat_history", Context.MODE_PRIVATE)

    @Synchronized
    fun load(): SavedConversation? {
        val packed = preferences.getString(KEY, null) ?: return null
        val bytes = Base64.decode(packed, Base64.NO_WRAP)
        require(bytes.size > IV_BYTES) { "The saved conversation could not be read. It has not been changed." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, bytes.copyOfRange(0, IV_BYTES)))
        val root = JSONObject(String(cipher.doFinal(bytes.copyOfRange(IV_BYTES, bytes.size)), Charsets.UTF_8))
        require(root.optInt("version") == VERSION) { "This saved conversation version is not supported. It has not been changed." }
        val array = root.getJSONArray("messages")
        val messages = (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val role = item.getString("role")
            require(role == "user" || role == "assistant") { "The saved conversation contains an invalid message. It has not been changed." }
            ChatMessage(role, item.getString("text"), item.optBoolean("completed"), (item.opt("imageUri") as? String)?.takeIf(String::isNotBlank))
        }
        return SavedConversation(messages, root.optString("draft"))
    }

    @Synchronized
    fun save(messages: List<ChatMessage>, draft: String) {
        require(messages.all { it.role == "user" || it.role == "assistant" })
        val encoded = JSONObject().put("version", VERSION).put("draft", draft).put("messages", JSONArray().apply {
            messages.forEach { message ->
                put(JSONObject().put("role", message.role).put("text", message.text).put("completed", message.completed).put("imageUri", message.imageUri ?: JSONObject.NULL))
            }
        }).toString().toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val packed = cipher.iv + cipher.doFinal(encoded)
        check(preferences.edit().putString(KEY, Base64.encodeToString(packed, Base64.NO_WRAP)).commit()) {
            "Could not save conversation history on this device."
        }
    }

    @Synchronized
    fun clear() {
        check(preferences.edit().remove(KEY).commit()) { "Could not delete conversation history from this device." }
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build())
        }.generateKey()
    }

    private companion object {
        const val KEY = "encrypted_messages"
        const val KEY_ALIAS = "suric_chat_history_v1"
        const val VERSION = 1
        const val IV_BYTES = 12
        const val TAG_BITS = 128
    }
}
