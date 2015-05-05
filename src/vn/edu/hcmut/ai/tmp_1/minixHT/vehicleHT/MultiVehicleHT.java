package vn.edu.hcmut.ai.tmp_1.minixHT.vehicleHT;

import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import vn.edu.hcmut.ai.tmp_1.minix.Coordinate;
import vn.edu.hcmut.ai.tmp_1.minix.Util;
import vn.edu.hcmut.ai.tmp_1.minixHT.BulletHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.EnemyHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.MinixHT;


/**
 * a vehicle controller ,use for one multiple enemy , move base on centre point
 */

public class MultiVehicleHT extends VehicleHT
{
	private final boolean START_DEBUG = false;
	private final boolean SWITCH_DEBUG = false;
    private final boolean CHANGE_RADIUS_DEBUG = false;

	private final int COMPUTE_GAP = 10;
	private final double MAX_BEARING = 20;
	private final double TURN_RADIUS = 100;
	private final double MAX_STOP_TIME = 17;
	private final double AHEAD_TIME = 8;
	private final int MAX_BULLET_NUM = 5;

    private EnemyHT[] enemies;
	private double startTime;
    private double width;
	private double height;
	private ArrayList positions;
    private double[] headings;

	private double radiusOffset;
	private double lastChangeRadiusTime;

	public MultiVehicleHT( MinixHT operator , AdvancedRobot robot ){
		super( operator , robot );
		width = robot.getBattleFieldWidth();
		height = robot.getBattleFieldHeight();
		positions = new ArrayList();
		headings = new double[8];
		vehicleInit();
	}

    private void vehicleInit(){
		// init position
		for( int i=0; i<= width/COMPUTE_GAP; i++ ){
			positions.add( new Coordinate( i*COMPUTE_GAP, 0 ) );
			positions.add( new Coordinate( i*COMPUTE_GAP, height ) );
		}
		for( int i=1; i< height/COMPUTE_GAP; i++ ){
            positions.add( new Coordinate( 0, i*COMPUTE_GAP ) );
            positions.add( new Coordinate( width, i*COMPUTE_GAP ) );
		}
		// init headings
		headings[0] = 0;
		headings[1] = Util.computeLineHeading(oX,oY,width,height);
		headings[2] = 90;
		headings[3] = Util.computeLineHeading(oX,oY,width,0);
		headings[4] = 180;
		headings[5] = Util.computeLineHeading(oX,oY,0,0);
		headings[6] = 270;
		headings[7] = Util.computeLineHeading(oX,oY,0,height);
		// init radiusOffset, move near the wall
		radiusOffset = 0;
		lastChangeRadiusTime = 0;
		// init state
		changeRadius();
    }

    //--------------------------------------------------------------
	public void onMonitor(){ // control the vehicles action
        update();
		switch( vehicleState ){
            case MOVE:
				moveMonitor( );
			    break;
			case STOP:
				stopMonitor();
			    break;
			case CHANGE_RADIUS:
				changeRadiusMonitor();
			    break;
			case AWAY_ROBOT:
				awayRobotMonitor();
			    break;
	    } //switch
	}

	//---------------------------------------------------------------
	void update(){
		enemies = operator.getEnemies();
		computeCentre();
		computePreferHeading();
		super.update();
    }

