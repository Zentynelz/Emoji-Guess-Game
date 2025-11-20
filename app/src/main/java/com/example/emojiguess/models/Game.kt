package com.example.emojiguess.models

/**
 * Modelo de datos para una partida del juego
 * @param roomCode Código único de la sala
 * @param hostId ID del jugador host
 * @param players Mapa de jugadores (key: playerId, value: Player)
 * @param state Estado actual del juego
 * @param currentRound Número de ronda actual
 * @param currentTurnPlayerId ID del jugador que tiene el turno actual
 * @param roundStartTime Timestamp de inicio de la ronda actual
 * @param roundDuration Duración de cada ronda en segundos
 * @param winnerId ID del jugador ganador (null si no hay ganador aún)
 */
data class Game(
    val roomCode: String = "",
    val hostId: String = "",
    val players: Map<String, Player> = emptyMap(),
    val state: String = GameState.WAITING.name,
    val currentRound: Int = 0,
    val currentTurnPlayerId: String = "",
    val roundStartTime: Long = 0L,
    val roundDuration: Int = 30, // 30 segundos por defecto
    val winnerId: String? = null,
    val playersWhoPlayedThisRound: List<String> = emptyList() // Jugadores que ya jugaron en esta ronda
) {
    /**
     * Convierte el objeto Game a un Map para Firebase
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "roomCode" to roomCode,
            "hostId" to hostId,
            "players" to players.mapValues { it.value.toMap() },
            "state" to state,
            "currentRound" to currentRound,
            "currentTurnPlayerId" to currentTurnPlayerId,
            "roundStartTime" to roundStartTime,
            "roundDuration" to roundDuration,
            "winnerId" to (winnerId ?: ""),
            "playersWhoPlayedThisRound" to playersWhoPlayedThisRound
        )
    }
    
    /**
     * Obtiene la lista de jugadores vivos
     */
    fun getAlivePlayers(): List<Player> {
        return players.values.filter { it.isAlive }
    }
    
    /**
     * Obtiene el estado del juego como enum
     */
    fun getGameState(): GameState {
        return try {
            GameState.valueOf(state)
        } catch (e: IllegalArgumentException) {
            GameState.WAITING
        }
    }
    
    companion object {
        /**
         * Crea un objeto Game desde un Map de Firebase
         */
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>): Game {
            val playersMap = (map["players"] as? Map<String, Map<String, Any>>)?.mapValues { 
                Player.fromMap(it.value) 
            } ?: emptyMap()
            
            val playersWhoPlayed = (map["playersWhoPlayedThisRound"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            
            return Game(
                roomCode = map["roomCode"] as? String ?: "",
                hostId = map["hostId"] as? String ?: "",
                players = playersMap,
                state = map["state"] as? String ?: GameState.WAITING.name,
                currentRound = (map["currentRound"] as? Long)?.toInt() ?: 0,
                currentTurnPlayerId = map["currentTurnPlayerId"] as? String ?: "",
                roundStartTime = map["roundStartTime"] as? Long ?: 0L,
                roundDuration = (map["roundDuration"] as? Long)?.toInt() ?: 30,
                winnerId = (map["winnerId"] as? String)?.takeIf { it.isNotEmpty() },
                playersWhoPlayedThisRound = playersWhoPlayed
            )
        }
    }
}
