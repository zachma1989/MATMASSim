

import MASS.*;             // Library for Multi-Agent Spatial Simulation
import MASS.Debugger_base;
import java.util.*;        // for Vector
import java.awt.*;         // uses the abstract windowing toolkit
import java.awt.event.*;   // also uses key events so we need this

public class Wave2DMass extends Place {
  // constants
  public static final int init_ = 0;
  public static final int computeWave_ = 1;
  public static final int exchangeWave_ = 2;
  public static final int collectWave_ = 3;
  public static final int startGraphics_ = 4;
  public static final int writeToGraphics_ = 5;
  public static final int finishGraphics_ = 6;
  //public static final int doSpecial_ = 7; //testing line created by John
  
  // wave height at each cell
  // wave[0]: current, wave[1]: previous, wave[2]: one more previous height
  double[] wave = new double[3];
  
  int time = 0;
  int interval = 0;
  
  // wave height from four neighbors: north, east, south, and west
  private final int north = 0, east = 1, south = 2, west = 3;
  double[] neighbors = new double[4];
  
  // simulation constants
  private final double c  = 1.0; // wave speed
  private final double dt = 0.1; // time quantum
  private final double dd = 2.0; // change in system
  
  // the array size and my index in (x, y) coordinates
  private int sizeX, sizeY;
  private int myX, myY;
    
  // --------------------------------------------------------------------------
  /** 
   * Is the constructor of Wave2D.
   * @param interval a time interval to call writeToGraphics( )
   */
  public Wave2DMass( Object interval ) {
	//super( interval );
	this.interval = ( ( Integer )interval ).intValue( );
  }
  
  public Object callMethod( int funcId, Object args ) {
	switch( funcId ) {
	  case init_: return init( args );
	  case computeWave_: return computeWave( args );
	  case exchangeWave_: return ( Object )exchangeWave( args );
	  case collectWave_: return ( Object )collectWave( args );
	  case startGraphics_: return startGraphics( args );
	  case writeToGraphics_: return writeToGraphics( args );
	  case finishGraphics_: return finishGraphics( args );
	  //case doSpecial_: return doSpecial(args); //testing line created by John
	}
	return null;
  }
  
  /**
   * A method for debugging, created by John
   */
  /*
  public Object doSpecial(Object args){
	wave[0] = wave[1] = wave[2] = 20;
	return null;
  }
  */
  
  // --------------------------------------------------------------------------
  /** 
   * Since size[] and index[] are not yet set by
   * the system when the constructor is called, this init( ) method must
   * be called "after" rather than "during" the constructor call
   * @param args formally declared but actually not used
   */
  public Object init( Object args ) {
	sizeX = size[0]; sizeY = size[1]; // size  is the base data members
	myX = index[0];  myY = index[1];  // index is the base data members
	// reset the neighboring area information.
	neighbors[north] = neighbors[east] = neighbors[south] = 
	neighbors[west] = 0.0;
	
	return null;
  }
  
