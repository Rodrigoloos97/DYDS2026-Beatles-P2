package edu.dyds.trips.presentation.navigation

import edu.dyds.trips.domain.entity.Country
import edu.dyds.trips.domain.entity.CountryDetail
import edu.dyds.trips.domain.entity.Trip
import edu.dyds.trips.domain.entity.WeatherForecast
import edu.dyds.trips.presentation.detail.DetailUiState
import edu.dyds.trips.presentation.detail.DetailViewModel
import edu.dyds.trips.presentation.home.HomeUiState
import edu.dyds.trips.presentation.home.HomeViewModel
import edu.dyds.trips.presentation.trips.TripOperationUiState
import edu.dyds.trips.presentation.trips.TripsUiState
import edu.dyds.trips.presentation.trips.TripsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder
import javax.swing.plaf.basic.BasicScrollBarUI

private const val ROUTE_HOME   = "home"
private const val ROUTE_DETAIL = "detail"
private const val ROUTE_TRIPS  = "trips"

private data class CountryListItem(val country: Country) {
    override fun toString(): String = country.name
}

private data class TripListItem(val trip: Trip) {
    override fun toString(): String = trip.countryName
}

// ══════════════════════════════════════════════════════════════════════════════
//  T H E M E
// ══════════════════════════════════════════════════════════════════════════════
private object T {
    val BG          = Color(8,  12,  22)
    val BG_CARD     = Color(14, 21,  36)
    val BG_SURFACE  = Color(21, 32,  52)
    val BG_HOVER    = Color(28, 44,  72)
    val BG_NAV      = Color(5,  8,   18)
    val BG_SEL      = Color(4,  44,  64)
    val CYAN        = Color(6,  182, 212)
    val CYAN_LIGHT  = Color(103,232, 249)
    val CYAN_DARK   = Color(8,  145, 178)
    val PURPLE      = Color(139,92,  246)
    val EMERALD     = Color(16, 185, 129)
    val AMBER       = Color(245,158, 11)
    val RED         = Color(239,68,  68)
    val TEXT        = Color(241,245, 249)
    val TEXT_SEC    = Color(148,163, 184)
    val TEXT_MUTED  = Color(71, 85,  105)
    val BORDER      = Color(30, 41,  59)
    val BORDER_L    = Color(45, 60,  85)

    val F_LOGO  = Font("Segoe UI", Font.BOLD,  26)
    val F_TITLE = Font("Segoe UI", Font.BOLD,  20)
    val F_H2    = Font("Segoe UI", Font.BOLD,  15)
    val F_H3    = Font("Segoe UI", Font.BOLD,  13)
    val F_BODY  = Font("Segoe UI", Font.PLAIN, 13)
    val F_SMALL = Font("Segoe UI", Font.PLAIN, 11)
    val F_TINY  = Font("Segoe UI", Font.PLAIN, 10)
    val F_FLAG  = Font("Segoe UI Emoji", Font.PLAIN, 42)

    fun regionColor(r: String): Color = when (r.trim().lowercase()) {
        "europe"    -> Color(99,  102, 241)
        "asia"      -> Color(239, 68,  68)
        "africa"    -> Color(245, 158, 11)
        "americas"  -> Color(16,  185, 129)
        "oceania"   -> Color(6,   182, 212)
        else        -> Color(148, 163, 184)
    }

    fun weatherIcon(code: Int): String = when (code) {
        0           -> "\u2600"
        1, 2        -> "\u26C5"
        3           -> "\u2601"
        45, 48      -> "\u2601"
        51,53,55,
        61,63,65,
        80,81,82    -> "\uD83C\uDF27"
        71,73,75,
        77,85,86    -> "\u2744"
        95,96,99    -> "\u26C8"
        else        -> "?"
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  C U S T O M   C O M P O N E N T S
// ══════════════════════════════════════════════════════════════════════════════

private open class RPanel(
    private val bg: Color = T.BG_CARD,
    private val radius: Int = 14,
    private val borderClr: Color? = null
) : JPanel() {
    init { isOpaque = false }
    override fun paintComponent(g: Graphics) {
        val g2 = (g as Graphics2D).also {
            it.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        }
        if (borderClr != null) {
            g2.color = borderClr
            g2.fillRoundRect(0, 0, width, height, radius + 2, radius + 2)
            g2.color = bg
            g2.fillRoundRect(1, 1, width - 2, height - 2, radius, radius)
        } else {
            g2.color = bg
            g2.fillRoundRect(0, 0, width, height, radius, radius)
        }
        super.paintComponent(g)
    }
}

private class GPanel(
    private val c1: Color,
    private val c2: Color,
    private val radius: Int = 0,
    private val horizontal: Boolean = true
) : JPanel() {
    init { isOpaque = false }
    override fun paintComponent(g: Graphics) {
        val g2 = (g as Graphics2D).also {
            it.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        }
        g2.paint = if (horizontal)
            GradientPaint(0f, 0f, c1, width.toFloat(), 0f, c2)
        else
            GradientPaint(0f, 0f, c1, 0f, height.toFloat(), c2)
        if (radius > 0) g2.fillRoundRect(0, 0, width, height, radius, radius)
        else            g2.fillRect(0, 0, width, height)
        super.paintComponent(g)
    }
}

private class WBtn(
    text: String,
    style: Style = Style.GHOST
) : JButton(text) {
    enum class Style { PRIMARY, SECONDARY, DANGER, GHOST }

    private var hovered = false
    private val bgNorm  = when (style) {
        Style.PRIMARY   -> T.CYAN;   Style.SECONDARY -> T.PURPLE
        Style.DANGER    -> T.RED;    Style.GHOST     -> T.BG_SURFACE
    }
    private val bgHov   = when (style) {
        Style.PRIMARY   -> T.CYAN_LIGHT
        Style.SECONDARY -> Color(167, 139, 250)
        Style.DANGER    -> Color(252, 129, 129)
        Style.GHOST     -> T.BG_HOVER
    }
    private val fgNorm  = when (style) {
        Style.PRIMARY   -> Color(7, 25, 40)
        else            -> T.TEXT_SEC
    }

    init {
        isOpaque = false; isFocusPainted = false; isBorderPainted = false; isContentAreaFilled = false
        foreground = fgNorm; font = T.F_H3; cursor = Cursor(Cursor.HAND_CURSOR)
        border = EmptyBorder(9, 18, 9, 18)
        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) { hovered = true;  repaint() }
            override fun mouseExited (e: MouseEvent) { hovered = false; repaint() }
        })
    }

    override fun paintComponent(g: Graphics) {
        val g2 = (g as Graphics2D).also {
            it.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            it.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
        }
        g2.color = when { !isEnabled -> T.BG_SURFACE; hovered -> bgHov; else -> bgNorm }
        g2.fillRoundRect(0, 0, width, height, 10, 10)
        g2.color = if (isEnabled) foreground else T.TEXT_MUTED
        g2.font  = font
        val fm = g2.fontMetrics
        g2.drawString(text, (width - fm.stringWidth(text)) / 2, (height + fm.ascent - fm.descent) / 2)
    }
}

