# Criar uma missão

O conteúdo das missões antigas é declarado em `game/MissionCatalog.kt`. Cada `MissionDefinition` tem um identificador, título, objeto interativo e trilhas de idade. Cada trilha define contexto, objetivo, etapa inicial e uma lista de etapas identificadas. A investigação piloto usa os arquivos separados `game/InvestigationModel.kt`, `game/InvestigationCatalog.kt` e `game/InvestigationReducer.kt`.

## Investigação por aplicativos

`InvestigationCatalog` declara uma trilha por faixa etária com conversa, pistas associadas a Mensagens ou Navegador, conjunto de evidências que habilita a decisão, alternativas, consequências, feedback, estado semântico da Capi e conclusão. Cada pista progride de disponível para descoberta e uso. O `InvestigationReducer` mantém estado e transições separados da UI; `InvestigationSnapshotCodec` valida a tentativa que pode ser salva localmente.

Para adicionar uma pista, associe-a a um `SimulatedAppId` existente e dê a ela um identificador único. Para alterar o que habilita a decisão, ajuste `evidenceRequiredForDecision`; a validação exige IDs existentes e evidência nos dois aplicativos. O conteúdo fica em texto, sem exigir sprites.

## Tipos de etapa

- `InformationStep` apresenta uma situação e pode expor detalhes examináveis. Use `minimumDetailsToInspect` para exigir evidências suficientes sem obrigar a abrir todas as pistas; `requiredDetailIds` reserva-se a detalhes específicos indispensáveis. As pistas examinadas permanecem revisáveis durante as decisões.
- `DecisionStep` contém a mensagem, a pergunta reflexiva da Capi e alternativas. Cada `MissionChoice` inclui seu `ChoiceOutcome`: consequência imediata, feedback educativo, estado da Capi e identificador da próxima etapa. Alternativas podem convergir para o mesmo nó.
- `ConclusionStep` encerra a trilha. A conclusão é registrada quando essa etapa é apresentada.

Use IDs únicos por trilha e conecte cada próxima etapa existente. Evite ciclos; uma tentativa deve conseguir chegar a uma conclusão. Para os dois grupos etários, escreva trilhas próprias que ajustem investigação e autonomia, em vez de apenas alongar os textos.

## Validação

`MissionValidator.validate(mission)` retorna mensagens legíveis para título/contexto/objetivo ausentes, conteúdo incompleto, etapas ou detalhes inválidos, destinos inexistentes, trilhas sem conclusão alcançável, ciclos e etapas inalcançáveis. `GameReducer.OpenMission` recusa missões inválidas; a lógica e seus testes não carregam imagens.

Confira o catálogo com os testes locais do módulo Android usando `./gradlew testDebugUnitTest`; o build de depuração pode ser conferido com `./gradlew assembleDebug`.
