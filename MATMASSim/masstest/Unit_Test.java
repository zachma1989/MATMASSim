package masstest;

import edu.uw.bothell.css.dsl.MASS.MASS;
import edu.uw.bothell.css.dsl.MASS.Places;

import java.util.Vector;

public class Unit_Test{

	public  static void main( String[] args ) {
		
		if ( args.length != 9 ) {
			System.out.println( "usage: ./main username password machinefile port nProc nThr" );
			System.exit( 0 );
		}
		
		String[] arguments = new String[4];
		arguments[0] = args[1]; // username
		arguments[1] = args[2]; // password
		arguments[2] = args[3]; // machinefile
		arguments[3] = args[4]; // port

		int nProc = Integer.parseInt( args[5] ); 		// # of process
		int nThr = Integer.parseInt( args[6] );  		// # of thread
		int size = Integer.parseInt( args[7] );  		// array size
		int maxTime = Integer.parseInt( args[8] );  	// simulation time


		MASS.init( arguments, nProc, nThr );
		
		Places testPlaces = new Places( 1, "Test_Place", null, size);
		
		Vector< Vector< int[] > > argument = new Vector< Vector< int[] > >();
		
		//Assign different neighbors to all place
		testPlaces.callAll( Test_Place.setNeighbours, argument);
		
		
		testPlaces.exchangeAll( 1, Test_Place.exchangeParameter);
		int[] parameters = (int[]) testPlaces.callAll( Test_Place.collectParameter, null );
		
		
		// Printng out all place's parameter value
		for (int i = 0; i < parameters.length; i++ ) {
			
			System.out.println( parameters[i] + " " );
			
			if ( i % size == 0 )
				System.out.println();
			
		}
		
		MASS.finish();
		
	}
	
}
