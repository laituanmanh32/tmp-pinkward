package vn.edu.hcmut.ai.tmp_1.minixHT.radarHT;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import vn.edu.hcmut.ai.tmp_1.minixHT.EnemyHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.MinixHT;
/**
 *  Radar used when one enemy only
 */

public class UniRadarHT extends RadarHT
{
	EnemyHT enemy;

	public UniRadarHT( MinixHT operator , AdvancedRobot robot ){
		super( operator, robot );
		scan( 400 );
	}

	public void onScannedRobot( ScannedRobotEvent e ){
		if( enemy == null )
		   enemy = operator.getEnemy( e.getName() );
		track( enemy.getLineHeading() );
    }

    public void onRadarTurnComplete( ){
	     if( enemy == null ) scan( 400 );
		 else if( robot.getTime() - enemy.getTime() > 1 ) scan( 400 );
	}

} // class UniRadarHT