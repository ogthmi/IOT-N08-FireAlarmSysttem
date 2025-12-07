#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <DHT.h>
#include <Arduino_JSON.h>
#include <WiFiManager.h>
#include <HTTPClient.h>
#include <Update.h>
#include "time.h"

// =========================================================
// 1)  CẤU HÌNH CẢM BIẾN & ĐỊNH NGHĨA CHÂN
// =========================================================
#define DHTPIN 5
#define DHTTYPE DHT22
#define BUZZER_PIN 4
#define FIRE_SENSOR_PIN 34
#define MP2_SENSOR_PIN 35
#define PUMP_PIN 18

DHT dht(DHTPIN, DHTTYPE);

// =========================================================
// 2) NGƯỠNG CẢNH BÁO
// =========================================================
const int DEFAULT_FIRE_THRESHOLD = 1500;
const int DEFAULT_SMOKE_THRESHOLD = 3000;
int fireThreshold = DEFAULT_FIRE_THRESHOLD;
int smokeThreshold = DEFAULT_SMOKE_THRESHOLD;

// =========================================================
// 3) MQTT CẤU HÌNH
// =========================================================
#define MQTT_BROKER     "64742fa6b2a84cae8798c53987ca994e.s1.eu.hivemq.cloud"
#define MQTT_PORT       8883
#define MQTT_USER       "phongdo"
#define MQTT_PASSWORD   "Phonggda123"

WiFiClientSecure wifiClient;
PubSubClient mqttClient(wifiClient);

// =========================================================
// 4) NTP TIME
// =========================================================
const char* ntpServer = "pool.ntp.org";
const long  gmtOffset_sec = 7 * 3600;
const int   daylightOffset_sec = 0;

// =========================================================
// 5) FIRMWARE & OTA VARIABLES
// =========================================================
String currentFirmwareVersion = "1.0.0";
int currentVersionNumber = 1;

int otaProgress = 0;
bool otaInProgress = false;
bool otaCancelled = false;

// =========================================================
// 6) HỆ THỐNG BIẾN
// =========================================================
unsigned long lastSendTime = 0;
const unsigned long sendInterval = 2000;

unsigned long alarmPreviousMillis = 0;
const unsigned long alarmInterval = 500;
bool alarmState = false;

bool pumpState = false;
bool buzzerState = false;
bool pumpManualOff = false;
bool pumpManualMode = false;
bool buzzerManual = false;

float t = 0.0;
float h = 0.0;

// =========================================================
// 7) HÀM PHỤ TRỢ
// =========================================================
String getDeviceID() {
  uint64_t chipid = ESP.getEfuseMac();
  char idStr[20];
  snprintf(idStr, sizeof(idStr), "%04X%08X",
           (uint16_t)(chipid >> 32), (uint32_t)chipid);
  return String(idStr);
}
String deviceID = getDeviceID();

String getFormattedTime() {
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) return "N/A";
  char buf[30];
  strftime(buf, sizeof(buf), "%Y-%m-%d %H:%M:%S", &timeinfo);
  return String(buf);
}

// =========================================================
// 8) CÒI BÁO
// =========================================================
void alarmControl(bool fire, bool smoke) {
  if (buzzerManual) return;

  unsigned long currentMillis = millis();
  if (fire) {
    digitalWrite(BUZZER_PIN, HIGH);
    buzzerState = true;
  }
  else if (smoke) {
    if (currentMillis - alarmPreviousMillis >= alarmInterval) {
      alarmPreviousMillis = currentMillis;
      alarmState = !alarmState;
      digitalWrite(BUZZER_PIN, alarmState ? HIGH : LOW);
      buzzerState = alarmState;
    }
  }
  else {
    digitalWrite(BUZZER_PIN, LOW);
    buzzerState = false;
    alarmState = false;
  }
}

// =========================================================
// 9) ĐỌC DHT22
// =========================================================
void readDHT() {
  static unsigned long lastDHTRead = 0;
  if (millis() - lastDHTRead >= 2000) {
    lastDHTRead = millis();
    float _h = dht.readHumidity();
    float _t = dht.readTemperature();

    if (!isnan(_h)) h = _h;
    if (!isnan(_t)) t = _t;
  }
}

