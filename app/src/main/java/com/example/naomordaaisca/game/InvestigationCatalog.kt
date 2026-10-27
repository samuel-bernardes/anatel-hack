package com.example.naomordaaisca.game

object InvestigationCatalog {
    val definitions: List<InvestigationDefinition> by lazy {
        listOf(InvestigationDefinition(
            id = MissionId.DIGITAL_OFFERS,
            title = "A isca no jogo",
            ageTracks = mapOf(
                AgeBand.EXPLORER_8_10 to explorerTrack,
                AgeBand.AGENT_11_14 to agentTrack,
            ),
        ))
    }

    fun definition(id: MissionId): InvestigationDefinition? = definitions.firstOrNull { it.id == id }

    fun track(id: MissionId, ageBand: AgeBand): InvestigationTrack? = definition(id)?.ageTracks?.get(ageBand)

    fun track(ageBand: AgeBand): InvestigationTrack = track(MissionId.DIGITAL_OFFERS, ageBand)
        ?: error("Trilha de investigação não cadastrada para $ageBand")

    fun validate(definition: InvestigationDefinition): List<String> = buildList {
        if (definition.title.isBlank()) add("título da investigação ausente")
        if (definition.ageTracks.keys != AgeBand.entries.toSet()) add("faixas etárias ausentes ou inválidas")
        definition.ageTracks.values.forEach { track -> addAll(validate(track)) }
    }

    fun validate(track: InvestigationTrack): List<String> = buildList {
        if (track.context.isBlank() || track.objective.isBlank() || track.sender.isBlank() || track.message.isBlank() || track.conclusion.isBlank()) add("contexto, objetivo, remetente, mensagem e conclusão são obrigatórios")
        val ids = track.clues.map { it.id }
        if (ids.any(String::isBlank) || ids.distinct().size != ids.size) add("identificadores de pista ausentes ou duplicados")
        if (track.clues.any { it.title.isBlank() || it.content.isBlank() }) add("pista sem título ou conteúdo")
        if (track.clues.map { it.appId }.toSet() != SimulatedAppId.entries.toSet()) add("as pistas devem estar associadas aos aplicativos do MVP")
        if (track.evidenceRequiredForDecision.isEmpty() || !ids.containsAll(track.evidenceRequiredForDecision)) add("requisitos de evidência inválidos")
        if (track.clues.filter { it.id in track.evidenceRequiredForDecision }.map { it.appId }.toSet() != SimulatedAppId.entries.toSet()) add("a decisão deve usar evidência de cada aplicativo do caso")
        val choices = track.choices.map { it.id }
        if (choices.isEmpty() || choices.any(String::isBlank) || choices.distinct().size != choices.size) add("identificadores de alternativa ausentes ou duplicados")
        if (track.choices.any { it.label.isBlank() || it.consequence.isBlank() || it.feedback.isBlank() }) add("alternativa sem consequência ou feedback")
    }

    private val explorerTrack = InvestigationTrack(
        context = "Uma colega manda uma oferta de uma skin rara para o jogo que vocês jogam. Ela diz que viu a promoção, mas não sabe se o link é oficial.",
        objective = "Descubra o que a oferta pede e confira por outro caminho antes de decidir.",
        sender = "Lia, colega da turma",
        message = "Olha! Uma skin especial grátis. O site diz que acaba hoje. Será que é de verdade?",
        clues = listOf(
            InvestigationClue("offer-peer-context", SimulatedAppId.MESSAGES, "Como a mensagem chegou", "Lia encaminhou a oferta depois de recebê-la num grupo. Ela não criou o link nem sabe quem o publicou."),
            InvestigationClue("offer-request", SimulatedAppId.MESSAGES, "O que a página pede", "A página solicita a senha do jogo e um código enviado por mensagem para liberar a skin."),
            InvestigationClue("offer-domain", SimulatedAppId.BROWSER, "Endereço da promoção", "O endereço não é o site que você costuma abrir para jogar. O nome da empresa aparece escrito de outro jeito."),
            InvestigationClue("offer-official-news", SimulatedAppId.BROWSER, "Notícias do jogo", "Ao abrir o jogo pelo ícone conhecido, as notícias dizem que brindes nunca exigem senha ou código. Esta skin não aparece entre as promoções."),
        ),
        evidenceRequiredForDecision = setOf("offer-request", "offer-official-news"),
        choices = listOf(
            InvestigationChoice("verify-known-channel", "Conferir as notícias pelo jogo que já conheço", "Você encontra a lista oficial de promoções e não entrega os dados da conta.", "A mensagem veio de uma colega, mas ela também recebeu o link de outra pessoa. Conferir no jogo separa a identidade de quem encaminhou da segurança do pedido."),
            InvestigationChoice("enter-credentials", "Digitar a senha e o código para tentar pegar a skin", "A página recebe dados que podem permitir o acesso à conta.", "Uma promoção pode parecer atraente, mas senha e código não devem ser entregues para resgatar um item. Feche a página e peça ajuda para proteger a conta.", capiMood = CharacterMood.SURPRISED, recoveryRequired = true),
            InvestigationChoice("forward-link", "Repassar o link para o grupo decidir", "Mais colegas podem abrir a página e informar os dados pedidos.", "Pedir opinião é válido, mas encaminhar o link espalha o risco. Você poderia descrever a oferta sem compartilhar o endereço e conferir pelo jogo.", capiMood = CharacterMood.THINKING),
            InvestigationChoice("ask-trusted-adult", "Mostrar a mensagem a um adulto de confiança e conferir juntos", "Um adulto ajuda a abrir o jogo por um caminho conhecido e verificar a promoção.", "Buscar ajuda não exige concluir que Lia agiu de má-fé. A confirmação veio de um canal independente."),
        ),
        conclusion = "Lia compartilhou uma oferta sem conseguir confirmar sua origem. O pedido de senha e código era arriscado, e a conferência no próprio jogo mostrou que a promoção não existia.",
    )

