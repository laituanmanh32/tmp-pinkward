package vn.edu.hcmut.ai.tmp_1_Spring2015.minix;

public class TurnInfo
{
     protected double bearing;
	 protected boolean turnDirection;

	 TurnInfo( boolean turnDirection, double bearing ){
		 this.turnDirection = turnDirection;
		 this.bearing = bearing;
	 }

	 public double getBearing(){ return bearing; }
	 public boolean getDirection(){ return turnDirection; }
}
