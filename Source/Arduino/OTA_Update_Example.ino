/**
 * ESP32/ESP8266 OTA Firmware Update Example
 * Kết nối với MQTT và nhận lệnh OTA update từ server
 */

#include <WiFi.h>
#include <PubSubClient.h>
#include <HTTPClient.h>
#include <Update.h>
#include <ArduinoJson.h>
#include <WiFiClientSecure.h>

// WiFi credentials
const char* ssid = "Wokwi-GUEST";
const char* password = "";

// MQTT Broker settings
const char* mqtt_broker = "64742fa6b2a84cae8798c53987ca994e.s1.eu.hivemq.cloud";
const int mqtt_port = 8883;
const char* mqtt_username = "phongdo";
const char* mqtt_password = "Phonggda123";

// Device information
String deviceId = "DEVICE_001";
String currentFirmwareVersion = "1.0.0";
int currentVersionNumber = 1;

// MQTT Topics
String otaTopic = "iot/device/" + deviceId + "/ota";
String dataTopic = "iot/data";
String statusTopic = "iot/status";

WiFiClientSecure espClient;
PubSubClient client(espClient);

// OTA Update variables
int otaProgress = 0;
bool otaInProgress = false;
bool otaCancelled = false;

void setup() {
  Serial.begin(115200);
  
  // Connect to WiFi
  WiFi.begin(ssid, password);
  Serial.print("Connecting to WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi connected!");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());

  // Setup MQTT
  espClient.setInsecure(); // For testing only, use proper certificate in production
  client.setServer(mqtt_broker, mqtt_port);
  client.setCallback(mqttCallback);
  client.setBufferSize(2048); // Increase buffer for larger messages
  
  connectToMQTT();
  
  Serial.println("Device ready!");
  Serial.print("Current Firmware Version: ");
  Serial.println(currentFirmwareVersion);
}

void loop() {
  if (!client.connected()) {
    connectToMQTT();
  }
  client.loop();
  
  // Your normal sensor reading code here
  // ...
  
  delay(100);
}

/**
 * Connect to MQTT Broker
 */
void connectToMQTT() {
  while (!client.connected()) {
    String clientId = "ESP32_" + deviceId;
    Serial.print("Connecting to MQTT broker...");
    
    if (client.connect(clientId.c_str(), mqtt_username, mqtt_password)) {
      Serial.println("Connected!");
      
      // Subscribe to OTA topic
      client.subscribe(otaTopic.c_str());
      Serial.println("Subscribed to: " + otaTopic);
      
      // Send device status
      sendDeviceStatus("ONLINE");
    } else {
      Serial.print("Failed, rc=");
      Serial.print(client.state());
      Serial.println(" Retrying in 5 seconds...");
      delay(5000);
    }
  }
}

/**
 * MQTT Callback - Nhận message từ broker
 */
void mqttCallback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived on topic: ");
  Serial.println(topic);
  
  String message = "";
  for (int i = 0; i < length; i++) {
    message += (char)payload[i];
  }
  Serial.println("Message: " + message);
  
  // Check if it's OTA command
  if (String(topic) == otaTopic) {
    handleOTACommand(message);
  }
}

/**
 * Xử lý lệnh OTA từ server
 */
