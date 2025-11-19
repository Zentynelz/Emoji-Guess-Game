package com.example.emojiguess

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*

@Composable
fun DefeatScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("DERROTA", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        // Animación de Derrota (Opcional)
        // **NOTA:** Se requiere un archivo JSON de Lottie para la animación real.
        // Este es un placeholder visual.
        Text(
            text = "💀 Animación de Derrota Lottie 💀",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(32.dp)
        )
        // Ejemplo de cómo se usaría Lottie:
        /*
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.defeat_animation))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(200.dp)
        )
        */

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { navController.popBackStack(Screen.Welcome.route, inclusive = false) }) {
            Text("Volver al Inicio")
        }
    }
}