private class NavBtn(text: String) : JButton(text) {
    private var hovered = false
    var active = false
        set(v) { field = v; repaint() }

    init {
        isOpaque = false; isFocusPainted = false; isBorderPainted = false; isContentAreaFilled = false
        foreground = T.TEXT_SEC; font = T.F_H3; cursor = Cursor(Cursor.HAND_CURSOR)
        border = EmptyBorder(10, 20, 10, 20)
        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) { hovered = true;  repaint() }
            override fun mouseExited (e: MouseEvent) { hovered = false; repaint() }
        })
    }

    override fun paintComponent(g: Graphics) {
        val g2 = (g as Graphics2D).also {
            it.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            it.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
        }
        when {
            active  -> {
                g2.color = T.BG_SURFACE
                g2.fillRoundRect(0, 0, width, height, 10, 10)
                g2.color = T.CYAN
                g2.fillRect(0, height - 3, width, 3)
            }
            hovered -> {
                g2.color = Color(T.BG_SURFACE.red, T.BG_SURFACE.green, T.BG_SURFACE.blue, 100)
                g2.fillRoundRect(0, 0, width, height, 10, 10)
            }
        }
        g2.color = if (active) T.CYAN else if (hovered) T.TEXT else T.TEXT_SEC
        g2.font  = font
        val fm = g2.fontMetrics
        g2.drawString(text, (width - fm.stringWidth(text)) / 2, (height + fm.ascent - fm.descent) / 2)
    }
}

private class WField(cols: Int = 20, private val placeholder: String = "") : JTextField(cols) {
    init {
        isOpaque = false; background = T.BG_SURFACE; foreground = T.TEXT
        caretColor = T.CYAN; font = T.F_BODY; border = EmptyBorder(10, 14, 10, 14)
        selectionColor = T.CYAN_DARK; selectedTextColor = T.TEXT
    }
    override fun paintComponent(g: Graphics) {
        val g2 = (g as Graphics2D).also {
            it.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            it.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
        }
        g2.color = T.BG_SURFACE
        g2.fillRoundRect(0, 0, width, height, 10, 10)
        g2.color = if (hasFocus()) T.CYAN else T.BORDER_L
        g2.drawRoundRect(0, 0, width - 1, height - 1, 10, 10)
        super.paintComponent(g)
        if (text.isEmpty() && !hasFocus() && placeholder.isNotEmpty()) {
            g2.color = T.TEXT_MUTED; g2.font = font
            val fm = g2.fontMetrics
            g2.drawString(placeholder, 14, (height + fm.ascent - fm.descent) / 2)
        }
    }
}

private class CountryRenderer : ListCellRenderer<CountryListItem> {
    override fun getListCellRendererComponent(
        list: JList<out CountryListItem>, value: CountryListItem?, index: Int,
        isSelected: Boolean, cellHasFocus: Boolean
    ): Component {
        val country = value?.country
        val code = country?.code ?: ""
        val name = country?.name ?: ""
        val region = country?.region ?: ""

        val panel = object : JPanel(BorderLayout(14, 0)) {
            override fun paintComponent(g: Graphics) {
                val g2 = (g as Graphics2D).also {
                    it.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                }
                g2.color = when { isSelected -> T.BG_SEL; index % 2 == 0 -> T.BG_CARD; else -> Color(11, 17, 30) }
                g2.fillRect(0, 0, width, height)
                val rc = T.regionColor(region)
                g2.color = if (isSelected) T.CYAN else Color(rc.red, rc.green, rc.blue, 180)
                g2.fillRect(0, 0, 4, height)
            }
        }
        panel.isOpaque = false
        panel.border = EmptyBorder(10, 18, 10, 18)
        panel.preferredSize = Dimension(0, 58)

        val dot = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                val g2 = (g as Graphics2D).also { it.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON) }
                val rc = T.regionColor(region)
                g2.color = Color(rc.red, rc.green, rc.blue, 60); g2.fillOval(1, (height - 16) / 2, 16, 16)
                g2.color = rc; g2.fillOval(4, (height - 10) / 2, 10, 10)
            }
            override fun getPreferredSize() = Dimension(24, 40)
        }
        dot.isOpaque = false

        val center = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS); isOpaque = false }
        val nameL = lbl(name.ifBlank { code }, T.F_H3, if (isSelected) T.CYAN_LIGHT else T.TEXT)
        val codeL = lbl(code, T.F_TINY, T.TEXT_MUTED)
        center.add(nameL); center.add(Box.createVerticalStrut(2)); center.add(codeL)

        val badge = object : JLabel("  $region  ") {
            override fun paintComponent(g: Graphics) {
                val g2 = (g as Graphics2D).also { it.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON) }
                val rc = T.regionColor(region)
                g2.color = Color(rc.red, rc.green, rc.blue, 28); g2.fillRoundRect(0, 0, width, height, 8, 8)
                g2.color = Color(rc.red, rc.green, rc.blue, 100); g2.drawRoundRect(0, 0, width - 1, height - 1, 8, 8)
                super.paintComponent(g)
            }
        }
        badge.font = T.F_TINY; badge.foreground = T.regionColor(region); badge.isOpaque = false
        badge.verticalAlignment = SwingConstants.CENTER

        panel.add(dot, BorderLayout.WEST); panel.add(center, BorderLayout.CENTER); panel.add(badge, BorderLayout.EAST)
        return panel
    }
}

