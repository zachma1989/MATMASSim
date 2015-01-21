package matmassim;

import java.util.Vector;

import edu.uw.bothell.css.dsl.MASS.*;


public class MASSTest extends Place {
	
	public void setNeighbours( Vector<int[]> neighbours ){
    	
    	// Go through the list    	
    	// Store neighbours for each node and link to their corresponding Place class
    	for ( int i = 0; i < neighbours.size( ); i++ ) {
    			    
    		// for each neighbor
    		int[] offset = neighbours.get(i);
    		int[] neighborCoord = new int[2];

		    
		    neighbours.add( neighborCoord );
    	}
    	
    }
	
	public void exchangeNetworkParameter() {
		
		// move the previous return values to my neighbors[].
		if ( !(inMessages.length == 0) ) {

			for ( int i = 0; i < inMessages.length; i++ ){
			
				if (inMessages[i] != null){
				
					neighbours[i] = inMessages[i];
					//extractParameters();
				}
			}
			
			inMessages.length = 0;
		}

		
	}

}
