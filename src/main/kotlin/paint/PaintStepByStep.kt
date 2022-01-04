package paint

import shared.Instance
import shared.Solution
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Toolkit
import java.io.File
import java.util.Scanner
import javax.swing.JFrame
import javax.swing.JPanel

fun main() {
    val f = JFrame()

    val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
    val height = screenSize.getHeight().toInt()

    val instanceId = 2
    val instance = Instance.fromInstanceId(instanceId)

    val solution: Solution = Solution.fromFile(File("src/main/resources/results/best/i$instanceId-LS.txt"))
//    val solution: Solution = shared.Solution.fromFile(File("src/main/resources/results/fake-res-1m-i${instanceId}.txt"))
//    val solution: Solution? = null

    val max = instance.nodes.maxByOrNull { it.yCoordinate }!!.yCoordinate
    val multiplier = height * 0.9 / max

    val depotPointSize = 15
    val customerPointSize = 8

    var upToRoute = 0
    var routeStep = 0

    val pane: JPanel = object : JPanel() {
        override fun paintComponent(g: Graphics) {

            val g2 = g as Graphics2D

            g2.setRenderingHints(
                RenderingHints(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
                )
            )

            g2.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
            )
            g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE
            )

            val maxReadyTime = instance.nodes.maxOf { it.readyTime }

            @Suppress(
                "UNREACHABLE_CODE",
                "UNNECESSARY_SAFE_CALL",
                "UNUSED_ANONYMOUS_PARAMETER"
            )

            solution?.routes?.forEachIndexed { i, route ->
                if (i <= upToRoute) {
                    g.color = Color.getHSBColor(i.toFloat() / solution.routes.size, 1f, 1f)

                    route.nodes.zipWithNext().forEachIndexed { j, it ->

                        if (routeStep > j || i < upToRoute) {

                            val customer1 = instance.nodes[it.first.first]
                            val customer2 = instance.nodes[it.second.first]

                            if (j == 0) {
                                g.drawString(
                                    "R$i",
                                    ((customer1.xCoordinate + customer2.xCoordinate) * multiplier / 2).toFloat(),
                                    ((customer1.yCoordinate + customer2.yCoordinate) * multiplier / 2).toFloat()
                                )
                            }

                            g.drawLine(
                                (customer1.xCoordinate * multiplier).toInt(),
                                (customer1.yCoordinate * multiplier).toInt(),
                                (customer2.xCoordinate * multiplier).toInt(),
                                (customer2.yCoordinate * multiplier).toInt()
                            )
                        }
                    }
                }
            }

            //NOTE: manji b => svjetlija boja => raniji ready time
            //NOTE: veći b => tamija boja => kasniji ready time

            instance.nodes.forEachIndexed { i, node ->
                //g.color = if (i == 0) Color.red else Color.black
                g.color = Color.getHSBColor(0.3F, 1.0F, 1 - (node.readyTime / maxReadyTime.toFloat()))
                val pointSize = if (i == 0) depotPointSize else customerPointSize

                g.fillOval(
                    (node.xCoordinate * multiplier - pointSize / 2).toInt(),
                    (node.yCoordinate * multiplier - pointSize / 2).toInt(),
                    pointSize,
                    pointSize
                )

                //PRINTS NODE ID ABOVE NODE
                g.drawString(
                    "$i",
                    (node.xCoordinate * multiplier - pointSize / 2).toFloat(),
                    (node.yCoordinate * multiplier - pointSize / 2).toFloat()
                )
            }
        }
    }

    f.contentPane.add(pane)
    f.extendedState = JFrame.MAXIMIZED_BOTH
    f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    f.isVisible = true

    val sc = Scanner(System.`in`)
    while (true) {
        try {

            println("Enter route,step")
            val temp = sc.nextLine().split(",").map { it.toInt() }.toTypedArray()

            upToRoute = temp[0]
            routeStep = temp[1]
            pane.repaint()
        } catch (ex: Exception) {

        }
    }

}
