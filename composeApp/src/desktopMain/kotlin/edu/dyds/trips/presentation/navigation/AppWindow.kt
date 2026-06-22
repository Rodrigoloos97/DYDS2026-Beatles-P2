package edu.dyds.trips.presentation.navigation

import edu.dyds.trips.domain.entity.CountryDetail
import edu.dyds.trips.domain.entity.Result
import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.presentation.detail.DetailUiState
import edu.dyds.trips.presentation.detail.DetailViewModel
import edu.dyds.trips.presentation.home.HomeUiState
import edu.dyds.trips.presentation.home.HomeViewModel
import edu.dyds.trips.presentation.trips.TripsUiState
import edu.dyds.trips.presentation.trips.TripsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.CardLayout
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
import javax.swing.JTextField
import javax.swing.SwingUtilities

private const val ROUTE_HOME = "home"
private const val ROUTE_DETAIL = "detail"
private const val ROUTE_TRIPS = "trips"

fun createAndShowAppWindow(
    homeViewModel: HomeViewModel,
    tripsViewModel: TripsViewModel,
    createDetailViewModel: () -> DetailViewModel,
    onClose: () -> Unit
) {
    val frame = JFrame("Asistente de Viajes")
    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
    frame.setSize(980, 620)

    val cards = JPanel(CardLayout())

    val homePanel = JPanel(BorderLayout(8, 8))
    val detailPanel = JPanel(BorderLayout(8, 8))
    val tripsPanel = JPanel(BorderLayout(8, 8))

    cards.add(homePanel, ROUTE_HOME)
    cards.add(detailPanel, ROUTE_DETAIL)
    cards.add(tripsPanel, ROUTE_TRIPS)

    frame.contentPane = cards

    val uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    configureHomePanel(
        panel = homePanel,
        scope = uiScope,
        viewModel = homeViewModel,
        onOpenDetail = { code ->
            configureDetailPanel(
                panel = detailPanel,
                scope = uiScope,
                countryCode = code,
                detailViewModel = createDetailViewModel(),
                tripsViewModel = tripsViewModel,
                onBack = { showRoute(cards, ROUTE_HOME) }
            )
            showRoute(cards, ROUTE_DETAIL)
        },
        onOpenTrips = {
            configureTripsPanel(
                panel = tripsPanel,
                scope = uiScope,
                viewModel = tripsViewModel,
                onBack = { showRoute(cards, ROUTE_HOME) }
            )
            showRoute(cards, ROUTE_TRIPS)
        }
    )

    showRoute(cards, ROUTE_HOME)

    frame.addWindowListener(object : java.awt.event.WindowAdapter() {
        override fun windowClosed(e: java.awt.event.WindowEvent?) {
            uiScope.cancel()
            homeViewModel.dispose()
            tripsViewModel.dispose()
            onClose()
        }
    })

    frame.setLocationRelativeTo(null)
    frame.isVisible = true
}

private fun configureHomePanel(
    panel: JPanel,
    scope: CoroutineScope,
    viewModel: HomeViewModel,
    onOpenDetail: (String) -> Unit,
    onOpenTrips: () -> Unit
) {
    panel.removeAll()

    val top = JPanel(FlowLayout(FlowLayout.LEFT))
    val query = JTextField(24)
    val search = JButton("Buscar")
    val reload = JButton("Recargar")
    val detail = JButton("Ver detalle")
    val trips = JButton("Mis viajes")

    top.add(JLabel("Pais:"))
    top.add(query)
    top.add(search)
    top.add(reload)
    top.add(detail)
    top.add(trips)

    val model = DefaultListModel<String>()
    val list = JList(model)
    val status = JLabel("Listo")

    panel.add(top, BorderLayout.NORTH)
    panel.add(JScrollPane(list), BorderLayout.CENTER)
    panel.add(status, BorderLayout.SOUTH)

    search.addActionListener { viewModel.searchCountries(query.text) }
    reload.addActionListener {
        query.text = ""
        viewModel.loadCountries()
    }
    detail.addActionListener {
        val selected = list.selectedValue ?: run {
            JOptionPane.showMessageDialog(panel, "Selecciona un pais")
            return@addActionListener
        }
        onOpenDetail(selected.substringBefore(" - ").trim())
    }
    trips.addActionListener { onOpenTrips() }

    scope.launch {
        viewModel.uiState.collectLatest { state ->
            SwingUtilities.invokeLater {
                when (state) {
                    HomeUiState.Idle -> status.text = "Listo"
                    HomeUiState.Loading -> status.text = "Cargando paises..."
                    is HomeUiState.Error -> {
                        model.clear()
                        status.text = "Error: ${state.message}"
                    }

                    is HomeUiState.Success -> {
                        model.clear()
                        state.countries.sortedBy { it.name }.forEach { c ->
                            model.addElement("${c.code} - ${c.name} (${c.region})")
                        }
                        status.text = "${state.countries.size} paises"
                    }
                }
            }
        }
    }

    viewModel.loadCountries()
}

