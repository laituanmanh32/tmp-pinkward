package vn.edu.hcmut.ai.tmp_1_Spring2015;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.CustomEvent;
import robocode.DeathEvent;
import robocode.GunTurnCompleteCondition;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.MoveCompleteCondition;
import robocode.RadarTurnCompleteCondition;
import robocode.RobocodeFileOutputStream;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.TurnCompleteCondition;
import robocode.WinEvent;
import vn.edu.hcmut.ai.tmp_1_Spring2015.minix.Minix;
import vn.edu.hcmut.ai.tmp_1_Spring2015.wave.WaveSurfing;

public class PinkWard extends AdvancedRobot {

	/**
	 * The core, the heart of robot. All event will be handled by
	 * operator.
	 */
	private Operator operator;

	static final int OPERATOR_NUM = 2;
	static final int MINIX = 1;
	static final int WAVING = 0;
	/**
	 * store score info to all knowed enemies.
	 */
	private static Hashtable scoreInfo;
	/**
	 * store total score for each operator, indexed by operator number.
	 */
	private static double[] totalScore;
	/**
	 * store total round operator battle, indexed by operator number.
	 */
	private static int[] totalRound;

	private int currentOperatorIndex;
	private double currentScore;

	// ------------------- main thread for work --------------------
	@Override
	public void run() {
		// init the robot ,for example, choose the operator
		init();
		// begin to work ,may be never return
		operator.work();
	} // run

	// --------------------- function for init the robot---------------------
	/**
	 * Initialize the robot, add some needed custom event.
	 * Try to load previous statistic/score and choose the
	 * strategy for new war.
	 */
	private void init() {
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		// choose the operator base on the operator's score
		if (getRoundNum() == 0) {
			// is the first round load the old score info
			// for choose the first operator
			loadScore();
			setTurnRadarRight(400);
			while (operator == null){
				turnRight(1);
			}
		}else{
			chooseOperator();
		}
		// Registers custom event
		addCustomEvent(new MoveCompleteCondition(this));
		addCustomEvent(new TurnCompleteCondition(this));
		addCustomEvent(new RadarTurnCompleteCondition(this, 5));
		addCustomEvent(new GunTurnCompleteCondition(this));
	} // init

	/**
	 * Choose strategy base on previous score.
	 */
	private void chooseOperator() {
		currentOperatorIndex = -1;
		double bestScore = -1000000, averageScore;
		for (int i = 0; i < OPERATOR_NUM; i++) {
			out.println(OPERATOR_NUM);
			if (totalRound[i] <= 0)
				averageScore = totalScore[i];
			else
				averageScore = totalScore[i] / totalRound[i];
			if (averageScore > bestScore) {
				currentOperatorIndex = i;
				bestScore = averageScore;
			}
		}

		if (currentOperatorIndex == WAVING)
			operator = new WaveSurfing(this);

		else if (currentOperatorIndex == MINIX)
			operator = new Minix(this);
		else
			out.println("unknow operator");

//		operator = new WaveSurfing(this);

		out.println("operator is : " + operator.getName());
		for (int i = 0; i < totalScore.length; i++) {
			if (i == MINIX)
				out.println("Minix score  : " + totalScore[i] + " with "
						+ totalRound[i] + " rounds.");
			else if (i == WAVING)
				out.println("Waving score  : " + totalScore[i] + " with "
						+ totalRound[i] + " rounds.");
			else
				out.println("unknow operator score : " + totalScore[i]);
		}
	}

	// --------------------------------------------------------------
	private void onFinish() {
		totalScore[currentOperatorIndex] += currentScore;
		totalRound[currentOperatorIndex]++;
		for (int i = 0; i < OPERATOR_NUM; i++) {
			if (i != currentOperatorIndex) {
				totalScore[i] += 3d;
			}
		}
		out.println(operator.getName() + " get score : " + currentScore);
		storeScore();
	}

