package vn.edu.hcmut.ai.tmp_1.minixHT.vehicleHT;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import vn.edu.hcmut.ai.tmp_1.minix.Util;
import vn.edu.hcmut.ai.tmp_1.minix.Vehicle;
import vn.edu.hcmut.ai.tmp_1.minixHT.MinixHT;


/**
 * abstract class, define the interface that vehicle use by MinixHT should inherit
 */
public abstract class VehicleHT extends Vehicle
{
	MinixHT operator;

	int vehicleState;
	// state velue
	final int STOP = 0;
	final int MOVE = 1;
	// used by multi mode
	final int CHANGE_RADIUS = 2;
	final int AWAY_ROBOT = 3;

    final double MAX_RADIUS_OFFSET = 25;
	final double RESERVE_DISTANCE = 50;

    // O point info
	final double R; // radius rerative battle field
	final double oX, oY; // center coordinate of battle field
    // the bearing to the O point relative to my robot's facing
	double oBearing;
	// heading from my robot to the O point
    double oLineHeading;
	// distance from my robot to the O point
	double oLineDistance;

	// centre info relative my move
	double centreX ;  // center coordinate relative my move.
	double centreY ;
	// the bearing to the center point relative to my robot's facing
	double centreBearing;
	// heading from my robot to the center point
	double centreLineHeading;
	// distance from my robot to the center point
	double centreLineDistance;

	// in uni mode, centreLineHeading ->preferHeading
	// in multi mode, oLineHeading ->preferHeading
    double preferHeading;
	// centreLineDistance should -> preferRadius
	double preferRadius ;
    // others
	double time, myX, myY, velocity;

	VehicleHT( MinixHT operator,AdvancedRobot robot) {
		super( robot );
		this.operator = operator;
		oX = robot.getBattleFieldWidth()/2;
		oY = robot.getBattleFieldHeight()/2;
		R = Math.min(oX, oY) - RESERVE_DISTANCE;
    }

	void update( ){
		// must update centre coordinate relative my move before
		time = robot.getTime();
		myX = robot.getX();
		myY = robot.getY();
		velocity = robot.getVelocity();

		oLineHeading = Util.computeLineHeading( myX, myY, oX, oY );
		oLineDistance = Util.computeLineDistance( myX, myY, oX, oY );
        oBearing = Util.computeRelativeBearing( oLineHeading, robot.getHeading());

        centreLineHeading = Util.computeLineHeading( myX, myY, centreX, centreY );
		centreLineDistance = Util.computeLineDistance( myX, myY, centreX, centreY );
		centreBearing = Util.computeRelativeBearing(
			                 centreLineHeading, robot.getHeading()	);
	}

    // ----------- interface -----------------
	public abstract void onMonitor();

	public void onTurnComplete(){}
	public void onMoveComplete(){}
	public void onFinish(){}
	public void onHitByBullet( HitByBulletEvent e ){}
	public void onScannedRobot( ScannedRobotEvent event ){}

	public void onHitWall( HitWallEvent event ){
	    if( Math.abs( event.getBearing() )< 90 ) moveDirection = Util.BACK;
		else moveDirection = Util.AHEAD;
		moveRun( 100 );
	}

	public void onHitRobot( HitRobotEvent event ){
	    if( Math.abs( event.getBearing() )< 90 ) moveDirection = Util.BACK;
		else moveDirection = Util.AHEAD;
		moveRun( 100 );
	}

	// ----------------- tool function --------------------
	void computeTurnInfoForKeepRadius( ){
	    // keep |centreBearing| -> 90 .
	    if( -90 < centreBearing && centreBearing<=0){
			  turnDegree = 90 + centreBearing;
		      turnDirection = Util.RIGHT;
		}else if( -180 < centreBearing && centreBearing <=-90){
			  turnDegree = -90 -centreBearing;
		      turnDirection = Util.LEFT;
		}else if( 0<centreBearing && centreBearing< 90){
			  turnDegree = 90 -centreBearing;
		      turnDirection = Util.LEFT;
		}else if( 90 <= centreBearing && centreBearing <=180){
			  turnDegree = -90 + centreBearing;
		      turnDirection = Util.RIGHT;
	    }
    } // computeTurnInfoForKeepRadius

    void computeMoveDirectionToPreferHeading( ){
		double lineHeading;
		double bearing;
		if( robot.getOthers()>1 ){
			lineHeading = oLineHeading;
			bearing = oBearing;
		}else{
			lineHeading = centreLineHeading;
			bearing = centreBearing;
		}
        double tempBearing = preferHeading - lineHeading;
		if( bearing > 0 ){
			if( tempBearing >=0 && tempBearing < 180 )
			       moveDirection = Util.AHEAD;
			else if( tempBearing < -180 ) moveDirection = Util.AHEAD;
		    else moveDirection = Util.BACK;
	    }else{
			if( tempBearing < 0 && tempBearing >= -180 )
			       moveDirection = Util.AHEAD;
			else if( tempBearing > 180 )  moveDirection = Util.AHEAD;
			else moveDirection = Util.BACK;
	    }
    } // computeMoveDirectionToPreferHeading

    void computeInfoForMoveToRadius( double moveDegree , double radiusToMove ){
		double tempDegree;
        if( -90 < centreBearing && centreBearing<=0 ){
		      tempDegree = 90 + centreBearing;
		      turnDegree = ( moveDegree - tempDegree );
		      turnDirection = Util.LEFT;
	          if( centreLineDistance > radiusToMove )
		        moveDirection = Util.AHEAD;
		      else  moveDirection = Util.BACK;
	     }else if( -180 < centreBearing && centreBearing <=-90 ){
			 tempDegree = - 90 - centreBearing;
			 turnDegree = ( moveDegree - tempDegree );
		     turnDirection = Util.RIGHT;
		     if(centreLineDistance > radiusToMove )
		        moveDirection = Util.BACK;
		     else  moveDirection = Util.AHEAD;
	     }else if( 0<centreBearing && centreBearing< 90){
			 tempDegree = 90 -centreBearing;
			 turnDegree = ( moveDegree - tempDegree);
		     turnDirection = Util.RIGHT;
		     if(centreLineDistance > radiusToMove )
		        moveDirection = Util.AHEAD;
		     else  moveDirection = Util.BACK;
	     }else if( 90 <= centreBearing && centreBearing <=180){
			 tempDegree = -90 + centreBearing;
			 turnDegree = ( moveDegree - tempDegree );
		     turnDirection = Util.LEFT;
		     if(centreLineDistance > radiusToMove )
		        moveDirection = Util.BACK;
		     else  moveDirection = Util.AHEAD;
	     }

	} // computeInfoForMoveToPreferRadius

    void computeInfoForMoveToRadius( boolean preferDirection, double moveDegree, double radiusToMove ){
		 computeInfoForMoveToRadius( moveDegree , radiusToMove );
		 if( preferDirection != moveDirection ){ // modify turn info
			turnDirection = !turnDirection;
			turnDegree = 2*moveDegree - turnDegree;
            moveDirection = preferDirection;
		 }
	} // computeInfoForMoveToPreferRadius

    void computeInfoForMoveToRadius( boolean preferDirection, double radiusToMove ){
		 double offset = Math.abs( radiusToMove - centreLineDistance );
		 double moveDegree = 20*( (int)offset/MAX_RADIUS_OFFSET );
		 moveDegree = Math.min( moveDegree, 45 );
         computeInfoForMoveToRadius( preferDirection, moveDegree, radiusToMove );
	}

    // ---------------------- tool function ----------------------
	public int getState(){ return vehicleState; }

}// class vehicleHT