private fun configureDetailPanel(
    panel: JPanel,
    scope: CoroutineScope,
    countryCode: String,
    detailViewModel: DetailViewModel,
    tripsViewModel: TripsViewModel,
    onBack: () -> Unit
) {
    panel.removeAll()

    val header = JLabel("Detalle de $countryCode")
    val info = JPanel(GridLayout(0, 1))
    val forecastModel = DefaultListModel<String>()
    val forecastList = JList(forecastModel)

    val actions = JPanel(FlowLayout(FlowLayout.LEFT))
    val back = JButton("Volver")
    val save = JButton("Guardar viaje")
    save.isEnabled = false
    actions.add(back)
    actions.add(save)

    panel.add(header, BorderLayout.NORTH)
    panel.add(JScrollPane(forecastList), BorderLayout.CENTER)
    panel.add(info, BorderLayout.EAST)
    panel.add(actions, BorderLayout.SOUTH)

    var currentDetail: CountryDetail? = null

    back.addActionListener {
        detailViewModel.dispose()
        onBack()
    }

    save.addActionListener {
        val detailData = currentDetail ?: return@addActionListener
        val startDate = getValidatedDate(panel, "Fecha inicio (yyyy-MM-dd):") ?: return@addActionListener
        val endDate = getValidatedDate(panel, "Fecha fin (yyyy-MM-dd):") ?: return@addActionListener
        val notes = JOptionPane.showInputDialog(panel, "Notas:") ?: ""

        tripsViewModel.saveTrip(
            Trip(
                countryCode = detailData.country.code,
                countryName = detailData.country.name,
                startDate = startDate,
                endDate = endDate,
                notes = notes
            )
        ) { result ->
            SwingUtilities.invokeLater {
                when (result) {
                    is Result.Success -> JOptionPane.showMessageDialog(panel, "Viaje guardado")
                    is Result.Failure -> JOptionPane.showMessageDialog(panel, "Error: ${result.exception.message}")
                }
            }
        }
    }

    scope.launch {
        detailViewModel.uiState.collectLatest { state ->
            SwingUtilities.invokeLater {
                when (state) {
                    DetailUiState.Idle -> header.text = "Listo"
                    DetailUiState.Loading -> {
                        header.text = "Cargando detalle de $countryCode..."
                        save.isEnabled = false
                    }

                    is DetailUiState.Error -> {
                        header.text = "Error: ${state.message}"
                        forecastModel.clear()
                        info.removeAll()
                        info.revalidate()
                        info.repaint()
                        save.isEnabled = false
                    }

                    is DetailUiState.Success -> {
                        currentDetail = state.detail
                        bindDetail(state.detail, header, info, forecastModel)
                        save.isEnabled = true
                    }
                }
            }
        }
    }

    detailViewModel.loadCountryDetail(countryCode)
}

