#### Дз КПО Антиплагиат для НИУ ВШЭ

Микросервисное приложение для анализа студенческих работ на плагиат и сбора статистики текста.

#### Описание проекта

Система позволяет:
- Загружать текстовые файлы (.txt)
- Проводить анализ текста (подсчет абзацев, слов, символов)
- Проверять работы на 100% совпадения с ранее загруженными текстами

#### Сервисы
- File Storing Service (порт: 8080)
- File Analysis Service (порт: 8082)
- API Gateway (порт: 8086)

#### Запуск всех сервисов
```bash
docker-compose up --build
```

#### Запуск тестов

Каждый сервис содержит тесты. Для запуска тестов отдельного сервиса:

```bash
cd 'название-сервиса'
docker-compose -f docker-compose.test.yml up --build
```


#### API Документация

После запуска, Swagger UI доступен по следующим адресам:
- File Storing Service: http://localhost:8080/swagger-ui/index.html
- File Analysis Service: http://localhost:8082/swagger-ui/index.html
- API Gateway: http://localhost:8086/swagger-ui/index.html