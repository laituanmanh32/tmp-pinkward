package vn.edu.hcmut.ai.tmp_1.minix;

/*
 * base class for minix operator.
 * define some usefull math function.
 */
public class Util
{

     public static final boolean RIGHT = true, AHEAD = true;
     public static final boolean LEFT = false, BACK = false;
	 public static final double MAX_DOUBLE = 999999999; // max num in double

     public static double computeLineHeading( double x1,double y1,double x2,double y2){
	    //  compute line heading through ( x1,y1 ) and ( x2,y2 ),
	    //  from ( x1,y1 ) to the ( x2,y2 ).
		double lineHeading ;
        // special check
        if( x1 == x2 && y1 == y2 ) return 0;
        else if( x1 == x2 ){
	            if( y1 > y2 ) return 180;
	            else return 0;
	    }else if( y1 == y2 ){
		        if( x1> x2 ) return 270;
		        else return 90;
	    }
	    // normal condition
		if( x1 < x2 && y1 < y2 ){
			 lineHeading = Math.toDegrees(Math.atan((x2 - x1)/(y2 - y1)));
	    }else if( x1 < x2 && y1 > y2 ){
			 lineHeading = 90 + Math.toDegrees(Math.atan((y1 - y2)/(x2 - x1)));
		}else if( x1 > x2 && y1 > y2 ){
			 lineHeading = 180 + Math.toDegrees(Math.atan((x1 - x2)/(y1 - y2)));
	    }else {
			 lineHeading = 270 + Math.toDegrees(Math.atan((y2 - y1)/(x1 - x2)));
		}
        return lineHeading;

     } // computeLineHeading

     public static double computeLineDistance( double x1,double y1,double x2,double y2){
         return Math.sqrt(
		    ((x1-x2)*(x1-x2)) +((y1-y2)*(y1-y2)) );
     }

     public static double computeAbsoluteBearing( double heading1, double heading2 ){
		 double bearing = Math.abs( heading1 - heading2 );
		 if( bearing > 180 ) bearing = 360 - bearing;
	     return bearing;
	 }

	 public static double computeRelativeBearing( double heading1, double heading2 ){
        // compute relative bearing, base on heading2
        double bearing = heading1 - heading2 ;
		if( bearing <= 180 && bearing >= 0 ){ // -180 < bearing <= 180
		    // nothing change
		}else if( bearing > 180 ){
			bearing = bearing - 360;
        }else if( bearing < 0 && bearing > -180 ){
	        // nothing change
	    }else if( bearing <= -180 )
	        bearing = 360 + bearing;
        return bearing;
	 }

	 public static double modifyHeading( double heading ){
         if( heading >= 360 ) return modifyHeading( heading - 360 );
		 else if( heading < 0 ) return modifyHeading( heading + 360 );
		 else return heading;
	 }

     //----------------------------------------------------------------
     public static double cos( double degree ){
		 return Math.cos( Math.toRadians( degree ) );
	 }

	 public static double sin( double degree ){
		 return Math.sin( Math.toRadians( degree ) );
	 }

     public static double acos( double cos ){
		 return Math.toDegrees( Math.acos( cos ) );
	 }

     public static double asin( double sin ){
		 return Math.toDegrees( Math.sin( sin ) );
	 }

	 public static double atan( double tan ){
		 return Math.toDegrees( Math.atan( tan ) );
	 }

     //----------------------------------------------

	 public static Coordinate computeCoordinate( double x, double y, double facing, double distance ){
		 double X = x + distance * Util.sin( facing );
		 double Y = y + distance * Util.cos( facing );
		 return new Coordinate( X, Y );
	 }

	 public static TurnInfo computeTurnInfo( double heading1, double heading2 ){
		 // compute turn info , turn from heading1 to heading2
         double bearing = heading1 - heading2;
		 boolean turnDirection;

		 if( 0 <= bearing && bearing <= 180 ){
			    turnDirection = Util.LEFT;
		 }else if( bearing <= -180 ){
			    turnDirection = Util.LEFT;
			    bearing = ( 360 + bearing );
		 }else if( bearing < 0 ){
			    turnDirection = Util.RIGHT;
			    bearing =( -bearing );
		}else {
			    turnDirection = Util.RIGHT;
			    bearing = (360 - bearing);
	    }

        return new TurnInfo( turnDirection, bearing );
	 }

}