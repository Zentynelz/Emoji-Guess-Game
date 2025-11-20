package com.example.emojiguess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.emojiguess.ui.theme.EmojiGuessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val navigationRoute = intent.getStringExtra("navigation_route")
        
        setContent {
            EmojiGuessTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EmojiGuessApp(startRoute = navigationRoute)
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object CreateRoom : Screen("create_room")
    object JoinRoom : Screen("join_room")
    object Lobby : Screen("lobby")
    object Victory : Screen("victory")
    object Defeat : Screen("defeat")
}

@Composable
fun EmojiGuessApp(startRoute: String? = null) {
    val navController = rememberNavController()
    
    LaunchedEffect(startRoute) {
        if (!startRoute.isNullOrEmpty()) {
            navController.navigate(startRoute) {
                popUpTo(Screen.Welcome.route) { inclusive = false }
            }
        }
    }
    
    NavHost(navController = navController, startDestination = Screen.Welcome.route) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController)
        }
        composable(Screen.CreateRoom.route) {
            CreateRoomScreen(navController)
        }
        composable(Screen.JoinRoom.route) {
            JoinRoomScreen(navController)
        }
        composable("${Screen.Lobby.route}/{roomCode}/{playerId}/{playerName}/{isHost}") { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            val playerId = backStackEntry.arguments?.getString("playerId") ?: ""
            val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
            val isHost = backStackEntry.arguments?.getString("isHost")?.toBoolean() ?: false
            
            LobbyScreen(
                navController = navController,
                roomCode = roomCode,
                playerId = playerId,
                playerName = playerName,
                isHost = isHost
            )
        }
        composable("${Screen.Victory.route}/{winnerName}") { backStackEntry ->
            val winnerName = backStackEntry.arguments?.getString("winnerName") ?: ""
            VictoryScreen(navController, winnerName)
        }
        composable("${Screen.Defeat.route}/{winnerName}") { backStackEntry ->
            val winnerName = backStackEntry.arguments?.getString("winnerName") ?: ""
            DefeatScreen(navController, winnerName)
        }
    }
}
