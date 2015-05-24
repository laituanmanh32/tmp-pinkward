package vn.edu.hcmut.ai.tmp_1_Spring2015.minix;

import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import vn.edu.hcmut.ai.tmp_1_Spring2015.path.Path;
import vn.edu.hcmut.ai.tmp_1_Spring2015.path.PathNode;

/**
 * class EnemyHT, a Enemy instance for operator MinixHT.
 * store and update the current enemy state and bullet info.
 */

public class Enemy
{
    private final boolean ENEMY_FIRE_DEBUG = false;
    private final boolean HIT_BY_BULLET_DEBUG = false;
    private final boolean INIT_DEBUG = false;
    private final boolean HIT_INFO_DEGUG = false;

	private final int MAX_PATH_SIZE = 3000;
    private final int OFFSET = 46;

	private Minix operator;
	private AdvancedRobot robot;
	private String name;

	double firePower = 0;
	public double hitPower = 0;
	int fireNum = 0;
	int hitNum = 0;

	private ArrayList currentBullets;
	private ArrayList hitInfo;
	private ArrayList path;
	private Path origenalPath;
	private double energy = 100;
    private double relativeBearing;

	private double nextFireTime = 0;
	private double battleFieldHeight;
    private double battleFieldWidth;

	public Enemy( Minix operator, AdvancedRobot robot, String name ){
	   this.name = name;
	   this.robot = robot;
       this.operator = operator;
	   origenalPath = operator.getPathManager().getPath( name );

	   currentBullets = new ArrayList();
	   hitInfo = new ArrayList();
	   path = new ArrayList();
	   battleFieldHeight = robot.getBattleFieldHeight();
	   battleFieldWidth = robot.getBattleFieldWidth();
	   init( );
    }

	private void init( ){
	   int begin;
	   if( origenalPath.getSize() <= MAX_PATH_SIZE ) begin = 0;
	   else begin = origenalPath.getSize() - MAX_PATH_SIZE;
	   for( int i=begin; i< origenalPath.getSize(); i++ )
		   path.add( getNewNode(origenalPath.getNode(i)) );
       if( INIT_DEBUG ){
		    robot.out.println("create enemy info manager for " + name);
			robot.out.println("path size: " + path.size() );
	   }
	   for( int i=0; i< path.size(); i++ )
            if( getNode(i).eBulletHit >0 ) addHitInfo(i);

	   if( HIT_INFO_DEGUG ){
			robot.out.println("hit bullets: "+ hitInfo.size() );
	   }
	}

    //------------------------------------------------------------------------
    private PathNodeHT addHitInfo( int hitPosition ){
		PathNodeHT hitNode = getNode(hitPosition);
        int firePosition = getFirePosition(hitPosition);
   		if( firePosition >=0 ){
            PathNodeHT fireNode = getNode(firePosition);
            double bearing = Util.computeRelativeBearing(
				   Util.computeLineHeading(fireNode.eX, fireNode.eY, hitNode.mX, hitNode.mY ),
				   Util.computeLineHeading(fireNode.eX, fireNode.eY, fireNode.mX, fireNode.mY ));
			fireNode.fireBearing = bearing;
			hitInfo.add( fireNode );
			if( HIT_INFO_DEGUG ){
			    robot.out.println("fire time: " + fireNode.time);
			    robot.out.println("fire bearing: " + bearing);
				double heading =
					 Util.modifyHeading(fireNode.meHeading+180+bearing);
				robot.out.println("fire heading: " + heading);
			}
			return fireNode;
		}else if( HIT_INFO_DEGUG )
			 robot.out.println("can not find the fire point");
		return null;
	}

	private int getFirePosition( int hitPosition ){
		int beginPosition = getBeginPosition( hitPosition );
		PathNodeHT hitNode = getNode( hitPosition );
		for( int j=hitPosition; j>=beginPosition; j-- ){
			PathNodeHT aheadNode = getNode(j);
			if( aheadNode.eFire == 0 ) continue;
			double powerOffset =
				Math.abs( hitNode.eBulletHit - aheadNode.eFire );
			if( powerOffset >= 0.1 ) continue;
            double lineDistance =
				Util.computeLineDistance( hitNode.mX, hitNode.mY,
							    aheadNode.eX, aheadNode.eY );
			double moveDistance =
				(hitNode.time - aheadNode.time)*(20-3*aheadNode.eFire);
			if( Math.abs(lineDistance - moveDistance)<= 50 ) return j;
		}
		return -1;
	}

    private int getBeginPosition( int lastPosition ){
		for( int i=lastPosition; i>0; i-- )
		   if( getNode(i).time - getNode(i-1).time <= 0 )
				return i;
		return 0;
	}

