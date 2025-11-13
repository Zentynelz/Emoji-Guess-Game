# División de Trabajo - Emoji Guess Game
## Proyecto: Aplicación Android con Kotlin para juego multijugador en línea

---

## 📋 PERSONA 1 - Backend & Firebase (2.25 puntos)

### Responsabilidades Principales:
1. **Integración completa con Firebase (1.0 punto)**
   - Configurar Firebase en el proyecto Android
   - Implementar Firebase Realtime Database para sincronización en tiempo real
   - Crear estructura de datos para:
     - Salas de juego (game rooms)
     - Jugadores (players)
     - Estado del juego (game state)
     - Turnos y rondas
   - Implementar listeners para cambios en tiempo real
   - Gestionar autenticación anónima de jugadores

2. **Lógica del Juego - Core (1.0 punto)**
   - Sistema de asignación aleatoria de emojis
   - Control de turnos entre jugadores
   - Temporizador de ronda (countdown)
   - Validación de respuestas (correcta/incorrecta)
   - Sistema de eliminación de jugadores
   - Detección de victoria (último jugador en pie)
   - Reasignación de emojis en cada ronda

3. **Pruebas Unitarias (0.25 puntos)**
   - Crear pruebas para la lógica de asignación de emojis
   - Probar validación de respuestas
   - Probar detección de victoria

### Archivos a crear:
- `data/FirebaseManager.kt` - Gestión de Firebase
- `data/GameRepository.kt` - Repositorio de datos del juego
- `models/Game.kt` - Modelo de datos del juego
- `models/Player.kt` - Modelo de datos del jugador
- `models/GameState.kt` - Estados del juego
- `logic/GameEngine.kt` - Motor del juego (turnos, validaciones)
- `logic/EmojiManager.kt` - Gestión de emojis
- `test/GameEngineTest.kt` - Pruebas unitarias

---

## 📋 PERSONA 2 - Chat & Comunicación (2.0 puntos)

### Responsabilidades Principales:
1. **Sistema de Chat Completo (1.5 puntos)**
   - Implementar chat global en tiempo real con Firebase
   - Diseño de interfaz del chat (lista de mensajes)
   - Input de texto para enviar mensajes
   - Mostrar nombre/ID del jugador que envía mensaje
   - Timestamp de mensajes
   - Auto-scroll al último mensaje
   - Sincronización en tiempo real de mensajes

2. **Interfaz de Usuario - Pantalla de Juego (0.5 puntos)**
   - Diseño de la pantalla principal del juego
   - Visualización de emojis de otros jugadores
   - Mostrar el emoji oculto del jugador actual (con "?")
   - Grid/lista de jugadores con sus emojis
   - Indicador visual del turno actual
   - Temporizador visible en pantalla
   - Selector de emoji para adivinar
   - Integrar el chat en la interfaz

### Archivos a crear:
- `ui/chat/ChatFragment.kt` - Fragmento del chat
- `ui/chat/ChatAdapter.kt` - Adaptador para RecyclerView del chat
- `ui/chat/ChatViewModel.kt` - ViewModel del chat
- `ui/game/GameActivity.kt` - Actividad principal del juego
- `ui/game/GameViewModel.kt` - ViewModel del juego
- `ui/game/PlayerAdapter.kt` - Adaptador para mostrar jugadores
- `models/Message.kt` - Modelo de mensaje
- `res/layout/activity_game.xml` - Layout principal
- `res/layout/fragment_chat.xml` - Layout del chat
- `res/layout/item_message.xml` - Layout de mensaje individual
- `res/layout/item_player.xml` - Layout de jugador individual

---

## 📋 PERSONA 3 - UI/UX & Flujo de Navegación (1.5 puntos)

### Responsabilidades Principales:
1. **Pantallas de Navegación y Lobby (0.5 puntos)**
   - Pantalla de inicio/bienvenida
   - Pantalla para crear sala
   - Pantalla para unirse a sala (código de sala)
   - Pantalla de lobby (espera de jugadores)
   - Lista de jugadores en espera
   - Botón para iniciar partida (host)

