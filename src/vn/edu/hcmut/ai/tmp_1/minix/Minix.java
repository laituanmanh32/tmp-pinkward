package vn.edu.hcmut.ai.tmp_1.minix;
import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.Condition;
import robocode.CustomEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import vn.edu.hcmut.ai.tmp_1.Operator;

/**
 * Class Minix, a operator instance,
 * main thread control the gun and  fire.
 * radar and vehicle move are controlled by event handler.
 */
public class Minix extends Operator
{
	// for debug
	private int skippedTurn = 0;
    private final boolean FINISH_DEBUG = true;

    private EnemyManager enemyManager;
    private Radar radar;
	private Vehicle vehicle;
    private Gun gun;
	private final int VEHICLE_MONITOR_PERIODS = 1;

	public Minix( AdvancedRobot robot ){
	   super( robot );
	   setName("Minix");
       robot.setColors(null,null,null);
	   init( );
    }

	/**
	 * Initialize all part of the tank.
	 */
	private void init( ) {  // choose stratagem
	   enemyManager = new EnemyManager( this, robot );
	   initRadar( );
	   initVehicle( );
	   initGun( );

    } // init

    private void initRadar( ){
		radar = new Radar( this, robot );
		robot.setScanColor(Color.WHITE);
    }

    private void initVehicle( ){
	   	vehicle = new Vehicle( this, robot );

	    robot.addCustomEvent(  // for vehicle monitor
			 new Condition("vehicleMonitor") {
			       public boolean test() {
				     return ( (robot.getTime() % VEHICLE_MONITOR_PERIODS ) == 0 );
			       }
			 } );
	}

	private void initGun(){
       gun = new Gun( this, robot );
	}

    /**
     * Main method for operator execute.
     */
    public void work( ){
		/* main thread control the gun turn and fire
		*/
		while( true ){ gun.work(); }
	} // work

	/**
	 * Event handle for radar.
	 */
	public void onScannedRobot( ScannedRobotEvent event ){
		super.onScannedRobot( event ); // call pathManager
        enemyManager.onScannedRobot( event ); // call enemyManager
		radar.onScannedRobot( event );
		gun.onScannedRobot( event );
		vehicle.onScannedRobot( event );
	}

    public void onRadarTurnComplete( CustomEvent event ){
		radar.onRadarTurnComplete( );
	}

    //--------------------event handle for vehicle----------------------
    public void onHitWall( HitWallEvent event ){
	    vehicle.onHitWall( event );
	}

	public void onHitRobot( HitRobotEvent event ){
	    vehicle.onHitRobot( event );
    }

	public void onCustomEvent( CustomEvent event ){
		if( event.getCondition().getName().equals("vehicleMonitor")){
            vehicle.onMonitor();
		}
	} //onCustomEvent

	//-----------------------------------------------------------------------------
	public void onEnemyFire( String name, double power ){
	    super.onEnemyFire( name, power );
    }

	public void onFire( double power ){
	    super.onFire( power );
		enemyManager.onFire( power );
	}

	public void onHitByBullet( HitByBulletEvent event ){
		super.onHitByBullet( event );
		enemyManager.onHitByBullet( event );
	    vehicle.onHitByBullet( event );
	}

	public void onBulletHit( BulletHitEvent event ){
		super.onBulletHit( event );
		enemyManager.onBulletHit( event );
		gun.onBulletHit( event );
	}

	public void onFinish( ){
		super.onFinish( );
		if( FINISH_DEBUG ){
		    enemyManager.onFinish();
		    gun.onFinish();
			robot.out.println("skipped turn num: " + skippedTurn );
		}
	}

    //--------------------other event handle------------------------
	public void onRobotDeath( RobotDeathEvent event ){
	     enemyManager.onRobotDeath( event );
		 if( robot.getOthers() == 1 ){ // switch to uni mode
			 gun = new Gun( this, robot );
			 radar = new Radar( this, robot );
			 vehicle = new Vehicle( this, robot );
		 }
	}

    public void onSkippedTurn(SkippedTurnEvent event ){
	     skippedTurn ++;
		 //robot.out.println("skipped turn.");
	}

	//---------------------------------------------------------------
	public void onGunTurnComplete( CustomEvent event ){
	     robot.removeCustomEvent(event.getCondition());
	}

	 public void onTurnComplete( CustomEvent event ){
	     robot.removeCustomEvent(event.getCondition());
    }

    public void onMoveComplete( CustomEvent event ){
	     robot.removeCustomEvent(event.getCondition());
    }

    //------------------ tool function --------------------
    public Enemy getEnemy( String name ){
		return enemyManager.getEnemy( name );
	}

    public Enemy[] getEnemies(){
		return enemyManager.getEnemies();
	}

	public double getCount(){
		double count = 0;
		Enemy[] enemies = enemyManager.getEnemies();
		if( enemies != null )
			for( int i=0; i< enemies.length; i++ ) count -= enemies[i].getEnergy();
		count += robot.getEnergy();
		return count;
	}

} // class Minix
