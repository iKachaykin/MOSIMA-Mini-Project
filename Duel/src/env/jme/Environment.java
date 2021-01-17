package env.jme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
import sma.actionsBehaviours.LegalActions;
import sma.actionsBehaviours.LegalActions.LegalAction;
import sma.actionsBehaviours.LegalActions.Orientation;


/**
 * Class assembling the environment with every objects composing the world (terrain, players,..).
 * 
 * @author WonbinLIM
 *
 */
public class Environment extends SimpleApplication {

	//	private int time = 0;
	//	private int endtime;

	private BulletAppState bulletAppState;
	public Tuple2<Integer, float[]> heightmap_tuplet;
	private TerrainQuad terrain;
	private Material mat_terrain;
	//	private Node terrainNode;

	private Node shootables;
	private Node notshootables;
	
	private Node terrainNode;

	//	private Camera cam1;
	//	private Camera cam2;

	private HashMap<String, Spatial> players = new HashMap<String, Spatial>();
	private HashMap<String, LegalAction> lastActions = new HashMap<String, LegalAction>();
	//	private Node playersNode;
	//	private Node enemyNode;	
	private Node bulletNode;

	private HashMap<String, Geometry> marks = new HashMap<String, Geometry>();

	private final int FIELDOFVIEW = 35;
	private final int LIFE = 9;
	private final int DAMAGE = 3;

	//	private Spatial player1;
	//	private PlayerControl physicsPlayer1;
	//	private Node player1Node;

	//	private Spatial player2;
	//	private CharacterControl physicsPlayer2;
	//	private Node player2Node;



	public static void main(String[] args) {
		Environment.launchRandom(64);
		//		Environment.launch("flat_terrain_64");
	}

	/**
	 * Launches the given file's heightmap.
	 * -warning- the heightmap file must be a txt file, using the following syntaxe :
	 * 
	 * sizeoftheheightmap
	 * int:int:int:.....:int
	 * int:int:int:.....:int
	 * ...
	 * int:int:int:.....:int
	 * 
	 * 
	 * - each int is a integer representing the height of one position of the map.
	 * - the size of the heightmap must be a power of two (ex: 64,128,..).
	 * 
	 * @param filename name of the file containing the heightmap
	 * @return the created environment
	 */
	public static Environment launch(String filename){
		Environment env = new Environment(filename);
		SimpleApplication app = env;
		app.start();
		return env;
	}

	/**
	 * Generates and launches a random heightmap of the given size.
	 * - the size of the heightmap must be a power of two (ex: 64,128,..).
	 * @param size size of the heightmap
	 * @return the created environment
	 */
	public static Environment launchRandom(int size) {
		Environment env = new Environment(size);
		SimpleApplication app = env;
		app.start();
		return env;
	}


	/**
	 * Constructor, which implements the heightmap by random generation.
	 * @param size
	 */
	public Environment(int size) {
		//		this.heightmap_tuplet =  TerrainTools.getRandomMap(size);
		//		for (int i=0; i<heightmap_tuplet.getFirst()*heightmap_tuplet.getFirst(); i++)  System.out.println(heightmap_tuplet.getSecond()[i]);
		this.heightmap_tuplet =  TerrainTools.getPerlinAlgoMap(size);
		bulletAppState = new BulletAppState();
		bulletAppState.setSpeed(0.2f);
	}

	/**
	 * Constructor, which implements the heightmap using a file.
	 * @param filename
	 */
	public Environment(String filename){
		this.heightmap_tuplet = TerrainTools.getHeightMapFromTxt(filename);
		bulletAppState = new BulletAppState();
		bulletAppState.setSpeed(0.2f);
	}

