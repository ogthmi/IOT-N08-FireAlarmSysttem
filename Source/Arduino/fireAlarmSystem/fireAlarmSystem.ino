#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <DHT.h>
#include <Arduino_JSON.h>
#include <WiFiManager.h>
#include <HTTPClient.h>
#include <Update.h>
#include "time.h"

// ---------- Cấu hình cảm biến và chân ----------
#define DHTPIN 5
#define DHTTYPE DHT22
#define BUZZER_PIN 4
#define FIRE_SENSOR_PIN 34
#define MP2_SENSOR_PIN 35
#define PUMP_PIN 18

DHT dht(DHTPIN, DHTTYPE);

// ---------- Ngưỡng cảnh báo ----------
const int DEFAULT_FIRE_THRESHOLD = 1500;
const int DEFAULT_SMOKE_THRESHOLD = 3000;
int fireThreshold = DEFAULT_FIRE_THRESHOLD;
int smokeThreshold = DEFAULT_SMOKE_THRESHOLD;

// ---------- MQTT ----------
#define MQTT_BROKER     "64742fa6b2a84cae8798c53987ca994e.s1.eu.hivemq.cloud"
#define MQTT_PORT       8883
#define MQTT_USER       "phongdo"
#define MQTT_PASSWORD   "Phonggda123"


WiFiClientSecure wifiClient;
PubSubClient mqttClient(wifiClient);

// ---------- NTP ----------
const char* ntpServer = "pool.ntp.org";
const long  gmtOffset_sec = 7 * 3600;
const int   daylightOffset_sec = 0;

// ---------- Firmware Version ----------
String currentFirmwareVersion = "1.0.0";
int currentVersionNumber = 1;

// ---------- OTA Update variables ----------
int otaProgress = 0;
bool otaInProgress = false;
bool otaCancelled = false;

// ---------- Biến hệ thống ----------
unsigned long lastSendTime = 0;
const unsigned long sendInterval = 2000;

unsigned long alarmPreviousMillis = 0;
const unsigned long alarmInterval = 500;
bool alarmState = false;

// ---------- Trạng thái hệ thống ----------
bool pumpState = false;
bool buzzerState = false;
bool pumpManualOff = false;
bool pumpManualMode = false;
bool buzzerManual = false;

// ---------- Hàm phụ ----------
String getDeviceID() {
  uint64_t chipid = ESP.getEfuseMac();
  char idStr[20];
  snprintf(idStr, sizeof(idStr), "%04X%08X", 
           (uint16_t)(chipid >> 32), (uint32_t)chipid);
  return String(idStr);
}

String getFormattedTime() {
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) return "N/A";
  char buf[30];
  strftime(buf, sizeof(buf), "%Y-%m-%d %H:%M:%S", &timeinfo);
  return String(buf);
}

// ---------- Còi báo ----------
void alarmControl(bool fire, bool smoke) {
  // Nếu còi đang ở chế độ thủ công thì không can thiệp
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


// ---------- MQTT Callback ----------
void mqttCallback(char* topic, byte* payload, unsigned int length) {
  String msg;
  for (unsigned int i = 0; i < length; i++) msg += (char)payload[i];

  Serial.printf("Nhận từ topic: %s - %s\n", topic, msg.c_str());

  String deviceID = getDeviceID();
  String otaTopic = "iot/device/" + deviceID + "/ota";
  
  // Check if it's OTA command
  if (String(topic) == otaTopic) {
    handleOTACommand(msg);
    return;
  }

  JSONVar doc = JSON.parse(msg);
  if (JSON.typeof(doc) == "undefined") {
    Serial.println("JSON parse error");
    return;
  }

  String dev = (const char*) doc["device_id"];
  if (dev != deviceID) return; // bỏ qua thiết bị khác

  // ---- Pump ----
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
  }

  // ---- Buzzer ----
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
  }

  // ---- Threshold ----
  else if (String(topic) == "iot/control/threshold") {
    int fth = (int) doc["fire_threshold"];
    int sth = (int) doc["smoke_threshold"];

    if (fth == 0 || sth == 0) {
      fireThreshold = DEFAULT_FIRE_THRESHOLD;
      smokeThreshold = DEFAULT_SMOKE_THRESHOLD;
      Serial.println("Ngưỡng reset về mặc định");
    } else {
      fireThreshold = fth;
      smokeThreshold = sth;
      Serial.printf("Set ngưỡng mới: fire=%d, smoke=%d\n", fireThreshold, smokeThreshold);
    }
  }
}

