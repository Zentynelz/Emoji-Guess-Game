# División de Trabajo

Aplicación Android con Kotlin para juego multijugador en línea.

## PERSONA 1 - Backend & Firebase

### Responsabilidades:

**1. Integración con Firebase (1.0 punto)**
- Configurar Firebase en el proyecto
- Implementar Firebase Realtime Database
- Estructura de datos (salas, jugadores, estado, turnos)
- Listeners para cambios en tiempo real
- Autenticación anónima

**2. Lógica del Juego (1.0 punto)**
- Sistema de asignación aleatoria de emojis
- Control de turnos entre jugadores
- Temporizador de ronda
- Validación de respuestas
- Sistema de eliminación
- Detección de victoria
- Reasignación de emojis por ronda

**3. Pruebas Unitarias (0.25 puntos)**
- Pruebas de asignación de emojis
- Pruebas de validación de respuestas
- Pruebas de detección de victoria

## PERSONA 2 - Chat & Comunicación

### Responsabilidades:

**1. Sistema de Chat (1.5 puntos)**
- Chat global en tiempo real con Firebase
- Diseño de interfaz del chat
- Input de texto para mensajes
- Mostrar nombre del jugador
- Timestamp de mensajes
- Auto-scroll
- Sincronización en tiempo real

**2. Interfaz de Usuario - Pantalla de Juego (0.5 puntos)**
- Diseño de pantalla principal del juego
- Visualización de emojis de otros jugadores
- Emoji oculto del jugador actual ("?")
- Grid de jugadores con emojis
- Indicador visual del turno actual
- Temporizador visible
- Selector de emoji
- Integración del chat

## PERSONA 3 - UI/UX & Flujo de Navegación

### Responsabilidades:

**1. Pantallas de Navegación y Lobby (0.5 puntos)**
- Pantalla de inicio/bienvenida
- Pantalla para crear sala
- Pantalla para unirse a sala
- Pantalla de lobby (espera de jugadores)
- Lista de jugadores en espera
- Botón para iniciar partida (host)

**2. Manejo de Eventos y Flujo del Juego (0.5 puntos)**
- Botón para salir del juego
- Diálogo de confirmación
- Pantalla de victoria/derrota
- Animaciones de transición
- Feedback visual (eliminación, acierto/fallo)
- Manejo de estados de conexión

**3. Efectos y Animaciones (0.25 puntos)**
- Animación de countdown
- Animación de eliminación
- Animación de victoria
- Transiciones entre rondas
- Efectos visuales en mensajes

**4. Código Limpio y Documentado (0.25 puntos)**
- Documentar clases y funciones
- Crear README.md
- Comentarios en código complejo
- Seguir convenciones de Kotlin

## Distribución de Puntos

- Persona 1: 2.25 puntos
- Persona 2: 2.0 puntos
- Persona 3: 1.5 puntos

**Total:** 5.75 puntos
