package env.jme;


import fileManipulations.FileManipulations;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.ReadOnlyFileSystemException;
import java.text.DecimalFormat;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;

import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;

import dataStructures.tuple.Tuple2;
import env.terrain.TerrainTools;

import com.jme3.scene.control.Control;



/**
 * Generates a very simple 3D representation of an environment
 * The 3D engine is jMonkeyEngine. 
 * Its a 3D (LGPL, java-based, well documented and in active development) game engine. 
 * Its capabilities allow us to design it and to interact 
 * with it , to use a physical engine and to import models from 3DSMAX, blender, google sketchup,... to create you scene 
 * 
 * Contains 2 launching methods : launch(String filename) and launch(int size)
 * The first one launches a predefined heightmap file, while the second one launches a randomly generated heighmap.s
 * 
 * 
 * @author WonbinLIM
 *
 */
public class Env extends SimpleApplication /*implements ActionListener*/ {

	private BulletAppState bulletAppState;
	private RigidBodyControl landscape;
//	private CharacterControl player;
//	private Vector3f walkDirection = new Vector3f();
	private boolean left = false, right = false, up = false, down = false;
	private TerrainQuad terrain;
	private Material mat_terrain;
	
//	private Geometry player;
	private Spatial player;
	
	public Tuple2<Integer, float[]> hm_tup;
	
	
	private CharacterControl physicsCharacter;
	private Node characterNode;
	private CameraNode camNode;
	boolean rotate = false;
	private Vector3f walkDirection = new Vector3f(0,0,0);
	private Vector3f viewDirection = new Vector3f(0,0,0);
	boolean leftStrafe = false, rightStrafe = false, forward = false, backward = false, 
			leftRotate = false, rightRotate = false;
	
	
	
	
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
		SimpleApplication app = new Env(filename);
		app.start();
	}
	
	/**
	 * Generates and launches a random heightmap of the given size.
	 * - the size of the heightmap must be a power of two (ex: 64,128,..).
	 * @param size size of the heightmap
	 */
	public static void launchRandom(int size) {
		SimpleApplication app = new Env(size);
		app.start();
	}
	
	
	/**
	 * Constructor, which implements the heightmap by random generation.
	 * @param size
	 */
	public Env(int size) {
		this.hm_tup =  TerrainTools.getRandomMap(size);
	}
	
	/**
	 * Constructor, which implements the heightmap using a file.
	 * @param filename
	 */
	public Env(String filename){
		this.hm_tup = TerrainTools.getHeightMapFromTxt(filename);
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
	    terrain = new TerrainQuad("my terrain", patchSize, this.hm_tup.getFirst()+1, this.hm_tup.getSecond());
	 
	    /** 4. We give the terrain its material, position & scale it, and attach it. */
	    terrain.setMaterial(mat_terrain);
	    terrain.setLocalTranslation(0, -255, 0);
	    terrain.setLocalScale(2f, 1f, 2f);
	    rootNode.attachChild(terrain);
	 
	    /** 5. The LOD (level of detail) depends on where the camera is: */
	    TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
	    terrain.addControl(control);
	    
	    /** 6. Add physics: */
	    terrain.addControl(new RigidBodyControl(0));
	}
	
	@Override
	public void simpleInitApp() {
		bulletAppState = new BulletAppState();
	    stateManager.attach(bulletAppState);
	    
	    cam.setLocation(new Vector3f(0,150,0));
	    cam.lookAt(new Vector3f(0,0,0), new Vector3f(0,0,0).normalize());
		flyCam.setMoveSpeed(50);
//		flyCam.setEnabled(false);
		
		
//		setupKeys();
		
		makeTerrain();
		

	    // We attach the scene and the player to the rootnode and the physics space,
	    // to make them appear in the game world.
	    bulletAppState.getPhysicsSpace().add(terrain);
//	    bulletAppState.getPhysicsSpace().add(player);
	    
	 // Add a physics character to the world
	    physicsCharacter = new CharacterControl(new CapsuleCollisionShape(1.5f, 6f, 1), .05f);
	    physicsCharacter.setGravity(30f);
	    physicsCharacter.setPhysicsLocation(new Vector3f(0, 10, 0));
	    player = assetManager.loadModel("Models/Oto/Oto.mesh.xml");
//	    Sphere sphere = new Sphere(10,10,4f);
//	    player = new Geometry("Sphere", sphere);
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
		player.setMaterial(mat);
	    player.scale(0.25f);
//	    player.setLocalTranslation(new Vector3f(1,0,0));
	    characterNode = new Node("character node");
	    characterNode.addControl(physicsCharacter);
	    bulletAppState.getPhysicsSpace().add(physicsCharacter);
	    characterNode.attachChild(player);
	    rootNode.attachChild(characterNode);
	    


//	    // set forward camera node that follows the character
//	    camNode = new CameraNode("CamNode", cam);
//	    camNode.setControlDir(ControlDirection.SpatialToCamera);
//	    camNode.setLocalTranslation(new Vector3f(0, 1, -5));
//	    camNode.lookAt(player.getLocalTranslation(), Vector3f.UNIT_Y);
//	    characterNode.attachChild(camNode);
	    
	    
//	    initAgent();

	}
	
	public void moveto(Vector3f v) {
		
		
	}
	
