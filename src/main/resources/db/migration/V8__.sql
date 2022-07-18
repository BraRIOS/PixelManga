ALTER TABLE users
    ADD stripe_id VARCHAR(255) NULL;

ALTER TABLE users
    ADD CONSTRAINT uc_users_stripe UNIQUE (stripe_id);