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

package org.apache.lucene.spatial.strategy.vector;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.DoubleParser;
import org.apache.lucene.spatial.base.distance.DistanceCalculator;
import org.apache.lucene.spatial.base.shape.Point;
import org.apache.lucene.spatial.base.shape.simple.PointImpl;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.util.Map;

/**
 *
 * An implementation of the Lucene ValueSource model to support spatial relevance ranking.
 *
 */
public class DistanceValueSource extends ValueSource {

  private final TwoDoublesFieldInfo fields;
  private final DistanceCalculator calculator;
  private final Point from;
  private final DoubleParser parser;

  /**
   * Constructor.
   */
  public DistanceValueSource(Point from, DistanceCalculator calc, TwoDoublesFieldInfo fields, DoubleParser parser) {
    this.from = from;
    this.fields = fields;
    this.calculator = calc;
    this.parser = parser;
  }

  /**
   * Returns the ValueSource description.
   */
  @Override
  public String description() {
    return "DistanceValueSource("+calculator+")";
  }


  /**
   * Returns the FunctionValues used by the function query.
   */
  @Override
  public FunctionValues getValues(Map context, AtomicReaderContext readerContext) throws IOException {
    AtomicReader reader = readerContext.reader();

    final double[] ptX = FieldCache.DEFAULT.getDoubles(reader, fields.getFieldNameX(), true);
    final double[] ptY = FieldCache.DEFAULT.getDoubles(reader, fields.getFieldNameY(), true);
    final Bits validX =  FieldCache.DEFAULT.getDocsWithField(reader, fields.getFieldNameX());
    final Bits validY =  FieldCache.DEFAULT.getDocsWithField(reader, fields.getFieldNameY());

    return new FunctionValues() {
      @Override
      public float floatVal(int doc) {
        return (float) doubleVal(doc);
      }

      @Override
      public double doubleVal(int doc) {
        // make sure it has minX and area
        if (validX.get(doc) && validY.get(doc)) {
          PointImpl pt = new PointImpl( ptX[doc],  ptY[doc] );
          return calculator.distance(from, pt);
        }
        return 0;
      }

      @Override
      public String toString(int doc) {
        return description() + "=" + floatVal(doc);
      }
    };
  }

  /**
   * Determines if this ValueSource is equal to another.
   * @param obj the ValueSource to compare
   * @return <code>true</code> if the two objects are based upon the same query envelope
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) { return false; }
    if (obj == this) { return true; }
    if (obj.getClass() != getClass()) {
      return false;
    }
    DistanceValueSource rhs = (DistanceValueSource) obj;
    return new EqualsBuilder()
                  .append(calculator, rhs.calculator)
                  .append(from, rhs.from)
                  .append(fields, rhs.fields)
                  .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(59, 7).
        append(calculator).
        append(from).
        append(fields).
        toHashCode();
  }
}
