package space.themelon.eia64.tea;

import space.themelon.eia64.runtime.Executor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TeaMain {

  private static final Executor executor = new Executor();
  private static final ByteArrayOutputStream output = new ByteArrayOutputStream();

  public static void main(String[] args) {
    executor.setInputSupported(false);
    executor.setStandardOutput(new PrintStream(output));
    executor.loadMainSource("println(2+2)");
    System.out.println(output);
  }
}
