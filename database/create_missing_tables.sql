-- Safe create script for missing tables in BBJDigital_DB
-- Run this in TiDB Cloud Console -> SQL Editor or upload the file

CREATE DATABASE IF NOT EXISTS BBJDigital_DB;
USE BBJDigital_DB;

CREATE TABLE IF NOT EXISTS member_profiles (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT UNIQUE NOT NULL,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  phone VARCHAR(20),
  address VARCHAR(255),
  city VARCHAR(100),
  state VARCHAR(50),
  postal_code VARCHAR(20),
  generated_email VARCHAR(150) UNIQUE,
  joined_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS announcements (
  id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  content TEXT,
  created_by INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS events (
  id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  event_date DATETIME,
  location VARCHAR(200),
  description TEXT,
  created_by INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS sermons (
  id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  file_path VARCHAR(500),
  file_size BIGINT,
  file_type VARCHAR(50),
  uploaded_by INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS verification_tokens (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  token VARCHAR(128) NOT NULL UNIQUE,
  expires_at DATETIME NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  token VARCHAR(128) NOT NULL UNIQUE,
  expires_at DATETIME NOT NULL,
  used BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tabs (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  display_label VARCHAR(100) NOT NULL,
  icon VARCHAR(50),
  visible_to VARCHAR(50) DEFAULT 'all',
  display_order INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes (safe)
CREATE INDEX IF NOT EXISTS idx_announcements_created_by ON announcements(created_by);
CREATE INDEX IF NOT EXISTS idx_announcements_created_at ON announcements(created_at);
CREATE INDEX IF NOT EXISTS idx_events_created_by ON events(created_by);
CREATE INDEX IF NOT EXISTS idx_events_event_date ON events(event_date);
CREATE INDEX IF NOT EXISTS idx_sermons_uploaded_by ON sermons(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_sermons_created_at ON sermons(created_at);
CREATE INDEX IF NOT EXISTS idx_member_profiles_user_id ON member_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_verification_tokens_user_id ON verification_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);

-- Insert default tabs if missing
INSERT INTO tabs (name, display_label, icon, visible_to, display_order)
SELECT 'announcements','Announcements','📢','all',1 WHERE NOT EXISTS (SELECT 1 FROM tabs WHERE name='announcements');
INSERT INTO tabs (name, display_label, icon, visible_to, display_order)
SELECT 'events','Events','📅','all',2 WHERE NOT EXISTS (SELECT 1 FROM tabs WHERE name='events');
INSERT INTO tabs (name, display_label, icon, visible_to, display_order)
SELECT 'sermons','Sermons','🎙️','all',3 WHERE NOT EXISTS (SELECT 1 FROM tabs WHERE name='sermons');
INSERT INTO tabs (name, display_label, icon, visible_to, display_order)
SELECT 'members','Members','👥','all',4 WHERE NOT EXISTS (SELECT 1 FROM tabs WHERE name='members');
INSERT INTO tabs (name, display_label, icon, visible_to, display_order)
SELECT 'profile','Profile','👤','all',5 WHERE NOT EXISTS (SELECT 1 FROM tabs WHERE name='profile');
