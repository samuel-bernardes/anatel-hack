# Estado atual

## Concluído

- Aplicativo Android em Kotlin com telas de hub, missão, feedback e conclusão feitas em Jetpack Compose para TV.
- Seleção de faixa etária, abertura de missão pelo hub, inspeção opcional ou obrigatória de detalhes, ajuda da Capi, escolha, consequência, conclusão, retorno ao hub e repetição.
- Três missões com conteúdo separado da apresentação: oferta de item em jogo, mensagem relacionada à escola e apoio a alguém alvo de agressões num grupo.
- Duas trilhas de conteúdo por missão: Explorador (8–10) e Agente (11–14).
- Validação do catálogo contra referências inválidas, conteúdo incompleto, etapas inalcançáveis e ciclos.
- Fluxo de investigação inicial em dispositivos: Mensagens e Navegador, pistas disponíveis/descobertas/usadas, decisões, feedback e uma recuperação possível para a escolha de maior risco.
- Alternância entre Mensagens e Navegador sem perder a tentativa; voltar, sair e reiniciar têm comportamentos separados. Sair e reiniciar durante uma tentativa pedem confirmação.
- Tentativa ativa e IDs de missões concluídas guardados localmente em SharedPreferences; snapshots inválidos são rejeitados sem apagar conclusões válidas.
- As outras duas missões seguem acessíveis pelo fluxo Compose antigo em “Outras experiências”.
- O jogo não coleta dados pessoais nem usa rede.
- Testes unitários da lógica e do conteúdo; builds debug e release executados com sucesso nesta revisão.

## Parcial

- A interface está funcional e textual, mas ainda não usa sprites. Personagens e objetos são identificados por texto e dados semânticos.
- O projeto tem dependências de teste Compose, mas não tem testes instrumentados de tela no repositório.
- A primeira investigação usa conteúdo declarado em Kotlin; ainda não há formato externo de autoria de casos.
- A tela de Mensagens mostra o resumo da conversa e pistas do caso. O navegador é uma lista de páginas informativas, sem simulador de navegação geral.
- A tentativa persistida foi validada por testes de codec/reducer, mas não por encerramento forçado do processo nem reinstalação em dispositivo.
- Ao voltar do feedback para reconsiderar, a escolha anterior permanece no histórico da tentativa e a decisão fica disponível de novo.

## Limitações conhecidas

- Não foi possível testar instalação, foco por controle remoto, leitura de textos e navegação em uma TV Box: `adb` não está disponível neste ambiente e nenhum dispositivo foi detectado.
- Não há validação manual ou automatizada da aparência em emulador nesta revisão. Os testes unitários e o build não comprovam ergonomia em tela grande.
- Se o estado salvo de uma atividade vier inválido, a lógica recupera para o hub sem exibir uma mensagem específica de erro.
- A variante release é gerada sem assinatura configurada (`app-release-unsigned.apk`); não está pronta para distribuição/publicação. A execução release também emitiu avisos R8 sobre tipos ausentes em referências de AndroidX e Coroutines, embora o APK tenha sido empacotado.
- Os finais das missões são cenários narrativos simulados. A aplicação não consulta um jogo, escola, grupo ou serviço real.
- Os canais oficiais descritos nas duas primeiras missões pertencem a situações fictícias e devem ser adaptados antes de uso associado a uma escola ou plataforma real.

## Pendências prioritárias

1. Validar foco visível, seleção por controle remoto, rolagem e texto em uma TV Box real; a navegação foi implementada com botões Compose TV, mas hardware não foi acessado.
2. Fazer revisão de conteúdo com educadores e adaptar exemplos de jogos ao contexto de uso.
3. Considerar testes instrumentados Compose e testar a restauração após encerrar forçadamente o processo em dispositivo.
4. Após validar a investigação inicial, decidir como migrar as missões escolares e de grupo para o novo formato sem remover o fluxo existente antes disso.
