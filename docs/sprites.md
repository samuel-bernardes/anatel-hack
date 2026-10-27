# Preparar sprites sem acoplar o jogo a imagens

O jogo funciona sem sprites. As regras e o catálogo ficam em `app/src/main/java/com/example/naomordaaisca/game`; os composables em `MainActivity.kt` exibem texto, painéis e botões provisórios. `GameReducer` e `MissionValidator` não dependem de Android, Compose nem recursos gráficos.

## Associação futura

Personagens são representados por `CharacterCue(characterId, mood)`, usando `CharacterId` e `CharacterMood`; as alternativas da investigação também declaram o estado da Capi. A camada de interface poderá mapear esses valores para um recurso, por exemplo `CAPI + THINKING` para `R.drawable.character_capi_thinking`. Interações antigas usam `InteractableId` (`COMPUTER`, `PHONE` ou `CAPI`). A investigação identifica dispositivos e apps com `SimulatedDeviceId` e `SimulatedAppId` (`PHONE`, `COMPUTER`, `MESSAGES`, `BROWSER`); esses identificadores podem ser associados a objetos ou ícones pela apresentação. A ausência de mapeamento deve manter os rótulos textuais atuais.

Missões apresentam `MessageContent`, `InformationStep`, `DecisionStep` e `ConclusionStep` à interface. Textos, detalhes, alternativas e feedback permanecem dados renderizáveis; não devem ser embutidos em imagens. A lógica encaminha o jogador usando os identificadores de etapa e os resultados das alternativas, sem referência a `R.drawable`.

## Organização sugerida quando os arquivos existirem

Use os recursos Android em `app/src/main/res/drawable/` com nomes semânticos:

- `character_capi_neutral.webp`, `character_capi_thinking.webp`, `character_capi_surprised.webp`, `character_capi_explaining.webp`.
- `character_pescador_presenting_offer.webp`.
- `object_computer.webp`, `object_phone.webp`.
- Ícones de interface com nomes como `ui_icon_hint.webp`.

Uma tabela na camada `ui`, por exemplo `SpriteCatalog.kt`, pode converter `CharacterCue`, `InteractableId` e IDs de dispositivo/app em recursos. Só os composables precisam consultar essa tabela; catálogo, reducer e validação continuam funcionando e testáveis sem assets. Conversas, páginas, pistas e alternativas permanecem texto renderizado pela interface, e não devem ser embutidas em imagens. Não há arquivos de imagem vazios ou fictícios no projeto.

## Conteúdo de missões

As trilhas etárias e etapas estão em `MissionCatalog.kt`; `MissionValidator.validate` informa conteúdo incompleto, referências quebradas, etapas inalcançáveis, ciclos e trilhas sem conclusão alcançável. Antes de publicar conteúdo, valide cada missão e corrija as mensagens reportadas.
