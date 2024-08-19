package space.themelon.eia64.compiler.bytecode

import space.themelon.eia64.compiler.CompilerException
import java.io.OutputStream

class Dumper(
    private val output: OutputStream
) {
    fun write(bytecode: Bytecode) {
        output.write(bytecode.ordinal)
    }

    fun write(value: Byte) {
        output.write(value.toInt())
    }

    fun writeInt32(n: Int) {
        output.apply {
            write(n)
            write(n shr 8)
            write(n shr 16)
            write(n shr 24)
        }
    }

    fun writeString(string: String) {
        string.length.let {
            if (it > 255) {
                throw CompilerException("String name cannot exceed 255 characters")
            }
            output.write(it)
        }
        output.write(string.toByteArray())
    }

    fun close() {
        output.close()
    }
}