private class TripRenderer : ListCellRenderer<TripListItem> {
    override fun getListCellRendererComponent(
        list: JList<out TripListItem>, value: TripListItem?, index: Int,
        isSelected: Boolean, cellHasFocus: Boolean
    ): Component {
        val trip = value?.trip
        val cName = trip?.countryName ?: ""
        val cCode = trip?.countryCode ?: ""
        val start = trip?.startDate ?: ""
        val end = trip?.endDate ?: ""
        val notes = trip?.notes ?: ""

        val panel = object : JPanel(BorderLayout(12, 0)) {
            override fun paintComponent(g: Graphics) {
                val g2 = (g as Graphics2D).also { it.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON) }
                g2.color = when { isSelected -> T.BG_SEL; index % 2 == 0 -> T.BG_CARD; else -> Color(11, 17, 30) }
                g2.fillRect(0, 0, width, height)
                g2.color = if (isSelected) T.CYAN else T.PURPLE
                g2.fillRect(0, 0, 5, height)
            }
        }
        panel.isOpaque = false
        panel.border = EmptyBorder(14, 22, 14, 22)
        panel.preferredSize = Dimension(0, 80)

        val planeL = lbl("\u2708", Font("Segoe UI", Font.PLAIN, 28), if (isSelected) T.CYAN else T.PURPLE)
        planeL.border = EmptyBorder(0, 0, 0, 8)
        planeL.verticalAlignment = SwingConstants.CENTER

        val content = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS); isOpaque = false }
        content.add(lbl("$cName  ($cCode)", T.F_H2, if (isSelected) T.CYAN_LIGHT else T.TEXT))
        content.add(Box.createVerticalStrut(5))
        content.add(lbl("\uD83D\uDCC5  $start   \u2192   $end", T.F_BODY, T.TEXT_SEC))
        if (notes.isNotBlank()) {
            content.add(Box.createVerticalStrut(3))
            content.add(lbl("\uD83D\uDCDD  $notes", T.F_SMALL, T.TEXT_MUTED))
        }

        panel.add(planeL, BorderLayout.WEST); panel.add(content, BorderLayout.CENTER)
        return panel
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  M A I N   W I N D O W
// ══════════════════════════════════════════════════════════════════════════════

fun createAndShowAppWindow(
    homeViewModel: HomeViewModel,
    tripsViewModel: TripsViewModel,
    createDetailViewModel: () -> DetailViewModel,
    onClose: () -> Unit
) {
    try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) } catch (_: Exception) {}
    UIManager.put("OptionPane.background",        T.BG_CARD)
    UIManager.put("OptionPane.messageForeground", T.TEXT)
    UIManager.put("Panel.background",             T.BG_CARD)
    // Keep global LAF defaults for regular buttons/inputs to avoid unreadable text in system dialogs.

    val frame = JFrame("WorldGlance")
    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
    frame.setSize(1120, 720)
    frame.minimumSize = Dimension(900, 600)

    val root = object : JPanel(BorderLayout()) {
        override fun paintComponent(g: Graphics) { g.color = T.BG; g.fillRect(0, 0, width, height) }
    }
    root.isOpaque = true

    val cards = JPanel(CardLayout()).also { it.isOpaque = false }
    val homePanel   = JPanel(BorderLayout()).also { it.isOpaque = false }
    val detailPanel = JPanel(BorderLayout()).also { it.isOpaque = false }
    val tripsPanel  = JPanel(BorderLayout()).also { it.isOpaque = false }
    cards.add(homePanel, ROUTE_HOME); cards.add(detailPanel, ROUTE_DETAIL); cards.add(tripsPanel, ROUTE_TRIPS)

    val uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    var activeDetailViewModel: DetailViewModel? = null

    val navHome  = NavBtn("\uD83D\uDDFA  Explorar")
    val navTrips = NavBtn("\u2708  Mis Viajes")

    fun navigate(route: String) {
        navHome.active  = (route == ROUTE_HOME)
        navTrips.active = (route == ROUTE_TRIPS)
        showRoute(cards, route)
    }

    navHome.addActionListener  { navigate(ROUTE_HOME) }
    navTrips.addActionListener {
        configureTripsPanel(tripsPanel, uiScope, tripsViewModel)
        navigate(ROUTE_TRIPS)
    }

    configureHomePanel(
        panel     = homePanel,
        scope     = uiScope,
        viewModel = homeViewModel,
        onOpenDetail = { code ->
            activeDetailViewModel?.dispose()
            val detailViewModel = createDetailViewModel()
            activeDetailViewModel = detailViewModel
            configureDetailPanel(
                panel = detailPanel,
                scope = uiScope,
                countryCode = code,
                detailViewModel = detailViewModel,
                onSaveTrip = { trip -> tripsViewModel.saveTrip(trip) },
                saveOperationState = tripsViewModel.operationState,
                clearSaveOperationState = { tripsViewModel.clearOperationState() },
                onBack = { navigate(ROUTE_HOME) }
            )
            navigate(ROUTE_DETAIL)
        }
    )

    navigate(ROUTE_HOME)
    root.add(buildNavBar(navHome, navTrips), BorderLayout.NORTH)
    root.add(cards, BorderLayout.CENTER)
    frame.contentPane = root

    frame.addWindowListener(object : WindowAdapter() {
        override fun windowClosed(e: WindowEvent?) {
            uiScope.cancel()
            activeDetailViewModel?.dispose()
            homeViewModel.dispose()
            tripsViewModel.dispose()
            onClose()
        }
    })
    frame.setLocationRelativeTo(null)
    frame.isVisible = true
}

