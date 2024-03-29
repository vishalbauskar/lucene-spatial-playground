/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.spatial;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;
import org.apache.lucene.spatial.base.query.SpatialOperation;
import org.apache.lucene.spatial.base.shape.Rectangle;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Make sure we are reading the tests as expected
 */
public class TestTestFramework {

  @Test
  public void testQueries() throws IOException {
    String name = StrategyTestCase.QTEST_Cities_IsWithin_BBox;

    InputStream in = getClass().getClassLoader().getResourceAsStream(name);
    SpatialContext ctx = SimpleSpatialContext.GEO_KM;
    Iterator<SpatialTestQuery> iter = SpatialTestQuery.getTestQueries(
        new SpatialArgsParser(), ctx, name, in );
    List<SpatialTestQuery> tests = new ArrayList<SpatialTestQuery>();
    while( iter.hasNext() ) {
      tests.add( iter.next() );
    }
    Assert.assertEquals( 3, tests.size() );

    SpatialTestQuery sf = tests.get(0);
   // assert
    Assert.assertEquals( 1, sf.ids.size() );
    Assert.assertTrue( sf.ids.get(0).equals( "G5391959" ) );
    Assert.assertTrue( sf.args.getShape() instanceof Rectangle);
    Assert.assertEquals( SpatialOperation.IsWithin, sf.args.getOperation() );
  }

}