	@Override
	public void simpleInitApp() {
		stateManager.attach(bulletAppState);

		//	    terrainNode = new Node("terrain");
		//	    playersNode = new Node("player");
		//	    enemyNode = new Node("enemy");
		bulletNode = new Node("bullet");
		shootables = new Node("shootables");
		terrainNode = new Node("terrainNode");
		notshootables= new Node("notshootables");

		//	    rootNode.attachChild(terrainNode);
		//	    rootNode.attachChild(playersNode);
		//	    rootNode.attachChild(enemyNode);
		rootNode.attachChild(bulletNode);
		rootNode.attachChild(shootables);
		rootNode.attachChild(notshootables);
		rootNode.attachChild(terrainNode);

		cam.setViewPort(0.0f, 1.0f, 0.6f, 1.0f);
		cam.setLocation(new Vector3f(21.384611f, 146.78105f, 155.05727f));
		cam.lookAtDirection(new Vector3f(-0.0016761336f, -0.9035275f, -0.42852688f), new Vector3f(-0.003530928f, 0.4285301f, -0.9035206f));

		flyCam.setMoveSpeed(50);
		//	    flyCam.setEnabled(false);

		makeTerrain();


	}



	/**
	 * -Local use only-
	 * @return
	 */
	public PhysicsSpace getPhysicsSpace() {
		return this.bulletAppState.getPhysicsSpace();
	}


	/**
	 * Create the world's terrain.
	 */
	public void makeTerrain() {
		/** 1. Create terrain material and load four textures into it. */
		mat_terrain = new Material(assetManager, 
				"Common/MatDefs/Terrain/Terrain.j3md");

		/** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
		mat_terrain.setTexture("Alpha", assetManager.loadTexture(
				"Textures/Terrain/splat/alphamap.png"));

		/** 1.2) Add GRASS texture into the red layer (Tex1). */
		Texture grass = assetManager.loadTexture(
				"Textures/Terrain/splat/grass.jpg");
		grass.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex1", grass);
		mat_terrain.setFloat("Tex1Scale", 64f);

		/** 1.3) Add DIRT texture into the green layer (Tex2) */
		Texture dirt = assetManager.loadTexture(
				"Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex2", dirt);
		mat_terrain.setFloat("Tex2Scale", 32f);

		/** 1.4) Add ROAD texture into the blue layer (Tex3) */
		Texture rock = assetManager.loadTexture(
				"Textures/Terrain/splat/road.jpg");
		rock.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex3", rock);
		mat_terrain.setFloat("Tex3Scale", 128f);

		/** 2. load the height map */
		int patchSize = 65;
		terrain = new TerrainQuad("my terrain", patchSize, this.heightmap_tuplet.getFirst()+1, this.heightmap_tuplet.getSecond());
		/** 4. We give the terrain its material, position & scale it, and attach it. */
		terrain.setMaterial(mat_terrain);
		terrain.setLocalTranslation(0, -255, 0);
		terrain.setLocalScale(2f, 1f, 2f);
		terrain.setName("TERRAIN");

		/** 5. The LOD (level of detail) depends on were the camera is: */
		TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
		terrain.addControl(control);

		/** 6. Add physics: */

		terrain.addControl(new RigidBodyControl(0));
		getPhysicsSpace().add(terrain.getControl(RigidBodyControl.class));

		//	    terrainNode.attachChild(terrain);
		shootables.attachChild(terrain);
		terrainNode.attachChild(terrain);

	}