// ---------- MQTT Reconnect ----------
void reconnectMQTT(const String& deviceID) {
  while (!mqttClient.connected()) {
    Serial.print("Connecting MQTT...");
    String clientId = "ESP32-" + deviceID;
    if (mqttClient.connect(clientId.c_str(), MQTT_USER, MQTT_PASSWORD)) {
      Serial.println("MQTT connected");
      mqttClient.subscribe("iot/control/buzzer", 1);
      mqttClient.subscribe("iot/control/pump", 1);
      mqttClient.subscribe("iot/control/threshold", 1);
      
      // Subscribe to OTA topic
      String otaTopic = "iot/device/" + deviceID + "/ota";
      mqttClient.subscribe(otaTopic.c_str(), 1);
      Serial.println("Subscribed to OTA topic: " + otaTopic);
    } else {
      Serial.printf("MQTT failed, rc=%d\n", mqttClient.state());
      delay(3000);
    }
  }
}

// ---------- Gửi dữ liệu cảm biến ----------
void sendSensorDataMQTT(float h, float t, int fireVal, int smokeVal, bool fire, bool smoke, const String& id) {
  JSONVar json;
  json["device_id"] = id;
  json["timestamp"] = getFormattedTime();
  json["temperature"] = t;
  json["humidity"] = h;
  json["fire_value"] = fireVal;
  json["fire_detected"] = fire;
  json["smoke_value"] = smokeVal;
  json["smoke_detected"] = smoke;

  String payload = JSON.stringify(json);
  mqttClient.publish("iot/data", payload.c_str(), true);
}

// ---------- Gửi trạng thái thiết bị + ngưỡng ----------
void sendStatusMQTT(const String& id) {
  JSONVar json;
  json["device_id"] = id;
  json["timestamp"] = getFormattedTime();
  json["pump_state"] = pumpState ? "ON" : "OFF";
  json["buzzer_state"] = buzzerState ? "ON" : "OFF";
  json["fire_threshold"] = fireThreshold;
  json["smoke_threshold"] = smokeThreshold;
  json["firmware_version"] = currentFirmwareVersion;

  String payload = JSON.stringify(json);
  mqttClient.publish("iot/status", payload.c_str(), true);
}

// ========== OTA FIRMWARE UPDATE FUNCTIONS ==========

/**
 * Xử lý lệnh OTA từ server
 */
void handleOTACommand(String jsonMessage) {
  JSONVar doc = JSON.parse(jsonMessage);
  if (JSON.typeof(doc) == "undefined") {
    Serial.println("OTA JSON parse error");
    return;
  }
  
  String command = (const char*)doc["command"];
  
  if (command == "OTA_UPDATE") {
    String version = (const char*)doc["version"];
    int versionNumber = (int)doc["versionNumber"];
    String downloadUrl = (const char*)doc["downloadUrl"];
    
    Serial.println("=== OTA UPDATE COMMAND RECEIVED ===");
    Serial.println("Target Version: " + version);
    Serial.println("Download URL: " + downloadUrl);
    
    // Check if update is needed
    if (versionNumber <= currentVersionNumber) {
      Serial.println("Already on this version or newer. Skipping update.");
      notifyOTAStatus("SKIPPED", 0);
      return;
    }
    
    // Start OTA update
    performOTAUpdate(downloadUrl, version);
  }
  else if (command == "OTA_CANCEL") {
    Serial.println("=== OTA CANCEL COMMAND RECEIVED ===");
    
    if (otaInProgress) {
      otaCancelled = true;
      Serial.println("OTA update will be cancelled...");
      notifyOTAStatus("CANCELLED", otaProgress);
    } else {
      Serial.println("No OTA update in progress to cancel.");
    }
  }
}

