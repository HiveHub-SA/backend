import requests
import json

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

def test_flujo_operaciones_sala():
    print(f"Iniciando pruebas de operaciones de sala en: {BASE_URL}")
    
    # 1. Obtener el resumen inicial para la temporada "2026/2027"
    temporada = "2026/2027"
    res = requests.get(f"{BASE_URL}/api/hivehub/sala-extraccion/resumen", params={"temporada": temporada}, headers=HEADERS)
    assert res.status_code == 200, f"Error al obtener resumen inicial: {res.status_code}"
    resumen_inicial = res.json()
    
    miel_inicial = resumen_inicial.get("totalMielExtraida", 0.0)
    procesadas_inicial = resumen_inicial.get("alzasProcesadas", 0)
    espera_inicial = resumen_inicial.get("alzasEnEspera", 0)
    
    print(f"Resumen inicial -> Espera: {espera_inicial}, Procesadas: {procesadas_inicial}, Miel: {miel_inicial}")

    # 2. Registrar un INGRESO de alzas
    payload_ingreso = {
        "fecha": "2026-07-10",
        "tipoOperacion": "INGRESO",
        "cantidadAlzas": 10,
        "temporada": temporada
    }
    res_ingreso = requests.post(f"{BASE_URL}/api/hivehub/sala-extraccion", json=payload_ingreso, headers=HEADERS)
    assert res_ingreso.status_code == 201, f"Error al registrar ingreso: {res_ingreso.status_code} - {res_ingreso.text}"
    ingreso_data = res_ingreso.json()
    assert ingreso_data.get("tipoOperacion") == "INGRESO"
    assert ingreso_data.get("cantidadAlzas") == 10
    assert ingreso_data.get("temporada") == temporada
    print("Ingreso registrado con éxito.")

    # 3. Validar que el resumen se actualice (alzas en espera deben aumentar en 10)
    res = requests.get(f"{BASE_URL}/api/hivehub/sala-extraccion/resumen", params={"temporada": temporada}, headers=HEADERS)
    assert res.status_code == 200
    resumen_post_ingreso = res.json()
    espera_post_ingreso = resumen_post_ingreso.get("alzasEnEspera", 0)
    assert espera_post_ingreso == espera_inicial + 10, f"Se esperaban {espera_inicial + 10} alzas en espera, hay {espera_post_ingreso}"
    print(f"Resumen post-ingreso validado. Alzas en espera: {espera_post_ingreso}")

    # 4. Validar el historial
    res_hist = requests.get(f"{BASE_URL}/api/hivehub/sala-extraccion/historial", params={"temporada": temporada}, headers=HEADERS)
    assert res_hist.status_code == 200
    historial = res_hist.json()
    assert len(historial) > 0
    # Buscar nuestro registro en el historial
    registros_coincidentes = [op for op in historial if op.get("id") == ingreso_data.get("id")]
    assert len(registros_coincidentes) == 1, "No se encontró el registro de ingreso en el historial"
    print("Historial validado con éxito.")

    # 5. Registrar una EXTRACCION de alzas (procesando 4 alzas y extrayendo 120.5 kg de miel)
    payload_extraccion = {
        "fecha": "2026-07-10",
        "tipoOperacion": "EXTRACCION",
        "cantidadAlzas": 4,
        "kilosMiel": 120.5,
        "temporada": temporada
    }
    res_extraccion = requests.post(f"{BASE_URL}/api/hivehub/sala-extraccion", json=payload_extraccion, headers=HEADERS)
    assert res_extraccion.status_code == 201, f"Error al registrar extracción: {res_extraccion.status_code} - {res_extraccion.text}"
    extraccion_data = res_extraccion.json()
    assert extraccion_data.get("tipoOperacion") == "EXTRACCION"
    assert extraccion_data.get("cantidadAlzas") == 4
    assert extraccion_data.get("kilosMiel") == 120.5
    print("Extracción registrada con éxito.")

    # 6. Validar que el resumen final refleje los cambios:
    # - Alzas en espera disminuyen en 4
    # - Alzas procesadas aumentan en 4
    # - Kilos de miel aumentan en 120.5
    res = requests.get(f"{BASE_URL}/api/hivehub/sala-extraccion/resumen", params={"temporada": temporada}, headers=HEADERS)
    assert res.status_code == 200
    resumen_final = res.json()
    
    assert resumen_final.get("alzasEnEspera", 0) == espera_post_ingreso - 4, "Las alzas en espera no disminuyeron correctamente"
    assert resumen_final.get("alzasProcesadas", 0) == procesadas_inicial + 4, "Las alzas procesadas no aumentaron correctamente"
    assert resumen_final.get("totalMielExtraida", 0.0) == miel_inicial + 120.5, "Los kilos de miel no se sumaron correctamente"
    
    print(f"Resumen final validado. Espera: {resumen_final.get('alzasEnEspera')}, Procesadas: {resumen_final.get('alzasProcesadas')}, Miel: {resumen_final.get('totalMielExtraida')}")
    print("¡Todas las validaciones backend de operaciones de sala pasaron exitosamente!")

# Ejecutar la prueba
test_flujo_operaciones_sala()
