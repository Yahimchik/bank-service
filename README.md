# 💳 Проект по управлению банковскими картами

Это приложение — система для управления банковскими картами, транзакциями и лимитами.  
**Технологии:** `Spring Boot`, `PostgreSQL`, `Liquibase`, `Docker`, `Swagger`

---

## 🚀 Запуск проекта

### 📦 Требования
- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)

### ⚙️ Инструкция по запуску

1. Склонируйте репозиторий:
   ```bash
   git clone https://github.com/Yahimchik/bank-service.git
   cd bank-service
   ```

2. Постройте и запустите контейнеры:
   ```bash
   docker-compose up --build
   ```

   Будут запущены следующие сервисы:
    - 🐘 **PostgreSQL** — порт `5434`
    - 🗂️ **Adminer** — веб-интерфейс по порту `8092`
    - 🚀 **Spring Boot приложение** — порт `9080`

---

## 💾 Доступ к сервисам

После запуска контейнеров, перейдите в браузере по ссылкам:

- [**Swagger UI**](http://localhost:9080/swagger-ui/index.html) — OpenAPI-документация
- [**Adminer**](http://localhost:8092) — веб-интерфейс для работы с базой данных

**Параметры подключения к базе:**
- **System**: PostgreSQL
- **Server**: `card-postgres`
- **Username**: `user`
- **Password**: `password`
- **Database**: `cards_db`

---

## 📊 Функциональность

### 💳 Управление картами
- `POST /api/v1/cards` — создание новой карты
- `PATCH /api/v1/cards/{cardId}/block` — блокировка карты
- `PATCH /api/v1/cards/{cardId}/activate` — активация карты
- `GET /api/v1/cards` — просмотр всех карт
- `GET /api/v1/cards/me` — просмотр карт текущего пользователя
- `PUT /api/v1/cards/{cardId}/request-block` — запрос на блокировку карты
- `PUT /api/v1/cards/{cardId}/reject-block-request` — отклонить запрос на блокировку
- `DELETE /api/v1/cards/{cardId}` — удаление карты

### 💸 Транзакции
- `GET /api/transactions/transactions` — история транзакций
- `POST /api/transactions/{fromCardId}/transfer/{toCardId}` — перевод между картами
- `POST /api/transactions/{cardId}/withdraw` — снятие средств с карты
- `POST /api/transactions/{cardId}/deposit` — пополнение карты

### 📈 Лимиты
- `GET /api/v1/card-limits/{cardId}` — получение лимита карты
- `PATCH /api/v1/card-limits/{cardId}` — установка/обновление лимита

### 👤 Пользователи
- `POST /api/v1/users/registration` — регистрация нового пользователя
- `GET /api/v1/users/{id}` — восстановление пользователя
- `DELETE /api/v1/users/{id}` — удаление пользователя
- `PATCH /api/v1/users/{id}` — обновление информации пользователя
- `GET /api/v1/users` — просмотр всех пользователей

### 🔐 Аутентификация
- `POST /api/v1/auth/login` — аутентификация пользователя
- `POST /api/v1/auth/token` — обновление JWT токена
- `POST /api/v1/auth/logout` — выход из системы

### 🧩 Работа с базой
- **PostgreSQL** — хранение информации о пользователях, картах, транзакциях, лимитах
- **Liquibase** — управление миграциями базы данных

### 🛡️ Безопасность
- Криптографическая защита чувствительных данных
- Хранение секретов через конфигурации приложения

---

## 🗂️ Структура проекта

### 🐳 Контейнеры:
- `adminer` — веб-интерфейс управления базой данных
- `postgres` — хранилище данных о картах, транзакциях и лимитах
- `service-container` — основное Spring Boot приложение

### ⚙️ Конфигурационные файлы:
- `docker-compose.yml` — описание всех контейнеров
- `Dockerfile` — сборка Java-приложения
- `application.yml` — настройки безопасности и подключения к БД

---

