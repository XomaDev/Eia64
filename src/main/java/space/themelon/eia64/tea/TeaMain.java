package space.themelon.eia64.tea;

import org.teavm.jso.JSObject;
import space.themelon.eia64.runtime.Executor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TeaMain {

  private static final Executor executor = new Executor();
  private static final ByteArrayOutputStream output = new ByteArrayOutputStream();

  public static void main(String[] args) {
    executor.setInputSupported(false);
    executor.setStandardOutput(new PrintStream(output));
//    exportFoo((TeaMain::executeEia));
  }

  private static String executeEia(String source) {
    output.reset();
    executor.loadMainSource(source);
    return output.toString();
  }

//  @JSBody(params = "eia", script = "main.eia = eia;")
//  private static native void exportFoo(ExportedEia eia);
}

//@SuppressWarnings("unused")
//@JSFunctor
interface ExportedEia extends JSObject {
  String executeEia(String source);
}