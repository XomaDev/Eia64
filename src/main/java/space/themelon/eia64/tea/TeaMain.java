package space.themelon.eia64.tea;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

public class TeaMain {
  public static void main(String[] args) {
    System.out.println("TeaMain initialized");
    exportFoo((TeaMain::executeEia));
  }

  private static String executeEia(String source) {
    System.out.println(source);
    return "<meow_eia>";
  }

  @JSBody(params = "eia", script = "main.eia = eia;")
  private static native void exportFoo(ExportedEia eia);
}

@SuppressWarnings("unused")
@JSFunctor
interface ExportedEia extends JSObject {
  String executeEia(String source);
}