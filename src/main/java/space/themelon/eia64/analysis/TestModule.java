package space.themelon.eia64.analysis;

import space.themelon.eia64.runtime.Executor;
import space.themelon.eia64.syntax.Lexer;

import java.util.concurrent.atomic.AtomicReference;

public class TestModule {

  public final Object lock = new Object();

  public void loadModule() {
    synchronized(lock) {
      for (;;) {
        Parser parser = new Parser(new Executor());
        parser.parse(new Lexer("a = 5").getTokens());
      }
    }
  }
}
