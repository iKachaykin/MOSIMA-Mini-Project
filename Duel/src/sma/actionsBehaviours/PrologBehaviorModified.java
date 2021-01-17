package sma.actionsBehaviours;

import org.jpl7.Query;
import org.lwjgl.Sys;

import com.jme3.math.Vector3f;

import dataStructures.tuple.Tuple2;
import env.jme.NewEnv;
import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.agents.FinalAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class PrologBehaviorModified extends TickerBehaviour {


	private static final long serialVersionUID = 5739600674796316846L;

	public static FinalAgent agent;
	public static Class nextBehavior;

	public static Situation sit;
	public static String victoryLearningBasePath = "./ressources/learningBase/victory/victory.csv";
	public static String defeatLearningBasePath = "./ressources/learningBase/defeat/defeat.csv";
	public static String stateChangedLearningBasePath = "./ressources/learningBase/stateChanged/stateChanged.csv";
	
	public FileWriter victoryFileWriter;
	public FileWriter defeatFileWriter;
	public FileWriter stateChangedFileWriter;


	public PrologBehaviorModified(Agent a, long period) {
		super(a, period);
		agent = (FinalAgent)((AbstractAgent)a);
		try {
			victoryFileWriter = new FileWriter(victoryLearningBasePath, true);
			defeatFileWriter = new FileWriter(defeatLearningBasePath, true);
			stateChangedFileWriter = new FileWriter(stateChangedLearningBasePath, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	@Override
	protected void onTick() {
		try {
			String prolog = "consult('./ressources/prolog/duel/requete.pl')";

			if (!Query.hasSolution(prolog)) {
				System.out.println("Cannot open file " + prolog);
			}
			else {
				sit = Situation.getCurrentSituation(agent);
				List<String> behavior = Arrays.asList("explore", "hunt", "attack");
				ArrayList<Object> terms = new ArrayList<Object>();

				for (String b : behavior) {
					terms.clear();
					// Get parameters 
					if (b.equals("explore")) {
						terms.add(sit.timeSinceLastShot);
						terms.add(((ExploreBehavior.prlNextOffend)?sit.offSize:sit.defSize ));
						terms.add(InterestPoint.INFLUENCE_ZONE);
						terms.add(NewEnv.MAX_DISTANCE);
					}
					else if (b.equals("hunt")) {
						terms.add(sit.life);
						terms.add(sit.timeSinceLastShot);
						terms.add(sit.offSize);
						terms.add(sit.defSize);
						terms.add(InterestPoint.INFLUENCE_ZONE);
						terms.add(NewEnv.MAX_DISTANCE);
						terms.add(sit.enemyInSight);
					}else if(b.equals("attack")){
						//terms.add(sit.life);
						terms.add(sit.enemyInSight);
						//terms.add(sit.impactProba);
					}
					else { // RETREAT
						terms.add(sit.life);
						terms.add(sit.timeSinceLastShot);
					}

					String query = prologQuery(b, terms);
					if (Query.hasSolution(query)) {
						//System.out.println("has solution");
						setNextBehavior();

					}
				}
				
				if (sit.victory) {
					victoryFileWriter.write(sit.toCSVFile() + "\n");
					victoryFileWriter.close();
					defeatFileWriter.close();
					stateChangedFileWriter.close();
				}
				else if (sit.life <= 0) {
					defeatFileWriter.write(sit.toCSVFile() + "\n");
					victoryFileWriter.close();
					defeatFileWriter.close();
					stateChangedFileWriter.close();
				}
				else if (sit.enemyInSight || sit.timeSinceLastShot < 10) {
					stateChangedFileWriter.write(sit.toCSVFile() + "\n");
				}
				
			}
		}catch(Exception e) {
			System.err.println("Behaviour file for Prolog agent not found");
			System.exit(0);
		}
	}



	public void setNextBehavior(){

		if(agent.currentBehavior != null && nextBehavior == agent.currentBehavior.getClass()){
			return;
		}
		if (agent.currentBehavior != null){
			agent.removeBehaviour(agent.currentBehavior);
		}

		if (nextBehavior == ExploreBehavior.class){
			ExploreBehavior ex = new ExploreBehavior(agent, FinalAgent.PERIOD);
			agent.addBehaviour(ex);
			agent.currentBehavior = ex;

		}else if(nextBehavior == HuntBehavior.class){
			HuntBehavior h = new HuntBehavior(agent, FinalAgent.PERIOD);
			agent.currentBehavior = h;
			agent.addBehaviour(h);

		}else if(nextBehavior == Attack.class){

			Attack a = new Attack(agent, FinalAgent.PERIOD, sit.enemy);
			agent.currentBehavior = a;
			agent.addBehaviour(a);

		}


	}


	public String prologQuery(String behavior, ArrayList<Object> terms) {
		String query = behavior + "(";
		for (Object t: terms) {
			query += t + ",";
		}
		return query.substring(0,query.length() - 1) + ")";
	}

	public static void executeExplore() {
		//System.out.println("explore");
		nextBehavior = ExploreBehavior.class;
	}


	public static void executeHunt() {
		//System.out.println("hunt");
		nextBehavior = HuntBehavior.class;
	}

	public static void executeAttack() {
		//System.out.println("attack");
		nextBehavior = Attack.class;
	}


	public static void executeRetreat() {
		//System.out.println("retreat");
		//nextBehavior = RetreatBehavior.class;
	}

}