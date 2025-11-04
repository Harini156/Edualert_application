<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>📧 Message System Test - EduAlert</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; color: #2c3e50; margin-bottom: 30px; }
        .test-section { margin: 20px 0; padding: 15px; border: 2px solid #3498db; border-radius: 8px; background: #ecf0f1; }
        .test-title { font-size: 18px; font-weight: bold; color: #2980b9; margin-bottom: 10px; }
        .user-test { margin: 15px 0; padding: 10px; background: white; border-radius: 5px; border-left: 4px solid #e74c3c; }
        .success { border-left-color: #27ae60; }
        .warning { border-left-color: #f39c12; }
        .error { border-left-color: #e74c3c; }
        .message-item { background: #f8f9fa; margin: 5px 0; padding: 8px; border-radius: 4px; font-size: 14px; }
        .stats { display: flex; justify-content: space-around; margin: 20px 0; }
        .stat-box { text-align: center; padding: 15px; background: #3498db; color: white; border-radius: 8px; min-width: 120px; }
        .api-url { background: #2c3e50; color: #ecf0f1; padding: 10px; border-radius: 5px; font-family: monospace; margin: 10px 0; }
        .timestamp { color: #7f8c8d; font-size: 12px; }
        pre { background: #2c3e50; color: #ecf0f1; padding: 10px; border-radius: 5px; overflow-x: auto; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>📧 EduAlert Message System Test</h1>
            <p>Complete Message Flow Testing & Debugging</p>
            <div class="timestamp">Test Run: <?php echo date('Y-m-d H:i:s'); ?></div>
        </div>

<?php
include 'db.php';

// Test configuration
$test_users = [
    'student' => ['STU001', 'STU002', 'STU003'],
    'staff' => ['STF001', 'STF002', 'STF003'],
    'admin' => ['ADM001', 'ADM002']
];

$total_tests = 0;
$passed_tests = 0;
$failed_tests = 0;

function runTest($title, $callback) {
    global $total_tests, $passed_tests, $failed_tests;
    $total_tests++;
    
    try {
        $result = $callback();
        if ($result['success']) {
            $passed_tests++;
            $class = 'success';
            $icon = '✅';
        } else {
            $failed_tests++;
            $class = 'error';
            $icon = '❌';
        }
    } catch (Exception $e) {
        $failed_tests++;
        $result = ['success' => false, 'message' => 'Exception: ' . $e->getMessage()];
        $class = 'error';
        $icon = '❌';
    }
    
    echo "<div class='user-test $class'>";
    echo "<strong>$icon $title</strong><br>";
    echo $result['message'];
    if (isset($result['data'])) {
        echo "<pre>" . json_encode($result['data'], JSON_PRETTY_PRINT) . "</pre>";
    }
    echo "</div>";
    
    return $result;
}

// Display statistics
echo "<div class='stats'>";
echo "<div class='stat-box'><h3>" . count($test_users['student']) . "</h3><p>Test Students</p></div>";
echo "<div class='stat-box'><h3>" . count($test_users['staff']) . "</h3><p>Test Staff</p></div>";
echo "<div class='stat-box'><h3>" . count($test_users['admin']) . "</h3><p>Test Admins</p></div>";
echo "</div>";

// API URLs for reference
echo "<div class='test-section'>";
echo "<div class='test-title'>🔗 API Endpoints Being Tested</div>";
echo "<div class='api-url'>GET/POST: " . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/getadminmsg.php</div>";
echo "<div class='api-url'>GET/POST: " . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/get_staff_messages.php</div>";
echo "<div class='api-url'>GET/POST: " . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/get_student_messages.php</div>";
echo "<div class='api-url'>GET: " . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/adminsent.php</div>";
echo "<div class='api-url'>GET: " . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/staffsentmsg.php?sender_id=STF001</div>";
echo "</div>";

// Test 1: Database Connection
echo "<div class='test-section'>";
echo "<div class='test-title'>🔌 Database Connection Test</div>";
runTest("Database Connection", function() use ($conn) {
    if ($conn->connect_error) {
        return ['success' => false, 'message' => 'Connection failed: ' . $conn->connect_error];
    }
    return ['success' => true, 'message' => 'Database connected successfully'];
});
echo "</div>";

// Test 2: Table Structure Verification
echo "<div class='test-section'>";
echo "<div class='test-title'>🗃️ Database Table Structure</div>";

runTest("Messages Table Structure", function() use ($conn) {
    $result = $conn->query("DESCRIBE messages");
    if (!$result) {
        return ['success' => false, 'message' => 'Messages table not found'];
    }
    $columns = [];
    while ($row = $result->fetch_assoc()) {
        $columns[] = $row['Field'];
    }
    $required = ['id', 'title', 'content', 'recipient_type', 'created_at'];
    $missing = array_diff($required, $columns);
    if (empty($missing)) {
        return ['success' => true, 'message' => 'Messages table structure is correct', 'data' => $columns];
    } else {
        return ['success' => false, 'message' => 'Missing columns: ' . implode(', ', $missing), 'data' => $columns];
    }
});

runTest("Staff Messages Table Structure", function() use ($conn) {
    $result = $conn->query("DESCRIBE staffmessages");
    if (!$result) {
        return ['success' => false, 'message' => 'Staff messages table not found'];
    }
    $columns = [];
    while ($row = $result->fetch_assoc()) {
        $columns[] = $row['Field'];
    }
    $required = ['id', 'sender_id', 'title', 'content', 'recipient_type', 'created_at'];
    $missing = array_diff($required, $columns);
    if (empty($missing)) {
        return ['success' => true, 'message' => 'Staff messages table structure is correct', 'data' => $columns];
    } else {
        return ['success' => false, 'message' => 'Missing columns: ' . implode(', ', $missing), 'data' => $columns];
    }
});

echo "</div>";

// Test 3: Sample Data Check
echo "<div class='test-section'>";
echo "<div class='test-title'>📊 Sample Data Analysis</div>";

runTest("Admin Messages Count", function() use ($conn) {
    $result = $conn->query("SELECT COUNT(*) as count FROM messages");
    $row = $result->fetch_assoc();
    $count = $row['count'];
    return ['success' => true, 'message' => "Found $count admin messages in database"];
});

runTest("Staff Messages Count", function() use ($conn) {
    $result = $conn->query("SELECT COUNT(*) as count FROM staffmessages");
    $row = $result->fetch_assoc();
    $count = $row['count'];
    return ['success' => true, 'message' => "Found $count staff messages in database"];
});

runTest("Users Count by Type", function() use ($conn) {
    $result = $conn->query("SELECT user_type, COUNT(*) as count FROM users GROUP BY user_type");
    $users = [];
    while ($row = $result->fetch_assoc()) {
        $users[$row['user_type']] = $row['count'];
    }
    return ['success' => true, 'message' => 'User distribution retrieved', 'data' => $users];
});

echo "</div>";

// Test 4: API Endpoint Testing
echo "<div class='test-section'>";
echo "<div class='test-title'>🔧 API Endpoint Testing</div>";

// Test getadminmsg.php
runTest("getadminmsg.php - Student Test", function() use ($conn) {
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/getadminmsg.php");
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query(['user_id' => 'STU001']));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    if ($httpCode == 200) {
        $data = json_decode($response, true);
        if ($data && isset($data['status'])) {
            if ($data['status']) {
                $count = count($data['messages'] ?? []);
                return ['success' => true, 'message' => "Student can retrieve admin messages. Found: $count messages"];
            } else {
                return ['success' => false, 'message' => 'API returned error: ' . ($data['message'] ?? 'Unknown error')];
            }
        } else {
            return ['success' => false, 'message' => 'Invalid API response format', 'data' => $response];
        }
    } else {
        return ['success' => false, 'message' => "HTTP Error: $httpCode"];
    }
});

runTest("get_student_messages.php Test", function() use ($conn) {
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/get_student_messages.php");
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query(['user_id' => 'STU001']));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    if ($httpCode == 200) {
        $data = json_decode($response, true);
        if ($data && isset($data['status'])) {
            if ($data['status']) {
                $count = count($data['messages'] ?? []);
                return ['success' => true, 'message' => "Student can retrieve all messages. Found: $count messages"];
            } else {
                return ['success' => false, 'message' => 'API returned error: ' . ($data['message'] ?? 'Unknown error')];
            }
        } else {
            return ['success' => false, 'message' => 'Invalid API response format', 'data' => $response];
        }
    } else {
        return ['success' => false, 'message' => "HTTP Error: $httpCode"];
    }
});

runTest("get_staff_messages.php Test", function() use ($conn) {
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/get_staff_messages.php");
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query(['user_id' => 'STF001']));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    if ($httpCode == 200) {
        $data = json_decode($response, true);
        if ($data && isset($data['status'])) {
            if ($data['status']) {
                $count = count($data['messages'] ?? []);
                return ['success' => true, 'message' => "Staff can retrieve messages from other staff. Found: $count messages"];
            } else {
                return ['success' => false, 'message' => 'API returned error: ' . ($data['message'] ?? 'Unknown error')];
            }
        } else {
            return ['success' => false, 'message' => 'Invalid API response format', 'data' => $response];
        }
    } else {
        return ['success' => false, 'message' => "HTTP Error: $httpCode"];
    }
});

echo "</div>";

// Test 5: Message Flow Simulation
echo "<div class='test-section'>";
echo "<div class='test-title'>🔄 Complete Message Flow Test</div>";

runTest("Admin Sent Messages Retrieval", function() use ($conn) {
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/adminsent.php");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    if ($httpCode == 200) {
        $data = json_decode($response, true);
        if ($data && $data['status'] == 'success') {
            $count = count($data['messages'] ?? []);
            return ['success' => true, 'message' => "Admin sent messages API working. Found: $count messages"];
        } else {
            return ['success' => false, 'message' => 'API returned error or invalid format'];
        }
    } else {
        return ['success' => false, 'message' => "HTTP Error: $httpCode"];
    }
});

runTest("Staff Sent Messages Retrieval", function() use ($conn) {
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/staffsentmsg.php?sender_id=STF001");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    if ($httpCode == 200) {
        $data = json_decode($response, true);
        if ($data && $data['status'] == 'success') {
            $count = $data['total'] ?? 0;
            return ['success' => true, 'message' => "Staff sent messages API working. Found: $count messages"];
        } else {
            return ['success' => false, 'message' => 'API returned error or invalid format'];
        }
    } else {
        return ['success' => false, 'message' => "HTTP Error: $httpCode"];
    }
});

echo "</div>";

// Test 6: File Attachment Support
echo "<div class='test-section'>";
echo "<div class='test-title'>📎 File Attachment Support Test</div>";

runTest("Upload Directory Check", function() {
    $uploadDir = 'uploads/';
    if (!file_exists($uploadDir)) {
        if (mkdir($uploadDir, 0777, true)) {
            return ['success' => true, 'message' => 'Upload directory created successfully'];
        } else {
            return ['success' => false, 'message' => 'Failed to create upload directory'];
        }
    } else {
        if (is_writable($uploadDir)) {
            return ['success' => true, 'message' => 'Upload directory exists and is writable'];
        } else {
            return ['success' => false, 'message' => 'Upload directory exists but is not writable'];
        }
    }
});

runTest("Supported File Types", function() {
    $allowedTypes = ['pdf', 'doc', 'docx', 'jpg', 'jpeg', 'png', 'gif', 'bmp', 'txt', 'xls', 'xlsx', 'ppt', 'pptx', 'zip', 'rar'];
    return ['success' => true, 'message' => 'Supported file types: ' . implode(', ', $allowedTypes), 'data' => $allowedTypes];
});

echo "</div>";

// Final Statistics
echo "<div class='test-section'>";
echo "<div class='test-title'>📈 Test Results Summary</div>";
echo "<div class='stats'>";
echo "<div class='stat-box' style='background: #27ae60;'><h3>$passed_tests</h3><p>Passed</p></div>";
echo "<div class='stat-box' style='background: #e74c3c;'><h3>$failed_tests</h3><p>Failed</p></div>";
echo "<div class='stat-box' style='background: #3498db;'><h3>$total_tests</h3><p>Total Tests</p></div>";
$success_rate = $total_tests > 0 ? round(($passed_tests / $total_tests) * 100, 1) : 0;
echo "<div class='stat-box' style='background: #9b59b6;'><h3>{$success_rate}%</h3><p>Success Rate</p></div>";
echo "</div>";

if ($failed_tests > 0) {
    echo "<div class='user-test error'>";
    echo "<strong>⚠️ Issues Found</strong><br>";
    echo "Some tests failed. Please check the failed tests above and fix the issues before deploying to production.";
    echo "</div>";
} else {
    echo "<div class='user-test success'>";
    echo "<strong>🎉 All Tests Passed!</strong><br>";
    echo "Message system is working correctly. Ready for production deployment.";
    echo "</div>";
}

echo "</div>";

$conn->close();
?>

        <div class="test-section">
            <div class="test-title">🔗 Quick Test URLs</div>
            <div class="api-url">
                <strong>Test This Page:</strong><br>
                http://<?php echo $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI']; ?>
            </div>
            <div class="api-url">
                <strong>Test Individual APIs:</strong><br>
                • Admin Messages: http://<?php echo $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']); ?>/getadminmsg.php<br>
                • Staff Messages: http://<?php echo $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']); ?>/get_staff_messages.php<br>
                • Student Messages: http://<?php echo $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']); ?>/get_student_messages.php<br>
                • Admin Sent: http://<?php echo $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']); ?>/adminsent.php<br>
                • Staff Sent: http://<?php echo $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']); ?>/staffsentmsg.php?sender_id=STF001
            </div>
        </div>

        <div class="header">
            <p style="color: #7f8c8d; font-size: 14px;">
                💡 <strong>Usage:</strong> Run this test after deploying the message system files to verify everything works correctly.<br>
                🔄 <strong>Refresh:</strong> Reload this page to run tests again after making changes.
            </p>
        </div>
    </div>
</body>
</html>