  // --------------------------------------------------------------------------
  /** 
   * Compute this cell's wave height at a given time.
   * @param arg_time the current simulation time in Integer
   */
  public Object computeWave( Object arg_time ) {
	// retrieve the current simulation time
	time = ( ( Integer )arg_time ).intValue( );
	if(MASS_base.getMyPid() !=0){
	    //wave[2] = mytime;
	    //return null;
	    //MASS_base.log("why my time is: "+String.valueOf(time));
	}
	// move the previous return values to my neighbors[].
	if ( inMessages != null ) {
	  for ( int i = 0; i < 4; i++ ){
		if (inMessages[i] != null){ //mod by John 
		  neighbors[i] = ( ( Double )inMessages[i] ).doubleValue( );
		} //mod by John
	  }
	}
	
	if ( myX == 0 || myX == sizeX - 1 || myY == 0 || myY == sizeY-1 ) {
	  // this cell is on the edge of the Wave2D matrix
	  if ( time == 0 )
		wave[0] = 0.0; //current
	  if ( time == 1 )
		wave[1] = 0.0; //previous
	  else if ( time >= 2 )
		wave[2] = 0.0; //previous2
	}
	else {
	  // this cell is not on the edge
	  if ( time == 0 ) {
		// create an initial high tide in the central square area

	      wave[0] = 
		( sizeX * 0.2 <= myX && myX <= sizeX * 0.6 &&
		  sizeY * 0.2 <= myY && myY <= sizeY * 0.6 ) ? 20.0 : 0.0; //start w/ wave[0]
		  wave[1] = wave[2] = 0.0; // init wave[1] and wave[2] as 0.0
	  }
	  else if ( time == 1 ) {
		// simulation at time 1 
		wave[1] = wave[0] +
		c * c / 2.0 * dt * dt / ( dd * dd ) *
		( neighbors[north] + neighbors[east] + neighbors[south] + 
		 neighbors[west] - 4.0 * wave[0] ); //wave[1] based on wave[0]
	  } 
	  else if ( time >= 2 ) { 
		// simulation at time 2 and onwards
		wave[2] = 2.0 * wave[1] - wave[0] +
		c * c * dt * dt / ( dd * dd ) *
		( neighbors[north] + neighbors[east] + neighbors[south] + 
		 neighbors[west] - 4.0 * wave[1] ); //wave two based on wave[1] and wave[0]
		wave[0] = wave[1]; wave[1] = wave[2]; //shift wave[] measurements, prepare for a new wave[2]
	  }
	}
	if(MASS_base.getMyPid() !=0 && wave[2] != 0){
	    //MASS_base.log(String.valueOf(wave[2]));
	}
	return null;
  }
  
  // --------------------------------------------------------------------------
  /**
   * Exchange the local wave height with all my four neighbors.
   * @param args formally declared but actually not used.
   */
  public Double exchangeWave( Object args ) {
      if(time == 0){
	  return new Double(wave[0]);
      }
      return new Double(wave[1]);
  }
  
  // --------------------------------------------------------------------------
  /** 
   * Return the local wave height to the cell[0,0]
   * @param args formally declared but actually not used.
   */
  public Double collectWave( Object args ) {
	return new Double( wave[2] ); 
  }
  
  public Object getDebugData(){
      Object w = null;
      if(time == 0)
	  w = new Double(wave[0]);
      else if(time == 1)
	  w = new Double(wave[1]);
      else 
	  w = new Double(wave[2]);
      if(time == 0 && MASS_base.getMyPid() == 0 && wave[2] != 0){
	  //System.out.println(wave[2]);
      }
      return w;
  }
  
  public void setDebugData(Object argument){
      Double d = (Double)argument;
      wave[1] = d.doubleValue();
      MASS_base.log(String.valueOf(myX)+" "+ String.valueOf(myY)+" "+String.valueOf(wave[1]));
      return;
  }

  
  public void setNeighbours( int[][] neighbours ) {
		// TODO: Thinking about 2d array, and each cell will also be a int[] to store the indeces
  }

