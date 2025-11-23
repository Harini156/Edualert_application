<?php
/**
 * COMPLETE OTP SYSTEM TEST
 * Tests both email sending and OTP validation
 * Use this to verify the fix works 100%
 */

header('Content-Type: text/html; charset=UTF-8');
?>
<!DOCTYPE html>
<html>
<head>
    <title>🧪 Complete OTP System Test</title>
    <style>
        body { font-family: Arial; margin: 20px; background: #f5f5f5; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
        .header { background: #922381; color: white; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
        .test-section { background: #f8f9fa; padding: 15px; margin: 15px 0; border-radius: 5px; border-left: 4px solid #922381; }
        .success { background: #d4edda; border-left-color: #28a745; color: #155724; }
        .error { background: #f8d7da; border-left-color: #dc3545; color: #721c24; }
        .warning { background: #fff3cd; border-left-color: #ffc107; color: #856404; }
        input, button { padding: 10px; margin: 5px 0; width: 100%; box-sizing: border-box; }
        button { background: #922381; color: white; border: none; border-radius: 5px; cursor: pointer; font-size: 16px; }
        button:hover { background: #7a1d6b; }
        .code { background: #f4f4f4; padding: 10px; border-radius: 3px; font-family: monospace; margin: 10px 0; }
        .step { margin: 20px 0; padding: 15px; background: white; border: 1px solid #ddd; border-radius: 5px; }
        .step-number { background: #922381; color: white; width: 30px; height: 30px; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; font-weight: bold; margin-right: 10px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🧪 Complete OTP System Test</h1>
            <p>Test the fixed email sending and OTP validation</p>
        </div>

        <div class="test-section warning">
            <h3>⚠️ Before You Start:</h3>
            <ol>
                <li>Make sure your database is running (XAMPP MySQL)</li>
                <li>Have a valid email address ready (registered in the system)</li>
                <li>Check your email inbox (including spam folder)</li>
            </ol>
        </div>

        <!-- STEP 1: Send OTP -->
        <div class="step">
            <h2><span class="step-number">1</span> Send OTP Email</h2>
            <form id="sendOtpForm">
                <label><strong>Email Address:</strong></label>
                <input type="email" id="email" name="email" placeholder="Enter registered email" required>
                <button type="submit">📧 Send OTP</button>
            </form>
            <div id="sendResult"></div>
        </div>

        <!-- STEP 2: Check Email -->
        <div class="step">
            <h2><span class="step-number">2</span> Check Your Email</h2>
            <div class="test-section">
                <p><strong>What to look for:</strong></p>
                <ul>
                    <li>✅ Email subject: "EduAlert - Password Reset OTP"</li>
                    <li>✅ Email body should show: "Dear [Your Name]"</li>
                    <li>✅ Large OTP number displayed prominently</li>
                    <li>✅ Security information and instructions</li>
                    <li>❌ NO "noname" attachment</li>
                    <li>❌ NO empty email body</li>
                </ul>
                <p><strong>If email doesn't arrive:</strong> Check the OTP Backup Viewer</p>
                <a href="otp_backup_viewer.php" target="_blank" style="display: inline-block; padding: 10px 20px; background: #4caf50; color: white; text-decoration: none; border-radius: 5px; margin-top: 10px;">
                    🔐 Open OTP Backup Viewer
                </a>
            </div>
        </div>

        <!-- STEP 3: Reset Password -->
        <div class="step">
            <h2><span class="step-number">3</span> Reset Password with OTP</h2>
            <form id="resetPasswordForm">
                <label><strong>Email Address:</strong></label>
                <input type="email" id="resetEmail" name="email" placeholder="Same email as above" required>
                
                <label><strong>OTP Code:</strong></label>
                <input type="text" id="otp" name="otp" placeholder="Enter 6-digit OTP" maxlength="6" required>
                
                <label><strong>New Password:</strong></label>
                <input type="password" id="newPassword" name="new_password" placeholder="Enter new password (min 6 characters)" required>
                
                <button type="submit">🔒 Reset Password</button>
            </form>
            <div id="resetResult"></div>
        </div>

        <!-- STEP 4: Verify -->
        <div class="step">
            <h2><span class="step-number">4</span> Verification Checklist</h2>
            <div class="test-section">
                <h4>✅ Success Criteria:</h4>
                <ul>
                    <li>Email received with proper HTML content (not empty)</li>
                    <li>OTP visible in email body (not as attachment)</li>
                    <li>OTP validation successful</li>
                    <li>Password reset successful</li>
                    <li>Can login with new password</li>
                </ul>
            </div>
        </div>

        <!-- Debug Information -->
        <div class="test-section">
            <h3>🔧 Debug Information</h3>
            <div id="debugInfo">
                <p><strong>Server Time:</strong> <?php echo date('Y-m-d H:i:s'); ?></p>
                <p><strong>PHP Version:</strong> <?php echo phpversion(); ?></p>
                <p><strong>OpenSSL:</strong> <?php echo extension_loaded('openssl') ? '✅ Enabled' : '❌ Disabled'; ?></p>
                <p><strong>mbstring:</strong> <?php echo extension_loaded('mbstring') ? '✅ Enabled' : '❌ Disabled'; ?></p>
            </div>
        </div>
    </div>

    <script>
        // Send OTP Form
        document.getElementById('sendOtpForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const resultDiv = document.getElementById('sendResult');
            resultDiv.innerHTML = '<p>⏳ Sending OTP...</p>';
            
            const formData = new FormData(e.target);
            
            try {
                const response = await fetch('send_otp.php', {
                    method: 'POST',
                    body: formData
                });
                
                const data = await response.json();
                
                if (data.status === 'success') {
                    resultDiv.innerHTML = `
                        <div class="test-section success">
                            <h4>✅ Success!</h4>
                            <p>${data.message}</p>
                            <p><strong>Next:</strong> Check your email inbox (and spam folder) for the OTP.</p>
                        </div>
                    `;
                    // Auto-fill email in reset form
                    document.getElementById('resetEmail').value = document.getElementById('email').value;
                } else {
                    resultDiv.innerHTML = `
                        <div class="test-section error">
                            <h4>❌ Error</h4>
                            <p>${data.message}</p>
                        </div>
                    `;
                }
            } catch (error) {
                resultDiv.innerHTML = `
                    <div class="test-section error">
                        <h4>❌ Network Error</h4>
                        <p>${error.message}</p>
                    </div>
                `;
            }
        });

        // Reset Password Form
        document.getElementById('resetPasswordForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const resultDiv = document.getElementById('resetResult');
            resultDiv.innerHTML = '<p>⏳ Resetting password...</p>';
            
            const formData = new FormData(e.target);
            
            try {
                const response = await fetch('reset_password.php', {
                    method: 'POST',
                    body: formData
                });
                
                const data = await response.json();
                
                if (data.status === 'success') {
                    resultDiv.innerHTML = `
                        <div class="test-section success">
                            <h4>✅ Success!</h4>
                            <p>${data.message}</p>
                            <p><strong>Test Complete!</strong> You can now login with your new password.</p>
                        </div>
                    `;
                } else {
                    resultDiv.innerHTML = `
                        <div class="test-section error">
                            <h4>❌ Error</h4>
                            <p>${data.message}</p>
                            <p><strong>Troubleshooting:</strong></p>
                            <ul>
                                <li>Make sure you entered the correct OTP</li>
                                <li>Check if OTP has expired (valid for 10 minutes)</li>
                                <li>Try requesting a new OTP</li>
                                <li>Check OTP Backup Viewer for the correct OTP</li>
                            </ul>
                        </div>
                    `;
                }
            } catch (error) {
                resultDiv.innerHTML = `
                    <div class="test-section error">
                        <h4>❌ Network Error</h4>
                        <p>${error.message}</p>
                    </div>
                `;
            }
        });
    </script>
</body>
</html>