// =========================================================
// 10) MQTT CALLBACK
// =========================================================
void mqttCallback(char* topic, byte* payload, unsigned int length) {
  String msg = "";
  for (unsigned int i = 0; i < length; i++) msg += (char)payload[i];

  Serial.printf("[MQTT] Received from topic: %s - %s\n", topic, msg.c_str());

  String otaTopic = "iot/device/" + deviceID + "/ota";
  if (String(topic) == otaTopic) {
    handleOTACommand(msg);
    return;
  }

  // Parse JSON
  JSONVar doc = JSON.parse(msg);
  if (JSON.typeof(doc) == "undefined") {
    Serial.printf("[MQTT] ERROR: Invalid JSON\n");
    return;
  }

  String dev = (const char*) doc["device_id"];
  if (dev != deviceID) return;

  if (String(topic) == "iot/control/pump") {
    String state = (const char*) doc["state"];
    state.toLowerCase();
    pumpManualMode = true;

    if (state == "on") {
      digitalWrite(PUMP_PIN, HIGH);
      pumpState = true;
      pumpManualOff = false;
    } else if (state == "off") {
      digitalWrite(PUMP_PIN, LOW);
      pumpState = false;
      pumpManualOff = true;
    }

    Serial.printf("[MQTT] Pump command: %s\n", state.c_str());
  }
  else if (String(topic) == "iot/control/buzzer") {
    String state = (const char*) doc["state"];
    state.toLowerCase();
    buzzerManual = true;

    if (state == "on") {
      digitalWrite(BUZZER_PIN, HIGH);
      buzzerState = true;
    } else if (state == "off") {
      digitalWrite(BUZZER_PIN, LOW);
      buzzerState = false;
    }

    Serial.printf("[MQTT] Buzzer command: %s\n", state.c_str());
  }
  else if (String(topic) == "iot/control/threshold") {
    int fth = (int) doc["fire_threshold"];
    int sth = (int) doc["smoke_threshold"];

    if (fth == 0 || sth == 0) {
      fireThreshold = DEFAULT_FIRE_THRESHOLD;
      smokeThreshold = DEFAULT_SMOKE_THRESHOLD;
    } else {
      fireThreshold = fth;
      smokeThreshold = sth;
    }

    Serial.printf("[MQTT] Threshold updated: Fire=%d Smoke=%d\n", fireThreshold, smokeThreshold);
  }
}


// =========================================================
// 11) MQTT RECONNECT
// =========================================================
void reconnectMQTT(const String& deviceID) {
  while (!mqttClient.connected()) {
    Serial.print("[MQTT] Connecting...");
    String clientId = "ESP32-" + deviceID;
    if (mqttClient.connect(clientId.c_str(), MQTT_USER, MQTT_PASSWORD)) {
      Serial.println("[MQTT] Connected");
      mqttClient.subscribe("iot/control/buzzer", 1);
      mqttClient.subscribe("iot/control/pump", 1);
      mqttClient.subscribe("iot/control/threshold", 1);

      String otaTopic = "iot/device/" + deviceID + "/ota";
      mqttClient.subscribe(otaTopic.c_str(), 1);
      Serial.println("[MQTT] Subscribed to control and OTA topics");
    } else {
      Serial.printf("[MQTT] Connection failed, rc=%d\n", mqttClient.state());
      delay(3000);
    }
  }
}

// =========================================================
// 12) GỬI DỮ LIỆU SENSOR MQTT
// =========================================================
void sendSensorDataMQTT(float h, float t, int fireVal, int smokeVal,
                        bool fire, bool smoke,
                        bool pumpState, bool buzzerState,
                        const String& id){
  JSONVar json;
  json["deviceId"] = deviceID;

  JSONVar telemetries = JSON.parse("[]");

  JSONVar objTemp; objTemp["name"]="DHT22T"; objTemp["value"]=String(t,1);
  telemetries[telemetries.length()] = objTemp;

  JSONVar objHum; objHum["name"]="DHT22H"; objHum["value"]=String(h,1);
  telemetries[telemetries.length()] = objHum;

  JSONVar objSmoke; objSmoke["name"]="MP2"; objSmoke["value"]=smokeVal; objSmoke["status"]=smoke?"true":"false";
  telemetries[telemetries.length()] = objSmoke;

  JSONVar objFire; objFire["name"]="MHS"; objFire["value"]=fireVal; objFire["status"]=fire?"true":"false";
  telemetries[telemetries.length()] = objFire;

  JSONVar objPump; objPump["name"]="PUMP"; objPump["status"]=pumpState?"true":"false";
  telemetries[telemetries.length()] = objPump;

  JSONVar objBuzzer; objBuzzer["name"]="BUZZER"; objBuzzer["status"]=buzzerState?"true":"false";
  telemetries[telemetries.length()] = objBuzzer;

  json["telemetries"] = telemetries;
  mqttClient.publish("iot/data", JSON.stringify(json).c_str(), true);
}

