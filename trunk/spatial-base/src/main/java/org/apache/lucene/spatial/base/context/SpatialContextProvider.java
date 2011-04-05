package org.apache.lucene.spatial.base.context;

import org.apache.lucene.spatial.base.context.jts.JtsSpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO -- i think there is a more standard way to approach this problem
 */
public class SpatialContextProvider {
  static final Logger log = LoggerFactory.getLogger(SpatialContextProvider.class);

  private static SpatialContext instance = null;

  private SpatialContextProvider() {
  }

  @SuppressWarnings("unchecked")
  public static synchronized SpatialContext getContext() {
    if (instance != null) {
      return instance;
    }

    String cname = System.getProperty("SpatialContextProvider");
    if (cname != null) {
      try {
        Class<? extends SpatialContext> clazz = (Class<? extends SpatialContext>) Class.forName(cname);
        instance = clazz.newInstance();
        return instance;
      } catch (Exception e) {
        log.warn("Using default SpatialContext", e);
      }
    }

    try {
      // Try to make the JTS version
      instance = new JtsSpatialContext();
      return instance;
    }
    catch( Exception ex ) {
      log.warn("Unable to make JTS Spatial Context", ex);
    }
    instance = new SimpleSpatialContext();
    return instance;
  }

  static void clear() {
    instance = null;
  }
}
