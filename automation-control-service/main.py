import os
import json
from datetime import datetime
from fastapi import FastAPI
from pydantic import BaseModel
import redis
import py_eureka_client.eureka_client as eureka_client
from dotenv import load_dotenv
import threading

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

@app.post("/api/automation/process")
async def process_telemetry(data: TelemetryData):
    try:
        temp = data.value.get("temperature")
        zone_id = data.zoneId

        action = None
        if temp > 30:
            action = "TURN_FAN_ON"
        elif temp < 20:
            action = "TURN_HEATER_ON"

        if action:
            log_entry = {
                "zoneId": zone_id,
                "deviceId": data.deviceId,
                "action": action,
                "temperature": temp,
                "timestamp": datetime.utcnow().isoformat()
            }
            if redis_client:
                redis_client.lpush("automation:logs", json.dumps(log_entry))
            else:
                if not hasattr(app, "logs"):
                    app.logs = []
                app.logs.append(log_entry)
            print(f"✅ Action triggered: {action} | Temp: {temp}°C")
        return {"status": "processed", "action": action or "NO_ACTION"}
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