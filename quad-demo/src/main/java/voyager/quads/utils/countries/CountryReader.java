package voyager.quads.utils.countries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import voyager.quads.utils.shapefile.ShapeReader;

import com.vividsolutions.jts.geom.Geometry;

public class CountryReader
{

  public static CountryInfo read( SimpleFeature f )
  {
    CountryInfo c = new CountryInfo();
    c.geometry = (Geometry)f.getAttribute(0);
    c.name = (String)f.getAttribute( 6 );
    c.longName = (String)f.getAttribute( 6 );
    c.fips = (String)f.getAttribute( 1 );
    c.status = (String)f.getAttribute( 12 );
    c.sqKM = (Double)f.getAttribute( 14 );
    c.sqMI = (Double)f.getAttribute( 15 );
    c.population2005 = (Long)f.getAttribute( 13 );


    //0] the_geom :: class com.vividsolutions.jts.geom.MultiPolygon
    //1] FIPS_CNTRY :: class java.lang.String
    //2] GMI_CNTRY :: class java.lang.String
    //3] ISO_2DIGIT :: class java.lang.String
    //4] ISO_3DIGIT :: class java.lang.String
    //5] ISO_NUM :: class java.lang.Integer
    //6] CNTRY_NAME :: class java.lang.String
    //7] LONG_NAME :: class java.lang.String
    //8] ISOSHRTNAM :: class java.lang.String
    //9] UNSHRTNAM :: class java.lang.String
    //10] LOCSHRTNAM :: class java.lang.String
    //11] LOCLNGNAM :: class java.lang.String
    //12] STATUS :: class java.lang.String
    //13] POP2005 :: class java.lang.Long
    //14] SQKM :: class java.lang.Double
    //15] SQMI :: class java.lang.Double
    //16] COLORMAP :: class java.lang.Integer

    return c;
  }

  public static final void main( String[] args ) throws IOException
  {
    File file = new File( "F:/workspace/lucene-spatial/data/countries/cntry06.shp" );

    ShapeReader reader = new ShapeReader( file );
    int cnt = reader.getCount();
    reader.describe( System.out );
    System.out.println( "Count:"+cnt );
    FeatureReader<SimpleFeatureType, SimpleFeature> iter = reader.getFeatures();
    ArrayList<CountryInfo> countries = new ArrayList<CountryInfo>(300);
    while( iter.hasNext() ) {
      SimpleFeature f = iter.next();
      countries.add( CountryReader.read( f ) );
    }
    Collections.sort( countries, CountryInfo.POPULATION_ORDER );
    for( CountryInfo info : countries ) {
      System.out.println( "<option value=\""+info.fips+"\">"+info.name+"</option>" );
    }
    System.out.println( "done." );
  }
}
