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

### 2. Sistema de Generación de Semillas (Seed)
El tablero de 50 casillas es determinista y se genera mediante una cadena de 50 dígitos (0-5).
- **Tipos de casillas**: Normal (0), Ós (1), Trineu (2), Forat (3), Event (4), Trencadís (5).
- **Validación del Tablero**: Controles automáticos para asegurar distancias mínimas entre peligros, zonas de inicio/final y una distribución equilibrada de eventos.

### 3. Sistema de Menús y Navegación
Gestión centralizada mediante la clase `Main.java` con soporte para:
- **Cache de Escenas**: Almacenamiento dinámico de escenas para una navegación fluida y segura.
- **Modos de Pantalla**: Alternancia suave entre pantalla completa y modo ventana.
- **Carga Asíncrona**: Inicialización de recursos y audio en segundo plano durante la pantalla de bienvenida.

### 4. Mecánicas de Juego Core
- **Dados Dinámicos**: Uso de dados estándar (1-6) y dados especiales (Rápido: 5-10, Lento: 1-3).
- **Peligros en el Camino**:
  - **Ós (Oso)**: Ataque directo que resetea la posición a menos que uses un **Peix**.
  - **Trencadís (Hielo Frágil)**: Penalización basada en el número de ítems en el inventario (peso).
  - **Forat (Agujero)**: Caída con retroceso al último punto de control.
- **Interacción con Entidades**: La **Foca** bloquea el paso; puedes sobornarla con pescado para pasar o arriesgarte a ser golpeado.

### 5. Sistema de Sonido (AudioManager)
Sistema **Singleton** para audio ininterrumpido:
- Música de fondo (BGM) en bucle.
- Efectos de sonido (SFX) con gestión de memoria optimizada (auto-dispose).
- Controles de volumen independientes desde la configuración.

---

## ✨ Animaciones y Feedback Visual
Para mejorar la experiencia del usuario, el juego incluye un sistema de animaciones fluidas y notificaciones visuales:

- **Popups de Ítems**: Al recoger un objeto, aparece un icono flotante sobre el pingüino con una animación de ascenso y desvanecimiento (`Fade & Translate Transition`).
- **Ruleta de Eventos**: Una ruleta animada estilo pixel-art que gira físicamente para determinar de forma aleatoria los premios del jugador.
- **Alertas de Peligro**: 
  - **Ataque del Oso**: Destello visual en la casilla cuando el oso interactúa con el jugador.
  - **Movimiento de Ficha**: Desplazamiento suave entre casillas mediante transiciones secuenciales.
  - **Caída en un Agujero**: El pingüino cae en un agujero con una animación de caída.
- **Interfaz Adaptativa**: La mesa de juego escala dinámicamente para adaptarse a cualquier resolución de pantalla (especialmente laptops).

---

## 🚀 Cómo empezar
1. Asegúrate de tener configurado el **JavaFX SDK 21** en tus variables de entorno o en tu IDE (Eclipse/IntelliJ).
2. Configura las credenciales de la base de datos en el archivo `.env` (puedes usar `sample.env` como guía).
3. Ejecuta la clase `controlador.Main`.
