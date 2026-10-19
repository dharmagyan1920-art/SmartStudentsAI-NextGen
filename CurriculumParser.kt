package com.smartstudents.nextgen.data

import com.smartstudents.nextgen.model.*
import org.json.JSONArray
import org.json.JSONObject

/** Turns curriculum JSON into [Curriculum]. Throws (JSONException etc.) on malformed data. */
object CurriculumParser {

    fun parse(json: String): Curriculum {
        val root = JSONObject(json)
        val meta = root.getJSONObject("dataset")
        return Curriculum(
            datasetLabel = meta.getString("label"),
            isSample = meta.getBoolean("sample"),
            boards = root.getJSONArray("boards").mapObjects { Board(it.getString("id"), it.getString("name")) },
            mediums = root.getJSONArray("mediums").mapObjects { Medium(it.getString("id"), it.getString("name")) },
            streams = root.getJSONArray("streams").mapObjects { Stream(it.getString("id"), it.getString("name")) },
            subjects = root.getJSONArray("subjects").mapObjects {
                Subject(
                    id = it.getString("id"),
                    name = it.getString("name"),
                    boardIds = it.stringSet("boards"),
                    classLevels = it.classLevels(),
                    mediumIds = it.stringSet("mediums"),
                    streamIds = it.stringSet("streams"),
                    compulsory = it.optBoolean("compulsory", true),
                )
            },
            chapters = root.getJSONArray("chapters").mapObjects {
                Chapter(it.getString("id"), it.getString("subjectId"), it.getInt("classLevel"), it.getInt("number"), it.getString("title"))
            },
            lessons = root.getJSONArray("lessons").mapObjects {
                Lesson(it.getString("id"), it.getString("chapterId"), it.getInt("order"), it.getString("title"))
            },
            resources = root.getJSONArray("resources").mapObjects {
                LearningResource(
                    id = it.getString("id"),
                    lessonId = it.getString("lessonId"),
                    type = ResourceType.valueOf(it.getString("type")),
                    title = it.getString("title"),
                    body = it.getString("body"),
                    isVerified = it.optBoolean("verified", false),
                    source = it.optString("source", "Sample content"),
                )
            },
        )
    }

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
        List(length()) { transform(getJSONObject(it)) }

    private fun JSONObject.stringSet(key: String): Set<String> =
        optJSONArray(key)?.let { arr -> List(arr.length()) { arr.getString(it) }.toSet() } ?: emptySet()

    /** Accepts "classRange": [from, to] and/or "classes": [..]. */
    private fun JSONObject.classLevels(): Set<Int> {
        val result = mutableSetOf<Int>()
        optJSONArray("classRange")?.let { result += it.getInt(0)..it.getInt(1) }
        optJSONArray("classes")?.let { arr -> for (i in 0 until arr.length()) result += arr.getInt(i) }
        return result
    }
}
