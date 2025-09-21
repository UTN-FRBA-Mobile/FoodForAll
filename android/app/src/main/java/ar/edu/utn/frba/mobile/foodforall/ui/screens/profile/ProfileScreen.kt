package ar.edu.utn.frba.mobile.foodforall.ui.screens.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

sealed class ProfileTab(val title: String) {
    data object MyReviews : ProfileTab("Mis Reseñas")
    data object Saved : ProfileTab("Guardados")
}

private val profileTabs = listOf(ProfileTab.MyReviews, ProfileTab.Saved)

@Composable
fun ProfileScreen() {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedTab = profileTabs[selectedTabIndex]

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ProfileHeader(
            userProfile = SampleUserData.currentUser,
            modifier = Modifier.fillMaxWidth()
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
                        reviews = SampleUserData.userReviews,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is ProfileTab.Saved -> {
                    SavedRestaurantsTab(
                        savedRestaurants = SampleUserData.savedRestaurants,
                        onRestaurantClick = { /* TODO: Navigate to restaurant detail */ },
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
    ProfileScreen()
}