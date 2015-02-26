package masstest;

import edu.uw.bothell.css.dsl.MASS.Place;

public class Test_Place extends Place {
	
	
	public static final int setNeighbours = 0;
	public static final int exchangeParameter = 1;
	public static final int collectParameter = 2;
	
	private int paraValue = 0;
	
	@ Override
	public Object callMethod( int functionId, Object argument ) {
		
		switch ( functionId ) {
		
			case setNeighbours:
				setNeighbours( argument );
		
			case exchangeParameter: exchangeParameter( argument );
			
			case collectParameter: collectParameter( argument );
		
		}
    	return null;
    }
	
	public void setNeighbours( Object argument ) {
		// TODO: Thinking about 2d array, and each cell will also be a int[] to store the indi ces
		
		this.neighbours = ( Vector< int[] > )argument;
		
	}
	
	public int collectParameter( Object argument ) {
		
		return paraValue;
	}
	
	public int exchangeParameter( Object argument ) {
		
		return paraValue;
	}

}