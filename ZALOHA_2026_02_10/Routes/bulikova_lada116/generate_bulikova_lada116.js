const fetch = require('node-fetch').default;
const polyline = require('@mapbox/polyline');
const fs = require('fs');

const API_KEY = 'AIzaSyDCp9kYg5dVadbncxuP24QeYX2a1Qze2is';

async function buildRouteJson() {
  const url = 'https://maps.googleapis.com/maps/api/directions/json'
    + '?origin=Bulikova%2015,Bratislava,Slovakia'
    + '&destination=49.04492526064648,21.35752738462094'
    + '&mode=walking'
    + '&region=sk'
    + `&key=${API_KEY}`;

  console.log('Volam Directions API...');
  const res = await fetch(url);
  const data = await res.json();

  if (!data.routes || data.routes.length === 0) {
    console.error('Ziadna trasa nebola najdena:', data.status, data.error_message);
    return;
  }

  const route = data.routes[0];
  const leg = route.legs[0];

  // Spojíme všetky polyline zo steps, aby bolo viac bodov
  let allCoords = [];

  leg.steps.forEach(step => {
    const enc = step.polyline.points;
    const coords = polyline.decode(enc); // [[lat, lng], ...]
    allCoords = allCoords.concat(coords);
  });

  const points = allCoords.map(([lat, lng]) => ({ lat, lng }));

  const routeJson = {
    name: "Bulíkova 15 -> Lada 116 (pešo)",
    stepsPerMove: 12,
    points
  };

  fs.writeFileSync('route_bulikova_lada116.json', JSON.stringify(routeJson, null, 2), 'utf8');
  console.log('route_bulikova_lada116.json bol vytvoreny.');
}

buildRouteJson().catch(console.error);
