package sma.actionsBehaviours;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.agents.FinalAgent;

public class TempSphereCast extends TickerBehaviour {

	
	FinalAgent agent;
	Camera cam;
	
	public TempSphereCast(Agent a, long period) {
		super(a, period);
		agent = (FinalAgent)((AbstractAgent)a);
		cam = agent.getSpatial().getUserData("cam");
		agent.randomMove();
	}

	private static final long serialVersionUID = 8942553736315457097L;

	
	
	float x,y = 0f;
	
	protected void onTick() {
		
		Quaternion q = new Quaternion();
		Vector3f dir = cam.getDirection().clone();
		
		q.lookAt(dir, Vector3f.UNIT_X);
		
		q = q.fromAngleAxis(x/360f, Vector3f.UNIT_X);
		
		cam.setRotation(q);
		
		q = new Quaternion();
		dir = cam.getDirection().clone();
		
		q.lookAt(dir, Vector3f.UNIT_X);
		
		
		
		q = q.fromAngleAxis(y/360f, Vector3f.UNIT_Y);
		
		
		cam.setRotation(q);
		
		if(x < 360f*6.30f){
			x+=5f;
		}else{
			y+=50f;
			x = 0f;
		}
		System.out.println(x+" "+y);

	}

}
