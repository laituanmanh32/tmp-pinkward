package vn.edu.hcmut.ai.tmp_1.minixHT.vehicleHT;
import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import vn.edu.hcmut.ai.tmp_1.minix.Util;
import vn.edu.hcmut.ai.tmp_1.minixHT.BulletHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.EnemyHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.MinixHT;

/**
 * a vehicle controller ,use for one enemy only , move base on centre point
 */

public class UniVehicleHT extends VehicleHT
{
	// for debug
	private final boolean START_DEBUG = false;
	private final boolean SWITCH_DEBUG = false;

    private final double AHEAD_TIME = 8;
	private EnemyHT enemy;

	private double maxBearing;
	private double radiusToMove ; // radius my robot will move to
	private double startTime;

	public UniVehicleHT( MinixHT operator, AdvancedRobot robot ){
		super( operator , robot );
		vehicleInit();
	}

    private void vehicleInit(){
		robot.setMaxVelocity(8);
	    preferRadius = 350;
		stop();
	}

	void update(){
		if( enemy == null ){
            centreX = oX;
		    centreY = oY;
		}else{
		    centreX = enemy.getX();
		    centreY = enemy.getY();
		}
        super.update();
        // compute radius to move and max bearing
		double centreODistance = Util.computeLineDistance( oX, oY, centreX, centreY );
		if( centreODistance + preferRadius <= R ){
			radiusToMove = preferRadius;
			maxBearing = 1000;
		}else if( centreODistance <= R/4 ){
			centreX = oX;
		    centreY = oY;
			super.update();
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
        update();
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
        BulletHT[] bullets = enemy.getBullets();
		if( bullets == null ) return Util.MAX_DOUBLE;
		double count = 0;
        for( int i=0; i< bullets.length; i++ ){
			 BulletHT bullet = bullets[i];
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
		BulletHT[] bullets = enemy.getBullets();
		if( bullets == null ) return Util.MAX_DOUBLE;

		double min = Util.MAX_DOUBLE;
        for( int i=0; i< bullets.length; i++ ){
		    BulletHT bullet = bullets[i];
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

}//class UniVehicleHT
