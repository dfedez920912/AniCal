# Guía para contribuir

¡Gracias por tu interés en mejorar AniVexa! Las propuestas de cambios, correcciones de errores y mejoras de documentación son bienvenidas.

## Antes de empezar

1. Revisa los issues existentes para evitar trabajo duplicado.
2. Para cambios grandes, abre primero un issue y describe el problema o la propuesta.
3. Para vulnerabilidades, no abras un issue público: sigue las instrucciones de [SECURITY.md](SECURITY.md).
4. Lee el [Código de conducta](CODE_OF_CONDUCT.md).

## Preparar el entorno

- JDK 25, según la versión configurada en `gradle/gradle-daemon-jvm.properties`.
- Android SDK con la plataforma Android 36.
- Android Studio (recomendado) o Gradle Wrapper incluido en el repositorio.

No incluyas `local.properties`, archivos `.env`, claves de firma ni otros secretos en tus cambios.

## Flujo de trabajo

1. Crea una rama descriptiva desde la rama principal.
2. Mantén cada cambio enfocado y sigue los patrones existentes de Kotlin y Jetpack Compose.
3. Añade o actualiza pruebas cuando cambie el comportamiento.
4. Ejecuta las comprobaciones pertinentes:
   - Windows: `gradlew.bat testDebugUnitTest`
   - Linux/macOS: `./gradlew testDebugUnitTest`
   - Para comprobar la compilación, usa `gradlew.bat assembleDebug` o `./gradlew assembleDebug`, respectivamente.
5. Abre un pull request con el objetivo, los cambios y los resultados de las pruebas. Incluye capturas si modificaste la interfaz.

El flujo de publicación de GitHub Actions ejecuta las pruebas unitarias y compila el APK de release firmado. No incluyas credenciales ni claves de firma en commits o pull requests.

## Issues y pull requests

Usa las plantillas del repositorio para reportar errores o proponer funcionalidades. Mantén las descripciones concretas y proporciona pasos para reproducir los problemas. Las contribuciones se revisan antes de integrarse; puede que se soliciten cambios para mantener la calidad y coherencia del proyecto.
