<?php
/**
 * TIMEZONE CHECKER - Verify timezone configuration
 * Use this to confirm timezone fix is working
 */

// Set timezone to India
date_default_timezone_set('Asia/Kolkata');

header('Content-Type: text/html; charset=UTF-8');
?>
<!DOCTYPE html>
<html>
<head>
    <title>🕐 Timezone Checker</title>
    <style>
        body { font-family: Arial; margin: 20px; background: #f5f5f5; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
        .header { background: #922381; color: white; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
        .info-box { background: #f8f9fa; padding: 15px; margin: 15px 0; border-radius: 5px; border-left: 4px solid #922381; }
        .success { background: #d4edda; border-left-color: #28a745; }
        .error { background: #f8d7da; border-left-color: #dc3545; }
        .time-display { font-size: 24px; font-weight: bold; color: #922381; margin: 10px 0; }
        table { width: 100%; border-collapse: collapse; margin: 15px 0; }
        th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #922381; color: white; }
        .code { background: #f4f4f4; padding: 10px; border-radius: 3px; font-family: monospace; margin: 10px 0; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🕐 Timezone Configuration Checker</h1>
            <p>Verify that timezone is set correctly to India (IST)</p>
        </div>

        <?php
        // Get PHP timezone info
        $phpTimezone = date_default_timezone_get();
        $phpTime = date('Y-m-d H:i:s');
        $phpDate = date('l, F j, Y');
        $phpHour = date('h:i A');
        
        // Connect to database
        include 'db.php';
        
        // Get MySQL timezone info
        $result = $conn->query("SELECT 
            NOW() as mysql_time,
            CURRENT_TIMESTAMP as current_timestamp,
            @@session.time_zone as session_tz,
            @@global.time_zone as global_tz,
            DATE_FORMAT(NOW(), '%W, %M %e, %Y') as mysql_date,
            DATE_FORMAT(NOW(), '%h:%i %p') as mysql_hour
        ");
        $mysqlInfo = $result->fetch_assoc();
        
        // Check if timezone is correct
        $isCorrect = ($phpTimezone === 'Asia/Kolkata' || $phpTimezone === 'Asia/Calcutta');
        ?>

        <!-- Current Time Display -->
        <div class="info-box <?php echo $isCorrect ? 'success' : 'error'; ?>">
            <h2>📅 Current Date & Time</h2>
            <div class="time-display"><?php echo $phpDate; ?></div>
            <div class="time-display"><?php echo $phpHour; ?></div>
            <p><strong>Full Timestamp:</strong> <?php echo $phpTime; ?></p>
        </div>

        <!-- PHP Timezone Info -->
        <div class="info-box">
            <h3>🐘 PHP Timezone Configuration</h3>
            <table>
                <tr>
                    <th>Setting</th>
                    <th>Value</th>
                    <th>Status</th>
                </tr>
                <tr>
                    <td><strong>Timezone</strong></td>
                    <td><?php echo $phpTimezone; ?></td>
                    <td><?php echo $isCorrect ? '✅ Correct' : '❌ Wrong'; ?></td>
                </tr>
                <tr>
                    <td><strong>Current Time</strong></td>
                    <td><?php echo $phpTime; ?></td>
                    <td>-</td>
                </tr>
                <tr>
                    <td><strong>Expected Timezone</strong></td>
                    <td>Asia/Kolkata (IST)</td>
                    <td>-</td>
                </tr>
            </table>
        </div>

        <!-- MySQL Timezone Info -->
        <div class="info-box">
            <h3>🗄️ MySQL Timezone Configuration</h3>
            <table>
                <tr>
                    <th>Setting</th>
                    <th>Value</th>
                </tr>
                <tr>
                    <td><strong>MySQL NOW()</strong></td>
                    <td><?php echo $mysqlInfo['mysql_time']; ?></td>
                </tr>
                <tr>
                    <td><strong>MySQL Date</strong></td>
                    <td><?php echo $mysqlInfo['mysql_date']; ?></td>
                </tr>
                <tr>
                    <td><strong>MySQL Time</strong></td>
                    <td><?php echo $mysqlInfo['mysql_hour']; ?></td>
                </tr>
                <tr>
                    <td><strong>Session Timezone</strong></td>
                    <td><?php echo $mysqlInfo['session_tz']; ?></td>
                </tr>
                <tr>
                    <td><strong>Global Timezone</strong></td>
                    <td><?php echo $mysqlInfo['global_tz']; ?></td>
                </tr>
            </table>
        </div>

        <!-- Verification -->
        <div class="info-box <?php echo $isCorrect ? 'success' : 'error'; ?>">
            <h3>✅ Verification Result</h3>
            <?php if ($isCorrect): ?>
                <p><strong>✅ SUCCESS!</strong> Timezone is correctly set to India (IST).</p>
                <p>All timestamps will be in Indian Standard Time.</p>
                <p>OTP expiry times will work correctly.</p>
            <?php else: ?>
                <p><strong>❌ WARNING!</strong> Timezone is NOT set to India.</p>
                <p>Current timezone: <code><?php echo $phpTimezone; ?></code></p>
                <p>Expected timezone: <code>Asia/Kolkata</code></p>
                <p><strong>Action Required:</strong> Update db.php to set timezone correctly.</p>
            <?php endif; ?>
        </div>

        <!-- Time Comparison -->
        <div class="info-box">
            <h3>🔍 Time Comparison</h3>
            <p><strong>Compare this with your actual time:</strong></p>
            <ul>
                <li><strong>PHP Time:</strong> <?php echo $phpTime; ?></li>
                <li><strong>MySQL Time:</strong> <?php echo $mysqlInfo['mysql_time']; ?></li>
                <li><strong>Your Device Time:</strong> <span id="deviceTime"></span></li>
            </ul>
            <p><strong>All three should match!</strong> If they don't, there's a timezone issue.</p>
        </div>

        <!-- OTP Test Simulation -->
        <div class="info-box">
            <h3>🧪 OTP Expiry Simulation</h3>
            <p>If OTP is generated now:</p>
            <?php
            $otpGenerated = date('Y-m-d H:i:s');
            $otpExpires = date('Y-m-d H:i:s', strtotime('+30 minutes'));
            ?>
            <table>
                <tr>
                    <th>Event</th>
                    <th>Timestamp</th>
                </tr>
                <tr>
                    <td><strong>OTP Generated</strong></td>
                    <td><?php echo $otpGenerated; ?></td>
                </tr>
                <tr>
                    <td><strong>OTP Expires</strong></td>
                    <td><?php echo $otpExpires; ?></td>
                </tr>
                <tr>
                    <td><strong>Valid Duration</strong></td>
                    <td>30 minutes</td>
                </tr>
            </table>
            <p><strong>Note:</strong> OTP will be valid until <?php echo date('h:i A', strtotime('+30 minutes')); ?></p>
        </div>

        <!-- Instructions -->
        <div class="info-box">
            <h3>📖 How to Use This Checker</h3>
            <ol>
                <li>Check if the displayed time matches your actual time in India</li>
                <li>Verify PHP timezone shows "Asia/Kolkata"</li>
                <li>Verify MySQL time matches PHP time</li>
                <li>If all match, timezone is configured correctly ✅</li>
                <li>If times don't match, contact your system administrator</li>
            </ol>
        </div>

        <!-- Refresh Button -->
        <div style="text-align: center; margin-top: 20px;">
            <button onclick="location.reload()" style="background: #922381; color: white; padding: 10px 30px; border: none; border-radius: 5px; cursor: pointer; font-size: 16px;">
                🔄 Refresh Time
            </button>
        </div>
    </div>

    <script>
        // Display device time
        function updateDeviceTime() {
            const now = new Date();
            const year = now.getFullYear();
            const month = String(now.getMonth() + 1).padStart(2, '0');
            const day = String(now.getDate()).padStart(2, '0');
            const hours = String(now.getHours()).padStart(2, '0');
            const minutes = String(now.getMinutes()).padStart(2, '0');
            const seconds = String(now.getSeconds()).padStart(2, '0');
            
            const timeString = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
            document.getElementById('deviceTime').textContent = timeString;
        }
        
        updateDeviceTime();
        setInterval(updateDeviceTime, 1000);
    </script>
</body>
</html>
