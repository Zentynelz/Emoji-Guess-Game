package com.example.emojiguess

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.emojiguess.ui.game.GameActivity
import com.example.emojiguess.utils.Constants
import kotlinx.coroutines.launch

@Composable
fun JoinRoomScreen(navController: NavController) {
    var playerName by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }
    var isJoining by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Unirse a una Sala",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Campo para nombre del jugador
        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("Tu Nombre") },
            singleLine = true,
            enabled = !isJoining,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo para código de la sala
        OutlinedTextField(
            value = roomCode,
            onValueChange = { 
                if (it.length <= Constants.ROOM_CODE_LENGTH) {
                    roomCode = it.uppercase()
                }
            },
            label = { Text("Código de la Sala (${Constants.ROOM_CODE_LENGTH} caracteres)") },
            singleLine = true,
            enabled = !isJoining,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón para unirse
        Button(
            onClick = {
                if (playerName.isNotBlank() && roomCode.isNotBlank()) {
                    isJoining = true
                    scope.launch {
                        try {
                            val repository = com.example.emojiguess.data.GameRepository.getInstance()
                            
                            // Autenticar usuario
                            val playerId = repository.authenticateUser()
                            
                            // Unirse a la sala
                            val success = repository.joinRoom(roomCode, playerName)
                            
                            if (success) {
                                // Navegar a LobbyScreen
                                navController.navigate("${Screen.Lobby.route}/$roomCode/$playerId/$playerName/false")
                            } else {
                                Toast.makeText(context, "Sala no encontrada", Toast.LENGTH_SHORT).show()
                                isJoining = false
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            isJoining = false
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Ingresa tu nombre y el código de la sala",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            enabled = playerName.length >= Constants.MIN_PLAYER_NAME_LENGTH && 
                     playerName.length <= Constants.MAX_PLAYER_NAME_LENGTH && 
                     roomCode.length == Constants.ROOM_CODE_LENGTH && 
                     !isJoining,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isJoining) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Unirse a Sala")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de volver
        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !isJoining
        ) {
            Text("Volver")
        }
    }
}
