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

package com.googlecode.lucene.spatial.base.io.sample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.lucene.spatial.base.context.SpatialContext;
import org.apache.lucene.spatial.base.context.SpatialContextProvider;
import org.apache.lucene.spatial.base.shape.Shape;

import com.googlecode.lucene.spatial.base.context.JtsSpatialContext;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

public class SampleDataWriter {

  protected final PrintWriter out;
  protected final SpatialContext ctx;
  protected final boolean bbox;
  protected final int maxLength;

  public SampleDataWriter(File f, SpatialContext ctx, boolean bbox, int maxLength) throws IOException {
    this.ctx=ctx;
    this.bbox = bbox;
    this.maxLength = maxLength;

    out = new PrintWriter( new OutputStreamWriter(
        new FileOutputStream(f), "UTF8") );

    out.print( "#id" );
    out.print( '\t' );
    out.print( "name" );
    out.print( '\t' );
    out.print( "shape" );
    out.print( '\t' );
    out.println();
    out.flush();
  }

  public SampleDataWriter(File f ) throws IOException {
    this( f, SpatialContextProvider.getContext(), false, -1 );
  }

  public SampleDataWriter(File f, boolean bbox ) throws IOException {
    this( f, SpatialContextProvider.getContext(), bbox, -1 );
  }

  public SampleDataWriter(File f, int maxLength ) throws IOException {
    this( f, SpatialContextProvider.getContext(), false, -1 );
  }


  protected String toString( String name, Shape shape ) {
    String v = ctx.toString( shape );
    if( maxLength > 0 && v.length() > maxLength ) {
      Geometry g = ((JtsSpatialContext)ctx).getGeometryFrom(shape);

      long last = v.length();
      Envelope env = g.getEnvelopeInternal();
      double mins = Math.min(env.getWidth(), env.getHeight());
      double div = 1000;
      while (v.length() > maxLength) {
        double tolerance = mins / div;
        System.out.println( name + " :: Simplifying long geometry: WKT.length=" + v.length() + " tolerance=" + tolerance);
        Geometry simple = TopologyPreservingSimplifier.simplify(g, tolerance);
        v = simple.toText();
        if (v.length() == last) {
          System.out.println( name + " :: Can not simplify geometry smaller then max. " + last);
          break;
        }
        last = v.length();
        div *= .70;
      }
    }
    return v;
  }

  public void write(String id, String name, double x, double y)  throws IOException {
    this.write(id, name, ctx.makePoint(x, y) );
  }

  public void write(String id, String name, Shape shape)  throws IOException {

    String geo = toString( name, bbox?shape.getBoundingBox():shape );
    out.print( id );
    out.print( '\t' );
    out.print( name );
    out.print( '\t' );
    out.print( geo );
    out.print( '\t' );
    out.println();
    out.flush();
  }

  public void close() {
    out.close();
  }
}
