package sma.actionsBehaviours;

import java.util.ArrayList;

import com.jme3.math.Vector3f;

import dataStructures.tuple.Tuple2;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.agents.FinalAgent;

public class DumbBehavior extends TickerBehaviour {


	FinalAgent agent;
	
	private Vector3f target;
	private long randDate;
	
	public DumbBehavior(Agent a, long period) {
		super(a, period);
		agent = (FinalAgent)((AbstractAgent)a);
	}

	private static final long serialVersionUID = -5055323641823521045L;

	@Override
	protected void onTick() {
		
		
		ArrayList<Tuple2<Vector3f, String>> enemies = agent.getVisibleAgents(AbstractAgent.VISION_DISTANCE, AbstractAgent.VISION_ANGLE);
		
		if(enemies.size()!= 0){
			System.out.println("Dummy Agent - Fire and Forget - ENEMY !");
			agent.shoot(enemies.get(0).getSecond());
		}
		
		randomMove();

	}
	
	void randomMove(){
		long time = System.currentTimeMillis();
		if(time - randDate > 5 * getPeriod()){
			agent.randomMove(); 
			randDate = time;
			
		}
	}

}
