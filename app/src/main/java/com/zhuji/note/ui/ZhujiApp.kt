package com.zhuji.note.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zhuji.note.ui.screens.edit.EditScreen
import com.zhuji.note.ui.screens.notes.NotesScreen
import com.zhuji.note.ui.screens.settings.SettingsScreen
import com.zhuji.note.ui.screens.stats.StatsScreen
import com.zhuji.note.ui.screens.trash.TrashScreen
import com.zhuji.note.ui.screens.ai.AiChatScreen
import com.zhuji.note.ui.screens.pomodoro.PomodoroScreen
import com.zhuji.note.ui.screens.goal.WritingGoalScreen
import com.zhuji.note.ui.screens.template.TemplatePickerScreen

sealed class Routes(val path: String) {
    data object Notes : Routes("notes")
    data object Edit : Routes("edit/{id}?tpl={tpl}") {
        fun build(id: Long, tpl: String? = null) = if (tpl.isNullOrBlank()) "edit/$id" else "edit/$id?tpl=$tpl"
    }
    data object Settings : Routes("settings")
    data object Stats : Routes("stats")
    data object Trash : Routes("trash")
    data object AiChat : Routes("ai-chat")
    data object Pomodoro : Routes("pomodoro")
    data object Goal : Routes("goal")
    data object Template : Routes("template")
    data object MultiSelect : Routes("multiselect")
}

@Composable
fun ZhujiApp() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.Notes.path) {
        composable(Routes.Notes.path) {
            NotesScreen(
                onOpen = { id -> nav.navigate(Routes.Edit.build(id)) },
                onNew = { nav.navigate(Routes.Edit.build(0L)) },
                onSettings = { nav.navigate(Routes.Settings.path) },
                onStats = { nav.navigate(Routes.Stats.path) },
                onTrash = { nav.navigate(Routes.Trash.path) },
                onAi = { nav.navigate(Routes.AiChat.path) },
                onPomodoro = { nav.navigate(Routes.Pomodoro.path) },
                onGoal = { nav.navigate(Routes.Goal.path) },
                onTemplate = { nav.navigate(Routes.Template.path) },
                onMultiSelect = { nav.navigate(Routes.MultiSelect.path) },
            )
        }
        composable(
            route = Routes.Edit.path,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("tpl") { type = NavType.StringType; nullable = true; defaultValue = null },
            ),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            EditScreen(noteId = id, onBack = { nav.popBackStack() }, onOpenSettings = { nav.navigate(Routes.Settings.path) })
        }
        composable(Routes.Settings.path) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.Stats.path) {
            StatsScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.Trash.path) {
            TrashScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.AiChat.path) {
            AiChatScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.Pomodoro.path) {
            PomodoroScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.Goal.path) {
            WritingGoalScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.Template.path) {
            TemplatePickerScreen(
                onBack = { nav.popBackStack() },
                onPick = { tpl ->
                    nav.popBackStack()
                    nav.navigate(Routes.Edit.build(0L, tpl.id))
                },
            )
        }
        composable(Routes.MultiSelect.path) {
            val vm: com.zhuji.note.ui.screens.notes.NotesViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            com.zhuji.note.ui.screens.notes.MultiSelectNoteList(
                notes = state.notes,
                onNoteClick = { id -> nav.navigate(Routes.Edit.build(id)) },
                onBatchDelete = { ids -> vm.batchDelete(ids) },
                onBatchTag = { },
                onNewNote = { nav.navigate(Routes.Edit.build(0L)) },
            )
        }
    }
}