	private void computeCentre(){
	     double omHeading = Util.computeLineHeading(
			        oX, oY, robot.getX(), robot.getY());
		 double reserveDistance = TURN_RADIUS + RESERVE_DISTANCE + radiusOffset;

		 if( omHeading >= headings[0] && omHeading < headings[1] ){
			 if( width - robot.getX() <= reserveDistance ){
				 centreX = width - reserveDistance;
			     centreY = height - reserveDistance;
			     preferRadius = TURN_RADIUS;
			 }else{
                 centreX = robot.getX();
				 centreY = height/2;
                 preferRadius = height/2;
			 }
		 }else if( omHeading >= headings[1] && omHeading < headings[2] ){
			 if( height - robot.getY() <= reserveDistance ){
				 centreX = width - reserveDistance;
			     centreY = height - reserveDistance;
			     preferRadius = TURN_RADIUS;
			 }else{
                 centreX = width/2;
				 centreY = robot.getY();
                 preferRadius = width/2;
			 }
		 }else if( omHeading >= headings[2] && omHeading < headings[3] ){
			 if( robot.getY() <= reserveDistance ){
				 centreX = width - reserveDistance;
			     centreY = reserveDistance;
			     preferRadius = TURN_RADIUS;
			 }else{
                 centreX = width/2;
				 centreY = robot.getY();
                 preferRadius = width/2;
			 }
         }else if( omHeading >= headings[3] && omHeading < headings[4] ){
			 if( width - robot.getX() <= reserveDistance ){
				 centreX = width - reserveDistance;
			     centreY = reserveDistance;
			     preferRadius = TURN_RADIUS;
			 }else{
                 centreX = robot.getX();
				 centreY = height/2;
                 preferRadius = height/2;
			 }
		 }else if( omHeading >= headings[4] && omHeading < headings[5] ){
			 if( robot.getX() <= reserveDistance ){
				 centreX = reserveDistance;
			     centreY = reserveDistance;
			     preferRadius = TURN_RADIUS;
			 }else{
                 centreX = robot.getX();
				 centreY = height/2;
                 preferRadius = height/2;
			 }
		 }else if( omHeading >= headings[5] && omHeading < headings[6] ){
			 if( robot.getY() <= reserveDistance ){
				 centreX = reserveDistance;
			     centreY = reserveDistance;
			     preferRadius = TURN_RADIUS;
			 }else{
                 centreX = width/2;
				 centreY = robot.getY();
                 preferRadius = width/2;
			 }
		 }else if( omHeading >= headings[6] && omHeading < headings[7] ){
			 if( height - robot.getY() <= reserveDistance ){
				 centreX = reserveDistance;
			     centreY = height - reserveDistance;
			     preferRadius = TURN_RADIUS;
			 }else{
                 centreX = width/2;
				 centreY = robot.getY();
                 preferRadius = width/2;
			 }
		 }else if( omHeading >= headings[7] && omHeading < 360 ){
			 if( robot.getX() <= reserveDistance ){
				 centreX = reserveDistance;
			     centreY = height - reserveDistance;
			     preferRadius = TURN_RADIUS;
			 }else{
                 centreX = robot.getX();
				 centreY = height/2;
                 preferRadius = height/2;
			 }
		 }else robot.out.println("MultiVehecleHT: heading error.");
		 if( preferRadius != TURN_RADIUS )
			 preferRadius -= radiusOffset + RESERVE_DISTANCE;
	}

	private void computePreferHeading(){
		 if( enemies == null ){
			preferHeading = Util.computeLineHeading(
				                 robot.getX(), robot.getY(), oX, oY );
		    return;
		 }
		 int minIndex = 0;
         double x, y, distance, bearing, count, minCount = Util.MAX_DOUBLE;
         for( int index=0; index< positions.size(); index++ ){
			  Coordinate point = (Coordinate) positions.get(index);
              x = point.getX();
			  y = point.getY();
              // compute distance
			  distance = 0;
			  for( int i=0; i< enemies.length; i++ )
				  distance += Util.computeLineDistance(
				           x, y, enemies[i].getX(), enemies[i].getY() );
			  // bearing = computeBearing(x, y);
              // compare the count
              count = 1/distance; // bearing/distance
			  if( count < minCount ){
				  minCount = count;
				  minIndex = index;
			  }
		 }
		 Coordinate preferPoint = (Coordinate) positions.get(minIndex);
		 preferHeading = Util.computeLineHeading(
			   preferPoint.getX(), preferPoint.getY(), oX, oY );
	}

	private double computeBearing( double x, double y ){
		  double bearing;
		  double[] lineHeadings = new double[ enemies.length ];
          double [] bearings = new double[ enemies.length ];

		  for( int i=0; i< enemies.length; i++ )
			  lineHeadings[i] = Util.computeLineHeading(
					x, y, enemies[i].getX(), enemies[i].getY() );

		  for( int i = 1; i< lineHeadings.length; i++ )
			   for( int j = 0; j< lineHeadings.length - i; j++ )
			        if( lineHeadings[ j ] > lineHeadings[ j+1 ] ){
					     double temp = lineHeadings[ j ];
					     lineHeadings[ j ] = lineHeadings[ j+1 ];
					     lineHeadings[ j+1 ] = temp;
		  }

          for( int i=0; i< bearings.length-1 ; i++ )
		       bearings[i] = lineHeadings[ i+1 ] - lineHeadings[ i ];
          bearings[bearings.length-1] =  // specail deal with
		       360 - ( lineHeadings[lineHeadings.length-1] - lineHeadings[0] );

          int max = 0;
		  for( int i = 1; i< bearings.length; i++ )
		       if( bearings[i] > bearings[max] ) max = i;

		  return 360 - bearings[max];

	}

    //---------------------------------------------------------------
    private void move( ){ // begin move
        if( START_DEBUG )robot.out.println("start move : " + time );
		vehicleState = MOVE;
		startTime = time;
		moveInit();
    }

