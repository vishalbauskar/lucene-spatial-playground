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

package com.googlecode.lucene.spatial.strategy.prefix;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.SpatialTestCase;
import org.apache.lucene.spatial.base.prefix.quad.QuadPrefixTree;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.base.shape.simple.PointImpl;
import org.apache.lucene.spatial.strategy.SimpleSpatialFieldInfo;
import org.apache.lucene.spatial.strategy.prefix.TermQueryPrefixTreeStrategy;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class TestTermQueryGridStrategy extends SpatialTestCase {

  @Test
  public void testNGramPrefixGridLosAngeles() throws IOException {
    final JtsSpatialContext ctx = JtsSpatialContext.GEO_KM;
    final QuadPrefixTree grid = new QuadPrefixTree(ctx);

    SimpleSpatialFieldInfo fieldInfo = new SimpleSpatialFieldInfo("geo");
    TermQueryPrefixTreeStrategy prefixGridStrategy = new TermQueryPrefixTreeStrategy(grid);

    Shape point = new PointImpl(-118.243680, 34.052230);

    Document losAngeles = new Document();
    losAngeles.add(new Field("name", "Los Angeles", StringField.TYPE_STORED));
    losAngeles.add(prefixGridStrategy.createField(fieldInfo, point, true, true));

    addDocumentsAndCommit(Arrays.asList(losAngeles));

    // This won't work with simple spatial context...
    SpatialArgsParser spatialArgsParser = new SpatialArgsParser();

    SpatialArgs spatialArgs = spatialArgsParser.parse(
        "IsWithin(POLYGON((-127.00390625 39.8125,-112.765625 39.98828125,-111.53515625 31.375,-125.94921875 30.14453125,-127.00390625 39.8125)))",
        ctx);

    Query query = prefixGridStrategy.makeQuery(spatialArgs, fieldInfo);
    SearchResults searchResults = executeQuery(query, 1);
    assertEquals(1, searchResults.numFound);
  }
}
