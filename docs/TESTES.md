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

## Validação em TV Box real

Aparelho: TV Box genérica Allwinner H313 (Android 10, API 29, userspace 32 bits, 1 GB de RAM, tela 1280x720 a 160 dpi), instalação por `adb` pela rede e uso apenas com controle remoto (setas, OK e Voltar).

- **Release travava na abertura** no Android 10 (`IllegalAccessError` em `androidx.startup.InitializationProvider`), causado pela otimização R8 restrita por `packageScope`. Removido o `packageScope`; o release abre em cerca de 2 s (o debug leva 8 s) e usa cerca de 31 MB de RAM.
- Foco do controle era invisível. Agora o tema desenha um contorno claro no item focado.
- O celular simulado ficava esticado nos 1280 px e o dock da tela inicial era cortado. Agora a moldura fica centralizada (até 480 dp de largura) e a tela inicial rola quando falta altura.
- Ao examinar ou anotar uma pista, o foco caía no botão "Voltar" do topo. Agora vai para "Anotar como pista" e, depois, para o próximo item; quando as respostas são liberadas, o foco vai para a primeira.
- Os diálogos de sair/reiniciar começam na opção segura e não deixam o foco escapar para a tela de trás.
- Os textos de ajuda passaram de "toque para…" a "selecione para…", e os botões só decorativos do Navegador deixaram de receber foco.
- Fluxo completo validado com o controle: início, bloqueio, tela inicial, apps sem pistas, Send, Navegador, escolha de risco, recuperação, conclusão, reinício e Voltar. A tentativa e a conclusão sobreviveram a `am force-stop`.

Para instalar o release numa TV Box sem configurar assinatura no projeto, assine o APK com a chave de debug (`zipalign` + `apksigner` do build-tools) e use `adb install -r`.

## Limites

- Não há testes instrumentados Compose em `app/src/androidTest`.
- Leitor de tela e contraste não foram avaliados formalmente.
