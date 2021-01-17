package env.terrain;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Random;


import dataStructures.tuple.Tuple2;
import env.terrain.perlinalgorithm.Layer;
import env.terrain.perlinalgorithm.PerlinAlgorithm;
import fileManipulations.FileManipulations;

/**
 * Class for terrains generations, saving, and other tools.
 * 
 * @author WonbinLIM
 *
 */
public class TerrainTools {
	
	/**
	 * Load a HeighMap using data from a txt file.
	 * @param filename
	 * @return the tuple containing the size and and the data of the heightmap.
	 */
	public static Tuple2<Integer, float[]> getHeightMapFromTxt(String filename) {
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("ressources/env/maps/"+filename+".txt"));
			// read the first line for the size of the map
			int size = Integer.valueOf(br.readLine());
			
			float[] res = new float[size*size];
			String line=null;
			int cpt=0;
			while ((line = br.readLine()) != null) {
				String[] arrayLine = line.split(":");
				for (String el: arrayLine) {
					res[cpt] = Float.valueOf(el);
					cpt++;
				}
			}
			return new Tuple2<Integer, float[]>(size, res);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	/**
	 * Create a randomly generated heightmap.
	 * @param size of the heightmap.
	 * @return the tuple containing the size and and the data of the heightmap.
	 */
	public static Tuple2<Integer, float[]> getRandomMap(int size) {
		
		float[] res = new float[size*size];
		
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				float westnbr; 
				float northnbr;
				float avg = -1;
				
				if (j>0) {
					westnbr = res[(i*size+j)-1];
					if (i>0) {
						northnbr = res[(i*size+j)-size];
						avg = (westnbr + northnbr) / 2;
					}
					else {
						avg = westnbr;
					}
				}
				else if (i>0) {
					northnbr = res[(i*size+j)-size];
					avg = northnbr;
				}
				
				if (avg!=-1) {
					float max = avg+1.8f;
					float min = avg-1.8f;
					res[i*size+j] = new Random().nextFloat()*(max - min) + min;
				}
				else {
					res[i*size+j] = (float)(new Random().nextInt(256));
				}
			}
		}
		return new Tuple2<Integer, float[]>(size, res);
	}
	
	
	/**
	 * Create a map using Perlin's algorithm.
	 * @param size of the heightmap.
	 * @return the tuple containing the size and and the data of the heightmap.
	 */
	public static Tuple2<Integer, float[]> getPerlinAlgoMap(int size) {
		int octaves = 3;//3the more the better
		int frequency = 2;
		float persistence = (float)0.4;//4
		int lissage = 5;
		int maxAlt=256;
		
		Layer layer = new  Layer(size, persistence);
		Layer resCalq = PerlinAlgorithm.generate(frequency, octaves, persistence, lissage, layer,maxAlt);
		
		//System.out.println(resCalq);
		return new Tuple2<Integer, float[]>(size, resCalq.getHeightMap());
	}
	
	
	/**
	 * Saves a heightmap into a text file.
	 * @param filename name of the file to create.
	 * @param heightMap tuple of the heightmap we want to save.
	 */
	public static void saveHeightMap(String filename, Tuple2<Integer, float[]> heightMap) {
		PrintWriter writer;
		try {
			///System.out.println("Working Directory = " +System.getProperty("user.dir"));
			writer = new PrintWriter( System.getProperty("user.dir")+"/ressources/env/maps/"+filename+".txt", "UTF-8");
			writer.println(""+heightMap.getFirst());
			int cpt = 0;
			for (int i=0; i<heightMap.getFirst(); i++) {
				String line = ""+heightMap.getSecond()[cpt];
				cpt++;
				for (int j=1; j<heightMap.getFirst(); j++) {
					line += ":"+heightMap.getSecond()[cpt];
					cpt++;
				}
				writer.println(line);
			}
			writer.close();
			
			
			
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
