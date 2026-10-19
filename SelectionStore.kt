package com.smartstudents.nextgen.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smartstudents.nextgen.model.Board
import com.smartstudents.nextgen.model.Curriculum
import com.smartstudents.nextgen.model.SchoolClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val CUSTOM_BOARD_PREFIX = "custom:"

/** What the student has chosen so far. Saved on the device so it is not asked again. */
data class Selection(
    val boardId: String?,
    val classLevel: Int?,
    val mediumId: String?,
    val streamId: String?,
    val customBoards: List<String>,
) {
    val needsStream: Boolean get() = (classLevel ?: 0) >= SchoolClass.STREAM_FROM_LEVEL

    fun isComplete(): Boolean =
        boardId != null && classLevel != null && mediumId != null && (!needsStream || streamId != null)

    fun allBoards(curriculum: Curriculum): List<Board> =
        curriculum.boards + customBoards.map { Board(CUSTOM_BOARD_PREFIX + it, it, isCustom = true) }
}

private val Context.selectionDataStore by preferencesDataStore(name = "selection")

class SelectionStore(private val context: Context) {
    private object Keys {
        val BOARD = stringPreferencesKey("board_id")
        val CLASS = intPreferencesKey("class_level")
        val MEDIUM = stringPreferencesKey("medium_id")
        val STREAM = stringPreferencesKey("stream_id")
        val CUSTOM_BOARDS = stringSetPreferencesKey("custom_boards")
    }

    val selection: Flow<Selection> = context.selectionDataStore.data.map { p ->
        Selection(
            boardId = p[Keys.BOARD],
            classLevel = p[Keys.CLASS],
            mediumId = p[Keys.MEDIUM],
            streamId = p[Keys.STREAM],
            customBoards = p[Keys.CUSTOM_BOARDS].orEmpty().sorted(),
        )
    }

    suspend fun setBoard(id: String) {
        context.selectionDataStore.edit { it[Keys.BOARD] = id }
    }

    suspend fun setClass(level: Int) {
        context.selectionDataStore.edit {
            it[Keys.CLASS] = level
            if (level < SchoolClass.STREAM_FROM_LEVEL) it.remove(Keys.STREAM)
        }
    }

    suspend fun setMedium(id: String) {
        context.selectionDataStore.edit { it[Keys.MEDIUM] = id }
    }

    suspend fun setStream(id: String) {
        context.selectionDataStore.edit { it[Keys.STREAM] = id }
    }

    /** Returns the new board id, or null if the name was blank. */
    suspend fun addCustomBoard(name: String): String? {
        val clean = name.trim().take(40)
        if (clean.isEmpty()) return null
        context.selectionDataStore.edit { it[Keys.CUSTOM_BOARDS] = it[Keys.CUSTOM_BOARDS].orEmpty() + clean }
        return CUSTOM_BOARD_PREFIX + clean
    }
}
