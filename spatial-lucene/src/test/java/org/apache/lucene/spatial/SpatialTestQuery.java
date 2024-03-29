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
import org.apache.lucene.spatial.base.io.LineReader;
import org.apache.lucene.spatial.base.query.SpatialArgs;
import org.apache.lucene.spatial.base.query.SpatialArgsParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Helper class to execute queries
 */
public class SpatialTestQuery {
  public String testname;
  public String line;
  public int lineNumber = -1;
  public SpatialArgs args;
  public List<String> ids = new ArrayList<String>();

  /**
   * Get Test Queries
   */
  public static Iterator<SpatialTestQuery> getTestQueries(
      final SpatialArgsParser parser,
      final SpatialContext ctx,
      final String name,
      final InputStream in ) throws IOException {
    return new LineReader<SpatialTestQuery>(new InputStreamReader(in,"UTF-8")) {

      @Override
      public SpatialTestQuery parseLine(String line) {
        SpatialTestQuery test = new SpatialTestQuery();
        test.line = line;
        test.lineNumber = getLineNumber();

        try {
          // skip a comment
          if( line.startsWith( "[" ) ) {
            int idx = line.indexOf( ']' );
            if( idx > 0 ) {
              line = line.substring( idx+1 );
            }
          }

          int idx = line.indexOf('@');
          StringTokenizer st = new StringTokenizer(line.substring(0, idx));
          while (st.hasMoreTokens()) {
            test.ids.add(st.nextToken().trim());
          }
          test.args = parser.parse(line.substring(idx + 1).trim(), ctx);
          return test;
        }
        catch( Exception ex ) {
          throw new RuntimeException( "invalid query line: "+test.line, ex );
        }
      }
    };
  }

  @Override
  public String toString() {
    return line;
  }
}
