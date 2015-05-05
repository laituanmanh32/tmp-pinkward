package vn.edu.hcmut.ai.tmp_1.minixHT.radarHT;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import vn.edu.hcmut.ai.tmp_1.minix.Radar;
import vn.edu.hcmut.ai.tmp_1.minixHT.MinixHT;
/**
 * class RadarHT, define the interface that the UniRadar and MultiRadar should inherit
 * and some usefull tool function.
 */


public abstract class RadarHT extends Radar
{
	 MinixHT operator;

	 RadarHT( MinixHT operator , AdvancedRobot robot ){
		super( robot );
		this.operator = operator;
     }

     // communication interface with the operator, called by operator
	 public abstract void onScannedRobot( ScannedRobotEvent event );
	 public abstract void onRadarTurnComplete( );
     public void printInfo(){}
	 // --------- communication function --------------------

}
