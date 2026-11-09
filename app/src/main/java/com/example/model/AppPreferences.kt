package com.example.model

enum class AppThemeMode(val label: String, val description: String) {
    SYSTEM("Sistema", "Sigue el tema del dispositivo"),
    CYBERPUNK("Cyberpunk AniCal", "Púrpura y rosa neón intenso"),
    OLED("OLED Midnight", "Negro puro #000000 para ahorrar batería"),
    SAKURA("Sakura Pastel", "Tonos rosados y primaverales"),
    LIGHT("Claro Elegante", "Fondo blanco puro y limpio")
}

enum class StreamingPlatform(val displayName: String, val iconColorHex: Long) {
    ALL("Todas", 0xFF7C4DFF),
    CRUNCHYROLL("Crunchyroll", 0xFFF47521),
    NETFLIX("Netflix", 0xFFE50914),
    PRIME_VIDEO("Prime Video", 0xFF00A8E1),
    DISNEY_PLUS("Disney+", 0xFF113CCF),
    HIDIVE("HIDIVE", 0xFF00B4D8)
}