  // --------------------------------------------------------------------------
  /** 
   * Starts a Wave2 application with the MASS library.
   * @param receives the array size, the maximum simulation time, the graphic
   *        updating time, the number of processes to spawn, and the
   *        number of threads to create.
   */
  public static void main( String[] args ) throws Exception 
  {
	// validate the arguments.
	if ( args.length < 8 ) {
	  System.err.println( "usage: " +
						 "login pass port size time graph_interval " +
						 "#processes #threads showGfx" );
	  System.exit( -1 );
	}
        String login = args[0];
        String password = args[1];
        String port = args[2];
	int size = Integer.parseInt( args[3] );
	int maxTime = Integer.parseInt( args[4] );
	int interval = Integer.parseInt( args[5] );
	int nProcesses = Integer.parseInt( args[6] );
	int nThreads = Integer.parseInt( args[7] );
        boolean showGraphics = args.length == 9 ? true : false;

	System.out.println("Grid: " + args[3] + "\tDuration: " + args[4] + "\tProcesses: "+nProcesses);


	// start MASS
	// MASS.init( args, nProcesses, nThreads );
	String[] massArgs = new String[4];
	massArgs[0] = login;            // user name
	massArgs[1] = password;          // password
        massArgs[2] = "machinefile.txt";    // machine file
        massArgs[3] = port;
	MASS.init( massArgs, nProcesses, nThreads );
	
	// create a Wave2D array
	Places wave2D = new Places( 1, "Wave2DMass", ( Object )( new Integer( interval ) ), size, size );
	wave2D.callAll( init_);

	//Debugger Places
	int[] handler = new int[2]; handler[0] = 1;
	Places debugger = new Places(99, "Debugger", (Object)(handler), nProcesses);
	debugger.callAll( Debugger.init_);
	Object[] debugData = new Object[nProcesses];

	// start graphics
	/*if ( interval > 0 && showGraphics )
	  wave2D.callSome( startGraphics_, (Object)null, 0, 0 );
	*/
	// define the four neighbors of each cell
	Vector<int[]> neighbors = new Vector<int[]>( );
	int[] north = {  0, -1 }; neighbors.add( north );
	int[] east  = {  1,  0 }; neighbors.add( east );
	int[] south = {  0,  1 }; neighbors.add( south );
	int[] west  = { -1,  0 }; neighbors.add( west );
	
	Date startTime = new Date( );
	
	long ca_time_total = 0;
	long ea_time_total = 0;

	// now go into a cyclic simulation
	for ( int time = 0; time < maxTime; time++ ) {
	    //while debugger is sending data to gui, then wait
	    synchronized(Debugger_base.sending_lock){
		if(Debugger_base.sending_lock[0]){
		    System.out.println("sending data, I am waiting...");
		    Debugger_base.sending_lock.wait();
		}
	    }
	    //while user stoped computation, then wait
	    synchronized(Debugger_base.stop_lock){
		if(Debugger_base.stop_lock[0]){
		    System.out.println("I am stoped...");
		    Debugger_base.stop_lock.wait();
		}
	    }
	    System.out.println("time:"+time);
	    Date ca_start = new Date();
	    wave2D.callAll( computeWave_, ( Object )( new Integer( time ) ) );
	    Date ca_stop = new Date();
	    ca_time_total += (ca_stop.getTime() - ca_start.getTime());
	    Date ea_start = new Date();
	    wave2D.exchangeAll( 1, exchangeWave_, neighbors );
	    Date ea_stop = new Date();
	    ea_time_total += (ea_stop.getTime() - ea_start.getTime()); 
	  
	    //Mass Debugger
	    Object[] debugParam = new Integer[nProcesses];
	    debugData = (Object[])debugger.callAll(Debugger.fetchDebugData_, debugParam);
	    //here we should let debugger send data to GUI
	    Debugger.sendDataToGUI();

	    /* 
	    if(debugData == null){
		System.out.println("Get debug data failed!!!");
	    }else{
		for(int k = 0; k < debugData.length; k++){
		    if(debugData[k] == null){
			continue;
		    }else{
			Object[] ddata = (Object[])debugData[k];
			for(int ii = 0; ii<ddata.length; ii++){
			    if(((Double)ddata[ii]).doubleValue() > 1){
				((Double)ddata[ii]).doubleValue();
			    }
			}
		    }
		}
	    }*/
	    

	  // at every given time interval, display the array contents
	  
	   // some testing lines to make a certain square red
		  //if (!testWithNoGraphics){
		 // wave2D.callSome( doSpecial_, (Object)null, 5, 5); 
		//  }
		 // }
	
	if ( time % interval == 0 ) {
	    //Object[] waves = (Object[])wave2D.callAll( collectWave_, (Object[])null );
		//if (showGraphics) wave2D.callSome( writeToGraphics_, ( Object )waves, 0, 0 );
	  }
	}
	
	Date endTime = new Date( );
	System.out.println( "\tTime (ms): " +
					   ( endTime.getTime( ) - startTime.getTime( ) ) );
	System.out.println("exchangeAll time: " + ea_time_total + "\tcallAll time: " + ca_time_total);
	
	// stop graphics
	//if ( interval > 0 && showGraphics)
	//wave2D.callSome( finishGraphics_, (Object) null, 0, 0 );  
	
	MASS.finish( );
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  

  // Graphics --------------------------------------------------------------------------
  private static final int defaultN = 100; // the default system size
  private static final int defaultCellWidth = 8;
  private static Color bgColor;            //white background
  private static Frame gWin;               // a graphics window
  private static int cellWidth;            // each cell's width in the window
  private static Insets theInsets;         // the insets of the window 
  private static Color wvColor[];          // wave color
  private static int N = 0;                // array size
  
  // start a graphics window ------------------------------------------------------
  public Object startGraphics( Object args ) {
	// define the array size
	N = size[0];
	
	// Graphics must be handled by a single thread
	bgColor = new Color( 255, 255, 255 );//white background
	
	// the cell width in a window
	cellWidth = (int)((double) defaultCellWidth / ((double) N / (double) defaultN )); //mod by John
	if ( cellWidth == 0 )
	  cellWidth = 1;
	
	// initialize window and graphics:
	gWin = new Frame( "Wave Simulation" );
	gWin.setLocation( 50, 50 );  // screen coordinates of top left corner
	
	gWin.setResizable( false );
	gWin.setVisible( true );     // show it!
	theInsets = gWin.getInsets();
	Dimension frameDim = new Dimension (N * cellWidth + theInsets.left + theInsets.right,
										N * cellWidth + theInsets.top + theInsets.bottom);
	gWin.setSize(frameDim);
	
	// wait for frame to get initialized
	long resumeTime = System.currentTimeMillis() + 1000;
	do {} while (System.currentTimeMillis() < resumeTime);
	
	// paint the back ground
	Graphics g = gWin.getGraphics( );
	g.setColor( bgColor );
	g.fillRect( theInsets.left,
			   theInsets.top,
			   N * cellWidth,
			   N * cellWidth );
	
	// prepare cell colors
	wvColor = new Color[21];
	wvColor[0] = new Color( 0x0000FF );   // blue
	wvColor[1] = new Color( 0x0033FF );
	wvColor[2] = new Color( 0x0066FF );
	wvColor[3] = new Color( 0x0099FF );
	wvColor[4] = new Color( 0x00CCFF );
	wvColor[5] = new Color( 0x00FFFF );
	wvColor[6] = new Color( 0x00FFCC );
	wvColor[7] = new Color( 0x00FF99 );
	wvColor[8] = new Color( 0x00FF66 );
	wvColor[9] = new Color( 0x00FF33 );
	wvColor[10] = new Color( 0x00FF00 );  // green
	wvColor[11] = new Color( 0x33FF00 );
	wvColor[12] = new Color( 0x66FF00 );
	wvColor[13] = new Color( 0x99FF00 );
	wvColor[14] = new Color( 0xCCFF00 );
	wvColor[15] = new Color( 0xFFFF00 );
	wvColor[16] = new Color( 0xFFCC00 );
	wvColor[17] = new Color( 0xFF9900 );
	wvColor[18] = new Color( 0xFF6600 );
	wvColor[19] = new Color( 0xFF3300 );
	wvColor[20] = new Color( 0xFF0000 );  // red
	
	gWin.setLocation( 0, 0 ); //mod by John, debug line
	return null;
  }
  
  // update a graphics window with new cell information -------------------------------------
  public Object writeToGraphics( Object arg_waves ) {
	Object[] waves = ( Object[] )arg_waves;
	Graphics g = gWin.getGraphics( );
	for ( int i = 0; i < sizeX; i++  ) 
	  for ( int j = 0; j < sizeY; j++ ) {
		// convert a wave height to a color index ( 0 through to 20 )
		int index = ( int )( ( ( Double )( waves[i * sizeY + j ] ) ).doubleValue( ) / 2 + 10 );
		index = ( index > 20 ) ? 20 : ( ( index < 0 ) ? 0 : index );
		// show a cell
		g.setColor( wvColor[index] );
		g.fill3DRect( theInsets.left + i * cellWidth,
					 theInsets.top  + j * cellWidth,
					 cellWidth, cellWidth, true ); //mod by John
	  }
	return null;
  }
  
  // finish the graphics window -------------------------------------
  public Object finishGraphics( Object args ) {
	Graphics g = gWin.getGraphics( );
	g.dispose( );
	gWin.removeNotify( );
	gWin = null;
	
	return null;
  }
}


