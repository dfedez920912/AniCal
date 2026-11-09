# AniCal

AniCal es una aplicación Android nativa para consultar los estrenos de anime. El proyecto está desarrollado en Kotlin y utiliza Jetpack Compose para la interfaz. Según la descripción del proyecto, muestra un calendario de estrenos diarios y semanales con sinopsis y carátulas. También incluye recordatorios de episodios y un widget de cuenta regresiva para la pantalla de inicio.

> Proyecto en desarrollo. Las funciones que dependen de servicios externos pueden requerir credenciales y configuración adicionales.

## Funcionalidades

- Consulta de estrenos de anime por día y semana.
- Información de anime con sinopsis y carátulas.
- Recordatorios/notificaciones para estrenos de episodios.
- Widget de pantalla de inicio para mostrar la cuenta regresiva del próximo estreno.
- Interfaz construida con Jetpack Compose y tema de aplicación.

## Requisitos

- Android Studio con soporte para la versión de Android Gradle Plugin definida en el proyecto.
- JDK 17 para compilar con Gradle 9.
- Android SDK 36.1 (la configuración declara `compileSdk` 36 con minor API level 1).
- Android 7.0 (API 24) o posterior para instalar la aplicación (`minSdk = 24`).

## Compilar

Clona el repositorio y ejecuta desde la raíz:

```bash
git clone https://github.com/dfedez920912/AniCal.git
cd AniCal
./gradlew assembleDebug
```

En Windows, utiliza `gradlew.bat assembleDebug`. Gradle descarga las dependencias necesarias en la primera compilación. El APK de depuración se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Para ejecutar las pruebas unitarias:

```bash
./gradlew testDebugUnitTest
```

## Compilación de publicación

La variante `release` está configurada para firmarse con una clave de publicación. Para compilarla localmente, configura estas variables de entorno y proporciona un keystore válido con alias `upload`:

```bash
export KEYSTORE_PATH="/ruta/a/mi-upload-key.jks"
export STORE_PASSWORD="contraseña-del-keystore"
export KEY_PASSWORD="contraseña-de-la-clave"
./gradlew assembleRelease
```

En PowerShell, define las mismas variables con `$env:KEYSTORE_PATH`, `$env:STORE_PASSWORD` y `$env:KEY_PASSWORD`. El APK de publicación se genera en `app/build/outputs/apk/release/`.

**No subas al repositorio el keystore, sus contraseñas ni claves API.** El archivo `.env.example` contiene orientación de ejemplo sobre la clave de Gemini; no es una credencial válida. Si una función necesita un servicio externo, configura sus secretos de forma segura para tu entorno.

## Versiones y distribución

Las versiones se distribuyen como APK adjunto a una [GitHub Release](https://github.com/dfedez920912/AniCal/releases). Se recomienda etiquetar versiones con el formato `vMAJOR.MINOR.PATCH`, por ejemplo `v1.0.0`, y adjuntar notas de versión y un checksum SHA-256 junto al APK.

### Automatización de Releases (GitHub Actions)

Para compilar y publicar automáticamente un APK firmado cada vez que se suba una etiqueta `v*`, configura los siguientes secretos en **Settings → Secrets and variables → Actions**:

- `ANDROID_KEYSTORE_BASE64`: keystore de publicación codificado en Base64.
- `STORE_PASSWORD`: contraseña del keystore.
- `KEY_PASSWORD`: contraseña de la clave con alias `upload`.

El workflow debe ejecutarse ante `push` de tags `v*`, configurar Java 17 y Android SDK 36.1, decodificar el keystore en un archivo temporal (por ejemplo, en `$RUNNER_TEMP`), ejecutar `./gradlew testDebugUnitTest assembleRelease` y crear una GitHub Release que adjunte el APK y su checksum. El permiso `contents: write` debe estar habilitado para el job que crea la Release. No almacenes los secretos ni el keystore en el YAML o en el repositorio.

Actualmente, el módulo Android define `versionName = "1.0"` y `versionCode = 1` en `app/build.gradle.kts`. Para que las etiquetas generen metadatos de versión propios, conviene parametrizar esos valores en Gradle y pasarlos desde el workflow; el `versionCode` debe aumentar con cada versión publicada.

## Komi Store

AniCal se distribuye desde las Releases públicas de GitHub, que proporcionan el APK como un recurso descargable. Para facilitar que tiendas o catálogos que rastrean repositorios y releases puedan identificarlo:

- Mantén público el repositorio y publica versiones con etiquetas claras (`v1.0.0`).
- Adjunta el APK a cada Release con un nombre reconocible, por ejemplo `AniCal-v1.0.0.apk`.
- Incluye notas de versión y, de ser posible, un archivo `.sha256`.
- Completa la descripción del repositorio y añade temas pertinentes como `android`, `kotlin`, `anime-calendar` y `apk`.

La indexación depende de cómo Komi Store descubre y cataloga repositorios y releases; este README y los metadatos del proyecto no garantizan por sí solos que la aplicación sea indexada. No se agrega un manifiesto de tienda específico porque no se ha verificado un formato oficial requerido para Komi Store.

## Tecnologías

- Kotlin
- Android Gradle Plugin y Gradle Wrapper
- Jetpack Compose y Material 3
- Room, Retrofit, Moshi y Coroutines (dependencias declaradas)
- Firebase (incluyendo Firebase AI y App Check, según configuración)

## Estructura principal

- `app/`: aplicación Android, configuración de compilación y código fuente.
- `app/src/main/`: manifiesto, recursos y código de la aplicación.
- `gradle/libs.versions.toml`: versiones y catálogo de dependencias.
- `.env.example`: ejemplo/documentación de configuración de secretos; no contiene credenciales reales.

## Permisos Android

El manifiesto declara acceso a Internet y al estado de conectividad, notificaciones, alarmas exactas y vibración. Android puede requerir que el usuario autorice notificaciones o el uso de alarmas exactas, dependiendo de la versión y configuración del dispositivo.

## Licencia

No se identificó un archivo de licencia en la raíz al documentar este README. Antes de redistribuir o reutilizar el código, consulta al autor y añade aquí la licencia que corresponda.
