package cl.util;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
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
    Objects.requireNonNull(file);
    if (! file.exists())
      throw new RuntimeException("File don't exists : "+file);
      
    FileInfo fileInfo = hasBeenModified(file) ? null : fileToObjectCacheMap.get(file);
    Reference<O> ref = fileInfo == null ? null : fileInfo.ref;
    O o = ref == null ? null : ref.get();
    if (o == null) {
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
    long lastModified = fileInfo == null? -1 : fileInfo.modifiedDate;
    return lastModified != file.lastModified();
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
    FileInfo fileInfo = fileToObjectCacheMap.computeIfAbsent(file, f -> new FileInfo());
    if (fileInfo.metadataMap == null)
      fileInfo.metadataMap = new HashMap<>();
    return fileInfo.metadataMap;
  }
  
  private class FileInfo {
    Reference<O> ref;
    long modifiedDate;
    Map<Object, Object> metadataMap;
  }
}