    private val agentTrack = InvestigationTrack(
        context = "Depois de um torneio da comunidade, uma colega encaminha uma promoção de skin. O item é plausível, mas a origem e as condições ainda não foram confirmadas.",
        objective = "Compare a origem, o pedido da página e a informação publicada no canal do jogo.",
        sender = "Lia, colega da equipe",
        message = "Uma pessoa do grupo disse que esta skin foi liberada para quem participou. O link fecha hoje; você acha que vale tentar?",
        clues = listOf(
            InvestigationClue("offer-agent-context", SimulatedAppId.MESSAGES, "Contexto do torneio", "O torneio aconteceu e participantes receberam itens em outras ocasiões. Lia recebeu o endereço de outra pessoa e não sabe quem o publicou."),
            InvestigationClue("offer-agent-request", SimulatedAppId.MESSAGES, "Condições de resgate", "A página pede senha e código temporário. O jogo permite resgatar itens dentro da conta oficial, sem enviar códigos por mensagem."),
            InvestigationClue("offer-agent-domain", SimulatedAppId.BROWSER, "Endereço e certificado", "A conexão usa HTTPS, mas o domínio não corresponde ao endereço oficial que você acessa para jogar. HTTPS não confirma quem opera a página."),
            InvestigationClue("offer-agent-news", SimulatedAppId.BROWSER, "Canal oficial", "As notícias oficiais não mencionam a skin. O suporte informa que recompensas do torneio aparecem na conta e nunca exigem o envio de senha ou código."),
        ),
        evidenceRequiredForDecision = setOf("offer-agent-request", "offer-agent-news"),
        choices = listOf(
            InvestigationChoice("agent-verify", "Abrir o jogo pelo atalho conhecido e conferir conta e notícias", "Você confirma que o item não foi anunciado e mantém suas credenciais fora da página.", "O torneio torna a oferta plausível, mas não confirma este link. HTTPS protege a conexão, não comprova a identidade do site."),
            InvestigationChoice("agent-login", "Enviar senha e código antes do prazo", "A página pode usar os dados para entrar na conta enquanto o prazo pressiona você.", "Urgência e contexto plausível não autenticam o destino. Feche a página e peça ajuda para proteger a conta.", capiMood = CharacterMood.SURPRISED, recoveryRequired = true),
            InvestigationChoice("agent-forward", "Compartilhar o endereço com a equipe para comparar", "Outras pessoas podem confiar no link e fornecer credenciais.", "Comparar informações ajuda, mas o canal oficial é uma referência mais segura que espalhar um endereço ainda não confirmado.", capiMood = CharacterMood.THINKING),
            InvestigationChoice("agent-check-support", "Consultar o suporte pelo jogo ou canal salvo", "Você verifica a promoção sem depender do link recebido.", "Uma confirmação independente pode resolver a dúvida sem tratar Lia como culpada por encaminhar algo que também não confirmou."),
        ),
        conclusion = "O torneio era real, mas o link não era uma forma oficial de resgatar a recompensa. Contexto plausível e conexão HTTPS não substituem a confirmação do domínio e do pedido.",
    )
}
