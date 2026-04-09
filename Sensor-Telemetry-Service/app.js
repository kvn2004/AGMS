const express = require('express');
const mongoose = require('mongoose');
const axios = require('axios');
const dotenv = require('dotenv');
const Eureka = require('eureka-js-client').Eureka;

dotenv.config();

const app = express();
app.use(express.json());

const IOT_BASE = process.env.IOT_BASE_URL;
let accessToken = '';
let refreshToken = '';

const IOT_USERNAME = process.env.IOT_USERNAME;
const IOT_PASSWORD = process.env.IOT_PASSWORD;

// ==================== MongoDB Model ====================
const telemetrySchema = new mongoose.Schema({
  deviceId: String,
  zoneId: String,
  temperature: Number,
  humidity: Number,
  capturedAt: Date,
  fetchedAt: { type: Date, default: Date.now }
});

const Telemetry = mongoose.model('Telemetry', telemetrySchema);

// ==================== IoT Auth Functions ====================
async function loginToIoT() {
  try {
    const response = await axios.post(`${IOT_BASE}/auth/login`, {
      username: IOT_USERNAME,
      password: IOT_PASSWORD
    });

    accessToken = response.data.accessToken;
    refreshToken = response.data.refreshToken;
    console.log('✅ IoT Login successful');
    return true;
  } catch (error) {
    if (error.response && error.response.status === 401) {
      console.log('Trying to register user...');
      try {
        await axios.post(`${IOT_BASE}/auth/register`, {
          username: IOT_USERNAME,
          password: IOT_PASSWORD
        });
        console.log('✅ User registered successfully');
        return await loginToIoT(); // login again after register
      } catch (regError) {
        console.error('Register failed (user may already exist):', regError.message);
      }
    }
    console.error('❌ IoT Login failed:', error.message);
    return false;
  }
}

async function refreshAccessToken() {
  if (!refreshToken) return false;
  try {
    const response = await axios.post(`${IOT_BASE}/auth/refresh`, { refreshToken });
    accessToken = response.data.accessToken;
    if (response.data.refreshToken) refreshToken = response.data.refreshToken;
    console.log('✅ Token refreshed');
    return true;
  } catch (error) {
    console.error('❌ Token refresh failed, re-login...');
    return await loginToIoT();
  }
}

// ==================== Fetch & Push Function ====================
async function fetchAndPushTelemetry() {
  if (!accessToken) {
    const loggedIn = await loginToIoT();
    if (!loggedIn) return;
  }

  try {
    // Get all devices
    const devicesRes = await axios.get(`${IOT_BASE}/devices`, {
      headers: { Authorization: `Bearer ${accessToken}` }
    });

    const devices = devicesRes.data;

    for (const device of devices) {
      try {
        // Get telemetry for this device
        const telemetryRes = await axios.get(`${IOT_BASE}/devices/telemetry/${device.deviceId}`, {
          headers: { Authorization: `Bearer ${accessToken}` }
        });

        const telemetry = telemetryRes.data;

        // Save to MongoDB
        await new Telemetry({
          deviceId: telemetry.deviceId,
          zoneId: telemetry.zoneId,
          temperature: telemetry.value.temperature,
          humidity: telemetry.value.humidity,
          capturedAt: new Date(telemetry.capturedAt)
        }).save();

        // Push to Automation Service
        const automationUrl = process.env.AUTOMATION_SERVICE_URL;
        await axios.post(`${automationUrl}/api/automation/process`, telemetry);

        console.log(`✅ Pushed telemetry for device: ${device.deviceId} | Temp: ${telemetry.value.temperature}°C`);
      } catch (devErr) {
        console.error(`Telemetry error for device ${device.deviceId}:`, devErr.message);
      }
    }
  } catch (error) {
    if (error.response && error.response.status === 401) {
      console.log('Token expired → refreshing...');
      const refreshed = await refreshAccessToken();
      if (refreshed) fetchAndPushTelemetry(); // retry once
    } else {
      console.error('Fetch error:', error.message);
    }
  }
}

// ==================== Scheduler (every 10 seconds) ====================
setInterval(() => {
  fetchAndPushTelemetry();
}, 10000);

// ==================== Debug API ====================
app.get('/api/sensors/latest', async (req, res) => {
  try {
    const latest = await Telemetry.findOne().sort({ fetchedAt: -1 }).lean();
    res.json(latest || { message: 'No telemetry data yet' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ==================== MongoDB Connection ====================
mongoose.connect(process.env.MONGO_URI)
  .then(() => console.log('✅ MongoDB connected'))
  .catch(err => console.error('❌ MongoDB error:', err));

// ==================== Eureka Registration ====================
const eurekaClient = new Eureka({
  instance: {
    app: 'sensor-telemetry-service',
    instanceId: `sensor-telemetry-service:${process.env.PORT}`,
    hostName: 'localhost',           // ← මේක important
    ipAddr: '127.0.0.1',
    port: { '$': process.env.PORT, '@enabled': true },
    vipAddress: 'sensor-telemetry-service',
    dataCenterInfo: { '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo', name: 'MyOwn' }
  },
  eureka: {
    host: process.env.EUREKA_HOST,
    port: process.env.EUREKA_PORT,
    servicePath: '/eureka/apps/',
    registerWithEureka: true,
    fetchRegistry: true,
    preferIpAddress: true
  }
});
// ==================== Start Server ====================
const PORT = process.env.PORT || 8082;
app.listen(PORT, () => {
  console.log(`🚀 Sensor Telemetry Service running on port ${PORT}`);
  eurekaClient.start();           // Register with Eureka
  loginToIoT().then(() => {
    setTimeout(fetchAndPushTelemetry, 3000); // first fetch after 3 seconds
  });
});