	private int getBeginPosition( ){
        return getBeginPosition( path.size() -1 );
	}

	//-----------------------------------------------------------------------
	private PathNodeHT getNewNode( PathNode oldNode ){
           PathNodeHT newNode = new PathNodeHT();
		   double ox = battleFieldWidth/2;
		   double oy = battleFieldHeight/2;
		   // nornal
		   newNode.time = oldNode.time;
		   newNode.eX = oldNode.enemyX;
		   newNode.eY = oldNode.enemyY;
		   newNode.mX = oldNode.myX;
		   newNode.mY = oldNode.myY;
		   // two distance
           newNode.oeDistance = Util.computeLineDistance( newNode.eX, newNode.eY, ox, oy );
           newNode.meDistance = Util.computeLineDistance( newNode.mX, newNode.mY, newNode.eX, newNode.eY );
           // modify facing heading and velocity
		   newNode.meHeading = Util.computeLineHeading( newNode.mX, newNode.mY, newNode.eX, newNode.eY );
		   newNode.eHeading = oldNode.enemyHeading*180/Math.PI;
           newNode.mHeading = oldNode.myHeading*180/Math.PI;
           newNode.eVelocity = oldNode.enemyVelocity;
		   newNode.mVelocity = oldNode.myVelocity;
		   if( newNode.eVelocity < 0 ){
			   newNode.eVelocity = - newNode.eVelocity;
			   newNode.eHeading = Util.modifyHeading( newNode.eHeading + 180 );
           }else if( newNode.mVelocity < 0 ){
			   newNode.mVelocity = - newNode.mVelocity;
			   newNode.mHeading = Util.modifyHeading( newNode.mHeading + 180 );
		   }
		   // bearing
           newNode.emBearing = Util.computeRelativeBearing( newNode.eHeading, newNode.meHeading );
           newNode.meBearing = Util.computeRelativeBearing( newNode.mHeading, newNode.meHeading );
		   newNode.eoBearing = Util.computeRelativeBearing( newNode.eHeading,
                       Util.computeLineHeading( ox, oy, newNode.eX, newNode.eY )
		   );
           // bullet info
		   newNode.mFire = oldNode.myFire;
		   newNode.mBulletHit = oldNode.myBulletHit;
		   newNode.eFire = oldNode.enemyFire;
		   newNode.eBulletHit = oldNode.enemyBulletHit;

		   return newNode;
	}

    //----------------------------------------------------------------------------------------
    public void onScannedRobot( ScannedRobotEvent event ){
   	   // add new path node
	   int begin = origenalPath.getSize()-1;
	   for( ; begin >0; begin-- )
          if( origenalPath.getNode(begin-1).time == getNode().time ) break;
	   for( int i= begin; i<origenalPath.getSize(); i++ )
	      path.add( getNewNode(origenalPath.getNode(i)) );
	   while( path.size() > MAX_PATH_SIZE ) path.remove(0);
       // update current state
	   double power = energy - event.getEnergy();
	   energy = event.getEnergy();
       relativeBearing = event.getBearing();
       // check if enemy fire or bullet hit
	   if( power < 0 ){ // enemy 's bullet hit someone, so it's energy increat
		   hitNum ++;
		   hitPower -= power/3;
		   return;
	   }else if( event.getTime() < nextFireTime  ) return; // enemy gun has not cool
	   else if( power > 3 || power == 0 ) return; // enemy was hit by bullet which power >1
       //  0< power <=3
	   enemyFire( power );
	   nextFireTime = event.getTime() + (power/5 +1)/robot.getGunCoolingRate() - 4;
	}

	//---------------------------------------------------------------------------------------------
    private void enemyFire( double power ){
       // enemy fire!
	   firePower += power;
	   fireNum ++;
	   getNode().eFire = power;
	   operator.onEnemyFire( name, power );
       //compute fire info
	   Bullet bullet;
	   if( robot.getOthers() >1 ){
	       bullet = getFireInfoOnOpoint( power );
           currentBullets.add( bullet );
	       bullet = getFireInfoOnAhead( power );
	       currentBullets.add( bullet );
	   }else{
		   bullet = getFireInfoOnPath( power );
	       currentBullets.add( bullet );
	       bullet = getFireInfoOnBullet( power );
	       currentBullets.add( bullet );
	   }
	   if( ENEMY_FIRE_DEBUG ){
		   robot.out.println(name+" fire. "+"time: "+ getTime());
		   robot.out.println("power: "+ power+ ". heading: "+ bullet.heading);
	   }
	}

    //-----------------------------------------------------------------------
	private Bullet getFireInfoOnOpoint( double power ){
		return newBullet( robot.getX(), robot.getY(), power );
	}

