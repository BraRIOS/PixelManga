ALTER TABLE samples
DROP
COLUMN cover_path;

ALTER TABLE chapters
DROP
COLUMN image_path;

ALTER TABLE samples
DROP
KEY uc_samples_name;