// ══════════════════════════════════════════════════════════════════════════════
//  N A V   B A R
// ══════════════════════════════════════════════════════════════════════════════

private fun buildNavBar(homeBtn: NavBtn, tripsBtn: NavBtn): JPanel {
    val bar = GPanel(T.BG_NAV, Color(10, 18, 34), radius = 0)
    bar.layout = BorderLayout()
    bar.preferredSize = Dimension(0, 66)
    bar.border = MatteBorder(0, 0, 1, 0, T.BORDER)

    val logoWrap = JPanel(FlowLayout(FlowLayout.LEFT, 20, 0)).also { it.isOpaque = false }
    val globeL = lbl("\uD83C\uDF0D", Font("Segoe UI Emoji", Font.PLAIN, 30), T.CYAN)

    val titleBox = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS); isOpaque = false }
    titleBox.add(lbl("WorldGlance", T.F_LOGO, T.TEXT))
    titleBox.add(lbl("Your world at a glance", T.F_TINY, T.CYAN))

    logoWrap.add(globeL); logoWrap.add(titleBox)

    val navWrap = JPanel(FlowLayout(FlowLayout.RIGHT, 6, 0)).also {
        it.isOpaque = false; it.border = EmptyBorder(0, 0, 0, 20)
    }
    navWrap.add(homeBtn); navWrap.add(tripsBtn)

    bar.add(logoWrap, BorderLayout.WEST); bar.add(navWrap, BorderLayout.EAST)
    return bar
}

// ══════════════════════════════════════════════════════════════════════════════
//  H O M E   P A N E L
// ══════════════════════════════════════════════════════════════════════════════

private fun configureHomePanel(
    panel: JPanel, scope: CoroutineScope, viewModel: HomeViewModel,
    onOpenDetail: (String) -> Unit
) {
    panel.removeAll()

    val searchRow = JPanel(BorderLayout(10, 0)).also {
        it.isOpaque = false; it.border = EmptyBorder(18, 24, 14, 24)
    }
    val searchField = WField(32, "\uD83D\uDD0D  Buscar país por nombre o código...")
    val searchBtn   = WBtn("Buscar", WBtn.Style.PRIMARY)
    val reloadBtn   = WBtn("\u21BB  Recargar", WBtn.Style.GHOST)
    val detailBtn   = WBtn("Ver Detalle  \u2192", WBtn.Style.SECONDARY)

    val btns = JPanel(FlowLayout(FlowLayout.RIGHT, 8, 0)).also { it.isOpaque = false }
    btns.add(reloadBtn); btns.add(searchBtn); btns.add(detailBtn)
    searchRow.add(searchField, BorderLayout.CENTER); searchRow.add(btns, BorderLayout.EAST)

    val sectionHeader = JPanel(BorderLayout()).also { it.isOpaque = false; it.border = EmptyBorder(0, 24, 8, 24) }
    val sectionL = lbl("Países del Mundo", T.F_H2, T.TEXT_SEC)
    sectionHeader.add(sectionL, BorderLayout.WEST)

    val listModel   = DefaultListModel<CountryListItem>()
    val countryList = JList(listModel)
    countryList.cellRenderer = CountryRenderer()
    countryList.background = T.BG; countryList.selectionBackground = T.BG_SEL
    countryList.selectionForeground = T.TEXT; countryList.fixedCellHeight = -1; countryList.border = null
    countryList.addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            if (e.clickCount == 2) countryList.selectedValue?.let { onOpenDetail(it.country.code) }
        }
    })

    val statusBar = JPanel(BorderLayout()).also {
        it.isOpaque = false
        it.border = CompoundBorder(MatteBorder(1, 0, 0, 0, T.BORDER), EmptyBorder(8, 24, 8, 24))
    }
    val statusL = lbl("Cargando países...", T.F_SMALL, T.TEXT_MUTED)
    val hintL   = lbl("Doble clic para ver detalle", T.F_TINY, T.TEXT_MUTED)
    statusBar.add(statusL, BorderLayout.WEST); statusBar.add(hintL, BorderLayout.EAST)

    val topArea = JPanel(BorderLayout()).also { it.isOpaque = false }
    topArea.add(searchRow, BorderLayout.NORTH); topArea.add(sectionHeader, BorderLayout.SOUTH)

    panel.add(topArea, BorderLayout.NORTH)
    panel.add(styledScroll(countryList), BorderLayout.CENTER)
    panel.add(statusBar, BorderLayout.SOUTH)

    searchBtn.addActionListener    { viewModel.searchCountries(searchField.text) }
    searchField.addActionListener  { viewModel.searchCountries(searchField.text) }
    reloadBtn.addActionListener    { searchField.text = ""; viewModel.loadCountries() }
    detailBtn.addActionListener {
        val sel = countryList.selectedValue
        if (sel == null) { showMsg(panel, "Selecciona un país de la lista."); return@addActionListener }
        onOpenDetail(sel.country.code)
    }

    scope.launch {
        viewModel.uiState.collectLatest { state ->
            SwingUtilities.invokeLater {
                when (state) {
                    HomeUiState.Idle    -> { statusL.text = "Listo"; statusL.foreground = T.TEXT_MUTED }
                    HomeUiState.Loading -> { statusL.text = "\u23F3  Cargando países..."; statusL.foreground = T.TEXT_MUTED }
                    is HomeUiState.Error -> {
                        listModel.clear()
                        statusL.text = "\u26A0  Error: ${state.message}"; statusL.foreground = T.RED
                    }
                    is HomeUiState.Success -> {
                        listModel.clear()
                        state.countries.sortedBy { it.name }.forEach { c ->
                            listModel.addElement(CountryListItem(c))
                        }
                        statusL.text = "\u2713  ${state.countries.size} países cargados"
                        statusL.foreground = T.EMERALD
                    }
                }
            }
        }
    }
    viewModel.loadCountries()
}

// ══════════════════════════════════════════════════════════════════════════════
//  D E T A I L   P A N E L
// ══════════════════════════════════════════════════════════════════════════════

