package cryptography

import java.io.File
import javax.imageio.IIOException
import javax.imageio.ImageIO
import kotlin.experimental.xor

fun main() {
    while (true) {
        println("Task (hide, show, exit):")
        val userResponse = readln().lowercase()
        when (userResponse) {
            "exit" -> println("Bye!").also { return }
            "hide" -> try { Hide()}
            catch (e: IIOException) { println(e.message) }
            catch (e:Exception) { println(e.message) }
            "show" -> try { Show()} catch (e: Exception) { println(e.message) }
            else -> println("Wrong task: $userResponse")
        }
    }
}

class Show() {
    val inputFileName: String
    var msg: String = ""
    val msgEndArray = 3
    val pswd: String
    init {
        println("Input image file:")
        inputFileName = readln()

        val inputFile = File(inputFileName)
        val image = ImageIO.read(inputFile)

        println("Password:")
        pswd = readln()

        var msgArrayByte: ByteArray = byteArrayOf()
        var count = 1
        var volue: Int = 0
        for (y in 0..image.height - 1) {
            for (x in 0..image.width - 1) {
                 volue = (image.getRGB(x, y) and 0x000001) or volue
                if (count % 8 == 0 && count >= 3 * 8) {
                    if ((volue and 0x00ffffff) xor msgEndArray == 0) {

                        val pswdArrayByte = pswd.encodeToByteArray()
                        for (i in msgArrayByte.indices step pswdArrayByte.size) {
                            val range = if (i + pswdArrayByte.size > msgArrayByte.lastIndex) 0..msgArrayByte.lastIndex - i else 0..pswdArrayByte.lastIndex
                            for (j in range) {
                                msgArrayByte[i + j] = msgArrayByte[i + j] xor pswdArrayByte[j]
                            }
                        }

                        msg = msgArrayByte.toString(Charsets.UTF_8)
                        throw Exception("Message:\n$msg")
                    } else {
                        msgArrayByte += ((volue ushr 16) and 0x000000ff).toByte()
                        volue = volue and 0x0000ffff shl 1
                    }
                } else volue = volue shl 1
                count++
            }
        }
    }
}

class Hide() {
    val inputFileName: String
    val outputFileName: String
    val msg: String
    val msgEndArray = byteArrayOf(0, 0, 3)
    val pswd: String

    init {
        println("Input image file:")
        inputFileName = readln()
        println("Output image file:")
        outputFileName = readln()

        val inputFile = File(inputFileName)
        val image = ImageIO.read(inputFile)

        println("Message to hide:")
        msg = readln()
        println("Password:")
        pswd = readln()
        val exceptMsg = "The input image is not large enough to hold this message."
        if ((msg.length + 3) * 8  > image.width * image.height) throw Exception(exceptMsg)

        var msgArrayByte = msg.encodeToByteArray()
        val pswdArrayByte = pswd.encodeToByteArray()

        for (i in msgArrayByte.indices step pswdArrayByte.size) {
            val range = if (i + pswdArrayByte.size > msgArrayByte.lastIndex) 0..msgArrayByte.lastIndex - i else 0..pswdArrayByte.lastIndex
            for (j in range) {
                msgArrayByte[i + j] = msgArrayByte[i + j] xor pswdArrayByte[j]
            }
        }
        msgArrayByte += msgEndArray

        var count = 0L
        var volueArray = 0
        for (y in 0 .. msgArrayByte.size * 8 / image.width) {
            val range = if (msgArrayByte.size * 8 - y * image.width > image.width) {
                0 until image.width
            } else {
                0 until msgArrayByte.size * 8 - y * image.width
            }
            for (x in range) {
                if (count % 8 == 0L ) {
                    volueArray = msgArrayByte[(count / 8).toInt()].toInt()

                 //   println(volueArray.toChar())

                }
                val i = volueArray ushr 7 and 0x00000001
                val rgb = image.getRGB(x, y)
                val bl = rgb and 0x00000001
                val resultRgb = if (i xor bl == 0) {
                    rgb
                } else {
                    if (i == 1) {
                        rgb or 1
                    } else {
                        rgb and 0xfffffffe.toInt()
                    }
                }
                image.setRGB(x, y, resultRgb)
                volueArray = volueArray shl 1
                count++
            }
        }

        val outputFile = File(outputFileName)
        ImageIO.write(image, "png", outputFile)
        println("Message saved in $outputFileName image.")
    }
}

