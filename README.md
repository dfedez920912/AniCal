# AniCal

AniCal es una aplicación Android nativa para consultar los estrenos de anime. El proyecto está desarrollado en Kotlin y utiliza Jetpack Compose para la interfaz. Según la descripción del proyecto, muestra un calendario de estrenos diarios y semanales con sinopsis y carátulas. También incluye recordatorios de episodios y un widget de cuenta regresiva para la pantalla de inicio.

## Funcionalidades

- Consulta de estrenos de anime por día y semana.
- Información de anime con sinopsis y carátulas.
- Recordatorios/notificaciones para estrenos de episodios.
- Widget de pantalla de inicio de 4x3 para mostrar el listado de estrenos del dia.
- Widget de pantalla de inicio de 1x1 para mostrar la cantidad de estrenos del dia.
- Interfaz construida con Jetpack Compose y tema de aplicación.

## Requisitos

- Android Studio con soporte para la versión de Android Gradle Plugin definida en el proyecto.
- JDK 17 para compilar con Gradle 9.
- Android SDK 36.1 (la configuración declara `compileSdk` 36 con minor API level 1).
- Android 7.0 (API 24) o posterior para instalar la aplicación (`minSdk = 24`).

## Versiones y distribución

Las versiones se distribuyen como APK adjunto a una [GitHub Release](https://github.com/dfedez920912/AniCal/releases). Se recomienda etiquetar versiones con el formato `vMAJOR.MINOR.PATCH`, por ejemplo `v1.0.0`, y adjuntar notas de versión y un checksum SHA-256 junto al APK.

## Tecnologías

- Kotlin
- Android Gradle Plugin y Gradle Wrapper
- Jetpack Compose y Material 3
- Room, Retrofit, Moshi y Coroutines (dependencias declaradas)
- Firebase (incluyendo Firebase AI y App Check, según configuración)


## Licencia

Este proyecto se distribuye bajo la licencia GNU General Public License v2 (GPL-2.0). Puedes consultar el texto completo en [`LICENSE`](./LICENSE).

La GPL v2 garantiza que el software siga siendo libre y que cualquier modificación o redistribución se realice con los mismos derechos de acceso al código fuente y a la licencia. Antes de redistribuir versiones derivadas, asegúrate de mantener la licencia y las notas de copyright correspondientes.
