package vn.edu.hcmut.ai.tmp_1.minix;
import robocode.*;

/*
 * base class for minix operator.
 * class Vehicle, define the interfacer that control the vehicle .
 */

public class Vehicle {
     protected AdvancedRobot robot;

	 protected boolean turnDirection ;
	 protected boolean moveDirection ;
     protected double turnDegree ;
	 protected double moveDistance ;

	 protected Vehicle ( AdvancedRobot robot ) {
		this.robot = robot;
	 }

	 protected void moveRun(){
	        if( moveDirection == Util.AHEAD)  robot.setAhead( moveDistance );
	        else  robot.setBack( moveDistance );
	 }

	 protected void turnRun(){
	        if( turnDirection == Util.RIGHT)  robot.setTurnRight( turnDegree );
	        else  robot.setTurnLeft( turnDegree );
	 }

	 protected void moveRun( double distance ){
	        if( moveDirection == Util.AHEAD)  robot.setAhead( distance );
	        else  robot.setBack( distance );
	 }

	 protected void turnRun( double degree ){
	        if( turnDirection == Util.RIGHT)  robot.setTurnRight( degree );
	        else  robot.setTurnLeft( degree );
	 }

	 protected void reset( ){
            robot.setAhead( 0 );
            robot.setTurnRight( 0 );
	 }

} // class vehicle