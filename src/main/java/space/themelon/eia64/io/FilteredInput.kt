package space.themelon.eia64.io

import java.io.InputStream

class FilteredInput(val input: InputStream): InputStream() {
    override fun read(): Int {
        val read = input.read()
        // We gotta translate \r into \n
        if (read.toChar() == '\r') return '\n'.code
        return read
    }
}