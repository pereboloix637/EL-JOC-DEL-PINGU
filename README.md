
![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.10-blue?style=for-the-badge&logo=javafx)
![Oracle](https://img.shields.io/badge/Database-Oracle-red?style=for-the-badge&logo=oracle)
![Status](https://img.shields.io/badge/Status-Development-green?style=for-the-badge)

¡Bienvenido a **El Joc del Pingüí**! Un juego de tablero interactivo desarrollado en **JavaFX 21** donde la estrategia y la gestión de recursos son claves para sobrevivir al *tablero helado*.
A continuacion veran los **componentes**, los **tipos de casillas**, los **eventos** y el comportamiento del **Pingu** y la **Foca**.

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

---

### 2. Tablero y Tipos de Casillas
El tablero de 50 casillas se genera mediante una cadena de semillas (Seed). Cada tipo de casilla tiene efectos únicos:

- **⚪ Normal (0)**: Sin efectos especiales. Casilla segura.
- **🐻 Ós / Oso (1)**: Casilla de peligro. 
  - **Jugador**: Si es alcanzado, vuelve a la casilla inicial (0). Puede gastar **1 Pez** para salvarse.
  - **Foca**: Tiene un **50% de probabilidad** de esquivarlo. Si falla, vuelve a la casilla 0.
- **🛷 Trineu / Trineo (2)**: El personaje viaja automáticamente a la próxima posición donde haya otro trineo. Si no hay ninguno delante, permanece en la misma casilla.
- **🕳️ Forat / Agujero (3)**: El jugador retrocede hasta el **agujero inmediatamente anterior** en el tablero (o a la casilla 0 si no hay ninguno anterior).
- **🎉 Event / Evento (4)**: Activa la **Ruleta de Premios** con los siguientes efectos:
  - **Pez (15%)**: Añade un pez al inventario (Límite: 2).
  - **Bolas de Nieve (15%)**: Obtienes entre 1 y 3 bolas (Límite: 6).
  - **DADOS ESPECIALES (Límite consolidado: 3 dados especiales).**
    - **Dado Rápido (14%)**: Movimiento entre 5 y 10.
    - **Dado Lento (14%)**: Movimiento estratégico de 1 o 3 casillas.
  - **Moto de Nieve (14%)**: Teletransporta instantáneamente al próximo trineo disponible en el camino.
  - **Perder Turno (14%)**: El jugador queda bloqueado el siguiente turno.
  - **Perder Ítem (14%)**: Se elimina un ítem aleatorio del inventario.
- **🧊 Trencadís / Hielo Frágil (5)**: Penalización basada en el peso (total de ítems en el inventario):
  - **0 ítems**: Cruzas sin penalización.
  - **1 a 5 ítems**: El hielo se agrieta y pierdes **1 turno**.
  - **Más de 5 ítems**: El hielo se rompe. Vuelves a la **casilla inicial (0)**.

---

### 3. Mecánicas de la Foca Inteligente
La **Foca** ya no es un simple obstáculo; es un oponente controlado por una IA táctica que gestiona su propio inventario y toma decisiones para ganar o entorpecer al jugador.

#### 🧠 Inteligencia Táctica (Turno de la Foca)
Durante su turno, la foca analiza el estado del tablero y decide usar sus ítems recolectados:
1. **Modo Acelerar**: Si el líder le saca más de 10 casillas de ventaja, usará un **Dado Rápido**.
2. **Modo Embestida**: Si tiene a un jugador a menos de 3 casillas, usará un **Dado Lento** para intentar aterrizar exactamente sobre él y atacarlo.
3. **Ataque a Distancia**: Si hay un jugador a menos de 4 casillas, le lanzará una **Bola de Nieve** para hacerlo retroceder 2 espacios.

#### ⚔️ Interacciones y Ataques
- **Aplastar (Paso por encima)**: Si la foca pasa por encima de un pingüino durante su movimiento, lo aplasta automáticamente **robándole la mitad de sus ítems** (peces, bolas y dados).
- **Pegar (Colisión)**: Si la foca aterriza sobre un jugador, o un jugador aterriza sobre ella, la foca le golpea con la cola enviándolo al **agujero anterior**.
- **Sobornar a la Foca**: Si tienes un **Pez**, puedes elegir alimentarla al encontrártela. Esto la mantendrá feliz y bloqueada para ti durante **2 turnos**, permitiéndote pasar con seguridad.

---

## ✨ Animaciones y Feedback Visual
- **Popups de Ítems**: Iconos flotantes animados al recoger objetos.
- **Ruleta de Eventos**: Sistema visual animado con físicas de giro estilo pixel-art.
- **Efectos de Casilla**: Animaciones de caída en agujeros, destellos de ataque del oso y transiciones de movimiento fluido.
- **Confeti**: Celebración visual al obtener grandes premios en la ruleta.
- **Interfaz Adaptativa**: Escalado dinámico para resoluciones HD y dispositivos portátiles.

---

## 🚀 Cómo empezar
1. Configura el **JavaFX SDK 21** en tu entorno o IDE.
2. Configura las credenciales de la base de datos en el archivo `.env`.
3. Ejecuta la clase `controlador.Main`.
