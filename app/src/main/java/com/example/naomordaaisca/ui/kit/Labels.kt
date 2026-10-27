package com.example.naomordaaisca.ui.kit

import com.example.naomordaaisca.game.CharacterMood
import com.example.naomordaaisca.game.EvidenceStatus
import com.example.naomordaaisca.game.InteractableId
import com.example.naomordaaisca.game.InvestigationSession
import com.example.naomordaaisca.game.InvestigationTrack

fun CharacterMood.label(): String = when (this) {
    CharacterMood.NEUTRAL -> "neutra"
    CharacterMood.THINKING -> "pensativa"
    CharacterMood.SURPRISED -> "surpresa"
    CharacterMood.EXPLAINING -> "explicando"
    CharacterMood.PRESENTING_OFFER -> "apresentando uma oferta"
}

fun InteractableId.label(): String = when (this) {
    InteractableId.COMPUTER -> "Computador"
    InteractableId.PHONE -> "Celular"
    InteractableId.CAPI -> "Capi"
}

fun InvestigationSession.statusOf(clueId: String): EvidenceStatus = when (clueId) {
    in usedEvidenceIds -> EvidenceStatus.USED
    in discoveredEvidenceIds -> EvidenceStatus.DISCOVERED
    else -> EvidenceStatus.AVAILABLE
}

/** "Lia, colega da turma" → nome do contato e descrição exibidos no Send. */
val InvestigationTrack.contactName: String get() = sender.substringBefore(',').trim()
val InvestigationTrack.contactRole: String get() = sender.substringAfter(',', "").trim()

/** Dona do celular simulado nas telas do Figma. */
const val PHONE_OWNER = "Clara Azevedo"
const val PHONE_OWNER_INITIALS = "CA"
