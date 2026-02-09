// ==========================================
// 1. KONFIGURÁCIA FIREBASE (ZACHOVANÉ)
// ==========================================
const firebaseConfig = {
    apiKey: "AIzaSyB1dSuQkFo4X0Jhvz2wZduc_D_BAi32obc",
    authDomain: "vrtourist-accd3.firebaseapp.com",
    databaseURL: "https://vrtourist-accd3-default-rtdb.firebaseio.com",
    projectId: "vrtourist-accd3",
    storageBucket: "vrtourist-accd3.firebasestorage.app",
    messagingSenderId: "999831032070",
    appId: "1:999831032070:web:88497e4e06eba700224e87"
};

if (!firebase.apps.length) {
    firebase.initializeApp(firebaseConfig);
}
const database = firebase.database();

// ==========================================
// 2. LEVEL SYSTÉM (ZACHOVANÉ)
// ==========================================
const XP_CONSTANT = 250; 

const UNIQUE_LEVELS = [
    { level: 1, title: "Gaučový povaľač", description: "Tvoja cesta sa ešte len začína.", icon: "weekend" },
    { level: 2, title: "Rozhýbaný", description: "Prvých 500 XP je doma.", icon: "directions_run" },
    { level: 3, title: "Nedeľný chodec", description: "Začínaš si zvykať na pohyb.", icon: "directions_walk" },
    { level: 4, title: "Sídliskový prieskumník", description: "Poznáš každú lavičku.", icon: "place" },
    { level: 5, title: "Objaviteľ skratiek", description: "Vieš, kade prejsť rýchlejšie.", icon: "map" },
    { level: 6, title: "Parkový bežec", description: "Tvoje tempo je svižné.", icon: "hiking" },
    { level: 7, title: "Lovec krokov", description: "10 000 krokov? Ľavou zadnou.", icon: "explore" },
    { level: 8, title: "Mestský tulák", description: "Mesto je tvoje ihrisko.", icon: "hiking" },
    { level: 9, title: "Petržalský šampión", description: "Betónová džungľa ťa nezastaví.", icon: "star" },
    { level: 10, title: "TURISTA", description: "Prvý veľký míľnik!", icon: "emoji_events" },
    { level: 11, title: "Hľadač výhľadov", description: "Hľadáš najlepšiu panorámu.", icon: "explore" },
    { level: 12, title: "Nezastaviteľný", description: "Dážď ťa neodradí.", icon: "hiking" },
    { level: 13, title: "Maratónec (takmer)", description: "Tvoje nohy sô z ocele.", icon: "directions_walk" },
    { level: 14, title: "Zberateľ zážitkov", description: "Každý krok je nový príbeh.", icon: "star" },
    { level: 15, title: "Majster orientácie", description: "Nepotrebuješ GPS.", icon: "map" },
    { level: 16, title: "Kráľ chodníkov", description: "Chodci ti uhýbajú.", icon: "emoji_events" },
    { level: 17, title: "Vytrvalec", description: "Iní končia, ty začínaš.", icon: "hiking" },
    { level: 18, title: "Dobyvateľ", description: "Každá ulica je výzva.", icon: "explore" },
    { level: 19, title: "Legenda ulice", description: "Ľudia ťa začínajú spoznávať.", icon: "star" },
    { level: 20, title: "SVETOBEŽNÍK", description: "Obrovský úspech!", icon: "emoji_events" },
    { level: 21, title: "Expedičný vodca", description: "Sprievodca po meste.", icon: "map" },
    { level: 22, title: "Nezmar", description: "Únava neexistuje.", icon: "hiking" },
    { level: 23, title: "Pútnik", description: "Cesta je cieľ.", icon: "directions_walk" },
    { level: 24, title: "Grandmaster krokov", description: "Neuveriteľné štatistiky.", icon: "star" },
    { level: 25, title: "VR LEGENDA", description: "Vrchol prvej etapy.", icon: "emoji_events" },
    { level: 26, title: "Kartograf", description: "Každý kút mapy je tvoj.", icon: "map" },
    { level: 27, title: "Expedičný veterán", description: "Prešiel si už všetko.", icon: "hiking" },
    { level: 28, title: "Míľový boh", description: "Pohyb je tvoj domov.", icon: "directions_walk" },
    { level: 29, title: "Nesmrteľný tulák", description: "Štatistiky vytesané do kameňa.", icon: "star" },
    { level: 30, title: "OLYMP LEGENDA", description: "Absolútny vrchol!", icon: "workspace_premium" }
];

function getLevelFromXp(totalXp) {
    if (totalXp <= 0) return 1;
    let level = Math.floor( (1 + Math.sqrt(1 + 4 * (totalXp / XP_CONSTANT))) / 2 );
    return level < 1 ? 1 : level;
}

function getXpRequiredForLevel(level) {
    if (level === 1) return 0;
    return XP_CONSTANT * level * (level - 1);
}

function getRankTitle(level) {
    const levelData = UNIQUE_LEVELS.find(l => l.level === level);
    return levelData ? levelData.title : `VR Legenda ${level}`;
}

function calculatePoiXpReward(stepsFromStart) {
    if (stepsFromStart < 10) return 0; 
    if (stepsFromStart <= 100) return 10;
    if (stepsFromStart <= 500) return 20;
    if (stepsFromStart <= 1000) return 30;
    if (stepsFromStart <= 5000) return 50;
    if (stepsFromStart <= 10000) return 75;
    if (stepsFromStart <= 20000) return 100;
    if (stepsFromStart <= 50000) return 500;
    if (stepsFromStart <= 100000) return 750;
    if (stepsFromStart <= 500000) return 1000;
    return 15000;
}

