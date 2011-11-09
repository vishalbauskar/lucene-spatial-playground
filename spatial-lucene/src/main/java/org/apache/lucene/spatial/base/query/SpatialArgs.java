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

package org.apache.lucene.spatial.base.query;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.simple.SimpleSpatialContext;
import org.apache.lucene.spatial.base.distance.DistanceUnits;
import org.apache.lucene.spatial.base.exception.InvalidSpatialArgument;
import org.apache.lucene.spatial.base.shape.Shape;

public class SpatialArgs {

  public static final double DEFAULT_DIST_PRECISION = 0.025d;

  private SpatialOperation operation;
  private Shape shape;
  private double distPrecision = DEFAULT_DIST_PRECISION;

  // Useful for 'distance' calculations
  private Double min;
  private Double max;

  public SpatialArgs(SpatialOperation operation) {
    this.operation = operation;
  }

  public SpatialArgs(SpatialOperation operation, Shape shape) {
    this.operation = operation;
    this.shape = shape;
  }

  /**
   * Check if the arguments make sense -- throw an exception if not
   */
  public void validate() throws InvalidSpatialArgument {
    if (operation.isTargetNeedsArea() && !shape.hasArea()) {
      throw new InvalidSpatialArgument(operation + " only supports geometry with area");
    }
  }

  public String toString( SpatialContext context ) {
    StringBuilder str = new StringBuilder();
    str.append( operation.getName() ).append( '(' );
    str.append( context.toString( shape ) );
    if( min != null ) {
      str.append(" min=").append(min);
    }
    if( max != null ) {
      str.append(" max=").append(max);
    }
    str.append(" distPrec=").append(String.format("%.2f%%", distPrecision/100d));
    str.append( ')' );
    return str.toString();
  }

  @Override
  public String toString()
  {
    return toString( new SimpleSpatialContext(DistanceUnits.KILOMETERS) );
  }

  //------------------------------------------------
  // Getters & Setters
  //------------------------------------------------

  public SpatialOperation getOperation() {
    return operation;
  }

  public void setOperation(SpatialOperation operation) {
    this.operation = operation;
  }

  /**
   * Considers {@link SpatialOperation#BBoxWithin} in returning the shape.
   * @return
   */
  public Shape getShape() {
    if (shape != null && (operation == SpatialOperation.BBoxWithin || operation == SpatialOperation.BBoxIntersects))
      return shape.getBoundingBox();
    return shape;
  }

  public void setShape(Shape shape) {
    this.shape = shape;
  }

  /**
   * The fraction of the distance from the center of the query shape to its nearest edge that is considered acceptable
   * error. The algorithm for computing the distance to the nearest edge is actually a little different. It normalizes
   * the shape to a square given it's bounding box area:
   * <pre>sqrt(shape.bbox.area)/2</pre>
   * And the error distance is beyond the shape such that the shape is a minimum shape.
   */
  public Double getDistPrecision() {
    return distPrecision;
  }

  public void setDistPrecision(Double distPrecision) {
    if (distPrecision != null)
      this.distPrecision = distPrecision;
  }

  public Double getMin() {
    return min;
  }

  public void setMin(Double min) {
    this.min = min;
  }

  public Double getMax() {
    return max;
  }

  public void setMax(Double max) {
    this.max = max;
  }
}
