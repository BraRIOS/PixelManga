CREATE TABLE author_request
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    username   VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    message    VARCHAR(2000) NULL,
    status     VARCHAR(255) NOT NULL,
    created_at date         NOT NULL,
    updated_at date NULL,
    CONSTRAINT pk_author_request PRIMARY KEY (id)
);