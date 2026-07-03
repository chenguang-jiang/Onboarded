-- M-MySQL: durable storage for user-generated learning state.
-- Seed content (chapters/knowledge points/questions) stays in memory (SeedContentRepository).

CREATE TABLE user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  openid VARCHAR(64) NOT NULL,
  unionid VARCHAR(64) NULL,
  exam_date DATE NULL,
  daily_target INT NOT NULL DEFAULT 15,
  reminder_time TIME NULL,
  UNIQUE KEY uk_user_openid (openid)
);

CREATE TABLE user_onboarding_state (
  user_id BIGINT PRIMARY KEY,
  completed TINYINT NOT NULL DEFAULT 0,
  last_step VARCHAR(32) NOT NULL DEFAULT 'WX_LOGIN'
);

CREATE TABLE answer_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  selected_answer VARCHAR(8) NOT NULL,
  is_correct TINYINT NOT NULL,
  duration_sec INT NULL,
  error_reason VARCHAR(64) NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_answer_user_question_time (user_id, question_id, created_at),
  INDEX idx_answer_user_time (user_id, created_at)
);

CREATE TABLE wrong_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  chapter_id BIGINT NOT NULL,
  knowledge_point_id BIGINT NOT NULL,
  wrong_count INT NOT NULL DEFAULT 1,
  mastered TINYINT NOT NULL DEFAULT 0,
  consecutive_correct INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  last_wrong_at DATETIME NOT NULL,
  mastered_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_wrong_user_question (user_id, question_id),
  INDEX idx_wrong_user_chapter_mastered (user_id, chapter_id, mastered),
  INDEX idx_wrong_user_kp (user_id, knowledge_point_id)
);

CREATE TABLE user_mastery (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  knowledge_point_id BIGINT NOT NULL,
  mastery_score INT NOT NULL DEFAULT 0,
  study_count INT NOT NULL DEFAULT 0,
  correct_count INT NOT NULL DEFAULT 0,
  wrong_count INT NOT NULL DEFAULT 0,
  last_review_at DATETIME NULL,
  next_review_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_mastery_user_kp (user_id, knowledge_point_id)
);

CREATE TABLE daily_plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  plan_date DATE NOT NULL,
  total_count INT NOT NULL,
  completed_count INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_daily_plan_user_date (user_id, plan_date)
);

CREATE TABLE daily_plan_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plan_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  knowledge_point_id BIGINT NOT NULL,
  question_id BIGINT NULL,
  source_type VARCHAR(24) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  sort_no INT NOT NULL DEFAULT 0,
  completed_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_plan_item_plan_status (plan_id, status),
  INDEX idx_plan_item_user_kp (user_id, knowledge_point_id)
);

CREATE TABLE ai_chat_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_ai_session_user_time (user_id, updated_at)
);

CREATE TABLE ai_chat_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(16) NOT NULL,
  content MEDIUMTEXT NOT NULL,
  references_json JSON NULL,
  tokens_used INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  INDEX idx_ai_message_session_time (session_id, created_at)
);
