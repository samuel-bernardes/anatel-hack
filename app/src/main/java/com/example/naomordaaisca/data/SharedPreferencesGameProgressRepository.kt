package com.example.naomordaaisca.data

import android.content.Context
import com.example.naomordaaisca.game.GameProgress
import com.example.naomordaaisca.game.GameProgressRepository
import com.example.naomordaaisca.game.InvestigationSnapshotCodec
import com.example.naomordaaisca.game.MissionId

class SharedPreferencesGameProgressRepository(context: Context) : GameProgressRepository {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    override fun load(): GameProgress {
        val completed = preferences.getStringSet(KEY_COMPLETED, emptySet())
            .orEmpty()
            .mapNotNull { name -> runCatching { MissionId.valueOf(name) }.getOrNull() }
            .toSet()
        return GameProgress(
            completedMissionIds = completed,
            activeInvestigation = InvestigationSnapshotCodec.decode(preferences.getString(KEY_INVESTIGATION, null)),
        )
    }

    override fun save(progress: GameProgress) {
        preferences.edit()
            .putStringSet(KEY_COMPLETED, progress.completedMissionIds.map { it.name }.toSet())
            .putString(KEY_INVESTIGATION, progress.activeInvestigation?.let(InvestigationSnapshotCodec::encode))
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "game_progress"
        const val KEY_COMPLETED = "completed_missions"
        const val KEY_INVESTIGATION = "active_investigation_v1"
    }
}
