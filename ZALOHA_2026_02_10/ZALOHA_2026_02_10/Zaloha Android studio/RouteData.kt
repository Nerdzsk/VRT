package com.example.vrturist

// ==========================================
// DÁTOVÉ TRIEDY (Modely)
// ==========================================

data class PointOfInterest(
    val requiredSteps: Int, // Kedy sa bod odomkne (počet krokov)
    val title: String,
    val text: String,
    // Automatický výpočet XP (ak je bod > 20 krokov, dáva odmenu)
    val xpReward: Int = LevelSystem.calculatePoiXpReward(requiredSteps)
)

data class Achievement(
    val title: String,
    val description: String,
    val threshold: Int
)

// 🔥 PRIDANÉ: totalPositions (Celkový počet bodov na mape - pre zobrazenie "Pozícia X / Y")
data class RouteMetaInfo(val totalSteps: Int, val totalDistKm: Float, val totalPositions: Int)

// ==========================================
// KONFIGURÁCIA A TEXTY
// ==========================================

val ROUTE_NAMES = mapOf(
    "bulikova_lada116" to "Bulíkova -> Lada",
    "bulikova_eurovea" to "Bulíkova -> Eurovea"
)

val ROUTE_DESCRIPTIONS = mapOf(
    "bulikova_lada116" to "Krátka a príjemná prechádzka cez srdce sídliska až k legendárnej budove Lady.",
    "bulikova_eurovea" to "Dlhšia trasa smerom k Dunaju. Prejdi sa cez Starý most a uži si atmosféru nábrežia."
)

// 🔥 ÚDAJE O DĹŽKE TRÁS (Synchronizované s webom)
// Predpoklad: 1 bod na mape = 10 krokov
val ROUTE_METADATA = mapOf(
    "bulikova_lada116" to RouteMetaInfo(3800, 2.71f, 380), // 3800 krokov / 10 = 380 pozícií
    "bulikova_eurovea" to RouteMetaInfo(5200, 3.65f, 520)  // 5200 krokov / 10 = 520 pozícií
)

// 3. Body záujmu (Smart Turista)
val ROUTE_POIS = mapOf(
    "bulikova_lada116" to listOf(
        PointOfInterest(23, "Štartujeme!", "Vitaj na trase. Začíname na Bulíkovej ulici. Priprav sa na virtuálnu prechádzku."),
        PointOfInterest(460, "Miestna zeleň", "Všimni si stromy okolo cesty. Petržalka je zelenšia, než sa zdá."),
        PointOfInterest(1150, "Polovica cesty", "Máš za sebou polovicu trasy k Lade. Len tak ďalej!"),
        PointOfInterest(1840, "Blížime sa", "Cieľ je už na dohľad. Ešte pár krokov."),
        PointOfInterest(2530, "Cieľ: Lada", "Gratulujem! Dorazil si do cieľa. Lada 116 ťa víta.")
    ),
    "bulikova_eurovea" to listOf(
        PointOfInterest(23, "Štart: Bulíkova", "Vitaj pri Saleziánoch. Začíname v srdci Ovsišťa."),
        PointOfInterest(460, "Pankúchova a lesík", "Kráčaš okolo Pankúchovej ulice. Všimni si, koľko zelene sa zachovalo medzi panelákmi."),
        PointOfInterest(1150, "Jantárová cesta", "Vstupuješ na Jantárovú cestu. Jej názov odkazuje na starovekú obchodnú trasu."),
        PointOfInterest(1840, "Chorvátske rameno", "Podchádzaš alebo míňaš Chorvátske rameno. Je to pozostatok starého ramena Dunaja."),
        PointOfInterest(2500, "Nástup na Starý most", "Prichádzaš k Starému mostu. Je to ikonická stavba, ktorá spája Petržalku s centrom."),
        PointOfInterest(3000, "Výhľad na Hrad", "Zastav sa a pozri vľavo – najkrajšia panoráma Hradu."),
        PointOfInterest(3500, "Univerzita", "Šafárikovo námestie a budova Univerzity Komenského."),
        PointOfInterest(4000, "Cieľ: Eurovea", "Sme v cieli! Uži si nábrežie Dunaja.")
    )
)

// 4. Achievementy (Ciele)
val STEP_MILESTONES = listOf(
    Achievement("Prvý krok", "Tvoj úplne prvý záznam.", 100),
    Achievement("Rozcvička", "Prvých 1000 krokov.", 1000),
    Achievement("Turista", "Poriadna prechádzka.", 5000),
    Achievement("Denný cieľ", "10 000 krokov.", 10000),
    Achievement("Víkendový bojovník", "Dva dni aktívneho chodenia.", 25000),
    Achievement("Maratónec", "50 000 krokov.", 50000),
    Achievement("Stovka", "100 000 krokov.", 100000),
    Achievement("Nezastaviteľný", "250 000 krokov.", 250000),
    Achievement("Pol milióna", "500 000 krokov.", 500000),
    Achievement("Milionár", "1 000 000 krokov.", 1000000),
    Achievement("Cez krajinu", "2.5 mil. krokov.", 2500000),
    Achievement("Nomád", "5 mil. krokov.", 5000000),
    Achievement("Svetobežník", "10 mil. krokov.", 10000000),
    Achievement("Forrest Gump", "25 mil. krokov.", 25000000),
    Achievement("Okolo sveta", "50 mil. krokov.", 50000000)
)

val DAILY_MILESTONES = listOf(
    Achievement("Dnešný Štandard", "10k za deň.", 10000),
    Achievement("Dvojitá Dávka", "20k za deň.", 20000),
    Achievement("Výletník", "30k za deň.", 30000),
    Achievement("Mestský Maratón", "40k za deň.", 40000),
    Achievement("Polovica Stovky", "50k za deň.", 50000),
    Achievement("Lovec Krokov", "60k za deň.", 60000),
    Achievement("Od Úsvitu", "70k za deň.", 70000),
    Achievement("Železný Muž", "80k za deň.", 80000),
    Achievement("Takmer Tam", "90k za deň.", 90000),
    Achievement("LEGENDÁRNY DEŇ", "100 000 krokov.", 100000)
)

val CALORIE_MILESTONES = listOf(
    Achievement("Prvé kalórie", "1000 kcal.", 1000),
    Achievement("Hamburger", "2500 kcal.", 2500),
    Achievement("Pizza Večer", "5000 kcal.", 5000),
    Achievement("Jeden Kilogram", "10 000 kcal.", 10000),
    Achievement("Nedeľný Obed", "25 000 kcal.", 25000),
    Achievement("Fitness Začiatočník", "50 000 kcal.", 50000),
    Achievement("Spaľovač", "100 000 kcal.", 100000),
    Achievement("Elektráreň", "250 000 kcal.", 250000),
    Achievement("Jadrový Reaktor", "500 000 kcal.", 500000),
    Achievement("Megawatthodina", "1 milión kcal.", 1000000),
    Achievement("Supernova", "2 milióny kcal.", 2000000),
    Achievement("Hypernova", "3 milióny kcal.", 3000000),
    Achievement("Kvasar", "5 miliónov kcal.", 5000000),
    Achievement("Veľký Tresk", "7.5 milióna kcal.", 7500000),
    Achievement("Pán Vesmíru", "10 miliónov kcal.", 10000000)
)