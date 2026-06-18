package edu.dyds.trips

import edu.dyds.trips.di.TripsDependencyInjector
import edu.dyds.trips.presentation.navigation.createAndShowAppWindow
import javax.swing.SwingUtilities

fun main() {
    SwingUtilities.invokeLater {
        createAndShowAppWindow(
            scope = TripsDependencyInjector.appScope(),
            homeViewModel = TripsDependencyInjector.createHomeViewModel(),
            tripsViewModel = TripsDependencyInjector.createTripsViewModel(),
            createDetailViewModel = { TripsDependencyInjector.createDetailViewModel() },
            onClose = { TripsDependencyInjector.shutdown() }
        )
    }
}
