package vn.edu.hcmut.ai.tmp_1.minixHT.radarHT;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import vn.edu.hcmut.ai.tmp_1.minix.Coordinate;
import vn.edu.hcmut.ai.tmp_1.minix.Util;
import vn.edu.hcmut.ai.tmp_1.minixHT.EnemyHT;
import vn.edu.hcmut.ai.tmp_1.minixHT.MinixHT;

/**
 * a Radar Controller used when not only one enemy
 */

public class MultiRadarHT extends RadarHT
{
    private EnemyHT[] enemyInfo;

    private final boolean UPDATE_DEBUG = false;
    private final boolean PERIODS_DEBUG = false;

    private final double DEGREE_MODIFY = 1.1;

    private double centreHeading;
	private double maxBearing;
	private double startScanTime;

	public MultiRadarHT( MinixHT operator , AdvancedRobot robot ){
		super( operator , robot );
        centreHeading = robot.getRadarHeading();
	    maxBearing = 400;
	    startScan();
	}

	public void onScannedRobot( ScannedRobotEvent event ){  // called by event handle onScannedRobot
        enemyInfo = operator.getEnemies();
		if( isScannedAll() ){
			if( PERIODS_DEBUG )
				robot.out.println("scan all enemy advance. " + robot.getTime() );
			update();
			startScan();
		}
	}

    public void onRadarTurnComplete( ){
        enemyInfo = operator.getEnemies();
        if( ( robot.getTime() - startScanTime ) <= (1+DEGREE_MODIFY)*maxBearing/45 ){
		    if( PERIODS_DEBUG )
				robot.out.println("not scan the whole bearing. " + robot.getTime() );
	    }else if( isScannedAll() ){
			if( PERIODS_DEBUG )
				robot.out.println("scan all enemy. " + robot.getTime() );
			update();
		}else{
			if( PERIODS_DEBUG )
				robot.out.println("can not scan all enemy!!!!!!!!! " + robot.getTime() );
			maxBearing = DEGREE_MODIFY*maxBearing ;
		}
        startScan();
	}

	// ------------------- tool function --------------------

    private boolean isScannedAll(){
		if( enemyInfo == null ) return false;
        int scannedNum = 0;
		for( int i = 0; i< enemyInfo.length; i++ )
			if(  enemyInfo[i].getTime() >= startScanTime) scannedNum ++;

        if( scannedNum == robot.getOthers() ) return true;
		else return false;
	}

    private void startScan(){
		startScanTime = robot.getTime();
        computeTurnInfo( centreHeading );
		turnDegree = turnDegree + DEGREE_MODIFY * maxBearing ;
		run();
	}

    private void update(){
		double nowTime = robot.getTime();
		double myX = robot.getX();
		double myY = robot.getY();

		double[] lineHeadings = new double[ enemyInfo.length ];
        int position = 0;
        for( int i = 0; i< enemyInfo.length; i++ ){
			     double x = enemyInfo[i].getX();
				 double y = enemyInfo[i].getY();
                 double distance =
					 enemyInfo[i].getVelocity()*(nowTime - enemyInfo[i].getTime());
                 Coordinate point =
					 Util.computeCoordinate( x, y, enemyInfo[i].getFacingHeading(), distance );
                 x = point.getX();
				 y = point.getY();
				 lineHeadings[ position ++ ] = Util.computeLineHeading( myX, myY, x, y );
		}

        // sort lineHeadings
		for( int i = 1; i< lineHeadings.length; i++ )
			for( int j = 0; j< lineHeadings.length - i; j++ )
			    if( lineHeadings[ j ] > lineHeadings[ j+1 ] ){
					double temp = lineHeadings[ j ];
					lineHeadings[ j ] = lineHeadings[ j+1 ];
					lineHeadings[ j+1 ] = temp;
		        }

        double[] bearings = new double[ enemyInfo.length ];
        for( int i = 0; i< bearings.length -1 ; i++ )
			bearings[i] = lineHeadings[ i+1 ] - lineHeadings[ i ];
        bearings[bearings.length-1] =  // specail deal with
			360 - ( lineHeadings[lineHeadings.length-1] - lineHeadings[0] );

		int max = 0;
		for( int i = 1; i< bearings.length; i++ )
			if( bearings[i] > bearings[max] ) max = i;

        centreHeading = Util.modifyHeading(
			lineHeadings[ max ] + bearings[ max ]/2 + 180 );
        maxBearing = ( 360 - bearings[ max ] )/2;

		if(UPDATE_DEBUG){
			robot.out.println("update enemies info:");
			for( int i = 0; i< lineHeadings.length; i++ )
                robot.out.println("lineHeading " + i + " : " + lineHeadings[i] );
            for( int i = 0; i< bearings.length; i++ )
                robot.out.println("bearing " + i + " : " + bearings[i] );
            robot.out.println("centreHeading : " + centreHeading );
            robot.out.println("maxBearing " + maxBearing );
		}
	}

    //----------------------- for debug ---------------------------

} // class MultiRadarHT