package paint

import Instance
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JFrame
import javax.swing.JPanel

fun main(args: Array<String>) {
    val f = JFrame()

    val instanceId = 1
    val instance = Instance.fromInstanceId(instanceId)

    val max = instance.customers.maxByOrNull { it.yCoordinate }!!.yCoordinate

    val multiplier = 1000 / max

    val pane: JPanel = object : JPanel() {
        override fun paintComponent(g: Graphics) {
            (g as Graphics2D).setRenderingHints(
                RenderingHints(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
                )
            )

            g.color = Color.red
            g.fillOval(instance.depot.xCoordinate * multiplier, instance.depot.yCoordinate * multiplier, 10, 10)

            g.color = Color.black
            instance.customers.forEach {
                g.fillOval(it.xCoordinate * multiplier, it.yCoordinate * multiplier, 3, 3)
            }
        }
    }

    f.contentPane.add(pane)
    f.extendedState = JFrame.MAXIMIZED_BOTH
    f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    f.isVisible = true
}