    private void moveInit( ){ // compute move direction
		double bearing =
			 Util.computeAbsoluteBearing( preferHeading, oLineHeading );
		if( bearing > MAX_BEARING )
			 computeMoveDirectionToPreferHeading();
		else{
			 double aheadX = myX + 32 * Util.sin( robot.getHeading() );
		     double aheadY = myY + 32 * Util.cos( robot.getHeading() );
		     double aheadCount = computeCount( aheadX, aheadY, AHEAD_TIME );

		     aheadX = myX + -32 * Util.sin( robot.getHeading() );
		     aheadY = myY + -32 * Util.cos( robot.getHeading() );
		     double backCount = computeCount( aheadX, aheadY, AHEAD_TIME );

			 if( aheadCount >= backCount ) moveDirection = Util.AHEAD;
		     else moveDirection = Util.BACK;
		}
    }

    private void moveMonitor(){
        computeTurnInfoForKeepRadius();
		if( turnDegree < 10 && Math.abs(velocity) >7 )
			if( needChangeRadius() ){
			     changeRadius();
				 return;
		    }else if( !moveContinue() ){
				 stop();
				 return;
			}
		if( isTurn())computeInfoForMoveToRadius( moveDirection, 5, preferRadius );
		robot.setMaxVelocity( 45/turnDegree );
	    turnRun();
		moveRun( 10000 );
	}

	private boolean moveContinue(){
		double bearing =
			 Util.computeAbsoluteBearing( preferHeading, oLineHeading );
		if( bearing > MAX_BEARING ){
			 computeMoveDirectionToPreferHeading();
             return true;
		}

		double aheadX = myX + velocity *Util.sin( robot.getHeading())*AHEAD_TIME;
		double aheadY = myY + velocity *Util.cos( robot.getHeading())*AHEAD_TIME;
		double moveCount = computeCount( aheadX, aheadY, AHEAD_TIME );
		double stopCount = computeCount( myX, myY, AHEAD_TIME );

		if( moveCount < stopCount && time - startTime >30 ) return false;
		//else if(computeMinDistance(aheadX,aheadY,AHEAD_TIME)<50) return false;
		else return true;
	}

    private boolean isTurn(){
		double reserveDistance = TURN_RADIUS + RESERVE_DISTANCE + radiusOffset;
		if( myX <= reserveDistance && myY <= reserveDistance ) return true;
		else if( myX <= reserveDistance && myY >= height - reserveDistance ) return true;
		else if( myX >= width - reserveDistance && myY >= height - reserveDistance ) return true;
		else if( myX >= width - reserveDistance && myY < reserveDistance ) return true;
		else return false;
	}

    private boolean needChangeRadius(){
        if( enemies == null ) return false;
		else if( isTurn() ) return false;
		else if( radiusOffset >0 && time-lastChangeRadiusTime >50 ){
			if(CHANGE_RADIUS_DEBUG)
				robot.out.println("prefer move near the wall.");
			return true;
		}
		double bx, by, aheadX, aheadY, aheadTime = 8;
		aheadX = myX + velocity*Util.sin(robot.getHeading())*aheadTime;
		aheadY = myY + velocity*Util.cos( robot.getHeading())*aheadTime;

		for( int i=0; i< enemies.length; i++ )
             if( Util.computeLineDistance( aheadX, aheadY,
			     enemies[i].getX(), enemies[i].getY() ) < 50 ){
			     if(CHANGE_RADIUS_DEBUG)
				      robot.out.println("move away from enemy.");
			     return true;
		}

		BulletHT[] bullets = getAllBullets();
        if( bullets == null ) return false;
		BulletHT bullet;
		double bearing;
        for( int i=0; i< bullets.length; i++ ){
		    bullet = bullets[i];
			bearing = Util.computeAbsoluteBearing( bullet.heading,robot.getHeading());
			if( bearing >90 ) bearing = 180 - bearing;
			if( bearing >30 ) continue;
		    bx = bullet.fireX;
			by = bullet.fireY;
			bx = bx + (20-3*bullet.power)*Util.sin( bullet.heading)
				       *( aheadTime + time - bullet.fireTime);
		    by = by + (20-3*bullet.power)*Util.cos( bullet.heading)
				       *( aheadTime + time - bullet.fireTime);
            if( Util.computeLineDistance( aheadX, aheadY, bx, by ) < 50 ){
				if(CHANGE_RADIUS_DEBUG)
				     robot.out.println("move away from bullet.");
				return true;
			}
		}
		return false;
	}

    //--------------------- function for change radius --------------------
    private void changeRadius(){
        if( START_DEBUG )robot.out.println("change radius : " + time );
		vehicleState = CHANGE_RADIUS;
		startTime = time;
		lastChangeRadiusTime = time;
		if( radiusOffset == 0 ) radiusOffset = 100;
		else radiusOffset = 0;
	}

