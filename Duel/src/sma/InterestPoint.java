package sma;

import java.util.ArrayList;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;


public class InterestPoint {
	
	
	public static final boolean DRAW_POINTS = true;
	
	public static enum Type {
		Offensive,
		Defensive;
	}
	
	public static float HEIGHT_COEFF = 1f;
	
	public static float INFLUENCE_ZONE = 35f;
	
	public Type type;
	public Vector3f position;
	public float value;
	
	public long lastVisit;
	
	public InterestPoint(Type type, AbstractAgent agent){
		this.type = type;
		this.position = agent.getSpatial().getWorldTranslation().clone();
		this.lastVisit = System.currentTimeMillis();
		
		if(type == Type.Offensive)
			EvalOffendValue(agent);
		else
			EvalDefendValue(agent);
		
		if (DRAW_POINTS){
			if( type == Type.Offensive){
				agent.drawBox(position, 0);
			}else{
				agent.drawBox(position, 1);
			}
		}
		
	}
	

	public void EvalOffendValue(AbstractAgent agent){
		
		ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), INFLUENCE_ZONE, AbstractAgent.FAR_PRECISION);
		
		
		int diff = AbstractAgent.FAR_PRECISION - points.size();
		
		value += diff * INFLUENCE_ZONE; 
		
		for (Vector3f point : points){
			value += INFLUENCE_ZONE - position.distance(point);
		}
		// Maximum value  = INFLUENCE_ZONE * FAR_PRECISION
		//value = agent.getSpatial().getWorldTranslation().y;
	}
	
	public void EvalDefendValue(AbstractAgent agent){ //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ WRONG JUST A COPY
		
		ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), INFLUENCE_ZONE, AbstractAgent.FAR_PRECISION);
		
		int diff = AbstractAgent.FAR_PRECISION - points.size();
		
		value += diff * INFLUENCE_ZONE; 
		
		for (Vector3f point : points){
			value += INFLUENCE_ZONE - position.distance(point);
		}
	}
	
	public boolean isInfluenceZone(Vector3f oth, Type othType){	
		
		//float dist = (float) Math.sqrt((position.x - oth.x)*(position.x - oth.x) + (position.z-oth.x)*(position.z-oth.z));
		float dist = position.distance(oth);
		return type == othType && dist < INFLUENCE_ZONE; 
	}
	
	
	
}
