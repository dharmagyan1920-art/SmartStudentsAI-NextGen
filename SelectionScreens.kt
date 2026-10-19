package com.smartstudents.nextgen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smartstudents.nextgen.model.*

@Composable
fun BoardScreen(
    boards: List<Board>,
    selectedId: String?,
    onBack: (() -> Unit)?,
    onSelect: (String) -> Unit,
    onAddCustom: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    AppScaffold("Choose your board", onBack) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Boards are listed only to organise content. This app is not affiliated with or endorsed by any board.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            items(boards, key = { it.id }) { board ->
                SelectableCard(
                    title = board.name,
                    subtitle = if (board.isCustom) "Custom board (saved on this device)" else null,
                    selected = board.id == selectedId,
                    onClick = { onSelect(board.id) },
                )
            }
            item {
                OutlinedButton(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add a custom board")
                }
            }
        }
    }
    if (showDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Custom board") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text("Board name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = { onAddCustom(name); showDialog = false },
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassScreen(selectedLevel: Int?, onBack: (() -> Unit)?, onSelect: (Int) -> Unit) {
    AppScaffold("Choose your class", onBack) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(96.dp),
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(SchoolClass.ALL, key = { it.level }) { cls ->
                val selected = cls.level == selectedLevel
                Card(
                    onClick = { onSelect(cls.level) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Class", style = MaterialTheme.typography.labelMedium)
                        Text(
                            cls.level.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center,
                        )
                        if (selected) Text("Selected", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun MediumScreen(mediums: List<Medium>, selectedId: String?, onBack: (() -> Unit)?, onSelect: (String) -> Unit) {
    AppScaffold("Choose your medium", onBack) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(mediums, key = { it.id }) { m ->
                SelectableCard(m.name, null, m.id == selectedId, { onSelect(m.id) })
            }
        }
    }
}

@Composable
fun StreamScreen(streams: List<Stream>, selectedId: String?, onBack: (() -> Unit)?, onSelect: (String) -> Unit) {
    AppScaffold("Choose your stream", onBack) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text("Classes 11 and 12 are organised by stream.", style = MaterialTheme.typography.bodyMedium) }
            items(streams, key = { it.id }) { s ->
                SelectableCard(s.name, null, s.id == selectedId, { onSelect(s.id) })
            }
        }
    }
}
