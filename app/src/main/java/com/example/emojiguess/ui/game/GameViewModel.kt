package com.example.emojiguess.ui.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emojiguess.data.GameRepository
import com.example.emojiguess.logic.EmojiManager
import com.example.emojiguess.models.Game
import com.example.emojiguess.models.GameState
import com.example.emojiguess.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val repository = GameRepository.getInstance()
    
    private val _roomId = MutableLiveData<String>()
    val roomId: LiveData<String> = _roomId

    private val _players = MutableLiveData<List<PlayerUiModel>>(emptyList())
    val players: LiveData<List<PlayerUiModel>> = _players

    private val _availableEmojis = MutableLiveData<List<String>>(emptyList())
    val availableEmojis: LiveData<List<String>> = _availableEmojis

    private val _selectedEmoji = MutableLiveData<String?>(null)
    val selectedEmoji: LiveData<String?> = _selectedEmoji

    private val _hiddenEmoji = MutableLiveData("?")
    val hiddenEmoji: LiveData<String> = _hiddenEmoji

    private val _statusMessage = MutableLiveData<String>("")
    val statusMessage: LiveData<String> = _statusMessage

    private val _timerSeconds = MutableLiveData(30)
    val timerSeconds: LiveData<Int> = _timerSeconds

    private val _isPlayerTurn = MutableLiveData(false)
    val isPlayerTurn: LiveData<Boolean> = _isPlayerTurn

    private val _isConfirmEnabled = MediatorLiveData<Boolean>().apply {
        value = false
        addSource(_selectedEmoji) { evaluateConfirmState() }
        addSource(_isPlayerTurn) { evaluateConfirmState() }
    }
    val isConfirmEnabled: LiveData<Boolean> = _isConfirmEnabled
    
    private val _gameFinished = MutableLiveData<GameResult?>(null)
    val gameFinished: LiveData<GameResult?> = _gameFinished
    
    data class GameResult(val won: Boolean, val winnerName: String)

    private var timerJob: Job? = null
    private var selfPlayerId: String = ""
    private var selfPlayerName: String = ""
    private var currentGame: Game? = null

    fun initialize(roomId: String, playerId: String, playerName: String) {
        android.util.Log.d("GameViewModel", "Initialize - RoomId: $roomId, PlayerId: $playerId, PlayerName: $playerName")
        _roomId.value = roomId
        selfPlayerId = playerId
        selfPlayerName = playerName
        
        if (roomId.isEmpty()) {
            android.util.Log.e("GameViewModel", "ERROR: RoomId está vacío!")
            _statusMessage.postValue("Error: Código de sala vacío")
            return
        }
        
        observeGame(roomId)
    }

    private fun observeGame(roomCode: String) {
        viewModelScope.launch {
            repository.observeGame(roomCode)
                .catch { e -> 
                    _statusMessage.postValue("Error: ${e.message}")
                }
                .collect { game ->
                    game?.let { updateGameState(it) }
                }
        }
    }

    private fun updateGameState(game: Game) {
        android.util.Log.d("GameViewModel", "UpdateGameState - Estado: ${game.state}, Jugadores: ${game.players.size}")
        currentGame = game
        
        // Actualizar lista de jugadores
        val playersList = game.players.values.map { player ->
            val isSelf = player.id == selfPlayerId
            val visibleEmoji = if (isSelf) "?" else (player.emoji.takeIf { it.isNotEmpty() } ?: "?")
            val isCurrentTurn = game.currentTurnPlayerId == player.id
            
            PlayerUiModel(
                id = player.id,
                displayName = player.name,
                visibleEmoji = visibleEmoji,
                isCurrentTurn = isCurrentTurn,
                isSelf = isSelf,
                isEliminated = !player.isAlive
            )
        }
        _players.postValue(playersList)
        
        // Actualizar emoji oculto del jugador y emojis disponibles
        val myPlayer = game.players[selfPlayerId]
        if (myPlayer != null && myPlayer.emoji.isNotEmpty()) {
            _hiddenEmoji.postValue("?") // Siempre oculto para el jugador
        }
        
        // Actualizar emojis disponibles para seleccionar
        val alivePlayers = game.getAlivePlayers()
        val myEmoji = myPlayer?.emoji
        
        // Solo mostrar opciones si el jugador tiene emoji asignado
        if (alivePlayers.isNotEmpty() && myEmoji != null && myEmoji.isNotEmpty()) {
            // Asegurarse de que el emoji del jugador SIEMPRE esté en las opciones
            val otherEmojis = EmojiManager.getAllEmojis()
                .filter { it != myEmoji }
                .shuffled()
                .take(Constants.EMOJI_SELECTOR_OPTIONS - 1)
            
            // Mezclar para que el emoji correcto no siempre esté en la misma posición
            val emojisToShow = (otherEmojis + myEmoji).shuffled()
            _availableEmojis.postValue(emojisToShow)
        }
        
        // Actualizar si es el turno del jugador
        val isMyTurn = game.currentTurnPlayerId == selfPlayerId
        _isPlayerTurn.postValue(isMyTurn)
        
        // Actualizar temporizador
        if (game.state == GameState.IN_PROGRESS.name) {
            val remaining = repository.getRemainingTime(game).coerceAtMost(Constants.DEFAULT_ROUND_DURATION)
            _timerSeconds.postValue(remaining)
            
            // Solo iniciar temporizador si es mi turno, hay tiempo restante y no hay uno activo
            if (isMyTurn && remaining > 0 && (timerJob == null || timerJob?.isActive == false)) {
                startTimer(remaining)
            } else if (!isMyTurn) {
                // Cancelar temporizador si ya no es mi turno
                timerJob?.cancel()
            }
        }
        
        // Actualizar mensaje de estado
        updateStatusMessage(game, isMyTurn)
    }

    private fun updateStatusMessage(game: Game, isMyTurn: Boolean) {
        val message = when (game.getGameState()) {
            GameState.WAITING -> "Esperando que inicie el juego..."
            GameState.STARTING -> "¡El juego está comenzando!"
            GameState.IN_PROGRESS -> {
                if (isMyTurn) {
                    if (_selectedEmoji.value.isNullOrBlank()) {
                        "Tu turno: Selecciona tu emoji"
                    } else {
                        "Listo para confirmar ${_selectedEmoji.value}"
                    }
                } else {
                    val currentPlayer = game.players[game.currentTurnPlayerId]
                    "Turno de ${currentPlayer?.name ?: "otro jugador"}"
                }
            }
            GameState.ROUND_END -> "Fin de ronda"
            GameState.FINISHED -> {
                // Si winnerId está vacío, no hay ganador (todos fallaron)
                if (game.winnerId.isNullOrEmpty()) {
                    _gameFinished.postValue(GameResult(false, "Nadie"))
                    "Todos fueron eliminados. No hay ganador."
                } else {
                    val winner = game.players[game.winnerId]
                    val won = winner?.id == selfPlayerId
                    val winnerName = winner?.name ?: "Desconocido"
                    
                    // Notificar que el juego terminó
                    _gameFinished.postValue(GameResult(won, winnerName))
                    
                    if (won) {
                        "¡Ganaste! 🎉"
                    } else {
                        "Ganó $winnerName"
                    }
                }
            }
        }
        _statusMessage.postValue(message)
    }

    fun onEmojiSelected(emoji: String) {
        _selectedEmoji.value = emoji
        currentGame?.let { game ->
            updateStatusMessage(game, game.currentTurnPlayerId == selfPlayerId)
        }
    }

    fun confirmSelection() {
        if (_isPlayerTurn.value != true || _selectedEmoji.value.isNullOrBlank()) return
        val guess = _selectedEmoji.value ?: return
        val game = currentGame ?: return
        
        // Obtener el emoji del jugador
        val myPlayer = game.players[selfPlayerId]
        val myEmoji = myPlayer?.emoji
        
        android.util.Log.d("GameViewModel", "Confirmando selección:")
        android.util.Log.d("GameViewModel", "  - Emoji seleccionado: $guess")
        android.util.Log.d("GameViewModel", "  - Mi emoji real: $myEmoji")
        android.util.Log.d("GameViewModel", "  - Jugador: ${myPlayer?.name}")
        android.util.Log.d("GameViewModel", "  - Ronda actual: ${game.currentRound}")
        
        if (myEmoji.isNullOrEmpty()) {
            android.util.Log.e("GameViewModel", "ERROR: El jugador no tiene emoji asignado!")
            _statusMessage.postValue("Error: No tienes emoji asignado")
            return
        }
        
        viewModelScope.launch {
            _statusMessage.postValue("Verificando tu respuesta...")
            
            // Cancelar el timer actual
            timerJob?.cancel()
            
            val isCorrect = repository.submitAnswer(game, guess)
            
            android.util.Log.d("GameViewModel", "Resultado: ${if (isCorrect) "CORRECTO" else "INCORRECTO"}")
            
            if (isCorrect) {
                _statusMessage.postValue("¡Correcto! Sigues en el juego")
            } else {
                _statusMessage.postValue("Incorrecto. Has sido eliminado")
            }
            
            _selectedEmoji.postValue(null)
            
            // Esperar un momento para que el usuario vea el resultado
            kotlinx.coroutines.delay(1500)
            
            // Esperar a que se actualice el juego después de la respuesta
            kotlinx.coroutines.delay(500)
            
            // Obtener el juego actualizado
            val updatedGame = currentGame ?: return@launch
            
            if (repository.isGameOver(updatedGame)) {
                android.util.Log.d("GameViewModel", "Juego terminado")
                repository.finishGame(updatedGame)
            } else {
                // Marcar que este jugador ya jugó en esta ronda
                repository.markPlayerAsPlayed(updatedGame.roomCode, selfPlayerId)
                
                // Verificar si todos los jugadores vivos ya jugaron
                val alivePlayers = updatedGame.getAlivePlayers()
                val playersWhoPlayed = updatedGame.playersWhoPlayedThisRound + selfPlayerId
                val allPlayersPlayed = alivePlayers.all { it.id in playersWhoPlayed }
                
                if (allPlayersPlayed) {
                    // Todos jugaron, iniciar nueva ronda
                    android.util.Log.d("GameViewModel", "Todos jugaron, iniciando nueva ronda...")
                    repository.endRound(updatedGame)
                } else {
                    // Avanzar al siguiente turno
                    val nextPlayerId = repository.getNextPlayer(updatedGame, selfPlayerId)
                    if (nextPlayerId != null) {
                        android.util.Log.d("GameViewModel", "Siguiente turno: $nextPlayerId")
                        repository.startPlayerTurn(updatedGame.roomCode, nextPlayerId)
                    }
                }
            }
        }
    }

    private fun startTimer(duration: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (second in duration downTo 0) {
                _timerSeconds.postValue(second)
                delay(1_000)
            }
            
            // Si se acabó el tiempo y es mi turno, eliminar por timeout
            if (_isPlayerTurn.value == true) {
                val game = currentGame
                if (game != null) {
                    repository.eliminateByTimeout(game.roomCode, selfPlayerId)
                    _statusMessage.postValue("Se acabó el tiempo. Has sido eliminado")
                }
            }
        }
    }

    private fun MediatorLiveData<Boolean>.evaluateConfirmState() {
        value = _isPlayerTurn.value == true && !_selectedEmoji.value.isNullOrBlank()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}