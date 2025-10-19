# FoodForAll

App Android para descubrir restaurantes con opciones para restricciones alimentarias.

## Requisitos

- Android Studio
- Node.js y npm
- Firebase CLI: `npm install -g firebase-tools`

## Setup

### 1. Instalar dependencias

```bash
npm install
```

### 2. Configurar archivos

#### Google Maps API Key

Crear `android/local.properties`:
```properties
sdk.dir=/path/to/Android/Sdk
MAPS_API_KEY=TU_API_KEY
```

#### Firebase

Usar los `.template` como referencia:
- `android/app/src/debug/google-services.json` (desarrollo con emuladores)
- `android/app/src/release/google-services.json` (producción)

### 3. Correr el proyecto

```bash
# Terminal 1: Iniciar emuladores Firebase (Firestore + Auth)
npm run emulators

# Terminal 2: Cargar datos de prueba
npm run seed

# Terminal 3: Instalar app
cd android && ./gradlew installDebug
```

**Emulador UI:** http://localhost:4000

## Estructura

```
FoodForAll/
├── android/          # App Android (Kotlin + Compose)
├── firebase/         # Configuración Firebase
│   ├── seed.js      # Script de datos
│   ├── firebase.json
│   └── firestore.rules
└── package.json
```

## Autenticación

La app incluye un sistema de autenticación con Firebase Auth:

- **Login/Registro:** Email y contraseña
- **Modo guest:** Navegar sin autenticación
- **Protección de features:** Login requerido para crear reviews, guardar restaurantes y acceder al perfil
- **Logout:** Disponible en la pantalla de perfil

### Usuarios de prueba (creados por el seeder)

Todos los usuarios tienen la contraseña: `test123`

```
juan@example.com
maria@example.com
carlos@example.com
laura@example.com
pedro@example.com
```

## Comandos

```bash
# Firebase
npm run emulators    # Iniciar emuladores (Firestore:9000, Auth:9099, UI:4000)
npm run seed        # Cargar datos de prueba (usuarios, restaurantes, reviews)

# Android
cd android
./gradlew installDebug     # Debug (conecta a emuladores)
./gradlew installRelease   # Release (conecta a Firebase producción)
./gradlew clean           # Limpiar build
```

## Troubleshooting

**Error: google-services.json missing**
```bash
cp android/app/src/debug/google-services.json.template android/app/src/debug/google-services.json
# Editar con tus credenciales
```

**Error: No se conecta al emulador**
- Verificar que esté corriendo: `npm run emulators`
- La app usa `10.0.2.2:9000` (Firestore) y `10.0.2.2:9099` (Auth) desde el emulador Android
- Verificar que el emulador UI muestre datos en http://localhost:4000

**Error: Datos no se cargan después de cambios**
```bash
# Limpiar datos de la app
adb shell pm clear ar.edu.utn.frba.mobile.foodforall
# Reinstalar
cd android && ./gradlew installDebug
```

**Error de autenticación (INVALID_REFRESH_TOKEN)**
```bash
# Limpiar cache de la app
adb shell pm clear ar.edu.utn.frba.mobile.foodforall
# O desde el dispositivo: Settings → Apps → FoodForAll → Storage → Clear Data
```
