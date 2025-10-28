<!DOCTYPE html>
<html>
<head>
    <title>Notification System Test</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .section { margin: 20px 0; padding: 15px; border: 1px solid #ccc; }
        .success { background-color: #d4edda; }
        .error { background-color: #f8d7da; }
        .warning { background-color: #fff3cd; }
        table { border-collapse: collapse; width: 100%; margin: 10px 0; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .test-form { background: #f8f9fa; padding: 15px; margin: 10px 0; }
        input, select { margin: 5px; padding: 5px; }
        button { background: #007bff; color: white; padding: 8px 15px; border: none; cursor: pointer; }
    </style>
</head>
<body>
    <h1>EduAlert Notification System Test</h1>
    
    <?php
    include('db.php');
    
    // Test 1: Check database tables
    echo "<div class='section'>";
    echo "<h2>1. Database Tables Check</h2>";
    
    $tables = ['messages', 'staffmessages', 'users', 'student_details', 'staff_details'];
    foreach ($tables as $table) {
        $result = $conn->query("SHOW TABLES LIKE '$table'");
        if ($result->num_rows > 0) {
            echo "<p class='success'>✓ Table '$table' exists</p>";
            
            // Count records
            $count_result = $conn->query("SELECT COUNT(*) as count FROM $table");
            $count = $count_result->fetch_assoc()['count'];
            echo "<p>Records in $table: $count</p>";
        } else {
            echo "<p class='error'>✗ Table '$table' missing</p>";
        }
    }
    echo "</div>";
    
    // Test 2: Sample users
    echo "<div class='section'>";
    echo "<h2>2. Sample Users</h2>";
    
    $users_sql = "SELECT user_id, name, email, user_type, dept, year FROM users ORDER BY user_type, user_id LIMIT 10";
    $users_result = $conn->query($users_sql);
    
    if ($users_result->num_rows > 0) {
        echo "<table>";
        echo "<tr><th>User ID</th><th>Name</th><th>Email</th><th>Type</th><th>Dept</th><th>Year</th></tr>";
        while ($user = $users_result->fetch_assoc()) {
            echo "<tr>";
            echo "<td>" . htmlspecialchars($user['user_id']) . "</td>";
            echo "<td>" . htmlspecialchars($user['name']) . "</td>";
            echo "<td>" . htmlspecialchars($user['email']) . "</td>";
            echo "<td>" . htmlspecialchars($user['user_type']) . "</td>";
            echo "<td>" . htmlspecialchars($user['dept']) . "</td>";
            echo "<td>" . htmlspecialchars($user['year']) . "</td>";
            echo "</tr>";
        }
        echo "</table>";
    } else {
        echo "<p class='warning'>No users found</p>";
    }
    echo "</div>";
    
    // Test 3: Messages with status
    echo "<div class='section'>";
    echo "<h2>3. Messages Status</h2>";
    
    $messages_sql = "SELECT id, title, recipient_type, status, created_at FROM messages ORDER BY created_at DESC LIMIT 5";
    $messages_result = $conn->query($messages_sql);
    
    if ($messages_result->num_rows > 0) {
        echo "<h3>Admin Messages:</h3>";
        echo "<table>";
        echo "<tr><th>ID</th><th>Title</th><th>Recipient</th><th>Status</th><th>Created</th></tr>";
        while ($msg = $messages_result->fetch_assoc()) {
            echo "<tr>";
            echo "<td>" . $msg['id'] . "</td>";
            echo "<td>" . htmlspecialchars(substr($msg['title'], 0, 30)) . "</td>";
            echo "<td>" . htmlspecialchars($msg['recipient_type']) . "</td>";
            echo "<td>" . htmlspecialchars($msg['status']) . "</td>";
            echo "<td>" . $msg['created_at'] . "</td>";
            echo "</tr>";
        }
        echo "</table>";
    }
    
    $staff_messages_sql = "SELECT id, title, recipient_type, status, created_at FROM staffmessages ORDER BY created_at DESC LIMIT 5";
    $staff_messages_result = $conn->query($staff_messages_sql);
    
    if ($staff_messages_result->num_rows > 0) {
        echo "<h3>Staff Messages:</h3>";
        echo "<table>";
        echo "<tr><th>ID</th><th>Title</th><th>Recipient</th><th>Status</th><th>Created</th></tr>";
        while ($msg = $staff_messages_result->fetch_assoc()) {
            echo "<tr>";
            echo "<td>" . $msg['id'] . "</td>";
            echo "<td>" . htmlspecialchars(substr($msg['title'], 0, 30)) . "</td>";
            echo "<td>" . htmlspecialchars($msg['recipient_type']) . "</td>";
            echo "<td>" . htmlspecialchars($msg['status']) . "</td>";
            echo "<td>" . $msg['created_at'] . "</td>";
            echo "</tr>";
        }
        echo "</table>";
    }
    echo "</div>";
    
    // Test 4: Notification count test
    echo "<div class='section'>";
    echo "<h2>4. Test Notification Count API</h2>";
    
    if (isset($_POST['test_count'])) {
        $test_user_type = $_POST['test_user_type'];
        $test_user_id = $_POST['test_user_id'];
        $test_department = $_POST['test_department'];
        $test_year = $_POST['test_year'];
        
        // Simulate API call
        $post_data = [
            'user_type' => $test_user_type,
            'user_id' => $test_user_id,
            'department' => $test_department,
            'year' => $test_year
        ];
        
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . '/get_message_count.php');
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($post_data));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        
        $response = curl_exec($ch);
        curl_close($ch);
        
        echo "<h3>API Response:</h3>";
        echo "<pre>" . htmlspecialchars($response) . "</pre>";
    }
    
    echo "<div class='test-form'>";
    echo "<h3>Test Message Count:</h3>";
    echo "<form method='post'>";
    echo "<select name='test_user_type'>";
    echo "<option value='student'>Student</option>";
    echo "<option value='staff'>Staff</option>";
    echo "<option value='admin'>Admin</option>";
    echo "</select>";
    echo "<input type='text' name='test_user_id' placeholder='User ID' value='STU001'>";
    echo "<input type='text' name='test_department' placeholder='Department' value='CSE'>";
    echo "<input type='text' name='test_year' placeholder='Year' value='2'>";
    echo "<button type='submit' name='test_count'>Test Count</button>";
    echo "</form>";
    echo "</div>";
    echo "</div>";
    
    $conn->close();
    ?>
    
    <div class='section'>
        <h2>5. Quick Fixes</h2>
        <p><strong>If notification count is 0 but you have unread messages:</strong></p>
        <ul>
            <li>Check if messages have status = 'unread'</li>
            <li>Verify user filters (department, year, etc.) match</li>
            <li>Check recipient_type matches user type</li>
        </ul>
        
        <p><strong>If mark as read is not working:</strong></p>
        <ul>
            <li>Check message ID is being passed correctly</li>
            <li>Verify table name ('messages' or 'staffmessages')</li>
            <li>Check database permissions</li>
        </ul>
    </div>
</body>
</html>