import os
import uuid
from datetime import datetime
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from sqlalchemy import create_engine, Column, String, Integer, DateTime, Enum
from sqlalchemy.orm import sessionmaker, declarative_base
from sqlalchemy.exc import SQLAlchemyError
from dotenv import load_dotenv
import py_eureka_client.eureka_client as eureka_client
import threading

load_dotenv()

# ====================== MySQL Connection ======================
DATABASE_URL = f"mysql+pymysql://{os.getenv('MYSQL_USER')}:{os.getenv('MYSQL_PASSWORD')}@{os.getenv('MYSQL_HOST')}:{os.getenv('MYSQL_PORT')}/{os.getenv('MYSQL_DATABASE')}"

engine = create_engine(DATABASE_URL, echo=False)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# ====================== Database Model ======================
class CropDB(Base):
    __tablename__ = "crops"
    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    name = Column(String(100), nullable=False)
    batch_id = Column(String(50), nullable=False)
    quantity = Column(Integer, nullable=False)
    status = Column(Enum('SEEDLING', 'VEGETATIVE', 'HARVESTED', name='status_enum'), nullable=False, default='SEEDLING')
    created_at = Column(DateTime, default=datetime.utcnow)

Base.metadata.create_all(bind=engine)   # Table එක auto create වෙනවා

# ====================== Pydantic Models ======================
class CropCreate(BaseModel):
    name: str
    batchId: str
    quantity: int
    status: str = "SEEDLING"

class CropUpdateStatus(BaseModel):
    status: str

# ====================== FastAPI App ======================
app = FastAPI()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.post("/api/crops")
async def create_crop(crop: CropCreate):
    db = next(get_db())
    try:
        db_crop = CropDB(
            name=crop.name,
            batch_id=crop.batchId,
            quantity=crop.quantity,
            status=crop.status
        )
        db.add(db_crop)
        db.commit()
        db.refresh(db_crop)
        print(f"✅ New crop created: {crop.name} | Status: {crop.status}")
        return db_crop
    except SQLAlchemyError as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

@app.put("/api/crops/{crop_id}/status")
async def update_crop_status(crop_id: str, update: CropUpdateStatus):
    db = next(get_db())
    try:
        crop = db.query(CropDB).filter(CropDB.id == crop_id).first()
        if not crop:
            raise HTTPException(status_code=404, detail="Crop not found")
        crop.status = update.status
        db.commit()
        db.refresh(crop)
        print(f"✅ Crop {crop_id} status updated to {update.status}")
        return {"message": "Status updated", "crop": crop}
    except SQLAlchemyError as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/crops")
async def get_all_crops():
    db = next(get_db())
    return db.query(CropDB).all()

@app.get("/health")
async def health():
    return {"status": "UP"}

# ====================== EUREKA REGISTRATION ======================
def register_eureka():
    print("🔄 Registering Crop Inventory Service with Eureka...")
    try:
        eureka_client.init(
            eureka_server=os.getenv("EUREKA_SERVER"),
            app_name="crop-inventory-service",
            instance_port=int(os.getenv("PORT", 8084)),
            instance_host="localhost"
        )
        print("🎉 SUCCESS! Crop Inventory Service registered with Eureka")
    except Exception as e:
        print("❌ Eureka failed:", str(e))

threading.Thread(target=register_eureka, daemon=True).start()

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=int(os.getenv("PORT", 8084)))