import json
import math

def interpolate(p1, p2, num_points):
    res = []
    for i in range(num_points):
        f = i / num_points
        res.append({
            "lat": round(p1[0] + (p2[0] - p1[0]) * f, 6),
            "lng": round(p1[1] + (p2[1] - p1[1]) * f, 6)
        })
    return res

# Rozšírené kľúčové body pre dosiahnutie 19 km (South Rim -> River -> Plateau Point)
key_waypoints = [
    (36.05735, -112.14385), # Start: South Rim
    (36.0585, -112.1459),   
    (36.0614, -112.1537),   
    (36.0603, -112.1565),   # Mile-and-a-half Resthouse
    (36.0556, -112.1624),   # Three-mile Resthouse
    (36.0586, -112.1678),   
    (36.0645, -112.1741),   # Indian Garden
    (36.0782, -112.1261),   # Klesanie k rieke Colorado
    (36.1030, -112.0950),   # Dosiahnutie úrovne rieky
    (36.0920, -112.1090),   # Návrat smerom k Plateau Point
    (36.0613, -112.2126)    # Cieľ: Plateau Point
]

all_points = []
# Zvýšený počet bodov (250 na úsek) pre dosiahnutie 19 km trasy
for i in range(len(key_waypoints) - 1):
    all_points.extend(interpolate(key_waypoints[i], key_waypoints[i+1], 250))

route_data = {
    "name": "Grand Canyon Expedition (19km)",
    "stepsPerMove": 15,
    "baseXp": 1500,
    "points": all_points
}

with open('route_grand_canyon.json', 'w', encoding='utf-8') as f:
    json.dump(route_data, f, indent=2)

print(f"Hotovo! Vygenerovaných {len(all_points)} bodov pre 19km trasu.")