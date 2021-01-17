package env.jme;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;


/**
 * Class for the player object's moving.
 * @author WonbinLIM
 *
 */
public class PlayerControl extends CharacterControl{
	
	public TerrainQuad terrain;
	private Camera cam;
	// boolean for the destination-moving function
	public boolean ismoving = false;
	public Vector3f destination;
	// boolean for the direction-moving function
	private boolean ismoving2 = false;
	private final float STEP = 2;
	private Vector3f initialPosition;
	private Vector3f direction;

	
	public PlayerControl(CollisionShape shape, float stepHeight, TerrainQuad terrain) {
		super(shape, stepHeight);
        this.terrain = terrain;
        this.setMaxSlope((float) (Math.PI*2d)); // Super Powers
        
    }

	public void update(float tpf) {
		super.update(tpf);
		
		if (this.ismoving) {
			//if (!equalsCoordinates(this.spatial.getWorldTranslation().x, destination.x) /*|| !equalsCoordinates(player.getWorldTranslation().z, dest.z)*/ || !equalsCoordinates(this.spatial.getWorldTranslation().z, destination.z)) {
			if(spatial.getWorldTranslation().distance(destination)>1f){
				Vector3f dir = this.destination.subtract(this.spatial.getWorldTranslation());
				
				dir.setY(dir.getY()+15);
				
				setViewDirection(dir.clone().setY(0));
				dir.normalizeLocal();
				dir.multLocal(0.8f);
				setWalkDirection(dir);
				cam.setLocation(this.spatial.getWorldTranslation());
				cam.lookAtDirection(dir, Vector3f.UNIT_Y);
				
			}
			else {
				setViewDirection(this.spatial.getWorldTranslation().clone().setY(0));
				cam.setLocation(this.spatial.getWorldTranslation());
				cam.lookAtDirection(getViewDirection(), Vector3f.UNIT_Y);
				setWalkDirection(new Vector3f(0, 0, 0));
				this.ismoving = false;
				this.destination = null;
				
			}
		}
	}
	
	
	public void teleport(Vector3f dest){
		spatial.getWorldTranslation().set(dest);
		cam.setLocation(this.spatial.getWorldTranslation());
		ismoving = false;
		destination = null;
		
	}
	
	private boolean equalsCoordinates(float a, float b) {
		return b-2.5 <= a && a <= b+2.5;
	}
	
	/**
	 * Moves the player to the destination coordinates in the map.
	 * @param destination
	 */
	public void moveTo(Vector3f destination) {
		this.destination = destination;
		ismoving = true;
	}
	
	public void move(Vector3f direction) {
		initialPosition = this.spatial.getWorldTranslation().clone();
		this.direction = direction;
		ismoving2 = true;
	}
	
	public Vector3f getDestination() {
		return destination;
	}
	
	public void setCam(Camera cam) {
		this.cam = cam;
	}

}