// =========================================================
// 13) OTA COMMAND HANDLER
// =========================================================
void handleOTACommand(String jsonMessage) {
  JSONVar doc = JSON.parse(jsonMessage);
  if (JSON.typeof(doc) == "undefined") {
    Serial.println("[OTAU] ERROR: OTA JSON parse error");
    return;
  }
  
  String command = (const char*)doc["command"];
  
  if (command == "OTA_UPDATE") {
    String version = (const char*)doc["version"];
    int versionNumber = (int)doc["versionNumber"];
    String downloadUrl = (const char*)doc["downloadUrl"];
    
    Serial.println("[OTAU] === OTA UPDATE COMMAND RECEIVED ===");
    Serial.println("[OTAU] Target Version: " + version);
    Serial.println("[OTAU] Download URL: " + downloadUrl);
  
    performOTAUpdate(downloadUrl, version);
  }
  else if (command == "OTA_CANCEL") {
    if (otaInProgress) {
      otaCancelled = true;
      Serial.println("[OTAU] OTA CANCEL command received");
      notifyOTAStatus("CANCELLED", otaProgress);
    }
  }
}

// =========================================================
// 14) OTA UPDATE
// =========================================================
void performOTAUpdate(String url, String targetVersion) {
  if (otaInProgress) {
    Serial.println("[OTAU] Update already running — ignored");
    return;
  }
  
  otaInProgress = true;
  otaProgress = 0;
  otaCancelled = false;

  Serial.println("\n[OTAU] =====================================");
  Serial.println("[OTAU] OTA UPDATE START");
  Serial.println("[OTAU] URL: " + url);
  Serial.println("[OTAU] Target FW: " + targetVersion);
  Serial.println("[OTAU] =====================================\n");

  notifyOTAStatus("IN_PROGRESS", 0);

  HTTPClient http;
  http.begin(url);
  http.setTimeout(30000);
  http.setFollowRedirects(HTTPC_STRICT_FOLLOW_REDIRECTS);
  
  int httpCode = http.GET();
  Serial.printf("[OTAU] HTTP GET response: %d\n", httpCode);
  
  if (httpCode == HTTP_CODE_OK) {
    int contentLength = http.getSize();
    Serial.printf("[OTAU] Firmware size: %d bytes\n", contentLength);

    if (contentLength <= 0) {
      Serial.println("[OTAU] ERROR: Invalid firmware size!");
      notifyOTAStatus("FAILED", 0);
      http.end();
      otaInProgress = false;
      return;
    }
    
    bool canBegin = Update.begin(contentLength, U_FLASH);

    if (!canBegin) {
      Serial.println("[OTAU] ERROR: Update.begin() failed!");
      notifyOTAStatus("FAILED", 0);
      http.end();
      otaInProgress = false;
      return;
    }

    Serial.println("[OTAU] Writing firmware...");

    WiFiClient * stream = http.getStreamPtr();
    size_t written = 0;
    uint8_t buff[512] = { 0 };
    unsigned long lastMqttKeepAlive = millis();
    
    while (http.connected() && (written < contentLength) && !otaCancelled) {

      if (millis() - lastMqttKeepAlive > 100) {
        mqttClient.loop();
        lastMqttKeepAlive = millis();
      }

      size_t available = stream->available();
      
      if (available) {
        int c = stream->readBytes(buff, min(available, sizeof(buff)));
        written += Update.write(buff, c);

        int progress = (written * 100) / contentLength;

        if (progress != otaProgress && progress % 10 == 0) {
          otaProgress = progress;

          Serial.printf("[OTAU] Progress: %d%% (%d / %d bytes)\n",
                        progress, (int)written, contentLength);

          notifyOTAStatus("IN_PROGRESS", progress);
        }
      }
      delay(1);
    }

    if (otaCancelled) {
      Serial.println("[OTAU] OTA CANCELLED by server");
      Update.abort();
      notifyOTAStatus("CANCELLED", otaProgress);
    }
    else if (Update.end()) {
      if (Update.isFinished()) {
        Serial.println("[OTAU] OTA COMPLETED SUCCESSFULLY");
        notifyOTAStatus("COMPLETED", 100);

        Serial.println("[OTAU] Sending MQTT confirmation...");
        for (int i = 0; i < 50; i++) {
          mqttClient.loop();
          delay(100);
        }

        Serial.println("[OTAU] Rebooting ESP32...");
        ESP.restart();
      } 
      else {
        Serial.println("[OTAU] ERROR: Update did not finish");
        notifyOTAStatus("FAILED", otaProgress);
      }
    } else {
      Serial.println("[OTAU] ERROR: Update.end() failed");
      notifyOTAStatus("FAILED", otaProgress);
    }
  }
  else {
    Serial.println("[OTAU] ERROR: HTTP request failed");
    notifyOTAStatus("FAILED", 0);
  }
  
  http.end();
  otaInProgress = false;
  otaCancelled = false;

  Serial.println("[OTAU] OTA UPDATE END\n");
}

