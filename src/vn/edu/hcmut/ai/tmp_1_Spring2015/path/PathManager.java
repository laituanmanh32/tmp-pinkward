package vn.edu.hcmut.ai.tmp_1_Spring2015.path;

import java.util.Enumeration;
import java.util.Hashtable;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;

public class PathManager {
	private AdvancedRobot robot;
	private static Hashtable<String,Path> pathes = new Hashtable<String,Path>();
	private int round;

	public PathManager( AdvancedRobot robot ){
		this.robot = robot;
		round = robot.getRoundNum();
    }

	//--------------------------------------------------------------------------------------
		public void onScannedRobot( ScannedRobotEvent event ){
			Path path = getPath( event.getName() );
			if( path == null ){
				path = new Path( event.getName() );
				pathes.put( event.getName(), path );
			}

	        PathNode newNode = new PathNode();

			newNode.round = this.round;
			newNode.time = event.getTime();

			newNode.enemyX = robot.getX() +
				event.getDistance()*Math.sin(event.getBearingRadians()+robot.getHeadingRadians());
			newNode.enemyY = robot.getY() +
				event.getDistance()*Math.cos(event.getBearingRadians()+robot.getHeadingRadians());
			newNode.myX = robot.getX();
			newNode.myY = robot.getY();

			newNode.enemyHeading = event.getHeadingRadians();
			newNode.myHeading = robot.getHeadingRadians();
			newNode.enemyVelocity = event.getVelocity();
			newNode.myVelocity = robot.getVelocity();

			newNode.enemyEnergy = event.getEnergy();
			newNode.myEnergy = robot.getEnergy();

			path.addNode( newNode );
		}

	    public void onFire( double power ){
			for( Enumeration<Path> e = pathes.elements(); e.hasMoreElements(); )
				 e.nextElement().onFire( power );
		}

		public void onBulletHit( BulletHitEvent event ){
			Path path = getPath( event.getName() );
			if( path != null )
			    path.onBulletHit( event.getBullet().getPower());
		}

	    public void onEnemyFire( String name, double power ){
	        Path path = getPath( name );
			if( path != null ) path.onEnemyFire( power );
		}

		public void onHitByBullet( HitByBulletEvent event ){
	        Path path = getPath( event.getName() );
			if( path != null ) path.onEnemyBulletHit( event.getPower());
		}

	    //-------------------------------------------------------------------------
	    public Path getPath( String name ){
			return pathes.get( name );
		}


}