//	/** We over-write some navigational key mappings here, so we can
//	 * add physics-controlled walking and jumping: */
//	private void setUpKeys() {
//		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
//		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
//		inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
//		inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
//		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
//		inputManager.addListener(this, "Left");
//		inputManager.addListener(this, "Right");
//		inputManager.addListener(this, "Up");
//		inputManager.addListener(this, "Down");
//		inputManager.addListener(this, "Jump");
//	}
//
//	/** These are our custom actions triggered by key presses.
//	 * We do not walk yet, we just keep track of the direction the user pressed. */
//	public void onAction(String binding, boolean value, float tpf) {
//		if (binding.equals("Left")) {
//			if (value) { left = true; } else { left = false; }
//		} else if (binding.equals("Right")) {
//			if (value) { right = true; } else { right = false; }
//		} else if (binding.equals("Up")) {
//			if (value) { up = true; } else { up = false; }
//		} else if (binding.equals("Down")) {
//			if (value) { down = true; } else { down = false; }
//		} else if (binding.equals("Jump")) {
//			player.jump();
//		}
//	}
//
//	/**
//	 * This is the main event loop--walking happens here.
//	 * We check in which direction the player is walking by interpreting
//	 * the camera direction forward (camDir) and to the side (camLeft).
//	 * The setWalkDirection() command is what lets a physics-controlled player walk.
//	 * We also make sure here that the camera moves with player.
//	 */
//	@Override
//	public void simpleUpdate(float tpf) {
//		Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
//		Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
//		walkDirection.set(0, 0, 0);
//		if (left)  { walkDirection.addLocal(camLeft); }
//		if (right) { walkDirection.addLocal(camLeft.negate()); }
//		if (up)    { walkDirection.addLocal(camDir); }
//		if (down)  { walkDirection.addLocal(camDir.negate()); }
//		player.setWalkDirection(walkDirection);
//		cam.setLocation(player.getPhysicsLocation());
//	}
	
	
//	private void setupKeys() {
//        inputManager.addMapping("Strafe Left", 
//                new KeyTrigger(KeyInput.KEY_J));
//        inputManager.addMapping("Strafe Right", 
//                new KeyTrigger(KeyInput.KEY_L));
//        inputManager.addMapping("Rotate Left", 
//                new KeyTrigger(KeyInput.KEY_U));
//        inputManager.addMapping("Rotate Right", 
//                new KeyTrigger(KeyInput.KEY_O));
//        inputManager.addMapping("Walk Forward", 
//                new KeyTrigger(KeyInput.KEY_I));
//        inputManager.addMapping("Walk Backward", 
//                new KeyTrigger(KeyInput.KEY_K));
//        inputManager.addMapping("Jump", 
//                new KeyTrigger(KeyInput.KEY_SPACE));
//        inputManager.addMapping("Shoot", 
//                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
//        inputManager.addListener(this, "Strafe Left", "Strafe Right");
//        inputManager.addListener(this, "Rotate Left", "Rotate Right");
//        inputManager.addListener(this, "Walk Forward", "Walk Backward");
//        inputManager.addListener(this, "Jump", "Shoot");
//    }
	
	public void simpleUpdate(float tpf) {
//        Vector3f camDir = cam.getDirection().mult(0.2f);
//        Vector3f camLeft = cam.getLeft().mult(0.2f);
//        camDir.y = 0;
//        camLeft.y = 0;
//        viewDirection.set(camDir);
//        walkDirection.set(0, 0, 0);
//        if (leftStrafe) {
//            walkDirection.addLocal(camLeft);
//        } else
//        if (rightStrafe) {
//            walkDirection.addLocal(camLeft.negate());
//        }
//        if (leftRotate) {
//            viewDirection.addLocal(camLeft.mult(0.02f));
//        } else
//        if (rightRotate) {
//            viewDirection.addLocal(camLeft.mult(0.02f).negate());
//        }
//        if (forward) {
//            walkDirection.addLocal(camDir);
//        } else
//        if (backward) {
//            walkDirection.addLocal(camDir.negate());
//        }
//        physicsCharacter.setWalkDirection(walkDirection);
//        physicsCharacter.setViewDirection(viewDirection);
		/*Vector3f currentpostion = player.getLocalTranslation();
		player.lookAt(new Vector3f(10, currentpostion.y, 10), new Vector3f(0, 0, 0));*/

		
//		if (cpt%2==0) {
//			player.move(-10+1*tpf,0f,0f);
//		}
//		else {
//			player.move(10+1*tpf,0f,0f);
//		}
//		cpt++;
    }
	
//	public void moveTo(String direction) {
//        if (direction.equals("Strafe Left")) {
//            left = true;
//        } else if (direction.equals("Strafe Right")) {
//            right = true;
//        } else if (direction.equals("Rotate Left")) {
//            if (value) {
//                leftRotate = true;
//            } else {
//                leftRotate = false;
//            }
//        } else if (direction.equals("Rotate Right")) {
//            if (value) {
//                rightRotate = true;
//            } else {
//                rightRotate = false;
//            }
//        } else if (direction.equals("Walk Forward")) {
//            if (value) {
//                forward = true;
//            } else {
//                forward = false;
//            }
//        } else if (direction.equals("Walk Backward")) {
//            if (value) {
//                backward = true;
//            } else {
//                backward = false;
//            }
//        } else if (binding.equals("Jump")) {
//            physicsCharacter.jump();
//        }
//    }

  private PhysicsSpace getPhysicsSpace() {
    return bulletAppState.getPhysicsSpace();
  }

//	public void initAgent() {
//		Spatial player = assetManager.loadModel("Models/Oto/Oto.mesh.xml");
//		Material mat_default = new Material( 
//				assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
//		player.setMaterial(mat_default);
//		rootNode.attachChild(player);
//	}
	
  

	
}
