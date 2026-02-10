# Copilot Instructions for VR Turista

## Project Overview
- **VR Turista** is a web-based and mobile-integrated project that lets users walk in real life and move through Google Street View routes (e.g., Grand Canyon, Bratislava) in a VR/3D environment.
- The project combines a web frontend (HTML/CSS/JS, A-Frame for VR), Firebase for real-time data, and route/POI generation scripts (Node.js, Python).

## Key Components
- **Frontend:**
  - `index.html`, `style.css`, `script.js` — Main UI, logic, and VR rendering (A-Frame, Google Street View integration, user stats, and guide panels).
  - Uses Firebase for real-time updates (user steps, progress, etc.).
- **Route/POI Data:**
  - JSON files (e.g., `route_grand_canyon.json`, `poi_grand_canyon.json`) define routes and points of interest.
  - Located in root and `Routes/` subfolders, grouped by location.
- **Route Generation:**
  - Node.js scripts (e.g., `generate_grand_canyon.js`, `generate_route.js`) fetch and decode Google Directions API data, outputting route JSONs.
  - Python scripts (e.g., `generator.py.py`, `finish_route.py`) interpolate and smooth route points for more realistic movement.

## Developer Workflows
- **Generating/Updating Routes:**
  - Run Node.js scripts in `Routes/<route>/` to fetch and decode Google Maps data. Example:
    ```
    node generate_grand_canyon.js
    ```
  - Use Python scripts for custom interpolation or smoothing:
    ```
    python finish_route.py
    ```
- **Frontend Development:**
  - Edit `index.html`, `script.js`, and `style.css` for UI/logic changes.
  - Use A-Frame for VR scene updates.
- **Firebase:**
  - Configuration is in `script.js`. Uses Realtime Database for step tracking.

## Project-Specific Patterns & Conventions
- Route and POI JSONs follow a consistent schema: arrays of `{lat, lng}` or POI objects.
- Route generation scripts are duplicated per route (see `Routes/<route>/`).
- API keys are hardcoded in scripts (replace with your own for production).
- Level system and user progress logic are in `script.js`.
- Use Slovak for UI and comments.

## Integration Points
- Google Maps Directions API (Node.js scripts)
- Firebase Realtime Database (frontend)
- A-Frame (VR rendering)

## Example Files
- `Routes/grand_canyon/generate_grand_canyon.js` — Google Maps fetch & decode
- `Routes/finish_route.py` — Path smoothing
- `script.js` — Main app logic, Firebase, level system

---

For new features, follow the structure of existing route scripts and JSONs. When in doubt, check similar files for patterns.
