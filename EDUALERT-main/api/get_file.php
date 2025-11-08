<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $file_path = isset($_GET['file']) ? $_GET['file'] : '';
    
    if (empty($file_path)) {
        http_response_code(400);
        echo json_encode(['error' => 'File parameter is required']);
        exit;
    }
    
    // Security: Only allow files from uploads directory
    if (!str_contains($file_path, 'uploads/')) {
        http_response_code(403);
        echo json_encode(['error' => 'Access denied']);
        exit;
    }
    
    // Clean the path - remove any api/ prefix if present
    $clean_path = str_replace('api/', '', $file_path);
    $full_path = __DIR__ . '/' . $clean_path;
    
    // Check if file exists
    if (!file_exists($full_path)) {
        http_response_code(404);
        echo json_encode(['error' => 'File not found', 'path' => $clean_path]);
        exit;
    }
    
    // Get file info
    $file_info = pathinfo($full_path);
    $file_extension = strtolower($file_info['extension']);
    
    // Set appropriate content type for ALL supported file types
    $content_types = [
        'pdf' => 'application/pdf',
        'doc' => 'application/msword',
        'docx' => 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'xls' => 'application/vnd.ms-excel',
        'xlsx' => 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'ppt' => 'application/vnd.ms-powerpoint',
        'pptx' => 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
        'jpg' => 'image/jpeg',
        'jpeg' => 'image/jpeg',
        'png' => 'image/png',
        'gif' => 'image/gif',
        'bmp' => 'image/bmp',
        'tiff' => 'image/tiff',
        'svg' => 'image/svg+xml',
        'webp' => 'image/webp',
        'txt' => 'text/plain',
        'csv' => 'text/csv',
        'rtf' => 'application/rtf',
        'zip' => 'application/zip',
        'rar' => 'application/x-rar-compressed',
        '7z' => 'application/x-7z-compressed',
        'tar' => 'application/x-tar',
        'gz' => 'application/gzip',
        'mp4' => 'video/mp4',
        'avi' => 'video/x-msvideo',
        'mov' => 'video/quicktime',
        'mp3' => 'audio/mpeg',
        'wav' => 'audio/wav',
        'json' => 'application/json',
        'xml' => 'application/xml',
        'html' => 'text/html',
        'css' => 'text/css',
        'js' => 'application/javascript',
        'md' => 'text/markdown',
        'log' => 'text/plain'
    ];
    
    $content_type = isset($content_types[$file_extension]) ? $content_types[$file_extension] : 'application/octet-stream';
    
    // Set headers for both viewing and downloading
    header('Content-Type: ' . $content_type);
    header('Content-Length: ' . filesize($full_path));
    
    // Enable both inline viewing AND download
    $download_param = isset($_GET['download']) ? $_GET['download'] : 'false';
    if ($download_param === 'true') {
        header('Content-Disposition: attachment; filename="' . basename($full_path) . '"');
    } else {
        header('Content-Disposition: inline; filename="' . basename($full_path) . '"');
    }
    
    header('Cache-Control: public, max-age=3600');
    header('Accept-Ranges: bytes');
    
    // Output file
    readfile($full_path);
    exit;
    
} else {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
}
?>