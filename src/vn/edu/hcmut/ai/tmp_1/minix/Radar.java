package vn.edu.hcmut.ai.tmp_1.minix;
import robocode.*;

/*
 * base class for minix operator.
 * class Radar, define the interfacer that control the radar .
 */

public class Radar {
     protected AdvancedRobot robot;
	 protected boolean turnDirection;
	 protected double turnDegree;

	 protected Radar ( AdvancedRobot robot ){
		 this.robot = robot;
	 }

	 // ----------------------- tool function ------------------
	 protected void computeTurnInfo( double lineHeading ){
		    double radarHeading = robot.getRadarHeading();
		    TurnInfo info = Util.computeTurnInfo( radarHeading ,lineHeading );
			turnDirection = info.getDirection();
	        turnDegree = info.getBearing();
	 } // turn to lineHeading

	 protected void track( double lineHeading ){
		    computeTurnInfo( lineHeading );
			turnDegree = turnDegree * 1.2 + 2;
			run();
	 }

	 protected void scan( double degree ){
	        turnDegree = degree;
	        run( );
	 }

	 protected void scan( double degree , boolean direction ){
	        turnDegree = degree;
	        turnDirection = direction;
	        run( );
	 } //scan

	 protected void run( ){
		    if( turnDirection == Util.RIGHT )
			     robot.setTurnRadarRight( turnDegree );
			else robot.setTurnRadarLeft( turnDegree );
			//robot.execute( );
	 } // run

} // class radar



