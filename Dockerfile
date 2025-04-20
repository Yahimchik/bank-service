# Используем самый последний доступный образ для Maven
FROM maven:latest AS build

# Устанавливаем рабочую директорию в контейнере
WORKDIR /app

# Копируем pom.xml и загружаем зависимости (кэшируем этот слой для ускорения сборки)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код в контейнер
COPY src ./src

# Собираем Spring Boot приложение
RUN mvn clean package -DskipTests

# Используем минимальный образ для запуска приложения
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем скомпилированный jar файл из предыдущего этапа
COPY --from=build /app/target/*.jar /app/app.jar

# Открываем нужный порт (по умолчанию Spring Boot использует 8080)
EXPOSE 9080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
