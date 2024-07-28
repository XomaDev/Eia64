package space.themelon.eia64.io

import java.io.OutputStream

class FilteredOutput(private val output: OutputStream): OutputStream() {

    override fun write(b: Int) {
        output.apply {
            // Since we translate \r -> \n at Filtered Input
            // Now we have to does it vice versa
            if (b.toChar() == '\n') write('\r'.code)
            write(b)
            flush()
        }
    }
}