// =========================================================
// 15) GỬI OTA STATUS
// =========================================================
void notifyOTAStatus(String status, int progress) {
  JSONVar json;
  json["deviceId"] = getDeviceID();
  json["status"] = status;
  json["progress"] = progress;
  json["currentVersion"] = currentFirmwareVersion;
  json["timestamp"] = millis();

  mqttClient.publish("iot/ota_status", JSON.stringify(json).c_str());
}

// =========================================================
// 16) SETUP
// =========================================================
void setup() {
  Serial.begin(115200);
  Serial.println("[INIT] Starting setup...");

  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(PUMP_PIN, OUTPUT);
  digitalWrite(BUZZER_PIN, LOW);
  digitalWrite(PUMP_PIN, LOW);
  Serial.println("[INIT] Pins initialized");

  WiFiManager wm;
  if(!wm.autoConnect("FireAlarm-AP")) {
    Serial.println("[INIT] Created Access Point for WiFi configuration");
  } else {
    Serial.println("[INIT] WiFi connected!");
  }

  wifiClient.setInsecure();
  mqttClient.setServer(MQTT_BROKER, MQTT_PORT);
  mqttClient.setCallback(mqttCallback);
  mqttClient.setBufferSize(2048);
  Serial.println("[INIT] MQTT client configured");

  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  struct tm timeinfo;
  while (!getLocalTime(&timeinfo)) {
    delay(500);
    Serial.println("[INIT] Waiting for NTP time...");
  }
  Serial.println("[INIT] NTP time synchronized");

  dht.begin();
  Serial.println("[INIT] DHT sensor initialized");
  Serial.println("[INIT] Setup completed\n");
}

// =========================================================
// 17) LOOP
// =========================================================
void loop() {
  String deviceID = getDeviceID();

  if (!mqttClient.connected()) reconnectMQTT(deviceID);
  mqttClient.loop();

  readDHT();
  int fireVal = analogRead(FIRE_SENSOR_PIN);
  int smokeVal = analogRead(MP2_SENSOR_PIN);

  bool fire = (fireVal < fireThreshold);
  bool smoke = (smokeVal > smokeThreshold);

  // Pump auto
  if (fire && !pumpManualOff) {
    if (!pumpState) { digitalWrite(PUMP_PIN, HIGH); pumpState = true; }
  }
  else if (!fire && !pumpManualMode) {
    if (pumpState) { digitalWrite(PUMP_PIN, LOW); pumpState = false; }
  }

  // Buzzer
  alarmControl(fire, smoke);

  //Reset manual flags
  if (!fire && !smoke) {
    if (!pumpState) { pumpManualMode = false; pumpManualOff = false; }
    if (!buzzerState) { buzzerManual = false; }
  }

  // Send data
  unsigned long now = millis();
  if (now - lastSendTime >= sendInterval) {
    lastSendTime = now;

    sendSensorDataMQTT(h, t, fireVal, smokeVal, fire, smoke, pumpState, buzzerState, deviceID);

    Serial.printf(
      "[DATA] T:%.1fC H:%.1f%% | Fire:%d(%s) | Smoke:%d(%s) | Pump:%s | Buzzer:%s | Th(F:%d S:%d)\n",
      t, h,
      fireVal, fire ? "DETECTED" : "OK",
      smokeVal, smoke ? "DETECTED" : "OK",
      pumpState ? "ON" : "OFF",
      buzzerState ? "ON" : "OFF",
      fireThreshold, smokeThreshold
    );
  }

  delay(100);
}