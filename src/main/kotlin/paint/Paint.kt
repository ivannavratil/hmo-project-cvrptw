package paint

import shared.Instance
import shared.Solution
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel

fun main() {
    val f = JFrame()

    val edgeMargin = 10

    val screenSize: Dimension = Toolkit.getDefaultToolkit().screenSize
    val height = screenSize.getHeight().toInt()

    val instanceId = 3
    val instance = Instance.fromInstanceId(instanceId)

    val solution: Solution =
        Solution.fromFile(File("src/main/resources/results/best-ant-ls/res-un-i3-theta-2022-01-09-05-23-45-2200515233-LS.txt"))
//    val solution: Solution = shared.Solution.fromFile(File("src/main/resources/results/fake-res-1m-i${instanceId}.txt"))
//    val solution: Solution? = null

    val max = instance.nodes.maxByOrNull { it.yCoordinate }!!.yCoordinate
    val multiplier = height * 0.9 / max

    val depotPointSize = 15
    val customerPointSize = 8

    val pane: JPanel = object : JPanel() {

        var saved: AtomicBoolean = AtomicBoolean(false)

        fun save() {
            val bImg = BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB)
            val cg = bImg.createGraphics()
            this.paintAll(cg)
            try {
                ImageIO.write(bImg, "png", File("src/main/resources/pics/pic.png"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun paintComponent(g: Graphics) {

            val g2 = g as Graphics2D

            g2.color = Color.WHITE
            g2.fillRect(0, 0, width, height)

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
            println(maxReadyTime)

            @Suppress(
                "UNREACHABLE_CODE",
                "UNNECESSARY_SAFE_CALL",
                "UNUSED_ANONYMOUS_PARAMETER"
            )
            solution?.routes?.forEachIndexed { i, route ->
                g.color = Color.getHSBColor(i.toFloat() / solution.routes.size, 1f, 1f)

                route.nodes.zipWithNext().forEachIndexed { j, it ->

                    val customer1 = instance.nodes[it.first.first]
                    val customer2 = instance.nodes[it.second.first]

                    if (j == 0) {
                        g.drawString(
                            "R$i",
                            ((customer1.xCoordinate + customer2.xCoordinate) * multiplier / 2 + edgeMargin).toFloat(),
                            ((customer1.yCoordinate + customer2.yCoordinate) * multiplier / 2 + edgeMargin).toFloat()
                        )
                    }

                    g.drawLine(
                        (customer1.xCoordinate * multiplier + edgeMargin).toInt(),
                        (customer1.yCoordinate * multiplier + edgeMargin).toInt(),
                        (customer2.xCoordinate * multiplier + edgeMargin).toInt(),
                        (customer2.yCoordinate * multiplier + edgeMargin).toInt()
                    )
                }
            }

            //NOTE: manji b => svjetlija boja => raniji ready time
            //NOTE: veći b => tamija boja => kasniji ready time

            instance.nodes.forEachIndexed { i, node ->
                //g.color = if (i == 0) Color.red else Color.black
                g.color = Color.getHSBColor(225.0F, 1.0F, 1 - (node.readyTime / maxReadyTime.toFloat()))
                val pointSize = if (i == 0) depotPointSize else customerPointSize

                g.fillOval(
                    (node.xCoordinate * multiplier - pointSize / 2 + edgeMargin).toInt(),
                    (node.yCoordinate * multiplier - pointSize / 2 + edgeMargin).toInt(),
                    pointSize,
                    pointSize
                )
            }

            if (!saved.get()) {
                saved.set(true)
                save()
            }

        }
    }

    f.contentPane.add(pane)
    f.extendedState = JFrame.MAXIMIZED_BOTH
    f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    f.isVisible = true
}
