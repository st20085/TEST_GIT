package cl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

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
        System.err.println("try to read file "+file);
        List<String> lines = Files.readAllLines(file.toPath());
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
    System.out.println("lines.size="+lines.size());

//    tmpFile.setLastModified(System.currentTimeMillis());
//    Files.write(tmpPath, Arrays.asList("1", "2", "3"));
//    tmpFile.delete();

    cacheFileManager.expungeStaleEntries();
    System.out.println(cacheFileManager);

    List<String> lines2 = cacheFileManager.getInCacheOrLoad(tmpFile);
    System.out.println("lines.size="+lines2.size());

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
