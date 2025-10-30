# EDUALERT DEPLOYMENT INSTRUCTIONS

## 🚀 Complete Deployment Package

This package contains:
1. **Updated project files** with all bug fixes
2. **Database SQL file** (edualert.sql) 
3. **Server test file** for verification

## 📋 Deployment Steps

### Step 1: Backup Current Setup
```bash
# Backup current files
cp -r /path/to/current/edualert /path/to/backup/edualert_backup_$(date +%Y%m%d)

# Backup current database
mysqldump -u root -p edualert > edualert_backup_$(date +%Y%m%d).sql
```

### Step 2: Deploy New Files
```bash
# Extract new project files
unzip edualert_updated.zip
cp -r EDUALERT-main/* /path/to/web/directory/edualert/
```

### Step 3: Database Setup
```bash
# Import the working database
mysql -u root -p edualert < edualert.sql

# OR if database doesn't exist:
mysql -u root -p -e "CREATE DATABASE edualert;"
mysql -u root -p edualert < edualert.sql
```

### Step 4: File Permissions
```bash
# Set proper permissions
chmod 755 /path/to/edualert/api/
chmod 644 /path/to/edualert/api/*.php
chmod 777 /path/to/edualert/api/uploads/
```

### Step 5: Database Configuration
Edit `api/db.php` if needed:
```php
$host = "localhost";     // Your database host
$user = "your_db_user";  // Your database username  
$pass = "your_db_pass";  // Your database password
$dbname = "edualert";    // Database name
```

## 🧪 Testing After Deployment

### Test 1: Server Status
Visit: `http://your-domain/edualert/api/test_server.php`
**Expected:** JSON response with "Server is working correctly!"

### Test 2: Database Connection
Check the test_server.php response for:
```json
{
  "status": "success",
  "database": "Connected successfully"
}
```

### Test 3: Login Functionality
Try logging in with the mobile app.
**Expected:** No more "MalformedJsonException" errors

## 🔧 Fixes Included

### 1. Login Issue Fixed
- **Problem:** Server returning HTML errors instead of JSON
- **Solution:** Added error suppression and proper JSON handling
- **Files:** `api/login.php`, `api/db.php`

### 2. Duplicate Messages Fixed
- **Problem:** Admin messages being sent twice
- **Solution:** Server-side duplicate prevention
- **Files:** `api/adminsendmsg.php`

### 3. UI Issues Fixed
- **Problem:** Various Android app UI issues
- **Solution:** Enhanced error handling and debug capabilities
- **Files:** Android app files

## 🆘 Troubleshooting

### If login still fails:
1. Check `test_server.php` response
2. Verify database connection in test response
3. Check PHP error logs: `/var/log/apache2/error.log`
4. Ensure all files uploaded correctly

### If database issues:
1. Verify database name is "edualert"
2. Check user permissions for database
3. Import the provided SQL file
4. Update `db.php` with correct credentials

## 📞 Support
If issues persist, provide:
1. Response from `test_server.php`
2. PHP error logs
3. Database connection status
4. Any error messages from mobile app

## ✅ Success Indicators
- ✅ `test_server.php` shows "success" status
- ✅ Database shows "Connected successfully"  
- ✅ Mobile app login works without JSON errors
- ✅ Admin messages send only once (no duplicates)
- ✅ All app features working normally