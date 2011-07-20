package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.test.strategy.BaseRecursiveGridStrategyTestCase;
import org.junit.Before;


public class RecursiveGridStrategyTestCase extends BaseRecursiveGridStrategyTestCase {

  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected SpatialContext getSpatialContext() {
    return new SimpleSpatialContext();
  }
}
