# blog-backend

Бэкенд для блога на Java.

## Стек

- Java 21, Maven
- Spring Framework (Core/Context, Web MVC, JDBC) — без Spring Boot
- Spring Data JDBC
- Tomcat 10.1 (embedded, внутри Docker-образа)
- PostgreSQL 16
- JUnit 5, Spring Test, Mockito

## Требования

- JDK 21 и Maven 3.9+ — для сборки бэкенда и запуска тестов.
- Docker с Compose v2 — для запуска приложения.

## Сборка бэкенда

Через Maven:

```bash
mvn -B clean package
```

Результат: `target/blog-backend.jar` + `target/lib/` (используются при сборке образа).

Через Docker:

```bash
docker build -t blog-backend .
```

Тесты запускаются автоматически на этапе сборки образа.

## Запуск тестов

```bash
mvn -B test
```

При сборке образа Docker тесты также выполняются в build-стадии.

## Деплой в сервлет-контейнер

Приложение поставляется как исполняемый jar с embedded Tomcat (сервлет-контейнер) и разворачивается в Docker-образе. Деплой = запуск образа через docker-compose:

```bash
docker compose up --build
```

Параметры окружения сервиса `app`:

| Переменная | Назначение | По умолчанию |
|------------|------------|--------------|
| `DB_URL` | JDBC-URL PostgreSQL | `jdbc:postgresql://localhost:5432/blogdb` |
| `DB_USER` | Пользователь БД | `blog` |
| `DB_PASSWORD` | Пароль БД | `blog` |
| `PORT` | Порт приложения | `8080` |
| `SEED_DATABASE` | Сидировать демо-данные при старте | `true` |

## Запуск и использование

```bash
docker compose up --build
```

- Приложение: http://localhost:8082
- База данных PostgreSQL: `localhost:5432`, пользователь `blog`, пароль `blog`, БД `blogdb`
- При первом старте создаётся схема, и БД наполняется 10 демо-постами

Примеры запросов:

```bash
# Лента постов (поиск + пагинация)
curl "http://localhost:8082/api/posts?search=&pageNumber=1&pageSize=5"

# Получить пост
curl http://localhost:8082/api/posts/1

# Создать пост
curl -X POST http://localhost:8082/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"Заголовок","text":"Текст поста","tags":["java","spring"]}'

# Обновить пост
curl -X PUT http://localhost:8082/api/posts/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Новый заголовок","text":"Новый текст","tags":["java"]}'

# Удалить пост
curl -X DELETE http://localhost:8082/api/posts/1

# Лайк поста (ответ — новое число лайков)
curl -X POST http://localhost:8082/api/posts/1/likes

# Загрузить картинку поста
curl -X PUT http://localhost:8082/api/posts/1/image -F "image=@photo.jpg"

# Получить картинку поста
curl -o photo.jpg http://localhost:8082/api/posts/1/image

# Комментарии поста
curl http://localhost:8082/api/posts/1/comments

# Добавить комментарий
curl -X POST http://localhost:8082/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{"text":"Отличный пост"}'
```

## Запросы (API)

| Метод | Путь | Описание |
|-------|------|----------|
| GET | `/api/posts?search=&pageNumber=&pageSize=` | Получить ленту постов (пагинация и поиск) |
| GET | `/api/posts/{id}` | Получить пост по id |
| POST | `/api/posts` | Создать пост |
| PUT | `/api/posts/{id}` | Обновить пост |
| DELETE | `/api/posts/{id}` | Удалить пост |
| POST | `/api/posts/{id}/likes` | Поставить лайк посту |
| PUT | `/api/posts/{id}/image` | Загрузить картинку поста (multipart `image`) |
| GET | `/api/posts/{id}/image` | Получить картинку поста |
| GET | `/api/posts/{postId}/comments` | Получить комментарии поста |
| GET | `/api/posts/{postId}/comments/{commentId}` | Получить комментарий по id |
| POST | `/api/posts/{postId}/comments` | Добавить комментарий |
| PUT | `/api/posts/{postId}/comments/{commentId}` | Обновить комментарий |
| DELETE | `/api/posts/{postId}/comments/{commentId}` | Удалить комментарий |
