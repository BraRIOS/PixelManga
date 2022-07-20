CREATE TABLE users_followed_samples
(
    samples_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    CONSTRAINT pk_users_followed_samples PRIMARY KEY (samples_id, user_id)
);

CREATE TABLE users_viewed_chapters
(
    chapters_id BIGINT NOT NULL,
    user_id     BIGINT NOT NULL
);

ALTER TABLE user_samples_list
    ADD cover VARCHAR(255) NULL;

ALTER TABLE users_followed_samples
    ADD CONSTRAINT fk_usefolsam_on_sample FOREIGN KEY (samples_id) REFERENCES samples (id);

ALTER TABLE users_followed_samples
    ADD CONSTRAINT fk_usefolsam_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE users_viewed_chapters
    ADD CONSTRAINT fk_useviecha_on_chapter FOREIGN KEY (chapters_id) REFERENCES chapters (id);

ALTER TABLE users_viewed_chapters
    ADD CONSTRAINT fk_useviecha_on_user FOREIGN KEY (user_id) REFERENCES users (id);