// ==========================================
// 3. IKONY A MÍĽNIKY (ZACHOVANÉ)
// ==========================================
const STEP_MILESTONES = [
    { t: 100, title: "Prvý krok" }, { t: 1000, title: "Rozcvička" }, { t: 5000, title: "Turista" },
    { t: 10000, title: "Denný cieľ" }, { t: 25000, title: "Víkendový bojovník" }, { t: 50000, title: "Maratónec" },
    { t: 100000, title: "Stovka" }, { t: 250000, title: "Nezastaviteľný" }, { t: 500000, title: "Pol milióna" },
    { t: 1000000, title: "Milionár" }
];

const CALORIE_MILESTONES = [
    { t: 1000, title: "Prvé kalórie" }, { t: 2500, title: "Hamburger" }, { t: 5000, title: "Pizza Večer" },
    { t: 10000, title: "Jeden Kilogram" }, { t: 25000, title: "Nedeľný Obed" }, { t: 50000, title: "Fitness Začiatoči" }
];

const DAILY_MILESTONES = [
    { t: 10000, title: "Dnešný Štandard" }, { t: 20000, title: "Dvojitá Dávka" }, { t: 30000, title: "Výletník" }
];

const POI_ICONS = {
    "START": "play_circle_filled",
    "STREET": "edit_road",
    "LANDMARK": "castle",
    "SETTLEMENT": "location_city",
    "WATER": "water_drop",
    "PROGRESS": "stars",
    "FINISH": "emoji_events"
};

// ==========================================
// 4. GLOBÁLNE PREMENNÉ
// ==========================================
let CURRENT_ROUTE_ID = ''; 
let routePoints = [];
let currentRoutePois = []; 
let poiMarkers = []; 
let stepsPerMove = 10;
let lastRenderedIndex = -1;
let userWeight = 70; 
let userTotalXp = 0; 
let totalGlobalSteps = 0;
let maxDailySteps = 0;
let currentRouteSteps = 0; 
let maxTriggeredSteps = 0;  

let isVRActive = false; // ������ DÔLEŽITÉ: Musí tu byť pre VR

let lastSeenTriggeredStep = -1;
let isFirstLoadForToasts = true;

const ROUTE_NAMES = {
    "bulikova_lada116": "Bulíkova 15 ➝ Lada 116",
    "bulikova_eurovea": "Bulíkova 15 ➝ Eurovea", // Pridaná čiarka
    "grand_canyon": "Grand Canyon - Bright Angel Trail"
};

// NOVÉ: Dve čiary pre mapu (nahradenie mapPolyline)
let map, panorama, mapMarker, traveledPolyline, remainingPolyline;

const statsEl = {
    steps: document.getElementById('steps'),
    dist: document.getElementById('traveledDistance'),
    totalDist: document.getElementById('totalRouteDistance'), 
    pos: document.getElementById('position'),
    routeName: document.getElementById('routeName'),
    calories: document.getElementById('calories'),
    lvlNum: document.getElementById('userLevel'),
    lvlRank: document.getElementById('userRank'),
    lvlBar: document.getElementById('levelProgressBar'),
    lvlText: document.getElementById('levelXpText'),
    goalName: document.getElementById('nextGoalName'),
    goalBar: document.getElementById('goalProgressBar'),
    goalCurr: document.getElementById('goalCurrent'),
    goalTarget: document.getElementById('goalTarget'),
    goalRem: document.getElementById('goalRemaining'),
    guideList: document.getElementById('guideList'),
    btnGuide: document.getElementById('toggleGuide'),
    panelGuide: document.getElementById('guidePanel'),
    btnLevels: document.getElementById('btnShowLevels'),
    modalLevels: document.getElementById('levelsModal'),
    btnCloseLevels: document.getElementById('closeLevels'),
    levelsContainer: document.getElementById('levelsListContainer'),
    achievementBox: document.querySelector('.achievement-box'),
    status: document.getElementById('status')
    
};

const divPanorama = document.getElementById('panorama');
const divMap = document.getElementById('map');

// ==========================================
// 5. VR LOGIKA A DIAGNOSTIKA (ŠPECIÁLNE PRE QUEST 3)
// ==========================================

const btnVR = document.getElementById('startVR');
const btnExitVR = document.getElementById('exitVR');

// Pomocná funkcia na výpis diagnostiky priamo na displej headsetu
function debugLog(msg) {
    if (statsEl.status) {
        statsEl.status.innerText = msg;
        console.log("VR_DEBUG: " + msg);
    }
}

function handleOrientation(event) {
    // Na Meta Quest 3 vypíname manuálny setPov, aby sme uvoľnili cestu natívnemu trackingu.
    // Ak senzory fungujú, uvidíte čísla v konzole.
    if (isVRActive && event.alpha !== null) {
        console.log("Senzory aktívne:", Math.round(event.alpha));
    }
}

window.addEventListener('deviceorientation', handleOrientation, true);

