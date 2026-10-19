package com.smartstudents.nextgen.model

/** Listing a board here does NOT imply any affiliation with or endorsement by that board. */
data class Board(val id: String, val name: String, val isCustom: Boolean = false)

data class SchoolClass(val level: Int) {
    val label: String get() = "Class $level"
    val hasStreams: Boolean get() = level >= STREAM_FROM_LEVEL

    companion object {
        const val STREAM_FROM_LEVEL = 11
        val ALL: List<SchoolClass> = (1..12).map { SchoolClass(it) }
    }
}

data class Medium(val id: String, val name: String)

data class Stream(val id: String, val name: String)

/**
 * Empty [boardIds] / [mediumIds] / [streamIds] mean "applies to all".
 * Streams only matter for classes 11-12.
 */
data class Subject(
    val id: String,
    val name: String,
    val boardIds: Set<String>,
    val classLevels: Set<Int>,
    val mediumIds: Set<String>,
    val streamIds: Set<String>,
    val compulsory: Boolean,
) {
    fun appliesTo(boardId: String, classLevel: Int, mediumId: String, streamId: String?): Boolean {
        if (classLevel !in classLevels) return false
        if (boardIds.isNotEmpty() && boardId !in boardIds) return false
        if (mediumIds.isNotEmpty() && mediumId !in mediumIds) return false
        if (classLevel >= SchoolClass.STREAM_FROM_LEVEL && streamIds.isNotEmpty()) {
            return streamId != null && streamId in streamIds
        }
        return true
    }
}

data class Chapter(val id: String, val subjectId: String, val classLevel: Int, val number: Int, val title: String)

data class Lesson(val id: String, val chapterId: String, val order: Int, val title: String)

enum class ResourceType(val label: String) {
    NOTES("Notes"),
    EXPLANATION("Explanations"),
    QUESTION_ANSWER("Question and answer"),
    MCQ("Multiple-choice questions"),
    PRACTICE("Practice exercises"),
    WORKSHEET("Worksheets"),
    QUIZ("Quizzes"),
    REVISION("Revision material"),
}

data class LearningResource(
    val id: String,
    val lessonId: String,
    val type: ResourceType,
    val title: String,
    val body: String,
    /** false = sample content written for this app; true only for content that has been reviewed. */
    val isVerified: Boolean,
    val source: String,
)

/** The whole curriculum dataset. Pure Kotlin, no Android dependencies, so it is unit-testable. */
data class Curriculum(
    val datasetLabel: String,
    val isSample: Boolean,
    val boards: List<Board>,
    val mediums: List<Medium>,
    val streams: List<Stream>,
    val subjects: List<Subject>,
    val chapters: List<Chapter>,
    val lessons: List<Lesson>,
    val resources: List<LearningResource>,
) {
    fun subjectsFor(boardId: String, classLevel: Int, mediumId: String, streamId: String?): List<Subject> =
        subjects.filter { it.appliesTo(boardId, classLevel, mediumId, streamId) }
            .sortedWith(compareByDescending<Subject> { it.compulsory }.thenBy { it.name })

    fun subject(id: String): Subject? = subjects.firstOrNull { it.id == id }
    fun chaptersFor(subjectId: String, classLevel: Int): List<Chapter> =
        chapters.filter { it.subjectId == subjectId && it.classLevel == classLevel }.sortedBy { it.number }

    fun chapter(id: String): Chapter? = chapters.firstOrNull { it.id == id }
    fun lessonsFor(chapterId: String): List<Lesson> =
        lessons.filter { it.chapterId == chapterId }.sortedBy { it.order }

    fun lesson(id: String): Lesson? = lessons.firstOrNull { it.id == id }
    fun resourcesFor(lessonId: String): List<LearningResource> = resources.filter { it.lessonId == lessonId }
    fun resource(id: String): LearningResource? = resources.firstOrNull { it.id == id }
}
