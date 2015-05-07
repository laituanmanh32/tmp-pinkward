package vn.edu.hcmut.ai.tmp_1;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.CustomEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.WinEvent;
import vn.edu.hcmut.ai.tmp_1.path.PathManager;

public abstract class Operator {

	protected AdvancedRobot robot;
	private PathManager pathManager;
	private String name = null;

	public Operator(AdvancedRobot robot) {
		this.robot = robot;
		pathManager = new PathManager(robot);
	}

	public PathManager getPathManager() {
		return pathManager;
	}

	/**
	 * set the operator's name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * return the operator's name.
	 */
	public String getName() {
		return name;
	}

	// -------------------- interface ------------------------
	/**
	 * perform the robot in this method
	 */
	public abstract void work();

	public void onFire(double power) {
		pathManager.onFire(power);
	}

	public void onEnemyFire(String name, double power) {
		pathManager.onEnemyFire(name, power);
	}

	// system event, must call pathManager
	public void onBulletHit(BulletHitEvent e) {
		pathManager.onBulletHit(e);
	}

	public void onHitByBullet(HitByBulletEvent e) {
		pathManager.onHitByBullet(e);
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		pathManager.onScannedRobot(e);
	}

	// system event
	public  void onFinish() {
	}

	public void onDeath(DeathEvent e) {
	}

	public void onWin(WinEvent e) {
	}

	public void onRobotDeath(RobotDeathEvent e) {
	}

	public void onHitWall(HitWallEvent e) {
	}

	public void onHitRobot(HitRobotEvent e) {
	}

	public void onBulletMissed(BulletMissedEvent e) {
	}

	public void onBulletHitBullet(BulletHitBulletEvent e) {
	}

	// test if computing too much
	public void onSkippedTurn(SkippedTurnEvent e) {
	}

	// Custom Event
	public void onMoveComplete(CustomEvent e) {
	}

	public void onTurnComplete(CustomEvent e) {
	}

	public void onGunTurnComplete(CustomEvent e) {
	}

	public void onRadarTurnComplete(CustomEvent e) {
	}

	public void onCustomEvent(CustomEvent event) {
	}

}