if (btnVR) {
    btnVR.addEventListener('click', async () => {
        const panoElement = document.getElementById('panorama');

        // Povolenie senzorov (vyžadované pre VR v prehliadačoch)
        if (typeof DeviceOrientationEvent !== 'undefined' && typeof DeviceOrientationEvent.requestPermission === 'function') {
            try { 
                const permission = await DeviceOrientationEvent.requestPermission();
                if (permission !== 'granted') {
                    debugLog("Prístup k senzorom zamietnutý.");
                }
            } catch (e) { console.error(e); }
        }

        // Vstup do Fullscreenu (Quest potrebuje celú obrazovku pre A-Frame/WebXR)
        if (panoElement.requestFullscreen) { await panoElement.requestFullscreen(); }
        else if (panoElement.webkitRequestFullscreen) { await panoElement.webkitRequestFullscreen(); }

        document.getElementById('statsPanel').classList.add('hidden');
        document.getElementById('viewControls').style.display = 'none';
        if(btnExitVR) btnExitVR.classList.remove('hidden');
        
        isVRActive = true;
        updateLevelUI();

        // ������ SYNCHRONIZÁCIA: Hneď pri vstupe do VR vynútime "pohľad vpred" (0,0,0)
// Keďže skybox je už na webe zrovnaný s cestou, toto ťa natočí priamo na ňu.
        const cameraEl = document.getElementById('vrCamera');
        if (cameraEl && cameraEl.components['look-controls']) {
    // Resetujeme interné objekty look-controls (yaw a pitch)
        cameraEl.components['look-controls'].yawObject.rotation.y = 0;
        cameraEl.components['look-controls'].pitchObject.rotation.x = 0;
    
    // Pre istotu resetujeme aj samotný atribút entity
        cameraEl.setAttribute('rotation', '0 0 0');
        }
        // ������ AKTUALIZÁCIA HUD PANELA
        const vrHud = document.getElementById('vrHud');
        const vrContent = document.getElementById('vrHudContent');
        const vrIcon = document.getElementById('vrToggleIcon');

        if (vrHud && vrContent) {
            vrHud.classList.remove('hidden');
            vrContent.style.display = "block";
            if (vrIcon) vrIcon.innerText = "visibility";
            
            vrHud.style.background = "rgba(0,0,0,0.85)";
            vrHud.style.border = "2px solid rgba(255, 255, 255, 0.2)";
            setupVrToggle();
        }

        // ������ AKTIVÁCIA A-FRAME REŽIMU A POSILNENÁ POISTKA SENZOROV
        const scene = document.querySelector('a-scene');
        if (scene) {
            // POISTKA: Reset a vynútené prepojenie kamery so senzormi headsetu
            const cameraEntity = scene.querySelector('[camera]');
            if (cameraEntity) {
                // Odstránime a znova pridáme controls pre totálny reset senzorov
                cameraEntity.removeAttribute('look-controls');
                cameraEntity.setAttribute('look-controls', {
                    enabled: true,
                    magicWindowTrackingEnabled: true,
                    touchEnabled: true,
                    mouseEnabled: true
                });
            }

            // Vynútime vstup do VR módu v Questa
            if (scene.hasLoaded) {
                scene.enterVR();
            } else {
                scene.addEventListener('loaded', () => scene.enterVR());
            }
        }

        // ������ VYNÚTENÁ AKTUALIZÁCIA DÁT HNEĎ PRI ŠTARTE
        const vrSteps = document.getElementById('vrStepsDisplay');
        const vrCals = document.getElementById('vrCaloriesDisplay');
        const vrDist = document.getElementById('vrDistanceDisplay');
        
        if(vrSteps) vrSteps.innerText = formatNum(currentRouteSteps);
        if(vrCals) vrCals.innerText = Math.floor(currentRouteSteps * 0.04 * (userWeight/70));
        if(vrDist) vrDist.innerText = (currentRouteSteps * 0.00075).toFixed(2) + " km";

        // Oprava: Volanie aktualizácie cieľa hneď po vstupe do VR
        updateVrNextPoi(currentRouteSteps, panoElement);

        // Pôvodná Google panoráma - teraz ju skryjeme, aby nezavadzala A-Frame scéne
        // Pôvodná Google panoráma - skryjeme ju
        if (panorama) {
            panorama.setOptions({ 
                visible: false, 
                motionTracking: false 
            });

            // FIX: Získame smer z 2D webu pre počiatočnú synchronizáciu
            const currentWebHeading = panorama.getPov().heading;
            const sky = document.getElementById('skybox');
            
            // Získame orientáciu fotky bez pádovej funkcie getPanoData
            const sv = new google.maps.StreetViewService();
            sv.getPanorama({ location: panorama.getPosition(), radius: 50 }, (data, status) => {
                if (status === "OK" && sky) {
                    const panoCenterHeading = data.tiles.centerHeading || 0;
                    const syncRotation = panoCenterHeading - currentWebHeading - 90;
                    sky.setAttribute('rotation', `0 ${syncRotation} 0`);
                }
            });

            setTimeout(() => {
                google.maps.event.trigger(panorama, 'resize');
                if(statsEl.status) statsEl.status.innerText = "VR režim aktívny.";
            }, 1000);
        }
        startVrArrowLoop();
    });
}

if (btnExitVR) {
    btnExitVR.addEventListener('click', () => {
        if (document.exitFullscreen) { document.exitFullscreen(); }
        else if (document.webkitExitFullscreen) { document.webkitExitFullscreen(); }
    });
}

// PRIDANÉ LISTENERY PRE TLAČIDLÁ (ZACHOVANÉ)
const btnStats = document.getElementById('toggleStats');
const panelStats = document.getElementById('statsPanel');
const tGuideBtn = document.getElementById('toggleGuide');
const tGuidePanel = document.getElementById('guidePanel');

if(btnStats) btnStats.addEventListener('click', () => { panelStats.classList.toggle('visible'); panelStats.classList.toggle('hidden'); btnStats.classList.toggle('active'); });
if(tGuideBtn) tGuideBtn.addEventListener('click', () => { tGuidePanel.classList.toggle('visible'); tGuidePanel.classList.toggle('hidden'); tGuideBtn.classList.toggle('active'); });

if (statsEl.btnLevels) { statsEl.btnLevels.addEventListener('click', () => { renderLevelsList(); statsEl.modalLevels.classList.remove('hidden'); statsEl.modalLevels.style.display = 'flex'; }); }
if (statsEl.btnCloseLevels) { statsEl.btnCloseLevels.addEventListener('click', () => { statsEl.modalLevels.classList.add('hidden'); statsEl.modalLevels.style.display = 'none'; }); }

if (!document.getElementById('goalsModal')) {
    const goalsModalHTML = `<div id="goalsModal" class="modal hidden" style="display: none;"><div class="modal-content"><div class="modal-header"><span class="modal-title">Ciele</span><span id="closeGoals" class="close-btn">×</span></div><div id="goalsListContainer"></div></div></div>`;
    document.body.insertAdjacentHTML('beforeend', goalsModalHTML);
}
const modalGoals = document.getElementById('goalsModal');
const btnCloseGoals = document.getElementById('closeGoals');
if (btnCloseGoals) { btnCloseGoals.addEventListener('click', () => { modalGoals.style.display = 'none'; }); }

