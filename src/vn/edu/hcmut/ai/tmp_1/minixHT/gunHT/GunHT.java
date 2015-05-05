package vn.edu.hcmut.ai.tmp_1.minixHT.gunHT;
import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.ScannedRobotEvent;
import vn.edu.hcmut.ai.tmp_1.minix.Gun;
import vn.edu.hcmut.ai.tmp_1.minixHT.MinixHT;

public abstract class GunHT extends Gun
{
	MinixHT operator;

    final boolean FIRE_DEBUG = false;
	final boolean NO_FIRE_DEBUG = false;

	boolean needFire;
	double battleFieldHeight;
    double battleFieldWidth;

	double firePower = 0;
	double hitPower = 0;
	int fireNum = 0;
	int hitNum = 0;

	GunHT( MinixHT operator , AdvancedRobot robot){
		super( robot );
		this.operator = operator;

        battleFieldHeight = robot.getBattleFieldHeight();
		battleFieldWidth = robot.getBattleFieldWidth();
	}

	public abstract void work();
	public abstract void onScannedRobot( ScannedRobotEvent event );

	public void onBulletHit( BulletHitEvent event ){
		 hitPower += event.getBullet().getPower();
		 hitNum++;
	}

	void fire(){
		 runHaveToWait();
		 if( needFire && turnDegree <= 20 ){
			 robot.fire( bulletPower );
			 operator.onFire( bulletPower );
			 firePower += bulletPower;
		     fireNum++;
			 if( FIRE_DEBUG ){
			    robot.out.println("fire. time: "+ robot.getTime() );
			    robot.out.println("power: "+ bulletPower +". heading: "+ fireHeading );
			 }
		 }else if( needFire && NO_FIRE_DEBUG )
			 robot.out.println("turn degree too much");
	}

	public void onFinish(){
		 double count = 0;
		 if( firePower != 0 ) count = hitPower/firePower;
	     robot.out.println("MinixHT.");
		 robot.out.println("power(hit/fire): "+hitPower+"/"+firePower );
		 robot.out.println("num(hit/fire): " +hitNum+"/"+fireNum );
		 if( firePower >0 )
				 robot.out.println("fire count: "+ hitPower/firePower );
		 robot.out.println("compute num: " + BulletManagerHT.computeNum );
		 robot.out.println("compute error num: " + BulletManagerHT.computeErrorNum );
	}

}// class gunHT