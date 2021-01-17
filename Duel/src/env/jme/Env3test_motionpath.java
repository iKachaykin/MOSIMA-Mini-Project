package env.jme;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

import dataStructures.tuple.Tuple2;
import env.terrain.TerrainTools;

public class Env3test_motionpath extends SimpleApplication {
	
	private BulletAppState bulletAppState;
	public Tuple2<Integer, float[]> heightmap_tuplet;
	private TerrainQuad terrain;
	private Material mat_terrain;
	
	private Spatial player;
	private CharacterControl physicsCharacter;
	private Node characterNode;
	
	private MotionPath path;
    private MotionEvent motionControl;
	
	
	public static void main(String[] args) {
		Env3test_motionpath.launchRandom(64);
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
	 */
	public static void launch(String filename){
		SimpleApplication app = new Env3test_motionpath(filename);
		app.start();
	}
	
	/**
	 * Generates and launches a random heightmap of the given size.
	 * - the size of the heightmap must be a power of two (ex: 64,128,..).
	 * @param size size of the heightmap
	 */
	public static void launchRandom(int size) {
		SimpleApplication app = new Env3test_motionpath(size);
		app.start();
	}
	
	
	/**
	 * Constructor, which implements the heightmap by random generation.
	 * @param size
	 */
	public Env3test_motionpath(int size) {
		this.heightmap_tuplet =  TerrainTools.getRandomMap(size);
	}
	
	/**
	 * Constructor, which implements the heightmap using a file.
	 * @param filename
	 */
	public Env3test_motionpath(String filename){
		this.heightmap_tuplet = TerrainTools.getHeightMapFromTxt(filename);
	}

	@Override
	public void simpleInitApp() {
		// TODO Auto-generated method stub
		bulletAppState = new BulletAppState();
	    stateManager.attach(bulletAppState);
	   
	    cam.setLocation(new Vector3f(21.384611f, 146.78105f, 155.05727f));
	    cam.lookAtDirection(new Vector3f(-0.0016761336f, -0.9035275f, -0.42852688f), new Vector3f(-0.003530928f, 0.4285301f, -0.9035206f));
		flyCam.setMoveSpeed(50);
		
		
		
		makeTerrain();
		// We attach the scene and the player to the rootnode and the physics space,
	    // to make them appear in the game world.
		getPhysicsSpace().add(terrain);
		
		
		physicsCharacter = new CharacterControl(new CapsuleCollisionShape(1.5f, 6f, 1), .05f);
	    physicsCharacter.setGravity(30);
	    physicsCharacter.setPhysicsLocation(new Vector3f(0, 10, 0));
		player = assetManager.loadModel("Models/Oto/Oto.mesh.xml");
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
		player.setMaterial(mat);
	    player.scale(0.25f);
	    rootNode.attachChild(player);
	    characterNode = new Node("character node");
	    characterNode.addControl(physicsCharacter);
	    getPhysicsSpace().add(physicsCharacter);
	    rootNode.attachChild(characterNode);
	    characterNode.attachChild(player);
		
		path = new MotionPath();
		path.addWayPoint(player.getLocalTranslation());
		path.addWayPoint(new Vector3f(30, player.getLocalTranslation().y, 10));
		path.enableDebugShape(assetManager, rootNode);
		path.setCycle(true);
		
		
		motionControl = new MotionEvent(player,path);
		motionControl.setDirectionType(MotionEvent.Direction.PathAndRotation);
        motionControl.setRotation(new Quaternion().fromAngleNormalAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y));
        motionControl.setInitialDuration(10f);
        motionControl.setSpeed(2f);
		
        
	}
	
	public void simpleUpdate(float tpf) {
        motionControl.play();
	}
	
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
	    rootNode.attachChild(terrain);
	 
	    /** 5. The LOD (level of detail) depends on were the camera is: */
	    TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
	    terrain.addControl(control);
	    
	    /** 6. Add physics: */
	    terrain.addControl(new RigidBodyControl(0));
	}
	
	private PhysicsSpace getPhysicsSpace() {
		return bulletAppState.getPhysicsSpace();
	}
	

}
