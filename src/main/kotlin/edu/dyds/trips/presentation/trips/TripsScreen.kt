package edu.dyds.trips.presentation.trips

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JList
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

fun createAndShowTripsScreen(viewModel: TripsViewModel) {
    val frame = JFrame("Mis viajes")
    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
    frame.minimumSize = Dimension(760, 440)
    frame.layout = BorderLayout(8, 8)

    val status = JLabel("Cargando...")
    val model = DefaultListModel<String>()
    val list = JList(model)

    val buttons = JPanel(FlowLayout(FlowLayout.LEFT))
    val refreshButton = JButton("Recargar")
    val deleteButton = JButton("Eliminar seleccionado")
    buttons.add(refreshButton)
    buttons.add(deleteButton)

    frame.add(status, BorderLayout.NORTH)
    frame.add(JScrollPane(list), BorderLayout.CENTER)
    frame.add(buttons, BorderLayout.SOUTH)

    val idsByIndex = mutableListOf<String>()

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    scope.launch {
        viewModel.uiState.collectLatest { state ->
            SwingUtilities.invokeLater {
                when (state) {
                    TripsUiState.Loading -> status.text = "Cargando viajes..."
                    is TripsUiState.Error -> {
                        status.text = "Error: ${state.message}"
                        model.clear()
                        idsByIndex.clear()
                    }

                    is TripsUiState.Success -> {
                        model.clear()
                        idsByIndex.clear()
                        state.trips.forEach { trip ->
                            idsByIndex.add(trip.id)
                            model.addElement(
                                "${trip.countryName} (${trip.countryCode}) | ${trip.startDate} -> ${trip.endDate} | ${trip.notes}"
                            )
                        }
                        status.text = "${state.trips.size} viajes"
                    }
                }
            }
        }
    }

    scope.launch {
        viewModel.operationState.collectLatest { operationState ->
            SwingUtilities.invokeLater {
                when (operationState) {
                    TripOperationUiState.Idle,
                    TripOperationUiState.InFlight -> Unit
                    is TripOperationUiState.Success -> viewModel.clearOperationState()
                    is TripOperationUiState.Error -> {
                        status.text = "No se pudo eliminar: ${operationState.message}"
                        viewModel.clearOperationState()
                    }
                }
            }
        }
    }

    refreshButton.addActionListener { viewModel.loadTrips() }
    deleteButton.addActionListener {
        val index = list.selectedIndex
        if (index >= 0 && index < idsByIndex.size) {
            viewModel.deleteTrip(idsByIndex[index])
        }
    }

    frame.addWindowListener(object : java.awt.event.WindowAdapter() {
        override fun windowClosed(e: java.awt.event.WindowEvent?) {
            scope.cancel()
        }
    })

    frame.setLocationRelativeTo(null)
    frame.isVisible = true

    viewModel.loadTrips()
}

