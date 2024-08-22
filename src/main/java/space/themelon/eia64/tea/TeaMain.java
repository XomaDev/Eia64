package space.themelon.eia64.tea;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import space.themelon.eia64.runtime.Executor;

import java.io.ByteArrayOutputStream;

public class TeaMain {

  private static final Executor executor = new Executor();
  private static final ByteArrayOutputStream displayStream = executor.getDisplayStream();

  public static void main(String[] args) {
    exportExecCall((TeaMain::executeEia));
    provideUserInput((TeaMain::provideInput));
  }

  // We tell the JS to provide user input
  @JSBody(script = "inputRequired();")
  public static native void flagInputRequired();

  // We post execution result
  @JSBody(params = { "result" }, script = "execResult(result);")
  public static native void flagExecResult(String result);

  private static void executeEia(String source) {
    displayStream.reset();
    new Thread(() -> {
      executor.loadMainSource(source);
      flagExecResult(displayStream.toString());
    }).start();
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