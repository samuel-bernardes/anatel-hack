package com.example.naomordaaisca.game

/** Content checks run independently of Android, Compose, and graphics. */
object MissionValidator {
    fun validate(mission: MissionDefinition): List<String> = buildList {
        if (mission.title.isBlank()) add("Título da missão vazio.")
        AgeBand.entries.forEach { ageBand ->
            val track = mission.ageTracks[ageBand]
            if (track == null) {
                add("Trilha ausente para ${ageBand.label()}.")
                return@forEach
            }
            validateTrack(track, ageBand).forEach { add("${ageBand.label()}: $it") }
        }
        (mission.ageTracks.keys - AgeBand.entries.toSet()).forEach { add("Faixa etária desconhecida: $it.") }
    }

    fun isValid(mission: MissionDefinition): Boolean = validate(mission).isEmpty()

    private fun validateTrack(track: MissionTrack, ageBand: AgeBand): List<String> = buildList {
        if (track.context.isBlank()) add("contexto vazio.")
        if (track.objective.isBlank()) add("objetivo vazio.")
        if (track.steps.isEmpty()) { add("nenhuma etapa definida."); return@buildList }
        val ids = track.steps.map { it.id }
        if (ids.any(String::isBlank)) add("há etapa com identificador vazio.")
        if (ids.distinct().size != ids.size) add("há identificadores de etapa repetidos.")
        val detailIds = track.steps.filterIsInstance<InformationStep>().flatMap { step -> step.details.map { it.id } }
        if (detailIds.distinct().size != detailIds.size) add("há identificadores de detalhe repetidos na trilha.")
        val byId = track.steps.associateBy { it.id }
        if (track.startingStepId !in byId) add("etapa inicial '${track.startingStepId}' inexistente.")

        track.steps.forEach { step ->
            when (step) {
                is InformationStep -> {
                    if (step.heading.isBlank() || step.body.isBlank()) add("etapa '${step.id}' sem título ou texto.")
                    validateDetails(step.details, step.requiredDetailIds, step.id).forEach(::add)
                    if (step.minimumDetailsToInspect < step.requiredDetailIds.size || step.minimumDetailsToInspect > step.details.size) {
                        add("etapa '${step.id}' exige uma quantidade inválida de detalhes examinados.")
                    }
                    if (step.nextStepId !in byId) add("etapa '${step.id}' aponta para '${step.nextStepId}', que não existe.")
                }
                is DecisionStep -> {
                    val message = step.message
                    if (message.senderDisplayName.isBlank() || message.senderIdentity.isBlank() ||
                        message.context.isBlank() || message.text.isBlank() || step.capiPrompt.isBlank()
                    ) add("decisão '${step.id}' tem mensagem ou orientação incompleta.")
                    if (step.choices.isEmpty()) add("decisão '${step.id}' não tem alternativas.")
                    if (step.choices.map { it.id }.any(String::isBlank) ||
                        step.choices.map { it.id }.distinct().size != step.choices.size
                    ) add("decisão '${step.id}' tem identificadores de alternativa vazios ou repetidos.")
                    step.choices.forEach { choice ->
                        if (choice.label.isBlank()) add("alternativa '${choice.id}' da etapa '${step.id}' sem texto.")
                        val outcome = choice.outcome
                        if (outcome.consequence.isBlank() || outcome.educationalFeedback.isBlank()) {
                            add("alternativa '${choice.id}' da etapa '${step.id}' sem consequência ou feedback.")
                        }
                        if (outcome.nextStepId !in byId) {
                            add("alternativa '${choice.id}' da etapa '${step.id}' aponta para '${outcome.nextStepId}', que não existe.")
                        }
                    }
                }
                is ConclusionStep -> if (step.title.isBlank() || step.body.isBlank()) add("conclusão '${step.id}' incompleta.")
            }
        }

        val reachable = mutableSetOf<String>()
        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()
        var cycleFound = false
        fun visit(id: String) {
            if (id !in byId || id in visited) return
            if (!visiting.add(id)) { cycleFound = true; return }
            reachable += id
            successors(byId.getValue(id)).forEach(::visit)
            visiting -= id
            visited += id
        }
        visit(track.startingStepId)
        if (cycleFound) add("há ciclo que pode prender o jogador na trilha ${ageBand.label()}.")
        val unreachable = ids.toSet() - reachable
        if (unreachable.isNotEmpty()) add("etapas inalcançáveis: ${unreachable.sorted().joinToString()}.")
        if (reachable.none { byId[it] is ConclusionStep }) add("não há condição de conclusão alcançável.")
        reachable.forEach { id ->
            val step = byId[id] ?: return@forEach
            if (step !is ConclusionStep && successors(step).isEmpty()) add("etapa '$id' não leva a outra etapa nem à conclusão.")
        }
    }

    private fun validateDetails(details: List<InspectableDetail>, required: Set<String>, stepId: String): List<String> = buildList {
        val ids = details.map { it.id }
        if (ids.any(String::isBlank) || ids.distinct().size != ids.size) add("investigação '$stepId' tem detalhes com identificadores vazios ou repetidos.")
        if (details.any { it.label.isBlank() || it.information.isBlank() }) add("investigação '$stepId' tem detalhe sem rótulo ou informação.")
        if (!ids.toSet().containsAll(required)) add("investigação '$stepId' exige detalhes inexistentes.")
    }

    private fun successors(step: MissionStep): List<String> = when (step) {
        is InformationStep -> listOf(step.nextStepId)
        is DecisionStep -> step.choices.map { it.outcome.nextStepId }
        is ConclusionStep -> emptyList()
    }
}

fun AgeBand.label(): String = when (this) {
    AgeBand.EXPLORER_8_10 -> "Explorador (8–10)"
    AgeBand.AGENT_11_14 -> "Agente (11–14)"
}
