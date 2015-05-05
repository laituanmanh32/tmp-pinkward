package vn.edu.hcmut.ai.tmp_1.minix;
import robocode.*;

/* 
 * base class for minix operator.
 * class Gun, define the interfacer that control the gun .
 */	

public abstract class Gun
{
	protected boolean turnDirection; // direction the gun will be turn 
	protected double  turnDegree; // degree the gun will be turn 
	protected double  fireHeading; // heading the gun will be turn to 
	protected double bulletPower; // power of the bullet will be fire

    protected AdvancedRobot robot;

	protected Gun ( AdvancedRobot robot ) {
		this.robot = robot;
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

}