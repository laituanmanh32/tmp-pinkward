package vn.edu.hcmut.ai.tmp_1.minixHT.gunHT;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import vn.edu.hcmut.ai.tmp_1.minix.Util;
import vn.edu.hcmut.ai.tmp_1.minixHT.EnemyHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.MinixHT;


public class UniGunHT extends GunHT
{
    private BulletManagerHT bulletManager;
	private EnemyHT enemy;

	public UniGunHT( MinixHT operator , AdvancedRobot robot ){
         super( operator, robot );
	}

	public void work(){
	     init();
		 if( needFire ){
             setBulletPower( );
			 fireHeading = bulletManager.getFireHeading( bulletPower );
			 if( fireHeading < 0 ) needFire = false;
         }
		 if( ! needFire )
		      if( enemy != null )
			      fireHeading = Util.computeLineHeading(
			                robot.getX(), robot.getY(), enemy.getX(), enemy.getY() );
		      else fireHeading = Util.computeLineHeading( robot.getX(), robot.getY(),
			                battleFieldWidth/2, battleFieldHeight/2 );
		 computeTurnInfo();
		 fire();
	}

    public void onScannedRobot( ScannedRobotEvent event ){
         if( bulletManager == null ){
			 enemy = operator.getEnemy( event.getName() );
             bulletManager = new BulletManagerHT( enemy, robot );
		 }else bulletManager.onScannedRobot();
	}

    private void init(){
         needFire = true;
		 if( bulletManager == null ) needFire = false;
		 else if( enemy.isDired() ) needFire = false;
	     else if( robot.getGunHeat() > 0 || robot.getEnergy()< 0.2 ) needFire = false;
	}

    private void setBulletPower( ){
			if( enemy.getLineDistance() > 500 ) bulletPower = 1.5; // set bullet power
		    else if( enemy.getLineDistance() < 300 ) bulletPower = 3;
		    else bulletPower = 3 - enemy.getLineDistance()/500 ;

			if( enemy.getEnergy()< 4*bulletPower )
				bulletPower = Math.max( enemy.getEnergy()/4 , 0.1 );
			if( bulletPower >robot.getEnergy()-0.1) bulletPower=robot.getEnergy()-0.1;
    }

}