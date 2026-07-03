CREATE TABLE app_schema_version_marker (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  marker_key VARCHAR(64) NOT NULL UNIQUE,
  marker_value VARCHAR(128) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO app_schema_version_marker (marker_key, marker_value)
VALUES ('schema', 'm0-foundation');
