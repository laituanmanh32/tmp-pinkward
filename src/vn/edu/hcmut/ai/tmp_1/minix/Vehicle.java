package vn.edu.hcmut.ai.tmp_1.minix;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

/*
 * base class for minix operator.
 * class Vehicle, define the interfacer that control the vehicle .
 */

public class Vehicle {
	protected AdvancedRobot robot;

	protected boolean turnDirection;
	protected boolean moveDirection;
	protected double turnDegree;
	protected double moveDistance;

	Minix operator;

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
	final double R; // radius relative battle field
	final double oX, oY; // center coordinate of battle field
	// the bearing to the O point relative to my robot's facing
	double oBearing;
	// heading from my robot to the O point
	double oLineHeading;
	// distance from my robot to the O point
	double oLineDistance;

	// center info relative my move
	double centreX; // center coordinate relative my move.
	double centreY;
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
	double preferRadius;
	// others
	double time, myX, myY, velocity;

	// for debug
	private final boolean START_DEBUG = false;
	private final boolean SWITCH_DEBUG = false;

	private final double AHEAD_TIME = 8;
	private Enemy enemy;

	private double maxBearing;
	private double radiusToMove; // radius my robot will move to
	private double startTime;

	public Vehicle(Minix operator, AdvancedRobot robot) {
		this.robot = robot;
		this.operator = operator;
		oX = robot.getBattleFieldWidth() / 2;
		oY = robot.getBattleFieldHeight() / 2;
		R = Math.min(oX, oY) - RESERVE_DISTANCE;
		vehicleInit();
	}

	private void vehicleInit(){
		robot.setMaxVelocity(8);
	    preferRadius = 350;
		stop();
	}

	protected void moveRun() {
		if (moveDirection == Util.AHEAD)
			robot.setAhead(moveDistance);
		else
			robot.setBack(moveDistance);
	}

	protected void turnRun() {
		if (turnDirection == Util.RIGHT)
			robot.setTurnRight(turnDegree);
		else
			robot.setTurnLeft(turnDegree);
	}

	protected void moveRun(double distance) {
		if (moveDirection == Util.AHEAD)
			robot.setAhead(distance);
		else
			robot.setBack(distance);
	}

	protected void turnRun(double degree) {
		if (turnDirection == Util.RIGHT)
			robot.setTurnRight(degree);
		else
			robot.setTurnLeft(degree);
	}

	protected void reset() {
		robot.setAhead(0);
		robot.setTurnRight(0);
	}


