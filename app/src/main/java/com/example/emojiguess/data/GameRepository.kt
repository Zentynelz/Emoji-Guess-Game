package com.example.emojiguess.data

import com.example.emojiguess.logic.GameEngine
import com.example.emojiguess.models.Game
import com.example.emojiguess.models.Message
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que centraliza el acceso a datos del juego
 * Actúa como intermediario entre la UI y Firebase
 */
class GameRepository private constructor(
    private val firebaseManager: FirebaseManager
) {
    
    private val gameEngine = GameEngine(firebaseManager)
    
    companion object {
        @Volatile
        private var instance: GameRepository? = null
        
        fun getInstance(): GameRepository {
            return instance ?: synchronized(this) {
                val firebaseManager = FirebaseManager.getInstance()
                instance ?: GameRepository(firebaseManager).also { instance = it }
            }
        }
    }
    
    /**
     * Autentica al usuario
     */
    suspend fun authenticateUser(): String {
        return firebaseManager.authenticateAnonymously()
    }
    
    /**
     * Obtiene el ID del usuario actual
     */
    fun getCurrentUserId(): String? {
        return firebaseManager.getCurrentUserId()
    }
    
    /**
     * Crea una nueva sala
     */
    suspend fun createRoom(hostName: String): String {
        val userId = getCurrentUserId() ?: throw Exception("Usuario no autenticado")
        return firebaseManager.createRoom(userId, hostName)
    }
    
    /**
     * Une a un jugador a una sala
     */
    suspend fun joinRoom(roomCode: String, playerName: String): Boolean {
        val userId = getCurrentUserId() ?: throw Exception("Usuario no autenticado")
        return firebaseManager.joinRoom(roomCode, userId, playerName)
    }
    
    /**
     * Observa los cambios en el juego
     */
    fun observeGame(roomCode: String): Flow<Game?> {
        return firebaseManager.observeGame(roomCode)
    }
    
    /**
     * Inicia el juego
     */
    suspend fun startGame(roomCode: String) {
        gameEngine.startGame(roomCode)
    }
    
    /**
     * Asigna emojis a los jugadores
     */
    suspend fun assignEmojis(game: Game) {
        gameEngine.assignEmojisToPlayers(game)
    }
    
    /**
     * Inicia el turno de un jugador
     */
    suspend fun startPlayerTurn(roomCode: String, playerId: String) {
        gameEngine.startPlayerTurn(roomCode, playerId)
    }
    
    /**
     * Procesa la respuesta de un jugador
     */
    suspend fun submitAnswer(game: Game, selectedEmoji: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        return gameEngine.processPlayerAnswer(game, userId, selectedEmoji)
    }
    
    /**
     * Elimina a un jugador por timeout
     */
    suspend fun eliminateByTimeout(roomCode: String, playerId: String) {
        gameEngine.eliminatePlayerByTimeout(roomCode, playerId)
    }
    
    /**
     * Verifica si el juego terminó
     */
    fun isGameOver(game: Game): Boolean {
        return gameEngine.checkGameOver(game)
    }
    
    /**
     * Finaliza el juego
     */
    suspend fun finishGame(game: Game) {
        gameEngine.finishGame(game)
    }
    
    /**
     * Finaliza la ronda actual
     */
    suspend fun endRound(game: Game) {
        gameEngine.endRound(game)
    }
    
    /**
     * Obtiene el tiempo restante del turno
     */
    fun getRemainingTime(game: Game): Int {
        return gameEngine.getRemainingTime(game)
    }
    
    /**
     * Verifica si el turno expiró
     */
    fun isTurnExpired(game: Game): Boolean {
        return gameEngine.isTurnExpired(game)
    }
    
    /**
     * Obtiene el siguiente jugador
     */
    fun getNextPlayer(game: Game, currentPlayerId: String): String? {
        return gameEngine.getNextPlayer(game, currentPlayerId)
    }
    
    /**
     * Envía un mensaje al chat
     */
    suspend fun sendMessage(roomCode: String, text: String) {
        val userId = getCurrentUserId() ?: return
        val game = observeGame(roomCode)
        
        // Nota: En implementación real, deberías obtener el nombre del jugador del Game
        val message = Message(
            id = System.currentTimeMillis().toString(),
            playerId = userId,
            playerName = "Player", // Obtener del Game
            text = text,
            timestamp = System.currentTimeMillis()
        )
        
        firebaseManager.sendMessage(roomCode, message)
    }
    
    /**
     * Observa los mensajes del chat
     */
    fun observeMessages(roomCode: String): Flow<List<Message>> {
        return firebaseManager.observeMessages(roomCode)
    }
    
    /**
     * Sale de la sala
     */
    suspend fun leaveRoom(roomCode: String) {
        val userId = getCurrentUserId() ?: return
        firebaseManager.leaveRoom(roomCode, userId)
    }
    
    /**
     * Elimina la sala (solo el host)
     */
    suspend fun deleteRoom(roomCode: String) {
        firebaseManager.deleteRoom(roomCode)
    }
    
    /**
     * Marca a un jugador como que ya jugó en esta ronda
     */
    suspend fun markPlayerAsPlayed(roomCode: String, playerId: String) {
        firebaseManager.markPlayerAsPlayed(roomCode, playerId)
    }
}
