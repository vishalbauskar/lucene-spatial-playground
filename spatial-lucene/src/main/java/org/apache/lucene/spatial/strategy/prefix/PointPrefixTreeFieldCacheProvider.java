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

package org.apache.lucene.spatial.strategy.prefix;

import org.apache.lucene.spatial.base.prefix.Node;
import org.apache.lucene.spatial.base.prefix.SpatialPrefixTree;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.strategy.util.ShapeFieldCacheProvider;
import org.apache.lucene.util.BytesRef;

public class PointPrefixTreeFieldCacheProvider extends ShapeFieldCacheProvider<Point> {

  final SpatialPrefixTree grid; //

  public PointPrefixTreeFieldCacheProvider(SpatialPrefixTree grid, String shapeField, int defaultSize) {
    super( shapeField, defaultSize );
    this.grid = grid;
  }

  //A kluge that this is a field
  private Node scanCell = null;

  @Override
  protected Point readShape(BytesRef term) {
    scanCell = grid.getNode(term.bytes, term.offset, term.length, scanCell);
    return scanCell.isLeaf() ? scanCell.getShape().getCenter() : null;
  }
}
