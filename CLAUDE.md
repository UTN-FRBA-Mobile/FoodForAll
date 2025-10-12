# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FoodForAll is an Android application built with Kotlin and Jetpack Compose that helps users discover restaurants. The app features a map view, restaurant listings, and filtering capabilities.

## Code Style Rules

**IMPORTANT: Comments are prohibited in this codebase.**
- Do NOT add code comments (no `//`, `/* */`, `/** */`)
- Code should be self-documenting through clear naming and structure
- Use descriptive variable and function names instead of comments
- This applies to all files: Kotlin, JavaScript, configuration files, etc.

## Build and Development Commands

### Prerequisites

1. **Node.js & Firebase CLI**
   ```bash
   npm install
   npm install -g firebase-tools
   ```

2. **Google Maps API Key** - Create `android/local.properties`:
   ```
   MAPS_API_KEY=<YOUR_GOOGLE_MAPS_API_KEY>
   ```

3. **Firebase Credentials** - Download `google-services.json` from Firebase Console:
   - `android/app/src/debug/google-services.json` (dev)
   - `android/app/src/release/google-services.json` (prod)
   - Use `.template` files as reference

### Running the Project

```bash
# Start Firebase emulator
npm run emulators

# Load test data (in another terminal)
npm run seed

# Build and install app
cd android && ./gradlew installDebug
```

### Other Commands
```bash
# Android
cd android
./gradlew clean              # Clean build
./gradlew test               # Unit tests
./gradlew installRelease     # Production build
```

## Architecture

### Project Structure
- **Package**: `ar.edu.utn.frba.mobile.foodforall`
- **Main Activity**: `MainActivity.kt` - Entry point using Jetpack Compose
- **Navigation**: `ui/navigation/AppRoot.kt` - Bottom navigation with 3 tabs (Home, Search, Profile)
- **UI Structure**:
  - `ui/screens/home/` - Home screen with map and restaurant list tabs
  - `ui/screens/search/` - Search functionality
  - `ui/screens/profile/` - User profile
  - `ui/components/` - Reusable UI components
  - `ui/theme/` - App theming and styling

### Key Components
- **HomeScreen**: Main screen with tabbed interface (Map view and Restaurant list)
- **Restaurant data**: Stored in Firebase Firestore, loaded via repositories
- **RestaurantCard**: Component for displaying restaurant information with async images
- **MapTab**: Google Maps integration with Firebase-backed restaurant locations
- **Navigation**: Uses Jetpack Navigation Compose with bottom navigation
- **AsyncImage**: Reusable component for loading remote images with Coil

### Dependencies
- Jetpack Compose with Material 3
- Google Maps Compose (`maps-compose:4.3.3`)
- Google Play Services Maps and Location
- Navigation Compose
- Firebase Firestore with GeoFire
- **Coil** (`coil-compose:2.5.0`) for async image loading
- Target SDK: 36, Min SDK: 32

### Firebase Configuration
- **Development**: Connects to local emulator (`10.0.2.2:9000` from Android emulator)
- **Production**: Uses Firebase cloud (`android/app/src/release/google-services.json`)
- **Automatic switching**: `FoodForAllApplication.kt` detects `BuildConfig.DEBUG`

## Firebase Emulator & Seeding

### Structure
```
firebase/
├── data/                # Sample data
├── seed.js             # Seeding script
├── firebase.json       # Emulator config
└── firestore.rules     # Security rules
```

### Usage

```bash
# Start emulator (Firestore: 9000, UI: 4000)
npm run emulators

# Load test data (manual)
npm run seed
```

Access Emulator UI: http://localhost:4000

### Seeding Strategy

The `firebase/seed.js` script runs **manually** (via `npm run seed`):
- Clears all collections (CRUD: Delete)
- Creates 5 users (CRUD: Create)
- Creates 9 restaurants with geohashes (CRUD: Create)
- Generates reviews (CRUD: Create)
- Creates saved restaurants (CRUD: Create)
- Updates restaurant ratings (CRUD: Update)

**Key Features:**
- **Manual execution** - Full control over when to load/clear data
- **Idempotent** - Same result every time
- **Visible** - Verify at http://localhost:4000
- **Editable** - Modify `firebase/seed.js` directly

### How It Works
- Debug builds connect to emulator automatically
- Release builds use Firebase production
- No auto-seeding - run `npm run seed` when needed

## Restaurant Images

Las imágenes de restaurantes se cargan de forma asíncrona desde URLs usando Coil.

### Estructura
- **Modelo**: `Restaurant.imageUrl` contiene la URL de la imagen (puede ser Firebase Storage, CDN, etc.)
- **Componente**: `AsyncImage` maneja la carga asíncrona con estados de loading/error
- **Placeholder**: Si no hay URL, se muestra `R.drawable.restaurant_placeholder`

### Para usar Firebase Storage
1. Subir imágenes a Firebase Storage
2. Obtener la URL pública de cada imagen
3. Actualizar `seed.js` con las URLs reales
4. Ejecutar `npm run seed` para actualizar la base de datos

## Common Development Tasks

### Adding New Screens
1. Create new screen composable in `ui/screens/[screen_name]/`
2. Add route constant to `Routes` object in `AppRoot.kt`
3. Add navigation item to `bottomItems` list if needed
4. Add composable route to NavHost in `AppRoot.kt`

### Working with Maps
- Google Maps API key is loaded from `local.properties`
- Maps integration uses `maps-compose` library
- Location permissions handled in `LocationPermission.kt`

### Testing
The project includes basic test setup with JUnit and Espresso for UI testing.