    private Bullet getFireInfoOnAhead( double power ){
       int totallTime = 0;
	   double nextX = robot.getX();
	   double nextY = robot.getY();
	   double heading = robot.getHeading();
	   double velocity = robot.getVelocity();

	   while( true ){
			// compute next position
			nextX = nextX + velocity * Util.sin( heading ) * 1;
		    nextY = nextY + velocity * Util.cos( heading ) * 1;
	        // next loop
            totallTime ++;
			if( (20-3*power)*( totallTime )>= Util.computeLineDistance(
				nextX, nextY, getX(), getY() ) )
                return newBullet( nextX, nextY, power );
			// check for hit the wall
            if( nextX > battleFieldWidth || nextY > battleFieldHeight
				|| nextX< 0|| nextY< 0 )
				heading =Util.modifyHeading( heading +180);
	   }

	}

	//--------------------------------------------------------------------------
	private Bullet getFireInfoOnPath( double power ){
		Bullet bullet = null;
		if( robot.getGunHeat() > 0 ){
		    int position = getFitPosition( );
            bullet = getHitBullet( position, power );
		}
        if( bullet == null ) return getFireInfoOnAhead(power);
		else return bullet;
	}

	private int getFitPosition( ){
         int nodeNum = getPathSize();
		 int offset = OFFSET;
         int offsetGap;

		 if( nodeNum < 2*OFFSET ) offset = nodeNum/2;
         offset = Math.min( getCurrentSize(), offset );
         offsetGap = Math.max(1,offset/5);

		 double min = Util.MAX_DOUBLE;
		 int position = 0;
		 final double R = Math.max( battleFieldHeight, battleFieldWidth );
		 for( int index = 0; index <= ( nodeNum - offset*2 ); index++ ){
             double count = 0;
			 PathNodeHT node1, node2;
			 for( int i = 0; i< offset; i+=offsetGap ){
		         node1 = getNode( index + i );
				 node2 = getNode( nodeNum - offset + i );
				 double add = 0;

                 add += Math.abs( node1.meDistance - node2.meDistance )/R;
                 add += Math.abs( node1.mVelocity - node2.mVelocity )/4;

				 double temp = Math.abs( node1.meBearing - node2.meBearing );
				 if( temp > 180 ) temp = 360 - temp;
  				 add += temp/60;

				 if( node1.eFire != node2.eFire ) add += 0.5;
				 if( node1.eBulletHit != node2.eBulletHit ) add += 0.5;

				 count += add*(1+i);
			 }
			 if( count < min || ( count == min && Math.random()<0.5 ) ){
				 min = count;
				 position = index + offset;
			 }
		 }// for
		 return position;
	}

    private Bullet getHitBullet( int position, double power ){
		 int size = getPathSize();
		 if( position < 1 || position >= size ) return null;

		 PathNodeHT oNode = getNode( position - 1 );
		 PathNodeHT nextNode = getNode( position );
		 PathNodeHT lastNode = getNode();
		 double moveTime,distance, turnDegree, nextX, nextY;
		 //
         while( true ){
			distance = Util.computeLineDistance(
				              oNode.mX, oNode.mY, nextNode.mX, nextNode.mY );
			turnDegree = Util.computeRelativeBearing( oNode.mHeading,
			       Util.computeLineHeading( oNode.mX, oNode.mY, nextNode.mX, nextNode.mY ) );
			nextX = lastNode.mX + distance * Util.sin( lastNode.mHeading - turnDegree );
			nextY = lastNode.mY + distance * Util.cos( lastNode.mHeading - turnDegree );
			moveTime = nextNode.time - oNode.time;
		    // check it
			if( moveTime < 0 ) return null;
			else if( nextX > battleFieldWidth || nextY > battleFieldHeight
				     || nextX < 0 || nextY < 0 ) return null;
			// check if can hit
			if( (20-3*power)* moveTime >= Util.computeLineDistance(
				nextX, nextY, lastNode.eX, lastNode.eY ) )
				return newBullet( nextX, nextY, power );
			// for next loop
			nextNode = getNode( ++position );
			if( position >= size ) return null;
		 }
	}

