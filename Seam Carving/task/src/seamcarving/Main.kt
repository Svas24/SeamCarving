package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt

fun main(args: Array<String>) {
    var image = ImageIO.read(File(args[1]))
    for (i in 0 until args[5].toInt()) image = removeVerticalSeam(image)
    image = rotate90(image, true)
    for (i in 0 until args[7].toInt()) image = removeVerticalSeam(image)
    image = rotate90(image, true)
    ImageIO.write(image, "png", File(args[3]))
}

fun removeVerticalSeam(image: BufferedImage): BufferedImage {
    fun pow2(i: Int) = i * i
    with (image) {
        val energies = Array(height) { Array(width) { 0.0 } }
        for (y in 0 until height)
            for (x in 0 until width) {
                val cx = x.coerceIn(1..width - 2)
                val cy = y.coerceIn(1..height - 2)
                val west = Color(getRGB(cx - 1, y))
                val east = Color(getRGB(cx + 1, y))
                val north = Color(getRGB(x, cy - 1))
                val south = Color(getRGB(x, cy + 1))
                val gradX = pow2(west.red - east.red) + pow2(west.green - east.green) + pow2(west.blue - east.blue)
                val gradY =
                    pow2(north.red - south.red) + pow2(north.green - south.green) + pow2(north.blue - south.blue)
                energies[y][x] = sqrt((gradX + gradY).toDouble())
            }
        val weights = Array(height) { Array(width) { 0.0 } }
        weights[0] = energies[0].copyOf()
        for (y in 1 until height) {
            for (x in 0 until width) {
                var min = weights[y - 1][x]
                if (x > 0 && min > weights[y - 1][x - 1]) min = weights[y - 1][x - 1]
                if (x < width - 1 && min > weights[y - 1][x + 1]) min = weights[y - 1][x + 1]
                weights[y][x] = energies[y][x] + min
            }
        }
        val newImage = BufferedImage(width - 1, height, type)
        var remX = 0
        var remValue = 0.0
        for (y in height - 1 downTo 0) {
            if (y == height - 1) for (x in 1 until width) if (weights[y][remX] > weights[y][x]) remX = x
            else for (x in (remX - 1).coerceAtLeast(0)..(remX + 1).coerceAtMost(width - 1))
                if (weights[y][x] == remValue - weights[y][remX]) remX = x
            remValue = weights[y][remX]
            for (x in 0 until width - 1) {
                newImage.setRGB(x, y, getRGB(if(x >= remX) x + 1 else x, y))
            }
        }
        return newImage
    }
}

fun rotate90(image: BufferedImage, clockwise: Boolean = true): BufferedImage {
    with (image) {
        val newImage = BufferedImage(height, width, type)
        val graphics = newImage.createGraphics()
        if (clockwise) {
            graphics.translate(height, 0)
            graphics.rotate(90.0 * Math.PI / 180.0)
        } else {
            graphics.translate(0, width)
            graphics.rotate(-90.0 * Math.PI / 180.0)
        }
        graphics.drawImage(image, 0, 0, null)
        graphics.dispose()
        return newImage
    }
}
