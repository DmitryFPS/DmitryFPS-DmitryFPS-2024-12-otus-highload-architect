# Создаем нагрузку на чтение с помощью составленного плана, делаем замеры.

---

- В тесте используем различные нагрузки: 1, 10, 100 и 1000 одновременных запросов
- Делаем запросы на /user/get/{id}
- Делаем запросы на /user/search

---

## Генерация 1_000_000 анкет

- Запускаем GenerateCSV будет сгенерированы 3 файла в /resources/migration
  users.csv, user_roles.csv, user_interests.csv
- Можно на уникальность проверить csv
    - sort users.csv | uniq -d
    - sort user_roles.csv | uniq -d
    - sort user_interests.csv | uniq -d

## Проверка, что в файлах полное кол-во данных (IDEA почему то не всегда показывает больше 12 000 строк)

- wc -l users.csv
- wc -l user_interests.csv
- wc -l user_roles.csv

## Для нагрузочного тестирования поиска анкет по префиксу имени и фамилии (одновременно)

- Запрос в форме firstName LIKE ? and secondName LIKE ?
- Сортировать вывод по id анкеты
- GET /user/search?first_name=Конст&last_name=Оси

## Для нагрузочного тестирования поиска юзера по id

- Запрос на GET /user/get/{id}
- Для запроса /user/get/{id} в JMeter id нужно проставить случайное id
    - В поле "Path" укажите /api/v1/user/${id}
    - Добавьте элемент User Defined Variables и создайте переменную id
    - В значение переменной id вставьте функцию ${__Random(1,1000000,id)}
- Теперь будут отправляться два запроса на GET /user/search и GET /user/get/{id}

## Делаем репликацию

### Настроить 2 слейва и 1 мастер. Включить потоковую репликацию.

- Сначала проверяем есть ли такая сеть docker network ls
- Если есть сеть, то можно удалить docker network rm pgnet
- Создаем сеть, запоминаем адрес
    - docker network create pgnet
        - docker network inspect pgnet | grep Subnet # Запомнить маску сети (Это ip адрес - пример 172.19.0.0/16)

---

- Поднимаем мастер
    - docker run -dit -v "$PWD/volumes/pgmaster/:/var/lib/postgresql/data" -e POSTGRES_PASSWORD=pass -p "5432:5432"
      --restart=unless-stopped --network=pgnet --name=pgmaster postgres
- Меняем postgresql.conf на мастере (меняем в $PWD/volumes/pgmaster/ так как он смонтирован в контейнер)
    - ssl = off
    - wal_level = replica
    - max_wal_senders = 4 # expected slave num
    - max_connections = 10000 (максимальное количество подключений)
- Подключаемся к мастеру и создаем пользователя для репликации
    - docker exec -it pgmaster su - postgres -c psql
    - create role replicator with login replication password 'pass';
- Добавляем запись в pgmaster/pg_hba.conf (subnet с первого шага)
    - host --- replication --- replicator --- __SUBNET__ --- md5 (Пример SUBNET: 172.19.0.0/16)
- Перезапустим мастер
    - docker restart pgmaster

---

- Сделаем бэкап для реплик
    - docker exec -it pgmaster bash
    - mkdir /pgslave
    - pg_basebackup -h pgmaster -D /pgslave -U replicator -v -P --wal-method=stream
- Копируем директорию себе
    - docker cp pgmaster:/pgslave volumes/pgslave/
- Создадим файл, чтобы реплика узнала, что она реплика
    - touch volumes/pgslave/standby.signal
- Меняем postgresql.conf на реплике pgslave
    - primary_conninfo = 'host=pgmaster port=5432 user=replicator password=pass application_name=pgslave'
    - max_connections = 10000 (максимальное количество подключений)
- Запускаем реплику pgslave
    - docker run -dit -v "$PWD/volumes/pgslave/:/var/lib/postgresql/data" -e POSTGRES_PASSWORD=pass -p "15432:5432"
      --network=pgnet --restart=unless-stopped --name=pgslave postgres

---

- Запустим вторую реплику pgasyncslave
- Скопируем бэкап
    - docker cp pgmaster:/pgslave volumes/pgasyncslave/
- Изменим настройки pgasyncslave/postgresql.conf
    - primary_conninfo = 'host=pgmaster port=5432 user=replicator password=pass application_name=pgasyncslave'
    - max_connections = 10000 (максимальное количество подключений)
- Дадим знать что это реплика
    - touch volumes/pgasyncslave/standby.signal
