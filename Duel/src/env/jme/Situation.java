package env.jme;

import java.util.ArrayList;
import java.util.List;

import com.jme3.math.Vector3f;

import dataStructures.tuple.Tuple2;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.actionsBehaviours.HuntBehavior;
import sma.actionsBehaviours.LegalActions.LegalAction;
import sma.agents.FinalAgent;


/**
 * Class representing a situation at a given moment.
 * 
 * @author WonbinLIM
 *
 */
public class Situation {
	
	
	public static String SHOOT = "shoot";
	public static String FOLLOW = "follow";
	public static String EXPLORE_OFF = "explore_off";
	public static String EXPLORE_DEF = "explore_def";
	public static String HUNT = "hunt";
	public static String RETREAT = "retreat";
	
	
	// Database : 
	
	public int offSize;
	public int defSize;
	
	public float offValue;
	public float defValue;
	
	public float offScatteringValue;
	public float defScatteringValue;
	
	//Location
	
	public float averageAltitude;
	public float minAltitude;
	public float maxAltitude;
	public float currentAltitude;
	
	public float fovValue;
	
	//
	
	
	
	public String lastAction;
	
	public int life;
	public int timeSinceLastShot;	
	
	
	public boolean enemyInSight;
	public float impactProba;
	
	
	public String enemy;
	
	public boolean victory;
	
	
	public static Situation getCurrentSituation(FinalAgent a){
		Situation sit = new Situation();
		
		sit.offSize = a.offPoints.size();
		sit.defSize = a.defPoints.size();
		
		sit.offValue = getInterestPointSetValue(a.offPoints);
		sit.defValue = getInterestPointSetValue(a.defPoints);
		
		sit.offScatteringValue = 0f;
		sit.defScatteringValue = 0f;
		
		ArrayList<Vector3f> goldenPoints = a.sphereCast(a.getSpatial(), AbstractAgent.VISION_DISTANCE, AbstractAgent.FAR_PRECISION, AbstractAgent.VISION_ANGLE);
		
		setLocationInfo(a, sit, goldenPoints);
		
		sit.lastAction = a.lastAction;
		
		sit.life = a.life;
		sit.timeSinceLastShot = (int) Math.max(0, Math.min(Integer.MAX_VALUE,(System.currentTimeMillis() - a.lastHit))) ;
		
		setEnemyInfo(a, sit);
		
		sit.victory = false;
		
		return sit;
	}
	
	public static void setLocationInfo(FinalAgent a, Situation sit, ArrayList<Vector3f> goldenPoints){
		
		float min = 255f, max = -255f, averageAltitude = 0f, fovValue = 0f;
		
		for(Vector3f point : goldenPoints){
			if (point.getY() > max){
				max = point.getY();
			}
			if (point.getY() < min){
				min = point.getY();
			}
			
			averageAltitude+=point.getY();
			
			fovValue += AbstractAgent.VISION_DISTANCE - a.getSpatial().getWorldTranslation().distance(point);
		}
		
		fovValue += AbstractAgent.VISION_DISTANCE * (AbstractAgent.FAR_PRECISION - goldenPoints.size());
		
		sit.maxAltitude = max;
		sit.minAltitude = min;
		sit.averageAltitude = averageAltitude;
		sit.currentAltitude = a.getSpatial().getWorldTranslation().getY();
		
		sit.fovValue = fovValue;
		
	}
	
	
	public static void setEnemyInfo(FinalAgent a, Situation sit){
		
		
		Tuple2<Vector3f, String> t = HuntBehavior.checkEnemyInSight(a, false);
		
		sit.enemyInSight = false;
		sit.impactProba = 0f;
		
		if(t != null){
			sit.enemy = t.getSecond();
			sit.enemyInSight = true;
			sit.impactProba = a.impactProba(a.getCurrentPosition(), t.getFirst());
		}
		
	}
	
	public static float getInterestPointSetValue(ArrayList<InterestPoint> set){
		float val = 0f;
		
		for(InterestPoint point : set){
			val += point.value;
		}
		return val;
		
		
	}
	
	public static String getCSVColumns(){
		return "OffDataSize;DefDataSize;OffDataValue;DefDataValue;AvgAltitude;MinAltitude;MaxAltitude;CurrentAltitude;FovValue;LastAction;Life;ImpactProba";
		
	}
	
	public String toCSVFile(){
		String res = "";//getCSVColumns()+"\n";
		
		res+=offSize+";"+defSize+";"+offValue+";"+defValue+";"+averageAltitude+";"+minAltitude+";"+maxAltitude+";"+currentAltitude+";"+fovValue+";"+lastAction+";"+life
				+";"+impactProba+";";
		
		res+= (victory)?"VICTORY":"DEFEAT";
		
		return res;
	}
	
	
	
	
}
