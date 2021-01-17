package env.terrain.perlinalgorithm;

/**
 * Class representing a heightmap.
 * Based on the implementation described here : http://khayyam.developpez.com/articles/algo/perlin/  
 * 
 * A Layer represents a gray-scale image. 
 * Each layer will be used for interpolations. Each layer is associated with a persitence attribute that defines the level  onthe layer's data
 * @author WonbinLIM, hc
 * 
 * 
 * 
 *
 */
public class Layer {
	/**
	 * heights are stored here, [0,255]
	 */
	public float[][] v;
	/**
	 * Generated maps are square
	 */
	public int size;
	
	/**
	 * Used for interpolation
	 */
	public float persistence;
	
	public Layer(float[][] v, int size, float persistence) {
		this.v = v;
		this.size = size;
		this.persistence = persistence;
		
	}
	
	/**
	 * Create a flat map
	 * @param size (the map is a square)
	 * @param persistence
	 */
	public Layer(int size, float persistence) {
		this.v = new float[size][size];
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				this.v[i][j] = 0f;
			}
		}
		
		this.size = size;
		this.persistence = persistence;
		
	}
	
	/**
	 * Converts the heightmap matrix into a heightmap array, used for JME3 methods.
	 * @return a heightmap array.
	 */
	public float[] getHeightMap() {
		float[] res = new float[this.size*this.size];
		int cpt = 0;
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				res[cpt] = this.v[i][j];
				cpt++;
			}
		}
		return res;
	}
	
	/**
	 * Print a heightMap
	 */
	public String toString() {
		String s="";
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				s+=v[i][j]+" ";
			}
			s+="\n";
		}
		return s;
	}
	
	
}
