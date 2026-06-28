ALTER TABLE users ADD COLUMN username VARCHAR(50);

UPDATE users
SET username = LOWER(REGEXP_REPLACE(SPLIT_PART(email, '@', 1), '[^a-zA-Z0-9_]', '_', 'g'))
WHERE username IS NULL;

UPDATE users u
SET username = CONCAT(
        username,
        '_',
        SUBSTRING(REPLACE(CAST(u.id AS VARCHAR), '-', ''), 1, 4)
    )
WHERE u.id IN (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (PARTITION BY username ORDER BY created_at NULLS LAST, id) AS row_num
        FROM users
    ) duplicates
    WHERE row_num > 1
);

ALTER TABLE users ALTER COLUMN username SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);
CREATE INDEX idx_users_username ON users(username);
