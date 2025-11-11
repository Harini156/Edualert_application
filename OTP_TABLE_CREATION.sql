-- OTP Table for Forgot Password Functionality
-- Execute this SQL query in your database

CREATE TABLE IF NOT EXISTS `password_reset_otps` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `otp` varchar(6) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` timestamp NOT NULL,
  `is_used` tinyint(1) NOT NULL DEFAULT 0,
  `attempts` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `email_index` (`email`),
  KEY `otp_index` (`otp`),
  KEY `expires_index` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add index for better performance
CREATE INDEX idx_email_otp ON password_reset_otps(email, otp);
CREATE INDEX idx_active_otps ON password_reset_otps(email, is_used, expires_at);