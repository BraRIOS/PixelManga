CREATE TABLE chapters_images
(
    chapter_id BIGINT NOT NULL,
    image      VARCHAR(255) NULL
);

ALTER TABLE chapters_images
    ADD CONSTRAINT fk_chapters_images_on_chapter FOREIGN KEY (chapter_id) REFERENCES chapters (id);

ALTER TABLE chapters
DROP
COLUMN image;