const admin = require('firebase-admin');
const geofireCommon = require('geofire-common');

process.env.FIRESTORE_EMULATOR_HOST = 'localhost:9000';
process.env.FIREBASE_AUTH_EMULATOR_HOST = 'localhost:9099';
admin.initializeApp({ projectId: 'foodforall-22d50' });

const db = admin.firestore();
const auth = admin.auth();

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomFloat(min, max) {
  return Math.random() * (max - min) + min;
}

function randomChoice(array) {
  return array[Math.floor(Math.random() * array.length)];
}

const sampleRestaurants = [
  {
    name: "Panera Rosa",
    description: "2X1 en cafes HOY",
    snippet: "Cafetería con opciones saludables",
    lat: -34.5975,
    lng: -58.4180,
    imageUrl: "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400&h=300&fit=crop",
    hasVegetarianOption: true,
    hasCeliacOption: true,
    hasOffer: true,
    rating: 3.5
  },
  {
    name: "Pizza Corner",
    description: "Pizza artesanal con ingredientes premium",
    snippet: "Pizzería artesanal",
    lat: -34.6000,
    lng: -58.4200,
    imageUrl: "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400&h=300&fit=crop",
    hasVegetarianOption: true,
    hasVeganOption: true,
    hasOffer: true,
    rating: 3.7
  },
  {
    name: "McDonald's",
    description: "Nueva hamburguesa",
    snippet: "Fast food internacional",
    lat: -34.5995,
    lng: -58.4150,
    imageUrl: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=300&fit=crop",
    hasVegetarianOption: true,
    hasOffer: true,
    rating: 3.6
  },
  {
    name: "Tomate",
    description: "Veni a la nueva sucursal de Tomate",
    snippet: "Comida vegetariana y vegana",
    lat: -34.5845,
    lng: -58.4050,
    imageUrl: "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400&h=300&fit=crop",
    hasVegetarianOption: true,
    hasVeganOption: true,
    rating: 4.1
  },
  {
    name: "Roldán",
    description: "Obtené un 5% off en tu cena de hoy mostrando este mensaje!",
    snippet: "Parrilla moderna",
    lat: -34.5832,
    lng: -58.4272,
    imageUrl: "https://images.unsplash.com/photo-1603360946369-dc9bb6258143?w=400&h=300&fit=crop",
    hasVegetarianOption: true,
    hasCeliacOption: true,
    hasVeganOption: true,
    hasOffer: true,
    rating: 4.2
  },
  {
    name: "Kansas",
    description: "Nuevo Plato!",
    snippet: "Carnes premium",
    lat: -34.6108,
    lng: -58.3635,
    imageUrl: "https://images.unsplash.com/photo-1588168333986-5078d3ae3976?w=400&h=300&fit=crop",
    rating: 4.3
  },
  {
    name: "Mi Barrio",
    description: "Platos tradicionales de la casa",
    snippet: "Cocina casera argentina",
    lat: -34.6217,
    lng: -58.3695,
    imageUrl: "https://images.unsplash.com/photo-1544025162-d76694265947?w=400&h=300&fit=crop",
    hasVegetarianOption: true,
    hasSiboOption: true,
    hasOffer: true,
    rating: 4.8
  },
  {
    name: "La Parrilla",
    description: "Carnes a la parrilla todos los días",
    snippet: "Parrilla argentina tradicional",
    lat: -34.6345,
    lng: -58.3632,
    imageUrl: "https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?w=400&h=300&fit=crop",
    hasCeliacOption: true,
    hasOffer: true,
    rating: 4.9
  },
  {
    name: "Sushi Zen",
    description: "Sushi fresco y auténtico",
    snippet: "Comida japonesa",
    lat: -34.6280,
    lng: -58.3700,
    imageUrl: "https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400&h=300&fit=crop",
    hasVegetarianOption: true,
    hasSiboOption: true,
    rating: 4.7
  }
];