/**
 * Thực hiện OTA Update
 */
void performOTAUpdate(String url, String targetVersion) {
  if (otaInProgress) {
    Serial.println("OTA update already in progress!");
    return;
  }
  
  otaInProgress = true;
  otaProgress = 0;
  otaCancelled = false;
  
  Serial.println("Starting OTA update...");
  notifyOTAStatus("IN_PROGRESS", 0);
  
  HTTPClient http;
  http.begin(url);
  http.setTimeout(30000);
  http.setFollowRedirects(HTTPC_STRICT_FOLLOW_REDIRECTS);
  
  int httpCode = http.GET();
  
  if (httpCode == HTTP_CODE_OK) {
    int contentLength = http.getSize();
    
    if (contentLength <= 0) {
      Serial.println("Invalid firmware file size!");
      notifyOTAStatus("FAILED", 0);
      http.end();
      otaInProgress = false;
      return;
    }
    
    Serial.printf("Firmware size: %d bytes\n", contentLength);
    String contentType = http.header("Content-Type");
    Serial.println("Content-Type: " + contentType);
    
    bool canBegin = Update.begin(contentLength, U_FLASH);
    
    if (canBegin) {
      Serial.println("Begin OTA. This may take a while...");
      Serial.printf("Free space: %d bytes\n", ESP.getFreeSketchSpace());
      
      WiFiClient * stream = http.getStreamPtr();
      size_t written = 0;
      uint8_t buff[512] = { 0 };
      unsigned long lastMqttKeepAlive = millis();
      
      while (http.connected() && (written < contentLength) && !otaCancelled) {
        // Keep MQTT alive
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
            Serial.printf("Progress: %d%%\n", progress);
            notifyOTAStatus("IN_PROGRESS", progress);
          }
        }
        delay(1);
      }
      
      if (otaCancelled) {
        Serial.println("OTA update cancelled by user!");
        Update.abort();
        notifyOTAStatus("CANCELLED", otaProgress);
      }
      else if (Update.end()) {
        if (Update.isFinished()) {
          Serial.println("OTA update completed successfully!");
          notifyOTAStatus("COMPLETED", 100);
          delay(1000);
          Serial.println("Rebooting...");
          ESP.restart();
        } else {
          Serial.println("OTA update not finished!");
          notifyOTAStatus("FAILED", otaProgress);
        }
      } else {
        int error = Update.getError();
        Serial.printf("Error #: %d - ", error);
        
        switch(error) {
          case UPDATE_ERROR_WRITE: Serial.println("Flash write failed"); break;
          case UPDATE_ERROR_ERASE: Serial.println("Flash erase failed"); break;
          case UPDATE_ERROR_READ: Serial.println("Flash read failed"); break;
          case UPDATE_ERROR_SPACE: Serial.println("Not enough space"); break;
          case UPDATE_ERROR_SIZE: Serial.println("Bad size"); break;
          case UPDATE_ERROR_STREAM: Serial.println("Stream timeout"); break;
          case UPDATE_ERROR_MD5: Serial.println("MD5 failed"); break;
          case UPDATE_ERROR_MAGIC_BYTE: Serial.println("Invalid firmware file"); break;
          case UPDATE_ERROR_ACTIVATE: Serial.println("Activation failed"); break;
          case UPDATE_ERROR_NO_PARTITION: Serial.println("No partition"); break;
          case UPDATE_ERROR_BAD_ARGUMENT: Serial.println("Bad argument"); break;
          case UPDATE_ERROR_ABORT: Serial.println("Aborted"); break;
          default: Serial.println("Unknown error");
        }
        
        notifyOTAStatus("FAILED", otaProgress);
      }
    } else {
      Serial.println("Not enough space for OTA");
      notifyOTAStatus("FAILED", 0);
    }
  } else {
    Serial.printf("HTTP error: %d\n", httpCode);
    notifyOTAStatus("FAILED", 0);
  }
  
  http.end();
  otaInProgress = false;
  otaCancelled = false;
}

/**
 * Thông báo trạng thái OTA cho server
 */
