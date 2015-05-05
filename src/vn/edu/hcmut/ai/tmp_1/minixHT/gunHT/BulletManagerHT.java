package vn.edu.hcmut.ai.tmp_1.minixHT.gunHT;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import vn.edu.hcmut.ai.tmp_1.minix.Util;
import vn.edu.hcmut.ai.tmp_1.minixHT.BulletHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.EnemyHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.PathNodeHT;

class BulletManagerHT
{
	private final boolean FIRE_HEADING_DEBUG = false;
	private final boolean COMPUTE_ERROR_DEBUG = false;
	static int computeErrorNum;
	static int computeNum;
	private static double lastComputeTime;

	private AdvancedRobot robot;
    private EnemyHT enemy;

    private final int OFFSET = 46;
	private double battleFieldHeight;
    private double battleFieldWidth;
	private ArrayList bullets;  // store the image bullet
	private int bulletNum;
    private double count = 0;

	BulletManagerHT( EnemyHT enemy, AdvancedRobot robot ){
        this.enemy = enemy;
		this.robot = robot;

		battleFieldHeight = robot.getBattleFieldHeight();
		battleFieldWidth = robot.getBattleFieldWidth();

        computeErrorNum = 0;
	    computeNum = 0;
	    lastComputeTime = 0;

		bullets = new ArrayList();
	}

    //-------------------------------------------------------------------------------
	void onScannedRobot( ){
		updateCount();
		if( robot.getOthers()==1 ) return;
		else if( robot.getTime() <= lastComputeTime ) return;
		else if( robot.getGunHeat() == 0 ) return;
		else addBullet( );
	}

    private void updateCount(){
		BulletHT bullet;
        //final double R = Math.max( battleFieldWidth, battleFieldHeight );
		for( int i=0; i< bullets.size(); i++ ){
		        bullet = (BulletHT) bullets.get(i);
			    if( bullet.hitTime <= enemy.getTime() ){
					double distance = Util.computeLineDistance(
						 bullet.hitX, bullet.hitY, enemy.getX(), enemy.getY() );
					bullets.remove(i--);
					count += distance;
					bulletNum++;
			    }
		}
	}

    private void addBullet( ){
		int position = getFitPosition( );
		BulletHT  bullet = getHitBullet( position, 3 );
		if( bullet != null ) bullets.add( bullet );
	}

    //--------------------------------------------------------------------
	String  getName(){ return enemy.getName(); }

	double getCount( ){
		if( bulletNum == 0 ) return Util.MAX_DOUBLE;
		else return count/bulletNum;
	}

	//--------------------------------------------------------------------
	double getFireHeading( double power ){
		double heading = getFireHeadingOnPath( power );
		if( robot.getOthers() == 1 ) return heading;
		else if( heading < 0 ) return getFireHeadingOnAhead( power );
		else return heading;
	}

	//--------------------------------------------------------------------
    private double getFireHeadingOnPath( double power ){
		int position = getFitPosition( );
		BulletHT bullet = getHitBullet( position, power );
		if( bullet == null ) return -1;
        bullets.add( bullet );
		return bullet.heading;
	}

	private int getFitPosition( ){
		int nodeNum = enemy.getPathSize();
		int offset = OFFSET;
        int offsetGap;

		if( nodeNum < 2*OFFSET ) offset = nodeNum/2;
        offset = Math.min( enemy.getCurrentSize(), offset );

        if( robot.getOthers() == 1 ) offsetGap = Math.max(1,offset/9);
		else offsetGap = Math.max(1,offset/5);

		int enemyNum = robot.getOthers();
		double min = Util.MAX_DOUBLE;
		int position = 0;
		for( int index = 0; index <= ( nodeNum - offset*2 ); index++ ){
             double count = 0;
			 PathNodeHT node1, node2;
             double add = 0;
			 for( int i = 0; i< offset; i+=offsetGap ){
		         node1 = enemy.getNode( index + i );
				 node2 = enemy.getNode( nodeNum - offset + i );

				 if( enemyNum >1 )
				      add = multiMode( node1, node2 );
				 else add = uniMode( node1, node2 );

				 count += add*( i+1 );
			 }
			 if( count < min
				 ||( count == min && Math.random()<0.5 ) ){ //enemy.getNode(index).time< enemy.getNode(position - offset).time) ){
				 min = count;
				 position = index + offset;
			 }
		}
        lastComputeTime = robot.getTime();
		computeNum++;
        return position;
	}

    // --------------------------------------------------------------------------------
	private double multiMode( PathNodeHT node1,  PathNodeHT node2 ){
        double add = 0;
        final double R = Math.max( battleFieldWidth/2, battleFieldHeight/2 );

		add += Math.abs( node1.oeDistance - node2.oeDistance )/R;  // importance 1
        add += Math.abs( node1.eVelocity - node2.eVelocity )/4;  // importance 2

        double temp = Math.abs( node1.eoBearing - node2.eoBearing );
		if( temp > 180 ) temp = 360 - temp;
  		add += temp/90; // importance 2

		return add;
	}

