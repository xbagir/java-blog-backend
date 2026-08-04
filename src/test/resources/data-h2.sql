DELETE FROM comments;
DELETE FROM post_tags;
DELETE FROM posts;

INSERT INTO posts (id, title, text, likes_count, created_at) VALUES
    (1, 'Пост первый', 'Короткий текст поста', 2, '2026-01-01 10:00:00'),
    (2, 'Пост второй', 'Lalala это текст второго поста', 1, '2026-01-02 10:00:00'),
    (3, 'Пост третий', 'Длинный текст ' || repeat('т', 300), 1, '2026-01-03 10:00:00'),
    (4, 'Пост четвертый', 'Обычный текст четвертого поста', 0, '2026-01-04 10:00:00');

INSERT INTO post_tags (post_id, tag) VALUES
    (1, 'tag1'),
    (1, 'tag2'),
    (2, 'lalala'),
    (3, 'tag3');

INSERT INTO comments (id, post_id, text) VALUES
    (1, 1, 'Комментарий к первому'),
    (2, 2, 'Комментарий ко второму');

ALTER TABLE posts ALTER COLUMN id RESTART WITH 5;
ALTER TABLE comments ALTER COLUMN id RESTART WITH 3;
