CREATE TABLE IF NOT EXISTS posts (
    id                 BIGSERIAL PRIMARY KEY,
    title              VARCHAR(255) NOT NULL,
    text               TEXT         NOT NULL,
    likes_count        BIGINT       NOT NULL DEFAULT 0,
    image              BYTEA,
    image_content_type VARCHAR(100),
    created_at         TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS post_tags (
    post_id BIGINT      NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    tag     VARCHAR(50) NOT NULL,
    PRIMARY KEY (post_id, tag)
);

CREATE TABLE IF NOT EXISTS comments (
    id         BIGSERIAL PRIMARY KEY,
    post_id    BIGINT    NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    text       TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_comments_post_id ON comments (post_id);
CREATE INDEX IF NOT EXISTS idx_post_tags_post_id ON post_tags (post_id);
