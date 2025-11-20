# 🚀 PHPMailer Quick Start Guide

## ⚡ 5-Minute Setup

### Step 1: Get Gmail App Password (2 minutes)
1. Visit: https://myaccount.google.com/security
2. Enable "2-Step Verification"
3. Click "App passwords"
4. Generate for: Mail → Other (EduAlert)
5. **Copy the 16-character password**

### Step 2: Upload Files (1 minute)
Upload these 5 files to `/api/` folder:
- ✅ PHPMailer.php
- ✅ SMTP.php
- ✅ Exception.php
- ✅ send_otp.php (replace existing)
- ✅ reset_password.php (replace existing)

### Step 3: Configure (1 minute)
Edit **send_otp.php** (line ~138):
```php
$mail->Password = 'your_app_password_here';  // ← Paste your 16-char password
```

Edit **reset_password.php** (line ~138):
```php
$mail->Password = 'your_app_password_here';  // ← Paste your 16-char password
```

### Step 4: Test (1 minute)
1. Open your Android app
2. Click "Forgot Password"
3. Enter your email
4. Check inbox for OTP
5. Enter OTP and new password
6. Check inbox for confirmation

## ✅ Done!

Your forgot password feature is now working with professional Gmail delivery!

---

## 📋 File Locations

```
EDUALERT-main/
└── api/
    ├── PHPMailer.php          ← NEW
    ├── SMTP.php               ← NEW
    ├── Exception.php          ← NEW
    ├── send_otp.php           ← UPDATED
    ├── reset_password.php     ← UPDATED
    └── db.php                 ← EXISTING (no changes)
```

---

## 🔑 Configuration Summary

**Gmail Account:** edualert.notifications@gmail.com  
**SMTP Server:** smtp.gmail.com  
**Port:** 587  
**Encryption:** STARTTLS (TLS)  
**Authentication:** Gmail App Password (16 characters)

---

## 🐛 Quick Troubleshooting

**Email not sending?**
→ Check Gmail App Password is correct (no spaces)

**Email not received?**
→ Check spam folder, wait 1-2 minutes

**Authentication failed?**
→ Regenerate Gmail App Password

**Need detailed help?**
→ Read `PHPMAILER_DEPLOYMENT_GUIDE.md`

---

## 📞 Support Files

- **Full Guide:** `PHPMAILER_DEPLOYMENT_GUIDE.md`
- **Complete Summary:** `PHPMAILER_IMPLEMENTATION_COMPLETE.md`
- **Test Script:** `test_phpmailer_setup.php`

---

## 🎉 Success Checklist

- [ ] Gmail App Password created
- [ ] All 5 files uploaded
- [ ] Password configured in both files
- [ ] Test email received
- [ ] OTP email received
- [ ] Password reset works
- [ ] Confirmation email received

**All checked? You're ready for production!** 🚀
