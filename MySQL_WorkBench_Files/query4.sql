DELIMITER $$

CREATE EVENT IF NOT EXISTS update_hourly_stats
ON SCHEDULE EVERY 1 HOUR
STARTS CURRENT_TIMESTAMP
DO
BEGIN
    INSERT INTO hourly_stats (keyword, hour, positive_count, neutral_count, negative_count, total_count)
    SELECT 
        keyword,
        DATE_FORMAT(created_at, '%Y-%m-%d %H:00:00') as hour,
        SUM(CASE WHEN sentiment = 'positive' THEN 1 ELSE 0 END) as positive_count,
        SUM(CASE WHEN sentiment = 'neutral' THEN 1 ELSE 0 END) as neutral_count,
        SUM(CASE WHEN sentiment = 'negative' THEN 1 ELSE 0 END) as negative_count,
        COUNT(*) as total_count
    FROM posts
    WHERE created_at >= NOW() - INTERVAL 1 HOUR
    GROUP BY keyword, DATE_FORMAT(created_at, '%Y-%m-%d %H:00:00')
    ON DUPLICATE KEY UPDATE
        positive_count = VALUES(positive_count),
        neutral_count = VALUES(neutral_count),
        negative_count = VALUES(negative_count),
        total_count = VALUES(total_count);
END$$

DELIMITER ;