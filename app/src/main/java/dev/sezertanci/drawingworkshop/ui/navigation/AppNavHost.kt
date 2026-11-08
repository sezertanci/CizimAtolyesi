package dev.sezertanci.drawingworkshop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.sezertanci.drawingworkshop.ui.drawing.DrawingRoute
import dev.sezertanci.drawingworkshop.ui.gallery.GalleryRoute
import dev.sezertanci.drawingworkshop.ui.gallery.TrashRoute

/** Uygulamanın ekranları. Açılış ekranı galeridir. */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.GALLERY) {
        composable(Routes.GALLERY) { entry ->
            GalleryRoute(
                onNewDrawing = { navController.navigateFrom(entry, Routes.drawing(null)) },
                onOpenDrawing = { id -> navController.navigateFrom(entry, Routes.drawing(id)) },
                onOpenTrash = { navController.navigateFrom(entry, Routes.TRASH) },
            )
        }
        composable(
            route = Routes.DRAWING_PATTERN,
            arguments = listOf(
                navArgument(Routes.ARG_DRAWING_ID) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        ) { entry ->
            DrawingRoute(onHome = { navController.popBackFrom(entry) })
        }
        composable(Routes.TRASH) { entry ->
            TrashRoute(onBack = { navController.popBackFrom(entry) })
        }
    }
}

/*
 * Çocuklar düğmelere art arda hızlıca basabilir. Geçiş sürerken gelen ikinci dokunuş yok sayılır;
 * böylece aynı ekran iki kez açılmaz ve galeri de kapanıp boş ekran kalmaz.
 */
private fun NavBackStackEntry.isActive(): Boolean = lifecycle.currentState == Lifecycle.State.RESUMED

private fun NavController.navigateFrom(entry: NavBackStackEntry, route: String) {
    if (entry.isActive()) navigate(route)
}

private fun NavController.popBackFrom(entry: NavBackStackEntry) {
    if (entry.isActive()) popBackStack()
}