private fun configureDetailPanel(
    panel: JPanel, scope: CoroutineScope, countryCode: String,
    detailViewModel: DetailViewModel,
    onSaveTrip: (Trip) -> Unit,
    saveOperationState: StateFlow<TripOperationUiState>,
    clearSaveOperationState: () -> Unit,
    onBack: () -> Unit
) {
    panel.removeAll()

    // Hero
    val hero = GPanel(Color(8, 26, 48), Color(6, 18, 36), radius = 0, horizontal = false)
    hero.layout = BorderLayout(20, 8); hero.preferredSize = Dimension(0, 110)
    hero.border = EmptyBorder(18, 28, 18, 28)

    val flagL = lbl("\uD83C\uDF10", T.F_FLAG, T.TEXT)
    flagL.border = EmptyBorder(0, 0, 0, 12); flagL.verticalAlignment = SwingConstants.CENTER

    val heroTxt = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS); isOpaque = false }
    val countryNameL = lbl("Cargando $countryCode...", T.F_TITLE, T.TEXT)
    val countrySubL  = lbl(" ", T.F_SMALL, T.TEXT_SEC)
    heroTxt.add(countryNameL); heroTxt.add(Box.createVerticalStrut(4)); heroTxt.add(countrySubL)
    hero.add(flagL, BorderLayout.WEST); hero.add(heroTxt, BorderLayout.CENTER)

    // Split
    val split = JSplitPane(JSplitPane.HORIZONTAL_SPLIT).also {
        it.isOpaque = false; it.background = T.BG; it.dividerSize = 1
        it.isContinuousLayout = true; it.resizeWeight = 0.38; it.border = null
    }

    // Left: info
    val infoOuter = JPanel(BorderLayout()).also { it.isOpaque = false; it.background = T.BG; it.border = EmptyBorder(20, 24, 20, 12) }
    val infoTitleL = lbl("\uD83D\uDCCB  Información del País", T.F_H2, T.CYAN)
    infoTitleL.border = EmptyBorder(0, 0, 14, 0)
    val infoRows = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS); isOpaque = false }
    infoOuter.add(infoTitleL, BorderLayout.NORTH)
    infoOuter.add(styledScroll(infoRows).also { it.border = null }, BorderLayout.CENTER)

    // Right: forecast
    val fxOuter = JPanel(BorderLayout()).also { it.isOpaque = false; it.background = T.BG; it.border = EmptyBorder(20, 12, 20, 24) }
    val fxTitleL = lbl("\uD83C\uDF24  Pronóstico del Tiempo \u2014 7 días", T.F_H2, T.CYAN)
    fxTitleL.border = EmptyBorder(0, 0, 14, 0)
    val fxCards = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS); isOpaque = false }
    fxOuter.add(fxTitleL, BorderLayout.NORTH)
    fxOuter.add(styledScroll(fxCards).also { it.border = null }, BorderLayout.CENTER)

    split.leftComponent = infoOuter; split.rightComponent = fxOuter

    // Bottom
    val actBar = JPanel(FlowLayout(FlowLayout.RIGHT, 12, 12)).also {
        it.isOpaque = false; it.border = MatteBorder(1, 0, 0, 0, T.BORDER)
    }
    val backBtn = WBtn("\u2190  Volver", WBtn.Style.GHOST)
    val saveBtn = WBtn("\u2708  Guardar Viaje", WBtn.Style.PRIMARY)
    saveBtn.isEnabled = false
    actBar.add(backBtn); actBar.add(saveBtn)

    panel.add(hero, BorderLayout.NORTH); panel.add(split, BorderLayout.CENTER); panel.add(actBar, BorderLayout.SOUTH)

    var currentDetail: CountryDetail? = null

    backBtn.addActionListener { detailViewModel.dispose(); onBack() }
    saveBtn.addActionListener {
        val d = currentDetail ?: return@addActionListener
        val start = getValidatedDate(panel, "Fecha de inicio (yyyy-MM-dd):") ?: return@addActionListener
        val end   = getValidatedDate(panel, "Fecha de fin (yyyy-MM-dd):")   ?: return@addActionListener
        val notes = showTextInputDialog(panel, "Notas del viaje", "Podés dejar notas para recordar este plan:") ?: ""
        onSaveTrip(
            Trip(countryCode = d.country.code, countryName = d.country.name, startDate = start, endDate = end, notes = notes)
        )
    }

    scope.launch {
        saveOperationState.collectLatest { operationState ->
            SwingUtilities.invokeLater {
                when (operationState) {
                    TripOperationUiState.Idle,
                    TripOperationUiState.InFlight -> Unit
                    is TripOperationUiState.Success -> {
                        showMsg(panel, "\u2713  ${operationState.message}")
                        clearSaveOperationState()
                    }
                    is TripOperationUiState.Error -> {
                        showMsg(panel, "\u26A0  Error: ${operationState.message}")
                        clearSaveOperationState()
                    }
                }
            }
        }
    }

    scope.launch {
        detailViewModel.uiState.collectLatest { state ->
            SwingUtilities.invokeLater {
                when (state) {
                    DetailUiState.Idle    -> {}
                    DetailUiState.Loading -> {
                        countryNameL.text = "Cargando $countryCode..."; countryNameL.foreground = T.TEXT
                        countrySubL.text = ""; saveBtn.isEnabled = false
                    }
                    is DetailUiState.Error -> {
                        countryNameL.text = "\u26A0  Error: ${state.message}"; countryNameL.foreground = T.RED
                        saveBtn.isEnabled = false
                    }
                    is DetailUiState.Success -> {
                        currentDetail = state.detail
                        bindDetail(state.detail, countryNameL, countrySubL, flagL, infoRows, fxCards)
                        saveBtn.isEnabled = true
                        panel.revalidate(); panel.repaint()
                    }
                }
            }
        }
    }
    detailViewModel.loadCountryDetail(countryCode)
}