- Запустим реплику pgasyncslave
    - docker run -dit -v "$PWD/volumes/pgasyncslave/:/var/lib/postgresql/data" -e POSTGRES_PASSWORD=pass -p "25432:5432"
      --network=pgnet --restart=unless-stopped --name=pgasyncslave postgres

---

- Копируем файлы users.csv, user_roles.csv, user_interests.csv в /volumes/pgmaster/ что бы сделать миграцию
- Перезапустим мастер
    - docker restart pgmaster

---

- Запускаем App (Что бы создались схемы)
- Останавливаем после запуска (Для миграции лучше остановить, так просто быстрее)
- Настройки в application.yml
- ReplicationDataSourceConfig (Создаст схемы и запустит мастер и слейвы с балансировкой)
- username (В Environment)
- password (В Environment)

- JWT_EXPIRATION 86400
- JWT_REFRESH_EXPIRATION 86400
- JWT_SECRET +AVVLHD+9HxbBZYQmEnnuwisUGzW/m89H7i5FMkHEqE=
- PASSWORD_BD pass
- USER_NAME_BD postgres

- jdbc:postgresql://pgmaster:5432/postgres
- jdbc:postgresql://pgslave:5432/postgres
- jdbc:postgresql://pgasyncslave:5432/postgres

---

- Делаем миграцию (Делаем до синхронной работы со слейвами, что бы миграция прошла быстрее)
- Заходим в docker exec -it pgmaster su - postgres -c psql
- Делаем миграцию
    - \copy users (first_name, last_name, birth_date, gender, email, password, is_active, city_id) FROM '
      /var/lib/postgresql/data/users.csv' WITH (FORMAT csv, HEADER);
    - \copy user_roles (user_id, role_id) FROM '/var/lib/postgresql/data/user_roles.csv' WITH (FORMAT csv, HEADER);
    - \copy user_interests (user_id, interest_id) FROM '/var/lib/postgresql/data/user_interests.csv' WITH (FORMAT csv,
      HEADER);

---

- Проверим наличие данных на pgmaster
    - docker exec -it pgmaster su - postgres -c psql
    - SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';

- Проверим наличие данных на pgslave
    - docker exec -it pgslave su - postgres -c psql
    - SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';

- Проверим наличие данных на pgasyncslave
    - docker exec -it pgasyncslave su - postgres -c psql
    - SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';

- Данные должны быть одинаковыми

---

- Убеждаемся что обе реплики работают в асинхронном режиме на pgmaster
    - docker exec -it pgmaster su - postgres -c psql
    - select application_name, sync_state from pg_stat_replication;
- Включаем синхронную репликацию на pgmaster
- Меняем файл pgmaster/postgresql.conf
    - synchronous_commit = on
    - synchronous_standby_names = 'FIRST 1 (pgslave, pgasyncslave)'
- Перечитываем конфиг
    - docker exec -it pgmaster su - postgres -c psql
    - select pg_reload_conf();
- Убеждаемся, что реплика стала синхронной
    - docker exec -it pgmaster su - postgres -c psql
    - select application_name, sync_state from pg_stat_replication;

---

## Тестирование

1. Создаем нагрузку на чтение.
2. Настроить 2 слейва и 1 мастер.
3. Включить потоковую репликацию.
4. Добавить в проект replicated datasource:
    - Использование Replication DataSource (Класс ReplicationDataSourceConfig в директории configuration)
5. Создаем нагрузку на чтение с помощью составленного на предыдущем шаге плана, делаем замеры.
6. Добавить сравнение результатов в
   [Отчет: ](./readreport/replicationperf.md)

---

## Настроить кворумную синхронную репликацию.

- В volumes/pgmaster/postgresql.conf настраиваем
    - synchronous_commit = on
    - synchronous_standby_names = 'ANY 1 (pgslave, pgasyncslave)'
    - Согласованность данных (данные записываются на обе реплики).
    - Отказоустойчивость (данные не теряются при сбое мастера).
- Перечитываем конфиг
    - docker exec -it pgmaster su - postgres -c psql
    - select pg_reload_conf();
- Убеждаемся, что реплика стала синхронной и quorum
    - docker exec -it pgmaster su - postgres -c psql
    - select application_name, sync_state from pg_stat_replication;

## Тестирование записи в БД /api/v1/user/register

- Запускаем docker-compose-monitoring.yml
    - Мониторинг
