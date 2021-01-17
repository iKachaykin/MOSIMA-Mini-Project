package env.jme;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.lwjgl.Sys;

import com.bulletphysics.collision.narrowphase.GjkEpaSolver.Results;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingSphere;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainPatch;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jogamp.opengl.math.geom.Frustum;

import dataStructures.tuple.Tuple2;
import env.terrain.TerrainTools;
import sma.AbstractAgent;
import sma.actionsBehaviours.LegalActions;
import sma.actionsBehaviours.LegalActions.LegalAction;
import sma.actionsBehaviours.LegalActions.Orientation;
import sma.actionsBehaviours.PrologBehavior;
import sma.agents.FinalAgent;


/**
 * Class assembling the NewEnv with every objects composing the world (terrain, players,..).
 * 
 * @author Pablo Petit et Cédric Herpson
 *
 */
public class NewEnv extends SimpleApplication {


	public static float MAX_DISTANCE;
	
	private final int LIFE = 9;
	private final int DAMAGE = 3;



	// Scene Stuff
	private BulletAppState bulletAppState;
	public Tuple2<Integer, float[]> heightmap_tuplet;
	private TerrainQuad terrain;
	private Material mat_terrain;
	private HashMap<String, Geometry> marks = new HashMap<String, Geometry>();

	// Nodes :
	private Node bulletNode;
	private Node terrainNode;
	private Node playersNode;
	
	private Node camNode;


	// Players
	private HashMap<String, Spatial> players = new HashMap<String, Spatial>();
	
	private HashMap<String, FinalAgent> agents = new HashMap<>();
	
	public static String victoryLearningBasePath = "./ressources/learningBase/victory/victory.csv";
	public static String defeatLearningBasePath = "./ressources/learningBase/defeat/defeat.csv";
	
	public FileWriter victoryFileWriter;
	public FileWriter defeatFileWriter;
	public FileWriter stateChangedFileWriter;


    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ ENVIRONEMENT CREATION @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@


	public static NewEnv launch(String filename){
		NewEnv env = new NewEnv(filename);
		SimpleApplication app = env;
		app.start();
		return env;
	}

	public static NewEnv launchRandom(int size) {
		NewEnv env = new NewEnv(size);
		SimpleApplication app = env;
		app.start();
		return env;
	}


