// generate_route.js
const fs = require('fs');
const https = require('https');
const polyline = require('@googlemaps/polyline-codec');

const API_KEY = 'AIzaSyDCp9kYg5dVadbncxuP24QeYX2a1Qze2is'; // tvoj kľúč
const ORIGIN = 'Bulikova 15, Bratislava, Slovakia';
const DEST = 'Eurovea, Bratislava, Slovakia';
const MODE = 'walking';

function callDirectionsApi() {
  const url = `https://maps.googleapis.com/maps/api/directions/json?origin=${encodeURIComponent(ORIGIN)}&destination=${encodeURIComponent(DEST)}&mode=${MODE}&key=${API_KEY}`;

  https.get(url, (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => {
      try {
        const json = JSON.parse(data);
        if (!json.routes || !json.routes.length) {
          console.error('Žiadne trasy v odpovedi:', json.status);
          return;
        }
        processRoute(json);
      } catch (e) {
        console.error('Chyba pri parsovaní JSON:', e);
      }
    });
  }).on('error', (err) => {
    console.error('HTTP chyba:', err);
  });
}

function processRoute(directions) {
  const route = directions.routes[0];
  const leg = route.legs[0];

  const distanceMeters = leg.distance.value; // celková dĺžka v metroch

  // Detailná trasa: spojíme všetky step polylines
  const allPoints = [];
  leg.steps.forEach((step, stepIndex) => {
    const enc = step.polyline.points;
    const decoded = polyline.decode(enc); // [ [lat, lng], ... ]

    decoded.forEach(([lat, lng], i) => {
      // aby sa body nezdvojovali, prvý bod každého ďalšieho stepu preskočíme
      if (stepIndex > 0 && i === 0) return;
      allPoints.push({ lat, lng });
    });
  });

  const pointCount = allPoints.length;
  const avgDistance = distanceMeters / (pointCount - 1); // priemer m medzi bodmi
  const stepLength = 0.7; // m
  const stepsPerMove = Math.round(avgDistance / stepLength);

  const routeJson = {
    name: 'Bulíkova 15 → Eurovea (pešo, detailná trasa)',
    lengthMeters: distanceMeters,
    stepsPerMove: stepsPerMove,
    points: allPoints
  };

  fs.writeFileSync(
    'route_bulikova_eurovea.json',
    JSON.stringify(routeJson, null, 2),
    'utf8'
  );

  console.log('Dĺžka trasy (m):', distanceMeters);
  console.log('Body (detailné):', pointCount);
  console.log('Priemerná vzdialenosť medzi bodmi (m):', avgDistance.toFixed(2));
  console.log('StepsPerMove (krokov na jeden bod):', stepsPerMove);
  console.log('Zapísané do route_bulikova_eurovea.json');
}

callDirectionsApi();