const sampleUsers = [
  {
    fullName: "Juan Pérez",
    username: "@juanperez",
    email: "juan@example.com",
    avatarUrl: null,
    dietaryRestrictions: ["vegetarian"]
  },
  {
    fullName: "María González",
    username: "@mariag",
    email: "maria@example.com",
    avatarUrl: null,
    dietaryRestrictions: ["vegan", "celiac"]
  },
  {
    fullName: "Carlos Rodríguez",
    username: "@carlosr",
    email: "carlos@example.com",
    avatarUrl: null,
    dietaryRestrictions: ["celiac"]
  },
  {
    fullName: "Ana Martínez",
    username: "@anam",
    email: "ana@example.com",
    avatarUrl: null,
    dietaryRestrictions: ["sibo"]
  },
  {
    fullName: "Pedro López",
    username: "@pedrol",
    email: "pedro@example.com",
    avatarUrl: null,
    dietaryRestrictions: []
  }
];

const sampleComments = [
  "Excelente lugar, muy recomendable!",
  "La comida es deliciosa y el servicio impecable.",
  "Buena relación precio-calidad.",
  "Ambiente acogedor y platos bien preparados.",
  "Perfecta opción para mi restricción alimentaria.",
  "Volveré sin dudas!",
  "Me gustó mucho, aunque el servicio podría mejorar.",
  "Increíble experiencia culinaria!",
  "Opciones vegetarianas excelentes.",
  "Ideal para cenar en familia."
];

async function clearCollections() {
  console.log('🧹 Limpiando base de datos...');

  try {
    const listUsersResult = await auth.listUsers();
    for (const userRecord of listUsersResult.users) {
      await auth.deleteUser(userRecord.uid);
    }
    console.log(`   🗑️  Auth users: ${listUsersResult.users.length} usuarios eliminados`);
  } catch (error) {
    console.error('   ⚠️  Error limpiando usuarios de Auth:', error.message);
  }

  const collections = ['restaurants', 'users', 'reviews', 'savedRestaurants'];

  for (const collectionName of collections) {
    const snapshot = await db.collection(collectionName).get();
    const batch = db.batch();
    snapshot.docs.forEach(doc => batch.delete(doc.ref));
    await batch.commit();
    console.log(`   🗑️  ${collectionName}: ${snapshot.size} documentos eliminados`);
  }
}

async function seedUsers() {
  console.log('\n👥 Creando usuarios...');
  const userIds = [];
  const now = Date.now();
  const defaultPassword = 'test123';

  for (const user of sampleUsers) {
    try {
      const userRecord = await auth.createUser({
        email: user.email,
        password: defaultPassword,
        displayName: user.fullName
      });

      await db.collection('users').doc(userRecord.uid).set({
        fullName: user.fullName,
        username: user.username,
        email: user.email,
        avatarUrl: user.avatarUrl,
        dietaryRestrictions: user.dietaryRestrictions,
        createdAt: now,
        updatedAt: now
      });

      userIds.push(userRecord.uid);
      console.log(`   ✅ ${user.fullName} (${user.email} / ${defaultPassword})`);
    } catch (error) {
      console.error(`   ❌ Error creando ${user.fullName}:`, error.message);
    }
  }

  return userIds;
}

async function seedRestaurants() {
  console.log('\n🍽️  Creando restaurantes...');
  const restaurantIds = [];
  const now = Date.now();

  for (const restaurant of sampleRestaurants) {
    const geohash = geofireCommon.geohashForLocation([restaurant.lat, restaurant.lng]);

    const docRef = await db.collection('restaurants').add({
      ...restaurant,
      geohash,
      icon: 0,
      likes: randomInt(50, 300),
      comments: randomInt(5, 50),
      saves: randomInt(10, 100),
      hasVegetarianOption: restaurant.hasVegetarianOption || false,
      hasVeganOption: restaurant.hasVeganOption || false,
      hasCeliacOption: restaurant.hasCeliacOption || false,
      hasSiboOption: restaurant.hasSiboOption || false,
      hasOffer: restaurant.hasOffer || false,
      createdAt: now,
      updatedAt: now
    });

    restaurantIds.push(docRef.id);
    console.log(`   ✅ ${restaurant.name} (geohash: ${geohash})`);
  }

  return restaurantIds;
}

