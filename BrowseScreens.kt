package com.smartstudents.nextgen.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartstudents.nextgen.data.Selection
import com.smartstudents.nextgen.model.*

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    curriculum: Curriculum,
    selection: Selection,
    onOpenSubject: (String) -> Unit,
    onChangeBoard: () -> Unit,
    onChangeClass: () -> Unit,
    onChangeMedium: () -> Unit,
    onChangeStream: () -> Unit,
    onOpenStatus: () -> Unit,
) {
    val boardName = selection.allBoards(curriculum).firstOrNull { it.id == selection.boardId }?.name ?: "?"
    val mediumName = curriculum.mediums.firstOrNull { it.id == selection.mediumId }?.name ?: "?"
    val streamName = curriculum.streams.firstOrNull { it.id == selection.streamId }?.name
    val level = selection.classLevel
    var query by rememberSaveable { mutableStateOf("") }

    AppScaffold(
        title = "Smart Students AI",
        onBack = null,
        actions = {
            IconButton(onClick = onOpenStatus) { Icon(Icons.Filled.Info, contentDescription = "Feature status") }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (curriculum.isSample) SampleBanner(curriculum.datasetLabel)
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(onClick = onChangeBoard, label = { Text("Board: $boardName") })
                AssistChip(onClick = onChangeClass, label = { Text("Class: ${level ?: "?"}") })
                AssistChip(onClick = onChangeMedium, label = { Text("Medium: $mediumName") })
                if (selection.needsStream) {
                    AssistChip(onClick = onChangeStream, label = { Text("Stream: ${streamName ?: "?"}") })
                }
            }
            SearchField(query, { query = it }, "Search subjects and chapters")

            val subjects = remember(curriculum, selection) {
                if (selection.boardId != null && level != null && selection.mediumId != null) {
                    curriculum.subjectsFor(selection.boardId, level, selection.mediumId, selection.streamId)
                } else emptyList()
            }
            val q = query.trim()
            val shown = if (q.isEmpty() || level == null) subjects else subjects.filter { s ->
                s.name.contains(q, ignoreCase = true) ||
                    curriculum.chaptersFor(s.id, level).any { it.title.contains(q, ignoreCase = true) }
            }

            if (shown.isEmpty()) {
                EmptyState(
                    if (q.isEmpty()) "No subjects yet" else "No matches",
                    if (q.isEmpty()) "There are no subjects for this board, class, medium and stream in the current data."
                    else "Nothing matches \"$q\". Try a different word.",
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(shown, key = { it.id }) { s ->
                        val chapterCount = if (level != null) curriculum.chaptersFor(s.id, level).size else 0
                        SelectableCard(
                            title = s.name,
                            subtitle = (if (s.compulsory) "Compulsory" else "Optional") +
                                " · " + (if (chapterCount == 0) "no chapters yet" else "$chapterCount chapter(s)"),
                            selected = false,
                            onClick = { onOpenSubject(s.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChaptersScreen(
    curriculum: Curriculum,
    subjectId: String,
    classLevel: Int?,
    onBack: () -> Unit,
    onOpenChapter: (String) -> Unit,
) {
    val subject = curriculum.subject(subjectId)
    var query by rememberSaveable { mutableStateOf("") }
    AppScaffold(subject?.name ?: "Subject", onBack) { padding ->
        Column(Modifier.padding(padding)) {
            if (subject == null || classLevel == null) {
                EmptyState("Not found", "This subject or class is not available.")
                return@Column
            }
            val chapters = curriculum.chaptersFor(subject.id, classLevel)
            val shown = chapters.filter { it.title.contains(query.trim(), ignoreCase = true) }
            Text("Class $classLevel", Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.titleMedium)
            if (chapters.isNotEmpty()) SearchField(query, { query = it }, "Search chapters")
            if (shown.isEmpty()) {
                EmptyState(
                    if (chapters.isEmpty()) "No chapters yet" else "No matches",
                    if (chapters.isEmpty()) "Chapters for this subject have not been added to the sample data."
                    else "Nothing matches \"${query.trim()}\".",
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(shown, key = { it.id }) { c ->
                        SelectableCard("${c.number}. ${c.title}", null, false, { onOpenChapter(c.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun LessonsScreen(curriculum: Curriculum, chapterId: String, onBack: () -> Unit, onOpenLesson: (String) -> Unit) {
    val chapter = curriculum.chapter(chapterId)
    AppScaffold(chapter?.title ?: "Chapter", onBack) { padding ->
        val lessons = if (chapter != null) curriculum.lessonsFor(chapter.id) else emptyList()
        if (lessons.isEmpty()) {
            EmptyState("No lessons yet", "Lessons for this chapter have not been added to the sample data.", Modifier.padding(padding))
        } else {
            LazyColumn(
                Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(lessons, key = { it.id }) { l ->
                    SelectableCard("${l.order}. ${l.title}", null, false, { onOpenLesson(l.id) })
                }
            }
        }
    }
}

@Composable
fun ResourcesScreen(curriculum: Curriculum, lessonId: String, onBack: () -> Unit, onOpenResource: (String) -> Unit) {
    val lesson = curriculum.lesson(lessonId)
    AppScaffold(lesson?.title ?: "Lesson", onBack) { padding ->
        val resources = curriculum.resourcesFor(lessonId)
        if (resources.isEmpty()) {
            EmptyState("No resources yet", "Learning resources for this lesson have not been added to the sample data.", Modifier.padding(padding))
        } else {
            val grouped = resources.groupBy { it.type }
            LazyColumn(
                Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ResourceType.values().filter { it in grouped }.forEach { type ->
                    item(key = "h_${type.name}") {
                        Text(type.label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    items(grouped.getValue(type), key = { it.id }) { r ->
                        SelectableCard(r.title, if (r.isVerified) "Verified" else "Sample content", false, { onOpenResource(r.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceDetailScreen(curriculum: Curriculum, resourceId: String, onBack: () -> Unit) {
    val r = curriculum.resource(resourceId)
    AppScaffold(r?.title ?: "Resource", onBack) { padding ->
        if (r == null) {
            EmptyState("Not found", "This resource is not available.", Modifier.padding(padding))
        } else {
            Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
                if (!r.isVerified) SampleBanner("SAMPLE CONTENT - not verified educational material.")
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Tag(r.type.label, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                    Text(r.body, style = MaterialTheme.typography.bodyLarge)
                    Text("Source: ${r.source}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

private data class FeatureStatus(val name: String, val done: Boolean, val note: String)

private val FEATURES = listOf(
    FeatureStatus("Board, class, medium and stream selection", true, "Saved on this device. Custom boards are local to this device."),
    FeatureStatus("Data-driven subjects, chapters, lessons", true, "Loaded from a bundled SAMPLE JSON file. Not official or complete."),
    FeatureStatus("Search over subjects and chapters", true, "Works on the bundled data only."),
    FeatureStatus("Learning resources (8 types)", true, "Sample content only; very few lessons have resources."),
    FeatureStatus("Teacher / admin management", false, "Not implemented. There is no login or backend, so nothing here is secure."),
    FeatureStatus("AI tutor, AI notes and quizzes", false, "Not implemented. Needs a server so no API key lives in the app."),
    FeatureStatus("Photo / document upload and OCR", false, "Not implemented."),
    FeatureStatus("Progress tracking, parent dashboard", false, "Not implemented."),
    FeatureStatus("Educational games, online courses", false, "Not implemented."),
    FeatureStatus("Cloud sync", false, "Not implemented."),
    FeatureStatus("Verified official curriculum content", false, "Not included. Content must be added with permission."),
)

@Composable
fun FeatureStatusScreen(onBack: () -> Unit) {
    AppScaffold("What works today", onBack) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(FEATURES, key = { it.name }) { f ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Tag(
                            if (f.done) "IMPLEMENTED" else "NOT IMPLEMENTED",
                            if (f.done) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                            if (f.done) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                        Text(f.name, style = MaterialTheme.typography.titleMedium)
                        Text(f.note, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
