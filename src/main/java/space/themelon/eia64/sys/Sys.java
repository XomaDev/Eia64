package space.themelon.eia64.sys;

import kotlin.random.Random;

@SuppressWarnings("unused")
public class Sys {

  public static void println(Object o) {
    System.out.println(o);
  }

  public static void print(Object o) {
    System.out.print(o);
  }

  public static long time() {
    return System.currentTimeMillis();
  }

  public static void sleep(long millis) throws InterruptedException {
    Thread.sleep(millis);
  }

  public static void exit(int exitCode) {
    System.exit(exitCode);
  }

  public static int rand(int min, int max) {
    return Random.Default.nextInt(min, max);
  }
}
