# Testes e validação

## Testes automatizados

Suítes: `app/src/test/java/com/example/naomordaaisca/game/GameReducerTest.kt` e `app/src/test/java/com/example/naomordaaisca/game/InvestigationTest.kt`, executadas pelo task Gradle `testDebugUnitTest`.

Os testes cobrem catálogo e trilhas etárias antigas, referências, ações inválidas/repetidas, escolhas e feedback, conclusão, repetição e recuperação de estado. Para a investigação nova, cobrem referências de conteúdo, associação das pistas aos dois apps, gate de evidências descobertas/usadas, navegação e retomada, alternativas e recuperação em ambas as faixas, saída/reinício com confirmação, rejeição de estado inválido e round-trip do snapshot após interrupção.

## Comandos

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Na validação mais recente, `./gradlew testDebugUnitTest assembleDebug` terminou com sucesso: **33 testes passaram, sem falhas ou erros**, e o APK debug foi gerado em `app/build/outputs/apk/debug/app-debug.apk`. A suíte de investigação inclui também validação das definições registradas e da disponibilidade de escolhas calculada pelo reducer.

O R8 mostrou avisos de tipos não encontrados em referências de `androidx.compose.ui.graphics`, `androidx.window` e Coroutines (`java.lang.instrument`/`sun.misc`). Eles não impediram o empacotamento. Não foram suprimidos nesta revisão; valide o APK release em dispositivo e investigue compatibilidade das referências antes de distribuir.

## Limites

- Não há testes instrumentados Compose em `app/src/androidTest`.
- `adb` não está instalado neste ambiente; não foi possível executar `connectedDebugAndroidTest`, instalar o APK ou testar numa TV Box/emulador.
- Portanto, o build confirma compilação e empacotamento; os testes unitários confirmam regras puras e validação do catálogo. Eles não confirmam foco do controle remoto, tamanho visual de textos, contraste, leitor de tela ou comportamento de instalação em hardware.
- A persistência real de SharedPreferences e o encerramento forçado do processo não foram testados em dispositivo; os testes cobrem codec e recuperação do snapshot sem Android.
