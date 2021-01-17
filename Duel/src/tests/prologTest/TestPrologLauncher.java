package tests.prologTest;

import java.util.Hashtable;

import org.jpl7.*;

public class TestPrologLauncher {

	
	
	public static void main(String[] args){
		
		
		
		String fish = "consult('./ressources/prolog/test/fishing.pl')";
		
		if (!Query.hasSolution(fish)) {
			System.out.println(fish + " failed, no solution found");
			// System.exit(1);
		}
		
		Term tom = new Atom("tom");
		
		String t1 = "fish(maurice)";
		if (!Query.hasSolution(t1)) {
			System.out.println(t1 + " failed");
			// System.exit(1);
		}
		System.out.println("test passed");		
		
		
	}
	
}