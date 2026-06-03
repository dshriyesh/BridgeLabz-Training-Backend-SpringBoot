CREATE DATABASE IF NOT EXISTS fundoo_notes;
USE fundoo_notes;

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) UNIQUE NOT NULL,
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  created_date DATETIME,
  updated_date DATETIME
);

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  email VARCHAR(150) UNIQUE NOT NULL,
  username VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  profile_image VARCHAR(255),
  account_status VARCHAR(20) NOT NULL,
  email_verified BIT NOT NULL,
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  created_date DATETIME,
  updated_date DATETIME
);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS labels (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  user_id BIGINT NOT NULL,
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  created_date DATETIME,
  updated_date DATETIME,
  CONSTRAINT fk_labels_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS notes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  color VARCHAR(100),
  is_pinned BIT NOT NULL,
  is_archived BIT NOT NULL,
  is_trashed BIT NOT NULL,
  reminder_time DATETIME,
  trashed_at DATETIME,
  user_id BIGINT NOT NULL,
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  created_date DATETIME,
  updated_date DATETIME,
  CONSTRAINT fk_notes_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS note_labels (
  note_id BIGINT NOT NULL,
  label_id BIGINT NOT NULL,
  PRIMARY KEY (note_id, label_id),
  CONSTRAINT fk_note_labels_note FOREIGN KEY (note_id) REFERENCES notes(id),
  CONSTRAINT fk_note_labels_label FOREIGN KEY (label_id) REFERENCES labels(id)
);

CREATE TABLE IF NOT EXISTS attachments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  file_name VARCHAR(255) NOT NULL,
  file_type VARCHAR(100) NOT NULL,
  file_path VARCHAR(500) NOT NULL,
  note_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  created_date DATETIME,
  updated_date DATETIME,
  CONSTRAINT fk_attachments_note FOREIGN KEY (note_id) REFERENCES notes(id),
  CONSTRAINT fk_attachments_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  token VARCHAR(512) UNIQUE NOT NULL,
  expiry_date DATETIME NOT NULL,
  user_id BIGINT NOT NULL,
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  created_date DATETIME,
  updated_date DATETIME,
  CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS email_verification_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  token VARCHAR(512) UNIQUE NOT NULL,
  expiry_date DATETIME NOT NULL,
  user_id BIGINT UNIQUE NOT NULL,
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  created_date DATETIME,
  updated_date DATETIME,
  CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS reminders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  note_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  reminder_time DATETIME NOT NULL,
  notified BIT NOT NULL,
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  created_date DATETIME,
  updated_date DATETIME,
  CONSTRAINT fk_reminders_note FOREIGN KEY (note_id) REFERENCES notes(id),
  CONSTRAINT fk_reminders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS in_app_notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  message VARCHAR(512) NOT NULL,
  is_read BIT NOT NULL,
  created_by VARCHAR(100),
  updated_by VARCHAR(100),
  created_date DATETIME,
  updated_date DATETIME,
  CONSTRAINT fk_in_app_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_notes_user ON notes(user_id);
CREATE INDEX idx_notes_trashed_at ON notes(trashed_at);
CREATE INDEX idx_note_labels_label ON note_labels(label_id);
CREATE INDEX idx_reminders_time_notified ON reminders(reminder_time, notified);

INSERT IGNORE INTO roles(name, created_by, updated_by, created_date, updated_date)
VALUES ('ROLE_USER', 'SYSTEM', 'SYSTEM', NOW(), NOW()), ('ROLE_ADMIN', 'SYSTEM', 'SYSTEM', NOW(), NOW());
