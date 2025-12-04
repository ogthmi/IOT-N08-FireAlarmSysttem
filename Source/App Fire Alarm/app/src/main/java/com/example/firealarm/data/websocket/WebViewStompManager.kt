package com.example.firealarm.data.websocket

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.firealarm.data.model.telemetry.TelemetryResponse
import com.example.firealarm.data.model.auth.toDomain
import com.example.firealarm.data.model.telemetry.toDomain
import com.example.firealarm.domain.model.Telemetry
import com.example.firealarm.presentation.utils.AppPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebViewStompManager @Inject constructor(
    private val gson: Gson,
    private val context: Context
) {
    private val TAG = "WebViewStompManager"
    private var webView: WebView? = null
    private var isConnected = false
    private val baseUrl = "http://26.136.76.251:8080"
    private val wsPath = "/ws"
    
    private var telemetryCallback: ((List<Telemetry>) -> Unit)? = null
    private var notificationCallback: ((String) -> Unit)? = null
    private var connectionStatusCallback: ((Boolean) -> Unit)? = null
    
    // JavaScript Interface để nhận callbacks từ JavaScript
    inner class AndroidJSInterface {
        @JavascriptInterface
        fun onConnected() {
            Log.d(TAG, "WebSocket Connected via JavaScript")
            isConnected = true
            connectionStatusCallback?.invoke(true)
        }
        
        @JavascriptInterface
        fun onDisconnected() {
            Log.d(TAG, "WebSocket Disconnected via JavaScript")
            isConnected = false
            connectionStatusCallback?.invoke(false)
        }
        
        @JavascriptInterface
        fun onError(error: String) {
            Log.e(TAG, "WebSocket Error via JavaScript: $error")
            isConnected = false
            connectionStatusCallback?.invoke(false)
        }
        
        @JavascriptInterface
        fun onTelemetryReceived(json: String) {
            try {
                Log.d(TAG, "Received telemetry: $json")
                val telemetryList = parseTelemetry(json)
                telemetryCallback?.invoke(telemetryList)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing telemetry: ${e.message}", e)
            }
        }
        
        @JavascriptInterface
        fun onNotificationReceived(message: String) {
            try {
                Log.d(TAG, "Received notification: $message")
                notificationCallback?.invoke(message)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification: ${e.message}", e)
            }
        }
    }
    
    private var isWebViewReady = false
    
    fun initializeWebView(webView: WebView) {
        this.webView = webView
        
        // Cấu hình WebView
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        
        // Thêm JavaScript Interface
        webView.addJavascriptInterface(AndroidJSInterface(), "AndroidInterface")
        
        // Set WebViewClient để load HTML
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "WebView page loaded")
                isWebViewReady = true
            }
        }
        
        // Load HTML file từ assets
        webView.loadUrl("file:///android_asset/stomp.html")
    }
    
    fun connect() {
        if (isConnected) {
            Log.d(TAG, "WebSocket already connected")
            return
        }
        
        val token = AppPreferences.getToken()
        if (token == null) {
            Log.e(TAG, "No token found, cannot connect to WebSocket")
            return
        }
        
        if (webView == null) {
            Log.e(TAG, "WebView not initialized")
            return
        }
        
        if (!isWebViewReady) {
            Log.w(TAG, "WebView not ready yet, waiting...")
            // Đợi WebView load xong
            webView?.postDelayed({
                connect()
            }, 500)
            return
        }
        
        try {
            val serverUrl = "$baseUrl$wsPath"
            Log.d(TAG, "Connecting to: $serverUrl with token")
            
            // Gọi JavaScript function để kết nối
            // Đảm bảo escape token để tránh lỗi JavaScript
            val escapedToken = token.replace("'", "\\'").replace("\n", "\\n")
            val jsCode = """
                (function() {
                    try {
                        if (window.StompWebSocket) {
                            console.log('Calling StompWebSocket.connect');
                            window.StompWebSocket.connect('$serverUrl', '$escapedToken');
                        } else {
                            console.error('StompWebSocket not available');
                            if (window.AndroidInterface) {
                                window.AndroidInterface.onError('StompWebSocket not available');
                            }
                        }
                    } catch (e) {
                        console.error('Error in connect:', e);
                        if (window.AndroidInterface) {
                            window.AndroidInterface.onError('Connect error: ' + e.message);
                        }
                    }
                })();
            """.trimIndent()
            
            webView?.post {
                webView?.evaluateJavascript(jsCode) { result ->
                    Log.d(TAG, "Connect JS result: $result")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to WebSocket: ${e.message}", e)
            isConnected = false
            connectionStatusCallback?.invoke(false)
        }
    }
    
    fun disconnect() {
        if (!isConnected) {
            Log.d(TAG, "WebSocket already disconnected")
            return
        }
        
        try {
            val jsCode = """
                if (window.StompWebSocket) {
                    window.StompWebSocket.disconnect();
                }
            """.trimIndent()
            
            webView?.post {
                webView?.evaluateJavascript(jsCode, null)
            }
            
            isConnected = false
            connectionStatusCallback?.invoke(false)
            Log.d(TAG, "WebSocket disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting WebSocket: ${e.message}", e)
        }
    }
    
    private fun parseTelemetry(json: String): List<Telemetry> {
        if (json.isBlank()) {
            Log.w(TAG, "Empty JSON string for telemetry")
            return emptyList()
        }
        
        return try {
            val type = object : TypeToken<List<TelemetryResponse>>() {}.type
            val responses = gson.fromJson<List<TelemetryResponse>>(json, type)
            responses?.map { it.toDomain() } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing telemetry JSON: ${e.message}", e)
            emptyList()
        }
    }
    
    fun setTelemetryCallback(callback: (List<Telemetry>) -> Unit) {
        this.telemetryCallback = callback
    }
    
    fun setNotificationCallback(callback: (String) -> Unit) {
        this.notificationCallback = callback
    }
    
    fun setConnectionStatusCallback(callback: (Boolean) -> Unit) {
        this.connectionStatusCallback = callback
    }
    
    fun isConnected(): Boolean = isConnected
    
    fun cleanup() {
        disconnect()
        webView?.destroy()
        webView = null
    }
}

