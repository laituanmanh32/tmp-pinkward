package vn.edu.hcmut.ai.tmp_1_Spring2015.path;

public class PathNode {
	public int round;
	public long time;
	public double enemyX, enemyY, myX, myY;
	public double enemyHeading, myHeading;
	public double enemyVelocity, myVelocity;
	public double enemyFire = 0, enemyBulletHit = 0;
	public double myFire = 0, myBulletHit = 0;
	public double enemyEnergy, myEnergy;

	public PathNode() {
	}
}
