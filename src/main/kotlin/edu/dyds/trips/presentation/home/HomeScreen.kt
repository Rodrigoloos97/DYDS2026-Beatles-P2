package edu.dyds.trips.presentation.home

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
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

fun createAndShowHomeScreen(
    scope: CoroutineScope,
    viewModel: HomeViewModel,
    onOpenDetail: (String) -> Unit,
    onOpenTrips: () -> Unit,
    onClose: () -> Unit
) {
    val frame = JFrame("Asistente de Viajes - Home")
    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
    frame.minimumSize = Dimension(900, 540)
    frame.layout = BorderLayout(8, 8)

    val topPanel = JPanel(FlowLayout(FlowLayout.LEFT))
    val searchField = JTextField(24)
    val searchButton = JButton("Buscar")
    val reloadButton = JButton("Recargar")
    val detailButton = JButton("Ver detalle")
    val tripsButton = JButton("Mis viajes")

    topPanel.add(JLabel("Pais:"))
    topPanel.add(searchField)
    topPanel.add(searchButton)
    topPanel.add(reloadButton)
    topPanel.add(detailButton)
    topPanel.add(tripsButton)

    val listModel = DefaultListModel<String>()
    val countriesList = JList(listModel)
    val statusLabel = JLabel("Listo")

    frame.add(topPanel, BorderLayout.NORTH)
    frame.add(JScrollPane(countriesList), BorderLayout.CENTER)
    frame.add(statusLabel, BorderLayout.SOUTH)

    scope.launch {
        viewModel.uiState.collectLatest { state ->
            SwingUtilities.invokeLater {
                when (state) {
                    HomeUiState.Idle -> statusLabel.text = "Listo"
                    HomeUiState.Loading -> statusLabel.text = "Cargando paises..."
                    is HomeUiState.Error -> {
                        listModel.clear()
                        statusLabel.text = "Error: ${state.message}"
                    }

                    is HomeUiState.Success -> {
                        listModel.clear()
                        state.countries
                            .sortedBy { it.name }
                            .forEach { country ->
                                listModel.addElement("${country.code} - ${country.name} (${country.region})")
                            }
                        statusLabel.text = "${state.countries.size} paises"
                    }
                }
            }
        }
    }

    searchButton.addActionListener {
        viewModel.searchCountries(searchField.text)
    }

    reloadButton.addActionListener {
        searchField.text = ""
        viewModel.loadCountries()
    }

    detailButton.addActionListener {
        val selected = countriesList.selectedValue
        if (selected == null) {
            JOptionPane.showMessageDialog(frame, "Selecciona un pais de la lista")
            return@addActionListener
        }
        val code = selected.substringBefore(" - ").trim()
        if (code.isBlank()) {
            JOptionPane.showMessageDialog(frame, "No se pudo obtener el codigo del pais")
            return@addActionListener
        }
        onOpenDetail(code)
    }

    tripsButton.addActionListener {
        onOpenTrips()
    }

    frame.addWindowListener(object : WindowAdapter() {
        override fun windowClosed(e: WindowEvent?) {
            viewModel.dispose()
            onClose()
        }
    })

    frame.setLocationRelativeTo(null)
    frame.isVisible = true

    viewModel.loadCountries()
}
