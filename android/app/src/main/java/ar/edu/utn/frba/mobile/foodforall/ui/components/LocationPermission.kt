package ar.edu.utn.frba.mobile.foodforall.ui.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Función de ayuda privada para verificar si la aplicación ya tiene concedidos los permisos de ubicación
 * (ya sea fina o gruesa).
 *
 * @param activity La Activity actual, necesaria para verificar los permisos.
 * @return `true` si al menos uno de los permisos de ubicación (fina o gruesa) está concedido, `false` en caso contrario.
 */
private fun hasLocationPermission(activity: Activity): Boolean {
    val fine = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

/**
 * `LocationPermissionGate` es un Composable que actúa como una "puerta" o guardián para el contenido
 * que requiere permisos de ubicación.
 *
 * Se encarga de:
 * 1. Verificar si los permisos ya están concedidos.
 * 2. Solicitar los permisos si no están concedidos (opcionalmente al inicio).
 * 3. Manejar el resultado de la solicitud de permisos.
 * 4. Determinar si los permisos fueron denegados permanentemente.
 * 5. Invocar un callback `onResult` con el estado final del permiso.
 * 6. Mostrar el contenido proporcionado (`content`) solo si los permisos son finalmente concedidos.
 *
 * @param requestOnStart Si es `true`, la solicitud de permisos se lanzará automáticamente
 *                       cuando el componente entre en la composición si los permisos no están ya concedidos.
 *                       Si es `false`, los permisos solo se solicitarán mediante una acción explícita
 *                       (no implementada directamente en este gate, pero podría ser a través del `rationale`).
 * @param onResult Callback que se invoca con `true` si los permisos de ubicación fueron concedidos,
 *                 o `false` en caso contrario (denegados o denegados permanentemente).
 * @param content El Composable que se mostrará si y solo si los permisos de ubicación son concedidos.
 *                Si los permisos no se conceden, este `content` no se compondrá.
 */
@Composable
fun LocationPermissionGate(
    requestOnStart: Boolean = true,
    onResult: (granted: Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity

    // Estado para rastrear si la solicitud de permisos ya se ha realizado
    var askedOnce by remember { mutableStateOf(false) }

    // Estado para rastrear si los permisos de ubicación están concedidos.
    // Se inicializa verificando el estado actual de los permisos.
    var granted by remember { mutableStateOf(hasLocationPermission(activity)) }

    val permissions = remember {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    // rememberLauncherForActivityResult crea un launcher que puede iniciar una Activity (en este caso, la solicitud de permisos)
    // y manejar su resultado.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions() // Contrato para solicitar múltiples permisos.
    ) { permissionsResultMap ->
        // Este bloque se ejecuta cuando el usuario responde a la solicitud de permisos.
        // permissionsResultMap es un Map<String, Boolean> donde la clave es el nombre del permiso
        // y el valor es true si fue concedido, false en caso contrario.

        askedOnce = true
        granted = permissionsResultMap[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissionsResultMap[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                hasLocationPermission(activity)
        onResult(granted)
    }

    // LaunchedEffect se utiliza para ejecutar código cuando el Composable entra en la composición
    // o cuando sus claves cambian. Aquí, `Unit` como clave significa que se ejecutará solo una vez
    // cuando el Composable se componga por primera vez.
    LaunchedEffect(Unit) {
        if (!granted && requestOnStart) launcher.launch(permissions)
        else onResult(granted)
    }

    // Lógica para determinar si el permiso fue denegado permanentemente.
    // shouldShowRequestPermissionRationale devuelve true si se debe mostrar una justificación al usuario
    // (es decir, el usuario ya negó el permiso una vez pero no seleccionó "No volver a preguntar").
    // Si devuelve false y el permiso no está concedido, puede significar que el usuario seleccionó "No volver a preguntar"
    // o que una política del dispositivo impide solicitar el permiso.
    // val canAskAgain = permissions.any {
    //    ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
    //}
    //val permanentlyDenied = !granted && askedOnce && !canAskAgain

    if (granted) {
        content()
    } else {
        //Aca se puede manejar de alguna manera si el usuario no dio permiso y no se le vuelve a
        //preguntar para preguntarle si lo quiere hacer por la configuracion de la app en android
//        val askAgain = { launcher.launch(permissions) }
//        val openSettings = {
//            val intent = Intent(
//                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                Uri.fromParts("package", activity.packageName, null)
//            )
//            activity.startActivity(intent)
//        }
//        rationale(
//            askAgain = if (!permanentlyDenied) askAgain else openSettings,
//            openSettings = openSettings
//        )
    }
}