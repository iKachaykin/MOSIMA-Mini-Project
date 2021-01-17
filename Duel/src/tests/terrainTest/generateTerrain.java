package tests.terrainTest;

import dataStructures.tuple.Tuple2;
import env.terrain.TerrainTools;

public class generateTerrain {
	
	public static void main(String[] args) {
		Tuple2<Integer, float[]> t = TerrainTools.getRandomMap(64);
		System.out.println("Test Generate Terrain");
		System.out.println("size : "+t.getFirst()+"x"+t.getFirst());
		System.out.println("heightmap size : "+t.getSecond().length+" elements");
		
		
	}
}
