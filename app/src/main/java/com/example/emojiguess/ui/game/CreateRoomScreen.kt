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
fun CreateRoomScreen(navController: NavController) {
    var playerName by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Resetear isCreating cuando volvemos a esta pantalla
    LaunchedEffect(Unit) {
        isCreating = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear Nueva Sala",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("Tu Nombre") },
            singleLine = true,
            enabled = !isCreating,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (playerName.length >= Constants.MIN_PLAYER_NAME_LENGTH && 
                    playerName.length <= Constants.MAX_PLAYER_NAME_LENGTH && 
                    !isCreating) {
                    isCreating = true
                    scope.launch {
                        try {
                            val repository = com.example.emojiguess.data.GameRepository.getInstance()
                            
                            // Autenticar usuario
                            val playerId = repository.authenticateUser()
                            android.util.Log.d("CreateRoom", "PlayerId: $playerId")
                            
                            // Crear sala en Firebase
                            val roomCode = repository.createRoom(playerName)
                            android.util.Log.d("CreateRoom", "RoomCode creado: $roomCode")
                            
                            // Mostrar código de sala al usuario
                            Toast.makeText(context, "Sala creada: $roomCode", Toast.LENGTH_LONG).show()
                            
                            // Navegar a LobbyScreen
                            android.util.Log.d("CreateRoom", "Navegando a Lobby con roomCode: $roomCode")
                            navController.navigate("${Screen.Lobby.route}/$roomCode/$playerId/$playerName/true")
                            
                            // Resetear estado después de navegar
                            isCreating = false
                        } catch (e: Exception) {
                            android.util.Log.e("CreateRoom", "Error al crear sala", e)
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            isCreating = false
                        }
                    }
                } else if (playerName.length < Constants.MIN_PLAYER_NAME_LENGTH) {
                    Toast.makeText(context, "Nombre muy corto (mínimo ${Constants.MIN_PLAYER_NAME_LENGTH} caracteres)", Toast.LENGTH_SHORT).show()
                } else if (playerName.length > Constants.MAX_PLAYER_NAME_LENGTH) {
                    Toast.makeText(context, "Nombre muy largo (máximo ${Constants.MAX_PLAYER_NAME_LENGTH} caracteres)", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = playerName.length >= Constants.MIN_PLAYER_NAME_LENGTH && 
                     playerName.length <= Constants.MAX_PLAYER_NAME_LENGTH && 
                     !isCreating,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Crear Sala y Entrar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !isCreating
        ) {
            Text("Volver")
        }
    }
}
