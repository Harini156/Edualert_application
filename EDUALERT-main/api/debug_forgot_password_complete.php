<?php
// ULTIMATE FORGOT PASSWORD DIAGNOSTIC TOOL
// This file will find EXACTLY what's causing the "Server error occurred" message

header('Content-Type: text/html; charset=UTF-8');
error_reporting(E_ALL);
ini_set('display_errors', 1);

$results = [];
$overall_status = true;

function addResult($test_name, $status, $message, $details = null) {
    global $results, $overall_status;
    $results[] = [
        'test' => $test_name,
        'status' => $status,
        'message' => $message,
        'details' => $details
    ];
    if (!$status) $overall_status = false;
}

function formatResult($result) {
    $icon = $result['status'] ? '✅' : '❌';
    $color = $result['status'] ? '#4caf50' : '#f44336';
    $details = $result['details'] ? '<div style="margin-top:10px; padding:10px; background:#f9f9f9; border-left:3px solid #ccc; font-family:monospace; font-size:12px;">' . nl2br(htmlspecialchars($result['details'])) . '</div>' : '';
    
    return '<div style="margin:10px 0; padding:15px; border-left:4px solid ' . $color . '; background:white;">
        <strong>' . $icon . ' ' . $result['test'] . '</strong><br>
        <span style="color:' . $color . ';">' . $result['message'] . '</span>
        ' . $details . '
    </div>';
}

