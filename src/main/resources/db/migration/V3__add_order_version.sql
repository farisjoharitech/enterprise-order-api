ALTER TABLE orders
    ADD COLUMN version BIGINT;

UPDATE orders
SET version = 0
WHERE version IS NULL;

ALTER TABLE orders
    ALTER COLUMN version SET DEFAULT 0;

ALTER TABLE orders
    ALTER COLUMN version SET NOT NULL;
