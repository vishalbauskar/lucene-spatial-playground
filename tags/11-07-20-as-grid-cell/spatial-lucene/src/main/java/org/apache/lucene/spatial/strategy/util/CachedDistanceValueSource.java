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

package org.apache.lucene.spatial.strategy.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.lucene.index.IndexReader.AtomicReaderContext;
import org.apache.lucene.queries.function.DocValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.shape.Point;

/**
 *
 * An implementation of the Lucene ValueSource model to support spatial relevance ranking.
 *
 */
public class CachedDistanceValueSource extends ValueSource {

  private final ShapeFieldCacheProvider<Point> provider;
  private final DistanceCalculator calculator;
  private final Point from;

  /**
   * Constructor.
   * @param queryEnvelope the query envelope
   * @param queryPower the query power (scoring algorithm)
   * @param targetPower the target power (scoring algorithm)
   */
  public CachedDistanceValueSource(Point from, DistanceCalculator calc, ShapeFieldCacheProvider<Point> provider) {
    this.from = from;
    this.provider = provider;
    this.calculator = calc;
  }

  /**
   * Returns the ValueSource description.
   * @return the description
   */
  @Override
  public String description() {
    return "DistanceValueSource("+calculator+")";
  }


  /**
   * Returns the DocValues used by the function query.
   * @param reader the index reader
   * @return the values
   */
  @Override
  public DocValues getValues(Map context, AtomicReaderContext readerContext) throws IOException {
    final ShapeFieldCache<Point> cache =
      provider.getCache(readerContext.reader);

    return new DocValues() {
      @Override
      public float floatVal(int doc) {
        return (float) doubleVal(doc);
      }

      @Override
      public double doubleVal(int doc) {
        List<Point> vals = cache.getShapes( doc );
        if( vals != null ) {
          double v = calculator.calculate(from, vals.get(0));
          if( vals.size() > 1 ) {
            for( int i=1; i<vals.size(); i++ ) {
              v = Math.min(v, calculator.calculate(from, vals.get(i)));
            }
          }
          return v;
        }
        return Double.NaN; // ?? maybe max?
      }

      @Override
      public String toString(int doc) {
        return description() + "=" + floatVal(doc);
      }
    };
  }

  /**
   * Determines if this ValueSource is equal to another.
   * @param o the ValueSource to compare
   * @return <code>true</code> if the two objects are based upon the same query envelope
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) { return false; }
    if (obj == this) { return true; }
    if (obj.getClass() != getClass()) {
      return false;
    }
    CachedDistanceValueSource rhs = (CachedDistanceValueSource) obj;
    return new EqualsBuilder()
                  .append(calculator, rhs.calculator)
                  .append(from, rhs.from)
                  .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(31, 97).
        append(calculator).
        append(from).
        toHashCode();
  }
}