	/**
	 * Creates a new agent of  given type in the environment.
	 * @param agentName name of the agent we want to deploy.
	 * @param playertype type of the agent : player or enemy.
	 * @return true if the agent is deployed, false if an agent with this name already exists.
	 */
	public synchronized boolean deployAgent(String agentName, String playertype) {
		if (this.players.containsKey(agentName)) {
			System.out.println("DeployAgent Error : A player with the name '"+agentName+"' already exists.");
			//			System.exit(0);
			return false;
		}
		else {

			SphereCollisionShape capsuleShape = new SphereCollisionShape(2);
			PlayerControl physicsPlayer = new PlayerControl(capsuleShape, 0.05f, terrain);
			physicsPlayer.setJumpSpeed(5);
			physicsPlayer.setFallSpeed(500);
			physicsPlayer.setGravity(500);
			physicsPlayer.setMaxSlope(500f);

			// we make the function wait 5 seconds for letting the objets be created before.
			try {
				wait(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getPhysicsSpace().add(physicsPlayer);


			//			Spatial player = assetManager.loadModel("Models/Oto/Oto.mesh.xml");
			//			Spatial player = assetManager.loadModel("assets/Models/GR-75MediumTransport.blend");
			Box b  = new Box(2, 2, 2);
			Geometry player = new Geometry("Box", b);

			player.setModelBound(new BoundingBox());
			player.updateModelBound();
			player.updateGeometricState();
			//			Spatial player = assetManager.loadModel("Models/Test/BasicCubeLow.obj");
			Material mat;
			if (playertype.equals("player")) {
				mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
				Camera cam1 = cam.clone();
				cam1.setViewPort(0f, .5f, 0f, 0.6f);
				cam1.setLocation(player.getLocalTranslation());
				player.setUserData("cam", cam1);
				physicsPlayer.setCam(cam1);
				ViewPort view1 = renderManager.createMainView("Bottom Left", cam1);
				view1.setClearFlags(true, true, true);
				view1.attachScene(rootNode);
			}
			else {
				mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
				mat.setColor("Color", ColorRGBA.Red);
				Camera cam2 = cam.clone();
				cam2.setViewPort(.5f, 1f, 0f, 0.6f);
				cam2.setLocation(player.getLocalTranslation());
				player.setUserData("cam", cam2);
				physicsPlayer.setCam(cam2);
				ViewPort view2 = renderManager.createMainView("Bottom Right", cam2);
				view2.setClearFlags(true, true, true);
				view2.attachScene(rootNode);
			}

			player.setMaterial(mat);
			player.scale(0.25f);
			player.addControl(physicsPlayer);
			//		    physicsPlayer.setAnim(player);
			player.setUserData("name", agentName);
			player.setUserData("playertype", playertype);
			player.setUserData("life", LIFE);
			player.setName(agentName);		      

			shootables.attachChild(player);


			this.players.put(agentName, player);
			this.lastActions.put(agentName, null);

			//randomMove(agentName);

			// test for arrow
			//		    Arrow arrow = new Arrow(Vector3f.UNIT_Z.mult(2));
			//		    Geometry mark = new Geometry("arrow", arrow);
			//			Material m1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
			//			m1.setColor("Color", ColorRGBA.Green);
			//			mark.setMaterial(m1);
			//			marks.put(agentName, mark);
		}
		return true;
	}



	/**
	 * -Local use only-
	 * Creates and returns a bullet object.
	 * @return a bullet
	 */
	private Spatial getBullet() {
		Node node = new Node("bullet");
		//		Picture pic = new Picture("Bullet");
		//        Texture2D tex = (Texture2D) assetManager.loadTexture("src/main/resources/assets/Textures/Bullet.png");
		Sphere sphere = new Sphere(10, 10, 0.45f);
		Spatial bullet = new Geometry("Sphere", sphere);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Yellow);
		bullet.setMaterial(mat);

		//	    Material mat_red = new Material(assetManager,
		//	            "Common/MatDefs/Misc/Particle.j3md");
		//	    mat_red.setTexture("Texture", assetManager.loadTexture(
		//	            "Effects/Explosion/flame.png"));
		//	    bullet.setMaterial(mat_red);

		node.attachChild(bullet);
		return node;
	}

	/**
	 * makes an agent looks in the specific cardinal direction (8 possible directions : North, North-East, East, South-East, South, South-West, West, North-West).
	 * @param agent name of the agent we want to move.
	 * @param direction cardinal-direction N, NE, E, SE, S, SW, W, NW.
	 */
	public synchronized void lookAt(String agent, LegalAction direction) {
		if (players.containsKey(agent)) {
			Spatial player = players.get(agent);
			Camera cam = ((Camera)players.get(agent).getUserData("cam"));
			Vector3f currentPosition = new Vector3f(0,0,0);
			switch (direction) {
			case LOOKTO_NORTH :
				currentPosition.setZ(currentPosition.z-30);
				break;
			case LOOKTO_NORTHEAST:
				currentPosition.setZ(currentPosition.z-30);
				currentPosition.setX(currentPosition.x+30);
				break;
			case LOOKTO_EAST:
				currentPosition.setX(currentPosition.x+30);
				break;
			case LOOKTO_SOUTHEAST:
				currentPosition.setZ(currentPosition.z+30);
				currentPosition.setX(currentPosition.x+30);
				break;
			case LOOKTO_SOUTH:
				currentPosition.setZ(currentPosition.z+30);
				break;
			case LOOKTO_SOUTHWEST:
				currentPosition.setZ(currentPosition.z+30);
				currentPosition.setX(currentPosition.x-30);
				break;
			case LOOKTO_WEST:
				currentPosition.setX(currentPosition.x-30);
				break;
			case LOOKTO_NORTHWEST:
				currentPosition.setZ(currentPosition.z-30);
				currentPosition.setX(currentPosition.x-30);
				break;
			default:
				System.out.println("Error, no compatible action");
				System.exit(-1);
			}
			player.getControl(PlayerControl.class).setViewDirection(currentPosition);
			cam.setLocation(player.getWorldTranslation());
			cam.lookAtDirection(currentPosition, Vector3f.UNIT_Y);
			this.lastActions.put(agent, direction);

		}
	}


	/**
	 * Makes an agent go to the specific coordinates of the world.
	 * @param agent name of the agent we want to move.
	 * @param dest coordinates of the destination.
	 * @return true if we can move the agent, false if the agent's already there.
	 */
	public synchronized boolean moveTo(String agent, Vector3f dest) {
		if (players.containsKey(agent)) {
			Spatial player = players.get(agent);
			if (!approximativeEquals(player.getWorldTranslation().x, dest.x) || !approximativeEquals(player.getWorldTranslation().z, dest.z) || !approximativeEquals(player.getWorldTranslation().y, dest.y)) {
				//			if (!player.getWorldTranslation().equals(dest)) {
				//				System.out.println("not arrived");
				player.getControl(PlayerControl.class).moveTo(dest);

				return true;
			}
			else {
				//				System.out.println("arrived");
				return false;
			}
		}
		System.out.println("moveTo Error : the agent "+agent+" doesn't exist.");
		return false;
	}


	/**
	 * -Local use only-
	 * Makes an agent go to move in a given direction (forward, backward, left, right).
	 * WARNING : no limit for this moving function in the map, so the agent could go over the map and then fall.
	 * @param agent name of the agent we want to move.
	 * @param direction FORWARD, BACKWARD, LEFT, RIGHT.
	 * @return true if we can move the agent, false if the agent doesn't exist.
	 */
	private synchronized boolean directionalMove(String agent, String direction) {		
		if (players.containsKey(agent)) {

			Spatial player = players.get(agent);

			Camera cam = ((Camera)players.get(agent).getUserData("cam"));
			Vector3f camDir = cam.getDirection().clone().multLocal(0.8f);
			Vector3f camLeft = cam.getLeft().clone().multLocal(0.8f);			
			Vector3f walkDirection = new Vector3f(0, 0, 0);

			switch (direction) {
			case "FORWARD" :
				walkDirection.addLocal(camDir);
				break;
			case "BACKWARD":
				walkDirection.addLocal(camDir.negate());
				break;
			case "LEFT":
				walkDirection.addLocal(camLeft);
				break;
			case "RIGHT":
				walkDirection.addLocal(camLeft.negate());
				break;
			}
			player.getControl(PlayerControl.class).move(walkDirection);
			return true;
		}
		return false;
	}

	/**
	 * Makes an agent go to move in a given cardinal direction (8 possible directions : North, North-East, East, South-East, South, South-West, West, North-West).
	 * @param agent name of the agent we want to move.
	 * @param direction cardinal-direction N, NE, E, SE, S, SW, W, NW.
	 * @return
	 */
	public synchronized boolean cardinalMove(String agent, LegalAction direction) {
		int max = heightmap_tuplet.getFirst()-(heightmap_tuplet.getFirst()%10);
		Vector3f position = players.get(agent).getWorldTranslation().clone();
		lookAt(agent, LegalActions.MoveToLook(direction));
		boolean res = true;
		switch (direction) {
		case MOVE_NORTH :
			if (-max < position.z) res = directionalMove(agent, "FORWARD");
			break;
		case MOVE_NORTHEAST:
			if (-max < position.z && position.x < max) res = directionalMove(agent, "FORWARD");
			break;
		case MOVE_EAST:
			if (position.x < max) res = directionalMove(agent, "FORWARD");
			break;
		case MOVE_SOUTHEAST:
			if (position.z < max && position.x < max) res = directionalMove(agent, "FORWARD");
			break;
		case MOVE_SOUTH:
			if (position.z < max) res = directionalMove(agent, "FORWARD");
			break;
		case MOVE_SOUTHWEST:
			if (position.z < max && -max < position.x) res = directionalMove(agent, "FORWARD");
			break;
		case MOVE_WEST:
			if (-max < position.x) res = directionalMove(agent, "FORWARD");
			break;
		case MOVE_NORTHWEST:
			if (-max < position.z && -max < position.x) res = directionalMove(agent, "FORWARD");
			break;
		default:
			System.out.println("Error, no compatible action");
			System.exit(-1);
			return false;
		}
		if (res) {
			this.lastActions.put(agent, direction);
			return true;
		}
		return false;
	}

	/**
	 * Makes an agent go to random coordinates of the world.
	 * @param agent
	 * @return true if we can move the agent, false if the agent's already there.
	 */
	public synchronized boolean randomMove(String agent) {
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



	/**
	 * Shooting process : an agent shoot at another agent.
	 * @param agent the name of the agent who wants to shoot.
	 * @param enemy the name of the target agent.
	 * @return true if the shooting has succeeded, false if the enemy doesn't exist or if the shooting has failed.
	 */
	public boolean shoot(String agent, String enemy) {
		if (players.containsKey(agent) && players.containsKey(enemy)) {

			Vector3f origin = getCurrentPosition(agent);
			Vector3f target = getCurrentPosition(enemy);
			Vector3f dir = target.subtract(origin).normalize();

			if (isVisible(agent, enemy)) {
				//				// arrow
				//				((Arrow) (marks.get(agent).getMesh())).setArrowExtent(Vector3f.UNIT_Z.mult(origin.distance(closest.getContactPoint())));
				//				marks.get(agent).setLocalTranslation(closest.getContactPoint());
				//				Quaternion q = new Quaternion();
				//				q.lookAt(dir, Vector3f.UNIT_Z);
				//				marks.get(agent).setLocalRotation(q);
				//				rootNode.attachChild(marks.get(agent));

				//				System.out.println("closest: "+closest.getGeometry().getWorldTranslation());
				//				System.out.println("target: "+players.get(enemy).getWorldTranslation());
				this.lastActions.put(agent, LegalAction.SHOOT);
				Random r = new Random();
				if (r.nextFloat()<0.7) {
					players.get(agent).getControl(PlayerControl.class).setViewDirection(dir);
					Spatial bullet = getBullet();
					bullet.setLocalTranslation(origin);
					bullet.addControl(new BulletControl(dir));
					bulletNode.attachChild(bullet);
					System.out.println("bang");

					int enemyLife = ((int)players.get(enemy).getUserData("life"))-DAMAGE;
					if (enemyLife<=0) {
						System.out.println(enemy+" killed.");
						explode(target);
						//			                	playersNode.detachChildNamed(enemy);
						shootables.detachChildNamed(enemy);
						rootNode.detachChild(marks.get(agent));
						players.remove(enemy);
					}
					else {
						players.get(enemy).setUserData("life", enemyLife);
					}
					return true;
				}
				else {
					System.out.println("target missed");
					return false;
				}
			}
		}
		return false;
	}


	/**
	 * Check for an agent, if the target enemy is visible (if he's on his field of view and their distance is less than the limit).
	 * @param agent the name of the agent who wants to check.
	 * @param enemy the name of the target agent.
	 * @return true if the enemy is visible, false if not.
	 */
	private boolean isVisible(String agent, String enemy) {
		Vector3f origin = getCurrentPosition(agent);
		Vector3f target = getCurrentPosition(enemy);
		Vector3f dir = target.subtract(origin).normalize();

		BoundingVolume bv = players.get(enemy).getWorldBound();
		bv.setCheckPlane(0);


		if (((Camera)players.get(agent).getUserData("cam")).contains(bv).equals(FrustumIntersect.Inside)) {
			Ray ray = new Ray(origin, dir);
			ray.setLimit(FIELDOFVIEW);
			CollisionResults results = new CollisionResults();
			shootables.collideWith(ray, results);
			if (results.size()>1) {
				CollisionResult closest = results.getCollision(1);
				if ( approximativeEqualsCoordinates(closest.getGeometry().getWorldTranslation(), players.get(enemy).getWorldTranslation())) {
					if (origin.distance(target)<=FIELDOFVIEW) {
						return true;
					}
				}
			}
		}
		return false;
	}


	/**
	 * -Local use only-
	 * Creates a ray tracing into the given coordinates of the agent's field of view and returns the contact point's coordinates if there is one.
	 * @param ag the name of the agent who wants to check.
	 * @param camera the field of view of the agent.
	 * @param xOffset
	 * @param yOffset
	 * @return the coordinates of the contact point, null if there isn't any contact.
	 */
	private Vector3f intersects(String ag, Camera camera, final float xOffset, final float yOffset) {
		final Vector3f point = players.get(ag).getWorldTranslation().clone();
		final Vector3f direction = camera.getDirection().clone();
		point.setX(point.getX() + xOffset);
		point.setY(point.getY() + yOffset);

		CollisionResults res = new CollisionResults();
		res.clear();
		final Ray ray = new Ray();
		ray.setOrigin(point);
		ray.setDirection(direction);
		ray.setLimit(FIELDOFVIEW);
		shootables.collideWith(ray, res);

		if (res.size() > 0) {
			int size = 0;
			while (res.size() >= size && res.getCollision(size).getClass().equals(Geometry.class) ) {
				size++;
			}
			if (res.size()>size+1) {
				size++;
			}
			CollisionResult closest = res.getCollision(size);
			//		    System.out.println(ag+":size="+res.size()+";"+size+":"+closest.getGeometry()+" ++ "+closest.getGeometry().getClass());
			if (closest.getGeometry().getClass().equals(TerrainPatch.class)) {
				//		    	System.out.println("my position : "+players.get(ag).getWorldTranslation()+" contact point : "+closest.getContactPoint());
				return closest.getContactPoint();
			}		    
		}
		return null;
	}


	/**
	 * observes around the agent, according to its field of view, and returns all the situation's datas.
	 * @param ag name of the agent who observes.
	 * @param rayDistance the distance between each ray tracing (used to manage the computation complexity)
	 * @return an instance of the Situation class.
	 */
	//@param camera the field of view of the agent.

	public Situation observe(String ag, int rayDistance) {
		Camera camera = ((Camera)players.get(ag).getUserData("cam"));
		Vector3f agentPos = players.get(ag).getWorldTranslation();
		float highest = -255;
		Vector3f highestPosition = null;
		float lowest = 255;
		Vector3f lowestPosition = null;
		int nb = 0;
		float sum = 0;
		float maxDepth = 0;
		HashMap<Float, Integer> heights = new HashMap<Float, Integer>();


		for (int x = 0; x < camera.getWidth() / 2; x = x + rayDistance) {
			for (int y = 0; y < camera.getHeight() / 2; y = y + rayDistance) {

				ArrayList<Vector3f> points = new ArrayList<Vector3f>();
				Vector3f x1 = intersects(ag, camera, x, 0);
				if (x1 != null) { 
					points.add(x1);
					nb++;
					sum += x1.y;
					if (x1.distance(agentPos) > maxDepth) {
						maxDepth = x1.distance(agentPos);
					}
					heights.put(x1.y, 1);

				}
				Vector3f x2 = intersects(ag, camera, -x, 0);
				if (x2 != null) {
					points.add(x2);
					nb++;
					sum += x2.y;
					if (x2.distance(agentPos) > maxDepth) {
						maxDepth = x2.distance(agentPos);
					}
					heights.put(x2.y, 1);
				}
				Vector3f x3 = intersects(ag, camera, 0, y);
				if (x3 != null) {
					points.add(x3);
					nb++;
					sum += x3.y;
					if (x3.distance(agentPos) > maxDepth) {
						maxDepth = x3.distance(agentPos);
					}
					heights.put(x3.y, 1);
				}
				Vector3f x4 = intersects(ag, camera, 0, -y);
				if (x4 != null) {
					points.add(x4);
					nb++;
					sum += x4.y;
					if (x4.distance(agentPos) > maxDepth) {
						maxDepth = x4.distance(agentPos);
					}
					heights.put(x4.y, 1);
				}

				if (points.size() > 0) {
					Vector3f max = maxAltitude((ArrayList<Vector3f>)points.clone());
					if (max.y > highest) {
						highestPosition = max;
						highest = max.y;
					}
					Vector3f min = minAltitude((ArrayList<Vector3f>)points.clone());
					if (min.y < lowest) {
						lowestPosition = min;
						lowest = min.y;
					}
				}	      
			}
		}
		
		return null;//new Situation(FIELDOFVIEW,(LegalAction)players.get(ag).getUserData("lastAction"), agentPos, lowestPosition, highestPosition, sum/nb, nb, maxDepth, heights.size()*1./nb, observeAgents(ag));
	}


	/**
	 * -Local use only-
	 * returns the highest position for a given view.
	 * @param points set of all the coordinates perceived by the agent.
	 * @return the highest position.
	 */
	private Vector3f maxAltitude(ArrayList<Vector3f> points) {
		Vector3f highestPosition = points.remove(0);
		float highest = highestPosition.y;
		for (Vector3f v : points) {
			if (v.y > highest) {
				highestPosition = v;
				highest = v.y;
			}
		}
		return highestPosition;
	}

	/**
	 * -Local use only-
	 * returns the lowest position for a given view.
	 * @param points set of all the coordinates perceived by the agent.
	 * @return the lowest position.
	 */
	private Vector3f minAltitude(ArrayList<Vector3f> points) {
		Vector3f lowestPosition = points.remove(0);
		float lowest = lowestPosition.y;
		for (Vector3f v : points) {
			if (v.y < lowest) {
				lowestPosition = v;
				lowest = v.y;
			}
		}
		return lowestPosition;
	}


	/**
	 * -Local use only-
	 * observes around the agent, according to its field of view, and returns all the agents detected.
	 * @param agentName name of the agent who observes.
	 * @return a list of all the agents around the observer.
	 */
	private synchronized List<Tuple2<Vector3f, String>> observeAgents(String agentName) {

		List<Tuple2<Vector3f, String>> res = new ArrayList();

		Vector3f agentPosition = getCurrentPosition(agentName);
		for (String enemy : players.keySet()) {
			Vector3f enemyPosition = getCurrentPosition(enemy);
			Vector3f dir = enemyPosition.subtract(agentPosition).normalize();
			Ray ray = new Ray(agentPosition, dir);
			ray.setLimit(FIELDOFVIEW);
			CollisionResults results = new CollisionResults();
			shootables.collideWith(ray, results);
			//System.out.println("before : " + results.size());
			if (results.size()>1){// A ray can be casted towards the enemy
				//System.out.println("after : " + results.size());
				CollisionResult closest = results.getCollision(1);// WAS 1 @@@@@@@@@@@@@@
				// The enemy is within the FoV and not protected by the env
				if (agentPosition.distance(enemyPosition)<=FIELDOFVIEW && closest.getGeometry().equals(players.get(enemy))) {
					res.add(new Tuple2<Vector3f, String>(enemyPosition, enemy));
				}
			}
		}
		return res;
	}

	/**
	 * returns the agent's current position
	 * @param agent name of the agent we want the current position.
	 * @return the current position of the agent (of type Vector3f).
	 */
	public synchronized Vector3f getCurrentPosition(String agent) {
		if (players.containsKey(agent)) {
			return players.get(agent).getWorldTranslation();
		}
		System.out.println("getCurrentPosition Error : the agent "+agent+" doesn't exist.");
		return null;
	}

	/**
	 * returns the destination for a given agent.
	 * @param agent name of the agent we want the destination.
	 * @return the destination of the agent.
	 */
	public synchronized Vector3f getDestination(String agent) {
		Spatial ag = players.get(agent);
		Vector3f dest = ag.getControl(PlayerControl.class).getDestination();
		return dest;
	}
	
	

	/**
	 * -Local use only-
	 * Approximative equality function : compare 2 float values.
	 * @param a the first float value.
	 * @param b the second float value.
	 * @return true if equals, false if not.
	 */
	private boolean approximativeEquals(float a, float b) {
		return b-2.5 <= a && a <= b+2.5;
	}

	/**
	 * -Local use only-
	 * Approximative equality function : compare 2 float values.
	 * @param a the first float value.
	 * @param b the second float value.
	 * @return true if equals, false if not.
	 */
	private boolean approximativeEqualsCoordinates(Vector3f a, Vector3f b) {
		return approximativeEquals(a.x, b.x) && approximativeEquals(a.z, b.z);
	}


	/**
	 * -Local use only-
	 * Explosion animation.
	 * @param coord the coordinates of the explosion.
	 */
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



//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ MODIF ZONE @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@



	public synchronized Spatial getSpatial(String agent){
		return players.get(agent);
		
	}
	
	
	
	public ArrayList<Vector3f> goldenSphereCast(Spatial sp, float distance, int N){
		
		ArrayList<Vector3f> goldenPoints = getGoldenSphere(N);
		ArrayList<Vector3f> result = new ArrayList<>();
		
		Vector3f direction;
		Vector3f position = sp.getWorldTranslation();
		
		System.out.println("Golden start : "+goldenPoints.size());
		
		for(Vector3f golden : goldenPoints){
			
			
			golden = golden.add(position); // Should set the point around the player ?
			
			direction = position.subtract(golden).normalize(); // Vector pointing from the player to the exterior
			
			
			//This may avoid collision with the player. Useless since terrainNode is the only collider considered
			//position = sp.getWorldTranslation().add(direction.mult(3f)); 

			
			Vector3f rayHit = shootRay(position, direction, distance);
			
			if(rayHit != null){
				result.add(rayHit);
			}
			
		}
		System.out.println("Golden finished : "+result.size());
		
		return result;
	}
	
	public ArrayList<Vector3f> filterWithVisionAngle(ArrayList<Vector3f> in,Vector3f dir, float maxAngle){
		ArrayList<Vector3f> filtered = new ArrayList<>();
		
		for(Vector3f v3 : in){
			//if(dir.angleBetween())
		}
		return null;
		
	}
	
	
	public ArrayList<Vector3f> getGoldenSphere(int N){
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
	
	
	public ArrayList<Vector3f> sphereCast(Spatial sp, float angle, float distance, float precision){
		ArrayList<Vector3f> points = new ArrayList<>();
		
		float min =  - angle / 2f;
		float max =  angle / 2f;
		
		Camera cam = sp.getUserData("cam");
		
		Vector3f dir = cam.getDirection().clone();
		Vector3f direction;
		
		Quaternion q = new Quaternion();
		
		System.out.println("Sphere Cast ...");
		
		for (float angleX = min; angleX < max; angleX += precision){
			for (float angleY = min; angleY < max; angleY += precision){
				
				q.lookAt(dir, Vector3f.UNIT_Z);
				
				q = q.fromAngleAxis((float)Math.toRadians(angleX), Vector3f.UNIT_X);
				q = q.fromAngleAxis((float)Math.toRadians(angleY), Vector3f.UNIT_Y);
				
				cam.setRotation(q);
				
				Vector3f tmpV = shootRay(sp.getWorldTranslation(), cam.getDirection(), distance);
				
				if(tmpV != null){
					points.add(tmpV);
				}
			}
		}
		cam.lookAtDirection(dir, Vector3f.UNIT_Z);
		
		System.out.println("End : "+points.size());
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
		
		//drawDebug(point, point.add(direction.mult(10f)));
		
		if (res.size()>0){
			return res.getCollision(0).getContactPoint();
		}
		
		return null;
	}
	
	
	public synchronized void drawDebug(Vector3f s, Vector3f e){
		
		Mesh lineMesh = new Mesh();
		lineMesh.setMode(Mesh.Mode.Lines);
		lineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ s.x, s.y, s.z, e.x, e.y, e.z});
		lineMesh.setBuffer(VertexBuffer.Type.Index, 2, new short[]{ 0, 1 });
	
		//lineMesh.updateBound();
	
		//lineMesh.updateCounts();
	
		Geometry lineGeometry = new Geometry("line", lineMesh);
	
		Material mat_red = new Material(assetManager,
				"Common/MatDefs/Misc/Particle.j3md");
		mat_red.setTexture("Texture", assetManager.loadTexture(
				"Effects/Explosion/flame.png"));
		
		lineGeometry.setMaterial(mat_red);
	
		getRootNode().attachChild(lineGeometry);
	}

}




