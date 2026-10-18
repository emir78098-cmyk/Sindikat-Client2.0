# Sindikat Client (Fabric, Minecraft 26.1.2)

Модули: **ESP** (Box / Glow / Both), **Zoom** (удерживать C).
Меню: **Right Shift**. Настройки сохраняются в `config/sindikat.json`.

## Сборка
Нужен **JDK 25** (Minecraft 26.1 требует Java 25).
1. Один раз: `gradle wrapper --gradle-version 9.4.1`
2. `./gradlew build` (Windows: `gradlew.bat build`)
3. Готовый jar: `build/libs/sindikat-client-1.0.0.jar`

Либо загрузи папку в GitHub: Actions → build → Artifacts → Sindikat-Client.

Для запуска: Fabric Loader 0.19.2+ и Fabric API (0.155.3+26.1.2) для Minecraft 26.1.2.
