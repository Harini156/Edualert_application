<?php
// Test File Access - Debug endpoint to check file upload and access
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$response = [];

try {
    // Check uploads directory
    $uploads_dir = __DIR__ . '/uploads/';
    $response['uploads_dir_exists'] = is_dir($uploads_dir);
    $response['uploads_dir_path'] = $uploads_dir;
    
    if (is_dir($uploads_dir)) {
        $files = scandir($uploads_dir);
        $response['files_in_uploads'] = array_filter($files, function($file) {
            return $file !== '.' && $file !== '..';
        });
        $response['total_files'] = count($response['files_in_uploads']);
    } else {
        $response['files_in_uploads'] = [];
        $response['total_files'] = 0;
    }
    
    // Test file access for each file
    $response['file_access_tests'] = [];
    if (!empty($response['files_in_uploads'])) {
        foreach (array_slice($response['files_in_uploads'], 0, 5) as $filename) { // Test first 5 files
            $file_path = $uploads_dir . $filename;
            $response['file_access_tests'][$filename] = [
                'exists' => file_exists($file_path),
                'readable' => is_readable($file_path),
                'size' => file_exists($file_path) ? filesize($file_path) : 0,
                'mime_type' => file_exists($file_path) ? mime_content_type($file_path) : 'unknown'
            ];
        }
    }
    
    // Test get_file.php endpoint
    if (!empty($response['files_in_uploads'])) {
        $test_file = array_values($response['files_in_uploads'])[0];
        $test_url = "uploads/" . $test_file;
        $response['test_file_url'] = $test_url;
        $response['get_file_test_url'] = "get_file.php?file=" . urlencode($test_url);
    }
    
    $response['status'] = 'success';
    $response['message'] = 'File access test completed';
    
} catch (Exception $e) {
    $response['status'] = 'error';
    $response['message'] = 'Test failed: ' . $e->getMessage();
}

echo json_encode($response, JSON_PRETTY_PRINT);
?>