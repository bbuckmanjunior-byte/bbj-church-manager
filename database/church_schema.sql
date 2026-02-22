-- Schema for BBJ Digital Church Manager - TiDB Cloud
-- Database: BBJDigital_DB
-- Compatible with: TiDB Cloud, MySQL 8.0+, Render

DROP DATABASE IF EXISTS BBJDigital_DB;
CREATE DATABASE BBJDigital_DB;
USE BBJDigital_DB;

CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(150) UNIQUE,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  gender VARCHAR(50),
  phone VARCHAR(20),
  address VARCHAR(255),
  profile_picture VARCHAR(500),
  role ENUM('admin','member') DEFAULT 'member',
  profile_complete BOOLEAN DEFAULT FALSE,
  email_verified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE member_profiles (
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

CREATE TABLE announcements (
  id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  content TEXT,
  created_by INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE events (
  id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  event_date DATETIME,
  location VARCHAR(200),
  description TEXT,
  created_by INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE sermons (
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

CREATE TABLE verification_tokens (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  token VARCHAR(128) NOT NULL UNIQUE,
  expires_at DATETIME NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  token VARCHAR(128) NOT NULL UNIQUE,
  expires_at DATETIME NOT NULL,
  used BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE tabs (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  display_label VARCHAR(100) NOT NULL,
  icon VARCHAR(50),
  visible_to VARCHAR(50) DEFAULT 'all',
  display_order INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, password, email, first_name, last_name, gender, role, profile_complete, email_verified)
VALUES ('admin@bbj.com', SHA2('admin123',256), 'admin@bbj.com', 'Admin', 'User', 'Other', 'admin', TRUE, TRUE);

-- Create indexes for better query performance on TiDB Cloud
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_announcements_created_by ON announcements(created_by);
CREATE INDEX idx_announcements_created_at ON announcements(created_at);
CREATE INDEX idx_events_created_by ON events(created_by);
CREATE INDEX idx_events_event_date ON events(event_date);
CREATE INDEX idx_sermons_uploaded_by ON sermons(uploaded_by);
CREATE INDEX idx_sermons_created_at ON sermons(created_at);
CREATE INDEX idx_member_profiles_user_id ON member_profiles(user_id);
CREATE INDEX idx_verification_tokens_user_id ON verification_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);

INSERT INTO tabs (name, display_label, icon, visible_to, display_order) VALUES
('announcements', 'Announcements', '📢', 'all', 1),
('events', 'Events', '📅', 'all', 2),
('sermons', 'Sermons', '🎙️', 'all', 3),
('members', 'Members', '👥', 'all', 4),
('profile', 'Profile', '👤', 'all', 5);
