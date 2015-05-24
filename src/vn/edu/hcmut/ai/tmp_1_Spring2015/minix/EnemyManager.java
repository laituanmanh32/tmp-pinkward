package vn.edu.hcmut.ai.tmp_1_Spring2015.minix;
import java.util.Enumeration;
import java.util.Hashtable;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

public class EnemyManager
{
	private Minix operator;
	private AdvancedRobot robot;
    private Hashtable<String, Enemy> alive;
	private Hashtable<String, Enemy> dired;

	public EnemyManager( Minix operator, AdvancedRobot robot ){
        this.operator = operator;
		this.robot = robot;
		alive = new Hashtable<String, Enemy>();
		dired = new Hashtable<String, Enemy>();
	}

	//----------------------------------------------------------------
	public void onScannedRobot( ScannedRobotEvent event ){
        Enemy enemy = getEnemy( event.getName() );
		if( enemy == null ){
			enemy = new Enemy( operator, robot, event.getName() );
			alive.put( event.getName(), enemy );
		}
        enemy.onScannedRobot( event );
	}

	public void onHitByBullet( HitByBulletEvent event ){
        Enemy enemy = getEnemy( event.getName() );
        if( enemy != null )
			enemy.onEnemyBulletHit( event.getPower() );
	}

	public void onFire( double power ){
		for( Enumeration e = alive.elements(); e.hasMoreElements(); )
			 ((Enemy)e.nextElement()).onFire( power );
	}

	public void onBulletHit( BulletHitEvent event ){
		Enemy enemy = getEnemy( event.getName() );
		if( enemy != null )
			enemy.onBulletHit( event.getBullet().getPower() );
	}

	public void onRobotDeath( RobotDeathEvent event ){
        Enemy enemy = alive.remove( event.getName() );
		if( enemy == null ) return;
		dired.put( enemy.getName(), enemy );
		enemy.dired();
	}

	//-------------------------------------------------------------------
    public Enemy getEnemy( String name ){
		Enemy enemy = alive.get( name );
		return enemy;
	}

	public Enemy[] getEnemies(){
		if( alive.size() <1 ) return null;
		Enemy[] enemies = new Enemy[alive.size()];
		int index = 0;
		for( Enumeration e = alive.elements(); e.hasMoreElements(); ){
			 Enemy temp = (Enemy)e.nextElement();
			 enemies[index++] = temp;
		}
	    return enemies;
	}

    //------------------------------------------------------------------------------
    public void onFinish(){
        for( Enumeration e = alive.elements(); e.hasMoreElements(); ){
			 Enemy enemy = (Enemy)e.nextElement();
			 robot.out.println( enemy.getName()+"." );
			 robot.out.println("power(hit/fire): "+enemy.hitPower+"/"+enemy.firePower );
			 robot.out.println("num(hit/fire): " +enemy.hitNum+"/"+enemy.fireNum );
			 if( enemy.firePower >0 )
				 robot.out.println("fire count: "+enemy.hitPower/enemy.firePower );
		}
		for( Enumeration e = dired.elements(); e.hasMoreElements(); ){
			 Enemy enemy = (Enemy)e.nextElement();
			 robot.out.println( enemy.getName()+"." );
			 robot.out.println("power(hit/fire): "+enemy.hitPower+"/"+enemy.firePower );
			 robot.out.println("num(hit/fire): " +enemy.hitNum+"/"+enemy.fireNum );
			 if( enemy.firePower >0 )
				 robot.out.println("fire count: "+enemy.hitPower/enemy.firePower );
		}
	}

}