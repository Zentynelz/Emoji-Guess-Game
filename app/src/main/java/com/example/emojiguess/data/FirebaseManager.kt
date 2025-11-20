package com.example.emojiguess.data

import com.example.emojiguess.models.Game
import com.example.emojiguess.models.Message
import com.example.emojiguess.models.Player
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Gestor de Firebase para el juego Emoji Guess
 * Maneja todas las operaciones de Firebase Realtime Database y Authentication
 */
class FirebaseManager private constructor() {
    
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val gamesRef: DatabaseReference = database.getReference("games")
    private val messagesRef: DatabaseReference = database.getReference("messages")
    
    companion object {
        @Volatile
        private var instance: FirebaseManager? = null
        
        fun getInstance(): FirebaseManager {
            return instance ?: synchronized(this) {
                instance ?: FirebaseManager().also { instance = it }
            }
        }
    }
    
    /**
     * Autentica al usuario de forma anónima
     * @return ID del usuario autenticado
     */
    suspend fun authenticateAnonymously(): String {
        val result = auth.signInAnonymously().await()
        return result.user?.uid ?: throw Exception("Error al autenticar usuario")
    }
    
    /**
     * Obtiene el ID del usuario actual
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Genera un código de sala único de 6 caracteres
     */
    fun generateRoomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
    
    /**
     * Crea una nueva sala de juego
     * @param hostId ID del jugador host
     * @param hostName Nombre del host
     * @return Código de la sala creada
     */
    suspend fun createRoom(hostId: String, hostName: String): String {
        val roomCode = generateRoomCode()
        val host = Player(
            id = hostId,
            name = hostName,
            isHost = true,
            isAlive = true
        )
        
        val game = Game(
            roomCode = roomCode,
            hostId = hostId,
            players = mapOf(hostId to host)
        )
        
        gamesRef.child(roomCode).setValue(game.toMap()).await()
        return roomCode
    }
    
    /**
     * Une a un jugador a una sala existente
     * @param roomCode Código de la sala
     * @param playerId ID del jugador
     * @param playerName Nombre del jugador
     */
    suspend fun joinRoom(roomCode: String, playerId: String, playerName: String): Boolean {
        val gameSnapshot = gamesRef.child(roomCode).get().await()
        
        if (!gameSnapshot.exists()) {
            return false
        }
        
        val player = Player(
            id = playerId,
            name = playerName,
            isHost = false,
            isAlive = true
        )
        
        gamesRef.child(roomCode).child("players").child(playerId)
            .setValue(player.toMap()).await()
        
        return true
    }
    
    /**
     * Observa los cambios en una sala de juego
     * @param roomCode Código de la sala
     * @return Flow que emite el estado actualizado del juego
     */
    fun observeGame(roomCode: String): Flow<Game?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val game = snapshot.value?.let { value ->
                    @Suppress("UNCHECKED_CAST")
                    Game.fromMap(value as Map<String, Any>)
                }
                trySend(game)
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        val ref = gamesRef.child(roomCode)
        ref.addValueEventListener(listener)
        
        awaitClose { ref.removeEventListener(listener) }
    }
    
    /**
     * Actualiza el estado del juego
     */
    suspend fun updateGameState(roomCode: String, state: String) {
        gamesRef.child(roomCode).child("state").setValue(state).await()
    }
    
    /**
     * Actualiza los jugadores del juego
     */
    suspend fun updatePlayers(roomCode: String, players: Map<String, Player>) {
        val playersMap = players.mapValues { it.value.toMap() }
        gamesRef.child(roomCode).child("players").setValue(playersMap).await()
    }
    
    /**
     * Actualiza el turno actual
     */
    suspend fun updateCurrentTurn(roomCode: String, playerId: String, roundStartTime: Long) {
        val updates = mapOf(
            "currentTurnPlayerId" to playerId,
            "roundStartTime" to roundStartTime
        )
        gamesRef.child(roomCode).updateChildren(updates).await()
    }
    
    /**
     * Actualiza la ronda actual
     */
    suspend fun updateRound(roomCode: String, round: Int) {
        gamesRef.child(roomCode).child("currentRound").setValue(round).await()
    }
    
    /**
     * Marca a un jugador como eliminado
     */
    suspend fun eliminatePlayer(roomCode: String, playerId: String) {
        gamesRef.child(roomCode).child("players").child(playerId).child("isAlive")
            .setValue(false).await()
    }
    
    /**
     * Establece el ganador del juego
     */
    suspend fun setWinner(roomCode: String, winnerId: String) {
        gamesRef.child(roomCode).child("winnerId").setValue(winnerId).await()
    }
    
    /**
     * Envía un mensaje al chat
     */
    suspend fun sendMessage(roomCode: String, message: Message) {
        messagesRef.child(roomCode).push().setValue(message.toMap()).await()
    }
    
    /**
     * Observa los mensajes del chat
     */
    fun observeMessages(roomCode: String): Flow<List<Message>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                snapshot.children.forEach { child ->
                    child.value?.let { value ->
                        @Suppress("UNCHECKED_CAST")
                        messages.add(Message.fromMap(value as Map<String, Any>))
                    }
                }
                trySend(messages.sortedBy { it.timestamp })
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        val ref = messagesRef.child(roomCode)
        ref.addValueEventListener(listener)
        
        awaitClose { ref.removeEventListener(listener) }
    }
    
    /**
     * Elimina una sala del juego
     */
    suspend fun deleteRoom(roomCode: String) {
        gamesRef.child(roomCode).removeValue().await()
        messagesRef.child(roomCode).removeValue().await()
    }
    
    /**
     * Sale de una sala (elimina al jugador)
     */
    suspend fun leaveRoom(roomCode: String, playerId: String) {
        gamesRef.child(roomCode).child("players").child(playerId).removeValue().await()
    }
    
    /**
     * Marca a un jugador como que ya jugó en esta ronda
     */
    suspend fun markPlayerAsPlayed(roomCode: String, playerId: String) {
        val gameRef = gamesRef.child(roomCode)
        val snapshot = gameRef.child("playersWhoPlayedThisRound").get().await()
        
        @Suppress("UNCHECKED_CAST")
        val currentPlayers = (snapshot.value as? List<*>)?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
        
        if (!currentPlayers.contains(playerId)) {
            currentPlayers.add(playerId)
            gameRef.child("playersWhoPlayedThisRound").setValue(currentPlayers).await()
        }
    }
    
    /**
     * Limpia la lista de jugadores que jugaron en esta ronda
     */
    suspend fun clearPlayersWhoPlayed(roomCode: String) {
        gamesRef.child(roomCode).child("playersWhoPlayedThisRound").setValue(emptyList<String>()).await()
    }
}
