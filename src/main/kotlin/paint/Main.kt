package paint

import Instance
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Toolkit
import javax.swing.JFrame
import javax.swing.JPanel

fun main(args: Array<String>) {
    val f = JFrame()

    val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
    val width = screenSize.getWidth().toInt()
    val height = screenSize.getHeight().toInt()

    val instanceId = 1
    val instance = Instance.fromInstanceId(instanceId)

    val max = instance.customers.maxByOrNull { it.yCoordinate }!!.yCoordinate
    val multiplier = (height * 0.9).toInt() / max

    val depotPointSize = 10
    val customerPointSize = 3

    val pane: JPanel = object : JPanel() {
        override fun paintComponent(g: Graphics) {
            (g as Graphics2D).setRenderingHints(
                RenderingHints(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
                )
            )

            g.color = Color.red
            g.fillOval(
                instance.depot.xCoordinate * multiplier - depotPointSize / 2,
                instance.depot.yCoordinate * multiplier - depotPointSize / 2,
                depotPointSize,
                depotPointSize
            )

            g.color = Color.black
            instance.customers.forEach {
                g.fillOval(
                    it.xCoordinate * multiplier - customerPointSize / 2,
                    it.yCoordinate * multiplier - customerPointSize / 2,
                    customerPointSize,
                    customerPointSize
                )
            }
        }
    }

    f.contentPane.add(pane)
    f.extendedState = JFrame.MAXIMIZED_BOTH
    f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    f.isVisible = true
}