void handleOTACommand(String jsonMessage) {
  StaticJsonDocument<512> doc;
  DeserializationError error = deserializeJson(doc, jsonMessage);
  
  if (error) {
    Serial.print("JSON parse error: ");
    Serial.println(error.c_str());
    return;
  }
  
  String command = doc["command"];
  
  if (command == "OTA_UPDATE") {
    String version = doc["version"];
    int versionNumber = doc["versionNumber"];
    String downloadUrl = doc["downloadUrl"];
    
    Serial.println("=== OTA UPDATE COMMAND RECEIVED ===");
    Serial.println("Target Version: " + version);
    Serial.println("Download URL: " + downloadUrl);
    
    // Check if update is needed
    if (versionNumber <= currentVersionNumber) {
      Serial.println("Already on this version or newer. Skipping update.");
      notifyServerStatus("SKIPPED", 0);
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
      notifyServerStatus("CANCELLED", otaProgress);
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
  notifyServerStatus("IN_PROGRESS", 0);
  
  HTTPClient http;
  http.begin(url);
  http.setTimeout(30000); // 30 seconds timeout
  http.setFollowRedirects(HTTPC_STRICT_FOLLOW_REDIRECTS); // Follow redirects for Cloudinary
  
  int httpCode = http.GET();
  
  if (httpCode == HTTP_CODE_OK) {
    int contentLength = http.getSize();
    
    // Validate content length
    if (contentLength <= 0) {
      Serial.println("Invalid firmware file size!");
      notifyServerStatus("FAILED", 0);
      http.end();
      otaInProgress = false;
      return;
    }
    
    Serial.printf("Firmware size: %d bytes\n", contentLength);
    
    // Verify it's a binary file by checking content type
    String contentType = http.header("Content-Type");
    Serial.println("Content-Type: " + contentType);
    
    bool canBegin = Update.begin(contentLength, U_FLASH);
    
    if (canBegin) {
      Serial.println("Begin OTA. This may take a while...");
      Serial.printf("Free space: %d bytes\n", ESP.getFreeSketchSpace());
      
      WiFiClient * stream = http.getStreamPtr();
      size_t written = 0;
      uint8_t buff[128] = { 0 };
      unsigned long lastMqttKeepAlive = millis();
      
      while (http.connected() && (written < contentLength) && !otaCancelled) {
        // Keep MQTT connection alive during OTA
        if (millis() - lastMqttKeepAlive > 100) {
          client.loop();
          lastMqttKeepAlive = millis();
        }
        
        size_t available = stream->available();
        
        if (available) {
          int c = stream->readBytes(buff, min(available, sizeof(buff)));
          written += Update.write(buff, c);
          
          // Update progress
          int progress = (written * 100) / contentLength;
          if (progress != otaProgress && progress % 10 == 0) {
            otaProgress = progress;
            Serial.printf("Progress: %d%%\n", progress);
            notifyServerStatus("IN_PROGRESS", progress);
          }
        }
        delay(1);
      }
      
      // Check if cancelled
      if (otaCancelled) {
        Serial.println("OTA update cancelled by user!");
        Update.abort();
        notifyServerStatus("CANCELLED", otaProgress);
      }
      else if (Update.end()) {
        if (Update.isFinished()) {
          Serial.println("OTA update completed successfully!");
          notifyServerStatus("COMPLETED", 100);
          delay(1000);
          
          Serial.println("Rebooting...");
          ESP.restart();
        } else {
          Serial.println("OTA update not finished. Something went wrong!");
          notifyServerStatus("FAILED", otaProgress);
        }
      } else {
        int error = Update.getError();
        Serial.printf("Error Occurred. Error #: %d\n", error);
        
        // Print detailed error message
        switch(error) {
          case UPDATE_ERROR_WRITE:
            Serial.println("Flash write failed");
            break;
          case UPDATE_ERROR_ERASE:
            Serial.println("Flash erase failed");
            break;
          case UPDATE_ERROR_READ:
            Serial.println("Flash read failed");
            break;
          case UPDATE_ERROR_SPACE:
            Serial.println("Not enough space");
            break;
          case UPDATE_ERROR_SIZE:
            Serial.println("Bad size given");
            break;
          case UPDATE_ERROR_STREAM:
            Serial.println("Stream read timeout");
            break;
          case UPDATE_ERROR_MD5:
            Serial.println("MD5 check failed");
            break;
          case UPDATE_ERROR_MAGIC_BYTE:
            Serial.println("Wrong magic byte (not a valid firmware file)");
            break;
          case UPDATE_ERROR_ACTIVATE:
            Serial.println("Could not activate the firmware");
            break;
          case UPDATE_ERROR_NO_PARTITION:
            Serial.println("Partition could not be found");
            break;
          case UPDATE_ERROR_BAD_ARGUMENT:
            Serial.println("Bad argument");
            break;
          case UPDATE_ERROR_ABORT:
            Serial.println("Aborted");
            break;
          default:
            Serial.println("Unknown error");
        }
        
        notifyServerStatus("FAILED", otaProgress);
      }
    } else {
      Serial.println("Not enough space to begin OTA");
      notifyServerStatus("FAILED", 0);
    }
  } else {
    Serial.printf("HTTP error code: %d\n", httpCode);
    notifyServerStatus("FAILED", 0);
  }
  
  http.end();
  otaInProgress = false;
  otaCancelled = false;
}

/**
 * Thông báo trạng thái OTA cho server
 */
void notifyServerStatus(String status, int progress) {
  StaticJsonDocument<256> doc;
  doc["deviceId"] = deviceId;
  doc["status"] = status;
  doc["progress"] = progress;
  doc["currentVersion"] = currentFirmwareVersion;
  doc["timestamp"] = millis();
  
  String output;
  serializeJson(doc, output);
  
  // Gửi qua MQTT
  client.publish(statusTopic.c_str(), output.c_str());
  
  Serial.println("Status sent: " + output);
}

/**
 * Gửi trạng thái thiết bị
 */
void sendDeviceStatus(String status) {
  StaticJsonDocument<256> doc;
  doc["deviceId"] = deviceId;
  doc["status"] = status;
  doc["firmwareVersion"] = currentFirmwareVersion;
  doc["timestamp"] = millis();
  
  String output;
  serializeJson(doc, output);
  
  client.publish(statusTopic.c_str(), output.c_str());
}
