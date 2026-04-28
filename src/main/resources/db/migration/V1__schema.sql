-- Flywayのマイグレーションスクリプト
CREATE TABLE users (
  username VARCHAR(100) NOT NULL PRIMARY KEY,
  password VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 予定表テーブルの作成
CREATE TABLE schedule (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  username VARCHAR(100) NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  CONSTRAINT fk_schedule_user
    FOREIGN KEY (username) REFERENCES users (username)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- インデックスの作成
CREATE INDEX idx_schedule_username_starttime
  ON schedule (username, start_time);

-- 予定表テーブルのusername列にインデックスを作成
CREATE INDEX idx_schedule_username
  ON schedule (username);