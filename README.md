# Game
## Запускаем сервис:

1. Докер файл docker-compose.yml находится в директории \src\main\resources\docker
- docker-compose up
2. Упаковываем проект
- mvn clean package
3. Поднимаем сервис
- java -jar GameProject-0.1.0-jar-with-dependencies


### REST-сервис

##### Добавить нового юзера
POST http://localhost:8080/user

application/json
  {
  "name": "UserName",
  "wallet": "9999"
  }

##### Запрос юзера по id
GET http://localhost:8080/user/{id}

##### Запрос списка всех юзеров
GET http://localhost:8080/users

##### Удалить юзера по id
DELETE http://localhost:8080/delete-user/id

##### Добавить юзера в клан
PUT http://localhost:8080/add-user-to-clan

application/json
{
"userId": 3,
"clanId": 4
}

##### Добавить новый клан
POST http://localhost:8080/clan

application/json
{
"name": "ClanName"
}

##### Запрос клана по id
GET http://localhost:8080/clan/{id}

##### Запрос списка всех кланов
GET http://localhost:8080/clans

##### Удалить клан по id
DELETE http://localhost:8080/delete-user/id

##### Пополнение/изъятие золота из казны клана юзером. Имитация деятельности - несколько раз меняем баланс (разные транзакции в одной бизнес логике). Например, при пополнении казны клана на n золота, следом выполнится еще одна транзакция на n * 5.
PUT http://localhost:8080/top-up-gold

application/json
{
"replenishment": 200,
"idClan": 4,
"idUser": 2
}

##### Запрос всех транзакций по конкретному клану (Отслеживание транзакций)
GET http://localhost:8080/transactions/clan-id


На написание Unit тестов, к сожалению, не хватило времени.
