import json

def smooth_path(path, target_count):
    new_path = []
    for i in range(target_count):
        idx = i * (len(path) - 1) / (target_count - 1)
        low = int(idx)
        high = low + 1 if low < len(path) - 1 else low
        ratio = idx - low
        lat = path[low]['lat'] + (path[high]['lat'] - path[low]['lat']) * ratio
        lng = path[low]['lng'] + (path[high]['lng'] - path[low]['lng']) * ratio
        new_path.append({"lat": round(lat, 6), "lng": round(lng, 6)})
    return new_path

# Načítanie dát od Google
with open('google_raw.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

# Extrakcia bodov z jednotlivých krokov (Steps) - toto nepotrebuje knižnicu polyline
coords = []
steps = data['routes'][0]['legs'][0]['steps']
for s in steps:
    coords.append(s['start_location'])
coords.append(steps[-1]['end_location']) # Pridáme úplný koniec

# Zahustenie na 2001 bodov
final_points = smooth_path(coords, 2001)

final_json = {
    "routeName": "Grand Canyon - Bright Angel Trail",
    "stepsPerMove": 15,
    "points": final_points
}

with open('route_grand_canyon.json', 'w', encoding='utf-8') as f:
    json.dump(final_json, f, indent=2)

print(f"Hotovo! Vygenerovaných {len(final_points)} bodov bez potreby polyline knižnice.")