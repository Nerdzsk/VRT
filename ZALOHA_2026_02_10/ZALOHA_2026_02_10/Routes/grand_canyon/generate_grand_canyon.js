const fs = require('fs');
const https = require('https');
const polyline = require('@googlemaps/polyline-codec');

// Tvoj API kľúč a nastavenia pre Grand Canyon
const API_KEY = 'AIzaSyDCp9kYg5dVadbncxuP24QeYX2a1Qze2is'; 
const ORIGIN = '36.0573,-112.1438'; // South Rim Start
const DEST = '36.105151, -112.094595';
const MODE = 'walking';

function callDirectionsApi() {
  const url = `https://maps.googleapis.com/maps/api/directions/json?origin=${ORIGIN}&destination=${DEST}&mode=${MODE}&key=${API_KEY}`;

  https.get(url, (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
      try {
        const json = JSON.parse(data);
        if (!json.routes || !json.routes.length) {
          console.error('Žiadne trasy:', json.status);
          return;
        }
        processRoute(json);
      } catch (e) { console.error('Chyba:', e); }
    });
  });
}

function processRoute(directions) {
  const route = directions.routes[0];
  const leg = route.legs[0];
  const allPoints = [];

  leg.steps.forEach((step, stepIndex) => {
    const decoded = polyline.decode(step.polyline.points);
    decoded.forEach(([lat, lng], i) => {
      if (stepIndex > 0 && i === 0) return;
      allPoints.push({ lat, lng });
    });
  });

  // Interpolácia na presne 2001 bodov (aby ti sedelo UI 0/2001)
  const finalPoints = [];
  const targetCount = 2001;
  for (let i = 0; i < targetCount; i++) {
    const idx = i * (allPoints.length - 1) / (targetCount - 1);
    const low = Math.floor(idx);
    const high = Math.ceil(idx);
    const ratio = idx - low;
    
    const lat = allPoints[low].lat + (allPoints[high].lat - allPoints[low].lat) * ratio;
    const lng = allPoints[low].lng + (allPoints[high].lng - allPoints[low].lng) * ratio;
    finalPoints.push({ lat: parseFloat(lat.toFixed(6)), lng: parseFloat(lng.toFixed(6)) });
  }

  const routeJson = {
    routeName: "Grand Canyon - Bright Angel Trail",
    stepsPerMove: 15,
    points: finalPoints
  };

  fs.writeFileSync('route_grand_canyon.json', JSON.stringify(routeJson, null, 2));
  console.log(`Vygenerovaných ${finalPoints.length} bodov. Súbor pripravený!`);
}

callDirectionsApi();// JavaScript Document
