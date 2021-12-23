package paint

import Instance
import Solution
import java.awt.*
import java.io.File
import javax.swing.JFrame
import javax.swing.JPanel

fun main(args: Array<String>) {
    val f = JFrame()

    val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
    val height = screenSize.getHeight().toInt()

    val instanceId = 1
    val instance = Instance.fromInstanceId(instanceId)

    val solution: Solution = Solution.fromFile(File("src/main/resources/results/fake-res-1m-i${instanceId}.txt"))
//    val solution: Solution? = null

    val max = instance.nodes.maxByOrNull { it.yCoordinate }!!.yCoordinate
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

            instance.nodes.forEachIndexed { i, node ->
                g.color = if (i == 0) Color.red else Color.black
                val pointSize = if (i == 0) depotPointSize else customerPointSize

                g.fillOval(
                    node.xCoordinate * multiplier - pointSize / 2,
                    node.yCoordinate * multiplier - pointSize / 2,
                    pointSize,
                    pointSize
                )
            }

            @Suppress(
                "UNREACHABLE_CODE",
                "UNNECESSARY_SAFE_CALL",
                "UNUSED_ANONYMOUS_PARAMETER"
            ) solution?.routes?.forEachIndexed { i, route ->
                g.color = Color.getHSBColor(i.toFloat() / solution.routes.size, 1f, 1f)

                route.nodes.zipWithNext().forEach {
                    val customer1 = instance.nodes[it.first.first]
                    val customer2 = instance.nodes[it.second.first]

                    g.drawLine(
                        customer1.xCoordinate * multiplier,
                        customer1.yCoordinate * multiplier,
                        customer2.xCoordinate * multiplier,
                        customer2.yCoordinate * multiplier
                    )
                }
            }
        }
    }

    f.contentPane.add(pane)
    f.extendedState = JFrame.MAXIMIZED_BOTH
    f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    f.isVisible = true
}