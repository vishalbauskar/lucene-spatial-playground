package org.apache.lucene.spatial.base;

import junit.framework.TestCase;

import org.apache.lucene.spatial.base.jts.JTSShapeIO;
import org.apache.lucene.spatial.base.simple.SimpleShapeIO;



/**
 */
public class TestSpatialArgs extends TestCase
{
  public void checkSimpleArgs( ShapeIO reader) throws Exception
  {
    String arg = SpatialOperation.IsWithin+"(-10 -20 10 20) cache=true score=false";
    SpatialArgs out = SpatialArgs.parse(arg, reader);
    assertEquals( SpatialOperation.IsWithin, out.op );
    assertTrue( out.cacheable );
    assertFalse( out.calculateScore );
    BBox bounds = (BBox)out.shape;
    assertEquals( -10.0, bounds.getMinX() );
    assertEquals( 10.0, bounds.getMaxX() );

    // Disjoint should not be scored
    arg = SpatialOperation.IsDisjointTo+" (-10 10 -20 20) score=true";
    out = SpatialArgs.parse(arg, reader);
    assertEquals( SpatialOperation.IsDisjointTo, out.op );
    assertFalse( out.calculateScore );

    try {
      SpatialArgs.parse( SpatialOperation.IsDisjointTo+"[ ]", reader);
      fail( "spatial operations need args");
    }
    catch( Exception ex ) {}

    try {
      SpatialArgs.parse("XXXX(-10 10 -20 20)", reader);
      fail( "unknown operation!");
    }
    catch( Exception ex ) { }

    // Check distance
    arg = SpatialOperation.Distance+"(1 2) min=2.3 max=4.5";
    out = SpatialArgs.parse(arg, reader);
    assertEquals( SpatialOperation.Distance, out.op );
    assertTrue( out.shape instanceof Point );
    assertEquals( 2.3, out.min.doubleValue() );
    assertEquals( 4.5, out.max.doubleValue() );
  }

  public void testSimpleArgs() throws Exception
  {
    checkSimpleArgs( new SimpleShapeIO() );
  }

  public void testJTSArgs() throws Exception
  {
    ShapeIO reader = new JTSShapeIO();
    checkSimpleArgs( reader );

    // now check the complex stuff...
  }
}
