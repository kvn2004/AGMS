import os
import json
from datetime import datetime
from fastapi import FastAPI
from pydantic import BaseModel
import redis
import py_eureka_client.eureka_client as eureka_client
from dotenv import load_dotenv
import threading
import httpx

load_dotenv()

class TelemetryData(BaseModel):
    deviceId: str
    zoneId: str
    value: dict
    capturedAt: str

# Redis with fallback
redis_client = None
try:
    redis_client = redis.from_url(os.getenv("REDIS_URL"), decode_responses=True, socket_timeout=3)
    redis_client.ping()
    print("✅ Redis connected")
except:
    print("⚠️ Using in-memory logs")

app = FastAPI()

# ====================== Zone Service Integration ======================
async def fetch_zone_thresholds(zone_id: str) -> dict:
    """
    Fetch zone thresholds from Zone Service.
    Returns: {"minTemp": float, "maxTemp": float}
    Falls back to defaults if Zone Service is unavailable.
    """
    zone_service_url = os.getenv("ZONE_SERVICE_URL", "http://localhost:8081")
    
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            response = await client.get(f"{zone_service_url}/api/zones/{zone_id}")
            
            if response.status_code == 200:
                zone_data = response.json()
                min_temp = zone_data.get("minTemp", 20)
                max_temp = zone_data.get("maxTemp", 30)
                print(f"✅ Fetched zone {zone_id} thresholds: min={min_temp}°C, max={max_temp}°C")
                return {"minTemp": min_temp, "maxTemp": max_temp}
            else:
                print(f"⚠️ Zone Service returned {response.status_code}, using defaults")
                return {"minTemp": 20, "maxTemp": 30}
    except Exception as e:
        print(f"⚠️ Failed to fetch zone thresholds: {str(e)}, using defaults")
        return {"minTemp": 20, "maxTemp": 30}

@app.post("/api/automation/process")
async def process_telemetry(data: TelemetryData):
    try:
        temp = data.value.get("temperature")
        zone_id = data.zoneId

        # Fetch zone-specific thresholds
        thresholds = await fetch_zone_thresholds(zone_id)
        min_temp = thresholds["minTemp"]
        max_temp = thresholds["maxTemp"]

        action = None
        if temp > max_temp:
            action = "TURN_FAN_ON"
        elif temp < min_temp:
            action = "TURN_HEATER_ON"

        if action:
            log_entry = {
                "zoneId": zone_id,
                "deviceId": data.deviceId,
                "action": action,
                "temperature": temp,
                "minThreshold": min_temp,
                "maxThreshold": max_temp,
                "timestamp": datetime.utcnow().isoformat()
            }
            if redis_client:
                redis_client.lpush("automation:logs", json.dumps(log_entry))
            else:
                if not hasattr(app, "logs"):
                    app.logs = []
                app.logs.append(log_entry)
            print(f"✅ Action triggered: {action} | Temp: {temp}°C | Zone Thresholds: {min_temp}-{max_temp}°C")
        return {"status": "processed", "action": action or "NO_ACTION", "thresholds": {"minTemp": min_temp, "maxTemp": max_temp}}
    except Exception as e:
        print("Error:", str(e))
        return {"status": "error"}

@app.get("/api/automation/logs")
async def get_logs():
    if redis_client:
        return [json.loads(log) for log in redis_client.lrange("automation:logs", 0, -1)]
    return getattr(app, "logs", [])

@app.get("/health")
async def health():
    return {"status": "UP"}

# ====================== EUREKA REGISTRATION (Threaded - no event loop conflict) ======================
def register_eureka():
    print("🔄 Registering with Eureka...")
    try:
        eureka_client.init(
            eureka_server=os.getenv("EUREKA_SERVER"),
            app_name="automation-control-service",
            instance_port=int(os.getenv("PORT", 8083)),
            instance_host="localhost"
        )
        print("🎉 SUCCESS! Automation Service registered with Eureka")
    except Exception as e:
        print("❌ Eureka failed (normal in reload mode):", str(e))

# Run registration in background thread
threading.Thread(target=register_eureka, daemon=True).start()

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=int(os.getenv("PORT", 8083)))