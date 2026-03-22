USE sentiment_dashboard;

ALTER TABLE users 
ADD COLUMN receive_reports BOOLEAN DEFAULT TRUE,
ADD COLUMN report_frequency ENUM('daily', 'weekly', 'monthly') DEFAULT 'daily',
ADD COLUMN report_format ENUM('html', 'text') DEFAULT 'html';

ALTER TABLE users 
ADD COLUMN last_report_sent TIMESTAMP NULL;

UPDATE users SET 
    receive_reports = TRUE,
    report_frequency = 'daily',
    report_format = 'html'
WHERE receive_reports IS NULL;

DESCRIBE users;