async function seedReviews(userIds, restaurantIds) {
  console.log('\n⭐ Creando reviews...');
  const dietaryRestrictions = ['vegetarian', 'vegan', 'celiac', 'sibo', 'general'];
  let count = 0;

  for (let i = 0; i < restaurantIds.length; i++) {
    const restaurantId = restaurantIds[i];
    const targetRating = sampleRestaurants[i].rating;
    const numReviews = randomInt(5, 15);

    for (let j = 0; j < numReviews; j++) {
      const userId = userIds[j % userIds.length];
      const createdAt = Date.now() - (j * 86400000 * randomInt(1, 30));
      const rating = Math.max(1, Math.min(5, targetRating + randomFloat(-0.5, 0.5)));

      await db.collection('reviews').add({
        userId,
        restaurantId,
        rating,
        comment: randomChoice(sampleComments),
        dietaryRestriction: randomChoice(dietaryRestrictions),
        imageUrls: [],
        createdAt,
        updatedAt: createdAt
      });

      count++;
    }
  }

  console.log(`   ✅ ${count} reviews creadas`);
}

async function seedSavedRestaurants(userIds, restaurantIds) {
  console.log('\n💾 Creando restaurantes guardados...');
  let count = 0;
  const now = Date.now();

  for (const userId of userIds) {
    const numToSave = randomInt(2, 4);
    const shuffled = [...restaurantIds].sort(() => 0.5 - Math.random());
    const selected = shuffled.slice(0, numToSave);

    for (const restaurantId of selected) {
      await db.collection('savedRestaurants').add({
        userId,
        restaurantId,
        savedAt: now
      });
      count++;
    }
  }

  console.log(`   ✅ ${count} restaurantes guardados`);
}

async function updateRestaurantRatings(restaurantIds) {
  console.log('\n📊 Actualizando ratings de restaurantes...');

  for (const restaurantId of restaurantIds) {
    const reviewsSnapshot = await db.collection('reviews')
      .where('restaurantId', '==', restaurantId)
      .get();

    if (reviewsSnapshot.empty) continue;

    let totalRating = 0;
    reviewsSnapshot.forEach(doc => {
      totalRating += doc.data().rating;
    });
    const averageRating = totalRating / reviewsSnapshot.size;
    const reviewCount = reviewsSnapshot.size;

    await db.collection('restaurants').doc(restaurantId).update({
      rating: parseFloat(averageRating.toFixed(1)),
      comments: reviewCount,
      updatedAt: Date.now()
    });

    console.log(`   ✅ ${restaurantId}: ${averageRating.toFixed(1)}⭐ (${reviewCount} reviews)`);
  }
}

async function seed() {
  console.log('🌱 Seeding Firestore emulator...\n');
  console.log('📍 Conectando a: localhost:9000');
  console.log('🔗 UI disponible en: http://localhost:4000\n');

  try {
    await clearCollections();

    const userIds = await seedUsers();
    const restaurantIds = await seedRestaurants();
    await seedReviews(userIds, restaurantIds);
    await seedSavedRestaurants(userIds, restaurantIds);
    await updateRestaurantRatings(restaurantIds);

    console.log('\n🎉 ¡Seeding completado exitosamente!');
    console.log(`📊 Resumen:`);
    console.log(`   - ${userIds.length} usuarios (contraseña: test123)`);
    console.log(`   - ${restaurantIds.length} restaurantes`);
    console.log(`\n💡 Abrí http://localhost:4000 para ver los datos`);
    console.log(`🔐 Podés hacer login con cualquiera de estos usuarios:`);
    console.log(`   - juan@example.com / test123`);
    console.log(`   - maria@example.com / test123`);
    console.log(`   - carlos@example.com / test123`);
    console.log(`   - ana@example.com / test123`);
    console.log(`   - pedro@example.com / test123\n`);

    process.exit(0);
  } catch (error) {
    console.error('\n❌ Error durante el seeding:', error);
    process.exit(1);
  }
}

seed();
