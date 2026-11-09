package com.example.data

import com.example.model.AnimeRelease
import java.time.LocalDate
import java.time.ZoneId

object OfflineAnimeData {

    /**
     * Generates a rich, realistic schedule for the current week starting from today,
     * including both series premieres (Ep 1) and ongoing daily episodes (Ep 2+),
     * with synopses in both Spanish and English for seamless translation switching.
     */
    fun getCuratedWeekSchedule(today: LocalDate = LocalDate.now()): List<AnimeRelease> {
        val zoneId = ZoneId.systemDefault()
        val list = mutableListOf<AnimeRelease>()

        fun epochFor(date: LocalDate, hour: Int, minute: Int): Long {
            return date.atTime(hour, minute).atZone(zoneId).toEpochSecond()
        }

        // ==========================================
        // Day 0: HOY (Today)
        // ==========================================
        val day0 = today

        // Episodio del día en emisión: Solo Leveling (Ep 8)
        list.add(
            AnimeRelease(
                id = 16498,
                airingId = 1001,
                titleRomaji = "Solo Leveling: Arise from the Shadow",
                titleEnglish = "Solo Leveling Season 2",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx176496-eB1C33tC8JmU.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/176496-XfSsmP6K0y7Z.jpg",
                synopsis = "Tras despertar poderes inauditos en las mazmorras subterráneas, Sung Jinwoo continúa su ascenso como el Cazador de Rango S más temido y respetado. Ahora debe proteger a su familia y enfrentar las amenazas de monarcas que planean destruir la humanidad.",
                synopsisEs = "Tras despertar poderes inauditos en las mazmorras subterráneas, Sung Jinwoo continúa su ascenso como el Cazador de Rango S más temido y respetado. Ahora debe proteger a su familia y enfrentar las amenazas de monarcas que planean destruir la humanidad.",
                synopsisEn = "After awakening unprecedented powers in mysterious underground dungeons, Sung Jinwoo continues his legendary rise as humanity's strongest S-Rank Hunter, facing monarch threats and protecting the world.",
                episode = 8,
                totalEpisodes = 13,
                airingAtSeconds = epochFor(day0, 17, 30),
                genres = listOf("Acción", "Fantasía", "Aventura"),
                score = 8.6,
                studio = "A-1 Pictures",
                format = "TV",
                streamingPlatform = "Crunchyroll"
            )
        )

        // Episodio del día en emisión: Dandadan (Ep 9)
        list.add(
            AnimeRelease(
                id = 171018,
                airingId = 1002,
                titleRomaji = "Dandadan",
                titleEnglish = "DAN DA DAN",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx171018-bT1fK8D2mS5f.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/171018-8YF5fQ5f2n0G.jpg",
                synopsis = "Momo Ayase cree en fantasmas y Okarun cree en extraterrestres. Para probar quién tiene razón visitan lugares ocultos, desatando una caótica aventura repleta de poderes y humor.",
                synopsisEs = "Momo Ayase cree en fantasmas y Okarun cree en extraterrestres. Para probar quién tiene razón visitan lugares ocultos, desatando una caótica aventura repleta de poderes y humor.",
                synopsisEn = "High school girl Momo believes in ghosts but not aliens, while UFO-fanatic Okarun believes in aliens but not ghosts. Together they stumble into occult chaos and wild supernatural battles.",
                episode = 9,
                totalEpisodes = 12,
                airingAtSeconds = epochFor(day0, 19, 0),
                genres = listOf("Acción", "Comedia", "Sobrenatural", "Sci-Fi"),
                score = 8.7,
                studio = "Science SARU",
                format = "TV",
                streamingPlatform = "Netflix"
            )
        )

        // Episodio del día en emisión: One Piece (Ep 1124)
        list.add(
            AnimeRelease(
                id = 21,
                airingId = 1003,
                titleRomaji = "One Piece",
                titleEnglish = "One Piece: Egghead Arc",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/nx21-tXfaAljA2PUE.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/21-wf37VakJReke.jpg",
                synopsis = "Luffy y los Sombrero de Paja desembarcan en la Isla Egghead, laboratorio central del científico más brillante del mundo, el Dr. Vegapunk, desvelando verdades cruciales sobre el mundo.",
                synopsisEs = "Luffy y los Sombrero de Paja desembarcan en la Isla Egghead, laboratorio central del científico más brillante del mundo, el Dr. Vegapunk, desvelando verdades cruciales sobre el mundo.",
                synopsisEn = "Luffy and the Straw Hats arrive at the futuristic island of Egghead, the research sanctuary of Dr. Vegapunk, uncovering forbidden world history and facing cipher pol threats.",
                episode = 1124,
                totalEpisodes = null,
                airingAtSeconds = epochFor(day0, 10, 0),
                genres = listOf("Aventura", "Acción", "Fantasía", "Comedia"),
                score = 8.9,
                studio = "Toei Animation",
                format = "TV",
                streamingPlatform = "Crunchyroll"
            )
        )

        // Nuevo Estreno de Temporada: Sakamoto Days (Ep 1)
        list.add(
            AnimeRelease(
                id = 177937,
                airingId = 1004,
                titleRomaji = "Sakamoto Days",
                titleEnglish = "SAKAMOTO DAYS",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx177937-sakamoto.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/177937-sakamotob.jpg",
                synopsis = "Taro Sakamoto fue el asesino a sueldo más temido de los bajos fondos hasta que se enamoró, se retiró y abrió una tienda de ultramarinos. Ahora debe defender su vida pacífica familiar de antiguos colegas asesinos.",
                synopsisEs = "Taro Sakamoto fue el asesino a sueldo más temido de los bajos fondos hasta que se enamoró, se retiró y abrió una tienda de ultramarinos. Ahora debe defender su vida pacífica familiar de antiguos colegas asesinos.",
                synopsisEn = "Taro Sakamoto was the ultimate hitman revered by all assassins until he fell in love and retired to run a neighborhood convenience store. Now hitmen are after him!",
                episode = 1,
                totalEpisodes = 12,
                airingAtSeconds = epochFor(day0, 21, 30),
                genres = listOf("Acción", "Comedia"),
                score = 8.5,
                studio = "TMS Entertainment",
                format = "TV",
                streamingPlatform = "Netflix"
            )
        )

        // ==========================================
        // Day 1: MAÑANA (Tomorrow)
        // ==========================================
        val day1 = today.plusDays(1)

        // Episodio en emisión: Bleach TYBW Part 3 (Ep 7)
        list.add(
            AnimeRelease(
                id = 163134,
                airingId = 1005,
                titleRomaji = "Bleach: Sennen Kessen-hen - Soukoku-tan",
                titleEnglish = "Bleach: Thousand-Year Blood War Part 3",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx163134-rFh3H0q5j3G2.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/163134-vA9r5C5r0T8m.jpg",
                synopsis = "La batalla decisiva entre los Shinigami de la Sociedad de Almas y los Quincy de Yhwach alcanza el Palacio Real. Ichigo Kurosaki encara su destino definitivo.",
                synopsisEs = "La batalla decisiva entre los Shinigami de la Sociedad de Almas y los Quincy de Yhwach alcanza el Palacio Real. Ichigo Kurosaki encara su destino definitivo.",
                synopsisEn = "The bloody conflict between Soul Reapers and Quincies escalates in the Royal Palace as Ichigo reaches the summit to confront Yhwach.",
                episode = 7,
                totalEpisodes = 13,
                airingAtSeconds = epochFor(day1, 16, 30),
                genres = listOf("Acción", "Aventura", "Sobrenatural"),
                score = 8.8,
                studio = "Pierrot Films",
                format = "TV"
            )
        )

        // Episodio en emisión: Re:Zero Season 3 (Ep 6)
        list.add(
            AnimeRelease(
                id = 166700,
                airingId = 1006,
                titleRomaji = "Re:Zero kara Hajimeru Isekai Seikatsu 3rd Season",
                titleEnglish = "Re:ZERO -Starting Life in Another World- Season 3",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx163134-xK1mQ8N5p0Rt.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/163134-re0banner.jpg",
                synopsis = "En la ciudad de las compuertas de Priestella, Subaru y Emilia son sorprendidos por un asalto coordinado de los Arzobispos del Culto de la Bruja.",
                synopsisEs = "En la ciudad de las compuertas de Priestella, Subaru y Emilia son sorprendidos por un asalto coordinado de los Arzobispos del Culto de la Bruja.",
                synopsisEn = "A year after the Sanctuary crisis, Subaru and the Emilia camp travel to the water city Priestella, only to find the Sin Archbishops laying siege.",
                episode = 6,
                totalEpisodes = 16,
                airingAtSeconds = epochFor(day1, 18, 0),
                genres = listOf("Drama", "Fantasía", "Psicológico", "Suspenso"),
                score = 8.5,
                studio = "White Fox",
                format = "TV"
            )
        )

        // Nuevo Estreno: Gachiakuta (Ep 1)
        list.add(
            AnimeRelease(
                id = 177938,
                airingId = 1007,
                titleRomaji = "Gachiakuta",
                titleEnglish = "Gachiakuta",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx177938-gachiakuta.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/177938-gachiakutab.jpg",
                synopsis = "Rudo, un joven de los suburbios acusado falsamente de asesinato, es arrojado al Abismo de la basura. Allí descubre misteriosos artefactos con alma y poderes letales para clamar venganza.",
                synopsisEs = "Rudo, un joven de los suburbios acusado falsamente de asesinato, es arrojado al Abismo de la basura. Allí descubre misteriosos artefactos con alma y poderes letales para clamar venganza.",
                synopsisEn = "Framed for a murder he did not commit, Rudo is thrown into the Abyss where all of society's garbage is dumped. There, he discovers vital soul weapons called Jinki.",
                episode = 1,
                totalEpisodes = 24,
                airingAtSeconds = epochFor(day1, 20, 0),
                genres = listOf("Acción", "Fantasía Oscura", "Shounen"),
                score = 8.4,
                studio = "BONES",
                format = "TV"
            )
        )

        // ==========================================
        // Day 2: Day + 2
        // ==========================================
        val day2 = today.plusDays(2)
        list.add(
            AnimeRelease(
                id = 145139,
                airingId = 1008,
                titleRomaji = "Blue Lock vs. U-20 JAPAN",
                titleEnglish = "BLUE LOCK Season 2",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx163134-bluelock.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/163134-bluelockb.jpg",
                synopsis = "Los delanteros egocéntricos de Blue Lock saltan al terreno contra el equipo nacional Sub-20 en un duelo decisivo.",
                synopsisEs = "Los delanteros egocéntricos de Blue Lock saltan al terreno contra el equipo nacional Sub-20 en un duelo decisivo.",
                synopsisEn = "The Blue Lock striker project must prove its worth against Japan's national U-20 team in a high-stakes football clash.",
                episode = 6,
                totalEpisodes = 14,
                airingAtSeconds = epochFor(day2, 17, 0),
                genres = listOf("Deportes", "Acción", "Drama"),
                score = 8.1,
                studio = "8bit",
                format = "TV"
            )
        )
        list.add(
            AnimeRelease(
                id = 171019,
                airingId = 1009,
                titleRomaji = "Dragon Ball DAIMA",
                titleEnglish = "Dragon Ball DAIMA",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx171019-daima.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/171019-daimab.jpg",
                synopsis = "Goku y sus amigos son convertidos en niños por una conspiración y se adentran en el enigmático Reino Demoníaco.",
                synopsisEs = "Goku y sus amigos son convertidos en niños por una conspiración y se adentran en el enigmático Reino Demoníaco.",
                synopsisEn = "Due to a conspiracy, Goku and his friends are turned into kids, embarking on an epic adventure into the mysterious Demon Realm.",
                episode = 5,
                totalEpisodes = 20,
                airingAtSeconds = epochFor(day2, 18, 30),
                genres = listOf("Acción", "Aventura", "Comedia", "Fantasía"),
                score = 8.0,
                studio = "Toei Animation",
                format = "TV"
            )
        )

        // ==========================================
        // Day 3: Day + 3
        // ==========================================
        val day3 = today.plusDays(3)
        list.add(
            AnimeRelease(
                id = 173599,
                airingId = 1011,
                titleRomaji = "SPY x FAMILY Season 3",
                titleEnglish = "SPY x FAMILY Season 3",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx173599-spy.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/173599-spyb.jpg",
                synopsis = "Anya, Loid y Yor continúan sus misiones secretas combinando comedia familiar y espionaje de alto riesgo.",
                synopsisEs = "Anya, Loid y Yor continúan sus misiones secretas combinando comedia familiar y espionaje de alto riesgo.",
                synopsisEn = "The fake Forger family balances domestic bliss with top-secret spy, assassin, and telepathic school adventures.",
                episode = 4,
                totalEpisodes = 12,
                airingAtSeconds = epochFor(day3, 16, 0),
                genres = listOf("Comedia", "Acción", "Vida Cotidiana"),
                score = 8.6,
                studio = "WIT Studio / CloverWorks",
                format = "TV"
            )
        )
        // Gran estreno de película: Chainsaw Man Reze
        list.add(
            AnimeRelease(
                id = 166701,
                airingId = 1012,
                titleRomaji = "Chainsaw Man: Reze-hen",
                titleEnglish = "Chainsaw Man: The Movie - Reze Arc",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx166701-csm.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/166701-csmb.jpg",
                synopsis = "Denji conoce a Reze en un café. Un romance explosivo que desata los planes de un letal demonio bombardero.",
                synopsisEs = "Denji conoce a Reze en un café. Un romance explosivo que desata los planes de un letal demonio bombardero.",
                synopsisEn = "Denji falls head over heels for an enigmatic cafe waitress named Reze, only to face an explosive romantic trap.",
                episode = 1,
                totalEpisodes = 1,
                airingAtSeconds = epochFor(day3, 21, 0),
                genres = listOf("Acción", "Sobrenatural", "Gore", "Romance"),
                score = 9.0,
                studio = "MAPPA",
                format = "Película"
            )
        )

        // ==========================================
        // Day 4: Day + 4
        // ==========================================
        val day4 = today.plusDays(4)
        list.add(
            AnimeRelease(
                id = 154588,
                airingId = 1013,
                titleRomaji = "Kimetsu no Yaiba: Mugen Jou-hen",
                titleEnglish = "Demon Slayer: Kimetsu no Yaiba - Infinity Castle",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx154588-kny.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/154588-knyb.jpg",
                synopsis = "Tanjiro y los Pilares caen en el laberinto infinito de Muzan para librar la batalla definitiva de la humanidad.",
                synopsisEs = "Tanjiro y los Pilares caen en el laberinto infinito de Muzan para librar la batalla definitiva de la humanidad.",
                synopsisEn = "Tanjiro and the Hashira fall deep into Muzan's shifting Infinity Castle for the final war against the Upper Ranks.",
                episode = 1,
                totalEpisodes = 3,
                airingAtSeconds = epochFor(day4, 18, 0),
                genres = listOf("Acción", "Fantasía", "Sobrenatural"),
                score = 9.1,
                studio = "ufotable",
                format = "Película"
            )
        )
        list.add(
            AnimeRelease(
                id = 176498,
                airingId = 1014,
                titleRomaji = "Jujutsu Kaisen: Shimetsukai-hen",
                titleEnglish = "Jujutsu Kaisen: Culling Game",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx176498-jjk.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/176498-jjkb.jpg",
                synopsis = "Kenjaku da inicio al 'Juego del Sacrificio'. Yuji y Megumi deben entrar a las colonias para salvar a los hechiceros.",
                synopsisEs = "Kenjaku da inicio al 'Juego del Sacrificio'. Yuji y Megumi deben entrar a las colonias para salvar a los hechiceros.",
                synopsisEn = "In the wake of Shibuya, Kenjaku initiates the Culling Game battle royale, forcing Yuji and friends into lethal colonies.",
                episode = 1,
                totalEpisodes = 24,
                airingAtSeconds = epochFor(day4, 20, 30),
                genres = listOf("Acción", "Sobrenatural", "Fantasía Oscura"),
                score = 9.2,
                studio = "MAPPA",
                format = "TV"
            )
        )

        // ==========================================
        // Day 5: Day + 5
        // ==========================================
        val day5 = today.plusDays(5)
        list.add(
            AnimeRelease(
                id = 163135,
                airingId = 1015,
                titleRomaji = "Kusuriya no Hitorigoto 2nd Season",
                titleEnglish = "The Apothecary Diaries Season 2",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx163135-apoth.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/163135-apothb.jpg",
                synopsis = "Maomao investiga nuevos misterios y venenos en la corte imperial con la ayuda de Jinshi.",
                synopsisEs = "Maomao investiga nuevos misterios y venenos en la corte imperial con la ayuda de Jinshi.",
                synopsisEn = "Apothecary prodigy Maomao returns to solve intricate poisonings and palace intrigue with Jinshi.",
                episode = 5,
                totalEpisodes = 24,
                airingAtSeconds = epochFor(day5, 17, 30),
                genres = listOf("Misterio", "Drama", "Histórico"),
                score = 8.8,
                studio = "TOHO animation / OLM",
                format = "TV"
            )
        )
        list.add(
            AnimeRelease(
                id = 171020,
                airingId = 1016,
                titleRomaji = "Oshi no Ko 3rd Season",
                titleEnglish = "[Oshi No Ko] Season 3",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx171020-onk.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/171020-onkb.jpg",
                synopsis = "Aqua avanza en su venganza en el mundo del espectáculo mientras Ruby se consolida como ídolo.",
                synopsisEs = "Aqua avanza en su venganza en el mundo del espectáculo mientras Ruby se consolida como ídolo.",
                synopsisEn = "Aqua delves deeper into the entertainment industry's shadows as Ruby aims for super-stardom.",
                episode = 3,
                totalEpisodes = 11,
                airingAtSeconds = epochFor(day5, 19, 0),
                genres = listOf("Drama", "Misterio", "Psicológico"),
                score = 8.7,
                studio = "Doga Kobo",
                format = "TV"
            )
        )

        // ==========================================
        // Day 6: Day + 6
        // ==========================================
        val day6 = today.plusDays(6)
        list.add(
            AnimeRelease(
                id = 176499,
                airingId = 1017,
                titleRomaji = "Boku no Hero Academia: Final Season",
                titleEnglish = "My Hero Academia: The Final Battle",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx176499-mha.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/176499-mhab.jpg",
                synopsis = "Deku enfrenta a Shigaraki y All For One en el clímax que decidirá la sociedad de héroes.",
                synopsisEs = "Deku enfrenta a Shigaraki y All For One en el clímax que decidirá la sociedad de héroes.",
                synopsisEn = "Izuku Midoriya clashes against Tomura Shigaraki and All For One in the final stand of hero society.",
                episode = 12,
                totalEpisodes = 21,
                airingAtSeconds = epochFor(day6, 17, 0),
                genres = listOf("Acción", "Superpoderes", "Shounen"),
                score = 8.4,
                studio = "BONES",
                format = "TV"
            )
        )
        list.add(
            AnimeRelease(
                id = 176500,
                airingId = 1018,
                titleRomaji = "Mushoku Tensei: Isekai Ittara Honki Dasu 3rd Season",
                titleEnglish = "Mushoku Tensei: Jobless Reincarnation Season 3",
                coverImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx176500-mt.jpg",
                bannerImageUrl = "https://s4.anilist.co/file/anilistcdn/media/anime/banner/176500-mtb.jpg",
                synopsis = "Rudeus Greyrat asume los mayores desafíos como mago de élite y cabeza de familia al adentrarse en los secretos del Dios Hombre.",
                synopsisEs = "Rudeus Greyrat asume los mayores desafíos como mago de élite y cabeza de familia al adentrarse en los secretos del Dios Hombre.",
                synopsisEn = "Rudeus Greyrat balances marriage, fatherhood, and escalating battles against the Man-God's manipulations.",
                episode = 4,
                totalEpisodes = 24,
                airingAtSeconds = epochFor(day6, 20, 0),
                genres = listOf("Fantasía", "Aventura", "Drama", "Isekai"),
                score = 8.9,
                studio = "Studio Bind",
                format = "TV"
            )
        )

        return list
    }
}
