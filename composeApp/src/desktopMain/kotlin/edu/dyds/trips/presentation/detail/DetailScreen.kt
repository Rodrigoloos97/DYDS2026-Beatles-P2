package edu.dyds.trips.presentation.detail

import edu.dyds.trips.domain.entity.CountryDetail
import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.presentation.trips.TripsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

fun createAndShowDetailScreen(
    countryCode: String,
    viewModel: DetailViewModel,
    tripsViewModel: TripsViewModel
) {
    val frame = JFrame("Detalle de pais")
    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
    frame.minimumSize = Dimension(760, 480)
    frame.layout = BorderLayout(8, 8)

    val header = JLabel("Cargando...")
    val statsPanel = JPanel(GridLayout(0, 1))
    val forecastModel = DefaultListModel<String>()
    val forecastList = JList(forecastModel)

    val actions = JPanel(FlowLayout(FlowLayout.LEFT))
    val saveTripButton = JButton("Guardar viaje")
    saveTripButton.isEnabled = false
    actions.add(saveTripButton)

    frame.add(header, BorderLayout.NORTH)
    frame.add(JScrollPane(forecastList), BorderLayout.CENTER)
    frame.add(statsPanel, BorderLayout.EAST)
    frame.add(actions, BorderLayout.SOUTH)

    var currentDetail: CountryDetail? = null

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    scope.launch {
        viewModel.uiState.collectLatest { state ->
            SwingUtilities.invokeLater {
                when (state) {
                    DetailUiState.Idle -> header.text = "Listo"
                    DetailUiState.Loading -> {
                        header.text = "Cargando detalle de $countryCode..."
                        saveTripButton.isEnabled = false
                    }

                    is DetailUiState.Error -> {
                        header.text = "Error: ${state.message}"
                        forecastModel.clear()
                        statsPanel.removeAll()
                        saveTripButton.isEnabled = false
                        statsPanel.revalidate()
                        statsPanel.repaint()
                    }

                    is DetailUiState.Success -> {
                        currentDetail = state.detail
                        bindDetail(state.detail, header, statsPanel, forecastModel)
                        saveTripButton.isEnabled = true
                    }
                }
            }
        }
    }

    saveTripButton.addActionListener {
        val detail = currentDetail ?: return@addActionListener
        val startDate = JOptionPane.showInputDialog(frame, "Fecha inicio (yyyy-MM-dd):") ?: return@addActionListener
        val endDate = JOptionPane.showInputDialog(frame, "Fecha fin (yyyy-MM-dd):") ?: return@addActionListener
        val notes = JOptionPane.showInputDialog(frame, "Notas:") ?: ""

        tripsViewModel.saveTrip(
            Trip(
                countryCode = detail.country.code,
                countryName = detail.country.name,
                startDate = startDate,
                endDate = endDate,
                notes = notes
            )
        ) { result ->
            SwingUtilities.invokeLater {
                when (result) {
                    is edu.dyds.trips.domain.entity.Result.Success ->
                        JOptionPane.showMessageDialog(frame, "Viaje guardado")

                    is edu.dyds.trips.domain.entity.Result.Failure ->
                        JOptionPane.showMessageDialog(frame, "No se pudo guardar: ${result.exception.message}")
                }
            }
        }
    }

    frame.setLocationRelativeTo(null)
    frame.isVisible = true

    viewModel.loadCountryDetail(countryCode)

    frame.addWindowListener(object : java.awt.event.WindowAdapter() {
        override fun windowClosed(e: java.awt.event.WindowEvent?) {
            scope.cancel()
            viewModel.dispose()
        }
    })
}

private fun bindDetail(
    detail: CountryDetail,
    header: JLabel,
    statsPanel: JPanel,
    forecastModel: DefaultListModel<String>
) {
    header.text = "${detail.country.name} (${detail.country.code})"

    statsPanel.removeAll()
    statsPanel.layout = GridLayout(0, 1)
    statsPanel.add(JLabel("Region: ${detail.country.region}"))
    statsPanel.add(JLabel("Capital: ${detail.country.capital ?: "-"}"))
    statsPanel.add(JLabel("Poblacion: ${detail.country.population}"))
    statsPanel.add(JLabel("Timezone: ${detail.country.timezones.firstOrNull() ?: "-"}"))
    statsPanel.revalidate()
    statsPanel.repaint()

    forecastModel.clear()
    if (detail.weatherForecast.isEmpty()) {
        forecastModel.addElement("Sin pronostico disponible")
    } else {
        detail.weatherForecast.take(7).forEach { day ->
            forecastModel.addElement(
                "${day.date} | ${day.description} | Min ${day.tempMinCelsius}C | Max ${day.tempMaxCelsius}C"
            )
        }
    }
}

