<?php
// Clear old OTPs from backup file
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $logFile = __DIR__ . '/otp_backup.txt';
    
    if (file_exists($logFile)) {
        // Keep only non-expired OTPs
        $otps = file($logFile, FILE_IGNORE_NEW_LINES);
        $validOtps = [];
        
        foreach ($otps as $otp_line) {
            if (empty(trim($otp_line))) continue;
            
            // Parse the log entry
            if (preg_match('/\[(.*?)\] Email: (.*?) \| Name: (.*?) \| OTP: (\d{6}) \| Expires: (.*?)$/', $otp_line, $matches)) {
                $expires = $matches[5];
                
                // Keep only non-expired OTPs
                if (strtotime($expires) >= time()) {
                    $validOtps[] = $otp_line;
                }
            }
        }
        
        // Write back only valid OTPs
        file_put_contents($logFile, implode("\n", $validOtps) . "\n");
        
        echo json_encode(['status' => 'success', 'message' => 'Old OTPs cleared']);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'No backup file found']);
    }
} else {
    echo json_encode(['status' => 'error', 'message' => 'Invalid request method']);
}
?>