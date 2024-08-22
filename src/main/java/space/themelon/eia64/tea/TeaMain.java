package space.themelon.eia64.tea;

import org.teavm.interop.Async;
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

  @Async
  @JSBody(script = "return stdInput();")
  public static native String readUserInput();

  private static String[] executeEia(String source) {
    displayStream.reset();
    executor.loadMainSource(source);
    return new String[] {String.valueOf(executor.getAwaitingInput()), displayStream.toString()};
  }

  private static void provideInput(String input) {
    // means the user enters text through the UI
    //executor.pushUserInput(input);
  }

  @Async
  @JSBody(params = "eia", script = "main.eia = eia;")
  private static native void exportExecCall(ExecuteEia eia);

  @JSBody(params = "input", script = "main.input = input;")
  private static native void provideUserInput(UserInputEia input);
}

@SuppressWarnings("unused")
@JSFunctor
interface ExecuteEia extends JSObject {
  String[] executeEia(String source);
}

@SuppressWarnings("unused")
@JSFunctor
interface UserInputEia extends JSObject {
  void executeEia(String userInput);
}