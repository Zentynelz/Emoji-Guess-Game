package com.example.emojiguess.logic

import com.example.emojiguess.data.FirebaseManager
import com.example.emojiguess.models.Game
import com.example.emojiguess.models.GameState
import com.example.emojiguess.models.Player
import com.example.emojiguess.utils.Constants
import kotlinx.coroutines.delay

/**
 * Motor del juego que maneja toda la lógica de Emoji Guess
 */
class GameEngine(private val firebaseManager: FirebaseManager) {
    
    /**
     * Inicia una nueva partida
     * @param roomCode Código de la sala
     */
    suspend fun startGame(roomCode: String) {
        // Cambiar estado a STARTING
        firebaseManager.updateGameState(roomCode, GameState.STARTING.name)
        
        // Pequeña pausa para que todos vean que el juego está iniciando
        delay(Constants.GAME_START_DELAY)
        
        // Iniciar primera ronda
        startNewRound(roomCode, 1)
    }
    
    /**
     * Inicia una nueva ronda del juego
     * @param roomCode Código de la sala
     * @param roundNumber Número de ronda
     */
    suspend fun startNewRound(roomCode: String, roundNumber: Int) {
        // Obtener el juego actual
        val gameSnapshot = firebaseManager.observeGame(roomCode)
        
        // Nota: En una implementación real, deberías obtener el snapshot actual
        // Por ahora, asumimos que tienes acceso al Game actual
        
        // Actualizar número de ronda
        firebaseManager.updateRound(roomCode, roundNumber)
        
        // Cambiar estado a IN_PROGRESS
        firebaseManager.updateGameState(roomCode, GameState.IN_PROGRESS.name)
    }
    
    /**
     * Asigna emojis aleatorios a todos los jugadores vivos
     * @param game Estado actual del juego
     */
    suspend fun assignEmojisToPlayers(game: Game) {
        val alivePlayers = game.getAlivePlayers()
        
        if (alivePlayers.isEmpty()) return
        
        // Obtener emojis únicos para cada jugador
        val emojis = EmojiManager.assignEmojis(alivePlayers.size)
        
        // Crear mapa actualizado de jugadores con nuevos emojis
        val updatedPlayers = game.players.toMutableMap()
        
        alivePlayers.forEachIndexed { index, player ->
            updatedPlayers[player.id] = player.copy(emoji = emojis[index])
        }
        
        // Actualizar en Firebase
        firebaseManager.updatePlayers(game.roomCode, updatedPlayers)
    }
    
    /**
     * Inicia el turno de un jugador
     * @param roomCode Código de la sala
     * @param playerId ID del jugador
     */
    suspend fun startPlayerTurn(roomCode: String, playerId: String) {
        val currentTime = System.currentTimeMillis()
        firebaseManager.updateCurrentTurn(roomCode, playerId, currentTime)
    }
    
    /**
     * Obtiene el siguiente jugador vivo en orden
     * @param game Estado actual del juego
     * @param currentPlayerId ID del jugador actual
     * @return ID del siguiente jugador o null si no hay más
     */
    fun getNextPlayer(game: Game, currentPlayerId: String): String? {
        val alivePlayers = game.getAlivePlayers()
        
        if (alivePlayers.isEmpty()) return null
        if (alivePlayers.size == 1) return alivePlayers.first().id
        
        val currentIndex = alivePlayers.indexOfFirst { it.id == currentPlayerId }
        
        return if (currentIndex == -1 || currentIndex == alivePlayers.size - 1) {
            alivePlayers.first().id
        } else {
            alivePlayers[currentIndex + 1].id
        }
    }
    
    /**
     * Valida la respuesta de un jugador
     * @param game Estado actual del juego
     * @param playerId ID del jugador que responde
     * @param selectedEmoji Emoji seleccionado por el jugador
     * @return true si la respuesta es correcta, false si es incorrecta
     */
    fun validateAnswer(game: Game, playerId: String, selectedEmoji: String): Boolean {
        val player = game.players[playerId] ?: return false
        return player.emoji == selectedEmoji
    }
    
    /**
     * Procesa la respuesta de un jugador
     * @param game Estado actual del juego
     * @param playerId ID del jugador
     * @param selectedEmoji Emoji seleccionado
     * @return true si acertó, false si falló
     */
    suspend fun processPlayerAnswer(
        game: Game,
        playerId: String,
        selectedEmoji: String
    ): Boolean {
        val isCorrect = validateAnswer(game, playerId, selectedEmoji)
        
        if (!isCorrect) {
            // Eliminar al jugador
            firebaseManager.eliminatePlayer(game.roomCode, playerId)
        }
        
        return isCorrect
    }
    
