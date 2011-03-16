package org.apache.lucene.spatial.core.grid.jts;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.lucene.spatial.core.BBox;
import org.apache.lucene.spatial.core.Shape;
import org.apache.lucene.spatial.core.grid.LinearSpatialGrid;
import org.apache.lucene.spatial.core.jts.JtsEnvelope;
import org.apache.lucene.spatial.core.jts.JtsGeometry;
import org.apache.lucene.spatial.core.jts.JtsPoint2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

public class JtsLinearSpatialGrid extends LinearSpatialGrid
{
  public GeometryFactory factory = new GeometryFactory();

  public JtsLinearSpatialGrid( double xmin, double xmax, double ymin, double ymax, int maxLevels )
  {
    super( xmin, xmax, ymin, ymax, maxLevels );
  }

  @Override
  protected BBox makeExtent( double xmin, double xmax, double ymin, double ymax )
  {
    return new JtsEnvelope( new Envelope( xmin, xmax, ymin, ymax) );
  }


  @Override
  public Shape readShape(String str) throws IOException {
    if( str.length() < 1 ) {
      throw new RuntimeException( "invalid string" );
    }
    if( !Character.isLetter(str.charAt(0)) ) {
      StringTokenizer st = new StringTokenizer( str, " " );
      double p0 = Double.parseDouble( st.nextToken() );
      double p1 = Double.parseDouble( st.nextToken() );
      if( st.hasMoreTokens() ) {
        double p2 = Double.parseDouble( st.nextToken() );
        double p3 = Double.parseDouble( st.nextToken() );
        return new JtsEnvelope( new Envelope( p0, p2, p1, p3 ) );
      }
      return new JtsPoint2D( factory.createPoint(new Coordinate(p0, p1)) );
    }

    WKTReader reader = new WKTReader(factory);
    try {
      Geometry geo = reader.read( str );
      if( geo instanceof Point ) {
        return new JtsPoint2D((Point)geo);
      }
      return new JtsGeometry( geo );
    }
    catch( com.vividsolutions.jts.io.ParseException ex ) {
      throw new IOException( "error reading shape", ex );
    }
  }
}
