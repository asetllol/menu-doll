# Menu Doll (Fabric, Minecraft 26.2, client-only)

Только визуально: кукла твоего скина справа от центра главного меню, смотрит на курсор.

Сборка: JDK 25, `./gradlew build` -> `build/libs/menu-doll-1.0.0.jar`.
Если нет gradle wrapper: скопируй `gradlew`, `gradlew.bat`, папку `gradle/` из fabric-example-mod
(или выполни `gradle wrapper`).

Строки с комментарием `// CHECK` в `MenuDoll.java` — единственные места, где имена API 26.2
я не смог проверить компиляцией; если IDE подчеркнёт — поправь по автодополнению.
