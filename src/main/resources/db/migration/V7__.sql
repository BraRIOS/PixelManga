ALTER TABLE author_request
    ADD reject_reason VARCHAR(1000) NULL;

ALTER TABLE author_request
    ADD updated_by VARCHAR(255) NULL;