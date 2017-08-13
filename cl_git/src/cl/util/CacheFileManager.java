package cl.util;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The class <b>CacheFileManager</b> allows to.<br>
 */
public class CacheFileManager<O> {
  final Map<File, FileInfo> fileToObjectCacheMap = new HashMap<>();
  final Function<File, O> function;

  /**
   * Constructor
   *
   * @param function
   */
  public CacheFileManager(Function<File, O> function) {
    this.function = function;
  }

  /**
   * Get object in cache if exists and if not last modified else Load with function
   *
   * @param file
   */
  public O getInCacheOrLoad(File file) {
    FileInfo fileInfo = hasBeenModified(file) ? null : fileToObjectCacheMap.get(file);
    Reference<O> ref = fileInfo == null ? null : fileInfo.ref;
    O o = ref == null ? null : ref.get();
    if (o == null) {
      removeFromCache(file);

      o = load(file);

      if (fileInfo == null) {
        fileInfo = new FileInfo();
        fileToObjectCacheMap.put(file, fileInfo);
      }
      
      fileInfo.ref = createReference(o);
      fileInfo.modifiedDate = file.lastModified();
    }

    return o;
  }

  /**
   * Load with function
   *
   * @param file
   */
  public final O load(File file) {
    return function.apply(file);
  }

  /**
   * Return true if file has been modified (use last modified date, to modify)
   *
   * @param file
   */
  public boolean hasBeenModified(File file) {
    FileInfo fileInfo = fileToObjectCacheMap.get(file);
    Long lastModified = fileInfo == null? null : fileInfo.modifiedDate;
    return lastModified == null || lastModified.longValue() != file.lastModified();
  }

  /**
   * Create default weak reference
   *
   * @param o
   */
  protected Reference<O> createReference(O o) {
    return new WeakReference<>(o);
  }

  /**
   * Get object in cache if exists else return null
   *
   * @param file
   */
  public O getInCache(File file) {
    FileInfo fileInfo = fileToObjectCacheMap.get(file);
    Reference<O> ref = fileInfo == null? null : fileInfo.ref;
    return ref == null ? null : ref.get();
  }

  /**
   * Remove objet from cache
   *
   * @param file
   */
  public void removeFromCache(File file) {
    fileToObjectCacheMap.remove(file);
  }
  
  /**
   * Return the metadata
   * @param file
   */
  protected Map<Object, Object> getMetadata(File file) {
    FileInfo fileInfo = fileToObjectCacheMap.get(file);
    if (fileInfo.metadataMap == null)
      fileInfo.metadataMap = new HashMap<>();
    return fileInfo.metadataMap;
  }
  
//  protected void putMetadata(File file, Object key, Object value) {
//    FileInfo fileInfo = fileToObjectCacheMap.get(file);
//    fileInfo.metadataMap.put(key, value);
//  }
//  
//  protected <T> T getMetadata(File file, Object key) {
//    FileInfo fileInfo = fileToObjectCacheMap.get(file);
//    return (T) fileInfo.metadataMap.get(key);
//  }
  
  private class FileInfo {
    Reference<O> ref;
    long modifiedDate;
    Map<Object, Object> metadataMap;
  }
}
