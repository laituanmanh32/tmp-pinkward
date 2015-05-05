package vn.edu.hcmut.ai.tmp_1.minixHT;
import java.util.Enumeration;
import java.util.Hashtable;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

public class EnemyManagerHT
{
	private MinixHT operator;
	private AdvancedRobot robot;
    private Hashtable alive;
	private Hashtable dired;

	public EnemyManagerHT( MinixHT operator, AdvancedRobot robot ){
        this.operator = operator;
		this.robot = robot;
		alive = new Hashtable();
		dired = new Hashtable();
	}

	//----------------------------------------------------------------
	public void onScannedRobot( ScannedRobotEvent event ){
        EnemyHT enemy = getEnemy( event.getName() );
		if( enemy == null ){
			enemy = new EnemyHT( operator, robot, event.getName() );
			alive.put( event.getName(), enemy );
		}
        enemy.onScannedRobot( event );
	}

	public void onHitByBullet( HitByBulletEvent event ){
        EnemyHT enemy = getEnemy( event.getName() );
        if( enemy != null )
			enemy.onEnemyBulletHit( event.getPower() );
	}

	public void onFire( double power ){
		for( Enumeration e = alive.elements(); e.hasMoreElements(); )
			 ((EnemyHT)e.nextElement()).onFire( power );
	}

	public void onBulletHit( BulletHitEvent event ){
		EnemyHT enemy = getEnemy( event.getName() );
		if( enemy != null )
			enemy.onBulletHit( event.getBullet().getPower() );
	}

	public void onRobotDeath( RobotDeathEvent event ){
        EnemyHT enemy = ( EnemyHT )alive.remove( event.getName() );
		if( enemy == null ) return;
		dired.put( enemy.getName(), enemy );
		enemy.dired();
	}

	//-------------------------------------------------------------------
    public EnemyHT getEnemy( String name ){
		EnemyHT enemy = (EnemyHT)alive.get( name );
		return enemy;
	}

	public EnemyHT[] getEnemies(){
		if( alive.size() <1 ) return null;
		EnemyHT[] enemies = new EnemyHT[alive.size()];
		int index = 0;
		for( Enumeration e = alive.elements(); e.hasMoreElements(); ){
			 EnemyHT temp = (EnemyHT)e.nextElement();
			 enemies[index++] = temp;
		}
	    return enemies;
	}

    //------------------------------------------------------------------------------
    public void onFinish(){
        for( Enumeration e = alive.elements(); e.hasMoreElements(); ){
			 EnemyHT enemy = (EnemyHT)e.nextElement();
			 robot.out.println( enemy.getName()+"." );
			 robot.out.println("power(hit/fire): "+enemy.hitPower+"/"+enemy.firePower );
			 robot.out.println("num(hit/fire): " +enemy.hitNum+"/"+enemy.fireNum );
			 if( enemy.firePower >0 )
				 robot.out.println("fire count: "+enemy.hitPower/enemy.firePower );
		}
		for( Enumeration e = dired.elements(); e.hasMoreElements(); ){
			 EnemyHT enemy = (EnemyHT)e.nextElement();
			 robot.out.println( enemy.getName()+"." );
			 robot.out.println("power(hit/fire): "+enemy.hitPower+"/"+enemy.firePower );
			 robot.out.println("num(hit/fire): " +enemy.hitNum+"/"+enemy.fireNum );
			 if( enemy.firePower >0 )
				 robot.out.println("fire count: "+enemy.hitPower/enemy.firePower );
		}
	}

}