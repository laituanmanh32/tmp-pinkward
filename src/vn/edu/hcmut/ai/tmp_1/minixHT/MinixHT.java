package vn.edu.hcmut.ai.tmp_1.minixHT;
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
import vn.edu.hcmut.ai.tmp_1.minixHT.gunHT.GunHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.gunHT.MultiGunHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.gunHT.UniGunHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.radarHT.MultiRadarHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.radarHT.RadarHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.radarHT.UniRadarHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.vehicleHT.MultiVehicleHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.vehicleHT.UniVehicleHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.vehicleHT.VehicleHT;

/**
 * class MinixHT, a operator instance , by xieming for HT
 * main thread control the gun turn and fire ,
 * radar and vehicle are controled by envent handle
 */

public class MinixHT extends Operator
{
	// for debug
	private int skippedTurn = 0;
    private final boolean FINISH_DEBUG = true;

    private EnemyManagerHT enemyManager;
    private RadarHT radar;
	private VehicleHT vehicle;
    private GunHT gun;
	private final int VEHICLE_MONITOR_PERIODS = 1;

	public MinixHT( AdvancedRobot robot ){
	   super( robot );
	   setName("MinixHT");
       robot.setColors(null,null,null);
	   init( );
    }
    // -------------------- function for init ------------------
	private void init( ) {  // choose stratagem
	   enemyManager = new EnemyManagerHT( this, robot );
	   initRadar( );
	   initVehicle( );
	   initGun( );
			robot.setRadarColor(Color.WHITE);
    } // init

    private void initRadar( ){
		if( robot.getOthers() > 1 ) radar = new MultiRadarHT( this, robot );
		else radar = new UniRadarHT( this, robot );
    }

    private void initVehicle( ){
	    if( robot.getOthers() > 1 ) vehicle = new MultiVehicleHT( this, robot );
		else vehicle = new UniVehicleHT( this, robot );

	    robot.addCustomEvent(  // for vehicle monitor
			 new Condition("vehicleMonitor") {
			       public boolean test() {
				     return ( (robot.getTime() % VEHICLE_MONITOR_PERIODS ) == 0 );
			       }
			 } );
	}

	private void initGun(){
        if( robot.getOthers() > 1 ) gun = new MultiGunHT( this, robot );
		else gun = new UniGunHT( this, robot );
	}

    // --------------------- main thread --------------------
    public void work( ){
		/* main thread control the gun turn and fire
		*/
		while( true ){ gun.work(); }
	} // work

	//--------------------event handle for radar----------------------
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
			 gun = new UniGunHT( this, robot );
			 radar = new UniRadarHT( this, robot );
			 vehicle = new UniVehicleHT( this, robot );
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
    public EnemyHT getEnemy( String name ){
		return enemyManager.getEnemy( name );
	}

    public EnemyHT[] getEnemies(){
		return enemyManager.getEnemies();
	}

	public double getCount(){
		double count = 0;
		EnemyHT[] enemies = enemyManager.getEnemies();
		if( enemies != null )
			for( int i=0; i< enemies.length; i++ ) count -= enemies[i].getEnergy();
		count += robot.getEnergy();
		return count;
	}

} // class MinixHT