- docker-compose -f docker-compose-monitoring.yml up -d
- Если что то не запускается то смотрим логи (docker logs id_container)
- В основном проблемы в Linux это права доступа и их править можно (Пример chmod -R 777 ./data/grafana/)
- Если вдруг приложение не запустилось, то просто перезапускаем.
- MacBook тут не подойдет (cadvisor не работает на ARM64)

---

## App

- Приложение запускается в контейнер, так что можно с ним работать как с обычным контейнером и делать на него запросы.

---

## Настраиваем Prometheus и grafana

- Запускаем http://localhost:9091/ -> Status -> Target health (Смотрим что бы все было Up)
- Запускаем http://localhost:3000/ (логин admin, пароль MYPASSWORT)
- Заходим в Data sources -> Add data source -> prometheus
    - Connection -> http://prometheus:9090
    - Save & test
- Dashboards -> New -> Import
    - Тут можно с офф сайта https://grafana.com/grafana/dashboards/
    - Можно просто выбирать Дашборды по Id
    - Например, для Docker monitoring Id 193
    - Вводим в Find and import dashboards for common applications at grafana.com/dashboards 193 и жмем Load
    - prometheus выбираем из списка prometheus
    - Import
    - И так далее ...
    - cAdvisor: ID 193 — для мониторинга ресурсов Docker-контейнеров (CPU, память, сеть и т.д.).
    - Node Exporter: ID 1860 — для мониторинга хоста, на котором запущены контейнеры.
    - Nginx Exporter: ID 12708 — для мониторинга метрик Nginx.


- Создать нагрузку на запись в любую тестовую таблицу.
    - Делаем 300 записей (ПК слабоват)
- На стороне, которой нагружаем считать, сколько строк мы успешно записали.
    - [Останавливаю slave и заканчиваю нагрузку: ](writereport/kill_replic.png)
    - Как видно из картинки, что slave был остановлен, но при этом потерь данных нет (все 300 аккаунтов создались)
    - [Нагрузка на контейнеры: ](writereport/docker.png)
    - [Нагрузка на App: ](writereport/App.png)
- Что бы увидеть как распределяется нагрузка между master и slave вводим docker logs -f id_container и смотрим логи
  приложения
- [Потерь нет: ](writereport/ok.png)

## Убиваем Мастер

- docker stop pgmaster
- [Мертвый Мастер: ](writereport/kill_master.png)

## Выбираем самый свежий слейв.

- docker exec -it pgslave su - postgres -c psql
- Промоутим его до мастера
    - select pg_promote();
- [Свежий слэйв: ](writereport/promout_slave.png)

## Переключаем на него второй слейв

    - Заходим в postgresql.conf pgslave
    - изменяем конфиг
    - коментируем primary_conninfo (так как pgmaster больше не мастер)
    - synchronous_commit = on
    - synchronous_standby_names = 'ANY 1 (pgmaster, pgasyncslave)'

- перечитываем конфиг
    - docker exec -it pgslave su - postgres -c psql
    - select pg_reload_conf();

## Подключим вторую реплику pgasyncslave к новому мастеру pgslave

- изменяем конфиг pgasyncslave/postgresql.conf
- primary_conninfo = 'host=pgslave port=5432 user=replicator password=pass application_name=pgasyncslave'
- запускаем docker start pgasyncslave
- перечитываем конфиг
    - docker exec -it pgasyncslave su - postgres -c psql
    - select pg_reload_conf();

## Проверяем что к новому мастеру pgslave подключена реплика и она работает

- docker exec -it pgslave su - postgres -c psql
- select application_name, sync_state from pg_stat_replication;
- [pgasyncslave: ](writereport/pgasyncslave_new_connect.png)

## Восстановим старый мастер pgmaster как реплику

- Помечаем как реплику
    - touch volumes/pgmaster/standby.signal
- Изменяем конфиг pgmaster/postgresql.conf
    - primary_conninfo = 'host=pgslave port=5432 user=replicator password=pass application_name=pgmaster'
    - комментируем synchronous_standby_names (Так как больше не мастер)
- Запустим pgmaster
    - docker start pgmaster
- Убедимся что pgmaster подключился как реплика к pgslave
    - docker exec -it pgslave su - postgres -c psql
    - select application_name, sync_state from pg_stat_replication;
    - [Результат: ](writereport/pgslave_new_master.png)

## Проверяем, есть ли потери транзакций

- [Результат: ](writereport/result.png)
- Потерь нет!!!
