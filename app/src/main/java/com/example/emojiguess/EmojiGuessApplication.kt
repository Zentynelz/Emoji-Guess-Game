package com.example.emojiguess

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.FirebaseDatabase

class EmojiGuessApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Inicializar Firebase
            val app = FirebaseApp.initializeApp(this)
            
            if (app != null) {
                Log.d("EmojiGuess", "Firebase inicializado correctamente")
                Log.d("EmojiGuess", "Firebase App Name: ${app.name}")
                Log.d("EmojiGuess", "Firebase Project ID: ${app.options.projectId}")
                
                // Habilitar persistencia offline
                try {
                    FirebaseDatabase.getInstance().setPersistenceEnabled(true)
                    Log.d("EmojiGuess", "Persistencia habilitada")
                } catch (e: Exception) {
                    Log.d("EmojiGuess", "Persistencia ya estaba habilitada")
                }
            } else {
                Log.e("EmojiGuess", "ERROR: Firebase no se pudo inicializar")
            }
        } catch (e: Exception) {
            Log.e("EmojiGuess", "ERROR al inicializar Firebase: ${e.message}", e)
        }
    }
}
