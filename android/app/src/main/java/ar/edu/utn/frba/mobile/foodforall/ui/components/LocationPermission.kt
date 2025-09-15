package ar.edu.utn.frba.mobile.foodforall.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest

private fun hasLocationPermission(activity: Activity): Boolean {
    val fine = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

@Composable
fun LocationPermissionGate(
    requestOnStart: Boolean = true,
    onResult: (granted: Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    var askedOnce by remember { mutableStateOf(false) }
    var granted by remember { mutableStateOf(hasLocationPermission(activity)) }

    val permissions = remember {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        askedOnce = true
        granted = res[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                res[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                hasLocationPermission(activity)
        onResult(granted)
    }

    LaunchedEffect(Unit) {
        if (!granted && requestOnStart) launcher.launch(permissions)
        else onResult(granted)
    }

    val canAskAgain = permissions.any {
        ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
    }
    val permanentlyDenied = !granted && askedOnce && !canAskAgain

    if (granted) {
        content()
    } else {
        val askAgain = { launcher.launch(permissions) }
        val openSettings = {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.packageName, null)
            )
            activity.startActivity(intent)
        }
//        rationale(
//            askAgain = if (!permanentlyDenied) askAgain else openSettings,
//            openSettings = openSettings
//        )
    }
}