private fun bindDetail(
    detail: CountryDetail,
    nameL: JLabel, subL: JLabel, flagL: JLabel,
    infoRows: JPanel, fxCards: JPanel
) {
    val c = detail.country
    nameL.text = c.name; nameL.foreground = T.TEXT
    subL.text  = c.officialName.let { if (it != c.name) "$it  ·  ${c.region}" else c.region }
    flagL.text = countryCodeToFlag(c.code)

    infoRows.removeAll()
    fun row(icon: String, label: String, value: String) {
        val card = RPanel(T.BG_SURFACE, 10, T.BORDER)
        card.layout = BorderLayout(8, 0); card.border = EmptyBorder(10, 14, 10, 14)
        card.maximumSize = Dimension(Int.MAX_VALUE, 50); card.alignmentX = Component.LEFT_ALIGNMENT
        val valL = lbl(value, T.F_H3, T.TEXT); valL.horizontalAlignment = SwingConstants.RIGHT
        card.add(lbl("$icon  $label", T.F_SMALL, T.TEXT_MUTED), BorderLayout.WEST)
        card.add(valL, BorderLayout.EAST)
        infoRows.add(card); infoRows.add(Box.createVerticalStrut(7))
    }
    row("\uD83C\uDFDB", "Capital",     c.capital ?: "—")
    row("\uD83C\uDF0D", "Región",      c.region)
    row("\uD83D\uDCCD", "Sub-región",  c.subregion ?: "—")
    row("\uD83D\uDC65", "Población",   "%,d".format(c.population))
    row("\uD83D\uDD50", "Zona horaria",c.timezones.firstOrNull() ?: "—")
    row("\uD83D\uDCAC", "Idiomas",     c.languages.values.take(3).joinToString(", ").ifBlank { "—" })
    row("\uD83D\uDCB1", "Monedas",     c.currencies.values.take(2).joinToString(", ") { "${it.name} (${it.symbol})" }.ifBlank { "—" })
    infoRows.revalidate(); infoRows.repaint()

    fxCards.removeAll()
    if (detail.weatherForecast.isEmpty()) {
        val noData = lbl("Sin pronóstico disponible", T.F_BODY, T.TEXT_MUTED)
        noData.border = EmptyBorder(20, 0, 0, 0); noData.alignmentX = Component.LEFT_ALIGNMENT
        fxCards.add(noData)
    } else {
        detail.weatherForecast.take(7).forEach { day ->
            fxCards.add(buildWeatherCard(day)); fxCards.add(Box.createVerticalStrut(8))
        }
    }
    fxCards.revalidate(); fxCards.repaint()
}

private fun buildWeatherCard(day: WeatherForecast): JPanel {
    val card = RPanel(T.BG_SURFACE, 12, T.BORDER)
    card.layout = BorderLayout(14, 0); card.border = EmptyBorder(12, 16, 12, 16)
    card.maximumSize = Dimension(Int.MAX_VALUE, 74); card.alignmentX = Component.LEFT_ALIGNMENT

    val left = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS); isOpaque = false }
    val iconL = lbl(T.weatherIcon(day.weatherCode), Font("Segoe UI", Font.PLAIN, 26), T.TEXT)
    iconL.alignmentX = Component.CENTER_ALIGNMENT
    val dateL = lbl(day.date.substring(5), T.F_TINY, T.TEXT_MUTED)
    dateL.alignmentX = Component.CENTER_ALIGNMENT
    left.add(iconL); left.add(Box.createVerticalStrut(2)); left.add(dateL)

    val descL = lbl(day.description, T.F_BODY, T.TEXT_SEC)

    val right = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS); isOpaque = false }
    val tempL = lbl("${day.tempMinCelsius.toInt()}° / ${day.tempMaxCelsius.toInt()}°C", T.F_H2, T.TEXT)
    tempL.alignmentX = Component.RIGHT_ALIGNMENT
    val windL = lbl("\uD83D\uDCA8 ${day.windSpeedKmh.toInt()} km/h   \uD83C\uDF27 ${day.precipitationMm} mm", T.F_TINY, T.TEXT_MUTED)
    windL.alignmentX = Component.RIGHT_ALIGNMENT
    right.add(tempL); right.add(Box.createVerticalStrut(4)); right.add(windL)

    card.add(left, BorderLayout.WEST); card.add(descL, BorderLayout.CENTER); card.add(right, BorderLayout.EAST)
    return card
}

// ══════════════════════════════════════════════════════════════════════════════
//  T R I P S   P A N E L
// ══════════════════════════════════════════════════════════════════════════════