    //--------------------------------------------------------------------
	private Bullet getFireInfoOnBullet( double power ){
		  double min = Util.MAX_DOUBLE;
		  PathNodeHT current = getNode();
		  double bearing = 0;
		  final double R = Math.max( battleFieldHeight, battleFieldWidth );
		  for( int i=0; i<hitInfo.size(); i++ ){
               double count = 0;
			   PathNodeHT fire = (PathNodeHT)hitInfo.get(i);

			   count += Math.abs( current.meDistance - fire.meDistance )/R;
               count += Math.abs( current.mVelocity - fire.mVelocity )/4;

			   double temp = Math.abs( current.meBearing - fire.meBearing );
			   if( temp > 180 ) temp = 360 - temp;
  			   count += temp/60;

               if( count <= min ){
				   min = count;
				   bearing = fire.fireBearing;
			   }
		  }
          double fireHeading = Util.modifyHeading(
			     180 + current.meHeading + bearing );
		  double hitX = getX() + getLineDistance()*Util.sin(fireHeading);
		  double hitY = getY() + getLineDistance()*Util.cos(fireHeading);
          return newBullet( hitX, hitY, power );
	}

	//-----------------------------------------------------------------------------------------
	private Bullet newBullet(double hitX, double hitY, double power){
		 Bullet bullet = new Bullet();
		 bullet.fireTime = getTime();
		 bullet.fireX = getX();
		 bullet.fireY = getY();
		 bullet.hitX = hitX;
		 bullet.hitY = hitY;
		 bullet.power = power;
		 bullet.heading = Util.computeLineHeading(bullet.fireX,bullet.fireY,hitX,hitY);
		 bullet.hitTime = Util.computeLineDistance(bullet.fireX,bullet.fireY,hitX,hitY)/
			              (20-3*power) + bullet.fireTime;
		 bullet.distance = Util.computeLineDistance( bullet.fireX, bullet.fireY,
			                                         robot.getX(), robot.getY() );
		 return bullet;
	}

	public void onEnemyBulletHit( double power ){
         if( HIT_BY_BULLET_DEBUG )
	         robot.out.println("hit time: "+ robot.getTime() + ". power: "+ power );

		 PathNodeHT node = getNode();
		 if( node != null ) node.eBulletHit = power;

         PathNodeHT fireNode = addHitInfo(path.size()-1);

		 if( fireNode == null ) return;

		 int oldSize = currentBullets.size();
	     for( int i=0; i< currentBullets.size(); i++ )
			 if( ((Bullet)currentBullets.get(i)).fireTime == fireNode.time )
			     currentBullets.remove(i--);
         if( HIT_BY_BULLET_DEBUG )
             robot.out.println("delete bullets: "+(oldSize-currentBullets.size()));
	}

	public Bullet[] getBullets(){
		 Bullet bullet;
		 double time = robot.getTime();
		 double mx = robot.getX();
		 double my = robot.getY();
		 double moveDistance, mfDistance;
		 for( int i=0; i< currentBullets.size(); i++ ){
			bullet = (Bullet)currentBullets.get(i);
			moveDistance = ( 20-3*bullet.power )*( time - bullet.fireTime );
            mfDistance = Util.computeLineDistance(
				            mx, my, bullet.fireX, bullet.fireY );
			if( moveDistance - mfDistance >50 )
				currentBullets.remove(i--);
		 }
         if( currentBullets.size() == 0 ) return null;

		 Bullet[] bullets = new Bullet[ currentBullets.size() ];
	     for( int i=0; i< bullets.length; i++ )
		     bullets[i] = (Bullet)currentBullets.get(i);
	     return bullets;
	}

    //--------------------------------------------------------------------------
	public PathNodeHT getNode( int index ){
		if( index <0 || index >=path.size() ) return null;
		else return (PathNodeHT) path.get( index );
	}

	public PathNodeHT getNode(){
		if( path.size()==0 ) return null;
        else return (PathNodeHT) path.get( path.size()-1 );
	}

    public int getPathSize(){
		return path.size();
	}

    public int getCurrentSize(){
		return path.size()-getBeginPosition( );
    }

    //------------------------------------------------------------------
	public String getName(){ return name; }
	public double getEnergy(){ return energy;}
    public double getRelativeBearing(){ return relativeBearing; }
    public double getLineHeading(){ return getNode().meHeading; }
	public double getLineDistance(){ return getNode().meDistance; }
	public double getFacingHeading(){ return getNode().eHeading; }
	public double getVelocity(){ return getNode().eVelocity; }
	public double getX(){ return getNode().eX; }
	public double getY(){ return getNode().eY; }
    public double getTime(){ return getNode().time; }
	public boolean equals( String name ){ return this.name.equals(name); }
	public boolean isDired(){
		if( energy == 0 ) return true;
		else return false;
	}
	//--------------------------------------------------------------------------
	public void onFire( double power ){
		 PathNodeHT node = getNode();
		 if( node != null ) node.mFire = power;
	}
	public void onBulletHit( double power ){
		 PathNodeHT node = getNode();
		 if( node != null ) node.mBulletHit = power;
	}
    public void dired(){ energy = 0; }

}//---------------------------------------------------------------------------