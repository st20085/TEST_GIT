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
  final Map<File, Reference<O>> fileToObjectCacheMap = new HashMap<>();
  final Map<File, Long> fileToDateCacheMap = new HashMap<>();
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
  public O getInCacheOrLoad(File file) throws Exception {

    // {
    // System.err.println("clear cache");
    // fileToObjectCacheMap.clear();
    // fileToDateCacheMap.clear();
    // }

    Reference<O> ref = hasBeenModified(file) ? null : fileToObjectCacheMap.get(file);
    O o = ref == null ? null : ref.get();
    if (o == null) {
      removeFromCache(file);

      o = load(file);

      fileToObjectCacheMap.put(file, createReference(o));
      fileToDateCacheMap.put(file, file.lastModified());
    }

    return o;
  }

  /**
   * Load with function
   *
   * @param file
   * @throws Exception
   */
  protected O load(File file) throws Exception {
    return function.apply(file);
  }

  /**
   * Return true if file has been modified (use last modified date)
   *
   * @param file
   */
  protected boolean hasBeenModified(File file) {
    Long lastModified = fileToDateCacheMap.get(file);
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
    Reference<O> ref = fileToObjectCacheMap.get(file);
    return ref == null ? null : ref.get();
  }

  /**
   * Remove objet from cache
   *
   * @param file
   */
  public void removeFromCache(File file) {
    fileToObjectCacheMap.remove(file);
    fileToDateCacheMap.remove(file);
  }
}
