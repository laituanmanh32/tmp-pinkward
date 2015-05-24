package vn.edu.hcmut.ai.tmp_1_Spring2015.wave;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import robocode.AdvancedRobot;
import robocode.Condition;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class WaveGun {

	private static double bearingDirection = 1, lastLatVel, lastVelocity, /*
																		 * lastReverseTime
																		 * ,
																		 * circleDir
																		 * = 1,
																		 * enemyFirePower
																		 * ,
																		 */
			enemyEnergy, enemyDistance, lastVChangeTime, enemyLatVel,
			enemyVelocity/* , enemyFireTime, numBadHits */;
	private static Point2D.Double enemyLocation;
	private static final int GF_ZERO = 15;
	private static final int GF_ONE = 30;
	private static String enemyName;
	private static int[][][][][][] guessFactors = new int[3][5][3][3][8][GF_ONE + 1];

	Color randomColor;
	private AdvancedRobot bot;

	public WaveGun(AdvancedRobot bot) {
		this.bot = bot;
	}

	public void run() {
		// setColors(Color.red, Color.white, Color.white);
		bot.setAdjustGunForRobotTurn(true);
		bot.setAdjustRadarForGunTurn(true);
		do {
			bot.turnRadarRightRadians(Double.POSITIVE_INFINITY);
		} while (true);
	}

	public void onScannedRobot(ScannedRobotEvent e) {

		/*-------- setup data -----*/
		if (enemyName == null) {

			enemyName = e.getName();
		}
		Point2D.Double robotLocation = new Point2D.Double(bot.getX(),
				bot.getY());
		double theta;
		double enemyAbsoluteBearing = bot.getHeadingRadians()
				+ e.getBearingRadians();
		enemyDistance = e.getDistance();
		enemyLocation = projectMotion(robotLocation, enemyAbsoluteBearing,
				enemyDistance);

		enemyEnergy = e.getEnergy();

		Rectangle2D.Double BF = new Rectangle2D.Double(18, 18, 764, 564);

		MicroWave w = new MicroWave();

		lastLatVel = enemyLatVel;
		lastVelocity = enemyVelocity;
		enemyLatVel = (enemyVelocity = e.getVelocity())
				* Math.sin(e.getHeadingRadians() - enemyAbsoluteBearing);

		int distanceIndex = (int) enemyDistance / 100;

		double bulletPower = distanceIndex == 0 ? 3 : 2;
		theta = Math.min(bot.getEnergy() / 4,
				Math.min(enemyEnergy / 4, bulletPower));
		if (theta == bulletPower)
			bot.addCustomEvent(w);
		bulletPower = theta;
		w.bulletVelocity = 20D - 3D * bulletPower;

		int accelIndex = (int) Math.round(Math.abs(enemyLatVel)
				- Math.abs(lastLatVel));

		if (enemyLatVel != 0)
			bearingDirection = enemyLatVel > 0 ? 1 : -1;
		w.bearingDirection = bearingDirection
				* Math.asin(8D / w.bulletVelocity) / GF_ZERO;

		double moveTime = w.bulletVelocity * lastVChangeTime++ / enemyDistance;
		int bestGF = moveTime < .1 ? 1 : moveTime < .3 ? 2 : moveTime < 1 ? 3
				: 4;

		int vIndex = (int) Math.abs(enemyLatVel / 3);

		if (Math.abs(Math.abs(enemyVelocity) - Math.abs(lastVelocity)) > .6) {
			lastVChangeTime = 0;
			bestGF = 0;

			accelIndex = (int) Math.round(Math.abs(enemyVelocity)
					- Math.abs(lastVelocity));
			vIndex = (int) Math.abs(enemyVelocity / 3);
		}

		if (accelIndex != 0)
			accelIndex = accelIndex > 0 ? 1 : 2;

		w.firePosition = robotLocation;
		w.enemyAbsBearing = enemyAbsoluteBearing;
		// now using PEZ' near-wall segment
		w.waveGuessFactors = guessFactors[accelIndex][bestGF][vIndex][BF
				.contains(projectMotion(robotLocation, enemyAbsoluteBearing
						+ w.bearingDirection * GF_ZERO, enemyDistance)) ? 0
				: BF.contains(projectMotion(robotLocation, enemyAbsoluteBearing
						+ .5 * w.bearingDirection * GF_ZERO, enemyDistance)) ? 1
						: 2][distanceIndex];

		bestGF = GF_ZERO;

		for (int gf = GF_ONE; gf >= 0 && enemyEnergy > 0; gf--)
			if (w.waveGuessFactors[gf] > w.waveGuessFactors[bestGF])
				bestGF = gf;

		bot.setTurnGunRightRadians(Utils
				.normalRelativeAngle(enemyAbsoluteBearing
						- bot.getGunHeadingRadians() + w.bearingDirection
						* (bestGF - GF_ZERO)));

		if (bot.getEnergy() > 4 || distanceIndex == 0)
			bot.setFire(bulletPower);
		if (e.getEnergy() <= 0.1) {
			if (bot.getTime() % 10 == 0) {
				Random rand = new Random();
				int r = rand.nextInt(255);
				int g = rand.nextInt(255);
				int b = rand.nextInt(255);

				randomColor = new Color(r, g, b);
			}
			bot.setScanColor(randomColor);
		}

		bot.setTurnRadarRightRadians(Utils
				.normalRelativeAngle(enemyAbsoluteBearing
						- bot.getRadarHeadingRadians()) * 2);

	}

	private static Point2D.Double projectMotion(Point2D.Double loc,
			double heading, double distance) {

		return new Point2D.Double(loc.x + distance * Math.sin(heading), loc.y
				+ distance * Math.cos(heading));
	}

	private static double absoluteBearing(Point2D.Double source,
			Point2D.Double target) {
		return Math.atan2(target.x - source.x, target.y - source.y);
	}

	class MicroWave extends Condition {

		Point2D.Double firePosition;
		int[] waveGuessFactors;
		double enemyAbsBearing, distance, bearingDirection, bulletVelocity;

		@Override
		public boolean test() {

			if ((WaveGun.enemyLocation).distance(firePosition) <= (distance += bulletVelocity)
					+ bulletVelocity) {
				try {
					waveGuessFactors[(int) Math.round((Utils
							.normalRelativeAngle(absoluteBearing(firePosition,
									WaveGun.enemyLocation) - enemyAbsBearing))
							/ bearingDirection + GF_ZERO)]++;
				} catch (ArrayIndexOutOfBoundsException e) {
				}
				bot.removeCustomEvent(this);
			}
			return false;
		}
	}

}