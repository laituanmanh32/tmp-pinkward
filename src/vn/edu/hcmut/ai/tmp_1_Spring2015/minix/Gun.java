package vn.edu.hcmut.ai.tmp_1_Spring2015.minix;

import java.awt.Color;
import java.util.Random;

import robocode.*;

/*
 * base class for minix operator.
 * class Gun, define the interfacer that control the gun .
 */

public class Gun {
	protected boolean turnDirection; // direction the gun will be turn
	protected double turnDegree; // degree the gun will be turn
	protected double fireHeading; // heading the gun will be turn to
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

	public Gun(Minix operator, AdvancedRobot robot) {
		this.robot = robot;
		this.operator = operator;

		battleFieldHeight = robot.getBattleFieldHeight();
		battleFieldWidth = robot.getBattleFieldWidth();
	}

	/**
	 * Compute the degree the gun will be turned.
	 */
	protected void computeTurnInfo() {
		double gunHeading = robot.getGunHeading();
		TurnInfo info = Util.computeTurnInfo(gunHeading, fireHeading);
		turnDirection = info.getDirection();
		turnDegree = info.getBearing();
	}

	/**
	 * Turn the gun but have to wait until it finish.
	 */
	protected void runHaveToWait() {
		if (turnDirection == Util.RIGHT) {
			robot.turnGunRight(turnDegree);
		} else {
			robot.turnGunLeft(turnDegree);
		}
	}

	/**
	 * Turn the gun immediately.
	 */
	protected void runNoWait() {
		if (turnDirection == Util.RIGHT) {
			robot.setTurnGunRight(turnDegree);
		} else {
			robot.setTurnGunLeft(turnDegree);
		}
	}

	/**
	 * Do the main work of gun.
	 */
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

	Color randomColor;

	/**
	 * Initialize the next fire, decide that if it is needed to fire.
	 */
	private void init() {
		needFire = true;
		if (bulletManager == null)
			needFire = false;
		else if (enemy.isDired()) {
			if (robot.getTime() % 10 == 0) {
				Random rand = new Random();
				int r = rand.nextInt(255);
				int g = rand.nextInt(255);
				int b = rand.nextInt(255);

				randomColor = new Color(r, g, b);
			}
			robot.setScanColor(randomColor);
			needFire = false;
		} else if (robot.getGunHeat() > 0 || robot.getEnergy() < 0.2)
			needFire = false;
	}

	/**
	 * Compute the power of bullet
	 */
	private void setBulletPower() {
		if (enemy.getLineDistance() > 500)
			bulletPower = 1; // set bullet power
		else if (enemy.getLineDistance() < 200)
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
		robot.out
				.println("compute error num: " + BulletManager.computeErrorNum);
	}

}