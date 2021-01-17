package tests.prologTest;

import java.util.Random;

import org.jpl7.Query;

public class TestPrologCalls2Ways {

	public static int succesRate=81;
	
	public static void test(){
		System.out.println("Java function test() called :)");
	}
	
	/**
	 * 
	 * @param fisherman Name of the fisherman
	 * @param fish Name of the fish
	 * @return true if the fisherman hooked the fish. 
	 */
	public static boolean hooked(String fisherman,String fish){
		Random r=new Random();
		//theoretically, the function should check in the environment that the conditions for the fish to be hooked are met.  
		int x=r.nextInt(100);
		System.out.println("Hooked function triggered; succesRate = "+succesRate+"; v= "+x);
		return (x<succesRate) ? true: false;
	}
	
	public static void main(String []args){
	 
		System.out.println("** Trigerring Java-JPL-Java loop **");
		//System.out.println(""+System.getProperty("user.dir"));
		
		//unexplicit loading of the file
		String query = "consult('./ressources/prolog/test/fishing.pl')";
		System.out.println(query+" ?: "+Query.hasSolution(query));
		
		System.out.println("**Test 1**");
		query="fish(tom)";
		System.out.println(query+" ?: "+Query.hasSolution(query));
		
		System.out.println("**Test 2**");
		query="fish(maurice)";
		System.out.println(query+" ?: "+Query.hasSolution(query));
		
		System.out.println("**Test 3 - Calling the java method 'hooked' from Prolog with wrong parameters**");
		query="caught(maurice,tom)";
		System.out.println(query+" ?: "+Query.hasSolution(query));
		
		System.out.println("**Test 4 - Calling the java method 'hooked' from Prolog with good parameters**");
		query="caught(tom,maurice)";
		System.out.println(query+" ?: "+Query.hasSolution(query));	   
		
		System.out.println("** All tests successfull, JAVA-PROLOG-JAVA loop working **");
	}
//
//		
//		Query q1 =
//	            new Query(
//	                "consult",
//	                new Term[] {new Atom("test.pl")}
//	            );
//	    
//	    
//	}    
	
}