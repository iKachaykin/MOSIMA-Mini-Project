package sma;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import dataStructures.tuple.Tuple2;
import env.EnvironmentManager;
import env.jme.Environment;
import env.jme.NewEnv;
import env.jme.Situation;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import sma.actionsBehaviours.LegalActions.LegalAction;
import sma.agents.FinalAgent;

public class AbstractAgent extends Agent implements EnvironmentManager {
	
	
	private static final long serialVersionUID = 1L;
	private NewEnv env;
	
	
	public static final float VISION_ANGLE = 3f; //Radians
	public static final float VISION_DISTANCE = 25f;
	
	public static final float NEIGHBORHOOD_DISTANCE = 12f;
	
	public static final int CLOSE_PRECISION = 100;
	public static final int FAR_PRECISION = 1000;
	
	public static final int MAX_LIFE = 12;
	public static final int SHOT_DAMAGE = 2;
	
	
	public AbstractAgent() {
		registerO2AInterface(EnvironmentManager.class, this);
	}

	public Vector3f getCurrentPosition() {
		return this.env.getCurrentPosition(getLocalName());
	}
	
	public Vector3f getDestination() {
		return this.env.getDestination(getLocalName());
	}

	public  ArrayList<Tuple2<Vector3f, String>> getVisibleAgents(float range, float angle){
		return env.getVisibleAgents(getLocalName(), range, angle);
	}
	
	public void stopMoving(){
		env.stopMoving(getLocalName());
	}


	public boolean moveTo(Vector3f myDestination) {
		return this.env.moveTo(getLocalName(), myDestination);
	}
	
	public void teleport(Vector3f dest){
		env.teleport(getLocalName(), dest);
	}
	
	public Vector3f getRandomPosition(){
		return env.getRandomPosition();
	}
	
	public void drawBox(Vector3f pos, int color){
		env.drawBox(pos, color);
	}
	

	public boolean randomMove() {
		return this.env.randomMove(getLocalName());
	}

	public boolean shoot(String target) {
		return this.env.shoot(getLocalName(), target);
	}
	
	
	public Spatial getSpatial(){
		return env.getSpatial(getLocalName());
	}
	
	public Vector3f adjusteHeight(Vector3f in){
		return env.adjusteHeight(in);
	}

	public boolean isVisible(String agent, String enemy, float distance){
		return env.isVisible(agent, enemy, distance);
	}
	
	
	public ArrayList<Vector3f> sphereCast(Spatial sp, float distance,  int N, float angle){
		return env.goldenSphereCast(sp, distance, N, angle);
	}
	
	public ArrayList<Vector3f> sphereCast(Spatial sp, float distance,  int N){
		return env.goldenSphereCast(sp, distance, N);
	}
	

	public float impactProba(Vector3f origin, Vector3f target){
		return env.impactProba(origin, target);
	}
	
	public  boolean isVisible(String enemy, float distance){
		return env.isVisible(getLocalName(), enemy, distance);
	}

	/**
	 * Deploy an agent tagged as a player
	 */
	public void deployAgent(NewEnv args, boolean color) {
		this.env = args;
		this.env.deployAgent(getLocalName(), "player", color);
	}

	/**
	 * Deploy an agent tagged as an enemy
	 */
	public void deployEnemy(NewEnv env) {
		this.env = env;
		this.env.deployAgent(getLocalName(), "enemy",false);
	}

	protected void setup() {
		super.setup();
	}
	
	public void addToAgents(FinalAgent agent){
		env.addAgent(agent);
	}
	

	@Override
	public void deployAgent(Environment paramEnvironment) {
		// TODO Auto-generated method stub	
	}
	
	public Vector3f getEnemyLocation(String enemy){
		return env.getCurrentPosition(enemy);
	}
	
}
