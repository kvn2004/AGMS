# AGMS Documentation

## Eureka Dashboard Screenshot

### How to Capture

1. **Start all services** using the startup sequence in README.md

2. **Wait 60 seconds** for all services to register with Eureka

3. **Navigate to**: http://localhost:8761/

4. **Expected Screen**:
   - Title: "Eureka Dashboard"
   - Section: "Instances currently registered with Eureka"
   - All services should show as UP in green:
     - **eureka-server** - UP (1)
     - **api-gateway** - UP (1)
     - **config-server** - UP (1)
     - **zone-service** - UP (1)
     - **sensor-telemetry-service** - UP (1)
     - **automation-control-service** - UP (1)
     - **crop-inventory-service** - UP (1)

5. **Capture screenshot** using:
   - Windows: Print Screen or Snipping Tool
   - Mac: Cmd + Shift + 4
   - Linux: Screenshot tool

6. **Save screenshot** as `eureka-dashboard.png` in this docs folder

---

## Service Health Verification

### Manual Health Checks

Run these commands to verify all services are running:

```bash
# Eureka Server
curl http://localhost:8761/eureka/apps

# Config Server
curl http://localhost:8888/actuator/health

# API Gateway
curl http://localhost:8080/actuator/health

# Zone Service
curl http://localhost:8081/health

# Sensor Service
curl http://localhost:8082/health

# Automation Service
curl http://localhost:8083/health

# Crop Service
curl http://localhost:8084/health
```

All should return status: "UP"

---

## Testing Endpoints

### With Postman Collection

1. Import `AGMS-Postman-Collection.json` into Postman
2. Follow the "End-to-End Flow" requests in order
3. Verify all responses are successful (2xx status codes)

### Manual curl Testing

See README.md for complete curl examples

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────┐
│           External IoT Data Provider               │
│      http://104.211.95.241:8080/api                │
└────────────────────────┬────────────────────────────┘
                         │ (Real-time telemetry)
                         ▼
        ┌────────────────────────────────────┐
        │     API Gateway (Port 8080)        │
        │   + JWT Authentication Filter      │
        │   + Service Routing & Load Balance │
        └────────┬───────────────────────────┘
                 │
    ┌────────────┼────────────┬──────────┐
    ▼            ▼            ▼          ▼
[Zone]       [Sensor]     [Automation] [Crop]
(8081)       (8082)       (8083)       (8084)
   │            │            │           │
   └────────┬───┴──────┬─────┴───┬──────┘
            │          │         │
            ▼          ▼         ▼
      [Eureka Server] [Config Server] [Databases]
        (Port 8761)   (Port 8888)    PostgreSQL
                                     MongoDB
                                     MySQL
```

---

## Performance Metrics

### Expected Response Times

- **Zone CRUD**: < 200ms
- **Sensor Fetch**: < 500ms (due to external API)
- **Automation Processing**: < 300ms
- **Crop Operations**: < 200ms
- **Service-to-Service Calls**: < 100ms (via Eureka load balancing)

---

## Deployment Checklist

- [x] Eureka Server running (8761)
- [x] Config Server running (8888) with file-based config
- [x] API Gateway running (8080) with JWT filter
- [x] Zone Service running (8081)
- [x] Sensor Telemetry Service running (8082)
- [x] Automation Control Service running (8083)
- [x] Crop Inventory Service running (8084)
- [ ] All services registered in Eureka (green)
- [ ] Postman collection tests passing (100%)
- [ ] Eureka dashboard screenshot captured
- [ ] README documentation reviewed

---

## Troubleshooting

See README.md for detailed troubleshooting guide

---

**Last Updated**: April 9, 2026
**Status**: Ready for Submission