private fun configureTripsPanel(
    panel: JPanel, scope: CoroutineScope, viewModel: TripsViewModel
) {
    panel.removeAll()

    val hero = GPanel(Color(8, 20, 40), Color(16, 12, 36), radius = 0, horizontal = false)
    hero.layout = BorderLayout(); hero.preferredSize = Dimension(0, 80); hero.border = EmptyBorder(0, 28, 0, 28)

    val countBadge = object : JLabel("") {
        override fun paintComponent(g: Graphics) {
            if (text.isNotBlank()) {
                val g2 = (g as Graphics2D).also { it.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON) }
                g2.color = T.CYAN; g2.fillRoundRect(0, 0, width, height, 14, 14)
            }
            super.paintComponent(g)
        }
    }
    countBadge.font = T.F_H3; countBadge.foreground = Color(7, 25, 40)
    countBadge.border = EmptyBorder(4, 10, 4, 10); countBadge.isOpaque = false

    val heroLeft = JPanel(FlowLayout(FlowLayout.LEFT, 12, 0)).also { it.isOpaque = false }
    heroLeft.add(lbl("\u2708  Mis Viajes", T.F_TITLE, T.TEXT)); heroLeft.add(countBadge)
    hero.add(heroLeft, BorderLayout.WEST)

    val listModel = DefaultListModel<TripListItem>()
    val tripList  = JList(listModel)
    tripList.cellRenderer = TripRenderer(); tripList.background = T.BG
    tripList.selectionBackground = T.BG_SEL; tripList.selectionForeground = T.TEXT
    tripList.fixedCellHeight = -1; tripList.border = null

    val statusL   = lbl("Cargando viajes...", T.F_SMALL, T.TEXT_MUTED)
    val editBtn   = WBtn("\u270F  Editar", WBtn.Style.GHOST)
    val deleteBtn = WBtn("\uD83D\uDDD1  Eliminar", WBtn.Style.DANGER)

    val bottomBar = JPanel(BorderLayout()).also { it.isOpaque = false; it.border = MatteBorder(1, 0, 0, 0, T.BORDER) }
    val statusWrap = JPanel(FlowLayout(FlowLayout.LEFT, 20, 10)).also { it.isOpaque = false; it.add(statusL) }
    val btnWrap    = JPanel(FlowLayout(FlowLayout.RIGHT, 10, 10)).also { it.isOpaque = false; it.add(editBtn); it.add(deleteBtn) }
    bottomBar.add(statusWrap, BorderLayout.WEST); bottomBar.add(btnWrap, BorderLayout.EAST)

    panel.add(hero, BorderLayout.NORTH); panel.add(styledScroll(tripList), BorderLayout.CENTER); panel.add(bottomBar, BorderLayout.SOUTH)

    editBtn.addActionListener {
        val selectedTrip = tripList.selectedValue?.trip
        if (selectedTrip == null) {
            showMsg(panel, "Selecciona un viaje para editar.")
            return@addActionListener
        }
        val startOld = selectedTrip.startDate.ifBlank { "2026-07-01" }
        val endOld = selectedTrip.endDate.ifBlank { "2026-07-10" }
        val notesOld = selectedTrip.notes
        val start = getValidatedDate(panel, "Fecha de inicio (yyyy-MM-dd):", startOld) ?: return@addActionListener
        val end   = getValidatedDate(panel, "Fecha de fin (yyyy-MM-dd):",   endOld)   ?: return@addActionListener
        val notes = showTextInputDialog(panel, "Editar notas", "Actualizá las notas del viaje:", notesOld) ?: ""
        viewModel.updateTrip(
            Trip(
                id = selectedTrip.id,
                countryCode = selectedTrip.countryCode,
                countryName = selectedTrip.countryName,
                startDate = start,
                endDate = end,
                notes = notes,
                createdAt = selectedTrip.createdAt
            )
        )
    }

    deleteBtn.addActionListener {
        val selectedTrip = tripList.selectedValue?.trip
        if (selectedTrip == null) {
            showMsg(panel, "Selecciona un viaje para eliminar.")
            return@addActionListener
        }
        if (showConfirmDeleteDialog(panel)) {
            viewModel.deleteTrip(selectedTrip.id)
        }
    }

    scope.launch {
        viewModel.operationState.collectLatest { operationState ->
            SwingUtilities.invokeLater {
                when (operationState) {
                    TripOperationUiState.Idle -> {
                        editBtn.isEnabled = true
                        deleteBtn.isEnabled = true
                    }
                    TripOperationUiState.InFlight -> {
                        editBtn.isEnabled = false
                        deleteBtn.isEnabled = false
                    }
                    is TripOperationUiState.Success -> {
                        editBtn.isEnabled = true
                        deleteBtn.isEnabled = true
                        viewModel.clearOperationState()
                    }
                    is TripOperationUiState.Error -> {
                        editBtn.isEnabled = true
                        deleteBtn.isEnabled = true
                        statusL.text = "\u26A0  Error: ${operationState.message}"
                        statusL.foreground = T.RED
                        viewModel.clearOperationState()
                    }
                }
            }
        }
    }

    scope.launch {
        viewModel.uiState.collectLatest { state ->
            SwingUtilities.invokeLater {
                when (state) {
                    TripsUiState.Loading -> { statusL.text = "\u23F3  Cargando viajes..."; statusL.foreground = T.TEXT_MUTED; countBadge.text = "" }
                    is TripsUiState.Error -> {
                        statusL.text = "\u26A0  Error: ${state.message}"; statusL.foreground = T.RED
                        listModel.clear(); countBadge.text = ""
                    }
                    is TripsUiState.Success -> {
                        listModel.clear()
                        state.trips.forEach { trip ->
                            listModel.addElement(TripListItem(trip))
                        }
                        val n = state.trips.size
                        statusL.text = if (n == 0) "No hay viajes registrados aún." else "\u2713  $n viaje${if (n != 1) "s" else ""} guardado${if (n != 1) "s" else ""}"
                        statusL.foreground = if (n == 0) T.TEXT_MUTED else T.EMERALD
                        countBadge.text = if (n == 0) "" else "$n"; countBadge.revalidate(); countBadge.repaint()
                    }
                }
            }
        }
    }
    viewModel.loadTrips()
}

// ══════════════════════════════════════════════════════════════════════════════
//  H E L P E R S
// ══════════════════════════════════════════════════════════════════════════════

private fun showRoute(cards: JPanel, route: String) { (cards.layout as CardLayout).show(cards, route) }

private fun showMsg(parent: Component, msg: String) {
    showMessageDialog(parent, "WorldGlance", msg)
}

private fun getValidatedDate(parent: Component, message: String, default: String = ""): String? {
    val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    var input: String? = default.ifEmpty { null }
    while (true) {
        input = showTextInputDialog(parent, "Fecha requerida", message, input ?: "") ?: return null
        if (regex.matches(input.trim())) return input.trim()
        showMessageDialog(parent, "Formato inválido", "Usa yyyy-MM-dd (ej: 2026-06-20).", isError = true)
    }
}

