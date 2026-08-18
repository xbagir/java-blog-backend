# blog-backend

Бэкенд для блога на Java.

## Стек

- Java 21
- Spring Boot 3.5 (Web MVC, Data JDBC) — встроенный сервлет-контейнер Tomcat
- Spring Data JDBC
- Gradle 8.14 (wrapper в репозитории)
- PostgreSQL 16
- JUnit 5, Spring Boot Test (`@WebMvcTest`, `@DataJdbcTest`), H2 (в PostgreSQL-режиме), Mockito

## Требования

- JDK 21 и Gradle 8.14 (или воспользоваться `./gradlew`) — для сборки и тестов.
- Docker с Compose v2 — для запуска приложения.

## Сборка бэкенда

Через Gradle wrapper:

```bash
./gradlew build
```

Результат: `build/libs/blog-backend.jar` — исполняемый (executable) JAR со встроенным сервлет-контейнером.

Только тесты без упаковки:

```bash
./gradlew test
```

## Запуск бэкенда

### Docker Compose (рекомендуется)

```bash
docker compose up --build
```

- Приложение: http://localhost:8082
- База данных PostgreSQL: `localhost:5432`, пользователь `blog`, пароль `blog`, БД `blogdb`
- При первом старте создаётся схема, и БД наполняется 10 демо-постами (управляется `SEED_DATABASE`)
- Тесты выполняются на этапе сборки образа

### Локально

```bash
./gradlew bootRun
```

или через собранный JAR (нужна доступная PostgreSQL):

```bash
java -jar build/libs/blog-backend.jar
```

Параметры окружения (переопределяют значения из `application.properties`):

| Переменная | Назначение | По умолчанию |
|------------|------------|--------------|
| `SPRING_DATASOURCE_URL` | JDBC-URL PostgreSQL | `jdbc:postgresql://localhost:5432/blogdb` |
| `SPRING_DATASOURCE_USERNAME` | Пользователь БД | `blog` |
| `SPRING_DATASOURCE_PASSWORD` | Пароль БД | `blog` |
| `SEED_DATABASE` | Применять схему и сидировать демо-данные при старте | `true` |
| `PORT` | Порт HTTP-сервера | `8080` |

## Использование

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