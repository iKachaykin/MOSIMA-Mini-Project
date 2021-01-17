package sma.actionsBehaviours;

import env.jme.NewEnv;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.agents.FinalAgent;

public class RetreatBehavior extends TickerBehaviour {


	private static final long serialVersionUID = -9136258186452459977L;
	
	public static int RETREAT_TIME = 4;
	
	FinalAgent agent;
	
	InterestPoint target;
	InterestPoint lastTarget;
	
	long startTime;

	public RetreatBehavior(Agent a, long period) {
		super(a, period);
		agent = (FinalAgent)((AbstractAgent)a);
		startTime = System.currentTimeMillis();
		System.out.println("RETREAT !!");
	}

	@Override
	protected void onTick() {
		
		if (System.currentTimeMillis() - startTime > RETREAT_TIME * getPeriod()){
			
			System.out.println("End of retreat");
			agent.currentBehavior = null;
			this.stop();
		}
		
		
		if (target == null){
			System.out.println("Looking for new target ...");
			
			InterestPoint point = findNextInterestPoint();
			
			if (point != null){
				target = point;
				agent.goTo(point.position);
				System.out.println("Found it : "+point.position);
				
			}else{
				System.out.println("Back to explore");
				ExploreBehavior ex = new ExploreBehavior(agent, FinalAgent.PERIOD);
				agent.currentBehavior = ex;
				agent.addBehaviour(ex);
				stop();
			}
		}
		
		
		
	}
	
	
public InterestPoint findNextInterestPoint(){
		
		float value = -1f;
		InterestPoint best = null;
		long time = System.currentTimeMillis();
		
		for (InterestPoint point : agent.defPoints){
			
			float tmp = evalutateInterestPoint(point, time);
			if (tmp > value  && point != lastTarget){
				best = point;
				value = tmp;
			}
			
		}
		
		return best;
		
	}
	
	public float evalutateInterestPoint(InterestPoint point, long time){
		
		float dist = agent.getSpatial().getWorldTranslation().distance(point.position);
		long idleness = (time - point.lastVisit) / 1000;
		return NewEnv.MAX_DISTANCE - dist + 5 * Math.max(30 - idleness, 0); 
		
	}

}
