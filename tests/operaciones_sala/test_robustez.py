import requests

# Fallback for target URL if not injected by TestSprite
try:
    BASE_URL = TARGET_URL
except NameError:
    BASE_URL = "https://ict-benefit-cottage-ricky.trycloudflare.com"

# Fallback for auth headers if not injected by TestSprite
try:
    HEADERS = __AUTH_HEADERS__
except NameError:
    HEADERS = {}

def test_intentar_romper_backend():
    print(f"Iniciando pruebas de robustez contra: {BASE_URL}")
    temporada = "2026/2027"

    # Con la corrección en SecurityConfig.java y DTO, todos los errores
    # de entrada y validación deben retornar exactamente 400 Bad Request.

    # 1. Enviar payload vacío
    print("\n--- Test 1: Payload completamente vacío ---")
    res = requests.post(f"{BASE_URL}/api/hivehub/sala-extraccion", json={}, headers=HEADERS)
    print(f"Status obtenido: {res.status_code}")
    assert res.status_code == 400, f"Se esperaba 400, se obtuvo {res.status_code}"
    print("Test 1 exitoso (el servidor rechazó correctamente el payload vacío con 400).")

    # 2. Cantidad de alzas igual a 0
    print("\n--- Test 2: Cantidad de alzas igual a 0 ---")
    payload = {
        "fecha": "2026-07-10",
        "tipoOperacion": "INGRESO",
        "cantidadAlzas": 0,
        "temporada": temporada
    }
    res = requests.post(f"{BASE_URL}/api/hivehub/sala-extraccion", json=payload, headers=HEADERS)
    print(f"Status obtenido: {res.status_code}")
    assert res.status_code == 400, f"Se esperaba 400, se obtuvo {res.status_code}"
    print("Test 2 exitoso (el servidor rechazó alzas = 0 con 400).")

    # 3. Cantidad de alzas negativa
    print("\n--- Test 3: Cantidad de alzas negativa ---")
    payload = {
        "fecha": "2026-07-10",
        "tipoOperacion": "INGRESO",
        "cantidadAlzas": -5,
        "temporada": temporada
    }
    res = requests.post(f"{BASE_URL}/api/hivehub/sala-extraccion", json=payload, headers=HEADERS)
    print(f"Status obtenido: {res.status_code}")
    assert res.status_code == 400, f"Se esperaba 400, se obtuvo {res.status_code}"
    print("Test 3 exitoso (el servidor rechazó alzas negativas con 400).")

    # 4. Tipo de operación inválido
    print("\n--- Test 4: Tipo de operación inválido (ROBO) ---")
    payload = {
        "fecha": "2026-07-10",
        "tipoOperacion": "ROBO",
        "cantidadAlzas": 5,
        "temporada": temporada
    }
    res = requests.post(f"{BASE_URL}/api/hivehub/sala-extraccion", json=payload, headers=HEADERS)
    print(f"Status obtenido: {res.status_code}")
    assert res.status_code == 400, f"Se esperaba 400, se obtuvo {res.status_code}"
    print("Test 4 exitoso (el servidor rechazó tipo de operación inválido con 400).")

    # 5. Temporada vacía
    print("\n--- Test 5: Temporada vacía ---")
    payload = {
        "fecha": "2026-07-10",
        "tipoOperacion": "INGRESO",
        "cantidadAlzas": 5,
        "temporada": "   "
    }
    res = requests.post(f"{BASE_URL}/api/hivehub/sala-extraccion", json=payload, headers=HEADERS)
    print(f"Status obtenido: {res.status_code}")
    assert res.status_code == 400, f"Se esperaba 400, se obtuvo {res.status_code}"
    print("Test 5 exitoso (el servidor rechazó temporada en blanco con 400).")

    # 6. Formato de fecha incorrecto
    print("\n--- Test 6: Formato de fecha incorrecto (DD-MM-YYYY) ---")
    payload = {
        "fecha": "10-07-2026",
        "tipoOperacion": "INGRESO",
        "cantidadAlzas": 5,
        "temporada": temporada
    }
    res = requests.post(f"{BASE_URL}/api/hivehub/sala-extraccion", json=payload, headers=HEADERS)
    print(f"Status obtenido: {res.status_code}")
    assert res.status_code == 400, f"Se esperaba 400, se obtuvo {res.status_code}"
    print("Test 6 exitoso (el formato de fecha inválido fue rechazado con 400).")

    # 7. Tipado incorrecto en cantidad de alzas (String)
    print("\n--- Test 7: Tipado incorrecto en cantidad de alzas (String) ---")
    payload = {
        "fecha": "2026-07-10",
        "tipoOperacion": "INGRESO",
        "cantidadAlzas": "diez",
        "temporada": temporada
    }
    res = requests.post(f"{BASE_URL}/api/hivehub/sala-extraccion", json=payload, headers=HEADERS)
    print(f"Status obtenido: {res.status_code}")
    assert res.status_code == 400, f"Se esperaba 400, se obtuvo {res.status_code}"
    print("Test 7 exitoso (el parsing de tipos falló correctamente con 400).")

    # 8. Intentar registrar una extracción con kilos de miel negativos (Ahora con validación @Positive)
    print("\n--- Test 8: Extracción con kilos de miel negativos ---")
    payload = {
        "fecha": "2026-07-10",
        "tipoOperacion": "EXTRACCION",
        "cantidadAlzas": 5,
        "kilosMiel": -250.0,
        "temporada": temporada
    }
    res = requests.post(f"{BASE_URL}/api/hivehub/sala-extraccion", json=payload, headers=HEADERS)
    print(f"Status obtenido: {res.status_code}")
    assert res.status_code == 400, f"Se esperaba 400 (Bad Request), se obtuvo {res.status_code}"
    print("Test 8 exitoso (el backend rechazó correctamente los kilos de miel negativos con 400).")
    
    print("\n¡Pruebas de robustez finalizadas con éxito total!")

# Ejecutar las pruebas
test_intentar_romper_backend()