2. **Manejo de Eventos y Flujo del Juego (0.5 puntos)**
   - Botón para salir del juego
   - Diálogo de confirmación para salir
   - Pantalla de victoria/derrota
   - Animaciones de transición entre pantallas
   - Feedback visual cuando un jugador es eliminado
   - Feedback visual cuando se acierta/falla
   - Manejo de estados de conexión/desconexión

3. **Efectos y Animaciones Opcionales (0.25 puntos)**
   - Animación de countdown del temporizador
   - Animación cuando un jugador es eliminado
   - Animación de victoria
   - Transiciones suaves entre rondas
   - Efectos visuales al enviar mensajes

4. **Código Limpio y Documentado (0.25 puntos)**
   - Documentar todas las clases y funciones principales
   - Crear README.md con instrucciones
   - Comentarios en código complejo
   - Seguir convenciones de Kotlin

### Archivos a crear:
- `ui/welcome/WelcomeActivity.kt` - Pantalla de inicio
- `ui/lobby/LobbyActivity.kt` - Sala de espera
- `ui/lobby/LobbyViewModel.kt` - ViewModel del lobby
- `ui/result/ResultActivity.kt` - Pantalla de resultado
- `ui/dialogs/ExitGameDialog.kt` - Diálogo de salir
- `ui/dialogs/EmojiSelectorDialog.kt` - Selector de emoji
- `utils/AnimationUtils.kt` - Utilidades de animación
- `utils/Constants.kt` - Constantes del juego
- `res/layout/activity_welcome.xml` - Layout de bienvenida
- `res/layout/activity_lobby.xml` - Layout de lobby
- `res/layout/activity_result.xml` - Layout de resultado
- `res/layout/dialog_emoji_selector.xml` - Layout selector
- `res/values/strings.xml` - Strings de la app
- `res/values/colors.xml` - Colores
- `res/values/themes.xml` - Temas
- `README.md` - Documentación del proyecto

---

## 🔄 Coordinación entre Personas

### Dependencias:
1. **PERSONA 1 → PERSONA 2**: Los modelos de datos y Firebase deben estar listos primero
2. **PERSONA 1 → PERSONA 3**: La lógica del juego debe estar definida antes de las pantallas
3. **PERSONA 2 ↔ PERSONA 3**: Coordinación en diseño de UI para mantener consistencia

### Orden de Desarrollo Sugerido:
1. **Semana 1**: PERSONA 1 configura Firebase y modelos básicos
2. **Semana 2**: PERSONA 2 y 3 trabajan en paralelo en UI mientras PERSONA 1 completa lógica
3. **Semana 3**: Integración y pruebas conjuntas

---

## 📊 Resumen de Puntos por Persona

| Persona | Responsabilidad Principal | Puntos |
|---------|---------------------------|--------|
| **Persona 1** | Backend, Firebase, Lógica del Juego, Pruebas | 2.25 |
| **Persona 2** | Chat, UI Principal del Juego | 2.0 |
| **Persona 3** | Navegación, Flujo, Animaciones, Documentación | 1.5 |
| **TOTAL** | | **5.75** |

*Nota: El total es 5.75 porque hay 0.25 puntos adicionales de colaboración general*

---

## 🛠️ Tecnologías Comunes a Usar

- **Lenguaje**: Kotlin
- **Backend**: Firebase Realtime Database
- **Autenticación**: Firebase Anonymous Auth
- **UI**: Material Design 3
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Componentes**: Jetpack (ViewModel, LiveData)
- **Testing**: JUnit para pruebas unitarias

---

## 📝 Notas Importantes

1. Todos deben usar **nombres de paquetes consistentes**
2. Seguir el patrón **MVVM** para separación de responsabilidades
3. Usar **LiveData/StateFlow** para observar cambios
4. Implementar **manejo de errores** en todas las operaciones de Firebase
5. Considerar **casos edge**: desconexión, jugador abandona, etc.
6. Mantener **comunicación constante** entre el equipo

---

## 🎯 Criterios de Éxito

- ✅ El juego funciona completamente de principio a fin
- ✅ Múltiples dispositivos pueden jugar simultáneamente
- ✅ El chat funciona en tiempo real
- ✅ Los turnos se respetan correctamente
- ✅ La detección de victoria funciona
- ✅ La UI es intuitiva y funcional
- ✅ El código está limpio y documentado
- ✅ Hay al menos una prueba unitaria funcional
