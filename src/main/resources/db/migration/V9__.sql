CREATE TABLE user_samples_list_followers
(
    followers_id         BIGINT NOT NULL,
    user_samples_list_id BIGINT NOT NULL
);

ALTER TABLE user_samples_list
    ADD is_public BIT(1) NULL;

ALTER TABLE user_samples_list_followers
    ADD CONSTRAINT fk_usesamlisfol_on_user FOREIGN KEY (followers_id) REFERENCES users (id);

ALTER TABLE user_samples_list_followers
    ADD CONSTRAINT fk_usesamlisfol_on_user_samples_list FOREIGN KEY (user_samples_list_id) REFERENCES user_samples_list (id);