if (statsEl.achievementBox && !document.getElementById('btnShowGoals')) {
    const btnHtml = `<button id="btnShowGoals" class="list-btn">Zobraziť ciele</button>`;
    statsEl.achievementBox.insertAdjacentHTML('beforeend', btnHtml);
    document.getElementById('btnShowGoals').addEventListener('click', () => { renderGoalsList(); modalGoals.classList.remove('hidden'); modalGoals.style.display = 'flex'; });
}

document.addEventListener('fullscreenchange', () => {
    if (!document.fullscreenElement) {
        isVRActive = false;
        const vrHud = document.getElementById('vrHud');
        if (vrHud) vrHud.classList.add('hidden');
        document.getElementById('viewControls').style.display = 'flex';
        if(btnExitVR) btnExitVR.classList.add('hidden');
        if (panorama) {
            panorama.setZoom(1); 
            panorama.setOptions({ motionTracking: false });
        }
        updateLayout();
    }
});

function updateLayout() {
    if (!divPanorama || !divMap) return;
    const isPanoHidden = divPanorama.classList.contains('hidden');
    const isMapHidden = divMap.classList.contains('hidden');
    divPanorama.style.width = ""; divMap.style.width = "";
    if (!isPanoHidden && !isMapHidden) { divPanorama.style.width = "70%"; divMap.style.width = "30%"; divPanorama.style.display = "block"; divMap.style.display = "block"; } 
    else if (!isPanoHidden && isMapHidden) { divPanorama.style.width = "100%"; divMap.style.display = "none"; } 
    else if (isPanoHidden && !isMapHidden) { divMap.style.width = "100%"; divPanorama.style.display = "none"; }
    if (window.google && google.maps) { if (map && !isMapHidden) { google.maps.event.trigger(map, "resize"); if (mapMarker) map.setCenter(mapMarker.getPosition()); } if (panorama && !isPanoHidden) google.maps.event.trigger(panorama, "resize"); }
}

const btnPanorama = document.getElementById('togglePanorama');
const btnMap = document.getElementById('toggleMap');
if(btnPanorama && btnMap) { 
    btnPanorama.addEventListener('click', () => { if (divMap.classList.contains('hidden')) return; divPanorama.classList.toggle('hidden'); updateLayout(); }); 
    btnMap.addEventListener('click', () => { if (divPanorama.classList.contains('hidden')) return; divMap.classList.toggle('hidden'); updateLayout(); }); 
}

// ==========================================
// 6. LOGIKA RENDEROVANIA & TOASTS (ZACHOVANÉ)
// ==========================================

function renderLevelsList() {
    statsEl.levelsContainer.innerHTML = "";
    const currentLevel = getLevelFromXp(userTotalXp);
    UNIQUE_LEVELS.forEach(lvl => {
        const isUnlocked = currentLevel >= lvl.level;
        const isCurrent = currentLevel === lvl.level;
        const xpReq = getXpRequiredForLevel(lvl.level);
        const card = document.createElement('div');
        card.className = `level-card ${isUnlocked ? 'unlocked' : 'locked'} ${isCurrent ? 'current' : ''}`;
        card.innerHTML = `<div class="lvl-number"><i class="material-icons">${lvl.icon}</i></div><div class="lvl-info"><div class="lvl-title">${lvl.title} ${isCurrent ? '<span class="current-badge">TERAZ</span>':''}</div><div class="lvl-desc">${lvl.description}</div></div><div class="lvl-req"><div>min.</div><div style="font-weight:900; color:#333;">${formatNum(xpReq)}</div><div>XP</div></div>`;
        statsEl.levelsContainer.appendChild(card);
    });
}

function renderGoalsList() {
    const goalsContainer = document.getElementById('goalsListContainer');
    if (!goalsContainer) return;
    goalsContainer.innerHTML = "";
    const totalCalories = Math.floor(totalGlobalSteps * 0.04 * (userWeight / 70));
    const createSection = (title, items, currentValue, unit, iconName) => {
        const header = document.createElement('div'); header.className = "achievement-section-title"; header.innerText = title; goalsContainer.appendChild(header);
        items.forEach(ach => {
            const isUnlocked = currentValue >= ach.t;
            const card = document.createElement('div'); card.className = `achievement-card ${isUnlocked ? 'unlocked' : 'locked'}`;
            card.innerHTML = `<div class="ach-icon"><i class="material-icons" style="color:${isUnlocked ? '#FFC107':'#ccc'}">${iconName}</i></div><div class="ach-info"><div class="ach-title">${ach.title}</div><div class="ach-value">${formatNum(ach.t)} ${unit}</div></div>`;
            goalsContainer.appendChild(card);
        });
    };
    createSection("Celkové kroky", STEP_MILESTONES, totalGlobalSteps, "kr.", "stars");
    createSection("Spálené kalórie", CALORIE_MILESTONES, totalCalories, "kcal", "local_fire_department");
    createSection("Denný rekord", DAILY_MILESTONES, maxDailySteps, "kr./deň", "event_available");
}

function loadRoute(id) {
    fetch(`route_${id}.json`).then(r=>r.json()).then(d => { 
        routePoints = d.points; 
        stepsPerMove = d.stepsPerMove || 10; 
        lastRenderedIndex = -1; 
        initMaps(id); 
        attachRouteListeners(id); 
    });
    database.ref(`poiData/${id}`).on('value', (snapshot) => { 
        currentRoutePois = []; 
        snapshot.forEach((child) => { currentRoutePois.push(child.val()); }); 
        currentRoutePois.sort((a, b) => a.requiredSteps - b.requiredSteps); 
        renderGuideList(); 
        updateGuideLocking(currentRouteSteps); 
        updatePoiMarkersOnMap(); 
        updateVrNextPoi(currentRouteSteps, document.getElementById('panorama'));
    });
}

