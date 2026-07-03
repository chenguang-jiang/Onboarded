CREATE TABLE ai_review_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  message_id BIGINT NOT NULL,
  content MEDIUMTEXT NOT NULL,
  references_json JSON NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_ai_review_user_message (user_id, message_id),
  INDEX idx_ai_review_user_status_time (user_id, status, created_at)
);

CREATE TABLE subscription_preference (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  template_id VARCHAR(128) NOT NULL,
  scene VARCHAR(32) NOT NULL,
  accepted TINYINT NOT NULL,
  status VARCHAR(16) NOT NULL,
  last_decision_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_subscription_user_scene (user_id, scene),
  INDEX idx_subscription_scene_status (scene, status)
);