private fun configureTripsPanel(
    panel: JPanel,
    scope: CoroutineScope,
    viewModel: TripsViewModel,
    onBack: () -> Unit
) {
    panel.removeAll()

    val status = JLabel("Cargando viajes...")
    val model = DefaultListModel<String>()
    val list = JList(model)
    val tripIds = mutableListOf<String>()

    val actions = JPanel(FlowLayout(FlowLayout.LEFT))
    val back = JButton("Volver")
    val refresh = JButton("Recargar")
    val edit = JButton("Editar")
    val delete = JButton("Eliminar")
    actions.add(back)
    actions.add(refresh)
    actions.add(edit)
    actions.add(delete)

    panel.add(status, BorderLayout.NORTH)
    panel.add(JScrollPane(list), BorderLayout.CENTER)
    panel.add(actions, BorderLayout.SOUTH)

    back.addActionListener { onBack() }
    refresh.addActionListener { viewModel.loadTrips() }

    edit.addActionListener {
        val index = list.selectedIndex
        if (index < 0 || index >= tripIds.size) {
            JOptionPane.showMessageDialog(panel, "Selecciona un viaje")
            return@addActionListener
        }

        val selectedLine = list.selectedValue ?: return@addActionListener
        val parts = selectedLine.split("|").map { it.trim() }
        val datesPart = parts.getOrNull(1) ?: ""
        val startOld = datesPart.substringBefore("->").trim().ifBlank { "2026-07-01" }
        val endOld = datesPart.substringAfter("->").trim().ifBlank { "2026-07-07" }
        val notesOld = parts.getOrNull(2) ?: ""
        val countryPart = parts.firstOrNull() ?: ""
        val countryName = countryPart.substringBefore("(").trim().ifBlank { "Pais" }
        val countryCode = countryPart.substringAfter("(").substringBefore(")").trim().ifBlank { "XX" }

        val start = getValidatedDate(panel, "Fecha inicio (yyyy-MM-dd):", startOld) ?: return@addActionListener
        val end = getValidatedDate(panel, "Fecha fin (yyyy-MM-dd):", endOld) ?: return@addActionListener
        val notes = JOptionPane.showInputDialog(panel, "Notas:", notesOld) ?: ""

        viewModel.updateTrip(
            Trip(
                id = tripIds[index],
                countryCode = countryCode,
                countryName = countryName,
                startDate = start,
                endDate = end,
                notes = notes
            )
        ) { result ->
            if (result is Result.Failure) {
                SwingUtilities.invokeLater {
                    status.text = "No se pudo actualizar: ${result.exception.message}"
                }
            }
        }
    }

    delete.addActionListener {
        val index = list.selectedIndex
        if (index >= 0 && index < tripIds.size) {
            viewModel.deleteTrip(tripIds[index]) { result ->
                if (result is Result.Failure) {
                    SwingUtilities.invokeLater {
                        status.text = "No se pudo eliminar: ${result.exception.message}"
                    }
                }
            }
        }
    }

    scope.launch {
        viewModel.uiState.collectLatest { state ->
            SwingUtilities.invokeLater {
                when (state) {
                    TripsUiState.Loading -> status.text = "Cargando viajes..."
                    is TripsUiState.Error -> {
                        status.text = "Error: ${state.message}"
                        model.clear()
                        tripIds.clear()
                    }

                    is TripsUiState.Success -> {
                        model.clear()
                        tripIds.clear()
                        state.trips.forEach { trip ->
                            tripIds.add(trip.id)
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

    viewModel.loadTrips()
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

private fun showRoute(cards: JPanel, route: String) {
    val layout = cards.layout as CardLayout
    layout.show(cards, route)
}

private fun getValidatedDate(parent: java.awt.Component, message: String, default: String = ""): String? {
    val dateRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    var input = if (default.isNotEmpty()) default else null

    while (true) {
        input = JOptionPane.showInputDialog(parent, message, input) ?: return null

        if (dateRegex.matches(input.trim())) {
            return input.trim()
        }

        JOptionPane.showMessageDialog(
            parent,
            "Formato invalido. Usa yyyy-MM-dd (ej: 2026-06-20)",
            "Error de formato",
            JOptionPane.ERROR_MESSAGE
        )
    }
}