function renderGuideList() {
    statsEl.guideList.innerHTML = "";
    if (!currentRoutePois || currentRoutePois.length === 0) { statsEl.guideList.innerHTML = "<div style='color:#777; font-style:italic; padding:10px;'>Hľadám sprievodcu vo Firebase...</div>"; return; }
    currentRoutePois.forEach(poi => {
        const item = document.createElement('div'); item.className = 'poi-item locked'; const targetSteps = poi.requiredSteps; item.dataset.steps = targetSteps; const iconName = POI_ICONS[poi.category] || "place"; const xpReward = poi.xpReward || calculatePoiXpReward(targetSteps);
        item.innerHTML = `<div style="min-width:45px; display:flex; align-items:center; justify-content:center;"><i class="material-icons" style="color:var(--primary)">${iconName}</i></div><div class="poi-content" style="flex:1"><div class="poi-title" style="font-weight:700;">${poi.title}</div><div class="poi-desc-preview" style="font-size:12px; color:#666; margin:4px 0;">${poi.text}</div><div class="poi-info" style="font-size:11px; font-weight:600; display:flex; gap:10px;"><span>Cieľ: ${formatNum(targetSteps)} kr.</span><span id="rem-${targetSteps}"></span><span style="color:#4CAF50;">+${xpReward} XP</span></div></div><button class="play-btn" style="border:none; background:var(--primary); color:white; width:35px; height:35px; border-radius:50%; cursor:pointer; display:flex; align-items:center; justify-content:center;"><i class="material-icons" style="font-size:18px;">volume_up</i></button>`;
        item.querySelector('.play-btn').addEventListener('click', (e) => { e.stopPropagation(); speakText(poi.text); });
        statsEl.guideList.appendChild(item);
    });
}

function showPoiToast(poi) {
    const container = document.getElementById('toast-container');
    if (!container) return;
    container.innerHTML = "";
    const toast = document.createElement('div'); 
    toast.className = 'toast'; 
    const xp = poi.xpReward || calculatePoiXpReward(poi.requiredSteps);
    const cleanText = (str) => str ? str.normalize("NFD").replace(/[\u0300-\u036f]/g, "") : "";
    toast.innerHTML = `<div style="display: flex; align-items: center; gap: 15px; width: 100%;"><i class="material-icons" style="font-size: 35px; color: #FFD600;">stars</i><div style="text-align: left; flex: 1;"><div style="font-size: 11px; opacity: 0.8; text-transform: uppercase; font-weight: bold;">Milnik dosiahnuty</div><div style="font-size: 17px; font-weight: 900;">${poi.title}</div></div><div style="color: #FFD600; font-size: 16px; font-weight: 900;">+${xp} XP</div></div><div style="border-top: 1px solid rgba(255,255,255,0.2); padding-top: 10px; font-size: 14px; line-height: 1.4; color: #eee; font-style: italic; width: 100%; text-align: left;">${poi.text}</div>`;
    container.appendChild(toast);

    const vrNotif = document.getElementById('vrPoiNotification');
    if (vrNotif) {
        document.getElementById('vrPoiTitle').setAttribute('value', cleanText(poi.title));
        document.getElementById('vrPoiDesc').setAttribute('value', cleanText(poi.text));
        vrNotif.setAttribute('visible', 'true');
        setTimeout(() => vrNotif.setAttribute('visible', 'false'), 20000);
    }
    setTimeout(() => { toast.style.opacity = "0"; setTimeout(() => toast.remove(), 500); }, 30000); 
}

// ==========================================
// 7. FIREBASE & MAPS (MODIFIKOVANÉ PRE ŠTATISTIKY)
// ==========================================

database.ref('currentRouteId').on('value', (s) => { 
    const id = s.val(); 
    if(id && id !== CURRENT_ROUTE_ID) { 
        CURRENT_ROUTE_ID = id; 
        lastSeenTriggeredStep = -1; 
        isFirstLoadForToasts = true;
        if(statsEl.routeName) statsEl.routeName.innerText = ROUTE_NAMES[id] || id; 
        loadRoute(id); 
    } 
});

database.ref('userProfile').on('value', (s) => { const d = s.val() || {}; if(d.weight) userWeight = parseFloat(d.weight); if(d.totalXp !== undefined) { userTotalXp = d.totalXp; updateLevelUI(); } checkAchievements(); });
database.ref('routes').on('value', (s) => { let sum = 0; s.forEach(c => { sum += parseInt(c.val().steps || 0); }); totalGlobalSteps = sum; checkAchievements(); });
database.ref('dailyStats').on('value', (s) => { let max=0; s.forEach(c => { const st = c.val().steps || 0; if(st > max) max = st; }); maxDailySteps = max; checkAchievements(); });

function updateLevelUI() {
    const lvl = getLevelFromXp(userTotalXp);
    const curXp = getXpRequiredForLevel(lvl); 
    const nxtXp = getXpRequiredForLevel(lvl + 1);
    let pct = nxtXp > curXp ? ((userTotalXp - curXp) / (nxtXp - curXp)) * 100 : 0;
    const clean = (str) => str ? str.normalize("NFD").replace(/[\u0300-\u036f]/g, "") : "";

    if(statsEl.lvlNum) statsEl.lvlNum.innerText = `Level ${lvl}`;
    if(statsEl.lvlRank) statsEl.lvlRank.innerText = getRankTitle(lvl);
    if(statsEl.lvlText) statsEl.lvlText.innerText = `${formatNum(userTotalXp)} / ${formatNum(nxtXp)} XP`;
    if(statsEl.lvlBar) statsEl.lvlBar.style.width = `${Math.min(100, pct)}%`;  

    const pano = document.getElementById('panorama');
    if (pano) {
        const vrLvlNum = pano.querySelector('#vrLevelNum');
        const vrLvlTitle = pano.querySelector('#vrLevelTitle');
        const vrLvlBar = pano.querySelector('#vrLevelBar');
        const vrLvlXpText = pano.querySelector('#vrLevelXpText');
        const v3dLvl = document.getElementById('vr3dLevelText');

        if (vrLvlNum) vrLvlNum.innerText = `Level ${lvl}`;
        if (vrLvlTitle) vrLvlTitle.innerText = getRankTitle(lvl);
        if (vrLvlBar) vrLvlBar.style.width = `${Math.min(100, pct)}%`;
        if (vrLvlXpText) vrLvlXpText.innerText = `${formatNum(userTotalXp)} / ${formatNum(nxtXp)} XP`;
        if (v3dLvl) v3dLvl.setAttribute('value', clean(`Level ${lvl} | ${getRankTitle(lvl)}`));
    }
}

