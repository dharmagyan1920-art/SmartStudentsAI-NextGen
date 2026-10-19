package com.smartstudents.nextgen

import com.smartstudents.nextgen.data.CurriculumParser
import com.smartstudents.nextgen.data.Selection
import com.smartstudents.nextgen.model.Curriculum
import org.json.JSONException
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class CurriculumDataTest {
    // Unit tests run with the module directory (app/) as working directory.
    private val curriculum: Curriculum by lazy {
        CurriculumParser.parse(File("src/main/assets/curriculum/sample_curriculum.json").readText())
    }

    private fun names(boardId: String, level: Int, medium: String = "english", stream: String? = null) =
        curriculum.subjectsFor(boardId, level, medium, stream).map { it.name }

    @Test fun datasetIsLabelledSampleAndNothingIsVerified() {
        assertTrue(curriculum.isSample)
        assertTrue(curriculum.datasetLabel.contains("SAMPLE"))
        assertTrue(curriculum.resources.none { it.isVerified })
    }

    @Test fun everyBoardAndClassHasSubjects() {
        for (board in curriculum.boards) for (level in 1..12) {
            val stream = if (level >= 11) "science" else null
            assertTrue("${board.id} class $level", names(board.id, level, stream = stream).isNotEmpty())
        }
    }

    @Test fun referencesAreConsistent() {
        val subjectIds = curriculum.subjects.map { it.id }.toSet()
        val chapterIds = curriculum.chapters.map { it.id }.toSet()
        val lessonIds = curriculum.lessons.map { it.id }.toSet()
        assertTrue(curriculum.chapters.all { it.subjectId in subjectIds })
        assertTrue(curriculum.lessons.all { it.chapterId in chapterIds })
        assertTrue(curriculum.resources.all { it.lessonId in lessonIds })
        assertEquals(curriculum.subjects.size, subjectIds.size)
        assertEquals(curriculum.resources.size, curriculum.resources.map { it.id }.toSet().size)
    }

    @Test fun classes11And12DependOnStream() {
        assertTrue("Physics" in names("cbse", 11, stream = "science"))
        assertFalse("Accountancy" in names("cbse", 11, stream = "science"))
        assertTrue("Accountancy" in names("cbse", 12, stream = "commerce"))
        assertTrue("History" in names("maharashtra", 11, stream = "arts"))
    }

    @Test fun compulsorySubjectsComeBeforeOptional() {
        val list = curriculum.subjectsFor("cbse", 11, "english", "science")
        assertEquals(list.sortedByDescending { it.compulsory }.map { it.id }.take(list.count { it.compulsory }),
            list.take(list.count { it.compulsory }).map { it.id })
        assertTrue(list.first().compulsory)
        assertFalse(list.last().compulsory)
    }

    @Test fun subjectsCanBeBoardSpecific() {
        assertTrue("Marathi" in names("maharashtra", 3))
        assertFalse("Marathi" in names("cbse", 3))
    }

    @Test fun customBoardsGetTheGenericSubjects() {
        assertTrue("Mathematics" in names("custom:My School Board", 3))
    }

    @Test fun chaptersLessonsAndResourcesAreReachable() {
        val chapters = curriculum.chaptersFor("math", 3)
        assertEquals(listOf(1, 2, 3), chapters.map { it.number })
        assertTrue(curriculum.lessonsFor("math3_c1").isNotEmpty())
        assertTrue(curriculum.resourcesFor("math3_c1_l1").map { it.type }.toSet().size == 8)
        assertTrue(curriculum.chaptersFor("math", 7).isEmpty())
    }

    @Test fun selectionCompleteness() {
        assertFalse(Selection(null, null, null, null, emptyList()).isComplete())
        assertTrue(Selection("cbse", 5, "english", null, emptyList()).isComplete())
        assertFalse(Selection("cbse", 11, "english", null, emptyList()).isComplete())
        assertTrue(Selection("cbse", 11, "english", "science", emptyList()).isComplete())
    }

    @Test fun malformedJsonFails() {
        assertThrows(JSONException::class.java) { CurriculumParser.parse("{ not json") }
    }
}
