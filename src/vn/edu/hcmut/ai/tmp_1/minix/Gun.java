package vn.edu.hcmut.ai.tmp_1.minix;
import robocode.*;
import vn.edu.hcmut.ai.tmp_1.Operator;

/*
 * base class for minix operator.
 * class Gun, define the interfacer that control the gun .
 */

public class Gun
{
	protected boolean turnDirection; // direction the gun will be turn
	protected double  turnDegree; // degree the gun will be turn
	protected double  fireHeading; // heading the gun will be turn to
	protected double bulletPower; // power of the bullet will be fire

    protected AdvancedRobot robot;

    Minix operator;

	final boolean FIRE_DEBUG = false;
	final boolean NO_FIRE_DEBUG = false;

	boolean needFire;
	double battleFieldHeight;
	double battleFieldWidth;

	double firePower = 0;
	double hitPower = 0;
	int fireNum = 0;
	int hitNum = 0;

	private BulletManager bulletManager;
	private Enemy enemy;

	public Gun (Minix operator, AdvancedRobot robot ) {
		this.robot = robot;
		this.operator = operator;

		battleFieldHeight = robot.getBattleFieldHeight();
		battleFieldWidth = robot.getBattleFieldWidth();
	}

	protected void computeTurnInfo(){  // compute the degree the gun will be turn
	     double gunHeading = robot.getGunHeading();
		 TurnInfo info = Util.computeTurnInfo( gunHeading ,fireHeading );
		 turnDirection = info.getDirection();
	     turnDegree = info.getBearing();
	}

    protected void runHaveToWait(){
	     if( turnDirection == Util.RIGHT ){
			  robot.turnGunRight( turnDegree );
	     }else{
		      robot.turnGunLeft( turnDegree );
		 }
	}

    protected void runNoWait(){
	     if( turnDirection == Util.RIGHT ){
			  robot.setTurnGunRight( turnDegree );
	     }else{
		      robot.setTurnGunLeft( turnDegree );
		 }
	}

    public void work() {
		init();
		if (needFire) {
			setBulletPower();
			fireHeading = bulletManager.getFireHeading(bulletPower);
			if (fireHeading < 0)
				needFire = false;
		}
		if (!needFire)
			if (enemy != null)
				fireHeading = Util.computeLineHeading(robot.getX(),
						robot.getY(), enemy.getX(), enemy.getY());
			else
				fireHeading = Util.computeLineHeading(robot.getX(),
						robot.getY(), battleFieldWidth / 2,
						battleFieldHeight / 2);
		computeTurnInfo();
		fire();
	}

	public void onScannedRobot(ScannedRobotEvent event) {
		if (bulletManager == null) {
			enemy = operator.getEnemy(event.getName());
			bulletManager = new BulletManager(enemy, robot);
		} else
			bulletManager.onScannedRobot();
	}

	private void init() {
		needFire = true;
		if (bulletManager == null)
			needFire = false;
		else if (enemy.isDired())
			needFire = false;
		else if (robot.getGunHeat() > 0 || robot.getEnergy() < 0.2)
			needFire = false;
	}

	private void setBulletPower() {
		if (enemy.getLineDistance() > 500)
			bulletPower = 1.5; // set bullet power
		else if (enemy.getLineDistance() < 300)
			bulletPower = 3;
		else
			bulletPower = 3 - enemy.getLineDistance() / 500;

		if (enemy.getEnergy() < 4 * bulletPower)
			bulletPower = Math.max(enemy.getEnergy() / 4, 0.1);
		if (bulletPower > robot.getEnergy() - 0.1)
			bulletPower = robot.getEnergy() - 0.1;
	}

	public void onBulletHit(BulletHitEvent event) {
		hitPower += event.getBullet().getPower();
		hitNum++;
	}

	void fire() {
		runHaveToWait();
		if (needFire && turnDegree <= 20) {
			robot.fire(bulletPower);
			operator.onFire(bulletPower);
			firePower += bulletPower;
			fireNum++;
			if (FIRE_DEBUG) {
				robot.out.println("fire. time: " + robot.getTime());
				robot.out.println("power: " + bulletPower + ". heading: "
						+ fireHeading);
			}
		} else if (needFire && NO_FIRE_DEBUG)
			robot.out.println("turn degree too much");
	}

	public void onFinish() {
		double count = 0;
		if (firePower != 0)
			count = hitPower / firePower;
		robot.out.println("MinixHT.");
		robot.out.println("power(hit/fire): " + hitPower + "/" + firePower);
		robot.out.println("num(hit/fire): " + hitNum + "/" + fireNum);
		if (firePower > 0)
			robot.out.println("fire count: " + hitPower / firePower);
		robot.out.println("compute num: " + BulletManager.computeNum);
		robot.out.println("compute error num: "
				+ BulletManager.computeErrorNum);
	}

}