void notifyOTAStatus(String status, int progress) {
  JSONVar json;
  json["deviceId"] = getDeviceID();
  json["status"] = status;
  json["progress"] = progress;
  json["currentVersion"] = currentFirmwareVersion;
  json["timestamp"] = millis();
  
  String payload = JSON.stringify(json);
  mqttClient.publish("iot/status", payload.c_str());
  
  Serial.println("OTA Status sent: " + payload);
}

// ========== END OTA FUNCTIONS ==========

// ---------- Setup ----------
void setup() {
  Serial.begin(115200);
  dht.begin();
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(PUMP_PIN, OUTPUT);
  digitalWrite(BUZZER_PIN, LOW);
  digitalWrite(PUMP_PIN, LOW);

  // ---------- WiFi Manager ----------
  WiFiManager wm;
  if(!wm.autoConnect("FireAlarm-AP")) {
    Serial.println("Tạo Access Point FireAlarm-AP để cấu hình WiFi...");
  } else {
    Serial.println("WiFi connected!");
  }

  wifiClient.setInsecure();
  mqttClient.setServer(MQTT_BROKER, MQTT_PORT);
  mqttClient.setCallback(mqttCallback);
  mqttClient.setBufferSize(2048); // Increase for OTA messages

  // ---------- NTP ----------
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  Serial.println("Syncing time...");
  struct tm timeinfo;
  while (!getLocalTime(&timeinfo)) {
    Serial.print(".");
    delay(500);
  }
  Serial.println("\nTime OK!");
  
  Serial.println("==========================================");
  Serial.println("Fire Alarm System with OTA Update");
  Serial.print("Firmware Version: ");
  Serial.println(currentFirmwareVersion);
  Serial.print("Device ID: ");
  Serial.println(getDeviceID());
  Serial.println("==========================================");
}

// ---------- Loop ----------
void loop() {
  String deviceID = getDeviceID();

  if (!mqttClient.connected()) reconnectMQTT(deviceID);
  mqttClient.loop();

  float h = dht.readHumidity();
  float t = dht.readTemperature();
  int fireVal = analogRead(FIRE_SENSOR_PIN);
  int smokeVal = analogRead(MP2_SENSOR_PIN);

  bool fire = (fireVal < fireThreshold);
  bool smoke = (smokeVal > smokeThreshold);

  // --- Máy bơm tự động ---
  if (fire && !pumpManualOff) {
    if (!pumpState) {
      digitalWrite(PUMP_PIN, HIGH);
      pumpState = true;
    }
  } 
  else if (!fire && !pumpManualMode) {
    if (pumpState) {
      digitalWrite(PUMP_PIN, LOW);
      pumpState = false;
    }
  }

  // --- Còi ---
  alarmControl(fire, smoke);

  // --- Reset manual flags khi hết cháy và khói ---
  // --- Reset manual flags khi hết cháy và khói ---
  if (!fire && !smoke) {
    // Nếu bơm đang tắt thì trả quyền điều khiển lại cho hệ thống
    if (!pumpState) {
      pumpManualMode = false;
      pumpManualOff = false;
    }

    // Nếu còi đang tắt thì trả quyền điều khiển lại cho hệ thống
    if (!buzzerState) {
      buzzerManual = false;
    }
  }

  // --- Gửi dữ liệu định kỳ ---
  unsigned long now = millis();
  if (now - lastSendTime >= sendInterval) {
    lastSendTime = now;

    sendSensorDataMQTT(h, t, fireVal, smokeVal, fire, smoke, deviceID);
    sendStatusMQTT(deviceID);

    Serial.printf("Temp: %.1fC, Hum: %.1f%%, Fire: %d (%s), Smoke: %d (%s)\n",
                  t, h, fireVal, fire?"YES":"NO", smokeVal, smoke?"YES":"NO");
    Serial.printf("Pump: %s, Buzzer: %s, FireTh: %d, SmokeTh: %d\n",
                  pumpState?"ON":"OFF", buzzerState?"ON":"OFF", fireThreshold, smokeThreshold);
    Serial.println("-------------------------------");
  }

  delay(100);
}
