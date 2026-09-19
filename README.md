# MyTasks

Android-приложение для управления задачами на русском языке. Задачи делятся по
категориям Work / Home и размеру (Daily / Medium / Large), имеют статус
(Started, Waiting, Paused, Stopped) и дедлайн с напоминанием.

Ключевая особенность — **создание и поиск задач через нейросеть**: голосом или
текстом, через AI-провайдера (ProxyAPI, OpenAI-совместимые модели). Запрос
«купить молоко завтра в 18» превращается в задачу с дедлайном.

## Возможности

- Списки задач Work / Home, вкладки Daily / Medium / Large
- Голосовой ввод (распознавание русской речи) и текстовый
- AI-создание и AI-поиск задач
- Напоминания в настраиваемое время (AlarmManager, переживают перезагрузку)
- Тёмная/светлая тема Material3
- Вход через Google-аккаунт (подготовка к синхронизации между устройствами)

## Стек

Kotlin 2.x · Jetpack Compose + Material3 · MVVM (ViewModel + StateFlow) ·
Hilt · Room · Retrofit/OkHttp · DataStore · minSdk 26 / targetSdk 34

## Сборка и запуск

Требуется JDK 21 и Android Studio (Koala или новее).

```bash
./gradlew assembleDebug   # сборка APK
./gradlew installDebug    # установка на подключённое устройство/эмулятор
./gradlew test            # юнит-тесты
```

### local.properties

Перед сборкой создайте `local.properties` (не коммитится) с ключами:

```properties
backend.url=http://10.0.2.2:8080      # адрес бэкенда для debug (эмулятор)
backend.api.key=<секрет X-Api-Key>    # общий ключ приложения и бэкенда
google.serverClientId=<web-client-ID> # OAuth Web Client из Google Cloud
```

Без этих ключей приложение собирается и работает в direct-режиме (запросы к
AI-провайдеру напрямую, ключ вводится в настройках приложения).

## Режимы AI-подключения

- **Direct** — приложение само обращается к AI-провайдеру; ключ хранится в DataStore.
- **Backend** — запросы идут через собственный Go-бэкенд
  ([MyTasksBackend](../MyTasksBackend)); ключ провайдера и промпты остаются на
  бэкенде, авторизация по X-Api-Key, вход в аккаунт — Google Sign-In.

Переключается в настройках приложения («Подключение к нейросети»).

## Структура

Исходники: `app/src/main/java/com/shkarov/mytasks/` — `screens/` (Compose-экраны),
`viewmodels/`, `repository/` (включая роутинг direct/backend), `network/`
(Retrofit-интерфейсы и DTO), `data_base/` (Room), `settings/` (DataStore),
`worker/` (напоминания). Схемы Room-БД — в `app/schemas/`.
