ALTER TABLE usuarios ADD COLUMN display_name VARCHAR(80) NOT NULL DEFAULT '';
UPDATE usuarios SET display_name = username;
ALTER TABLE usuarios ALTER COLUMN display_name DROP DEFAULT;
