CREATE TABLE attributes
(
    id      BIGINT AUTO_INCREMENT NOT NULL,
    name    VARCHAR(255) NOT NULL,
    type_id BIGINT       NOT NULL,
    `description` VARCHAR(255) NULL,
    CONSTRAINT pk_attributes PRIMARY KEY (id)
);

CREATE TABLE chapters
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    sample_id  BIGINT       NOT NULL,
    image      VARCHAR(255) NOT NULL,
    number     BIGINT       NULL,
    CONSTRAINT pk_chapters PRIMARY KEY (id)
);

CREATE TABLE roles
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE TABLE sample_attributes
(
    attributes_id BIGINT NOT NULL,
    sample_id     BIGINT NOT NULL,
    CONSTRAINT pk_sample_attributes PRIMARY KEY (attributes_id, sample_id)
);

CREATE TABLE samples
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    name             VARCHAR(255) NOT NULL,
    synopsis         VARCHAR(2000) NULL,
    publication_date date NULL,
    cover            VARCHAR(255) NULL,
    CONSTRAINT pk_samples PRIMARY KEY (id)
);

CREATE TABLE types
(
    id   BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_types PRIMARY KEY (id)
);

CREATE TABLE users
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    email     VARCHAR(255) NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    password  VARCHAR(255) NOT NULL,
    born_year INT          NOT NULL,
    icon      VARCHAR(255) NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE users_roles
(
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT pk_users_roles PRIMARY KEY (role_id, user_id)
);

ALTER TABLE attributes
    ADD CONSTRAINT uc_attributes_name UNIQUE (name);

ALTER TABLE roles
    ADD CONSTRAINT uc_roles_name UNIQUE (name);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_user_name UNIQUE (user_name);

ALTER TABLE attributes
    ADD CONSTRAINT FK_ATTRIBUTES_ON_TYPE FOREIGN KEY (type_id) REFERENCES types (id);

ALTER TABLE chapters
    ADD CONSTRAINT FK_CHAPTERS_ON_SAMPLE FOREIGN KEY (sample_id) REFERENCES samples (id);

ALTER TABLE sample_attributes
    ADD CONSTRAINT fk_samatt_on_attribute FOREIGN KEY (attributes_id) REFERENCES attributes (id);

ALTER TABLE sample_attributes
    ADD CONSTRAINT fk_samatt_on_sample FOREIGN KEY (sample_id) REFERENCES samples (id);

ALTER TABLE users_roles
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE users_roles
    ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES users (id);