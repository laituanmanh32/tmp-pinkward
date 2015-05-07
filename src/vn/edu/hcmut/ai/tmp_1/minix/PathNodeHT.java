package vn.edu.hcmut.ai.tmp_1.minix;

public class PathNodeHT
{
	// base
	public long time;
    public double eX, eY, mX, mY;
    public double eHeading, mHeading, meHeading;
	public double eVelocity, mVelocity;
    // extend
	public double meDistance, oeDistance;
	public double eoBearing, emBearing, meBearing, fireBearing;
	// bullet info
	public double mFire, mBulletHit, eFire, eBulletHit;

	public PathNodeHT(){}

}