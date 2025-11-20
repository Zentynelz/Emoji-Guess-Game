# Configuración de Firebase

## 1. Crear Proyecto en Firebase

1. Ir a [Firebase Console](https://console.firebase.google.com/)
2. Crear nuevo proyecto
3. Nombre: `EmojiGuess` (o el que prefieras)

## 2. Registrar App Android

1. En la página del proyecto, seleccionar Android
2. Registrar app con:
   - **Package name:** `com.example.emojiguess`
   - **App nickname:** Emoji Guess Game (opcional)
3. Descargar `google-services.json`
4. Colocar el archivo en `app/google-services.json`

**Ubicación correcta:**
```
EmojiGuess/
├── app/
│   ├── google-services.json  ← Aquí
│   ├── build.gradle.kts
│   └── src/
```

## 3. Habilitar Authentication

1. En Firebase Console → Authentication
2. Pestaña "Sign-in method"
3. Habilitar "Anónimo" (Anonymous)
4. Guardar

## 4. Crear Realtime Database

1. En Firebase Console → Realtime Database
2. Crear base de datos
3. Seleccionar ubicación (ej: `us-central1`)
4. Modo: "Comenzar en modo de prueba"
5. Habilitar

## 5. Configurar Reglas de Seguridad

En la pestaña "Reglas" de Realtime Database:

```json
{
  "rules": {
    "games": {
      "$roomCode": {
        ".read": true,
        ".write": true
      }
    },
    "messages": {
      "$roomCode": {
        ".read": true,
        ".write": true
      }
    }
  }
}
```

**Nota:** Estas reglas son para desarrollo. Para producción, implementar reglas más restrictivas.

## 6. Verificar Configuración

1. Abrir proyecto en Android Studio
2. Sincronizar Gradle
3. Compilar: `./gradlew build`
4. Ejecutar app
5. Verificar en Firebase Console que se crean datos

## Estructura de Datos en Firebase

```
firebase-root/
├── games/
│   └── ROOM123/
│       ├── roomCode: "ROOM123"
│       ├── hostId: "player1"
│       ├── state: "WAITING"
│       ├── currentRound: 0
│       ├── players/
│       │   ├── player1/
│       │   │   ├── id: "player1"
│       │   │   ├── name: "Alice"
│       │   │   ├── emoji: "😀"
│       │   │   ├── isAlive: true
│       │   │   └── isHost: true
│       │   └── player2/...
│       └── playersWhoPlayedThisRound: []
└── messages/
    └── ROOM123/
        ├── message1/
        │   ├── playerId: "player1"
        │   ├── playerName: "Alice"
        │   ├── text: "Hola!"
        │   └── timestamp: 1234567890
        └── message2/...
```

## Solución de Problemas

**Error: "google-services.json is missing"**
- Verificar que el archivo esté en `app/google-services.json`
- Sincronizar Gradle

**Error: "FirebaseApp initialization unsuccessful"**
- Verificar que el package name sea `com.example.emojiguess`
- Limpiar y reconstruir proyecto

**Los datos no aparecen en Firebase:**
- Verificar reglas de seguridad
- Verificar que Realtime Database esté habilitado
- Revisar logs en Logcat

**Error de autenticación:**
- Verificar que Anonymous Authentication esté habilitado
- Revisar Firebase Console para ver intentos de autenticación

## Checklist

- [ ] Proyecto creado en Firebase Console
- [ ] App Android registrada
- [ ] `google-services.json` en `app/`
- [ ] Authentication habilitado (Anónimo)
- [ ] Realtime Database creado
- [ ] Reglas de seguridad configuradas
- [ ] Proyecto compilado sin errores
- [ ] Datos visibles en Firebase Console
