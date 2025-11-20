# Emoji Guess Game

Aplicación Android multijugador en tiempo real donde los jugadores deben adivinar su emoji oculto antes de ser eliminados.

## Descripción

Juego de adivinanzas multijugador donde cada jugador recibe un emoji secreto que no puede ver. Los jugadores observan los emojis de los demás y deben deducir cuál es el suyo. El último jugador en pie gana.

## Tecnologías

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + XML Views
- **Arquitectura:** MVVM
- **Backend:** Firebase Realtime Database
- **Autenticación:** Firebase Anonymous Auth

## Requisitos

- Android Studio Hedgehog o superior
- Android SDK 24+ (Android 7.0)
- Cuenta de Firebase
- Conexión a Internet

## Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/Zentynelz/Emoji-Guess-Game.git
cd Emoji-Guess-Game
```

### 2. Configurar Firebase

Antes de ejecutar la aplicación, debes configurar Firebase. Ver instrucciones detalladas en [CONFIGURACION_FIREBASE.md](CONFIGURACION_FIREBASE.md)

Pasos básicos:
1. Crear proyecto en Firebase Console
2. Registrar app Android con package `com.example.emojiguess`
3. Descargar `google-services.json` y colocarlo en `app/`
4. Habilitar Authentication (Anónimo)
5. Crear Realtime Database
6. Configurar reglas de seguridad

### 3. Compilar y ejecutar

```bash
./gradlew assembleDebug
./gradlew installDebug
```

O ejecutar directamente desde Android Studio.

## Características

- Multijugador en tiempo real (2-8 jugadores)
- Lobby de espera con lista de jugadores
- Chat global durante el juego
- Temporizador de 30 segundos por turno
- 112 emojis diferentes
- Pantallas de victoria y derrota
- Validación de nombres y códigos de sala
- Sistema de turnos con tracking de rondas

## Cómo Jugar

Ver instrucciones completas en [COMO_JUGAR.md](COMO_JUGAR.md)

**Resumen:**
1. Crear o unirse a una sala con código de 6 caracteres
2. Esperar en el lobby (mínimo 2 jugadores)
3. El host inicia la partida
4. En tu turno, selecciona el emoji que crees que es el tuyo
5. Si aciertas, continúas; si fallas, eres eliminado
6. El último jugador vivo gana

## Estructura del Proyecto

```
app/src/main/java/com/example/emojiguess/
├── data/
│   ├── FirebaseManager.kt      # Gestión de Firebase
│   └── GameRepository.kt        # Repositorio de datos
├── logic/
│   ├── GameEngine.kt            # Lógica del juego
│   └── EmojiManager.kt          # Gestión de emojis
├── models/
│   ├── Game.kt                  # Modelo de partida
│   ├── Player.kt                # Modelo de jugador
│   ├── Message.kt               # Modelo de mensaje
│   └── GameState.kt             # Estados del juego
├── ui/
│   ├── chat/                    # Sistema de chat
│   ├── game/                    # Pantallas del juego
│   └── theme/                   # Temas de Compose
└── utils/
    └── Constants.kt             # Constantes del juego
```

## División de Trabajo

Ver detalles en [DIVISION_DE_TRABAJO.md](DIVISION_DE_TRABAJO.md)

- **Persona 1:** Backend, Firebase, lógica del juego, pruebas
- **Persona 2:** Chat, UI del juego, layouts
- **Persona 3:** Navegación, UX, pantallas Compose

## Pruebas

```bash
./gradlew test
```

Incluye pruebas unitarias para:
- Validación de respuestas
- Detección de fin de juego
- Asignación de emojis únicos
- Rotación de turnos
- Gestión de jugadores

## Solución de Problemas

**La app no compila:**
- Verifica que `google-services.json` esté en `app/`
- Sincroniza Gradle
- Limpia el proyecto: `./gradlew clean`

**No se conecta a Firebase:**
- Verifica conexión a Internet
- Revisa configuración de Firebase
- Verifica reglas de seguridad en Firebase Console

**El chat no funciona:**
- Verifica que Realtime Database esté habilitado
- Revisa logs en Logcat

## Licencia

Proyecto de código abierto para fines educativos.

## Autores

Desarrollado por el equipo de Emoji Guess Game.
