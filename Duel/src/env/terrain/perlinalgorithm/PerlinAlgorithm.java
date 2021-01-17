package env.terrain.perlinalgorithm;

import java.util.Random;

/**
 * Perlin algorithm for building heightmaps.
 * 
 * Based on the implementation described here : http://khayyam.developpez.com/articles/algo/perlin/
 * 
 * 1) The algorithm generates a random map,
 * 2) it then select specific points regularly distant (the frequency value define the number of points),
 * 3) it triggers the interpolation process 
 *  
 * 
 * @author WonbinLIM, hc
 *
 */
public class PerlinAlgorithm {
	
	/**
	 * Generates a random heightmap, improved and smoothed by the Perlin noise.
	 * @param frequency
	 * @param octaves
	 * @param persistence
	 * @param liss
	 * @param layer
	 * @param maxAlt Maximum altitude
	 * @return
	 */
	public static Layer generate(int frequency, int octaves, float persistence, int liss, Layer layer,int maxAlt) {
		int size = layer.size;
		int current_f;
		int n /*, x, y , k, l*/;
		float a;
		float step, sum_persistence;
		
		step = ((float)size)/frequency;
		float current_persistence = persistence;
		
		Layer rand;
		rand = new Layer(size, 1);
		
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				rand.v[i][j] = (float)new Random().nextInt(maxAlt);
			}
		}
		/*save rand[][]*/
		
		
		Layer[] my_copies = new Layer[octaves];
		
		for (int i=0; i<octaves; i++) {
			my_copies[i] = new Layer(size, current_persistence);
//			if (my_copies[i] == null) {
//				return null;
//			}
			current_persistence = persistence;
		}
		current_f = frequency;
		
		for (n=0; n<octaves; n++) {
			for (int i=0; i<size; i++) {
				for (int j=0; j<size; j++) {
					a = interpolated_value(i,j,current_f,rand);
					my_copies[n].v[i][j] = a;
				}
			}
			current_f *= frequency;
		}
		
		sum_persistence = 0;
		for (int i=0; i<octaves; i++) {
			sum_persistence += my_copies[i].persistence;
		}
		
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				for (n=0; n<octaves; n++) {
					layer.v[i][j] += my_copies[n].v[i][j]*my_copies[n].persistence;
				}
				layer.v[i][j] = (float) (layer.v[i][j] / sum_persistence);
			}
		}
		
		Layer smoothing;
		smoothing = new Layer(size, 0);
//		if (smoothing == null) {
//			return null;
//		}
		
		for (int x=0; x<size; x++) {
			for (int y=0; y<size; y++) {
				a=0;
				n=0;
				for (int k=x-liss; k<=x+liss; k++) {
					for (int l=y-liss; l<=y+liss; l++) {
						if ((k>=0) && (k<size) && (l>=0) && (l<size)) {
							n++;
							a += layer.v[k][l];
						}
					}
				}
				smoothing.v[x][y] = (float)a/n;
			}
		}
		
		return smoothing;
		
	}
	
	public static float interpolate(float y1, float y2, int n, int delta) {
		if (n==0) {
			return y1;
		}
		if (n==1) {
			return y2;
		}
		
		float a = (float) delta/n;
		
		float fac1 = (float) (3*Math.pow(1-a,2) - 2*Math.pow(1-a,3));
		float fac2 = (float) (3*Math.pow(a,2) - 2*Math.pow(a,3));
		
		return (int) (y1*fac1 + y2*fac2);
	}
	
	public static float interpolated_value(int i, int j, int frequence, Layer r) {
		int bound1x, bound1y, bound2x, bound2y;
		float q;
		float step = (float)(r.size*1./frequence);
		
		q = (float)i/step;
		bound1x = (int) (q*step);
		bound2x = (int) ((q+1)*step);
		
		if (bound2x >= r.size) {
			bound2x = r.size-1;
		}
		
		q = (float)j/step;
		bound1y = (int) (q*step);
		bound2y = (int) ((q+1)*step);
		
		if (bound2y >= r.size) {
			bound2y = r.size-1;
		}
		
		float b00,b01,b10,b11;
	    b00 = r.v[bound1x][bound1y];
	    b01 = r.v[bound1x][bound2y];
	    b10 = r.v[bound2x][bound1y];
	    b11 = r.v[bound2x][bound2y];
		
	    float v1 = interpolate(b00, b01, bound2y-bound1y, j-bound1y);
	    float v2 = interpolate(b10, b11, bound2y-bound1y, j-bound1y);
	    float end = interpolate(v1, v2, bound2x-bound1x , i-bound1x);
		
		return end;
	}
}
