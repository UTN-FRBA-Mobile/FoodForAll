# Guía para Imágenes de Restaurantes

## 📁 Ubicación de las Imágenes

Coloca las imágenes de restaurantes en:
```
android/app/src/main/res/drawable-hdpi/
```

## 📋 Nombres de Archivos Requeridos

- `panera_rosa.jpg` (o .png, .webp)
- `tomate.jpg`
- `mcdonalds.jpg`
- `mi_barrio.jpg`
- `roldan.jpg`
- `kansas.jpg`
- `mi_barrio_2.jpg`
- `la_parrilla.jpg`
- `sushi_zen.jpg`
- `pizza_corner.jpg`

## 🎨 Formatos Recomendados

- **WebP** (mejor compresión)
- **PNG** (para imágenes con transparencia)
- **JPG** (para fotos)

## 📏 Tamaños Recomendados

- **drawable-hdpi**: 240x240 dp (720x720 px)
- **drawable-xhdpi**: 320x320 dp (960x960 px)
- **drawable-xxhdpi**: 480x480 dp (1440x1440 px)

## 📂 Para Agregar Más Densidades

Crea estas carpetas si quieres soporte para diferentes densidades de pantalla:
```
android/app/src/main/res/drawable-mdpi/     (160 dpi)
android/app/src/main/res/drawable-hdpi/     (240 dpi) ← Ya creado
android/app/src/main/res/drawable-xhdpi/    (320 dpi)
android/app/src/main/res/drawable-xxhdpi/   (480 dpi)
android/app/src/main/res/drawable-xxxhdpi/  (640 dpi)
```

## 🔧 Cómo Activar las Imágenes

Una vez que agregues las imágenes, edita el archivo:
`android/app/src/main/java/ar/edu/utn/frba/mobile/foodforall/ui/screens/home/RestaurantCard.kt`

En la línea 209-215, reemplaza el placeholder con:
```kotlin
Image(
    painter = painterResource(id = R.drawable.${restaurant.imageResource}),
    contentDescription = restaurant.name,
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Crop
)
```

## 📝 Nota Importante

Por ahora, se muestra un placeholder "IMG" donde irían las imágenes reales. Una vez que agregues las imágenes con los nombres correctos, se mostrarán automáticamente.