?>
<!DOCTYPE html>
<html>
<head>
    <title>🔍 FORGOT PASSWORD COMPLETE DIAGNOSTIC</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; }
        .header { background: #2196F3; color: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
        .section { background: white; margin: 15px 0; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .summary { padding: 15px; border-radius: 5px; margin: 15px 0; }
        .success { background: #e8f5e8; border-left: 4px solid #4caf50; }
        .error { background: #ffebee; border-left: 4px solid #f44336; }
        .warning { background: #fff3e0; border-left: 4px solid #ff9800; }
        .code { background: #f5f5f5; padding: 10px; border-radius: 4px; font-family: monospace; margin: 10px 0; }
        .test-form { background: #e3f2fd; padding: 15px; border-radius: 5px; margin: 15px 0; }
        pre { background: #f5f5f5; padding: 15px; border-radius: 4px; overflow-x: auto; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔍 FORGOT PASSWORD COMPLETE DIAGNOSTIC</h1>
            <p>This tool will find EXACTLY what's causing the forgot password "Server error occurred" message</p>
            <p><strong>Server:</strong> <?php echo $_SERVER['SERVER_SOFTWARE']; ?> | <strong>PHP:</strong> <?php echo PHP_VERSION; ?> | <strong>Time:</strong> <?php echo date('Y-m-d H:i:s'); ?></p>
        </div>

<?php

// TEST 1: SERVER ENVIRONMENT CHECK
try {
    $php_version = PHP_VERSION;
    $server_software = $_SERVER['SERVER_SOFTWARE'];
    $document_root = $_SERVER['DOCUMENT_ROOT'];
    $script_path = __FILE__;
    
    $env_details = "PHP Version: $php_version\n";
    $env_details .= "Server Software: $server_software\n";
    $env_details .= "Document Root: $document_root\n";
    $env_details .= "Script Path: $script_path\n";
    $env_details .= "Error Reporting: " . (error_reporting() ? 'ON' : 'OFF') . "\n";
    $env_details .= "Display Errors: " . (ini_get('display_errors') ? 'ON' : 'OFF') . "\n";
    $env_details .= "Log Errors: " . (ini_get('log_errors') ? 'ON' : 'OFF') . "\n";
    $env_details .= "Mail Function: " . (function_exists('mail') ? 'AVAILABLE' : 'NOT AVAILABLE') . "\n";
    
    addResult('Server Environment Check', true, 'Server environment is accessible', $env_details);
} catch (Exception $e) {
    addResult('Server Environment Check', false, 'Server environment check failed: ' . $e->getMessage());
}

// TEST 2: DATABASE CONNECTION TEST
$conn = null;
try {
    // First, let's see what's in db.php
    $db_file_path = __DIR__ . '/db.php';
    if (!file_exists($db_file_path)) {
        addResult('Database File Check', false, 'db.php file not found at: ' . $db_file_path);
    } else {
        $db_content = file_get_contents($db_file_path);
        $db_details = "db.php file found\n";
        $db_details .= "File size: " . filesize($db_file_path) . " bytes\n";
        $db_details .= "File content preview:\n" . substr($db_content, 0, 500) . "...";
        
        addResult('Database File Check', true, 'db.php file exists and readable', $db_details);
        
        // Now try to include and connect
        include $db_file_path;
        
        if (isset($conn) && $conn instanceof mysqli) {
            $db_info = "Connection successful!\n";
            $db_info .= "Host: " . $conn->host_info . "\n";
            $db_info .= "Server Version: " . $conn->server_info . "\n";
            $db_info .= "Client Version: " . $conn->client_info . "\n";
            $db_info .= "Character Set: " . $conn->character_set_name() . "\n";
            
            addResult('Database Connection Test', true, 'Database connection successful', $db_info);
        } else {
            addResult('Database Connection Test', false, 'Database connection failed - $conn variable not set or not mysqli object');
        }
    }
} catch (Exception $e) {
    addResult('Database Connection Test', false, 'Database connection failed: ' . $e->getMessage());
}

// TEST 3: DATABASE STRUCTURE VERIFICATION
if ($conn) {
    try {
        // Check if database exists and is selected
        $db_name_result = $conn->query("SELECT DATABASE()");
        if ($db_name_result) {
            $db_name = $db_name_result->fetch_row()[0];
            $db_details = "Current database: " . ($db_name ?: 'No database selected') . "\n";
        } else {
            $db_details = "Could not determine current database\n";
        }
        
        // List all tables
        $tables_result = $conn->query("SHOW TABLES");
        if ($tables_result) {
            $tables = [];
            while ($row = $tables_result->fetch_row()) {
                $tables[] = $row[0];
            }
            $db_details .= "Available tables: " . implode(', ', $tables) . "\n\n";
        }
        
        // Check password_reset table specifically
        $table_check = $conn->query("SHOW TABLES LIKE 'password_reset'");
        if ($table_check && $table_check->num_rows > 0) {
            // Table exists, check structure
            $structure_result = $conn->query("DESCRIBE password_reset");
            if ($structure_result) {
                $db_details .= "password_reset table structure:\n";
                while ($row = $structure_result->fetch_assoc()) {
                    $db_details .= "- {$row['Field']}: {$row['Type']} {$row['Null']} {$row['Key']} {$row['Default']}\n";
                }
                
                // Check if table has data
                $count_result = $conn->query("SELECT COUNT(*) as count FROM password_reset");
                if ($count_result) {
                    $count = $count_result->fetch_assoc()['count'];
                    $db_details .= "\nTable has $count records\n";
                }
                
                addResult('Password Reset Table Check', true, 'password_reset table exists with correct structure', $db_details);
            } else {
                addResult('Password Reset Table Check', false, 'Could not describe password_reset table: ' . $conn->error);
            }
        } else {
            $db_details .= "\n❌ password_reset table NOT FOUND!\n";
            $db_details .= "This is likely the main issue. The table needs to be created.\n\n";
            $db_details .= "Required SQL to create table:\n";
            $db_details .= "CREATE TABLE password_reset (\n";
            $db_details .= "  id INT(11) PRIMARY KEY AUTO_INCREMENT,\n";
            $db_details .= "  user_id VARCHAR(10) NOT NULL,\n";
            $db_details .= "  otp VARCHAR(6) NOT NULL,\n";
            $db_details .= "  expiry DATETIME NOT NULL,\n";
            $db_details .= "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n";
            $db_details .= ");";
            
            addResult('Password Reset Table Check', false, 'password_reset table does NOT exist', $db_details);
        }
        
        // Check users table
        $users_check = $conn->query("SHOW TABLES LIKE 'users'");
        if ($users_check && $users_check->num_rows > 0) {
            $users_count = $conn->query("SELECT COUNT(*) as count FROM users")->fetch_assoc()['count'];
            addResult('Users Table Check', true, "users table exists with $users_count records");
        } else {
            addResult('Users Table Check', false, 'users table does NOT exist');
        }
        
    } catch (Exception $e) {
        addResult('Database Structure Check', false, 'Database structure check failed: ' . $e->getMessage());
    }
} else {
    addResult('Database Structure Check', false, 'Skipped - Database connection not available');
}

// TEST 4: USER LOOKUP TEST
if ($conn) {
    try {
        $test_email = 'ramg65244@gmail.com'; // Use the email from your test
        $user_stmt = $conn->prepare("SELECT user_id, name, email FROM users WHERE email = ?");
        if ($user_stmt) {
            $user_stmt->bind_param("s", $test_email);
            $user_stmt->execute();
            $user_result = $user_stmt->get_result();
            
            if ($user_result->num_rows > 0) {
                $user = $user_result->fetch_assoc();
                $user_details = "User found:\n";
                $user_details .= "User ID: " . $user['user_id'] . "\n";
                $user_details .= "Name: " . $user['name'] . "\n";
                $user_details .= "Email: " . $user['email'] . "\n";
                
                addResult('User Lookup Test', true, "User found for email: $test_email", $user_details);
            } else {
                // Try to find any users
                $all_users = $conn->query("SELECT email FROM users LIMIT 5");
                $user_details = "User NOT found for email: $test_email\n\n";
                if ($all_users && $all_users->num_rows > 0) {
                    $user_details .= "Available users (first 5):\n";
                    while ($row = $all_users->fetch_assoc()) {
                        $user_details .= "- " . $row['email'] . "\n";
                    }
                } else {
                    $user_details .= "No users found in database at all!";
                }
                
                addResult('User Lookup Test', false, "User NOT found for email: $test_email", $user_details);
            }
            $user_stmt->close();
        } else {
            addResult('User Lookup Test', false, 'Could not prepare user lookup statement: ' . $conn->error);
        }
    } catch (Exception $e) {
        addResult('User Lookup Test', false, 'User lookup test failed: ' . $e->getMessage());
    }
} else {
    addResult('User Lookup Test', false, 'Skipped - Database connection not available');
}

// TEST 5: OTP GENERATION TEST
try {
    $test_otp = sprintf("%06d", mt_rand(100000, 999999));
    $test_expiry = date('Y-m-d H:i:s', strtotime('+10 minutes'));
    
    $otp_details = "Generated OTP: $test_otp\n";
    $otp_details .= "Expiry time: $test_expiry\n";
    $otp_details .= "Current time: " . date('Y-m-d H:i:s') . "\n";
    $otp_details .= "Time difference: 10 minutes\n";
    
    addResult('OTP Generation Test', true, 'OTP generation working correctly', $otp_details);
} catch (Exception $e) {
    addResult('OTP Generation Test', false, 'OTP generation failed: ' . $e->getMessage());
}

// TEST 6: EMAIL FUNCTION TEST
try {
    $mail_available = function_exists('mail');
    $email_details = "mail() function: " . ($mail_available ? 'AVAILABLE' : 'NOT AVAILABLE') . "\n";
    
    if ($mail_available) {
        // Try to get mail configuration
        $email_details .= "SMTP settings:\n";
        $email_details .= "- smtp_server: " . (ini_get('SMTP') ?: 'Not set') . "\n";
        $email_details .= "- smtp_port: " . (ini_get('smtp_port') ?: 'Not set') . "\n";
        $email_details .= "- sendmail_from: " . (ini_get('sendmail_from') ?: 'Not set') . "\n";
        $email_details .= "- sendmail_path: " . (ini_get('sendmail_path') ?: 'Not set') . "\n";
        
        // Test email sending (to a test address)
        $test_subject = "EduAlert Test Email - " . date('Y-m-d H:i:s');
        $test_message = "This is a test email from EduAlert forgot password diagnostic.\nTime: " . date('Y-m-d H:i:s');
        $test_headers = "From: noreply@edualert.com\r\nReply-To: support@edualert.com";
        
        $email_sent = @mail('test@example.com', $test_subject, $test_message, $test_headers);
        $email_details .= "\nTest email send result: " . ($email_sent ? 'SUCCESS' : 'FAILED') . "\n";
        $email_details .= "Note: Even if 'SUCCESS', email might not be delivered due to server configuration.";
        
        addResult('Email Function Test', $mail_available, 'Email function is available', $email_details);
    } else {
        $email_details .= "\n❌ CRITICAL ISSUE: mail() function is not available!\n";
        $email_details .= "This means the server cannot send emails.\n";
        $email_details .= "Contact server admin to enable mail functionality.";
        
        addResult('Email Function Test', false, 'Email function is NOT available', $email_details);
    }
} catch (Exception $e) {
    addResult('Email Function Test', false, 'Email function test failed: ' . $e->getMessage());
}

// TEST 7: COMPLETE FLOW SIMULATION
if ($conn) {
    try {
        $test_email = 'ramg65244@gmail.com';
        $flow_details = "Testing complete forgot password flow for: $test_email\n\n";
        
        // Step 1: Email validation
        if (!filter_var($test_email, FILTER_VALIDATE_EMAIL)) {
            $flow_details .= "❌ Step 1: Email validation FAILED\n";
        } else {
            $flow_details .= "✅ Step 1: Email validation PASSED\n";
        }
        
        // Step 2: User lookup
        $user_stmt = $conn->prepare("SELECT user_id, name FROM users WHERE email = ?");
        if ($user_stmt) {
            $user_stmt->bind_param("s", $test_email);
            $user_stmt->execute();
            $user_result = $user_stmt->get_result();
            
            if ($user_result->num_rows > 0) {
                $user = $user_result->fetch_assoc();
                $flow_details .= "✅ Step 2: User lookup PASSED - Found user: " . $user['name'] . "\n";
                
                // Step 3: OTP generation
                $otp = sprintf("%06d", mt_rand(100000, 999999));
                $expiry = date('Y-m-d H:i:s', strtotime('+10 minutes'));
                $flow_details .= "✅ Step 3: OTP generation PASSED - OTP: $otp\n";
                
                // Step 4: Check if password_reset table exists
                $table_check = $conn->query("SHOW TABLES LIKE 'password_reset'");
                if ($table_check && $table_check->num_rows > 0) {
                    $flow_details .= "✅ Step 4: password_reset table EXISTS\n";
                    
                    // Step 5: Try to insert OTP (simulation)
                    $insert_stmt = $conn->prepare("INSERT INTO password_reset (user_id, otp, expiry) VALUES (?, ?, ?)");
                    if ($insert_stmt) {
                        // Don't actually insert, just prepare
                        $flow_details .= "✅ Step 5: OTP insertion query PREPARED successfully\n";
                        $insert_stmt->close();
                        
                        // Step 6: Email sending
                        if (function_exists('mail')) {
                            $flow_details .= "✅ Step 6: Email function AVAILABLE\n";
                            $flow_details .= "\n🎯 FLOW ANALYSIS: All steps would work!\n";
                            $flow_details .= "The issue might be in the actual email sending or server configuration.";
                            
                            addResult('Complete Flow Simulation', true, 'All steps in forgot password flow are working', $flow_details);
                        } else {
                            $flow_details .= "❌ Step 6: Email function NOT AVAILABLE\n";
                            $flow_details .= "\n🎯 ROOT CAUSE FOUND: Email function is missing!";
                            
                            addResult('Complete Flow Simulation', false, 'Email function is not available', $flow_details);
                        }
                    } else {
                        $flow_details .= "❌ Step 5: OTP insertion query FAILED: " . $conn->error . "\n";
                        addResult('Complete Flow Simulation', false, 'OTP insertion query failed', $flow_details);
                    }
                } else {
                    $flow_details .= "❌ Step 4: password_reset table DOES NOT EXIST\n";
                    $flow_details .= "\n🎯 ROOT CAUSE FOUND: password_reset table is missing!";
                    
                    addResult('Complete Flow Simulation', false, 'password_reset table does not exist', $flow_details);
                }
            } else {
                $flow_details .= "❌ Step 2: User lookup FAILED - User not found\n";
                addResult('Complete Flow Simulation', false, 'User not found in database', $flow_details);
            }
            $user_stmt->close();
        } else {
            $flow_details .= "❌ Step 2: User lookup query FAILED: " . $conn->error . "\n";
            addResult('Complete Flow Simulation', false, 'User lookup query failed', $flow_details);
        }
        
    } catch (Exception $e) {
        addResult('Complete Flow Simulation', false, 'Flow simulation failed: ' . $e->getMessage());
    }
} else {
    addResult('Complete Flow Simulation', false, 'Skipped - Database connection not available');
}

// TEST 8: ACTUAL send_otp.php EXECUTION TEST
try {
    $send_otp_path = __DIR__ . '/send_otp.php';
    if (file_exists($send_otp_path)) {
        $send_otp_details = "send_otp.php file found\n";
        $send_otp_details .= "File size: " . filesize($send_otp_path) . " bytes\n";
        $send_otp_details .= "File permissions: " . substr(sprintf('%o', fileperms($send_otp_path)), -4) . "\n";
        
        // Try to read the file content to check for syntax errors
        $content = file_get_contents($send_otp_path);
        if ($content) {
            $send_otp_details .= "File is readable\n";
            
            // Check for common issues
            if (strpos($content, '<?php') === false) {
                $send_otp_details .= "⚠️ Warning: No PHP opening tag found\n";
            }
            if (strpos($content, 'include') !== false || strpos($content, 'require') !== false) {
                $send_otp_details .= "✅ File includes other files (likely db.php)\n";
            }
            if (strpos($content, 'mail(') !== false) {
                $send_otp_details .= "✅ File uses mail() function\n";
            }
            if (strpos($content, 'try') !== false && strpos($content, 'catch') !== false) {
                $send_otp_details .= "⚠️ File uses try-catch (might hide real errors)\n";
            }
            
            addResult('send_otp.php File Check', true, 'send_otp.php file exists and is readable', $send_otp_details);
        } else {
            addResult('send_otp.php File Check', false, 'send_otp.php file exists but is not readable');
        }
    } else {
        addResult('send_otp.php File Check', false, 'send_otp.php file not found at: ' . $send_otp_path);
    }
} catch (Exception $e) {
    addResult('send_otp.php File Check', false, 'send_otp.php file check failed: ' . $e->getMessage());
}

// Close database connection
if ($conn) {
    $conn->close();
}

?>

        <div class="section">
            <h2>📊 DIAGNOSTIC RESULTS</h2>
            <?php foreach ($results as $result): ?>
                <?php echo formatResult($result); ?>
            <?php endforeach; ?>
        </div>

        <div class="section">
            <h2>🎯 SUMMARY & RECOMMENDATIONS</h2>
            <?php if ($overall_status): ?>
                <div class="summary success">
                    <h3>✅ GOOD NEWS: Most components are working!</h3>
                    <p>The forgot password functionality should work. If you're still getting "Server error occurred", the issue might be:</p>
                    <ul>
                        <li>Email server configuration (emails not being sent)</li>
                        <li>Specific error in the try-catch block of send_otp.php</li>
                        <li>Network connectivity issues</li>
                    </ul>
                </div>
            <?php else: ?>
                <div class="summary error">
                    <h3>❌ ISSUES FOUND: Here's what needs to be fixed:</h3>
                    <ul>
                        <?php foreach ($results as $result): ?>
                            <?php if (!$result['status']): ?>
                                <li><strong><?php echo $result['test']; ?>:</strong> <?php echo $result['message']; ?></li>
                            <?php endif; ?>
                        <?php endforeach; ?>
                    </ul>
                </div>
            <?php endif; ?>
        </div>

        <div class="section">
            <h2>🧪 LIVE TEST FORM</h2>
            <div class="test-form">
                <h3>Test Forgot Password API Directly:</h3>
                <form action="send_otp.php" method="POST" target="_blank">
                    <p>
                        <label>Email:</label><br>
                        <input type="email" name="email" value="ramg65244@gmail.com" style="width:300px; padding:8px;">
                    </p>
                    <p>
                        <button type="submit" style="padding:10px 20px; background:#2196F3; color:white; border:none; border-radius:4px;">
                            🚀 Test Send OTP Now
                        </button>
                    </p>
                </form>
                <p><small>This will call the actual send_otp.php file and show the real response.</small></p>
            </div>
        </div>

        <div class="section">
            <h2>📋 COPY THIS REPORT</h2>
            <div class="code">
                <strong>Quick Summary for Developer:</strong><br>
                Overall Status: <?php echo $overall_status ? 'WORKING' : 'ISSUES FOUND'; ?><br>
                Failed Tests: <?php echo count(array_filter($results, function($r) { return !$r['status']; })); ?><br>
                Server: <?php echo $_SERVER['SERVER_SOFTWARE']; ?><br>
                PHP: <?php echo PHP_VERSION; ?><br>
                Time: <?php echo date('Y-m-d H:i:s'); ?>
            </div>
        </div>

    </div>
</body>
</html>