-- Demo data. Seeded only when the posts table is empty (see DatabaseSeeder).
INSERT INTO posts (id, title, text, likes_count, created_at) VALUES
    (1, 'The Witcher 3: Wild Hunt', 'Открытый мир в жанре RPG: роль **Геральта из Ривии**, охота на чудовищ и нелинейный сюжет. Lalala, одна из лучших RPG за всю историю.', 2, now() - interval '25 days'),
    (2, 'Cyberpunk 2077', 'Мрачный *Найт-Сити* и история наёмника Ви. Открытый мир, кибер-импланты и глубокие диалоги в духе киберпанка.', 3, now() - interval '24 days'),
    (3, 'Hades', 'Roguelike про побег из царства Аида. **Каждая попытка** открывает новые диалоги, а бой не отпускает. Отличный инди-проект.', 4, now() - interval '23 days'),
    (4, 'Disco Elysium', 'Детективная RPG без боевой системы: расследование, внутренние диалоги и *политические* споры внутри головы героя.', 5, now() - interval '22 days'),
    (5, 'Portal 2', 'Головоломки от первого лица с портальной пушкой. **Кооператив**, юмор ГЛаДОС и гениальные механики.', 6, now() - interval '21 days'),
    (6, 'DOOM Eternal', 'Быстрый *шуттер* без укрытий. Демоны, рифление и требующий мастерства бой в аду Марса.', 7, now() - interval '20 days'),
    (7, 'Stardew Valley', 'Уютный симулятор фермы: выращивание, рыбалка, шахты и общение с жителями городка. Инди-хит для расслабления.', 1, now() - interval '19 days'),
    (8, 'Hollow Knight', 'Метроидвания в мрачном подземном королевстве. Точный платформер, атмосфера и боссы.', 2, now() - interval '18 days'),
    (9, 'Factorio', 'Стратегия про автоматизацию: добыча ресурсов и конвейеры. Lalala, ещё один час и всё заработает.', 3, now() - interval '17 days'),
    (10, 'Sekiro: Shadows Die Twice', 'Souls-like про синоби в Японии. **Точные парирования**, боссы и знаменитая сложность.', 4, now() - interval '16 days');

INSERT INTO post_tags (post_id, tag) VALUES
    (1, 'rpg'), (1, 'open-world'), (1, 'story'),
    (2, 'rpg'), (2, 'open-world'), (2, 'sci-fi'),
    (3, 'roguelike'), (3, 'indie'), (3, 'action'),
    (4, 'rpg'), (4, 'story'), (4, 'indie'),
    (5, 'puzzle'), (5, 'story'), (5, 'coop'),
    (6, 'shooter'), (6, 'action'), (6, 'fps'),
    (7, 'farming'), (7, 'indie'), (7, 'simulation'),
    (8, 'metroidvania'), (8, 'indie'), (8, 'platformer'),
    (9, 'strategy'), (9, 'automation'), (9, 'base-building'),
    (10, 'action'), (10, 'souls-like'), (10, 'difficult');

INSERT INTO comments (post_id, text, created_at) VALUES
    (1, 'Лучшая RPG последних лет!', now() - interval '24 days'),
    (1, 'Открытый мир просто гигантский.', now() - interval '23 days'),
    (1, 'Геральт — легенда.', now() - interval '22 days'),
    (2, 'После патчей стало сильно лучше.', now() - interval '23 days'),
    (2, 'Найт-Сити очень атмосферный.', now() - interval '22 days'),
    (3, 'Каждый забег не надоедает.', now() - interval '22 days'),
    (3, 'Диалоги — лучшая часть.', now() - interval '21 days'),
    (3, 'Боссы сложные, но честные.', now() - interval '20 days'),
    (3, 'Музыка из Hades — топ.', now() - interval '19 days'),
    (3, '100+ часов пролетели незаметно.', now() - interval '18 days'),
    (5, 'ГЛаДОС неподражаема.', now() - interval '20 days'),
    (5, 'В кооперативе играется отлично.', now() - interval '19 days'),
    (6, 'Абсолютно безумный темп.', now() - interval '19 days'),
    (7, 'Лучшая игра, чтобы отдохнуть вечером.', now() - interval '18 days'),
    (7, 'Ферма, шахты, рыбалка — всё в одном.', now() - interval '17 days'),
    (7, 'Моддеры делают её ещё лучше.', now() - interval '16 days'),
    (7, 'Деревня такая уютная.', now() - interval '15 days'),
    (8, 'Атмосфера просто завораживает.', now() - interval '17 days'),
    (9, 'Ещё один конвейер и всё.', now() - interval '16 days'),
    (9, 'Автоматизация затягивает на часы.', now() - interval '15 days'),
    (9, 'Это база, жду следующий блок.', now() - interval '14 days'),
    (10, 'Слишком сложно, но гениально.', now() - interval '15 days'),
    (10, 'Парирования — лучшее в боёвке.', now() - interval '14 days');

SELECT setval('posts_id_seq', (SELECT COALESCE(MAX(id), 1) FROM posts));
SELECT setval('comments_id_seq', (SELECT COALESCE(MAX(id), 1) FROM comments));
