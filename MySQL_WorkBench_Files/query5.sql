USE sentiment_dashboard;

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    full_name VARCHAR(100),
    role ENUM('admin', 'user') DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_username (username)
);

CREATE TABLE IF NOT EXISTS user_keywords (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    keyword VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_keyword (user_id, keyword),
    INDEX idx_user_keywords (user_id, keyword)
);

ALTER TABLE posts ADD COLUMN user_id INT NULL;
ALTER TABLE posts ADD FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE hourly_stats ADD COLUMN user_id INT NULL;
ALTER TABLE hourly_stats ADD FOREIGN KEY (user_id) REFERENCES users(id);

CREATE TABLE IF NOT EXISTS user_sessions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (session_token),
    INDEX idx_user_sessions (user_id)
);

-- (password: admin123)
INSERT INTO users (username, password, email, full_name, role) 
VALUES ('admin', '$2a$10$YourHashedPasswordHere', 'admin@localhost', 'Administrator', 'admin')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO users (username, password, email, full_name) VALUES 
('demo1', '$2a$10$YourHashedPasswordHere', 'demo1@example.com', 'Demo User 1'),
('demo2', '$2a$10$YourHashedPasswordHere', 'demo2@example.com', 'Demo User 2')
ON DUPLICATE KEY UPDATE id=id;

-- Sample keywords for demo users
INSERT INTO user_keywords (user_id, keyword) VALUES 
(1, 'technology'),
(1, 'ai'),
(2, 'sports'),
(2, 'music')
ON DUPLICATE KEY UPDATE id=id;

DELIMITER $$

CREATE PROCEDURE update_hourly_stats()
BEGIN
    INSERT INTO hourly_stats (keyword, hour, positive_count, neutral_count, negative_count, total_count, user_id)
    SELECT 
        p.keyword,
        DATE_FORMAT(p.created_at, '%Y-%m-%d %H:00:00') as hour,
        SUM(CASE WHEN p.sentiment = 'positive' THEN 1 ELSE 0 END) as positive_count,
        SUM(CASE WHEN p.sentiment = 'neutral' THEN 1 ELSE 0 END) as neutral_count,
        SUM(CASE WHEN p.sentiment = 'negative' THEN 1 ELSE 0 END) as negative_count,
        COUNT(*) as total_count,
        p.user_id
    FROM posts p
    WHERE p.created_at >= NOW() - INTERVAL 1 HOUR
    GROUP BY p.user_id, p.keyword, hour
    ON DUPLICATE KEY UPDATE
        positive_count = VALUES(positive_count),
        neutral_count = VALUES(neutral_count),
        negative_count = VALUES(negative_count),
        total_count = VALUES(total_count);
END$$

CREATE EVENT IF NOT EXISTS hourly_stats_event
ON SCHEDULE EVERY 1 HOUR
STARTS CURRENT_TIMESTAMP
DO
    CALL update_hourly_stats()$$

DELIMITER ;