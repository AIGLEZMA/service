package me.aiglez.service

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import me.aiglez.service.ui.shell.AppShell
import me.aiglez.service.ui.shell.UnsavedChangesController
import me.aiglez.service.ui.shell.UnsavedChangesDialog
import me.aiglez.service.ui.theme.ServiceTheme
import org.koin.compose.koinInject

@Composable
@Preview
fun App(
    exitRequested: Boolean = false,
    onExitCancelled: () -> Unit = {},
    onExitConfirmed: () -> Unit = {},
    unsavedChangesController: UnsavedChangesController = koinInject(),
) {
    val unsavedChanges by unsavedChangesController.state.collectAsState()
    LaunchedEffect(exitRequested, unsavedChanges.hasUnsavedChanges) {
        if (exitRequested && !unsavedChanges.hasUnsavedChanges) {
            onExitConfirmed()
        }
    }
    ServiceTheme(darkTheme = false) {
        AppShell()
        if (exitRequested && unsavedChanges.hasUnsavedChanges) {
            UnsavedChangesDialog(
                isSaving = unsavedChanges.isSaving,
                onSave = {
                    unsavedChangesController.save { saved ->
                        if (saved) onExitConfirmed()
                    }
                },
                onDiscard = {
                    unsavedChangesController.discard()
                    onExitConfirmed()
                },
                onCancel = onExitCancelled,
            )
        }
    }
}

@Composable
fun StartupScreen(showWaiting: Boolean) {
    ServiceTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (showWaiting) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Préparation de votre espace de travail…",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Le premier démarrage peut prendre quelques instants.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
