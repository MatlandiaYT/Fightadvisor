# FightAdvisor (Fabric 1.21.4)

Mod kliencki. Porównuje Twoje HP z HP przeciwnika (ten na celowniku albo ostatnio
uderzony w ciągu 5 s, do 10 bloków) i pokazuje na środku ekranu:

- **ORAJ!** (zielony) - masz wyraźną przewagę (>= 1,3x)
- **RÓWNO - UWAŻAJ** (żółty) - zbliżone siły (0,8x - 1,3x)
- **WYCOFAJ SIĘ** (czerwony) - przeciwnik jest mocniejszy (< 0,8x)

Do oceny liczy się: zdrowie + złote serca (absorpcja), skorygowane o pancerz.
Nie uwzględnia broni, efektów mikstur ani ping/lagów - to tylko podpowiedź.

Klawisz **V** włącza/wyłącza (do zmiany w Sterowaniu).

## Budowanie
Wymagana Java 21.

    gradle wrapper --gradle-version 8.12
    ./gradlew build

Gotowy mod: `build/libs/fightadvisor-1.0.0.jar` (NIE ten z `-sources`).
Do folderu `mods` profilu Fabric 1.21.4 + Fabric API 0.118.0+1.21.4.

Uwaga: część serwerów zakazuje modów z podpowiedziami w walce - sprawdź regulamin.
