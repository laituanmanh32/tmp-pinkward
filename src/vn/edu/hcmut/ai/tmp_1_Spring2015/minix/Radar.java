package vn.edu.hcmut.ai.tmp_1_Spring2015.minix;

import robocode.*;

/*
 * base class for minix operator.
 * class Radar, define the interfacer that control the radar .
 */

public class Radar {
	protected AdvancedRobot robot;
	protected boolean turnDirection;
	protected double turnDegree;

	Minix operator;

	Enemy enemy;

	public Radar(Minix operator, AdvancedRobot robot) {
		this.robot = robot;
		this.operator = operator;
		scan(400);
	}

	// ----------------------- tool function ------------------
	protected void computeTurnInfo(double lineHeading) {
		double radarHeading = robot.getRadarHeading();
		TurnInfo info = Util.computeTurnInfo(radarHeading, lineHeading);
		turnDirection = info.getDirection();
		turnDegree = info.getBearing();
	} // turn to lineHeading

	protected void track(double lineHeading) {
		computeTurnInfo(lineHeading);
		turnDegree = turnDegree * 1.2 + 2;
		run();
	}

	protected void scan(double degree) {
		turnDegree = degree;
		run();
	}

	protected void scan(double degree, boolean direction) {
		turnDegree = degree;
		turnDirection = direction;
		run();
	} // scan

	protected void run() {
		if (turnDirection == Util.RIGHT)
			robot.setTurnRadarRight(turnDegree);
		else
			robot.setTurnRadarLeft(turnDegree);
		// robot.execute( );
	} // run

	public void onScannedRobot(ScannedRobotEvent e) {
		if (enemy == null)
			enemy = operator.getEnemy(e.getName());
		track(enemy.getLineHeading());
	}

	public void onRadarTurnComplete() {
		if (enemy == null)
			scan(400);
		else if (robot.getTime() - enemy.getTime() > 1)
			scan(400);
	}

} // class radar

