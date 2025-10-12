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

Usar los `.template` como referencia.

### 3. Correr el proyecto

```bash
# Terminal 1: Iniciar emulador Firebase
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
│   └── ...
└── package.json
```

## Comandos

```bash
# Firebase
npm run emulators    # Iniciar emulador
npm run seed        # Cargar datos de prueba

# Android
cd android
./gradlew installDebug     # Debug
./gradlew installRelease   # Release
```

## Troubleshooting

**Error: google-services.json missing**
```bash
cp android/app/src/debug/google-services.json.template android/app/src/debug/google-services.json
# Editar con tus credenciales
```

**Error: No se conecta al emulador**
- Verificar que esté corriendo: `npm run emulators`
- La app usa `10.0.2.2:9000` para conectarse desde el emulador Android