    /**
     * Elimina a un jugador por timeout (no respondió a tiempo)
     * @param roomCode Código de la sala
     * @param playerId ID del jugador
     */
    suspend fun eliminatePlayerByTimeout(roomCode: String, playerId: String) {
        firebaseManager.eliminatePlayer(roomCode, playerId)
    }
    
    /**
     * Verifica si el juego ha terminado
     * @param game Estado actual del juego
     * @return true si no quedan jugadores vivos (todos perdieron)
     */
    fun checkGameOver(game: Game): Boolean {
        val alivePlayers = game.getAlivePlayers()
        // El juego solo termina si NO quedan jugadores vivos (todos perdieron)
        // Si queda 1 jugador, ese es el ganador pero el juego continúa hasta que todos jueguen
        return alivePlayers.isEmpty()
    }
    
    /**
     * Finaliza el juego y establece al ganador
     * @param game Estado actual del juego
     */
    suspend fun finishGame(game: Game) {
        val alivePlayers = game.getAlivePlayers()
        
        // Si queda exactamente 1 jugador, ese es el ganador
        if (alivePlayers.size == 1) {
            val winner = alivePlayers.first()
            firebaseManager.setWinner(game.roomCode, winner.id)
        }
        // Si no queda nadie (todos fallaron), no hay ganador
        else if (alivePlayers.isEmpty()) {
            firebaseManager.setWinner(game.roomCode, "")
        }
        
        firebaseManager.updateGameState(game.roomCode, GameState.FINISHED.name)
    }
    
    /**
     * Verifica si es necesario iniciar una nueva ronda
     * @param game Estado actual del juego
     * @return true si se debe iniciar nueva ronda
     */
    fun shouldStartNewRound(game: Game): Boolean {
        // Si todos los jugadores vivos ya jugaron su turno en esta ronda
        val alivePlayers = game.getAlivePlayers()
        return alivePlayers.size > 1
    }
    
    /**
     * Finaliza la ronda actual y prepara la siguiente
     * @param game Estado actual del juego
     */
    suspend fun endRound(game: Game) {
        android.util.Log.d("GameEngine", "Finalizando ronda ${game.currentRound}")
        
        // Cambiar estado a ROUND_END
        firebaseManager.updateGameState(game.roomCode, GameState.ROUND_END.name)
        
        // Pequeña pausa para mostrar resultados
        delay(Constants.ROUND_END_DELAY)
        
        val alivePlayers = game.getAlivePlayers()
        
        // Verificar si el juego terminó
        if (alivePlayers.isEmpty()) {
            // Todos perdieron - empate
            android.util.Log.d("GameEngine", "Todos perdieron - Empate")
            finishGame(game)
        } else if (alivePlayers.size == 1) {
            // Solo queda 1 jugador - es el ganador
            android.util.Log.d("GameEngine", "Solo queda 1 jugador - Ganador: ${alivePlayers.first().name}")
            finishGame(game)
        } else {
            android.util.Log.d("GameEngine", "Iniciando nueva ronda...")
            
            // Iniciar nueva ronda
            val nextRound = game.currentRound + 1
            
            // Limpiar la lista de jugadores que jugaron
            android.util.Log.d("GameEngine", "Limpiando lista de jugadores que jugaron")
            firebaseManager.clearPlayersWhoPlayed(game.roomCode)
            
            // Asignar nuevos emojis a los jugadores vivos
            android.util.Log.d("GameEngine", "Reasignando emojis para ronda $nextRound")
            assignEmojisToPlayers(game)
            
            // Esperar a que se actualicen los emojis
            delay(500)
            
            // Actualizar número de ronda
            firebaseManager.updateRound(game.roomCode, nextRound)
            
            // Cambiar estado a IN_PROGRESS
            firebaseManager.updateGameState(game.roomCode, GameState.IN_PROGRESS.name)
            
            // Iniciar turno del primer jugador vivo
            val firstPlayer = game.getAlivePlayers().firstOrNull()
            if (firstPlayer != null) {
                android.util.Log.d("GameEngine", "Iniciando turno de ${firstPlayer.name}")
                startPlayerTurn(game.roomCode, firstPlayer.id)
            }
        }
    }
    
    /**
     * Calcula el tiempo restante del turno actual
     * @param game Estado actual del juego
     * @return Segundos restantes o 0 si el tiempo expiró
     */
    fun getRemainingTime(game: Game): Int {
        val currentTime = System.currentTimeMillis()
        val elapsedSeconds = ((currentTime - game.roundStartTime) / 1000).toInt()
        val remaining = game.roundDuration - elapsedSeconds
        return maxOf(0, remaining)
    }
    
    /**
     * Verifica si el turno actual ha expirado
     * @param game Estado actual del juego
     * @return true si el tiempo se agotó
     */
    fun isTurnExpired(game: Game): Boolean {
        return getRemainingTime(game) <= 0
    }
}
