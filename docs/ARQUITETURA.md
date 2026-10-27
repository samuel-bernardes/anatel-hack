# Arquitetura

## Organização

- `MainActivity.kt` contém `GameApp` e os composables de hub, missão, feedback e conclusão. A tela mostra o estado apresentado e envia `GameAction` ao reducer.
- `game/GameModel.kt` declara identificadores, faixas etárias, etapas, alternativas, resultados, sessão e estado global.
- `game/MissionCatalog.kt` contém textos e caminhos das três missões.
- `game/GameReducer.kt` é a função de transição pura, sem dependência de Android, Compose, armazenamento ou imagens.
- `game/MissionValidator.kt` valida dados antes de uma missão ser aberta e também é exercitado pelos testes.
- `game/InvestigationModel.kt` contém identificadores, dados da investigação e estado de sessão.
- `game/InvestigationCatalog.kt` contém o conteúdo dos apps e as trilhas de texto por faixa etária.
- `game/InvestigationReducer.kt` processa ações e transições e contém o codec puro de snapshot.
- `data/SharedPreferencesGameProgressRepository.kt` mantém as conclusões e a tentativa ativa localmente.

O conteúdo está declarado em Kotlin, não em JSON. Não existe parser externo: `MissionCatalog` é o catálogo local usado pelo hub, pelo reducer e pelo validador.

## Modelo de missão

`MissionDefinition` reúne identificador, título, objeto interativo e uma trilha para cada `AgeBand`. Cada `MissionTrack` tem contexto, objetivo, etapa inicial e etapas identificadas:

- `InformationStep`: texto e detalhes examináveis, com quantidade mínima e itens obrigatórios opcionais.
- `DecisionStep`: mensagem estruturada, pergunta da Capi e alternativas.
- `ConclusionStep`: conclusão da trilha; a missão é marcada como concluída quando essa etapa é apresentada.

`MessageContent` mantém identidade apresentada, nome exibido, contexto, mensagem e personagem remetente separados. Cada `MissionChoice` aponta para um `ChoiceOutcome` com consequência, feedback educativo, estado semântico da Capi e próxima etapa. Os textos são apresentados pela interface, não incorporados em imagens.

## Estados e transições

`GameState` representa a tela atual, faixa selecionada, sessão ativa e missões concluídas. `MissionStatus` representa `NOT_STARTED`, `IN_PROGRESS`, `AWAITING_INTERACTION`, `AWAITING_CHOICE`, `PRESENTING_CONSEQUENCE` e `COMPLETED`. As ações são reduzidas por `GameReducer.reduce`.

O reducer rejeita ações fora do estado esperado. A escolha só avança se pertencer à decisão atual; o botão de continuar segue o destino declarado no resultado. `MissionValidator` verifica referências e alcance da conclusão. Um estado restaurado inconsistente volta ao hub.

## Apresentação e objetos gráficos

O hub prioriza a entrada por dispositivos. **Mensagens** abre a investigação ativa; **Navegador** fica disponível nela. As missões ainda não migradas aparecem em **Outras experiências**, pelo fluxo anterior. `MainActivity.kt` renderiza textos, painéis e botões de Compose TV Material e encaminha `GameAction`; não implementa consequências pedagógicas.

## Investigação por dispositivos

`InvestigationDefinition` liga um identificador e título às trilhas etárias do catálogo. `InvestigationSession` guarda missão/faixa, etapa narrativa, progresso das pistas e decisões. `InvestigationNavigation` mantém o app aberto e o histórico de apps separado da etapa; `InvestigationDialog` guarda apenas confirmações temporárias. `InvestigationReducer.availableChoices` determina quando e quais decisões podem ser exibidas. A tela exibida não é a fonte do progresso. `GameReducer` encaminha ações ao `InvestigationReducer`, que não importa Android ou Compose.

`InvestigationCatalog` declara pistas ligadas a `SimulatedAppId.MESSAGES` ou `BROWSER`, requisitos de evidência, alternativas, consequências, feedback, estado semântico da Capi e conclusão. `InvestigationStage` descreve investigação, consequência, recuperação e conclusão. A primeira decisão só é habilitada depois que as evidências requeridas forem descobertas e marcadas como usadas. O catálogo valida referências, conteúdo mínimo e IDs. A ausência de sprites não muda nenhuma regra.

Voltar entre aplicativos restaura o aplicativo anterior. Sair durante a investigação e reiniciar pedem confirmação; cancelar mantém o estado. A tentativa concluída permanece disponível para revisão/reinício pelo hub. Missões legadas continuam no caminho anterior e não foram migradas nesta etapa.

## Progresso e dados locais

`SharedPreferencesGameProgressRepository` armazena os IDs concluídos e um snapshot versionado da investigação ativa. `InvestigationSnapshotCodec` codifica apenas IDs e estados enumerados; decodificação rejeita versões, referências ou combinações inválidas. A recuperação inválida descarta a tentativa e mantém conclusões válidas. Não há conta, rede, telemetria ou dado pessoal.

O `Saver` da atividade mantém os estados no ciclo de restauração da UI; SharedPreferences permite recuperar a tentativa depois do encerramento do processo. O codec e o reducer são testados sem carregar Compose ou imagens. A persistência foi exercitada por round-trip de snapshot nos testes unitários, mas não houve teste de encerramento real em dispositivo nesta etapa.