	/**
	 * This method try to get the score of previous session/round
	 * game.
	 */
	private void loadScore() {
		try {
			ObjectInputStream input = new ObjectInputStream(
					new GZIPInputStream(new FileInputStream(
							getDataFile("score.dat"))));
			scoreInfo = (Hashtable) input.readObject();
			try { // close file
				input.close();
			} catch (IOException ioex) {
				out.println(ioex);
			}
		} catch (Exception e) {
			out.println(e);
			scoreInfo = new Hashtable();
		}
	}

	/**
	 * This method write out the data(score) of this round to the find
	 * to use in the feature.
	 */
	private void storeScore() {
		try {
			ObjectOutputStream output = new ObjectOutputStream(
					new GZIPOutputStream(new RobocodeFileOutputStream(
							getDataFile("score.dat"))));
			output.writeObject(scoreInfo);
			output.flush();
			output.close();
		} catch (IOException ioex) {
			out.println(ioex);
		}
	}

	/**
	 * The involved event on scanned robot.
	 * All event will be sent to operator.
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		if (operator == null) { // need choose operator
			totalScore = (double[]) scoreInfo.get(event.getName() + "_score");
			totalRound = (int[]) scoreInfo.get(event.getName() + "_round");
			if (totalScore == null || totalRound == null) {
				totalScore = new double[OPERATOR_NUM];
				totalRound = new int[OPERATOR_NUM];
				scoreInfo.put(event.getName() + "_score", totalScore);
				scoreInfo.put(event.getName() + "_round", totalRound);
			}
			chooseOperator();
		} else
			operator.onScannedRobot(event);
	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		operator.onRobotDeath(event);

		currentScore += 10d;
	}

	@Override
	public void onHitWall(HitWallEvent e) {
		operator.onHitWall(e);

		currentScore -= 3d;
	}

	/**
	 * Involved when 2 robot is hit each other.
	 * Just recalculate the score.
	 */
	@Override
	public void onHitRobot(HitRobotEvent e) {
		operator.onHitRobot(e);

		if (e.isMyFault()) {
			currentScore += 0.6 * 3d;
			// if ram killed the enemy
			if (e.getEnergy() <= 0d)
				currentScore += 18d;
		} else {
			currentScore -= 0.6 * 3d;
		}
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		operator.onHitByBullet(e);

		double score = e.getBullet().getPower() * 4;
		if (e.getBullet().getPower() > 1d)
			score += (e.getBullet().getPower() - 1d) * 2d;
		currentScore -= score;

		if(getEnergy() <= 40 ) chooseOperator();
	}

	@Override
	public void onBulletHit(BulletHitEvent e) {
		operator.onBulletHit(e);

		double score = e.getBullet().getPower() * 4;
		if (e.getBullet().getPower() > 1d)
			score += (e.getBullet().getPower() - 1d) * 2d;
		currentScore += score;
		// if you killed this enemy
		if (e.getEnergy() <= 0d)
			currentScore += 12d;
	}

	@Override
	public void onBulletMissed(BulletMissedEvent e) {
		operator.onBulletMissed(e);
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent e) {
		operator.onBulletHitBullet(e);
	}

	@Override
	public void onDeath(DeathEvent e) {
		currentScore -= 50d;
		currentScore -= getOthers() * 10d;

		operator.onDeath(e);
		operator.onFinish();
		onFinish();
	}

	@Override
	public void onWin(WinEvent e) {
		currentScore += 50d;

		operator.onWin(e);
		operator.onFinish();
		onFinish();
	}

	@Override
	public void onSkippedTurn(SkippedTurnEvent e) {
		operator.onSkippedTurn(e);
	}

	/**
	 * Handle all custom event.
	 */
	@Override
	public void onCustomEvent(CustomEvent event) {
		if (event.getCondition().getName()
				.equals("robocode.MoveCompleteCondition")) {
			operator.onMoveComplete(event);
		} else if (event.getCondition().getName()
				.equals("robocode.TurnCompleteCondition")) {
			operator.onTurnComplete(event);
		} else if (event.getCondition().getName()
				.equals("robocode.GunTurnCompleteCondition")) {
			operator.onGunTurnComplete(event);
		} else if (event.getCondition().getName()
				.equals("robocode.RadarTurnCompleteCondition")) {
			operator.onRadarTurnComplete(event);
		} else
			operator.onCustomEvent(event);
	}

}
