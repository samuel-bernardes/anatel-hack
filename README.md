# Não Morda a Isca!

Jogo educativo offline para crianças e adolescentes de 8 a 14 anos. O jogador investiga situações digitais, escolhe como agir e recebe consequências narrativas e feedback educativo.

<p>
  <img src="screenshots/04-tela-inicial.png" width="200">
  <img src="screenshots/07-send-conversa-lia.png" width="200">
  <img src="screenshots/12-escolhas-liberadas.png" width="200">
  <img src="screenshots/13-consequencia.png" width="200">
</p>

**Veja o passo a passo do protótipo, com prints de cada tela, em [docs/PROTOTIPO.md](docs/PROTOTIPO.md).**

## Tecnologias

- Kotlin e Android.
- Jetpack Compose com Material 3; telas seguem o Figma "Não Morda a Isca" (celular simulado com tema maré noturna).
- Gradle Wrapper 9.6.0 e Android Gradle Plugin 9.4.1.
- Conteúdo das missões declarado em Kotlin; não há serviço de rede. Ícones (vetores), papel de parede e fotos foram exportados do Figma para `res/drawable*`; fontes Inter e IBM Plex Mono (OFL) ficam em `res/font`.

## Pré-requisitos

- Android Studio ou uma instalação de linha de comando do Android SDK.
- Android SDK Platform 37, exigido pelo `compileSdk` atual.
- JDK 25 para o daemon Gradle, conforme `gradle/gradle-daemon-jvm.properties`. O projeto pode provisionar esse toolchain pela configuração do Gradle se ele não estiver instalado localmente.
- Para executar em hardware: dispositivo Android com API 23 ou superior. O manifesto também registra uma categoria de inicialização para Android TV.

O projeto declara `minSdk 23` e `targetSdk 37`. Isso descreve a configuração; não substitui testes nos dispositivos de destino.

## Executar e gerar o aplicativo

Abra o projeto no Android Studio e execute a configuração `app` em um dispositivo ou emulador. Pela linha de comando:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

O APK de depuração é gerado em `app/build/outputs/apk/debug/app-debug.apk`. Em uma máquina com ADB e dispositivo conectado, instale-o com:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

A primeira compilação pode precisar baixar o Gradle e dependências. Depois de instalado, o jogo usa conteúdo local e não precisa de conexão de rede.

## Organização

- `app/src/main/java/com/example/naomordaaisca/MainActivity.kt`: estado do jogo e roteamento entre as telas.
- `app/src/main/java/com/example/naomordaaisca/ui/`: tema e tokens (`theme/`), componentes do celular simulado (`kit/`), início (`hub/`), celular da investigação — bloqueio, tela inicial, Send, Navegador e resultados (`investigation/`) — e missões antigas (`legacy/`).
- `app/src/main/java/com/example/naomordaaisca/game/`: modelo, reducer das missões antigas e partes separadas do motor investigativo (`InvestigationModel.kt`, `InvestigationCatalog.kt`, `InvestigationReducer.kt`).
- `app/src/main/java/com/example/naomordaaisca/data/`: conclusões e tentativa de investigação ativa em SharedPreferences.
- `app/src/test/`: testes unitários da lógica e do catálogo.
- `docs/`: estado, arquitetura, decisões pedagógicas, testes e roteiro de demonstração.

Consulte [docs/DEMONSTRACAO.md](docs/DEMONSTRACAO.md) para apresentar o fluxo e [docs/TESTES.md](docs/TESTES.md) para os limites de validação.

## Fluxo de investigação

No início, selecione a faixa etária e toque em **Pegar o celular**. Desbloqueie o celular, abra o **Send** e a conversa com Lia, examine e anote as pistas; depois abra o **Navegador** pela tela inicial e faça o mesmo. As escolhas aparecem como respostas na conversa. Para a primeira decisão, use a pista que mostra o pedido da página e a confirmação pelo canal conhecido. Voltar entre os dois aplicativos mantém o progresso. A tentativa ativa é salva localmente e pode ser retomada após uma interrupção; sair ou reiniciar exige confirmação.

As outras duas missões permanecem acessíveis em **Outras experiências** pelo fluxo anterior enquanto a nova abordagem é validada.
