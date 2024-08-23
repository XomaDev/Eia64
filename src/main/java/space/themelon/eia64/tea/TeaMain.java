package space.themelon.eia64.tea;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import space.themelon.eia64.runtime.Executor;

public class TeaMain {

  private static final Executor executor = new Executor();

  public static void main(String[] args) {
    loadStdLib();
    exportExecCall((TeaMain::executeEia));
    provideUserInput((TeaMain::provideInput));
  }

  private static void loadStdLib() {
    // load all the Standard Library by default
    String[] stdLibClasses = {"array", "eint", "etype", "math", "string", "list"};
    for (String aClass : stdLibClasses) {
      executor.addModule(Executor.Companion.getSTD_LIB() + aClass + ".eia", aClass);
    }
  }

  // We tell the JS to provide user input
  @JSBody(script = "inputRequired();")
  public static native void flagInputRequired();

  // We post execution result
  @JSBody(params = { "result" }, script = "stdOutLn(result);")
  public static native void flagStdOutLn(String result);

  private static void executeEia(String source) {
    new Thread(() -> executor.loadMainSource(source)).start();
  }

  private static void provideInput(String input) {
    executor.pushUserInput(input);
  }

  @JSBody(params = "eia", script = "main.eia = eia;")
  private static native void exportExecCall(ExecuteEia eia);

  @JSBody(params = "stdInput", script = "main.stdInput = stdInput;")
  private static native void provideUserInput(PushUserInput input);
}

@SuppressWarnings("unused")
@JSFunctor
interface ExecuteEia extends JSObject {
  void executeEia(String source);
}

@SuppressWarnings("unused")
@JSFunctor
interface PushUserInput extends JSObject {
  void provideInput(String userInput);
}