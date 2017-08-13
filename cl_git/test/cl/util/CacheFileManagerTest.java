package cl.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

public class CacheFileManagerTest
{
  public static void main(String[] args) throws IOException
  {
    Path tmpPath = Files.createTempFile("test_", ".tmp");
    File tmpFile = tmpPath.toFile();
    tmpFile.deleteOnExit();
    
    System.out.println("tmpPath="+tmpPath);
    Files.write(tmpPath, Arrays.asList("1", "2"));
    
    //
    Function<File, List<String>> function = file -> {
      try
      {
        System.out.println("try to read file "+file);
        List<String> lines = Files.readAllLines(file.toPath());
        System.out.println("finish lines.size "+lines.size());
        return lines;
      }
      catch(IOException e)
      {
        e.printStackTrace();
        return Collections.emptyList();
      }
    };
    
//    List<String> lines = function.apply(tmpFile);
//    List<String> lines2 = function.apply(tmpFile);
    
    CacheFileManager<List<String>> cacheFileManager = new CacheFileManager<List<String>>(function);
    List<String> lines = cacheFileManager.getInCacheOrLoad(tmpFile);
    System.out.println(lines.size());
    lines.clear();
    
    List<String> lines2 = cacheFileManager.getInCacheOrLoad(tmpFile);
    System.out.println(lines2.size());
  }
}
