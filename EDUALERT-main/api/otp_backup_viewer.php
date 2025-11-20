<?php
// OTP Backup Viewer - Admin can check OTPs if email fails
header('Content-Type: text/html; charset=UTF-8');

?>
<!DOCTYPE html>
<html>
<head>
    <title>🔐 OTP Backup Viewer</title>
    <style>
        body { font-family: Arial; margin: 20px; background: #f5f5f5; }
        .container { max-width: 1000px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
        .header { background: #922381; color: white; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
        .otp-entry { background: #f8f9fa; padding: 15px; margin: 10px 0; border-radius: 5px; border-left: 4px solid #922381; }
        .otp-code { font-size: 24px; font-weight: bold; color: #922381; background: white; padding: 10px; border-radius: 5px; display: inline-block; }
        .expired { opacity: 0.5; background: #ffebee; border-left-color: #f44336; }
        .clear-btn { background: #f44336; color: white; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; }
        .refresh-btn { background: #4caf50; color: white; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; margin-left: 10px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔐 OTP Backup Viewer</h1>
            <p>Admin tool to view OTPs when email delivery fails</p>
        </div>

        <div style="margin-bottom: 20px;">
            <button class="refresh-btn" onclick="location.reload()">🔄 Refresh</button>
            <button class="clear-btn" onclick="clearOtps()">🗑️ Clear Old OTPs</button>
        </div>

        <?php
        $logFile = __DIR__ . '/otp_backup.txt';
        
        if (file_exists($logFile)) {
            $otps = file($logFile, FILE_IGNORE_NEW_LINES);
            $otps = array_reverse($otps); // Show newest first
            
            if (empty($otps)) {
                echo "<div class='otp-entry'><p>No OTPs found. This is good - means email delivery is working!</p></div>";
            } else {
                echo "<h2>📋 Recent OTPs (Newest First):</h2>";
                
                foreach ($otps as $otp_line) {
                    if (empty(trim($otp_line))) continue;
                    
                    // Parse the log entry
                    if (preg_match('/\[(.*?)\] Email: (.*?) \| Name: (.*?) \| OTP: (\d{6}) \| Expires: (.*?)$/', $otp_line, $matches)) {
                        $timestamp = $matches[1];
                        $email = $matches[2];
                        $name = $matches[3];
                        $otp = $matches[4];
                        $expires = $matches[5];
                        
                        // Check if expired
                        $isExpired = strtotime($expires) < time();
                        $expiredClass = $isExpired ? 'expired' : '';
                        
                        echo "<div class='otp-entry $expiredClass'>";
                        echo "<div style='display: flex; justify-content: space-between; align-items: center;'>";
                        echo "<div>";
                        echo "<strong>Email:</strong> $email<br>";
                        echo "<strong>Name:</strong> $name<br>";
                        echo "<strong>Generated:</strong> $timestamp<br>";
                        echo "<strong>Expires:</strong> $expires";
                        if ($isExpired) {
                            echo " <span style='color: #f44336; font-weight: bold;'>(EXPIRED)</span>";
                        }
                        echo "</div>";
                        echo "<div class='otp-code'>$otp</div>";
                        echo "</div>";
                        echo "</div>";
                    }
                }
            }
        } else {
            echo "<div class='otp-entry'><p>No OTP backup file found. This means no OTPs have been generated yet.</p></div>";
        }
        ?>

        <div style="margin-top: 30px; padding: 15px; background: #e3f2fd; border-radius: 5px;">
            <h3>📖 How to Use:</h3>
            <ol>
                <li><strong>Normal Operation:</strong> Users should receive OTPs via email</li>
                <li><strong>If Email Fails:</strong> Check this page for the user's OTP</li>
                <li><strong>Give OTP to User:</strong> User can use the OTP to reset password</li>
                <li><strong>Clean Up:</strong> Click "Clear Old OTPs" to remove expired entries</li>
            </ol>
            
            <h3>🔒 Security Note:</h3>
            <p>This page should only be accessible to administrators. OTPs are logged here as a backup when email delivery fails.</p>
        </div>
    </div>

    <script>
    function clearOtps() {
        if (confirm('Are you sure you want to clear all old OTPs?')) {
            fetch('otp_backup_clear.php', {method: 'POST'})
            .then(() => location.reload())
            .catch(() => alert('Failed to clear OTPs'));
        }
    }
    </script>
</body>
</html>