package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.EuclidianDistanceCalculator;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixGrid;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.SpatialStrategy;
import org.apache.lucene.spatial.strategy.util.CachedDistanceValueSource;
import org.apache.lucene.spatial.strategy.util.StringListTokenizer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author David Smiley - dsmiley@mitre.org
 */
public abstract class PrefixGridStrategy extends SpatialStrategy<SimpleSpatialFieldInfo> {
  protected final SpatialPrefixGrid grid;
  private final Map<String, PrefixFieldCacheProvider> provider = new ConcurrentHashMap<String, PrefixFieldCacheProvider>();
  protected int defaultFieldValuesArrayLen = 2;
  protected double distErrPct = SpatialArgs.DEFAULT_DIST_PRECISION;

  public PrefixGridStrategy(SpatialPrefixGrid grid) {
    this.grid = grid;
  }

  /** Used in the in-memory ValueSource as a default ArrayList length for this field's array of values, per doc. */
  public void setDefaultFieldValuesArrayLen(int defaultFieldValuesArrayLen) {
    this.defaultFieldValuesArrayLen = defaultFieldValuesArrayLen;
  }

  /** See {@link SpatialPrefixGrid#getMaxLevelForPrecision(org.apache.lucene.spatial.base.shape.Shape, double)}. */
  public void setDistErrPct(double distErrPct) {
    this.distErrPct = distErrPct;
  }

  @Override
  public Fieldable createField(SimpleSpatialFieldInfo fieldInfo, Shape shape, boolean index, boolean store) {
    int detailLevel = grid.getMaxLevelForPrecision(shape,distErrPct);
    List<SpatialPrefixGrid.Cell> cells = grid.getCells(shape, detailLevel, true);//true=intermediates cells
    if (cells.get(0).getLevel() == 0)
      cells.remove(0);//don't want world cell
    BasicGridFieldable fieldable = new BasicGridFieldable(fieldInfo.getFieldName(), store);
    List<String> tokens = SpatialPrefixGrid.cellsToTokenStrings(cells);
    fieldable.tokens = new StringListTokenizer(tokens);

    if (store) {//TODO is this for debugging?
      fieldable.value = tokens.toString();
    }

    return fieldable;
  }

  @Override
  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo) {
    DistanceCalculator calc = new EuclidianDistanceCalculator();
    return makeValueSource(args, fieldInfo,calc);
  }
  
  public ValueSource makeValueSource(SpatialArgs args, SimpleSpatialFieldInfo fieldInfo, DistanceCalculator calc) {
    PrefixFieldCacheProvider p = provider.get( fieldInfo.getFieldName() );
    if( p == null ) {
      synchronized (this) {//double checked locking idiom is okay since provider is threadsafe
        p = provider.get( fieldInfo.getFieldName() );
        if (p == null) {
          p = new PrefixFieldCacheProvider(grid, fieldInfo.getFieldName(), defaultFieldValuesArrayLen);
          provider.put(fieldInfo.getFieldName(),p);
        }
      }
    }
    Point point = args.getShape().getCenter();
    return new CachedDistanceValueSource(point, calc, p);
  }

  public SpatialPrefixGrid getGrid() {
    return grid;
  }
}