	public NewEnv(int size) {
		this.heightmap_tuplet =  TerrainTools.getPerlinAlgoMap(size);
		bulletAppState = new BulletAppState();
		bulletAppState.setSpeed(0.2f);
		MAX_DISTANCE = heightmap_tuplet.getFirst();
		try {
			victoryFileWriter = new FileWriter(victoryLearningBasePath, true);
			defeatFileWriter = new FileWriter(defeatLearningBasePath, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public NewEnv(String filename){
		this.heightmap_tuplet = TerrainTools.getHeightMapFromTxt(filename);
		bulletAppState = new BulletAppState();
		bulletAppState.setSpeed(0.2f);
		MAX_DISTANCE = heightmap_tuplet.getFirst();
		try {
			victoryFileWriter = new FileWriter(victoryLearningBasePath, true);
			defeatFileWriter = new FileWriter(defeatLearningBasePath, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void simpleInitApp() {
		stateManager.attach(bulletAppState);

		bulletNode = new Node("bullet");
		terrainNode = new Node("terrainNode");
		playersNode = new Node("players");
		camNode = new Node("cam");

		rootNode.attachChild(bulletNode);
		rootNode.attachChild(playersNode);
		rootNode.attachChild(terrainNode);
		rootNode.attachChild(camNode);

		//cam.setViewPort(0.0f, 1.0f, 0.6f, 1.0f);
		cam.setLocation(new Vector3f(21.384611f, 146.78105f, 155.05727f));
		cam.lookAtDirection(new Vector3f(-0.0016761336f, -0.9035275f, -0.42852688f), new Vector3f(-0.003530928f, 0.4285301f, -0.9035206f));

		flyCam.setMoveSpeed(100);

		initKeys();
		
		makeTerrain();

	}

	public PhysicsSpace getPhysicsSpace() {
		return this.bulletAppState.getPhysicsSpace();
	}


	public void makeTerrain() {
		mat_terrain = new Material(assetManager, 
				"Common/MatDefs/Terrain/Terrain.j3md");

		mat_terrain.setTexture("Alpha", assetManager.loadTexture(
				"Textures/Terrain/splat/alphamap.png"));

		Texture grass = assetManager.loadTexture(
				"Textures/Terrain/splat/grass.jpg");
		grass.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex1", grass);
		mat_terrain.setFloat("Tex1Scale", 64f);

		Texture dirt = assetManager.loadTexture(
				"Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex2", dirt);
		mat_terrain.setFloat("Tex2Scale", 32f);

		Texture rock = assetManager.loadTexture(
				"Textures/Terrain/splat/road.jpg");
		rock.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex3", rock);
		mat_terrain.setFloat("Tex3Scale", 128f);

		int patchSize = 65;
		terrain = new TerrainQuad("my terrain", patchSize, this.heightmap_tuplet.getFirst()+1, this.heightmap_tuplet.getSecond());

		terrain.setMaterial(mat_terrain);
		terrain.setLocalTranslation(0, -255, 0);
		terrain.setLocalScale(2f, 1f, 2f);
		terrain.setName("TERRAIN");

		TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
		terrain.addControl(control);


		terrain.addControl(new RigidBodyControl(0));
		getPhysicsSpace().add(terrain.getControl(RigidBodyControl.class));


		terrainNode.attachChild(terrain);

	}
	
	/**
	 * Declaring the "Picking" action, mapping to its triggers and adding flyCam
	 * actions.
	 */
	private void initKeys() {

		// To allow picking we should change the default mapping of the flyCam
		this.inputManager.setCursorVisible(true);
		this.flyCam.setEnabled(true);
		this.flyCam.setDragToRotate(true);
		this.flyCam.setMoveSpeed(50f);// increase the cam speed

		//this.inputManager.addMapping("FLYCAM_RotateDrag",new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

		this.inputManager.addMapping("FLYCAM_StrafeLeft", new KeyTrigger(
				KeyInput.KEY_J));
		this.inputManager.addMapping("FLYCAM_StrafeRight", new KeyTrigger(
				KeyInput.KEY_L));
		this.inputManager.addMapping("FLYCAM_Forward", new KeyTrigger(
				KeyInput.KEY_P));
		this.inputManager.addMapping("FLYCAM_Backward", new KeyTrigger(
				KeyInput.KEY_M));
		this.inputManager.addMapping("FLYCAM_Rise", new KeyTrigger(
				KeyInput.KEY_I));
		this.inputManager.addMapping("FLYCAM_Lower", new KeyTrigger(
				KeyInput.KEY_K));

		// Picking association
		//this.inputManager.addMapping("Picking", new MouseButtonTrigger(MouseInput.BUTTON_LEFT)); // trigger 2: left-button click

	}
	

	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ AGENT DEPLOIMENT@@@@@ @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@$
	
	
	
	public synchronized void drawBox(Vector3f pos, int color){
		Box b  = new Box(2, 2, 2); 
		Geometry player = new Geometry("Box", b);
		player.setModelBound(new BoundingBox());
		player.updateModelBound();
		player.updateGeometricState();
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		if (color == 0){
			mat.setColor("Color", ColorRGBA.Red);
		}else if (color == 1){
			mat.setColor("Color", ColorRGBA.Blue);
		}
		
		player.setMaterial(mat);
		player.scale(0.25f);
		rootNode.attachChild(player);
		//player.getWorldTranslation().set(pos);
		player.setLocalTranslation(pos);
	}

	public synchronized void addAgent(FinalAgent agent){
		agents.put(agent.getLocalName(), agent);
	}

	
	public synchronized boolean deployAgent(String agentName, String playertype, boolean color) {
		if (this.players.containsKey(agentName)) {
			System.out.println("DeployAgent Error : A player with the name '"+agentName+"' already exists.");
			return false;
		}
		else {

			SphereCollisionShape capsuleShape = new SphereCollisionShape(2);
			PlayerControl physicsPlayer = new PlayerControl(capsuleShape, 0.05f, terrain);
			physicsPlayer.setJumpSpeed(5);
			physicsPlayer.setFallSpeed(500);
			physicsPlayer.setGravity(500);
			physicsPlayer.setMaxSlope(500f);

			try {
				wait(5000);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			getPhysicsSpace().add(physicsPlayer);

			Box b  = new Box(2, 2, 2); // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ HERE WE CAN DO SOMETHING TO VISUALIZE ORIENTATION @@@@@@@@@@@@@@@@
			Geometry player = new Geometry("Box", b);

			player.setModelBound(new BoundingBox());
			player.updateModelBound();
			player.updateGeometricState();
			
			Material mat;
			
		
			
			if (color) {
				mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
				
				
				Camera cam1 = cam.clone();
				cam1.setViewPort(0f, .5f, 0f, 0.6f);
				cam1.setLocation(player.getLocalTranslation());
				player.setUserData("cam", cam1);
				
				physicsPlayer.setCam(cam1);
				ViewPort view1 = renderManager.createMainView("Bottom Left", cam1);
				view1.setClearFlags(true, true, true);
				
				view1.attachScene(camNode);
				
				view1.setEnabled( false);
				
			}
			else {
				mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
				mat.setColor("Color", ColorRGBA.Yellow);
				
				
				Camera cam2 = cam.clone();
				cam2.setViewPort(.5f, 1f, 0f, 0.6f);
				cam2.setLocation(player.getLocalTranslation());
				player.setUserData("cam", cam2);
				
				physicsPlayer.setCam(cam2);
				ViewPort view2 = renderManager.createMainView("Bottom Right", cam2);
				view2.setClearFlags(true, true, true);
				
				
				view2.attachScene(camNode);
				
				view2.setEnabled(false);
			}

			player.setMaterial(mat);
			player.scale(0.25f);
			player.addControl(physicsPlayer);
			player.setUserData("name", agentName);
			player.setUserData("playertype", playertype);
			player.setUserData("life", LIFE);
			player.setName(agentName);		      

			playersNode.attachChild(player);

			this.players.put(agentName, player);

		}
		return true;
	}

	private Spatial getBullet() {
		Node node = new Node("bullet");
		
		Sphere sphere = new Sphere(10, 10, 0.45f);
		Spatial bullet = new Geometry("Sphere", sphere);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Yellow);
		bullet.setMaterial(mat);

		node.attachChild(bullet);
		return node;
	}




    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ AGENT FUNCTIONS@@@@@@ @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@


	public synchronized void teleport(String agent,Vector3f dest){
		if (players.containsKey(agent)) {
			Spatial player = players.get(agent);
			player.getControl(PlayerControl.class).teleport(dest);;
			
		}
		System.out.println("moveTo Error : the agent "+agent+" doesn't exist.");
	}
	
	public synchronized void stopMoving(String agent){
		if (players.containsKey(agent)) {
			Spatial player = players.get(agent);
			((PlayerControl)player.getControl(PlayerControl.class)).destination = null;
			((PlayerControl)player.getControl(PlayerControl.class)).ismoving = false;
		}
	}

	public synchronized boolean moveTo(String agent, Vector3f dest) {
		if (players.containsKey(agent)) {
			Spatial player = players.get(agent);
			if (player.getWorldTranslation().distance(dest) > 1f){
				player.getControl(PlayerControl.class).moveTo(dest);
				return true;
			}
			else {
				return false;
			}
		}
		System.out.println("moveTo Error : the agent "+agent+" doesn't exist.");
		return false;
	}


	public synchronized boolean randomMove(String agent) { // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ SHOULD BE MODIFIED TO ALLOW RANGE SETTING @@@@@@@@@@@@@
		if (players.containsKey(agent)) {
			int max = heightmap_tuplet.getFirst()-(heightmap_tuplet.getFirst()%10);
			int min = -max;
			float randx = new Random().nextFloat()*(max - min) + min;
			float randz = new Random().nextFloat()*(max - min) + min;
			Vector3f dest = new Vector3f(randx, terrain.getHeightmapHeight(new Vector2f(randx, randz))-255f, randz);
			return moveTo(agent, dest);
		}
		return false;
	}
	
	public synchronized Vector3f adjusteHeight(Vector3f in){
		return new Vector3f(in.x, terrain.getHeightmapHeight(new Vector2f(in.x, in.z))-255f, in.z);
	}

	
	public synchronized Vector3f getRandomPosition(){
		
		float min =  - MAX_DISTANCE / 2f;
		
		Random r = new Random(System.currentTimeMillis());
		
		Vector3f res = new Vector3f(min + r.nextFloat() * MAX_DISTANCE, 0f, min + r.nextFloat() * MAX_DISTANCE);
		
		return adjusteHeight(res);
		
	}

	

	public synchronized boolean shoot(String agent, String enemy) {
		if (players.containsKey(agent) && players.containsKey(enemy)) {

			Vector3f origin = getCurrentPosition(agent);
			Vector3f target = getCurrentPosition(enemy);
			Vector3f dir = target.subtract(origin).normalize();
			
			FinalAgent enemyAgent = agents.get(enemy);
			
			if (isVisible(agent, enemy, MAX_DISTANCE)) {

				Random r = new Random();
				float impact = impactProba(origin, target);
				
				System.out.println(agent+" shooting : "+impact);

				if ( r.nextFloat() < impact){
					// Target shot
					System.out.println("Env - shot successfull !!");
					
					enemyAgent.life -= AbstractAgent.SHOT_DAMAGE;
					enemyAgent.lastHit = System.currentTimeMillis();

					if (enemyAgent.life <=0) {
						enemyAgent.dead = true;

						System.out.println(enemy+" killed.");
						System.out.println("Simulation done");
						
						if(!enemy.equals("Player1")){
							PrologBehavior.sit.victory = true;
							System.out.println("Victory!");
							try {
								victoryFileWriter.write(PrologBehavior.sit.toCSVFile() + "\n");
								victoryFileWriter.close();
								defeatFileWriter.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else {
							System.out.println("Defeat :(");
							try {
								defeatFileWriter.write(PrologBehavior.sit.toCSVFile() + "\n");
								victoryFileWriter.close();
								defeatFileWriter.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						saveCSV();
						System.exit(0);
					}

					return true;
				}else{
					System.out.println("Shot Missed");
				}

			}else{
				System.out.println("There is nobody to shoot here ...");
			}

		}
		return false;
	}
	
	public static void saveCSV(){
		
		String res = PrologBehavior.sit.toCSVFile();
		int id = new Random().nextInt(10000);
		System.out.println(res);
		try{
		    PrintWriter writer = new PrintWriter(System.getProperty("user.dir")+"/ressources/simus/Mosimu_"+id+".csv", "UTF-8");
		    writer.println(res);
		    writer.close();
		    System.out.println("Execution result saved in /ressources/simus/");
		} catch (IOException e) {
		  System.out.println(e);
		  System.out.println("Experiment saving failed");
		}
		
	}

	public synchronized float impactProba(Vector3f origin, Vector3f target){

		float distCoeff = 0.8f; // Should be public static final 
		float altCoeff = 0.2f;

		float randomness = 0.95f;

		float dist = origin.distance(target);
		float altDiff = origin.getY() - target.getY();

		float distValue = (MAX_DISTANCE - dist ) / MAX_DISTANCE;

		float altValue = altDiff / 255f;

		float proba =  ( distValue * distCoeff + altValue * altCoeff ) * randomness;
		
		return  proba;
	}
	

	public synchronized boolean isVisible(String agent, String enemy, float distance) { // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ TODO : CHANGE TO ANGLE + CAST
		Vector3f origin = getCurrentPosition(agent);
		Vector3f target = getCurrentPosition(enemy);
		Vector3f dir = target.subtract(origin).normalize();

		BoundingVolume bv = players.get(enemy).getWorldBound();
		bv.setCheckPlane(0);
		
		Camera cam = ((Camera)players.get(agent).getUserData("cam"));

		if (cam.getDirection().angleBetween(dir) < AbstractAgent.VISION_ANGLE) { // in angle ??
			
			Ray rayTerrain = new Ray(origin, dir);
			Ray rayPlayers = new Ray(origin, dir);
			
			rayTerrain.setLimit(distance);
			rayPlayers.setLimit(distance);
			
			
			CollisionResults resultsTerrain = new CollisionResults();
			CollisionResults resultsPlayers= new CollisionResults();
			
			terrainNode.collideWith(rayTerrain, resultsTerrain);
			playersNode.collideWith(rayPlayers, resultsPlayers);
			
			Vector3f terrainHit = (resultsTerrain.size()==0)?null:resultsTerrain.getCollision(0).getContactPoint();
			Vector3f playerHit = (resultsPlayers.size()==0)?null:resultsPlayers.getCollision(0).getContactPoint();
			
			if(resultsPlayers.size()!=0 && (resultsTerrain.size()==0 || origin.distance(playerHit) >  origin.distance(terrainHit))){
				return true;
			}
			
		}
		return false;
	}


	public synchronized ArrayList<Tuple2<Vector3f, String>> getVisibleAgents(String agentName, float range, float angle){

		Spatial agentPosition = getSpatial(agentName);
		
		Vector3f camDir = ((Camera)players.get(agentName).getUserData("cam")).getDirection();
		

		ArrayList<Tuple2<Vector3f, String>> res = new ArrayList<>();

		for (String enemy : players.keySet()) {
			Spatial enemyPosition = getSpatial(agentName);
			
			Vector3f dir = enemyPosition.getWorldTranslation().subtract(agentPosition.getWorldTranslation());
			
			if (isVisible(agentName, enemy, range) && camDir.angleBetween(dir) < angle){
				res.add(new Tuple2<Vector3f, String>(players.get(enemy).getWorldTranslation(), enemy));
			}
		
		}
		return res;
	}


    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ RAY CASTING @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@


	
	public synchronized ArrayList<Vector3f> goldenSphereCast(Spatial sp, float distance, int N){
		
		ArrayList<Vector3f> goldenPoints = getGoldenSphere(N);
		ArrayList<Vector3f> result = new ArrayList<>();
		
		Vector3f direction;
		Vector3f position = sp.getWorldTranslation();
		
		
		for(Vector3f golden : goldenPoints){
			
			golden = golden.add(position);
			direction = position.subtract(golden).normalize(); 
			Vector3f rayHit = shootRay(position, direction, distance);
			
			if(rayHit != null){
				result.add(rayHit);
			}
			
		}
		return result;
	}
	
	public synchronized ArrayList<Vector3f> goldenSphereCast(Spatial sp, float distance, int N, float angle){
		return filterWithVisionAngle(sp,goldenSphereCast(sp, distance, N), ((Camera)sp.getUserData("cam")).getDirection(), angle);
	}
	
	
	public synchronized ArrayList<Vector3f> filterWithVisionAngle(Spatial sp,ArrayList<Vector3f> in,Vector3f dir, float maxAngle){  // @@@@@@@@@@@@@@@@@@@@@@@@@@@  WRONGGGGGGGGGGGGGGG
		ArrayList<Vector3f> filtered = new ArrayList<>();
		Vector3f position = sp.getWorldTranslation(), vTo;
		for(Vector3f v3 : in){
			
			vTo = v3.subtract(position).normalize();
			
			if(dir.angleBetween(vTo) < maxAngle){
				filtered.add(v3);
			}
		}
		return filtered;
	}
	
	
	public synchronized ArrayList<Vector3f> getGoldenSphere(int N){
		ArrayList<Vector3f> points = new ArrayList<>();
				
		double y,r,phi;
		float X,Y,Z;
		
		double inc = (Math.PI * (3 - Math.sqrt(5d)));
		double off = 2f / N;
		
		for(int k = 0;k < N; k ++){
			
			y = k * off - 1 + (off / 2f);
			r = Math.sqrt(1 - y*y);
			phi = k * inc;
			
			X = (float) (Math.cos(phi) * r);
			Y = (float) y;
			Z = (float) (Math.sin(phi)*r);
			
			points.add(new Vector3f(X,Y,Z));
		}
	
		return points;
	}
	
	
	
	public Vector3f shootRay(Vector3f point, Vector3f direction, float distance){
		CollisionResults res = new CollisionResults();
		res.clear();
		final Ray ray = new Ray();
		ray.setOrigin(point);
		ray.setDirection(direction);
		ray.setLimit(distance);
		terrainNode.collideWith(ray, res);
		
		if (res.size()>0){
			return res.getCollision(0).getContactPoint();
		}
		
		return null;
	}



	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ GETTERS @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@


	public synchronized Spatial getSpatial(String agent){
		return players.get(agent);
		
	}

	public synchronized Vector3f getCurrentPosition(String agent) {
		if (players.containsKey(agent)) {
			return players.get(agent).getWorldTranslation();
		}
		System.out.println("getCurrentPosition Error : the agent "+agent+" doesn't exist.");
		return null;
	}


	public synchronized Vector3f getDestination(String agent) {
		Spatial ag = players.get(agent);
		Vector3f dest = ag.getControl(PlayerControl.class).getDestination();
		return dest;
	}




	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ USELESS @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@



	private void explode(Vector3f coord) {
		ParticleEmitter fire =
				new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
		Material mat_red = new Material(assetManager,
				"Common/MatDefs/Misc/Particle.j3md");
		mat_red.setTexture("Texture", assetManager.loadTexture(
				"Effects/Explosion/flame.png"));
		fire.setMaterial(mat_red);
		fire.setLocalTranslation(coord);
		fire.setImagesX(2);
		fire.setImagesY(2); // 2x2 texture animation
		fire.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
		fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
		fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
		fire.setStartSize(1.5f);
		fire.setEndSize(0.1f);
		fire.setGravity(0, 0, 0);
		fire.setLowLife(1f);
		fire.setHighLife(3f);
		fire.getParticleInfluencer().setVelocityVariation(0.3f);
		rootNode.attachChild(fire);

		ParticleEmitter debris =
				new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 10);
		Material debris_mat = new Material(assetManager,
				"Common/MatDefs/Misc/Particle.j3md");
		debris_mat.setTexture("Texture", assetManager.loadTexture(
				"Effects/Explosion/Debris.png"));
		debris.setMaterial(debris_mat);
		debris.setLocalTranslation(coord);
		debris.setImagesX(3);
		debris.setImagesY(3); // 3x3 texture animation
		debris.setRotateSpeed(4);
		debris.setSelectRandomImage(true);
		debris.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 4, 0));
		debris.setStartColor(ColorRGBA.White);
		debris.setGravity(0, 6, 0);
		debris.getParticleInfluencer().setVelocityVariation(.60f);
		rootNode.attachChild(debris);
		debris.emitAllParticles();
	}

}