private fun showMessageDialog(parent: Component, title: String, message: String, isError: Boolean = false) {
    val owner = SwingUtilities.getWindowAncestor(parent)
    val dialog = JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL)

    val shell = RPanel(T.BG_CARD, 14, T.BORDER)
    shell.layout = BorderLayout(0, 14)
    shell.border = EmptyBorder(16, 18, 16, 18)

    val icon = if (isError) "\u26A0" else "\u2728"
    val header = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).also { it.isOpaque = false }
    header.add(lbl(icon, Font("Segoe UI", Font.PLAIN, 18), if (isError) T.RED else T.CYAN))
    header.add(lbl(title, T.F_H2, T.TEXT))

    val body = JLabel("<html><div style='width:280px;'>$message</div></html>").also {
        it.font = T.F_BODY
        it.foreground = T.TEXT_SEC
    }

    val ok = WBtn("OK", WBtn.Style.PRIMARY)
    ok.addActionListener { dialog.dispose() }
    val actions = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0)).also { it.isOpaque = false; it.add(ok) }

    shell.add(header, BorderLayout.NORTH)
    shell.add(body, BorderLayout.CENTER)
    shell.add(actions, BorderLayout.SOUTH)

    dialog.contentPane = shell
    dialog.isUndecorated = true
    dialog.pack()
    dialog.setLocationRelativeTo(parent)
    dialog.rootPane.defaultButton = ok
    dialog.isVisible = true
}

private fun showConfirmDeleteDialog(parent: Component): Boolean {
    val title = "Eliminar viaje"
    val message = "¿Eliminar este viaje? Esta acción no se puede deshacer."
    val owner = SwingUtilities.getWindowAncestor(parent)
    val dialog = JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL)
    var accepted = false

    val shell = RPanel(T.BG_CARD, 14, T.BORDER)
    shell.layout = BorderLayout(0, 14)
    shell.border = EmptyBorder(16, 18, 16, 18)

    val header = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).also { it.isOpaque = false }
    header.add(lbl("\u2753", Font("Segoe UI", Font.PLAIN, 18), T.AMBER))
    header.add(lbl(title, T.F_H2, T.TEXT))

    val body = JLabel("<html><div style='width:320px;'>$message</div></html>").also {
        it.font = T.F_BODY
        it.foreground = T.TEXT_SEC
    }

    val cancel = WBtn("Cancelar", WBtn.Style.GHOST)
    val confirm = WBtn("Sí, eliminar", WBtn.Style.DANGER)
    cancel.addActionListener { dialog.dispose() }
    confirm.addActionListener { accepted = true; dialog.dispose() }

    val actions = JPanel(FlowLayout(FlowLayout.RIGHT, 8, 0)).also {
        it.isOpaque = false
        it.add(cancel)
        it.add(confirm)
    }

    shell.add(header, BorderLayout.NORTH)
    shell.add(body, BorderLayout.CENTER)
    shell.add(actions, BorderLayout.SOUTH)

    dialog.contentPane = shell
    dialog.isUndecorated = true
    dialog.pack()
    dialog.setLocationRelativeTo(parent)
    dialog.rootPane.defaultButton = confirm
    dialog.isVisible = true

    return accepted
}

private fun showTextInputDialog(parent: Component, title: String, prompt: String, initialValue: String = ""): String? {
    val owner = SwingUtilities.getWindowAncestor(parent)
    val dialog = JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL)
    var result: String? = null

    val shell = RPanel(T.BG_CARD, 14, T.BORDER)
    shell.layout = BorderLayout(0, 12)
    shell.border = EmptyBorder(16, 18, 16, 18)

    val header = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).also { it.isOpaque = false }
    header.add(lbl("\u270D", Font("Segoe UI", Font.PLAIN, 17), T.CYAN))
    header.add(lbl(title, T.F_H2, T.TEXT))

    val promptLabel = lbl(prompt, T.F_BODY, T.TEXT_SEC)
    val field = WField(28).also { it.text = initialValue }

    val center = JPanel()
    center.layout = BoxLayout(center, BoxLayout.Y_AXIS)
    center.isOpaque = false
    center.add(promptLabel)
    center.add(Box.createVerticalStrut(8))
    center.add(field)

    val cancel = WBtn("Cancelar", WBtn.Style.GHOST)
    val accept = WBtn("Aceptar", WBtn.Style.PRIMARY)
    cancel.addActionListener { dialog.dispose() }
    val submit = {
        result = field.text
        dialog.dispose()
    }
    accept.addActionListener { submit() }
    field.addActionListener { submit() }

    val actions = JPanel(FlowLayout(FlowLayout.RIGHT, 8, 0)).also {
        it.isOpaque = false
        it.add(cancel)
        it.add(accept)
    }

    shell.add(header, BorderLayout.NORTH)
    shell.add(center, BorderLayout.CENTER)
    shell.add(actions, BorderLayout.SOUTH)

    dialog.contentPane = shell
    dialog.isUndecorated = true
    dialog.pack()
    dialog.setLocationRelativeTo(parent)
    dialog.rootPane.defaultButton = accept
    SwingUtilities.invokeLater { field.requestFocusInWindow() }
    dialog.isVisible = true

    return result
}

private fun lbl(text: String, font: Font, color: Color) = JLabel(text).also { it.font = font; it.foreground = color }

private fun styledScroll(view: Component): JScrollPane = JScrollPane(view).apply {
    border = null; background = T.BG; viewport.background = T.BG
    verticalScrollBar.unitIncrement = 16
    verticalScrollBar.background = T.BG_CARD
    horizontalScrollBar.background = T.BG_CARD
    SwingUtilities.invokeLater {
        verticalScrollBar.ui = darkScrollBarUI()
        horizontalScrollBar.ui = darkScrollBarUI()
    }
}

private fun darkScrollBarUI(): BasicScrollBarUI = object : BasicScrollBarUI() {
    override fun configureScrollBarColors() { thumbColor = T.BG_HOVER; trackColor = T.BG_CARD }
    override fun createDecreaseButton(o: Int) = JButton().apply { preferredSize = Dimension(0, 0); isVisible = false }
    override fun createIncreaseButton(o: Int) = JButton().apply { preferredSize = Dimension(0, 0); isVisible = false }
}

private fun countryCodeToFlag(code: String): String {
    if (code.length != 2) return "\uD83C\uDF10"
    return try {
        val base = 0x1F1E6 - 'A'.code
        String(Character.toChars(code[0].uppercaseChar().code + base)) +
            String(Character.toChars(code[1].uppercaseChar().code + base))
    } catch (_: Exception) {
        "\uD83C\uDF10"
    }
}
