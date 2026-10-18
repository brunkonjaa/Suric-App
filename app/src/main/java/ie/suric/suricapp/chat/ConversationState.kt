package ie.suric.suricapp.chat

data class ConversationState(
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val modelLabel: String? = null,
    val historyNotice: String? = null
)

object ConversationReducer {
    fun appendUser(state: ConversationState, text: String, imageUri: String? = null): ConversationState = state.copy(
        messages = state.messages + ChatMessage("user", text, imageUri = imageUri), draft = "", isLoading = true, error = null
    )
    fun appendDelta(state: ConversationState, delta: String): ConversationState {
        val messages = state.messages.toMutableList()
        if (messages.lastOrNull()?.role == "assistant") {
            messages[messages.lastIndex] = messages.last().copy(text = messages.last().text + delta)
        } else messages += ChatMessage("assistant", delta)
        return state.copy(messages = messages)
    }
    fun completed(state: ConversationState): ConversationState {
        val messages = state.messages.toMutableList()
        if (messages.lastOrNull()?.role == "assistant") messages[messages.lastIndex] = messages.last().copy(completed = true)
        return state.copy(messages = messages, isLoading = false, error = null)
    }
    fun failed(state: ConversationState, message: String): ConversationState = state.copy(isLoading = false, error = message)
}
