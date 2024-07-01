package space.themelon.eia64;

import space.themelon.eia64.analysis.ParserV2;
import space.themelon.eia64.syntax.LexerV2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SimpleStart {
  public static void main(String[] args) throws IOException {
    File file = new File("/home/kumaraswamy/Documents/Eia64/src/main/resources/whenstatements.eia");
    new ParserV2(new LexerV2(Files.readString(file.toPath())).getHeadBlock());
  }
}
