package com.example.emojiguess

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay


data class GamePlayer(val id: Int, val name: String, val emoji: String, var isEliminated: Boolean = false)
data class ChatMessage(val sender: String, val message: String)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(navController: NavController) {
    // Simulación de estado del juego
    val players = remember {
        mutableStateListOf(
            GamePlayer(1, "Jugador 1", "😀"),
            GamePlayer(2, "Jugador 2", "😂"),
            GamePlayer(3, "Jugador 3", "😍"),
            GamePlayer(4, "Jugador 4", "😎")
        )
    }
    val myPlayerId = 1 // Simulación de que el usuario actual es el Jugador 1
    val mySecretEmoji = players.first { it.id == myPlayerId }.emoji
    val currentTurnPlayerId = remember { mutableStateOf(2) } // Simulación de turno
    val isMyTurn = currentTurnPlayerId.value == myPlayerId

    // Simulación de temporizador
    var countdown by remember { mutableStateOf(10) }
    LaunchedEffect(currentTurnPlayerId.value) {
        countdown = 10
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
        // Simulación de eliminación por tiempo
        if (players.none { !it.isEliminated && it.id == currentTurnPlayerId.value }) {
            // Si el jugador de turno no responde, se elimina (simulación)
            players.find { it.id == currentTurnPlayerId.value }?.isEliminated = true
            // Simulación de cambio de turno
            currentTurnPlayerId.value = (currentTurnPlayerId.value % 4) + 1
        }
    }

    // Simulación de chat
    val chatMessages = remember { mutableStateListOf(ChatMessage("Sistema", "¡Comienza la partida!")) }
    var chatInput by remember { mutableStateOf("") }

    // Simulación de la acción de adivinar
    fun onGuess(guessedEmoji: String) {
        val correct = guessedEmoji == mySecretEmoji
        if (correct) {
            // Simulación de acierto
            navController.navigate(Screen.Victory.route)
        } else {
            // Simulación de fallo
            players.find { it.id == myPlayerId }?.isEliminated = true
            navController.navigate(Screen.Defeat.route)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Turno de: ${players.find { it.id == currentTurnPlayerId.value }?.name ?: "Nadie"}")
                },
                actions = {
                    var showExitDialog by remember { mutableStateOf(false) }

                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Filled.Close, contentDescription = "Salir del Juego")
                    }

                    if (showExitDialog) {
                        AlertDialog(
                            onDismissRequest = { showExitDialog = false },
                            title = { Text("Confirmar Salida") },
                            text = { Text("¿Estás seguro de que quieres salir de la partida?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showExitDialog = false
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
            )
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Temporizador (Animación de Countdown - Fase 5)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tiempo restante: $countdown",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Área de jugadores y emojis visibles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                players.filter { it.id != myPlayerId }.forEach { player ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Feedback visual cuando un jugador es eliminado (Fase 4)
                        AnimatedVisibility(
                            visible = !player.isEliminated,
                            enter = fadeIn(tween(500)),
                            exit = fadeOut(tween(500))
                        ) {
                            Text(
                                text = player.emoji,
                                fontSize = 40.sp,
                                modifier = Modifier
                                    .background(
                                        if (player.id == currentTurnPlayerId.value) Color.Yellow.copy(alpha = 0.5f) else Color.Transparent,
                                        CircleShape
                                    )
                                    .padding(4.dp)
                                    .clickable(enabled = isMyTurn) {
                                        if (isMyTurn) onGuess(player.emoji)
                                    }
                            )
                        }
                        Text(player.name, style = MaterialTheme.typography.bodySmall)
                        if (player.isEliminated) {
                            Text("❌", fontSize = 20.sp)
                        }
                    }
                }
            }

            HorizontalDivider()

            // Chat Global
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                items(chatMessages) { message ->
                    Text(
                        text = "${message.sender}: ${message.message}",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Área de entrada de chat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    label = { Text("Escribe un mensaje...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (chatInput.isNotBlank()) {
                            chatMessages.add(ChatMessage("Yo", chatInput))
                            chatInput = ""
                        }
                    },
                    enabled = chatInput.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}
