package ar.edu.utn.frba.mobile.foodforall.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore("user_prefs")

object PreferencesKeys {
    val SHOW_FOREGROUND_RATIONALE = booleanPreferencesKey("show_foreground_rationale")
    val SHOW_BACKGROUND_RATIONALE = booleanPreferencesKey("show_background_rationale")
}

suspend fun Context.setRationaleVisibility(foreground: Boolean?, background: Boolean?) {
    dataStore.edit { prefs ->
        foreground?.let { prefs[PreferencesKeys.SHOW_FOREGROUND_RATIONALE] = it }
        background?.let { prefs[PreferencesKeys.SHOW_BACKGROUND_RATIONALE] = it }
    }
}

suspend fun Context.getRationaleVisibility(): Pair<Boolean, Boolean> {
    val prefs = dataStore.data.first()
    val fg = prefs[PreferencesKeys.SHOW_FOREGROUND_RATIONALE] ?: true
    val bg = prefs[PreferencesKeys.SHOW_BACKGROUND_RATIONALE] ?: true
    return fg to bg
}

private fun hasFineOrCoarse(activity: Activity): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        activity, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        activity, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

private fun hasBackgroundLocation(activity: Activity): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    } else true
}

@Composable
fun LocationPermissionGate(
    requestOnStart: Boolean = true,
    onResult: (granted: Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var foregroundGranted by remember { mutableStateOf(hasFineOrCoarse(activity)) }
    var attemptedForeground by remember { mutableStateOf(false) }
    var triedBackground by remember { mutableStateOf(false) }

    var showForegroundRationale by rememberSaveable { mutableStateOf(true) }
    var showBackgroundRationale by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val (fg, bg) = context.getRationaleVisibility()
        showForegroundRationale = fg
        showBackgroundRationale = bg
    }

    val notificationsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* noop */ }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        foregroundGranted =
            res[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    res[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                    hasFineOrCoarse(activity)
        onResult(foregroundGranted)
    }

    val backgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        triedBackground = true
        onResult(foregroundGranted)
    }

    val appSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val fg = hasFineOrCoarse(activity)
        val bg = hasBackgroundLocation(activity)
        foregroundGranted = fg

        if (fg && showForegroundRationale) {
            showForegroundRationale = false
            scope.launch { context.setRationaleVisibility(false, null) }
        }
        if (bg && showBackgroundRationale) {
            showBackgroundRationale = false
            scope.launch { context.setRationaleVisibility(null, false) }
        }
        onResult(fg)
    }

    fun openAppSettingsViaLauncher() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        )
        appSettingsLauncher.launch(intent)
    }

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val fg = hasFineOrCoarse(activity)
                val bg = hasBackgroundLocation(activity)
                foregroundGranted = fg

                if (fg && showForegroundRationale) {
                    showForegroundRationale = false
                    scope.launch { context.setRationaleVisibility(false, null) }
                }
                if (bg && showBackgroundRationale) {
                    showBackgroundRationale = false
                    scope.launch { context.setRationaleVisibility(null, false) }
                }
                onResult(fg)
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    val shouldShowFine = remember {
        mutableStateOf(
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
    val shouldShowCoarse = remember {
        mutableStateOf(
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    val shouldShowBg = remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            else false
        )
    }

    LaunchedEffect(foregroundGranted, triedBackground) {
        shouldShowFine.value =
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
            )
        shouldShowCoarse.value =
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            shouldShowBg.value =
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
        }
    }

    val showRationaleForeground =
        !foregroundGranted && (shouldShowFine.value || shouldShowCoarse.value)
    val suggestSettingsForeground =
        !foregroundGranted && !showRationaleForeground && attemptedForeground

    val showRationaleBgApi29 =
        (Build.VERSION.SDK_INT == 29) &&
                !hasBackgroundLocation(activity) &&
                shouldShowBg.value &&
                triedBackground

    val suggestSettingsBgApi29 =
        (Build.VERSION.SDK_INT == 29) &&
                !hasBackgroundLocation(activity) &&
                !shouldShowBg.value &&
                triedBackground

    val suggestSettingsBgApi30Plus =
        (Build.VERSION.SDK_INT >= 30) &&
                !hasBackgroundLocation(activity)

    LaunchedEffect(requestOnStart) {
        if (!requestOnStart) {
            onResult(foregroundGranted)
            return@LaunchedEffect
        }

        if (Build.VERSION.SDK_INT >= 33) {
            notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (!foregroundGranted) {
            attemptedForeground = true
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return@LaunchedEffect
        }

        if (Build.VERSION.SDK_INT == 29 &&
            !hasBackgroundLocation(activity) &&
            !triedBackground
        ) {
            triedBackground = true
            backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            return@LaunchedEffect
        }

        onResult(foregroundGranted)
    }

    if (foregroundGranted) {
        content()

        if (!hasBackgroundLocation(activity) && showBackgroundRationale) {
            Spacer(Modifier.height(12.dp))
            when {
                showRationaleBgApi29 -> {
                    RationaleCard(
                        title = "Permiso en segundo plano",
                        body = "Para detectar permanencias cuando la app no esté en primer plano, necesitamos acceso a la ubicación en segundo plano.",
                        primaryActionLabel = "Conceder en segundo plano",
                        onPrimary = { backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION) },
                        secondaryActionLabel = "Más tarde",
                        onSecondary = { /* noop */ },
                        onDismiss = { showBackgroundRationale = false }
                    )
                }
                suggestSettingsBgApi29 || suggestSettingsBgApi30Plus -> {
                    RationaleCard(
                        title = "Activar \"Permitir siempre\"",
                        body = if (Build.VERSION.SDK_INT >= 30)
                            "En Android 11 o superior, \"Permitir siempre\" se otorga desde Ajustes de la app."
                        else
                            "Si ya no aparece el diálogo, habilitalo desde Ajustes.",
                        primaryActionLabel = "Abrir Ajustes",
                        onPrimary = { openAppSettingsViaLauncher() },
                        onDismiss = {
                            showBackgroundRationale = false
                            scope.launch { context.setRationaleVisibility(null, false) }
                        }
                    )
                }
            }
        }
    } else {
        when {
            showRationaleForeground && showForegroundRationale -> {
                RationaleCard(
                    title = "Se necesita tu ubicación",
                    body = "Para que el servicio funcione, necesitamos acceso a la ubicación mientras usás la app.",
                    primaryActionLabel = "Conceder ubicación",
                    onPrimary = {
                        attemptedForeground = true
                        locationLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    secondaryActionLabel = when {
                        Build.VERSION.SDK_INT == 29 -> "También 2º plano (Android 10)"
                        Build.VERSION.SDK_INT >= 30 -> "Abrir Ajustes (Siempre)"
                        else -> null
                    },
                    onSecondary = {
                        if (Build.VERSION.SDK_INT == 29) {
                            triedBackground = true
                            backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        } else if (Build.VERSION.SDK_INT >= 30) {
                            openAppSettingsViaLauncher()
                        }
                    },
                    onDismiss = {
                        showForegroundRationale = false
                        scope.launch { context.setRationaleVisibility(false, null) }
                    }
                )
            }
            suggestSettingsForeground && showForegroundRationale -> {
                RationaleCard(
                    title = "Habilitar ubicación desde Ajustes",
                    body = "Parece que desactivaste los diálogos. Podés habilitarla desde Ajustes de la app.",
                    primaryActionLabel = "Abrir Ajustes",
                    onPrimary = { openAppSettingsViaLauncher() },
                    onDismiss = {
                        showForegroundRationale = false
                        scope.launch { context.setRationaleVisibility(false, null) }
                    }
                )
            }
            else -> {
                if (showForegroundRationale) {
                    RationaleCard(
                        title = "Permiso de ubicación",
                        body = "Necesitamos tu ubicación para iniciar el servicio.",
                        primaryActionLabel = "Conceder ubicación",
                        onPrimary = {
                            attemptedForeground = true
                            locationLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        onDismiss = {
                            showForegroundRationale = false
                            scope.launch { context.setRationaleVisibility(false, null) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RationaleCard(
    title: String,
    body: String,
    primaryActionLabel: String,
    onPrimary: () -> Unit,
    secondaryActionLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Box(Modifier.fillMaxWidth()) {
            if (onDismiss != null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    Button(onClick = onPrimary) { Text(primaryActionLabel) }
                    if (secondaryActionLabel != null && onSecondary != null) {
                        Button(onClick = onSecondary) { Text(secondaryActionLabel) }
                    }
                }
            }
        }
    }
}
