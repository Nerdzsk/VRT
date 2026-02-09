package com.example.vrturist

import kotlin.math.floor
import kotlin.math.sqrt

// Dátová trieda pre konkrétny Level
data class LevelDefinition(
    val level: Int,
    val title: String,
    val description: String,
    val iconName: String
    // requiredXp sa počíta dynamicky
)

object LevelSystem {

    // 🔥 250 pre jemný štart (rovnako ako na webe)
    private const val XP_CONSTANT = 250

    // 1. VÝPOČET AKTUÁLNEHO LEVELU
    fun getLevelFromXp(totalXp: Int): Int {
        if (totalXp <= 0) return 1
        // Inverzná funkcia k vzorcu C * L * (L-1)
        val level = floor((1 + sqrt(1 + 4.0 * (totalXp.toDouble() / XP_CONSTANT))) / 2.0).toInt()
        return if (level < 1) 1 else level
    }

    // 2. VÝPOČET XP PRE KONKRÉTNY LEVEL
    fun getXpRequiredForLevel(level: Int): Int {
        if (level <= 1) return 0
        return XP_CONSTANT * level * (level - 1)
    }

    // 3. PROGRES (0.0 až 1.0)
    fun getProgressToNextLevel(totalXp: Int): Float {
        val currentLevel = getLevelFromXp(totalXp)
        val currentLevelXp = getXpRequiredForLevel(currentLevel)
        val nextLevelXp = getXpRequiredForLevel(currentLevel + 1)

        if (nextLevelXp <= currentLevelXp) return 1.0f

        val xpInLevel = totalXp - currentLevelXp
        val xpNeeded = nextLevelXp - currentLevelXp

        return (xpInLevel.toFloat() / xpNeeded.toFloat()).coerceIn(0.0f, 1.0f)
    }

    // 4. ZÍSKANIE DEFINÍCIE LEVELU
    fun getLevelInfo(level: Int): LevelDefinition {
        val uniqueLevel = UNIQUE_LEVELS.find { it.level == level }
        if (uniqueLevel != null) return uniqueLevel

        return LevelDefinition(
            level,
            "VR Legenda $level",
            "Si na takej úrovni, že pre teba musíme vymyslieť nové meno!",
            "TROPHY"
        )
    }

    fun getRankTitle(level: Int): String {
        return getLevelInfo(level).title
    }

    // 🔥 5. VÝPOČET ODMENY ZA POI (NOVÁ LOGIKA PODĽA VZDIALENOSTI)
    fun calculatePoiXpReward(stepsFromStart: Int): Int {
        return when {
            stepsFromStart < 10 -> 0        // Ochrana proti úplnému začiatku
            stepsFromStart <= 100 -> 10     // 10 - 100 krokov
            stepsFromStart <= 500 -> 20     // 101 - 500 krokov
            stepsFromStart <= 1000 -> 30    // 501 - 1000 krokov
            stepsFromStart <= 5000 -> 50    // 1001 - 5000 krokov
            stepsFromStart <= 10000 -> 75   // 5001 - 10000 krokov
            stepsFromStart <= 20000 -> 100  // 10001 - 20000 krokov
            stepsFromStart <= 50000 -> 500  // 20001 - 50000 krokov
            stepsFromStart <= 100000 -> 750 // 50001 - 100000 krokov
            stepsFromStart <= 500000 -> 1000 // 100001 - 500000 krokov
            else -> 15000                   // Nad 500 000 (Kráľovská odmena)
        }
    }

    // ==========================================
    // ZOZNAM UNIKÁTNYCH LEVELOV (1 - 30)
    // ==========================================
    val UNIQUE_LEVELS = listOf(
        LevelDefinition(1, "Gaučový povaľač", "Tvoja cesta sa ešte len začína.", "SOFA"),
        LevelDefinition(2, "Rozhýbaný", "Prvých 500 XP je doma.", "WALK"),
        LevelDefinition(3, "Nedeľný chodec", "Začínaš si zvykať na pohyb.", "WALK"),
        LevelDefinition(4, "Sídliskový prieskumník", "Poznáš každú lavičku.", "MAP"),
        LevelDefinition(5, "Objaviteľ skratiek", "Vieš, kade prejsť rýchlejšie.", "MAP"),
        LevelDefinition(6, "Parkový bežec", "Tvoje tempo je svižné.", "HIKE"),
        LevelDefinition(7, "Lovec krokov", "10 000 krokov? Ľavou zadnou.", "COMPASS"),
        LevelDefinition(8, "Mestský tulák", "Mesto je tvoje ihrisko.", "HIKE"),
        LevelDefinition(9, "Petržalský šampión", "Betónová džungľa ťa nezastaví.", "STAR"),
        LevelDefinition(10, "TURISTA", "Prvý veľký míľnik!", "TROPHY"),
        LevelDefinition(11, "Hľadač výhľadov", "Hľadáš najlepšiu panorámu.", "COMPASS"),
        LevelDefinition(12, "Nezastaviteľný", "Dážď ťa neodradí.", "HIKE"),
        LevelDefinition(13, "Maratónec (takmer)", "Tvoje nohy sú z ocele.", "WALK"),
        LevelDefinition(14, "Zberateľ zážitkov", "Každý krok je nový príbeh.", "STAR"),
        LevelDefinition(15, "Majster orientácie", "Nepotrebuješ GPS.", "MAP"),
        LevelDefinition(16, "Kráľ chodníkov", "Chodci ti uhýbajú.", "TROPHY"),
        LevelDefinition(17, "Vytrvalec", "Iní končia, ty začínaš.", "HIKE"),
        LevelDefinition(18, "Dobyvateľ", "Každá ulica je výzva.", "COMPASS"),
        LevelDefinition(19, "Legenda ulice", "Ľudia ťa začínajú spoznávať.", "STAR"),
        LevelDefinition(20, "SVETOBEŽNÍK", "Obrovský úspech!", "TROPHY"),
        LevelDefinition(21, "Expedičný vodca", "Sprievodca po meste.", "MAP"),
        LevelDefinition(22, "Nezmar", "Únava neexistuje.", "HIKE"),
        LevelDefinition(23, "Pútnik", "Cesta je cieľ.", "WALK"),
        LevelDefinition(24, "Grandmaster krokov", "Neuveriteľné štatistiky.", "STAR"),
        LevelDefinition(25, "VR LEGENDA", "Vrchol prvej etapy.", "TROPHY"),
        // 🔥 NOVÉ LEVELY 26-30
        LevelDefinition(26, "Kartograf", "Každý kút mapy je tvoj.", "MAP"),
        LevelDefinition(27, "Expedičný veterán", "Prešiel si už všetko a chceš viac.", "HIKE"),
        LevelDefinition(28, "Míľový boh", "Pohyb je tvoj druhý domov.", "WALK"),
        LevelDefinition(29, "Nesmrteľný tulák", "Tvoje štatistiky sú vytesané do kameňa.", "STAR"),
        LevelDefinition(30, "OLYMP LEGENDA", "Absolútny vrchol!", "TROPHY")
    )
}