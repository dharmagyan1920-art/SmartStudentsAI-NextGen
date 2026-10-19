package com.smartstudents.nextgen.data

import android.content.Context
import com.smartstudents.nextgen.model.Curriculum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Source of curriculum data. Today: a JSON file bundled in the APK (SAMPLE data).
 * A remote/cloud implementation can be added later without touching any screen.
 */
interface CurriculumRepository {
    suspend fun load(): Result<Curriculum>
}

class AssetCurriculumRepository(
    private val context: Context,
    private val assetPath: String = "curriculum/sample_curriculum.json",
) : CurriculumRepository {

    @Volatile
    private var cached: Curriculum? = null

    override suspend fun load(): Result<Curriculum> = withContext(Dispatchers.IO) {
        runCatching {
            cached ?: CurriculumParser
                .parse(context.assets.open(assetPath).bufferedReader().use { it.readText() })
                .also { cached = it }
        }
    }
}
