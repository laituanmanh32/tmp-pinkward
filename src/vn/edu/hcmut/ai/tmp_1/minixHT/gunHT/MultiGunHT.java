package vn.edu.hcmut.ai.tmp_1.minixHT.gunHT;
import java.util.Hashtable;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import vn.edu.hcmut.ai.tmp_1.minix.Util;
import vn.edu.hcmut.ai.tmp_1.minixHT.EnemyHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.MinixHT;

public class MultiGunHT extends GunHT
{
    private Hashtable bulletManagers;
	private EnemyHT currentEnemy = null;

	public MultiGunHT( MinixHT operator , AdvancedRobot robot ){
         super( operator, robot );
		 bulletManagers = new Hashtable();
	}

	public void work(){
	     init();
		 if( needFire ){
			 if( FIRE_DEBUG )
				 robot.out.println("current enemy is " + currentEnemy.getName() );
			 setBulletPower();
             BulletManagerHT manager =
				(BulletManagerHT) bulletManagers.get( currentEnemy.getName());
			 fireHeading = manager.getFireHeading( bulletPower );
         }else if( currentEnemy != null ){
			 fireHeading = Util.computeLineHeading( robot.getX(), robot.getY(),
					                currentEnemy.getX(), currentEnemy.getY() );
		 }else fireHeading = Util.computeLineHeading( robot.getX(), robot.getY(),
			                battleFieldWidth/2, battleFieldHeight/2 );
		 computeTurnInfo();
		 fire();
	}

    public void onScannedRobot( ScannedRobotEvent event ){
		 BulletManagerHT manager = (BulletManagerHT) bulletManagers.get( event.getName() );
         if( manager == null ){
			 EnemyHT enemy = operator.getEnemy( event.getName());
             manager = new BulletManagerHT( enemy, robot );
			 bulletManagers.put( event.getName(), manager );
		 }else manager.onScannedRobot();
	}

    private void init(){
         needFire = false;
		 if( robot.getGunHeat() > 0 || robot.getEnergy()< 0.2  ) return;

		 needFire = true;
		 if( currentEnemy != null )
		    if( !currentEnemy.isDired() ) return;
		 // choise another manager
         currentEnemy = choiseEnemy();
         if( currentEnemy == null ) needFire = false;
	}

    //-----------------------------------------------------------------------------
	private EnemyHT getMinDistance( ){
		 EnemyHT[] enemies = operator.getEnemies();
         if( enemies == null ) return null;

		 EnemyHT enemy = null;
		 double min = Util.MAX_DOUBLE;
		 for( int i=0; i< enemies.length; i++ )
			if( enemies[i].getLineDistance() < min ){
			     min = enemies[i].getLineDistance();
				 enemy = enemies[i];
			}
		 return enemy;
	}

	private EnemyHT getMinCount( ){
		 EnemyHT[] enemies = operator.getEnemies();
         if( enemies == null ) return null;

		 EnemyHT enemy = null;
		 BulletManagerHT manager;
		 double min = Util.MAX_DOUBLE;
		 for( int i=0; i<enemies.length; i++ ){
			 manager = (BulletManagerHT)bulletManagers.get( enemies[i].getName());
			 if( manager.getCount() < min ){
				 min = manager.getCount();
				 enemy = enemies[i];
			 }
		 }
		 return enemy;
	}

	//------------------------------------------------------------------------------------
	private void setBulletPower( ){
		 // set bullet power
	     if( currentEnemy.getLineDistance() > 500 ) bulletPower = 2;
		 else if( currentEnemy.getLineDistance() < 300 ) bulletPower = 3;
		 else bulletPower = 3 - currentEnemy.getLineDistance()/400 ;

		 if( currentEnemy.getEnergy()< 4*bulletPower )
				bulletPower = Math.max( currentEnemy.getEnergy()/4 , 0.1 );
		 if( bulletPower >robot.getEnergy()-0.1) bulletPower=robot.getEnergy()-0.1;
    }

    void fire(){
         super.fire();
		 if( needFire && turnDegree <= 20 ) currentEnemy = choiseEnemy();
	}

    private EnemyHT choiseEnemy(){
		 // choise enemy
		 //return operator.getEnemy("cx.Minix");
		 EnemyHT minDistance = getMinDistance( );
		 EnemyHT minCount = getMinCount( );

         if( minDistance == null ) return null;
		 else if( minDistance.getLineDistance()< 100 ) return minDistance;
         else if( minCount == null ) return minDistance;
		 else if( minCount.getLineDistance() - minDistance.getLineDistance() < 150
			 || minDistance.getLineDistance() > 250 ) return minCount;
         else return minDistance;
	}

}