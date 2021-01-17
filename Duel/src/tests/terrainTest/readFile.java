package tests.terrainTest;

import dataStructures.tuple.Tuple2;
import env.terrain.TerrainTools;

public class readFile {
	public static void main(String[] args) {
		Tuple2<Integer, float[]> t = TerrainTools.getHeightMapFromTxt("PerlinMap");//circleMap2");
		System.out.println("Test Load terrain");
		System.out.println("size : "+t.getFirst()+"x"+t.getFirst());
		System.out.println("heightmap size : "+t.getSecond().length+" elements");
		
	}
}
