CREATE TABLE `admin`
(
    id      BIGINT NOT NULL,
    user_id BIGINT NULL,
    CONSTRAINT pk_admin PRIMARY KEY (id)
);

CREATE TABLE attribute
(
    id      BIGINT       NOT NULL,
    name    VARCHAR(255) NOT NULL,
    type_id BIGINT       NOT NULL,
    CONSTRAINT pk_attribute PRIMARY KEY (id)
);

CREATE TABLE author
(
    id      BIGINT NOT NULL,
    user_id BIGINT NULL,
    CONSTRAINT pk_author PRIMARY KEY (id)
);

CREATE TABLE chapter
(
    id        BIGINT NOT NULL,
    sample_id BIGINT NOT NULL,
    image     VARCHAR(255) NULL,
    CONSTRAINT pk_chapter PRIMARY KEY (id)
);

CREATE TABLE sample
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    name             VARCHAR(255) NOT NULL,
    synopsis         VARCHAR(255) NOT NULL,
    publication_date date NULL,
    cover            VARCHAR(255) NULL,
    CONSTRAINT pk_sample PRIMARY KEY (id)
);

CREATE TABLE sample_attributes
(
    attributes_id BIGINT NOT NULL,
    sample_id     BIGINT NOT NULL,
    CONSTRAINT pk_sample_attributes PRIMARY KEY (attributes_id, sample_id)
);

CREATE TABLE sample_revision
(
    id          BIGINT NOT NULL,
    is_approved BIT(1) NULL,
    message     VARCHAR(255) NULL,
    admin_id    BIGINT NULL,
    author_id   BIGINT NULL,
    sample_id   BIGINT NULL,
    CONSTRAINT pk_sample_revision PRIMARY KEY (id)
);

CREATE TABLE type
(
    id        BIGINT       NOT NULL,
    name      VARCHAR(255) NOT NULL,
    sample_id BIGINT NULL,
    CONSTRAINT pk_type PRIMARY KEY (id)
);

CREATE TABLE user
(
    id        BIGINT AUTO_INCREMENT NOT NULL,
    email     VARCHAR(255) NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    password  VARCHAR(255) NOT NULL,
    born_year VARCHAR(255) NOT NULL,
    icon      VARCHAR(255) NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

ALTER TABLE `admin`
    ADD CONSTRAINT uc_admin_user UNIQUE (user_id);

ALTER TABLE attribute
    ADD CONSTRAINT uc_attribute_name UNIQUE (name);

ALTER TABLE author
    ADD CONSTRAINT uc_author_user UNIQUE (user_id);

ALTER TABLE sample
    ADD CONSTRAINT uc_sample_name UNIQUE (name);

ALTER TABLE user
    ADD CONSTRAINT uc_user_email UNIQUE (email);

ALTER TABLE user
    ADD CONSTRAINT uc_user_user_name UNIQUE (user_name);

ALTER TABLE `admin`
    ADD CONSTRAINT FK_ADMIN_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE attribute
    ADD CONSTRAINT FK_ATTRIBUTE_ON_TYPE FOREIGN KEY (type_id) REFERENCES type (id);

ALTER TABLE author
    ADD CONSTRAINT FK_AUTHOR_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE chapter
    ADD CONSTRAINT FK_CHAPTER_ON_SAMPLE FOREIGN KEY (sample_id) REFERENCES sample (id);

ALTER TABLE sample_revision
    ADD CONSTRAINT FK_SAMPLE_REVISION_ON_ADMIN FOREIGN KEY (admin_id) REFERENCES `admin` (id);

ALTER TABLE sample_revision
    ADD CONSTRAINT FK_SAMPLE_REVISION_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES author (id);

ALTER TABLE sample_revision
    ADD CONSTRAINT FK_SAMPLE_REVISION_ON_SAMPLE FOREIGN KEY (sample_id) REFERENCES sample (id);

ALTER TABLE type
    ADD CONSTRAINT FK_TYPE_ON_SAMPLE FOREIGN KEY (sample_id) REFERENCES sample (id);

ALTER TABLE sample_attributes
    ADD CONSTRAINT fk_samatt_on_attribute FOREIGN KEY (attributes_id) REFERENCES attribute (id);

ALTER TABLE sample_attributes
    ADD CONSTRAINT fk_samatt_on_sample FOREIGN KEY (sample_id) REFERENCES sample (id);