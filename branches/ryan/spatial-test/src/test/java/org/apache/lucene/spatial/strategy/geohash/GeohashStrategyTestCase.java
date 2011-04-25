package org.apache.lucene.spatial.strategy.geohash;

import java.io.IOException;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.StrategyTestCase;
import org.junit.Test;


public class GeohashStrategyTestCase extends StrategyTestCase<SimpleSpatialFieldInfo> {

  @Test
  public void testGeohashStrategy() throws IOException {

    SimpleSpatialFieldInfo finfo = new SimpleSpatialFieldInfo( "geohash" );

    int maxLength = GridReferenceSystem.getMaxPrecision();
    GridReferenceSystem grs = new GridReferenceSystem(
        new SimpleSpatialContext(), maxLength );
    GeohashStrategy s = new GeohashStrategy( grs );

    // SimpleIO
    executeQueries( s, grs.shapeIO, finfo,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
  }


  @Test
  public void testGeohashStrategyWithJts() throws IOException {

    SimpleSpatialFieldInfo finfo = new SimpleSpatialFieldInfo( "geohash" );

    SpatialContext ctx = new SimpleSpatialContext();
    int maxLength = GridReferenceSystem.getMaxPrecision();
    GridReferenceSystem grs = new GridReferenceSystem( ctx, maxLength );
    GeohashStrategy s = new GeohashStrategy( grs );

    executeQueries( s, ctx, finfo,
        DATA_WORLD_CITIES_POINTS,
        QTEST_Cities_IsWithin_BBox );
  }
}
