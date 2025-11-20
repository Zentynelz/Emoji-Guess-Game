package com.example.emojiguess.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emojiguess.models.Message
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

/**
 * ViewModel responsable de escuchar y publicar mensajes del chat global de una sala.
 */
class ChatViewModel(
    private val roomId: String,
    private val playerId: String,
    private val playerName: String,
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private val _isSending = MutableLiveData(false)
    val isSending: LiveData<Boolean> = _isSending

    private val _inputEnabled = MediatorLiveData<Boolean>().apply {
        value = true
        addSource(_isSending) { value = !(it ?: false) }
    }
    val inputEnabled: LiveData<Boolean> = _inputEnabled

    private var messagesListener: ChildEventListener? = null

    init {
        attachListener()
    }

    private fun attachListener() {
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java) ?: return
                val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
                if (currentMessages.none { it.id == message.id }) {
                    currentMessages.add(message)
                    _messages.postValue(currentMessages.sortedBy { it.timestamp })
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onChildRemoved(snapshot: DataSnapshot) = Unit
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onCancelled(error: DatabaseError) = Unit
        }
        database.child("messages").child(roomId).addChildEventListener(listener)
        messagesListener = listener
    }

    fun sendMessage(rawContent: String) {
        val content = rawContent.trim()
        if (content.isEmpty() || _isSending.value == true) return

        val messageId = database.child("messages").child(roomId).push().key ?: return
        val message = Message(
            id = messageId,
            playerId = playerId,
            playerName = playerName,
            text = content,
            timestamp = System.currentTimeMillis()
        )
        _isSending.value = true
        database.child("messages").child(roomId).child(messageId)
            .setValue(message.toMap())
            .addOnCompleteListener {
                _isSending.postValue(false)
            }
    }

    override fun onCleared() {
        super.onCleared()
        messagesListener?.let {
            database.child("messages").child(roomId).removeEventListener(it)
        }
    }

    class Factory(
        private val roomId: String,
        private val playerId: String,
        private val playerName: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(roomId, playerId, playerName) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}