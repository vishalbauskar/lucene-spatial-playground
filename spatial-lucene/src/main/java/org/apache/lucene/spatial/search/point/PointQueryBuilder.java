/**
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

package org.apache.lucene.spatial.search.point;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.ValueSource;
import org.apache.lucene.search.function.ValueSourceQuery;
import org.apache.lucene.spatial.base.shape.BBox;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.spatial.search.SpatialArgs;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.distance.EuclidianDistanceCalculator;
import org.apache.lucene.spatial.base.shape.simple.Rectangle;
import org.apache.lucene.spatial.search.SpatialQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PointQueryBuilder implements SpatialQueryBuilder<PointFieldInfo> {
  static final Logger log = LoggerFactory.getLogger( PointQueryBuilder.class );

  @Override
  public Fieldable[] createFields(PointFieldInfo field, Shape shape, boolean index, boolean store) {
    throw new RuntimeException( "implemented by solr now..." );
  }

  @Override
  public ValueSource makeValueSource(SpatialArgs args, PointFieldInfo fields)
  {
    DistanceCalculator calc = new EuclidianDistanceCalculator();
    if (Point.class.isInstance(args.getShape())) {
      DistanceValueSource dvs = new DistanceValueSource(((Point)args.getShape()), calc, fields);
      if (args.getMin() != null) {
        dvs.min = args.getMin();
      }
      if (args.getMax() != null ) {
        dvs.max = args.getMax();
      }
    }
    throw new UnsupportedOperationException( "score only works with point or radius (for now)" );
  }

  @Override
  public Query makeQuery(SpatialArgs args, PointFieldInfo fields) {
    // For starters, just limit the bbox
    BBox bbox = args.getShape().getBoundingBox();
    if (bbox.getCrossesDateLine()) {
      throw new UnsupportedOperationException( "Crossing dateline not yet supported" );
    }

    PointQueryHelper helper = new PointQueryHelper(bbox,fields);
    Query spatial = null;
    switch (args.getOperation()) {
      case BBoxIntersects: spatial = helper.makeWithin(); break;
      case BBoxWithin: spatial =  helper.makeWithin(); break;
      case Contains: spatial =  helper.makeWithin(); break;
      case Intersects: spatial =  helper.makeWithin(); break;
      case IsWithin: spatial =  helper.makeWithin(); break;
      case Overlaps: spatial =  helper.makeWithin(); break;
      case IsDisjointTo: spatial =  helper.makeDisjoint(); break;
      case Distance: {
        if (args.getMax() == null) {
          // no bbox to limit
          return new ValueSourceQuery( makeValueSource( args, fields ) );
        }
        if (Point.class.isInstance(args.getShape())) {
          // first make a BBox Query
          Point p = (Point) args.getShape();
          double r = args.getMax();
          helper.queryExtent = new Rectangle(p.getX() - r, p.getX() + r, p.getY() - r, p.getY() + r);
          spatial =  helper.makeWithin(); break;
        }
        throw new IllegalArgumentException( "Distance only works with point (on point fields)" );
      }

      default:
        throw new UnsupportedOperationException(args.getOperation().name());
    }

    if (args.isCalculateScore()) {
      try {
        Query spatialRankingQuery = new ValueSourceQuery( makeValueSource( args, fields ) );
        BooleanQuery bq = new BooleanQuery();
        bq.add(spatial,BooleanClause.Occur.MUST);
        bq.add(spatialRankingQuery,BooleanClause.Occur.MUST);
        return bq;
      } catch(Exception ex) {
        log.warn( "error making score", ex );
      }
    }
    return spatial;
  }
}


class PointQueryHelper
{
  BBox queryExtent;
  PointFieldInfo field;

  public PointQueryHelper( BBox bbox, PointFieldInfo field )
  {
    this.queryExtent = bbox;
    this.field = field;
  }

  //-------------------------------------------------------------------------------
  //
  //-------------------------------------------------------------------------------

  /**
   * Constructs a query to retrieve documents that fully contain the input envelope.
   * @return the spatial query
   */
  Query makeWithin()
  {
    Query qX = NumericRangeQuery.newDoubleRange(field.fieldX,field.precisionStep,queryExtent.getMinX(),queryExtent.getMaxX(),true,true);
    Query qY = NumericRangeQuery.newDoubleRange(field.fieldY,field.precisionStep,queryExtent.getMinY(),queryExtent.getMaxY(),true,true);

    BooleanQuery bq = new BooleanQuery();
    bq.add(qX,BooleanClause.Occur.MUST);
    bq.add(qY,BooleanClause.Occur.MUST);
    return bq;
  }


  /**
   * Constructs a query to retrieve documents that fully contain the input envelope.
   * @return the spatial query
   */
  Query makeDisjoint()
  {
    Query qX = NumericRangeQuery.newDoubleRange(field.fieldX,field.precisionStep,queryExtent.getMinX(),queryExtent.getMaxX(),true,true);
    Query qY = NumericRangeQuery.newDoubleRange(field.fieldY,field.precisionStep,queryExtent.getMinY(),queryExtent.getMaxY(),true,true);

    BooleanQuery bq = new BooleanQuery();
    bq.add(qX,BooleanClause.Occur.MUST_NOT);
    bq.add(qY,BooleanClause.Occur.MUST_NOT);
    return bq;
  }
}




