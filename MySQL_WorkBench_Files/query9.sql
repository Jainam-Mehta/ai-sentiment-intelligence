USE sentiment_dashboard;

-- Add time preference columns
ALTER TABLE users 
ADD COLUMN report_hour INT DEFAULT 8,  -- 0-23 hour (default 8 AM)
ADD COLUMN report_minute INT DEFAULT 0, -- 0-59 minute
ADD COLUMN report_timezone VARCHAR(50) DEFAULT 'Asia/Kolkata'; -- User's timezone

-- Optional: Add last report sent tracking
ALTER TABLE users 
ADD COLUMN last_report_sent TIMESTAMP NULL;