	void updateSelf( ){
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
	void updateEnemy(){
		if( enemy == null ){
            centreX = oX;
		    centreY = oY;
		}else{
		    centreX = enemy.getX();
		    centreY = enemy.getY();
		}
        updateSelf();
        // compute radius to move and max bearing
		double centreODistance = Util.computeLineDistance( oX, oY, centreX, centreY );
		if( centreODistance + preferRadius <= R ){
			radiusToMove = preferRadius;
			maxBearing = 1000;
		}else if( centreODistance <= R/4 ){
			centreX = oX;
		    centreY = oY;
			updateSelf();
            radiusToMove = R;
			maxBearing = 1000;
		}else{
			double temp = R - centreODistance;
            double distance = temp + preferRadius;

			if( distance < 3*R/4 ) radiusToMove = 3*R/4 - temp;
			else if( distance > 7*R/4 ) radiusToMove = 7*R/4 - temp;
			else radiusToMove = preferRadius;

			double cos = ( centreODistance*centreODistance + radiusToMove*radiusToMove - R*R )/
				         ( 2*centreODistance*radiusToMove );
			maxBearing = Util.acos( cos );
		}
		preferHeading =
		    Util.computeLineHeading( oX, oY, centreX , centreY );
	}

	public void onScannedRobot( ScannedRobotEvent event ){
	    if( enemy == null ) enemy = operator.getEnemy( event.getName() );
	}

	public void onMonitor(){ // control the vehicles action
        updateEnemy();
		switch( vehicleState ){
			case MOVE:
				moveMonitor();
			    break;
            case STOP:
				stopMonitor();
			    break;
	    } //switch
	}

	// ------------------------ function for move -------------------------
	private void move( ){ // begin move
        if( START_DEBUG ){
		    robot.out.println("totall stop time : "+ ( time-startTime ) +"\n");
			robot.out.println("start move : " + time );
		}
		vehicleState = MOVE;
		startTime = time;
		moveInit();
    }

    private void moveInit( ){ // begin swing move
	    double aheadX = myX + 32 * Util.sin( robot.getHeading() );
		double aheadY = myY + 32 * Util.cos( robot.getHeading() );
		double aheadCount = computeCount( aheadX, aheadY, AHEAD_TIME );

		aheadX = myX + -32 * Util.sin( robot.getHeading() );
		aheadY = myY + -32 * Util.cos( robot.getHeading() );
		double backCount = computeCount( aheadX, aheadY, AHEAD_TIME );

		double bearing =
			 Util.computeAbsoluteBearing( preferHeading, centreLineHeading );
		if( bearing >= 3*maxBearing/4 ){
			if( SWITCH_DEBUG ) robot.out.println("move to prefer heading.");
			computeMoveDirectionToPreferHeading();
		}else if( aheadCount > backCount ){
			if( SWITCH_DEBUG ) robot.out.println("move ahead. "
			                   + aheadCount +", "+ backCount );
			moveDirection = Util.AHEAD;
        }else{
			if( SWITCH_DEBUG ) robot.out.println("move back. "
			                   + aheadCount +", "+ backCount );
			moveDirection = Util.BACK;
		}
    }

    private void moveMonitor(){
        if( moveContinue() ){
			double bearing = Util.computeAbsoluteBearing(
				   preferHeading, centreLineHeading );
		    if( bearing >= maxBearing ){
                 if( oLineDistance > R )
			        computeInfoForMoveToRadius( moveDirection, 45, 0 );
			     else computeInfoForMoveToRadius( moveDirection, 30, radiusToMove );
		    }else if( oLineDistance > R )
			     computeInfoForMoveToRadius( moveDirection, 45, radiusToMove );
		    else if( centreLineDistance < radiusToMove/2 )
			     computeInfoForMoveToRadius( moveDirection, 45, radiusToMove );
		    else if( Math.abs( centreLineDistance - radiusToMove ) > MAX_RADIUS_OFFSET )
			     computeInfoForMoveToRadius( moveDirection, 30 , radiusToMove );
            else computeTurnInfoForKeepRadius();
			turnRun();
			moveRun( 10000 );
		}else stop();
	}

	private boolean moveContinue(){
		if( Math.abs( velocity ) < 8 ) return true;

		double bearing = Util.computeAbsoluteBearing(
			             preferHeading, centreLineHeading );
		if( bearing >= maxBearing ){
			if( SWITCH_DEBUG ) robot.out.println("beyond the max bearing.");
            computeMoveDirectionToPreferHeading();
			return true;
		}

        double aheadX = myX + velocity * Util.sin( robot.getHeading() ) * AHEAD_TIME;
		double aheadY = myY + velocity * Util.cos( robot.getHeading() ) * AHEAD_TIME;
		double moveCount = computeCount( aheadX, aheadY, AHEAD_TIME );
		double stopCount = computeCount( myX, myY, AHEAD_TIME );
		if( moveCount < stopCount && time - startTime > 20 ){
			if( SWITCH_DEBUG ) robot.out.println("better to change direction.");
			return false;
		}else if( computeMinDistance( aheadX, aheadY, AHEAD_TIME ) < 50 ){
            if( SWITCH_DEBUG ) robot.out.println("bullet is too close.");
			return false;
		}else return true;
	}

    // ------------------------ function for stop -------------------------
    private void stop( ){
		if( START_DEBUG ){
			robot.out.println("move direction: " + moveDirection );
			robot.out.println("totall move time: " + ( time-startTime ) +"\n");
			robot.out.println("start stop : " + time );
		}
		reset( );
		vehicleState = STOP;
		startTime = time;
    }

    private void stopMonitor(){
		if( stopContinue() ){
            computeTurnInfoForKeepRadius( );
		    turnRun( );
		}else move();
	}

    private boolean stopContinue(){
        if( Math.abs( velocity )>0 ) return true;
        else if( enemy == null ) return true;

		double aheadX = myX + 32 * Util.sin( robot.getHeading() );
		double aheadY = myY + 32 * Util.cos( robot.getHeading() );
		double aheadCount = computeCount( aheadX, aheadY, AHEAD_TIME );

		aheadX = myX + -32 * Util.sin( robot.getHeading() );
		aheadY = myY + -32 * Util.cos( robot.getHeading() );
		double backCount = computeCount( aheadX, aheadY, AHEAD_TIME );

		double stopCount = computeCount( myX, myY, AHEAD_TIME );

        if( stopCount < aheadCount || stopCount < backCount ){
			if( SWITCH_DEBUG ) robot.out.println("better to move.");
			return false;
        }else if( computeMinDistance( aheadX, aheadY, 12 ) < 50 ){
            if( SWITCH_DEBUG ) robot.out.println("bullet is too close.");
			return false;
		}else return true;
	}

	// --------------------------------------------------------------------
	public void onHitByBullet( HitByBulletEvent e ){

		if( vehicleState == STOP ) move();
		else if( Math.random() < 0.5 ) moveDirection = ! moveDirection;

	}

	//-------------------------------------------------------------------------------
	private double computeCount( double aheadX, double aheadY, double aheadTime ){
        if( enemy == null ) return Util.MAX_DOUBLE;
        Bullet[] bullets = enemy.getBullets();
		if( bullets == null ) return Util.MAX_DOUBLE;
		double count = 0;
        for( int i=0; i< bullets.length; i++ ){
			 Bullet bullet = bullets[i];
			 double bx = bullet.fireX;
			 double by = bullet.fireY;
			 bx = bx + ( 20-3*bullet.power ) * Util.sin( bullet.heading )
				       * ( aheadTime + time - bullet.fireTime );
		     by = by + ( 20-3*bullet.power ) * Util.cos( bullet.heading )
				       * ( aheadTime + time - bullet.fireTime );
			 count += Util.computeLineDistance( aheadX, aheadY, bx, by );
		}
		return count;
	}

	private double computeMinDistance( double aheadX, double aheadY, double aheadTime ){
		if( enemy == null ) return Util.MAX_DOUBLE;
		Bullet[] bullets = enemy.getBullets();
		if( bullets == null ) return Util.MAX_DOUBLE;

		double min = Util.MAX_DOUBLE;
        for( int i=0; i< bullets.length; i++ ){
		    Bullet bullet = bullets[i];
		    double bx = bullet.fireX;
			double by = bullet.fireY;
			bx = bx + (20-3*bullet.power)*Util.sin( bullet.heading)
				       *( aheadTime + time - bullet.fireTime);
		    by = by + (20-3*bullet.power)*Util.cos( bullet.heading)
				       *( aheadTime + time - bullet.fireTime);
            double distance = Util.computeLineDistance( aheadX, aheadY, bx, by );
			if( distance < min ) min = distance;
		}
		return min;
	}

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

} // class vehicle