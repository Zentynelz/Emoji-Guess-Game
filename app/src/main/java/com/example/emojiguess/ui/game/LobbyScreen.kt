package com.example.emojiguess

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.emojiguess.data.GameRepository
import com.example.emojiguess.models.GameState
import com.example.emojiguess.ui.game.GameActivity
import com.example.emojiguess.utils.Constants
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    navController: NavController,
    roomCode: String,
    playerId: String,
    playerName: String,
    isHost: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { GameRepository.getInstance() }
    
    var players by remember { mutableStateOf<List<com.example.emojiguess.models.Player>>(emptyList()) }
    var gameState by remember { mutableStateOf(GameState.WAITING) }
    var canStartGame by remember { mutableStateOf(false) }
    
    // Observar cambios en el juego
    LaunchedEffect(roomCode) {
        repository.observeGame(roomCode)
            .catch { e -> 
                android.util.Log.e("LobbyScreen", "Error observando juego", e)
            }
            .collect { game ->
                game?.let {
                    players = it.players.values.toList()
                    gameState = it.getGameState()
                    canStartGame = players.size >= Constants.MIN_PLAYERS
                    
                    // Si el juego inició, navegar a GameActivity
                    if (gameState == GameState.IN_PROGRESS || gameState == GameState.STARTING) {
                        val intent = GameActivity.createIntent(context, roomCode, playerId, playerName)
                        context.startActivity(intent)
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sala: $roomCode") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sala: $roomCode",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = if (canStartGame) {
                    "Esperando que el host inicie..."
                } else {
                    "Esperando más jugadores (${players.size}/${Constants.MIN_PLAYERS})"
                },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Lista de jugadores
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(players) { player ->
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Filled.Person, contentDescription = "Jugador")
                            },
                            headlineContent = { 
                                Text(
                                    text = if (player.id == playerId) "${player.name} (Tú)" else player.name
                                )
                            },
                            trailingContent = {
                                if (player.isHost) {
                                    Text("HOST", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                        if (player != players.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón para iniciar partida (solo visible para el host)
            if (isHost) {
                var isStarting by remember { mutableStateOf(false) }
                
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isStarting = true
                                android.util.Log.d("LobbyScreen", "Host iniciando juego...")
                                
                                // Obtener el juego actual
                                val game = repository.observeGame(roomCode).catch { }.first { it != null }
                                
                                if (game != null) {
                                    // 1. Asignar emojis a los jugadores
                                    android.util.Log.d("LobbyScreen", "Asignando emojis...")
                                    repository.assignEmojis(game)
                                    
                                    // 2. Esperar un momento
                                    kotlinx.coroutines.delay(500)
                                    
                                    // 3. Iniciar el juego (cambia estado a STARTING)
                                    android.util.Log.d("LobbyScreen", "Iniciando juego...")
                                    repository.startGame(roomCode)
                                    
                                    // 4. Esperar un momento
                                    kotlinx.coroutines.delay(500)
                                    
                                    // 5. Iniciar el primer turno
                                    val firstPlayer = game.getAlivePlayers().firstOrNull()
                                    if (firstPlayer != null) {
                                        android.util.Log.d("LobbyScreen", "Iniciando turno de ${firstPlayer.name}")
                                        repository.startPlayerTurn(roomCode, firstPlayer.id)
                                    }
                                    
                                    // La navegación a GameActivity se hará automáticamente
                                    // cuando el estado cambie a IN_PROGRESS
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("LobbyScreen", "Error al iniciar juego", e)
                                isStarting = false
                            }
                        }
                    },
                    enabled = canStartGame && !isStarting
                ) {
                    if (isStarting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Iniciar Partida (${players.size}/${Constants.MAX_PLAYERS})")
                    }
                }
                
                if (!canStartGame) {
                    Text(
                        text = "Se necesitan al menos ${Constants.MIN_PLAYERS} jugadores",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                Text(
                    text = "Esperando a que el host inicie la partida...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para salir del juego (con diálogo de confirmación)
            var showExitDialog by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showExitDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Salir del Juego")
            }

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("Confirmar Salida") },
                    text = { Text("¿Estás seguro de que quieres salir de la sala? Perderás tu progreso.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showExitDialog = false
                                scope.launch {
                                    try {
                                        repository.leaveRoom(roomCode)
                                    } catch (e: Exception) {
                                        android.util.Log.e("LobbyScreen", "Error al salir", e)
                                    }
                                }
                                navController.popBackStack(Screen.Welcome.route, inclusive = false)
                            }
                        ) {
                            Text("Sí, Salir")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExitDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
