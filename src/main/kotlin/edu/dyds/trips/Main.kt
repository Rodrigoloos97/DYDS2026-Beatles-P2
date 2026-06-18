package edu.dyds.trips

import edu.dyds.trips.di.TripsDependencyInjector
import edu.dyds.trips.presentation.showSplashScreen
import edu.dyds.trips.presentation.navigation.createAndShowAppWindow
import kotlin.concurrent.thread
import javax.swing.SwingUtilities

fun main() {
    SwingUtilities.invokeLater {
        val splash = showSplashScreen()

        thread(name = "splash-delay") {
            Thread.sleep(1500)
            SwingUtilities.invokeLater {
                createAndShowAppWindow(
                    scope = TripsDependencyInjector.appScope(),
                    homeViewModel = TripsDependencyInjector.createHomeViewModel(),
                    tripsViewModel = TripsDependencyInjector.createTripsViewModel(),
                    createDetailViewModel = { TripsDependencyInjector.createDetailViewModel() },
                    onClose = { TripsDependencyInjector.shutdown() }
                )
                splash?.dispose()
            }
        }
    }
}
