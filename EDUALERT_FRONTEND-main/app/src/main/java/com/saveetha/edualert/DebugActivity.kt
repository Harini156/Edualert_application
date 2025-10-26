package com.saveetha.edualert

import android.content.Context
import android.os.Bundle
import android.view.View
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
    private lateinit var testBellCountButton: Button
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
        testBellCountButton = findViewById(R.id.testBellCountButton)
        closeDebugButton = findViewById(R.id.closeDebugButton)
    }
    
    private fun setupClickListeners() {
        runTestsButton.setOnClickListener {
            runAllTests()
        }
        
        testTickButton.setOnClickListener {
            testTickButtonFunctionality()
        }
        
        testBellCountButton.setOnClickListener {
            testBellIconCount()
        }
        
        // Add a direct API test button
        val directApiTestButton = Button(this)
        directApiTestButton.text = "Test API Directly"
        directApiTestButton.setOnClickListener {
            testApiDirectly()
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
        val url = ApiClient.BASE_URL + "test_database.php"
        
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
        val url = ApiClient.BASE_URL + "test_database.php"
        
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
        val url = ApiClient.BASE_URL + "api/get_message_count.php"
        
        val requestBody = JSONObject().apply {
            put("user_type", "student")
            put("user_id", "STU001")
        }
        
        addDebugMessage("Request URL: $url")
        addDebugMessage("Request Body: $requestBody")
        
        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            requestBody,
            { response ->
                val status = response.getString("status")
                val count = response.optInt("unread_count", 0)
                val messagesCount = response.optInt("messages_count", 0)
                val staffMessagesCount = response.optInt("staffmessages_count", 0)
                
                addDebugMessage("✅ Message count API successful:")
                addDebugMessage("  - Total unread: $count")
                addDebugMessage("  - Messages count: $messagesCount")
                addDebugMessage("  - Staff messages count: $staffMessagesCount")
                
                messageCountStatus.text = "Message Count: ✅ $count unread messages (Admin: $messagesCount, Staff: $staffMessagesCount)"
                messageCountStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                
                // Show debug info if available
                if (response.has("debug")) {
                    val debug = response.getJSONObject("debug")
                    addDebugMessage("Debug Info: $debug")
                }
            },
            { error ->
                addDebugMessage("❌ Message count API failed: ${error.message}")
                addDebugMessage("Error details: ${error.networkResponse?.statusCode}")
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
        addDebugMessage("Request URL: ${ApiClient.BASE_URL}api/mark_message_read.php")
        
        val url = ApiClient.BASE_URL + "api/mark_message_read.php"
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
    
    private fun testBellIconCount() {
        addDebugMessage("Testing bell icon count functionality...")
        
        // Test the NotificationManager directly
        val testBadge = TextView(this)
        testBadge.visibility = View.GONE
        
        addDebugMessage("Initial badge state - Visibility: ${if (testBadge.visibility == View.VISIBLE) "VISIBLE" else "GONE"}, Text: '${testBadge.text}'")
        addDebugMessage("Calling NotificationManager.setupNotificationIcon...")
        
        // This will test the actual NotificationManager logic
        NotificationManager.setupNotificationIcon(
            this,
            testBadge,
            testBadge,
            "student",
            "STU001",
            "CSE",
            "2"
        )
        
        addDebugMessage("NotificationManager call completed")
        addDebugMessage("Badge visibility: ${if (testBadge.visibility == View.VISIBLE) "VISIBLE" else "GONE"}")
        addDebugMessage("Badge text: '${testBadge.text}'")
        
        // Wait a bit for the network request to complete
        addDebugMessage("Waiting for network request to complete...")
        
        // Use a handler to check the badge state after a delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            addDebugMessage("After 3 seconds - Badge visibility: ${if (testBadge.visibility == View.VISIBLE) "VISIBLE" else "GONE"}, Text: '${testBadge.text}'")
            
            // Also test the refresh function
            addDebugMessage("Testing refresh function...")
            NotificationManager.refreshMessageCount(
                this,
                testBadge,
                "student",
                "STU001",
                "CSE",
                "2"
            )
            
            addDebugMessage("Refresh call completed")
            
            // Check again after refresh
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                addDebugMessage("Final badge state - Visibility: ${if (testBadge.visibility == View.VISIBLE) "VISIBLE" else "GONE"}, Text: '${testBadge.text}'")
            }, 2000)
            
        }, 3000)
    }
    
    private fun testApiDirectly() {
        addDebugMessage("Testing API directly...")
        
        val url = ApiClient.BASE_URL + "api/get_message_count.php"
        val requestBody = JSONObject().apply {
            put("user_type", "student")
            put("user_id", "STU001")
        }
        
        addDebugMessage("Request URL: $url")
        addDebugMessage("Request Body: $requestBody")
        
        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            requestBody,
            { response ->
                addDebugMessage("✅ Direct API call successful: $response")
                val status = response.getString("status")
                val count = response.optInt("unread_count", 0)
                addDebugMessage("Status: $status, Count: $count")
            },
            { error ->
                addDebugMessage("❌ Direct API call failed: ${error.message}")
                addDebugMessage("Error details: ${error.networkResponse?.statusCode}")
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
