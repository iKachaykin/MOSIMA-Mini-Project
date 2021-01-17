package tests.jmeTest;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Sphere;

public class RayTest extends SimpleApplication {
	private Material m;
	private Geometry g;
	private Geometry mark;
	
	public static void main(String[] args) {
		new RayTest().start();
	}
	
	public void simpleInitApp() {
		
		flyCam.setMoveSpeed(10);
		flyCam.setRotationSpeed(3);
		
		m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		
		Sphere sphere = new Sphere(10, 10, 2);
		g = new Geometry("sphere", sphere);
		g.setMaterial(m);
		rootNode.attachChild(g);
		
		Arrow arrow = new Arrow(Vector3f.UNIT_Z.mult(2));
		arrow.setLineWidth(3);
		
		mark = new Geometry("arrow", arrow);
		Material m1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		m1.setColor("Color", ColorRGBA.Green);
		mark.setMaterial(m1);
		 
		
	}
	
	public void simpleUpdate(float tpf) {
		
		System.out.println("loc:"+cam.getLocation());
		System.out.println("dir:"+cam.getDirection());
		Ray ray = new Ray(cam.getLocation(), cam.getDirection());
		CollisionResults results = new CollisionResults();
		g.collideWith(ray, results);
		
		if (results.size() > 0) {
			CollisionResult closest = results.getClosestCollision();
			mark.setLocalTranslation(closest.getContactPoint());
			
			Quaternion q = new Quaternion();
			q.lookAt(closest.getContactNormal(), Vector3f.UNIT_Z);
			mark.setLocalRotation(q);
			
			rootNode.attachChild(mark);
		}
		else {
			rootNode.attachChild(mark);
		}
	}
}
