package edu.dyds.trips.presentation

import java.awt.Color
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JWindow
import javax.swing.SwingConstants

private const val SPLASH_IMAGE_PATH = "/images/Launch_Screen.png"

fun showSplashScreen(): JWindow? {
    if (GraphicsEnvironment.isHeadless()) return null

    val originalImage = loadSplashImage() ?: return null
    val image = scaleToScreen(originalImage)
    val window = JWindow().apply {
        background = Color(0, 0, 0, 0)
        contentPane = JLabel(ImageIcon(image)).apply {
            border = BorderFactory.createLineBorder(Color(20, 28, 44), 1)
            horizontalAlignment = SwingConstants.CENTER
            verticalAlignment = SwingConstants.CENTER
        }
        pack()
        preferredSize = Dimension(image.width, image.height)
        setLocationRelativeTo(null)
        isVisible = true
    }

    return window
}

private fun loadSplashImage(): BufferedImage? {
    val resource = Thread.currentThread().contextClassLoader.getResource(SPLASH_IMAGE_PATH.removePrefix("/")) ?: return null
    return ImageIO.read(resource).takeIf { it.width > 0 && it.height > 0 }
}

private fun scaleToScreen(image: BufferedImage): BufferedImage {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val maxWidth = (screenSize.width * 0.7).toInt().coerceAtLeast(320)
    val maxHeight = (screenSize.height * 0.7).toInt().coerceAtLeast(240)

    val widthRatio = maxWidth.toDouble() / image.width.toDouble()
    val heightRatio = maxHeight.toDouble() / image.height.toDouble()
    val ratio = minOf(widthRatio, heightRatio, 1.0)

    val targetWidth = (image.width * ratio).toInt().coerceAtLeast(1)
    val targetHeight = (image.height * ratio).toInt().coerceAtLeast(1)

    val scaled = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
    val g2 = scaled.createGraphics()
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.drawImage(image, 0, 0, targetWidth, targetHeight, null)
    g2.dispose()
    return scaled
}




