package space.themelon.eia64.compiler

import space.themelon.eia64.compiler.bytecode.Bytecode
import java.io.File
import java.io.FileWriter
import java.util.*

class Disassembler(
    compiledFile: File,
    outputFile: File
) {
    // A program that converts compiled Eia code into assembly like human-readable format

    private val bytes: ByteArray = compiledFile.readBytes()

    private var index = 0
    private val size: Int = compiledFile.length().toInt()

    private val writer: FileWriter = FileWriter(outputFile)

    init {
        writer.use {
            while (!isEOF()) {
                disassemble()
            }
        }
    }

    private fun disassemble() {
        val instruction = Bytecode.entries[next().toInt()]
        val line = StringJoiner(" ")
        line.add(instruction.name)

        when (instruction) {
            Bytecode.INT -> line.add(readInt32().toString())
            else -> { }
        }

        writer.write(line.toString())
        writer.write("\n")
    }

    private fun readInt32() =
        next().toInt() and 255 or
                (next().toInt() and 255 shl 8) or
                (next().toInt() and 255 shl 16) or
                (next().toInt() and 255 shl 24)

    private fun next() = bytes[index++]

    private fun isEOF(): Boolean = index == size

}