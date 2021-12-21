package paint

import Customer
import Depot
import parse
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.io.File
import javax.swing.JFrame
import javax.swing.JPanel

fun main(args: Array<String>) {
    val f = JFrame()

    val instance = 1
    val buffer = File("src/main/resources/i$instance").readLines()
    val depot: Depot = Depot.fromLine(buffer[7].parse())
    val customers = IntRange(8, buffer.size - 1).map { Customer.fromLine(buffer[it].parse()) }.toList()

    val max = customers.maxByOrNull { it.yCoordinate }!!.yCoordinate

    val multiplier = 1000 / max

    val pane: JPanel = object : JPanel() {
        override fun paintComponent(g: Graphics) {
            (g as Graphics2D).setRenderingHints(
                RenderingHints(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
                )
            )

            g.color = Color.red
            g.fillOval(depot.xCoordinate * multiplier, depot.yCoordinate * multiplier, 10, 10)

            g.color = Color.black
            customers.forEach {
                g.fillOval(it.xCoordinate * multiplier, it.yCoordinate * multiplier, 3, 3)
            }
        }
    }

    f.contentPane.add(pane)
    f.extendedState = JFrame.MAXIMIZED_BOTH
    f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    f.isVisible = true
}