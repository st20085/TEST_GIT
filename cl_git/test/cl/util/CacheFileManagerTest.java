package cl.util;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.function.*;

public class CacheFileManagerTest
{
  public static void main(String[] args) throws IOException
  {
    Path tmpPath = Files.createTempFile("test_", ".tmp");
    
    System.out.println("tmpPath="+tmpPath);
    Files.write(tmpPath, Arrays.asList("1", "2"));
    
    File tmpFile = tmpPath.toFile();
    tmpFile.deleteOnExit();
    
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
    
    CacheFileManager<List<String>> cacheFileManager = new CacheFileManager<List<String>>(function) {
//      Map<File, byte[]> checksumMap = new HashMap<>();
      
      @Override
      public boolean hasBeenModified(File file)
      {
        if (super.hasBeenModified(file)) {
          try
          {
            byte[] sha1 = createSha1(file);
            byte[] expectedSha1 = (byte[]) getMetadata(file).computeIfAbsent("checksum", tmp -> sha1);
            if (Arrays.equals(sha1, expectedSha1)) {
              return false;
            }
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
          return true;
        }
        
        return false;
      }
    };
    List<String> lines = cacheFileManager.getInCacheOrLoad(tmpFile);
    System.out.println(lines.size());

    tmpFile.setLastModified(System.currentTimeMillis());
    
    List<String> lines2 = cacheFileManager.getInCacheOrLoad(tmpFile);
    System.out.println(lines2.size());
  }
  
  /**
   * Create SHA1 
   * @param file
   * @throws Exception
   */
  public static byte[] createSha1(File file) throws Exception  {
    MessageDigest digest = MessageDigest.getInstance("SHA-1");
    InputStream fis = new FileInputStream(file);
    int n = 0;
    byte[] buffer = new byte[8192];
    while (n != -1) {
        n = fis.read(buffer);
        if (n > 0) {
            digest.update(buffer, 0, n);
        }
    }
    fis.close();
    byte[] sha1 = digest.digest();
    
//    System.out.println(Arrays.toString(sha1));
//    System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(sha1));
//    System.out.println(javax.xml.bind.DatatypeConverter.printBase64Binary(sha1));

    return sha1;
  }
}
