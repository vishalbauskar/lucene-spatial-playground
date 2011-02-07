package org.apache.lucene.spatial.quads.gis;

import org.apache.lucene.spatial.quads.IntersectCase;
import org.apache.lucene.spatial.quads.Shape;
import org.apache.lucene.spatial.quads.ShapeExtent;
import org.apache.lucene.spatial.quads.SpatialGrid;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.predicate.RectangleIntersects;

public class JtsGeometry implements Shape
{
  public final Geometry geo;

  public JtsGeometry(Geometry geo)
  {
    this.geo = geo;
  }

  //----------------------------------------
  //----------------------------------------

  @Override
  public JtsEnvelope getExtent() {
    return new JtsEnvelope( geo.getEnvelopeInternal() );
  }

  private GeometryFactory getGeometryFactory( SpatialGrid grid ) {
    if( grid instanceof JtsLinearSpatialGrid ) {
      return ((JtsLinearSpatialGrid)grid).factory;
    }
    return new GeometryFactory();
  }

  @Override
  public IntersectCase intersect(Shape other, SpatialGrid grid)
  {
    ShapeExtent ext = other.getExtent();
    if( !ext.hasSize() ) {
      throw new IllegalArgumentException( "the query shape must cover some area (not a point or line)" );
    }

    // Quick test if this is outside
    Envelope gEnv = geo.getEnvelopeInternal();
    if (ext.getMinX() > gEnv.getMaxX() ||
        ext.getMaxX() < gEnv.getMinX() ||
        ext.getMinY() > gEnv.getMaxY() ||
        ext.getMaxY() < gEnv.getMinY() ){
      return IntersectCase.OUTSIDE;
    }

    Polygon qGeo = null;
    if( other instanceof JtsEnvelope ) {
      Envelope env = ((JtsEnvelope)other).envelope;
      qGeo = (Polygon) getGeometryFactory(grid).toGeometry(env);
    }
    else if( other instanceof JtsGeometry ) {
      qGeo = (Polygon)((JtsGeometry)other).geo;
    }
    else if( other instanceof ShapeExtent ) {
      ShapeExtent e = (ShapeExtent)other;
      Envelope env = new Envelope( e.getMinX(), e.getMaxX(), e.getMinY(), e.getMaxY() );
      qGeo = (Polygon) getGeometryFactory(grid).toGeometry(env);
    }
    else {
      throw new IllegalArgumentException( "this field only support intersectio with Extents or JTS Geometry" );
    }

    //fast algorithm, short-circuit
    if (!RectangleIntersects.intersects (qGeo, geo))
      return IntersectCase.OUTSIDE;

    //slower algorithm
    IntersectionMatrix matrix = geo.relate(qGeo);
    assert ! matrix.isDisjoint();//since rectangle intersection was true, shouldn't be disjoint
    if (matrix.isCovers())
      return IntersectCase.CONTAINS;

    if (matrix.isCoveredBy())
      return IntersectCase.WITHIN;

    assert matrix.isIntersects();
    return IntersectCase.INTERSECTS;
  }
}
