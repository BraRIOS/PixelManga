CREATE TABLE rate
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    rate INT NOT NULL,
    CONSTRAINT pk_rate PRIMARY KEY (id)
);

CREATE TABLE rate_samples
(
    rate_id    BIGINT NOT NULL,
    samples_id BIGINT NOT NULL,
    CONSTRAINT pk_rate_samples PRIMARY KEY (rate_id, samples_id)
);

CREATE TABLE rate_users
(
    rate_id  BIGINT NOT NULL,
    users_id BIGINT NOT NULL,
    CONSTRAINT pk_rate_users PRIMARY KEY (rate_id, users_id)
);

CREATE TABLE user_samples_list
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    user_id          BIGINT       NOT NULL,
    list_name        VARCHAR(255) NOT NULL,
    list_description VARCHAR(255) NULL,
    CONSTRAINT pk_user_samples_list PRIMARY KEY (id)
);

CREATE TABLE user_samples_list_samples
(
    samples_id           BIGINT NOT NULL,
    user_samples_list_id BIGINT NOT NULL,
    CONSTRAINT pk_user_samples_list_samples PRIMARY KEY (samples_id, user_samples_list_id)
);

CREATE TABLE users_favorite_samples
(
    samples_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    CONSTRAINT pk_users_favorite_samples PRIMARY KEY (samples_id, user_id)
);

ALTER TABLE user_samples_list
    ADD CONSTRAINT FK_USER_SAMPLES_LIST_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE rate_samples
    ADD CONSTRAINT fk_ratsam_on_rate FOREIGN KEY (rate_id) REFERENCES rate (id);

ALTER TABLE rate_samples
    ADD CONSTRAINT fk_ratsam_on_sample FOREIGN KEY (samples_id) REFERENCES samples (id);

ALTER TABLE rate_users
    ADD CONSTRAINT fk_ratuse_on_rate FOREIGN KEY (rate_id) REFERENCES rate (id);

ALTER TABLE rate_users
    ADD CONSTRAINT fk_ratuse_on_user FOREIGN KEY (users_id) REFERENCES users (id);

ALTER TABLE users_favorite_samples
    ADD CONSTRAINT fk_usefavsam_on_sample FOREIGN KEY (samples_id) REFERENCES samples (id);

ALTER TABLE users_favorite_samples
    ADD CONSTRAINT fk_usefavsam_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE user_samples_list_samples
    ADD CONSTRAINT fk_usesamlissam_on_sample FOREIGN KEY (samples_id) REFERENCES samples (id);

ALTER TABLE user_samples_list_samples
    ADD CONSTRAINT fk_usesamlissam_on_user_samples_list FOREIGN KEY (user_samples_list_id) REFERENCES user_samples_list (id);