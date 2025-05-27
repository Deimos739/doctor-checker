DoctorChecker - Веб-приложение для управления и форматирования документов

Описание:

DoctorChecker — веб-приложение для загрузки, хранения и форматирования документов Microsoft Word (.docx) с применением стилей ГОСТ 7.32-2017. Поддерживает управление метаданными через PostgreSQL и простой веб-интерфейс.

Требования:

Java 17+

Maven

PostgreSQL

Установка и запуск:

Клонирование репозитория:

git clone https://github.com/yourusername/doctor-checker.git

cd doctor-checker

Установка PostgreSQL:

На Ubuntu/Debian:

sudo apt update

sudo apt install postgresql postgresql-contrib

sudo -u postgres psql

CREATE DATABASE doctorchecker;

CREATE USER your_username WITH PASSWORD 'your_password';

GRANT ALL PRIVILEGES ON DATABASE doctorchecker TO your_username;

\q

На Windows:

Скачайте установщик с официального сайта PostgreSQL.

Установите и создайте базу doctorchecker с пользователем через pgAdmin или командную строку.

Настройка проекта:

Откройте src/main/resources/application.properties и укажите:

spring.datasource.url=jdbc:postgresql://localhost:5432/doctorchecker

spring.datasource.username=your_username

spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update

Создайте папку ./uploads для хранения файлов.

Сборка и запуск:

Выполните:

mvn clean install

mvn spring-boot:run

Откройте браузер и перейдите на http://localhost:8080.

Использование:

Загружайте .docx-файлы через веб-интерфейс.

Форматируйте документы по ГОСТ и скачивайте результаты.

Документация:

Руководство пользователя

Руководство администратора