    private void changeRadiusMonitor(){
		if( Math.abs(centreLineDistance - preferRadius)>MAX_RADIUS_OFFSET ){
			computeInfoForMoveToRadius( moveDirection,70,preferRadius );
		    robot.setMaxVelocity( 45/turnDegree );
	        turnRun();
		    moveRun( 10000 );
		}else stop();
	}

	// ------------------------ function for stop -------------------------
    private void stop( ){
		if( START_DEBUG )robot.out.println("start stop : " + time );
		reset( );
		vehicleState = STOP;
		startTime = time;
    }

    private void stopMonitor(){
		if( stopContinue() ){
            computeTurnInfoForKeepRadius();
		    turnRun();
		}else move();
	}

    private boolean stopContinue(){
        if( Math.abs( velocity )>0 ) return true;
        else if( time - startTime >= MAX_STOP_TIME ) return false;

		double bearing =
			 Util.computeAbsoluteBearing( preferHeading, oLineHeading );

        double aheadX = myX + 32 * Util.sin( robot.getHeading() );
		double aheadY = myY + 32 * Util.cos( robot.getHeading() );
		double aheadCount = computeCount( aheadX, aheadY, AHEAD_TIME );

		aheadX = myX + -32 * Util.sin( robot.getHeading() );
		aheadY = myY + -32 * Util.cos( robot.getHeading() );
		double backCount = computeCount( aheadX, aheadY, AHEAD_TIME );

		double stopCount = computeCount( myX, myY, AHEAD_TIME );

		if( bearing > MAX_BEARING ){
			computeMoveDirectionToPreferHeading();
            if( moveDirection == Util.AHEAD && aheadCount >= stopCount )
				 return false;
			else if( moveDirection == Util.BACK && backCount >= stopCount )
				 return false;
			else return true;
		}else if( backCount >= stopCount || aheadCount >= stopCount )
			 return false;
		else return true;
	}

	//-------------------------------------------------------------------
	private BulletHT[] getAllBullets(){
		if( enemies == null ) return null;
        ArrayList bulletList = new ArrayList();
		BulletHT[] bullets;

		for( int i=0; i< enemies.length; i++ ){
			bullets = enemies[i].getBullets();
			if( bullets == null ) continue;
			for( int j=0; j< bullets.length; j++ )
				bulletList.add(bullets[j]);
		}
        if( bulletList.size() == 0 ) return null;

		bullets = new BulletHT[bulletList.size()];
        for( int i=0; i<bullets.length; i++ )
			 bullets[i] = (BulletHT)bulletList.get(i);

		return bullets;
	}

	private BulletHT[] getBullets(){
        BulletHT[] bullets = getAllBullets();
		if( bullets == null ) return null;
        BulletHT bullet;
		for( int i = 1; i< bullets.length; i++ )
			 for( int j = 0; j< bullets.length - i; j++ )
			      if( bullets[ j ].distance > bullets[ j+1 ].distance ){
					  bullet = bullets[ j ];
					  bullets[ j ] = bullets[ j+1 ];
					  bullets[ j+1 ] = bullet;
		}

        ArrayList bulletList = new ArrayList();
        for( int i=0; i< bullets.length && i< MAX_BULLET_NUM; i++ )
			bulletList.add( bullets[i] );
		bullets = new BulletHT[bulletList.size()];
		for( int i=0; i<bullets.length; i++ )
			bullets[i] = (BulletHT)bulletList.get(i);

        return bullets;
	}

    private double computeCount( double aheadX, double aheadY, double aheadTime ){
		BulletHT[] bullets = getBullets();
        if( bullets == null ) return Util.MAX_DOUBLE;

		double count = 0;
        for( int i=0; i< bullets.length; i++ ){
			 BulletHT bullet = bullets[i];
			 count += Util.computeLineDistance(
				      aheadX, aheadY, bullet.hitX, bullet.hitY );
		}
		return count;
	}

    private double computeMinDistance( double aheadX, double aheadY, double aheadTime ){
		BulletHT[] bullets = getBullets();
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

    //-----------------------------------------------------------------------

	public void onHitWall( HitWallEvent event ){
		robot.out.println("hit wall: "+ event.getTime());
        super.onHitWall( event );
	}

	public void onHitRobot( HitRobotEvent event ){
        robot.out.println("hit robot: "+ event.getTime());
		super.onHitRobot(event);
        vehicleState = AWAY_ROBOT;
		startTime = time;
	}

	private void awayRobotMonitor(){
        if( time - startTime > 10 ) move();
		else moveRun(100);
	}

} // class MultiVehicleHT
