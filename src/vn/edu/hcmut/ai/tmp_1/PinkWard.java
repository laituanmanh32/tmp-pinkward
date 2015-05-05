package vn.edu.hcmut.ai.tmp_1;

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

public class PinkWard extends AdvancedRobot {
	/**
	 * the operator be choosed to battle in current round.
	 */
	private Operator operator;
	/**
	 * there are two robot operator.
	 */
	static final int OPERATOR_NUM = 2;
	static final int MINIX_HT = 0;
	static final int LOLITA = 1;
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
	private int enemyAmount;

	// ------------------- main thread for work --------------------
	public void run() {
		// init the robot ,for example, choose the operator
		init();
		// begin to work ,may be never return
		operator.work();
	} // run

	// --------------------- function for init the robot---------------------
	private void init() {
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		enemyAmount = getOthers();
		// choose the operator base on the operator's score
		if (getRoundNum() == 0) {
			// is the first round load the old score info
			// for choose the first operator
			loadScore();
			if (getOthers() == 1) { // uni mode
									// to choose operator, first want to know
									// the enemy's name
				setTurnRadarRight(400);
				// wait for radar to scaned the enemy, and choose a operator
				while (operator == null)
					turnRight(1);
			} else { // multi mode
				totalScore = (double[]) scoreInfo.get("multi_score");
				totalRound = (int[]) scoreInfo.get("multi_round");
				if (totalScore == null || totalRound == null) {
					totalScore = new double[OPERATOR_NUM];
					totalRound = new int[OPERATOR_NUM];
					scoreInfo.put("multi_score", totalScore);
					scoreInfo.put("multi_round", totalRound);
				}
				chooseOperator();
			}
		} else
			chooseOperator();

		// Registers custom event
		addCustomEvent(new MoveCompleteCondition(this));
		addCustomEvent(new TurnCompleteCondition(this));
		addCustomEvent(new RadarTurnCompleteCondition(this, 5));
		addCustomEvent(new GunTurnCompleteCondition(this));
	} // init

	private void chooseOperator() {
		currentOperatorIndex = -1;
		double bestScore = -1000000, averageScore;
		for (int i = 0; i < OPERATOR_NUM; i++) {
			if (totalRound[i] <= 0)
				averageScore = totalScore[i];
			else
				averageScore = totalScore[i] / totalRound[i];
			if (averageScore > bestScore) {
				currentOperatorIndex = i;
				bestScore = averageScore;
			}
		}

		// if (currentOperatorIndex == MINIX_HT)
		// operator = new MinixHT(this);
		// else if (currentOperatorIndex == LOLITA)
		// operator = new Lolita(this);
		// else
		// out.println("unknow operator");

		out.println("operator is : " + operator.getName());
		for (int i = 0; i < totalScore.length; i++) {
			if (i == MINIX_HT)
				out.println("MinixHT score  : " + totalScore[i] + " with "
						+ totalRound[i] + " rounds.");
			else if (i == LOLITA)
				out.println("Lolita score  : " + totalScore[i] + " with "
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
				totalScore[i] += 3d * Math.sqrt(enemyAmount);
			}
		}
		out.println(operator.getName() + " get score : " + currentScore);
		storeScore();
	}

	// -----------------------------------------------------------------------
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

	// -----------------------------------------------------------------
	// all robot event will be send to the operator
	// system event
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

	public void onRobotDeath(RobotDeathEvent event) {
		operator.onRobotDeath(event);

		currentScore += enemyAmount * 10d;
	}

	public void onHitWall(HitWallEvent e) {
		operator.onHitWall(e);

		currentScore -= 3d;
	}

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

	public void onHitByBullet(HitByBulletEvent e) {
		operator.onHitByBullet(e);

		double score = e.getBullet().getPower() * 4;
		if (e.getBullet().getPower() > 1d)
			score += (e.getBullet().getPower() - 1d) * 2d;
		currentScore -= score;
	}

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

	public void onBulletMissed(BulletMissedEvent e) {
		operator.onBulletMissed(e);
	}

	public void onBulletHitBullet(BulletHitBulletEvent e) {
		operator.onBulletHitBullet(e);
	}

	public void onDeath(DeathEvent e) {
		currentScore -= 50d;
		currentScore -= getOthers() * 10d;

		operator.onDeath(e);
		operator.onFinish();
		onFinish();
	}

	public void onWin(WinEvent e) {
		currentScore += 50d;

		operator.onWin(e);
		operator.onFinish();
		onFinish();
	}

	public void onSkippedTurn(SkippedTurnEvent e) {
		operator.onSkippedTurn(e);
	}

	// custom event handle
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
