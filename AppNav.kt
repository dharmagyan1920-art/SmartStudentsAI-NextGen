package com.smartstudents.nextgen.ui

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartstudents.nextgen.data.Selection
import com.smartstudents.nextgen.model.Curriculum

object Routes {
    const val BOARD = "board"
    const val CLASS = "class"
    const val MEDIUM = "medium"
    const val STREAM = "stream"
    const val SUBJECTS = "subjects"
    const val STATUS = "status"
    const val CHAPTERS = "chapters/{id}"
    const val LESSONS = "lessons/{id}"
    const val RESOURCES = "resources/{id}"
    const val RESOURCE = "resource/{id}"
    fun chapters(id: String) = "chapters/$id"
    fun lessons(id: String) = "lessons/$id"
    fun resources(id: String) = "resources/$id"
    fun resource(id: String) = "resource/$id"
}

@Composable
fun AppNav(vm: AppViewModel = viewModel()) {
    val selection by vm.selection.collectAsStateWithLifecycle()
    val sel = selection
    if (sel == null) {
        LoadingBox()
    } else {
        // Decided once, after the saved selection has been read: returning students go straight to subjects.
        val start = remember { if (sel.isComplete()) Routes.SUBJECTS else Routes.BOARD }
        AppNavHost(vm, start)
    }
}

/** Reads state inside each destination (not here) so the NavHost graph is built only once. */
@Composable
private fun Dest(vm: AppViewModel, content: @Composable (Curriculum, Selection) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val selection by vm.selection.collectAsStateWithLifecycle()
    val sel = selection
    if (sel == null) {
        LoadingBox()
    } else {
        CurriculumGate(state, onRetry = { vm.load() }) { curriculum -> content(curriculum, sel) }
    }
}

private fun NavHostController.finishSetup() {
    navigate(Routes.SUBJECTS) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
private fun AppNavHost(vm: AppViewModel, start: String) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = start) {
        composable(Routes.BOARD) {
            Dest(vm) { c, sel ->
                BoardScreen(
                    boards = sel.allBoards(c),
                    selectedId = sel.boardId,
                    onBack = if (nav.previousBackStackEntry != null) ({ nav.popBackStack() }) else null,
                    onSelect = { vm.selectBoard(it); nav.navigate(Routes.CLASS) },
                    onAddCustom = { vm.addCustomBoard(it) },
                )
            }
        }
        composable(Routes.CLASS) {
            Dest(vm) { _, sel ->
                ClassScreen(
                    selectedLevel = sel.classLevel,
                    onBack = { nav.popBackStack() },
                    onSelect = { vm.selectClass(it); nav.navigate(Routes.MEDIUM) },
                )
            }
        }
        composable(Routes.MEDIUM) {
            Dest(vm) { c, sel ->
                MediumScreen(
                    mediums = c.mediums,
                    selectedId = sel.mediumId,
                    onBack = { nav.popBackStack() },
                    onSelect = {
                        vm.selectMedium(it)
                        if (sel.needsStream) nav.navigate(Routes.STREAM) else nav.finishSetup()
                    },
                )
            }
        }
        composable(Routes.STREAM) {
            Dest(vm) { c, sel ->
                StreamScreen(
                    streams = c.streams,
                    selectedId = sel.streamId,
                    onBack = { nav.popBackStack() },
                    onSelect = { vm.selectStream(it); nav.finishSetup() },
                )
            }
        }
        composable(Routes.SUBJECTS) {
            Dest(vm) { c, sel ->
                SubjectsScreen(
                    curriculum = c,
                    selection = sel,
                    onOpenSubject = { nav.navigate(Routes.chapters(it)) },
                    onChangeBoard = { nav.navigate(Routes.BOARD) },
                    onChangeClass = { nav.navigate(Routes.CLASS) },
                    onChangeMedium = { nav.navigate(Routes.MEDIUM) },
                    onChangeStream = { nav.navigate(Routes.STREAM) },
                    onOpenStatus = { nav.navigate(Routes.STATUS) },
                )
            }
        }
        composable(Routes.STATUS) { FeatureStatusScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.CHAPTERS) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            Dest(vm) { c, sel ->
                ChaptersScreen(c, id, sel.classLevel, { nav.popBackStack() }, { nav.navigate(Routes.lessons(it)) })
            }
        }
        composable(Routes.LESSONS) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            Dest(vm) { c, _ ->
                LessonsScreen(c, id, { nav.popBackStack() }, { nav.navigate(Routes.resources(it)) })
            }
        }
        composable(Routes.RESOURCES) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            Dest(vm) { c, _ ->
                ResourcesScreen(c, id, { nav.popBackStack() }, { nav.navigate(Routes.resource(it)) })
            }
        }
        composable(Routes.RESOURCE) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            Dest(vm) { c, _ -> ResourceDetailScreen(c, id) { nav.popBackStack() } }
        }
    }
}
