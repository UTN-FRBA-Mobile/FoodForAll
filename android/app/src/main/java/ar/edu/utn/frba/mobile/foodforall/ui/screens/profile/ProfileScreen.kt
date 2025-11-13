package ar.edu.utn.frba.mobile.foodforall.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import ar.edu.utn.frba.mobile.foodforall.domain.model.Review
import ar.edu.utn.frba.mobile.foodforall.domain.model.User
import ar.edu.utn.frba.mobile.foodforall.domain.model.DietaryRestriction
import kotlinx.coroutines.launch

sealed class ProfileTab(val title: String) {
    data object MyReviews : ProfileTab("Mis Reseñas")
    data object Saved : ProfileTab("Guardados")
}

private val profileTabs = listOf(ProfileTab.MyReviews, ProfileTab.Saved)

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    authViewModel: ar.edu.utn.frba.mobile.foodforall.ui.viewmodel.AuthViewModel,
    onRestaurantClick: (String) -> Unit = {}
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedTab = profileTabs[selectedTabIndex]
    var showAuthDialog by rememberSaveable { mutableStateOf(false) }

    val authUser by authViewModel.currentUser.collectAsState()
    val userReviews by viewModel.userReviews.collectAsState()
    val savedRestaurants by viewModel.savedRestaurants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authUser?.id) {
        authUser?.id?.let { userId ->
            viewModel.loadUserData(userId)
        }
    }

    LaunchedEffect(error) {
        error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    if (authUser == null) {
        showAuthDialog = true
    }

    if (showAuthDialog) {
        ar.edu.utn.frba.mobile.foodforall.ui.screens.auth.AuthenticationDialog(
            authViewModel = authViewModel,
            onDismiss = { showAuthDialog = false }
        )
    }

    if (isLoading && authUser == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val user = authUser ?: return

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        ProfileHeader(
            userProfile = user,
            modifier = Modifier.fillMaxWidth(),
            onLogout = { authViewModel.logout() }
        )

        TabRow(selectedTabIndex = selectedTabIndex) {
            profileTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(tab.title) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            when (selectedTab) {
                is ProfileTab.MyReviews -> {
                    MyReviewsTab(
                        reviews = userReviews,
                        onRestaurantClick = onRestaurantClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is ProfileTab.Saved -> {
                    SavedRestaurantsTab(
                        savedRestaurants = savedRestaurants,
                        onRestaurantClick = { restaurant -> onRestaurantClick(restaurant.id) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Use device preview to see ProfileScreen with live data")
    }
}
}
