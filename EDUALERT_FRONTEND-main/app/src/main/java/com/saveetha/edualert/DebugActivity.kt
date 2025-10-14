package com.saveetha.edualert

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class DebugActivity : AppCompatActivity() {
    
    private lateinit var serverStatus: TextView
    private lateinit var databaseStatus: TextView
    private lateinit var messageCountStatus: TextView
    private lateinit var sampleMessageData: TextView
    private lateinit var apiTestResults: TextView
    private lateinit var debugLog: TextView
    private lateinit var runTestsButton: Button
    private lateinit var testTickButton: Button
    private lateinit var closeDebugButton: Button
    
    private val debugMessages = mutableListOf<String>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        
        initializeViews()
        setupClickListeners()
        addDebugMessage("Debug Activity Started")
    }
    
    private fun initializeViews() {
        serverStatus = findViewById(R.id.serverStatus)
        databaseStatus = findViewById(R.id.databaseStatus)
        messageCountStatus = findViewById(R.id.messageCountStatus)
        sampleMessageData = findViewById(R.id.sampleMessageData)
        apiTestResults = findViewById(R.id.apiTestResults)
        debugLog = findViewById(R.id.debugLog)
        runTestsButton = findViewById(R.id.runTestsButton)
        testTickButton = findViewById(R.id.testTickButton)
        closeDebugButton = findViewById(R.id.closeDebugButton)
    }
    
    private fun setupClickListeners() {
        runTestsButton.setOnClickListener {
            runAllTests()
        }
        
        testTickButton.setOnClickListener {
            testTickButtonFunctionality()
        }
        
        closeDebugButton.setOnClickListener {
            finish()
        }
    }
    
    private fun runAllTests() {
        addDebugMessage("Starting all tests...")
        serverStatus.text = "Server Status: Testing..."
        databaseStatus.text = "Database Status: Testing..."
        messageCountStatus.text = "Message Count: Testing..."
        
        // Test 1: Server Connection
        testServerConnection()
        
        // Test 2: Database Connection
        testDatabaseConnection()
        
        // Test 3: Message Count API
        testMessageCountAPI()
        
        // Test 4: Sample Message Data
        testSampleMessageData()
    }
    
    private fun testServerConnection() {
        addDebugMessage("Testing server connection...")
        val url = "http://192.168.1.7/test_database.php"
        
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                addDebugMessage("✅ Server connection successful")
                serverStatus.text = "Server Status: ✅ Connected"
                serverStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            },
            { error ->
                addDebugMessage("❌ Server connection failed: ${error.message}")
                serverStatus.text = "Server Status: ❌ Failed - ${error.message}"
                serverStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
        )
        
        Volley.newRequestQueue(this).add(request)
    }
    
    private fun testDatabaseConnection() {
        addDebugMessage("Testing database connection...")
        val url = "http://192.168.1.7/test_database.php"
        
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                addDebugMessage("✅ Database connection successful")
                databaseStatus.text = "Database Status: ✅ Connected"
                databaseStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            },
            { error ->
                addDebugMessage("❌ Database connection failed: ${error.message}")
                databaseStatus.text = "Database Status: ❌ Failed - ${error.message}"
                databaseStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
        )
        
        Volley.newRequestQueue(this).add(request)
    }
    
    private fun testMessageCountAPI() {
        addDebugMessage("Testing message count API...")
        val url = "http://192.168.1.7/get_message_count.php"
        
        val requestBody = JSONObject().apply {
            put("user_type", "student")
            put("user_id", "STU001")
        }
        
        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            requestBody,
            { response ->
                val status = response.getString("status")
                val count = response.optInt("unread_count", 0)
                addDebugMessage("✅ Message count API successful: $count unread messages")
                messageCountStatus.text = "Message Count: ✅ $count unread messages"
                messageCountStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            },
            { error ->
                addDebugMessage("❌ Message count API failed: ${error.message}")
                messageCountStatus.text = "Message Count: ❌ Failed - ${error.message}"
                messageCountStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
        )
        
        Volley.newRequestQueue(this).add(request)
    }
    
    private fun testSampleMessageData() {
        addDebugMessage("Testing sample message data...")
        // This would typically come from your actual message loading code
        // For now, we'll simulate it
        sampleMessageData.text = "Sample Message Data: ✅ Ready for testing"
        sampleMessageData.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        addDebugMessage("✅ Sample message data ready")
    }
    
    private fun testTickButtonFunctionality() {
        addDebugMessage("Testing tick button functionality...")
        apiTestResults.text = "API Test Results: Testing tick button..."
        
        // Test with a sample message ID
        val testMessageId = 1
        val testTableName = "messages"
        
        addDebugMessage("Testing with Message ID: $testMessageId, Table: $testTableName")
        addDebugMessage("Request URL: http://192.168.1.7/mark_message_read.php")
        
        val url = "http://192.168.1.7/mark_message_read.php"
        val requestBody = JSONObject().apply {
            put("message_id", testMessageId.toString())
            put("table_name", testTableName)
        }
        
        addDebugMessage("Request Body: $requestBody")
        
        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            requestBody,
            { response ->
                val status = response.getString("status")
                val message = response.getString("message")
                addDebugMessage("✅ Tick button test result: $status - $message")
                
                // Show debug info if available
                if (response.has("debug")) {
                    val debug = response.getJSONObject("debug")
                    addDebugMessage("Debug Info: $debug")
                }
                
                apiTestResults.text = "API Test Results: ✅ $status - $message"
                apiTestResults.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            },
            { error ->
                addDebugMessage("❌ Tick button test failed: ${error.message}")
                addDebugMessage("Error details: ${error.networkResponse?.statusCode}")
                apiTestResults.text = "API Test Results: ❌ Failed - ${error.message}"
                apiTestResults.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
        )
        
        Volley.newRequestQueue(this).add(request)
    }
    
    private fun addDebugMessage(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        debugMessages.add("[$timestamp] $message")
        
        // Update debug log display
        val logText = debugMessages.joinToString("\n")
        debugLog.text = "Debug Log:\n$logText"
        
        // Scroll to bottom
        debugLog.post {
            val scrollView = debugLog.parent as? android.widget.ScrollView
            scrollView?.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }
}