	//----------------------------------------------------------------------------------
	private double uniMode( PathNodeHT node1,  PathNodeHT node2 ){
         double add = 0;
		 final double R = Math.max( battleFieldWidth, battleFieldHeight );

         add += Math.abs( node1.meDistance - node2.meDistance )/R;
         add += Math.abs( node1.eVelocity - node2.eVelocity )/4;

		 double temp = Math.abs( node1.emBearing - node2.emBearing );
		 if( temp > 180 ) temp = 360 - temp;
  		 add += temp/90;

		 if( node1.mFire != node2.mFire ) add += 0.5;
		 if( node1.mBulletHit != node2.mBulletHit ) add += 0.5;

		 return add;
	}

    //----------------------------------------------------------------------------------
    private BulletHT getHitBullet( int position, double power ){
		 int size = enemy.getPathSize();
		 if( position < 1 || position >= size ){
			if( COMPUTE_ERROR_DEBUG ) robot.out.println("position error.");
			computeErrorNum++;
			return null;
		 }
		 PathNodeHT oNode = enemy.getNode( position - 1 );
		 PathNodeHT nextNode = enemy.getNode( position );
		 PathNodeHT lastNode = enemy.getNode();
		 double moveTime,distance, turnDegree, nextX, nextY;

         while( true ){
			distance = Util.computeLineDistance(
				              oNode.eX, oNode.eY, nextNode.eX, nextNode.eY );
			turnDegree = Util.computeRelativeBearing( oNode.eHeading,
			       Util.computeLineHeading( oNode.eX, oNode.eY, nextNode.eX, nextNode.eY ) );
			nextX = lastNode.eX + distance * Util.sin( lastNode.eHeading - turnDegree );
			nextY = lastNode.eY + distance * Util.cos( lastNode.eHeading - turnDegree );
			moveTime = nextNode.time - oNode.time;
			// check if compute error
			if( nextX > battleFieldWidth || nextY > battleFieldHeight
				     || nextX < 0 || nextY < 0 ){
				if( COMPUTE_ERROR_DEBUG ) robot.out.println("out of battle field.");
				computeErrorNum++;
				return null;
			}else if( moveTime < 0 ){
				if( COMPUTE_ERROR_DEBUG ) robot.out.println("beyond diffrent round.");
				computeErrorNum++;
				return null;
			}
			// check if can hit it
			if( (20-3*power)*(moveTime-1) >= Util.computeLineDistance(
				nextX, nextY, robot.getX(), robot.getY()) )
                return newBullet( nextX, nextY, power );
			// for next loop
			nextNode = enemy.getNode( ++position );
			if( position  >= size ){
				if( COMPUTE_ERROR_DEBUG ) robot.out.println("out of path size.");
				computeErrorNum++;
	            return null;
			}
		 }

	}

	//-------------------------------------------------------------------------
    private double getFireHeadingOnAhead( double power ){
       int totallTime = 0;
	   double nextX = enemy.getX();
	   double nextY = enemy.getY();
	   double heading = enemy.getFacingHeading();
	   double velocity = enemy.getVelocity();
	   while( true ){
			// compute next position
			nextX = nextX + velocity * Util.sin( heading ) * 1;
		    nextY = nextY + velocity * Util.cos( heading ) * 1;
	        // next loop
            totallTime ++;
			if( (20-3*power)*( totallTime )>= Util.computeLineDistance(
				nextX, nextY, robot.getX(), robot.getY() ) ) break;
			// check for hit the wall
            if( nextX > battleFieldWidth || nextY > battleFieldHeight
				|| nextX< 0|| nextY< 0 ) heading =Util.modifyHeading( heading +180);
	   }
	   BulletHT bullet = newBullet( nextX, nextY, power );
	   //bullets.add(bullet);
	   return bullet.heading;
	}

	private BulletHT newBullet(double hitX, double hitY, double power ){
        BulletHT bullet = new BulletHT();
		bullet.fireTime = (double)robot.getTime();
		bullet.fireX = robot.getX();
		bullet.fireY = robot.getY();
		bullet.hitX = hitX;
		bullet.hitY = hitY;
		bullet.power = power;
		bullet.heading = Util.computeLineHeading(bullet.fireX,bullet.fireY,hitX,hitY);
		bullet.hitTime = Util.computeLineDistance(bullet.fireX,bullet.fireY,hitX,hitY)/
			              (20-3*power) + bullet.fireTime;
		bullet.distance = Util.computeLineDistance( bullet.fireX, bullet.fireY,
			                                        enemy.getX(), enemy.getY() );
		return bullet;
	}

} // class BulletManagerHT
