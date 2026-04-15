# El Joc del Pingüí 🐧

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.10-blue?style=for-the-badge&logo=javafx)
![Oracle](https://img.shields.io/badge/Database-Oracle-red?style=for-the-badge&logo=oracle)
![Status](https://img.shields.io/badge/Status-Development-green?style=for-the-badge)

¡Bienvenido a **El Joc del Pingüí**! Un juego de tablero interactivo desarrollado en **JavaFX 21** donde la estrategia y la gestión de recursos son claves para sobrevivir al Ártico.

---

## 🛠️ Stack Tecnológico

| Componente | Versión / Detalle |
| :--- | :--- |
| **Lenguaje** | Java OpenJDK 21 |
| **GUI Framework** | JavaFX SDK 21.0.10 |
| **Módulos Core** | `controls`, `fxml`, `media`, `graphics` |
| **Persistencia** | Oracle Database (JDBC) con `ojdbc8.jar` |
| **Entorno** | Soporte para variables `.env` (gestión de secretos) |

---

## 📖 Documentación del Proyecto

### 1. Arquitectura del Sistema (MVC)
El proyecto implementa el patrón **Modelo-Vista-Controlador**:
- **Controlador**: Lógica de juego (`GestorTaulell`, `GestorPartida`, `AudioManager`).
- **Modelo**: Entidades (`Taulell`, `Pinguino`, `Foca`, `Casella`).
- **Vista**: Interfaces FXML y controladores de escena en los paquetes `resources` y `vista`.

### 2. Tablero y Tipos de Casillas
El tablero de 50 casillas es determinista y se genera mediante una cadena de semillas (Seed).
Aquí están las funcionalidades y efectos de los diferentes tipos de casillas:

- **⚪ Normal (0)**: No tiene ningún efecto especial. Casilla segura.
- **🐻 Ós / Oso (1)**: Casilla de peligro. El oso ataca de inmediato.
  - **Jugador (Pingüino)**: Si el jugador recibe un ataque, vuelve a la casilla de inicio (0). Si tiene **1 Pez**, puede decidir gastarlo para salvarse de volver al inicio.
  - **Foca**: Si la Foca cae en una casilla de Oso, tiene un **50% de probabilidad** de esquivarlo, y un **50% de probabilidad** de ser alcanzada (volviendo a la casilla 0).
- **🛷 Trineu / Trineo (2)**: Casilla de avance. El personaje viaja automáticamente a la próxima posición donde haya otro trineo. Si no hay ninguno delante, se queda en la misma casilla.
- **🕳️ Forat / Agujero (3)**: Casilla de retroceso. El jugador cae por el agujero y retrocede hasta el **Agujero inmediatamente anterior** en el tablero. Si es el primer agujero del tablero, vuelve a la casilla 0.
- **🎉 Event / Evento (4)**: Activa la **Ruleta de Premios** beneficiosa para el jugador. La ruleta tiene 4 posibles eventos con probabilidad equitativa (25% cada uno):
  - **1 Pez:** Se añade un pez al inventario (Límite: 2).
  - **1 a 3 Bolas de nieve:** Se consiguen aleatoriamente entre 1 y 3 bolas de nieve (Límite: 6).
  - **Dado rápido:** Un dado especial de alto movimiento que otorga resultados entre 5 y 10 (Límite: 3).
  - **Dado lento:** Un dado especial de bajo movimiento cauteloso de 1 a 3 (Límite: 3).
  - **Moto de Nieve:** Al obtener este "item", automaticamente seras teletrasportado hasta el trineo mas cercano (que tengas en frente, nunca te hara retroceder casillas).
  - **Perder turno:**
  - **Perder item:**
- **🧊 Trencadís / Hielo Frágil (5)**: Penalización dinámica según el peso (cantidad total de ítems en el inventario del Pingüino).
  - **0 ítems:** El hielo resiste, el jugador pasa sin penalización.
  - **1 a 5 ítems:** El jugador pierde un turno debido a que el hielo se quiebra ligeramente.
  - **Más de 5 ítems:** El peso es demasiado. El jugador cae al agua y es arrastrado de regreso a la **casilla inicial (0)**.

### 3. Mecánicas de Jugador vs Foca
La **Foca** patrulla el mapa y bloquea el paso, actuando como un obstáculo agresivo.

**Soborno a la Foca:**
Si el Pingüino tiene un **Pez**, puede usarlo para sobornar o alimentar a la foca. Hacer esto mantendrá a la Foca feliz y **bloqueada durante 2 turnos**, permitiendo al jugador, pero no a otros personajes (los demas que no hayan sobornado a la foca) pasar con total tranquilidad.

**Ataque de la Foca:**
Si el jugador no tiene un pez, o decide no dárselo, la Foca lo atacará irremediablemente. Dependiendo del contexto actual del jugador, la Foca activará distintas probabilidades de ataque:

1. **Contexto: Ventaja final (Casilla 40 o superior).**
   - **10% Pegar:** La foca pega al jugador, enviándolo al **Agujero (Forat) anterior**.
   - **25% Aplastar:** La foca aplasta al jugador, lo que **destruye absolutamente todos los ítems** del inventario (peces, bolas, dados).
2. **Contexto: Exceso de ítems (Más de 3 ítems totales).**
   - **25% Pegar.** 
   - **75% Aplastar:** La foca castiga la avaricia destruyendo el inventario.
3. **Contexto: Base (Normal).**
   - **50% Pegar / 50% Aplastar:** Al ser 50/50, se activa la **Ruleta Malvada**. Una ruleta visual decidirá la suerte y el tipo de castigo que recibirá el jugador.
La foca aparte es inteligente y a veces ignorara por completo la ruleta, aparte de que usara sus items a su favor mas frecuentemente.

### 4. Sistema de Sonido (AudioManager)
Sistema **Singleton** para audio ininterrumpido:
- Música de fondo (BGM) en bucle.
- Efectos de sonido (SFX) con gestión de memoria optimizada (auto-dispose).
- Controles de volumen independientes desde la configuración.

---

## ✨ Animaciones y Feedback Visual
Para mejorar la experiencia del usuario, el juego incluye un sistema de animaciones fluidas y notificaciones visuales:

- **Popups de Ítems**: Al recoger un objeto, aparece un icono flotante sobre el pingüino con una animación de ascenso y desvanecimiento (`Fade & Translate Transition`).
- **Ruletas Visuales**: 
  - **Ruleta de Eventos (Buena)**: Una ruleta animada estilo pixel-art que gira físicamente para otorgar los premios.
  - **Ruleta Malvada de la Foca**: En casos de 50/50 de probabilidad, entra en escena esta ruleta para decidir gráficamente el castigo que recibirá el Pingüino.
- **Alertas de Peligro**: 
  - **Ataque del Oso**: Destello visual en la casilla cuando el oso interactúa con el jugador.
  - **Caída en un Agujero**: El pingüino cae en un agujero con una animación de caída.
- **Interfaz Adaptativa**: La mesa de juego escala dinámicamente para adaptarse a cualquier resolución de pantalla (especialmente laptops).

---

## 🚀 Cómo empezar
1. Asegúrate de tener configurado el **JavaFX SDK 21** en tus variables de entorno o en tu IDE (Eclipse/IntelliJ).
2. Configura las credenciales de la base de datos en el archivo `.env` (puedes usar `sample.env` como guía).
3. Ejecuta la clase `controlador.Main`.