// Pomocná funkcia pre dnešný dátum (zhodná s StepService.kt)
function getTodayKey() {
    const d = new Date();
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// Listener pre denné kroky
const dailyKey = getTodayKey();
database.ref(`dailyStats/${dailyKey}/steps`).on('value', (snapshot) => {
    const val = snapshot.val() || 0;
    
    // Aktualizácia na webe
    const dailyStepsEl = document.getElementById('dailySteps');
    if (dailyStepsEl) dailyStepsEl.innerText = formatNum(val);

    // Aktualizácia v čiernom VR paneli
    const vrDailyDisplay = document.getElementById('vrDailyStepsDisplay');
    if (vrDailyDisplay) vrDailyDisplay.innerText = formatNum(val);

    // Aktualizácia v 3D VR texte
    const vr3dDaily = document.getElementById('vr3dDailySteps');
    if (vr3dDaily) {
        vr3dDaily.setAttribute('value', `Dnes: ${formatNum(val)} kr.`);
    }
});

function checkAchievements() {
    let effectiveTotal = Math.max(totalGlobalSteps, currentRouteSteps);
    const nextMilestone = STEP_MILESTONES.find(m => m.t > effectiveTotal);
    if(nextMilestone) { 
        const prevT = STEP_MILESTONES[STEP_MILESTONES.indexOf(nextMilestone)-1]?.t || 0;
        const range = nextMilestone.t - prevT;
        const prog = effectiveTotal - prevT;
        const pct = (prog / range) * 100;
        if(statsEl.goalName) statsEl.goalName.innerText = nextMilestone.title; 
        if(statsEl.goalBar) statsEl.goalBar.style.width = `${Math.min(100, pct)}%`; 
        if(statsEl.goalCurr) statsEl.goalCurr.innerText = formatNum(prog); 
        if(statsEl.goalTarget) statsEl.goalTarget.innerText = formatNum(range); 
        const remaining = nextMilestone.t - effectiveTotal; 
        if(statsEl.goalRem) statsEl.goalRem.innerText = `(-${formatNum(remaining)})`; 
    } 
}

function formatNum(n) { return n ? n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ") : "0"; }

let routeRef = null;

function attachRouteListeners(id) {
    if(routeRef) routeRef.off();
    routeRef = database.ref(`routes/${id}`);
    routeRef.on('value', (s) => {
        const d = s.val(); if(!d) return;
        const steps = d.steps || 0; 
        currentRouteSteps = steps; 
        const newMaxTriggered = d.maxTriggeredSteps ?? -1;

        if (newMaxTriggered > lastSeenTriggeredStep) { if (!isFirstLoadForToasts && newMaxTriggered > 0) { const triggeredPoi = currentRoutePois.find(p => p.requiredSteps === newMaxTriggered); if (triggeredPoi) showPoiToast(triggeredPoi); } lastSeenTriggeredStep = newMaxTriggered; isFirstLoadForToasts = false; }

        const traveledKm = (steps * 0.00075).toFixed(2);
        const calCount = Math.floor(steps * 0.04 * (userWeight/70));

        if(statsEl.steps) statsEl.steps.innerText = formatNum(steps);      
        if(statsEl.calories) statsEl.calories.innerText = `${calCount} kcal`;

        const pano = document.getElementById('panorama');
        if (pano) {
            const vrSteps = pano.querySelector('#vrStepsDisplay');
            const vrCals = pano.querySelector('#vrCaloriesDisplay');
            const vrDist = pano.querySelector('#vrDistanceDisplay');
            if(vrSteps) vrSteps.innerText = formatNum(steps);
            if(vrCals) vrCals.innerText = calCount;
            if(vrDist) vrDist.innerText = traveledKm + " km";

            const v3dSteps = document.getElementById('vr3dSteps');
            const v3dDist = document.getElementById('vr3dDist');
            const v3dCals = document.getElementById('vr3dCals');
            if (v3dSteps) v3dSteps.setAttribute('value', formatNum(steps) + " kr.");
            if (v3dDist) v3dDist.setAttribute('value', traveledKm + " km");
            if (v3dCals) v3dCals.setAttribute('value', calCount + " kcal");

            updateVrNextPoi(steps, pano);
        }

        let idx = Math.floor(steps / (stepsPerMove || 10));
        if(idx >= routePoints.length) idx = routePoints.length - 1;

        // ������ OPRAVA ŠTATISTÍK NA WEBE (Obrázok 1)
        if(statsEl.pos) statsEl.pos.innerText = `${idx} / ${routePoints.length}`;
        if(statsEl.dist) statsEl.dist.innerText = traveledKm + " km";
        if(document.getElementById('percentage') && routePoints.length > 0) {
            document.getElementById('percentage').innerText = ((idx / (routePoints.length - 1)) * 100).toFixed(1) + " %";
        }

        if(idx !== lastRenderedIndex) { 
            moveVirtualPlayer(idx, steps); 
            if (routePoints[idx]) {
                const pos = { lat: routePoints[idx].lat, lng: routePoints[idx].lng };
                // ������ OPRAVA: Rozdelenie farieb trasy na mape
                if (traveledPolyline) traveledPolyline.setPath(routePoints.slice(0, idx + 1));
                if (remainingPolyline) remainingPolyline.setPath(routePoints.slice(idx));
                if (panorama && !isVRActive) {                          
            panorama.setPosition(pos);
            
            // Ak existuje nasledujúci bod, vypočítame smer k nemu
            if (idx < routePoints.length - 1) {
                const nextPt = routePoints[idx + 1];
                const targetHeading = google.maps.geometry.spherical.computeHeading(
                    new google.maps.LatLng(pos.lat, pos.lng),
                    new google.maps.LatLng(nextPt.lat, nextPt.lng)
                );
                // Nastavíme pohľad panorámy priamo na ďalší bod
                panorama.setPov({ heading: targetHeading, pitch: 0 });
            }
        }
                if (mapMarker) { mapMarker.setPosition(pos); if (map) map.panTo(pos); }
            }
            lastRenderedIndex = idx; 
        }
        checkAchievements(); updateGuideLocking(steps); updatePoiMarkersOnMap(); 
    });
}

function updateVrNextPoi(steps, container) {
    if (!currentRoutePois || currentRoutePois.length === 0 || !container) return;
    const nextPoi = currentRoutePois.find(p => p.requiredSteps > steps);
    const clean = (str) => str ? str.normalize("NFD").replace(/[\u0300-\u036f]/g, "") : "";
    
    const nName = container.querySelector('#vrNextPoiName');
    const nDist = container.querySelector('#vrNextPoiDist');
    const gName = container.querySelector('#vrGoalName');
    const gBar = container.querySelector('#vrGoalBar');
    const gText = container.querySelector('#vrGoalStepsText');
    const v3dPoi = document.getElementById('vr3dNextPoi');

    if (nextPoi) {
        const prevSteps = currentRoutePois[currentRoutePois.indexOf(nextPoi)-1]?.requiredSteps || 0;
        const range = nextPoi.requiredSteps - prevSteps;
        const prog = steps - prevSteps;
        const pct = Math.min(100, (prog / range) * 100);
        const diff = nextPoi.requiredSteps - steps;
        const distText = diff > 999 ? (diff * 0.00075).toFixed(2) + " km" : Math.round(diff * 0.75) + " m";

        if (nName) nName.innerText = nextPoi.title;
        if (nDist) nDist.innerText = distText;
        if (gName) gName.innerText = nextPoi.title;
        if (gBar) gBar.style.width = pct + "%";
        if (gText) gText.innerText = `${formatNum(prog)} / ${formatNum(range)} kr.`;
        if (v3dPoi) v3dPoi.setAttribute('value', clean(`Ciel: ${nextPoi.title} (${distText})`));
    } else {
        if (nName) nName.innerText = "Koniec trasy";
        if (v3dPoi) v3dPoi.setAttribute('value', "Koniec trasy");
    }
}

function updateGuideLocking(currentSteps) { 
    const items = statsEl.guideList.querySelectorAll('.poi-item');
    items.forEach(item => { const targetSteps = parseInt(item.dataset.steps); const remEl = item.querySelector(`#rem-${targetSteps}`); if (currentSteps >= targetSteps || maxTriggeredSteps >= targetSteps) { item.classList.remove('locked'); item.classList.add('unlocked'); if(remEl) remEl.innerText = ""; } else { if(remEl) remEl.innerText = `(-${formatNum(targetSteps - currentSteps)})`; } });
}

function updatePoiMarkersOnMap() {
    poiMarkers.forEach(m => m.setMap(null)); poiMarkers = [];
    if (!map || !routePoints.length || !currentRoutePois.length) return;
    let totalSteps = CURRENT_ROUTE_ID === 'bulikova_lada116' ? 542027 : 28014;
    currentRoutePois.forEach(poi => {
        const ratio = Math.min(1, poi.requiredSteps / totalSteps);
        const pointIdx = Math.floor(ratio * (routePoints.length - 1));
        const point = routePoints[pointIdx];
        if (point) {
            const isReached = currentRouteSteps >= poi.requiredSteps;
            const marker = new google.maps.Marker({ 
                position: { lat: point.lat, lng: point.lng }, 
                map: map, 
                title: poi.title, // ������ PRIDANÉ: Teraz sa zobrazí názov pri prejdení myšou
                icon: { 
                    path: google.maps.SymbolPath.CIRCLE, 
                    scale: 6, 
                    fillColor: isReached ? '#4CAF50' : '#FFD600', 
                    fillOpacity: 1, 
                    strokeColor: '#333', 
                    strokeWeight: 1 
                } 
            });
            poiMarkers.push(marker);
        }
    });
}
function moveVirtualPlayer(idx, steps) {
    if(!routePoints.length) return;
    const pt = routePoints[idx];
    const loc = { lat: pt.lat, lng: pt.lng };
    const sv = new google.maps.StreetViewService();
    
    sv.getPanorama({ location: loc, radius: 50 }, (data, status) => {
        if (status === "OK") {
            const panoId = data.location.pano;
            
            // ������ OPRAVA 1: Tu musíme definovať panoCenterHeading z dát od Google
            const panoCenterHeading = (data.tiles && data.tiles.centerHeading) ? data.tiles.centerHeading : 0;
            
            const sky = document.getElementById('skybox');
            if (!sky) return;
            
            // Logika pre sťahovanie dlaždíc (Tento kód je v poriadku)
            const canvas = document.createElement('canvas'); 
            canvas.width = 4096; 
            canvas.height = 2048;
            const ctx = canvas.getContext('2d'); 
            ctx.fillStyle = "black"; 
            ctx.fillRect(0, 0, canvas.width, canvas.height);
            
            let loadedCount = 0;
            for (let y = 0; y < 4; y++) {
                for (let x = 0; x < 8; x++) {
                    const img = new Image(); 
                    img.crossOrigin = "anonymous";
                    img.src = `https://cbk0.google.com/cbk?output=tile&panoid=${panoId}&zoom=3&x=${x}&y=${y}`;
                    img.onload = function() {
                        ctx.drawImage(img, x * 512, y * 512, 512, 512); 
                        loadedCount++;
                        if (loadedCount === 32) {
                            const tex = new THREE.CanvasTexture(canvas); 
                            tex.mapping = THREE.EquirectangularMapping;
                            const mesh = sky.getObject3D('mesh'); 
                            if (mesh && mesh.material) { 
                                mesh.material.map = tex; 
                                mesh.material.needsUpdate = true; 
                            }
                        }
                    };
                    img.onerror = () => loadedCount++;
                }
            }

            // ������ OPRAVA 2: Natočenie nulového bodu a reset kamery
            if (idx < routePoints.length - 1) {
                const nextPt = routePoints[idx+1];
                const targetHeading = google.maps.geometry.spherical.computeHeading(
                    new google.maps.LatLng(pt.lat, pt.lng),
                    new google.maps.LatLng(nextPt.lat, nextPt.lng)
                );
                
                // Výpočet rotácie: orientácia fotky voči smeru trasy
                const finalSkyRotation = panoCenterHeading - targetHeading - 90;
                
                // Nastavíme rotáciu sféry
                sky.setAttribute('rotation', `0 ${finalSkyRotation} 0`);

                // ������ Reset kamery (len ak sme vo VR), aby si sa pozeral na 0 (teda na cestu)
                const cameraEl = document.getElementById('vrCamera');
                if (cameraEl && cameraEl.components['look-controls'] && isVRActive) {
                    cameraEl.components['look-controls'].yawObject.rotation.y = 0;
                    cameraEl.components['look-controls'].pitchObject.rotation.x = 0;
                }
            }
        } else {
            console.error("Street View panoráma nebola nájdená pre tento bod.");
        }
    });
    updateLevelUI(); 
    checkAchievements();
}

function initMaps(routeId) {
    if(!routePoints.length || !window.google) return;
    const start = { lat: routePoints[0].lat, lng: routePoints[0].lng };
    
    // Výpočet celkovej vzdialenosti (Celkom km)
    const path = routePoints.map(p => new google.maps.LatLng(p.lat, p.lng));
    const totalKm = parseFloat((google.maps.geometry.spherical.computeLength(path) / 1000).toFixed(2));
    const initialHeading = routePoints.length > 1 ? 
    google.maps.geometry.spherical.computeHeading(start, routePoints[1]) : 0;
    if(statsEl.totalDist) statsEl.totalDist.innerText = `${totalKm} km`;

    if(!map) {
        map = new google.maps.Map(divMap, { center: start, zoom: 15, streetViewControl: false });
        mapMarker = new google.maps.Marker({ position: start, map: map, icon: { path: google.maps.SymbolPath.CIRCLE, scale: 8, fillColor: '#1565C0', fillOpacity: 1, strokeColor: 'white', strokeWeight: 2 } });
        panorama = new google.maps.StreetViewPanorama(divPanorama, { 
            position: start, 
            pov: { heading: initialHeading, pitch: 0 }, // Dynamický štart
            zoom: 1, 
            addressControl: false,
            clickToGo: false 
        });
                map.setStreetView(panorama);
        panorama.addListener('pov_changed', updateVrArrow);
    }
    
    // ������ NOVÉ: Inicializácia dvoch čiar na mape
    if(traveledPolyline) traveledPolyline.setMap(null);
    if(remainingPolyline) remainingPolyline.setMap(null);
    traveledPolyline = new google.maps.Polyline({ path: [], strokeColor: '#4CAF50', strokeOpacity: 1.0, strokeWeight: 6, map: map });
    remainingPolyline = new google.maps.Polyline({ path: routePoints, strokeColor: '#FF9800', strokeOpacity: 0.6, strokeWeight: 4, map: map });
}

function speakText(text) { if ('speechSynthesis' in window) { window.speechSynthesis.cancel(); window.speechSynthesis.speak(new SpeechSynthesisUtterance(text)); } }
function setupVrToggle() {
    const btn = document.getElementById('vrToggleStats'); const content = document.getElementById('vrHudContent');
    if (btn && content) { btn.onclick = (e) => { e.stopPropagation(); content.style.display = content.style.display === "none" ? "block" : "none"; }; }
}

function updateVrArrow() {
    const arrow2d = document.getElementById('vrArrow');
    const cameraEl = document.getElementById('vrCamera');
    
    if (!arrow2d || !cameraEl) return;

    let relativeRotation = 0;

    if (isVRActive) {
        // VR REŽIM: Cesta je na 0°, tak len kompenzujeme tvoj pohľad
        // Prevod radiánov z A-Frame na stupne
        const cameraRotY = cameraEl.object3D.rotation.y * (180 / Math.PI);
        
        // Mínus zabezpečí, že šípka ukazuje späť k ceste (k nule)
        relativeRotation = -cameraRotY; 
    } else if (panorama) {
        // 2D REŽIM: Klasický výpočet podľa Google Maps kompasu
        const currentPt = routePoints[lastRenderedIndex];
        const nextPt = routePoints[lastRenderedIndex + 1];
        if (currentPt && nextPt) {
            const targetHeading = google.maps.geometry.spherical.computeHeading(currentPt, nextPt);
            relativeRotation = targetHeading - panorama.getPov().heading;
        }
    }

    arrow2d.style.transform = `rotate(${relativeRotation}deg)`;
}

// Spustíme slučku aktualizácie
// Jedna verzia slučky, ktorá beží stále a kontroluje šípku

// Táto funkcia beží stále a plynule otáča šípku
function startVrArrowLoop() {
    updateVrArrow();
    requestAnimationFrame(startVrArrowLoop);
}

// Spustenie hneď po načítaní stránky
startVrArrowLoop();

// Finálne zrovnanie rozloženia
updateLayout();