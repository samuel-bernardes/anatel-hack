package com.example.naomordaaisca.game

/** Mission text and rules are data, independent of Compose and image assets. */
object MissionCatalog {
    val missions = listOf(
        MissionDefinition(
            id = MissionId.DIGITAL_OFFERS,
            title = "A isca no jogo",
            interactable = InteractableId.COMPUTER,
            ageTracks = mapOf(
                AgeBand.EXPLORER_8_10 to MissionTrack(
                    context = "Você está jogando online quando aparece uma oferta de uma skin rara.",
                    objective = "Descubra o que conferir antes de tentar ganhar o item.",
                    startingStepId = "game-discovery",
                    steps = listOf(
                        InformationStep("game-discovery", "Uma skin lendária", "No intervalo da partida, aparece uma oferta: uma skin rara grátis, disponível por pouco tempo. Parece combinar com o jogo que você está jogando.", emptyList(), nextStepId = "game-investigation"),
                        InformationStep("game-investigation", "Investigue a oferta", "Selecione alguns detalhes para descobrir mais. Você não precisa abrir todos para decidir como conferir.", listOf(
                            InspectableDetail("explorer-sender", "Quem enviou?", "O perfil usa o nome e o símbolo da equipe do jogo, mas não tem confirmação de que é o canal oficial."),
                            InspectableDetail("explorer-channel", "Como chegou?", "A oferta apareceu numa mensagem direta depois da partida, não no aviso dentro do jogo."),
                            InspectableDetail("explorer-login", "O que o site pede?", "A página pede a senha da conta e um código temporário para liberar a skin."),
                        ), nextStepId = "game-offer-decision", minimumDetailsToInspect = 2),
                        decision("game-offer-decision", "Equipe de Eventos", "Equipe de Eventos", "Mensagem direta sobre a skin rara", "A skin especial está quase esgotada. Acesse o portal da promoção para resgatar.", CharacterId.PESCADOR,
                            "O nome parece familiar. Que outro caminho você pode usar para conferir a oferta?",
                            choice("verify-launcher", "Abrir o jogo pelo ícone conhecido e conferir as notícias", "Você não usou o link recebido e procurou a promoção dentro do jogo.", "O nome do perfil não confirma quem escreveu. O launcher é um caminho independente para conferir a oferta.", CharacterMood.EXPLAINING, "official-result"),
                            choice("enter-login", "Digitar minha senha e o código no portal", "Esse pedido pode dar a outra pessoa acesso à sua conta.", "Uma página pode parecer oficial e ainda pedir algo perigoso. Senha e código são só seus.", CharacterMood.SURPRISED, "game-reconsider"),
                            choice("share-offer", "Enviar o link para um amigo decidir comigo", "Seu amigo também poderia confiar no link e informar os dados.", "Pedir ajuda é bom; encaminhar o link pode levar outra pessoa a entregar dados. Avise sem compartilhar a página.", CharacterMood.THINKING, "game-reconsider"),
                            choice("ask-adult", "Fechar a mensagem e pedir ajuda a um adulto", "Você interrompeu a interação antes de compartilhar dados.", "Boa pausa. Com um adulto, confira a oferta no jogo ou no canal oficial acessado por conta própria.", CharacterMood.EXPLAINING, "official-result")),
                        info("game-reconsider", "Você ainda pode mudar de rumo", "Feche o portal. Se digitou a senha ou o código, peça ajuda a um adulto e proteja a conta pelo aplicativo oficial. Se enviou o link, avise seu amigo para não abri-lo.", "game-recovery-choice"),
                        decision("game-recovery-choice", "Capi", "Capi", "O que fazer agora?", "A página continua aberta e o link pode ter sido compartilhado.", null,
                            "Qual ação ajuda a proteger a conta e as outras pessoas?",
                            choice("close-and-alert", "Fechar, pedir ajuda e avisar quem recebeu o link", "Você interrompeu o contato e ajudou seu amigo a evitar a página.", "Se informou credenciais, altere a senha pelo canal oficial com ajuda de um adulto. Nunca compartilhe códigos de autenticação.", CharacterMood.EXPLAINING, "official-result"),
                            choice("keep-going", "Continuar porque o perfil parece oficial", "O perfil ainda não foi confirmado e o pedido continua arriscado.", "Ainda dá para fechar a página e pedir ajuda. Aparência e urgência não substituem uma confirmação independente.", CharacterMood.THINKING, "official-result")),
                        info("official-result", "Conferindo por outro caminho", "No aviso aberto pelo próprio jogo, a equipe confirma que promoções aparecem nas notícias oficiais e nunca pede senha nem código. Esta skin não aparece no canal oficial.", "game-conclusion"),
                        ConclusionStep("game-conclusion", "A skin e a verificação", "Uma oferta pode ser real, mas confirme por um canal oficial que você abriu por conta própria. Não compartilhe senha nem código."),
                    ),
                ),
                AgeBand.AGENT_11_14 to MissionTrack(
                    context = "Você está numa partida online quando uma oferta de skin rara aparece logo depois de um torneio da comunidade.",
                    objective = "Compare sinais que parecem confiáveis com aquilo que ainda precisa ser confirmado.",
                    startingStepId = "agent-discovery",
                    steps = listOf(
                        InformationStep("agent-discovery", "Uma oferta depois do torneio", "Você participou de um torneio da comunidade. Pouco depois, um perfil com o símbolo do jogo oferece uma skin rara grátis. O texto está bem escrito e o item combina com o jogo.", emptyList(), nextStepId = "agent-investigation"),
                        InformationStep("agent-investigation", "Compare as evidências", "Alguns sinais parecem familiares; outros podem mudar sua avaliação. Examine pelo menos dois detalhes. Os demais continuam disponíveis se quiser rever.", listOf(
                            InspectableDetail("agent-profile", "Perfil do remetente", "O nome é parecido com o da equipe de eventos e usa o símbolo do jogo. A conta, porém, não está vinculada ao perfil oficial."),
                            InspectableDetail("agent-delivery", "Como a oferta chegou", "Foi uma mensagem direta depois do torneio, não uma publicação na área oficial de notícias."),
                            InspectableDetail("agent-address", "Endereço do portal", "O endereço contém o nome do jogo, mas usa um domínio diferente do site oficial que você costuma acessar."),
                            InspectableDetail("agent-credentials", "Etapa de resgate", "O portal pede a senha e um código de autenticação de uso único. A equipe oficial diz que não solicita códigos por mensagem."),
                        ), nextStepId = "agent-offer-decision", minimumDetailsToInspect = 2),
                        decision("agent-offer-decision", "Equipe de Eventos", "Equipe de Eventos", "Mensagem direta enviada após o torneio da comunidade", "A skin rara está reservada para participantes. Resgate-a no portal antes que a oferta termine.", CharacterId.PESCADOR,
                            "O perfil e a oferta parecem plausíveis, mas algumas informações ainda não foram confirmadas. Como você verifica sem depender do link?",
                            choice("verify-launcher", "Abrir o launcher e procurar o anúncio por conta própria", "Você separou a afirmação da mensagem e conferiu num canal independente.", "Boa decisão. A oferta em si não prova fraude; a verificação pelo launcher permite comparar o anúncio e evita entregar credenciais ao portal recebido.", CharacterMood.EXPLAINING, "agent-official-result"),
                            choice("enter-login", "Entrar no portal porque o perfil usa o símbolo do jogo", "O símbolo e o texto bem escrito não confirmaram a identidade do site.", "Marcas e aparência podem ser copiadas. Senha e código dão acesso à conta; não os informe nesse portal.", CharacterMood.SURPRISED, "agent-reconsider"),
                            choice("forward", "Encaminhar para o grupo e perguntar se alguém conhece", "Outras pessoas poderiam abrir o link antes de saber se é oficial.", "É válido pedir uma segunda opinião, mas compartilhe a dúvida sem encaminhar o link. Confira com um adulto ou pelo suporte oficial.", CharacterMood.THINKING, "agent-reconsider"),
                            choice("ask-adult", "Guardar a mensagem e consultar um adulto ou o suporte oficial", "Você evitou agir sob pressão e buscou uma confirmação independente.", "Boa pausa. Não use o contato ou o endereço fornecido pela própria oferta para confirmar quem a enviou.", CharacterMood.EXPLAINING, "agent-official-result")),
                        info("agent-reconsider", "Reavalie os sinais", "O perfil pode parecer familiar e ainda assim não ser oficial. Feche o portal. Se informou credenciais, proteja a conta pelo aplicativo oficial e avise um adulto; se compartilhou o link, alerte o grupo.", "agent-recovery-choice"),
                        decision("agent-recovery-choice", "Capi", "Capi", "Reduzir o risco", "O link já foi aberto ou pode ter chegado a outras pessoas.", null,
                            "Qual ação responde melhor ao que aconteceu?",
                            choice("contain", "Fechar, alertar quem recebeu e proteger a conta pelo canal oficial", "Você interrompeu a interação e reduziu a chance de outras pessoas seguirem o link.", "Se digitou senha ou código, mude a senha pelo aplicativo oficial com ajuda de um adulto e siga as orientações de segurança da conta.", CharacterMood.EXPLAINING, "agent-official-result"),
                            choice("ignore", "Deixar como está porque talvez a oferta seja verdadeira", "A identidade do portal continua sem confirmação e as credenciais podem estar expostas.", "Você ainda pode fechar e pedir ajuda. Verifique a promoção por um canal oficial, mesmo quando não há certeza imediata.", CharacterMood.THINKING, "agent-official-result")),
                        info("agent-official-result", "O que o canal oficial confirma", "Nas notícias abertas pelo launcher, a equipe informa que promoções válidas são anunciadas ali e que nunca pede senha ou código por mensagem. A skin não está listada. A aparência profissional do portal não confirma sua legitimidade.", "agent-conclusion"),
                        ConclusionStep("agent-conclusion", "Uma decisão com evidências", "Ofertas podem ser legítimas, mas confira por um canal oficial independente. Não forneça senha ou código a um portal recebido por mensagem."),
                    ),
                ),
            ),
        ),
        MissionDefinition(
            id = MissionId.PHONE_MESSAGES,
            title = "A mensagem da escola",
            interactable = InteractableId.PHONE,
            ageTracks = mapOf(
                AgeBand.EXPLORER_8_10 to MissionTrack(
                    context = "A Feira de Ciências está marcada para sexta. A professora comentou que o horário talvez mude.",
                    objective = "Descubra se a mudança é real e onde confirmar o transporte.",
                    startingStepId = "school-discovery",
                    steps = listOf(
                        InformationStep("school-discovery", "A feira mudou de dia?", "No grupo da turma, alguém chamado “Coordenação” diz que a feira passou para quinta e pede confirmação do transporte. A data importa: você já preparou seu trabalho.", emptyList(), nextStepId = "school-investigation"),
                        InformationStep("school-investigation", "O que dá para confirmar?", "Examine pelo menos um detalhe. Você não precisa abrir todos para decidir como conferir.", listOf(
                            InspectableDetail("school-explorer-sender", "Quem enviou?", "O nome diz “Coordenação”, mas o número não está salvo como contato da escola."),
                            InspectableDetail("school-explorer-context", "O que você já sabe?", "A professora comentou que o horário talvez mudasse, mas ainda não disse o novo dia."),
                            InspectableDetail("school-explorer-request", "O que a mensagem pede?", "Para reservar o transporte, pede nome completo e turma por mensagem privada."),
                        ), nextStepId = "school-decision", minimumDetailsToInspect = 1),
                        decision("school-decision", "Coordenação", "Coordenação", "Mensagem no grupo da turma sobre a Feira de Ciências", "A feira será quinta. Confirme o transporte enviando seu nome completo e turma para este número.", null,
                            "A mudança pode ser verdadeira, mesmo que o pedido ainda precise de confirmação. Como você confere?",
                            choice("ask-adult", "Pedir a um responsável para conferir no app da escola", "Você evitou enviar dados e buscou a informação num canal conhecido.", "Boa forma de conferir: a mensagem pode trazer uma data certa sem que o pedido de dados venha da escola.", CharacterMood.EXPLAINING, "school-official-result"),
                            choice("send-details", "Enviar nome e turma para reservar", "O número recebeu seus dados, mas ainda não confirmou que pertence à escola.", "O pedido pode parecer razoável, mas confirme quem receberá seus dados pelo app escolar ou com um responsável.", CharacterMood.THINKING, "school-reconsider"),
                            choice("forward", "Repassar ao grupo como aviso confirmado", "Outras famílias podem agir com base numa mensagem ainda não verificada.", "A data pode estar certa, mas encaminhar como oficial também espalha o pedido de dados. Confira primeiro.", CharacterMood.SURPRISED, "school-reconsider"),
                            choice("ignore", "Apagar e não conferir", "Você não enviou dados, mas pode perder a mudança da feira.", "Um remetente desconhecido não prova que a informação é falsa. Confira por um canal conhecido antes de ignorar.", CharacterMood.THINKING, "school-official-result")),
                        info("school-reconsider", "Ainda dá para conferir", "Pare de enviar informações. Se compartilhou a mensagem, avise o grupo que a escola ainda não confirmou o pedido. Um responsável pode abrir o app escolar com você.", "school-recovery-choice"),
                        decision("school-recovery-choice", "Capi", "Capi", "Como confirmar agora?", "O grupo aguarda uma resposta sobre o transporte.", null,
                            "Que caminho pode confirmar a data e também quem deve receber os dados?",
                            choice("known-channel", "Conferir no app escolar com um responsável", "Você verificou a informação por um canal que já conhece.", "No app, confirme tanto o novo dia quanto onde fazer a inscrição. Se repassou o aviso, corrija o grupo com a informação confirmada.", CharacterMood.EXPLAINING, "school-official-result"),
                            choice("ask-sender", "Perguntar ao mesmo número se é da escola", "O número pode afirmar que é oficial, mas isso não confirma sua identidade.", "Uma resposta do próprio remetente não é uma confirmação independente. Confira no app escolar ou com a professora.", CharacterMood.THINKING, "school-official-result")),
                        info("school-official-result", "A informação e o pedido", "A professora confirma: a feira mudou para quinta. Mas as inscrições do transporte são feitas no app da escola, e ela não pediu que os alunos enviassem dados àquele número.", "school-conclusion"),
                        ConclusionStep("school-conclusion", "Duas coisas para conferir", "A data era verdadeira; o pedido por mensagem não era o canal da escola. Confira a informação e quem a enviou antes de compartilhar dados."),
                    ),
                ),
                AgeBand.AGENT_11_14 to MissionTrack(
                    context = "A Feira de Ciências está marcada para sexta. A professora avisou que uma assembleia talvez altere o horário; o app escolar ainda não publicou atualização.",
                    objective = "Separe a veracidade da mudança da identidade de quem pede os dados.",
                    startingStepId = "agent-school-discovery",
                    steps = listOf(
                        InformationStep("agent-school-discovery", "Uma mudança possível", "Antes de o app escolar atualizar, chega de “Coordenação”: “A feira será quinta. Confirme o transporte por resposta privada.” A data parece possível, mas o número e o pedido ainda precisam ser conferidos.", emptyList(), nextStepId = "agent-school-investigation"),
                        InformationStep("agent-school-investigation", "Compare a mensagem", "A mudança combina com o que você ouviu. Isso confirma também o remetente e o pedido? Examine pelo menos dois detalhes; os outros ficam disponíveis para consulta.", listOf(
                            InspectableDetail("school-agent-sender", "Identidade apresentada", "O nome e a assinatura dizem “Coordenação”, mas o número não aparece entre os contatos oficiais salvos pela família."),
                            InspectableDetail("school-agent-context", "Contexto da mudança", "A assembleia pode alterar a programação, mas a professora ainda não confirmou o novo dia."),
                            InspectableDetail("school-agent-request", "Pedido de inscrição", "A mensagem pede nome completo e turma por resposta privada para reservar o transporte."),
                            InspectableDetail("school-agent-channel", "Canal conhecido", "A escola costuma publicar avisos e inscrições no app. A atualização ainda não está lá."),
                        ), nextStepId = "agent-school-decision", minimumDetailsToInspect = 2),
                        decision("agent-school-decision", "Coordenação", "Coordenação", "Mensagem sobre a mudança da Feira de Ciências", "A feira passou para quinta. Para reservar seu lugar no transporte, responda com nome completo e turma hoje.", null,
                            "A assembleia torna a mudança plausível, mas a identidade e o pedido são questões separadas. O que você faz?",
                            choice("check-app", "Conferir o aviso e a inscrição no app escolar", "Você verificou a data e o canal para enviar os dados antes de agir.", "O contexto torna a mudança possível, não autentica o remetente. O app conhecido pode confirmar a data e mostrar onde se inscrever.", CharacterMood.EXPLAINING, "agent-school-result"),
                            choice("reply-private", "Responder com os dados para garantir o transporte", "O número recebeu seus dados sem que sua identidade fosse confirmada.", "A inscrição pode ser legítima; confirme por que canal a escola recebe os dados antes de enviá-los.", CharacterMood.THINKING, "agent-school-review"),
                            choice("forward-group", "Repassar ao grupo como informação oficial", "A turma pode tratar a data e o pedido de dados como confirmados.", "Mesmo uma data correta não confirma quem enviou o pedido. Verifique antes de encaminhar como oficial.", CharacterMood.SURPRISED, "agent-school-review"),
                            choice("wait", "Esperar o aviso no app ou perguntar à professora", "Você adiou a resposta até encontrar uma confirmação confiável.", "Esperar por pouco tempo pode ser sensato quando o canal conhecido ainda não atualizou. Confira de novo para não perder uma mudança real.", CharacterMood.EXPLAINING, "agent-school-result")),
                        info("agent-school-review", "A mensagem ainda pode ser corrigida", "Não envie mais dados. Se encaminhou a mensagem, avise o grupo que ainda falta confirmação. Se respondeu, conte a um responsável o que enviou e confiram a conta pelo app oficial.", "agent-school-recovery"),
                        decision("agent-school-recovery", "Capi", "Capi", "Qual confirmação vale?", "O prazo de resposta está chegando, mas o app escolar continua sendo o canal conhecido.", null,
                            "Como confirmar tanto a informação quanto o destino dos dados?",
                            choice("known-school-channel", "Abrir o app com um responsável ou falar com a professora", "Você conferiu a mensagem numa fonte que não depende do número que a enviou.", "Esse caminho confirma separadamente a data e onde fazer a inscrição. Se compartilhou o aviso, corrija o grupo com o que a escola confirmar.", CharacterMood.EXPLAINING, "agent-school-result"),
                            choice("trust-sender-claim", "Perguntar ao número se ele é mesmo da coordenação", "Uma resposta afirmando ser oficial não confirma a identidade do contato.", "A pessoa pode repetir a própria afirmação. Use o app escolar ou a professora para verificar por outro canal.", CharacterMood.THINKING, "agent-school-result")),
                        info("agent-school-result", "A data era real; o pedido não", "A professora confirma a mudança para quinta. A inscrição do transporte é feita no app da escola; ela não pediu que os alunos enviassem dados àquele número.", "agent-school-conclusion"),
                        ConclusionStep("agent-school-conclusion", "Separe fato e remetente", "Uma mensagem pode trazer uma informação verdadeira sem que o remetente ou o pedido sejam oficiais. Confirme a informação e o canal antes de enviar dados ou compartilhar."),
                    ),
                ),
            ),
        ),
        MissionDefinition(
            id = MissionId.GROUP_CHAT_SUPPORT,
            title = "Uma conversa no grupo",
            interactable = InteractableId.PHONE,
            ageTracks = mapOf(
                AgeBand.EXPLORER_8_10 to MissionTrack(
                    context = "No grupo da turma, alguém fez piadas repetidas sobre o desenho de Davi. Ele parou de responder.",
                    objective = "Ajude Davi sem chamar mais atenção para ele e pense quando pedir ajuda.",
                    startingStepId = "group-discovery",
                    steps = listOf(
                        InformationStep(
                            "group-discovery", "O grupo ficou em silêncio",
                            "As piadas continuam no grupo da turma. Você quer ajudar Davi, mas não sabe se ele quer conversar.",
                            listOf(
                                InspectableDetail("group-repetition", "O que aconteceu?", "Não foi uma piada isolada: mensagens parecidas apareceram várias vezes."),
                                InspectableDetail("group-silence", "Como Davi reagiu?", "Davi parou de responder. Isso não diz sozinho o que ele quer que os outros façam."),
                            ), nextStepId = "group-decision", minimumDetailsToInspect = 0,
                        ),
                        decision("group-decision", "Conversa da turma", "Grupo da turma", "Mensagens sobre o desenho de Davi", "As piadas aparecem de novo. Davi não respondeu.", null,
                            "Como você pode apoiar sem falar por Davi?",
                            choice("private-support", "Mandar uma mensagem gentil em particular e perguntar se quer ajuda", "Davi agradece, mas diz que não quer conversar agora. Você respeita o espaço dele.", "Apoiar é oferecer ajuda sem pressionar. Você não precisa resolver tudo sozinho.", CharacterMood.EXPLAINING, "group-conclusion"),
                            choice("tell-adult", "Contar em particular a um adulto de confiança", "O adulto pode ajudar a interromper as mensagens sem expor Davi no grupo.", "Se as piadas continuam ou deixam alguém triste, conte a um adulto. Você não precisa saber se quem escreveu queria machucar para pedir ajuda.", CharacterMood.EXPLAINING, "group-conclusion"),
                            choice("laugh-along", "Reagir com risada para acompanhar o grupo", "Mais colegas reagem e veem as piadas. Você percebe que pode parar de participar.", "Curtir ou repassar pode fazer a piada chegar a mais colegas. Ainda dá para mudar de atitude.", CharacterMood.THINKING, "group-reconsider")),
                        info("group-reconsider", "Você pode mudar o rumo", "Pare de repassar as mensagens. Se puder, tire sua reação. Você também pode escrever para Davi em particular ou contar a um adulto.", "group-recovery"),
                        decision("group-recovery", "Capi", "Capi", "Escolha um próximo passo", "As mensagens continuam aparecendo no grupo.", null,
                            "Qual passo ajuda sem obrigar Davi a falar?",
                            choice("repair-private", "Oferecer apoio em particular e respeitar se ele não quiser conversar", "Você interrompeu sua participação e deixou Davi escolher se quer falar.", "Respeitar o espaço da pessoa é parte do apoio. Se as mensagens continuarem, procure um adulto de confiança.", CharacterMood.EXPLAINING, "group-conclusion"),
                            choice("repair-adult", "Contar em particular a um adulto de confiança", "Um adulto pode ajudar a cuidar da situação com segurança.", "Buscar ajuda é adequado quando as mensagens se repetem ou causam sofrimento. Evite confrontos que possam aumentar a exposição.", CharacterMood.EXPLAINING, "group-conclusion")),
                        ConclusionStep("group-conclusion", "Apoiar também é escutar", "Você pode oferecer ajuda, respeitar se Davi não quiser conversar e contar a um adulto se as mensagens continuarem. A culpa não é de Davi."),
                    ),
                ),
                AgeBand.AGENT_11_14 to MissionTrack(
                    context = "No grupo de um trabalho da turma, colegas fazem comentários repetidos sobre a apresentação de Davi. Uma pessoa diz que é brincadeira; Davi deixou de responder.",
                    objective = "Considere o impacto e o contexto antes de decidir como apoiar Davi.",
                    startingStepId = "agent-group-discovery",
                    steps = listOf(
                        InformationStep(
                            "agent-group-discovery", "Uma conversa que mudou de tom",
                            "Você percebe que as mensagens não pararam. Não sabe se Davi viu tudo nem que tipo de ajuda prefere.",
                            listOf(
                                InspectableDetail("agent-group-pattern", "O padrão", "Os comentários sobre a apresentação se repetiram em dias diferentes."),
                                InspectableDetail("agent-group-impact", "O que você não sabe", "Davi não respondeu, mas o silêncio não confirma se está bem ou se quer uma intervenção pública."),
                                InspectableDetail("agent-group-audience", "Quem está vendo", "O grupo reúne a equipe do trabalho. Encaminhar as mensagens para outro grupo aumentaria o número de pessoas que as veem."),
                            ), nextStepId = "agent-group-decision", minimumDetailsToInspect = 0,
                        ),
                        decision("agent-group-decision", "Grupo do trabalho", "Grupo da turma", "Comentários repetidos sobre a apresentação de Davi", "É só brincadeira, dizem. Davi deixou de responder.", null,
                            "Que resposta considera o impacto sem presumir o que Davi quer?",
                            choice("agent-private-support", "Perguntar em particular se ele quer apoio, sem insistir numa resposta", "Davi agradece a mensagem e prefere não conversar agora. Você respeita a escolha dele.", "Uma conversa privada oferece apoio sem transformar Davi em assunto público. O silêncio dele não exige que você adivinhe ou pressione.", CharacterMood.EXPLAINING, "agent-group-conclusion"),
                            choice("agent-adult", "Falar em particular com um adulto ou moderador de confiança", "A pessoa responsável pode avaliar como interromper as mensagens sem expor Davi no grupo.", "Quando há repetição ou sofrimento, pedir ajuda é razoável mesmo sem saber a intenção de quem escreveu.", CharacterMood.EXPLAINING, "agent-group-conclusion"),
                            choice("agent-public-reply", "Responder no grupo que os comentários precisam parar", "Algumas pessoas param; outras discutem, e Davi fica ainda mais no centro da conversa.", "Defender alguém pode ser bem-intencionado, mas uma discussão pública pode aumentar a exposição. Você pode pausar e buscar apoio privado.", CharacterMood.THINKING, "agent-group-reconsider"),
                            choice("agent-forward", "Encaminhar as mensagens a outro grupo para pedir opinião", "Mais pessoas passam a ver os comentários sobre Davi.", "Pedir uma opinião não exige espalhar as mensagens. Converse em particular com alguém de confiança e compartilhe apenas o necessário.", CharacterMood.SURPRISED, "agent-group-reconsider")),
                        info("agent-group-reconsider", "Reduza a exposição", "Pare de encaminhar e evite prolongar a discussão no grupo. Se a situação se repete ou deixa alguém em sofrimento, converse em particular com um adulto ou moderador de confiança.", "agent-group-recovery"),
                        decision("agent-group-recovery", "Capi", "Capi", "Como seguir com cuidado", "Você pode apoiar Davi sem decidir por ele nem ampliar a audiência.", null,
                            "Qual passo combina com o que você sabe agora?",
                            choice("agent-repair-private", "Oferecer apoio em particular e aceitar se ele não quiser falar", "Davi pode escolher se quer conversar; você também pode procurar ajuda se os comentários continuarem.", "Apoio respeita a autonomia da pessoa. Não é preciso esperar uma resposta para pedir orientação sobre agressões repetidas.", CharacterMood.EXPLAINING, "agent-group-conclusion"),
                            choice("agent-repair-adult", "Relatar em particular a um adulto ou moderador de confiança", "A pessoa responsável pode avaliar a situação sem transformar a conversa em confronto público.", "Bloquear, denunciar ou buscar ajuda depende do contexto. Um adulto ou moderador pode ajudar a escolher uma resposta segura.", CharacterMood.EXPLAINING, "agent-group-conclusion")),
                        ConclusionStep("agent-group-conclusion", "Apoio sem exposição", "Leve a sério o impacto, escute sem pressionar e evite espalhar as mensagens. Se as agressões continuarem ou alguém estiver em sofrimento, procure apoio confiável. A culpa não é de quem foi alvo."),
                    ),
                ),
            ),
        ),
    )

    fun mission(id: MissionId): MissionDefinition = missions.first { it.id == id }

    private fun decision(
        id: String, senderName: String, senderIdentity: String, context: String, text: String,
        senderCharacter: CharacterId?, prompt: String, vararg choices: MissionChoice,
    ) = DecisionStep(id, MessageContent(senderIdentity, senderName, context, text, senderCharacter), prompt, choices.toList())

    private fun choice(id: String, label: String, consequence: String, feedback: String, mood: CharacterMood, next: String) =
        MissionChoice(id, label, ChoiceOutcome(consequence, feedback, mood, next))

    private fun info(id: String, heading: String, body: String, next: String) =
        InformationStep(id, heading, body, emptyList(), nextStepId = next)
}
