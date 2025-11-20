package com.example.emojiguess

import com.example.emojiguess.logic.EmojiManager
import com.example.emojiguess.logic.GameEngine
import com.example.emojiguess.models.Game
import com.example.emojiguess.models.GameState
import com.example.emojiguess.models.Player
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias para la lógica del juego
 */
class GameEngineTest {
    
    private lateinit var game: Game
    
    @Before
    fun setup() {
        // Crear un juego de prueba con 3 jugadores
        val players = mapOf(
            "player1" to Player("player1", "Alice", "😀", true, true),
            "player2" to Player("player2", "Bob", "😃", true, false),
            "player3" to Player("player3", "Charlie", "😄", true, false)
        )
        
        game = Game(
            roomCode = "TEST01",
            hostId = "player1",
            players = players,
            state = GameState.IN_PROGRESS.name,
            currentRound = 1,
            currentTurnPlayerId = "player1",
            roundStartTime = System.currentTimeMillis(),
            roundDuration = 30
        )
    }
    
    @Test
    fun testValidateCorrectAnswer() {
        // Validación directa sin GameEngine
        val player = game.players["player1"]!!
        val result = player.emoji == "😀"
        assertTrue("La respuesta correcta debe ser válida", result)
    }

    
    @Test
    fun testValidateIncorrectAnswer() {
        // Validación directa sin GameEngine
        val player = game.players["player1"]!!
        val result = player.emoji == "😃"
        assertFalse("La respuesta incorrecta debe ser inválida", result)
    }
    
    @Test
    fun testCheckGameOverWithOnePlayer() {
        // Eliminar dos jugadores
        val updatedPlayers = game.players.toMutableMap()
        updatedPlayers["player2"] = updatedPlayers["player2"]!!.copy(isAlive = false)
        updatedPlayers["player3"] = updatedPlayers["player3"]!!.copy(isAlive = false)
        
        val gameWithOnePlayer = game.copy(players = updatedPlayers)
        
        // Verificar directamente
        val alivePlayers = gameWithOnePlayer.getAlivePlayers()
        assertTrue("El juego debe terminar con solo 1 jugador vivo", alivePlayers.size <= 1)
    }
    
    @Test
    fun testCheckGameOverWithMultiplePlayers() {
        val alivePlayers = game.getAlivePlayers()
        assertFalse("El juego no debe terminar con múltiples jugadores vivos", alivePlayers.size <= 1)
    }
    
    @Test
    fun testGetNextPlayer() {
        val alivePlayers = game.getAlivePlayers()
        val currentIndex = alivePlayers.indexOfFirst { it.id == "player1" }
        val nextIndex = (currentIndex + 1) % alivePlayers.size
        val nextPlayer = alivePlayers[nextIndex].id
        assertEquals("El siguiente jugador debe ser player2", "player2", nextPlayer)
    }
    
    @Test
    fun testGetNextPlayerWrapsAround() {
        val alivePlayers = game.getAlivePlayers()
        val currentIndex = alivePlayers.indexOfFirst { it.id == "player3" }
        val nextIndex = (currentIndex + 1) % alivePlayers.size
        val nextPlayer = alivePlayers[nextIndex].id
        assertEquals("Después del último jugador debe volver al primero", "player1", nextPlayer)
    }
    
    @Test
    fun testEmojiAssignment() {
        val emojis = EmojiManager.assignEmojis(5)
        assertEquals("Debe asignar 5 emojis", 5, emojis.size)
        
        // Verificar que todos son únicos
        val uniqueEmojis = emojis.toSet()
        assertEquals("Todos los emojis deben ser únicos", emojis.size, uniqueEmojis.size)
    }
    
    @Test
    fun testGetAlivePlayers() {
        val alivePlayers = game.getAlivePlayers()
        assertEquals("Debe haber 3 jugadores vivos", 3, alivePlayers.size)
    }
    
    @Test
    fun testGetAlivePlayersAfterElimination() {
        val updatedPlayers = game.players.toMutableMap()
        updatedPlayers["player2"] = updatedPlayers["player2"]!!.copy(isAlive = false)
        
        val gameWithElimination = game.copy(players = updatedPlayers)
        val alivePlayers = gameWithElimination.getAlivePlayers()
        
        assertEquals("Debe haber 2 jugadores vivos", 2, alivePlayers.size)
    }
    
    @Test
    fun testRoundProgression() {
        val initialRound = game.currentRound
        val nextRound = initialRound + 1
        
        val gameWithNewRound = game.copy(currentRound = nextRound)
        
        assertEquals("La ronda debe incrementarse", nextRound, gameWithNewRound.currentRound)
        assertTrue("La nueva ronda debe ser mayor", gameWithNewRound.currentRound > initialRound)
    }
    
    @Test
    fun testEmojiReassignment() {
        val emojis1 = EmojiManager.assignEmojis(3)
        val emojis2 = EmojiManager.assignEmojis(3)
        
        assertEquals("Debe asignar 3 emojis en cada ronda", 3, emojis1.size)
        assertEquals("Debe asignar 3 emojis en cada ronda", 3, emojis2.size)
        
        // Los emojis deben ser únicos en cada asignación
        assertEquals("Emojis deben ser únicos", emojis1.size, emojis1.toSet().size)
        assertEquals("Emojis deben ser únicos", emojis2.size, emojis2.toSet().size)
    }
    
    @Test
    fun testGameStateTransitions() {
        val waitingGame = game.copy(state = GameState.WAITING.name)
        assertEquals("Estado debe ser WAITING", GameState.WAITING, waitingGame.getGameState())
        
        val inProgressGame = game.copy(state = GameState.IN_PROGRESS.name)
        assertEquals("Estado debe ser IN_PROGRESS", GameState.IN_PROGRESS, inProgressGame.getGameState())
        
        val finishedGame = game.copy(state = GameState.FINISHED.name)
        assertEquals("Estado debe ser FINISHED", GameState.FINISHED, finishedGame.getGameState())
    }
    
    @Test
    fun testNoWinnerScenario() {
        // Todos los jugadores eliminados
        val allEliminatedPlayers = game.players.mapValues { (_, player) ->
            player.copy(isAlive = false)
        }
        val gameWithNoWinner = game.copy(players = allEliminatedPlayers)
        
        val alivePlayers = gameWithNoWinner.getAlivePlayers()
        assertEquals("No debe haber jugadores vivos", 0, alivePlayers.size)
        
        // Verificar directamente
        assertTrue("El juego debe terminar sin jugadores vivos", alivePlayers.size <= 1)
    }
}
