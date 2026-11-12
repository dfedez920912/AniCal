# AniVexa v0.0.1 - Notas de la versión

## 🌟 Novedades Destacadas

### 🔄 Transición a AniVexa y Nuevo Icono Adaptativo
- **Nueva Identidad**: La aplicación y repositorio pasan a llamarse oficialmente **AniVexa**. Se actualizaron todos los identificadores en la aplicación, configuración de compilación y metadatos del proyecto.
- **Icono Oficial AniVexa**: Se incorporó el nuevo icono adaptativo con estética cyberpunk, integrando el logotipo de neón cian brillante con la leyenda "AniVexa" y soporte para iconos circulares en todas las densidades de pantalla.

---

### ⚡ Núcleo Backend Centralizado Embebido (In-App Core)
- **Motor Interno en la Aplicación**: El backend corre directamente dentro del proceso de la misma aplicación como núcleo central de red. No requiere ningún servidor externo ni configuración de IP para funcionar en el teléfono móvil del usuario.
- **Centralización y Cumplimiento de `api.md`**:
  - Centraliza todas las consultas a la API GraphQL de AniList (`https://graphql.anilist.co`).
  - **Caché en memoria**: 6 horas para temporadas, 30 minutos para horarios y 24 horas para fichas de detalles y reseñas.
  - **Control de Cuota (Rate Limit)**: Protección contra bloqueos 429 respetando los límites de 30-90 req/min de AniList.
  - **Filtro Seguro**: Exclusión automática de contenido para adultos (`isAdult: false`).
- **Backend Standalone en Rust (`backend-rust/`)**: Se incluye adicionalmente el servidor completo en Rust (Axum + Tokio) con los mismos endpoints para despliegues independientes en servidor o contenedores Docker.

---

### 📱 Nuevo Frontend en Flutter (`frontend-flutter/`)
- **Experiencia Fluida y Animaciones**: Se creó la base completa de la aplicación en **Flutter** con animaciones fluidas (`AnimatedSwitcher`, transiciones dinámicas entre pestañas y tarjetas).
- **Modelos y Servicios**: Modelos fuertemente tipados (`AnimeItem`, `AiringScheduleItem`) y servicio HTTP configurado para consultar directamente el backend en Rust.
- **Módulos**: Calendario de emisión, vistas diaria y semanal, y gestión de lista de seguimiento (*Watchlist*).

---

### 🎨 Experiencia de Usuario y Nuevas Pantallas
- **Splash Screen**: Pantalla de presentación con animación de entrada e icono de AniVexa.
- **Onboarding Interactivo**: Pantalla de bienvenida que guía al usuario por las principales funciones de la app (guardado en preferencias locales).
- **Widgets de Inicio**:
  - Widget contador 1x1 con los estrenos pendientes del día.
  - Widget de lista desplazable 3x2 con los animes de hoy.
  - Widget de cuenta regresiva para el próximo episodio.
- **Caché sin Conexión con Room**: Respaldo automático en base de datos local para consultar detalles y la lista de favoritos incluso sin conexión a internet.

---

### 🛠️ Correcciones y Mejoras
- **Icono Oficial en Flutter**: Se incluyeron todos los recursos del icono adaptativo oficial de AniVexa en la estructura de Android de Flutter, resolviendo el problema donde se mostraba el icono genérico de Flutter.
- **Permisos de Red y Tráfico**: Se agregaron explícitamente los permisos `android.permission.INTERNET`, `ACCESS_NETWORK_STATE` y `usesCleartextTraffic="true"` en el `AndroidManifest.xml` de Flutter para evitar excepciones `Operation not permitted (errno 1)`.
- **Conectividad con Respaldo Inteligente**: Se implementó tolerancia a fallos en `ApiService`: si el servidor backend local en Rust no está encendido o la dirección local no es alcanzable desde el dispositivo, la app consulta directamente la API pública de AniList o utiliza los datos fuera de línea sin mostrar pantallas de error.
- **Selector de Servidor en Ajustes**: Se añadió un diálogo en la barra superior para configurar la IP o URL del backend de forma interactiva.

---

## 📦 Descargas

El paquete APK generado por el workflow de GitHub Actions se encuentra disponible en los recursos adjuntos de este release.
