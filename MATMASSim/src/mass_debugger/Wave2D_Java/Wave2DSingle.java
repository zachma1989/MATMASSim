import java.awt.*;        // uses the abstract windowing toolkit
import java.awt.event.*;  // also uses key events so we need this

import java.io.*;

class Wave2DSingle {

    static final int defaultN = 100;  // the default system size
    static final int defaultCellWidth = 8;
    static final Color bgColor = new Color( 255, 255, 255 );//white background 

    static final double c = 1.0;             // wave speed
    static final double dt = 0.1;            // time quantum
    static final double dd = 2.0;            // change in system

    private int N = 0;                       // the system size
    private int maxTime = 0;                 // maximum simulation time
    private int interval = 0;                // interval to display results
    private double z[][][];                  // simulation space

    private int time;                        // the current simulation time
    private boolean show;                    // no graphics if false

    private Frame gWin;                      // a graphics window 
    private int cellWidth;                   // each cell's width in the window
    private Insets theInsets;                // the insets of the window
    private Color wvColor[];                 // wave color

    public Wave2DSingle( int argv[], boolean show ) {
	N = argv[0];                          // the system size
	maxTime = argv[1];                    // maximum simulation time
	interval = argv[2];                   // interval to display results
	this.show = show;

	z = new double[3][N][N]; // simulation space

	startGraphics( );
    }

    private void compute( ) {
	// initialize the simulation space at time = 0
	// calculate z[0][][]
	int weight = N / defaultN;
	for( int i = 0; i < N; i++ ) {
	    for( int j = 0; j < N; j++ ) {
		if( i > 40 * weight && i < 60 * weight  && 
		    j > 40 * weight && j < 60 * weight ) {
		    z[0][i][j] = 20.0;
		} else {
		    z[0][i][j] = 0.0;
		}
	    }
	}

	// simulation at time = 1
	// calculate z[1][][]
	// cells not on edge
	for( int i = 1; i < N - 1; i++ ) {
	    for( int j = 1; j < N - 1; j++ ) {
		z[1][i][j] = z[0][i][j] 
		    + c * c / 2.0 * dt * dt / ( dd * dd )
		    * (   z[0][i + 1][j] + z[0][i - 1][j] 
			+ z[0][i][j + 1] + z[0][i][j - 1]
			- 4.0 * z[0][i][j] );
	    }
	}
	// cells on edge
	for( int i = 0; i < N; i++ ) {
	    z[1][i][0]     = 0.0;
	    z[1][i][N - 1] = 0.0;
	    z[1][0][i]     = 0.0;
	    z[1][N-1][i]   = 0.0;
	}

	// simulation from time = 2 through to maxTime - 1
	// calculate z[2][][] from z[0][][] and z[1][][]
	for( time = 2; time < maxTime; time++ ) {
	    // cells not on edge
	    for( int i = 1; i < N - 1; i++ ) {
		for( int j = 1; j < N - 1; j++ ) { 
		    z[2][i][j] = 2.0 * z[1][i][j] - z[0][i][j]
			+ c * c * dt * dt / ( dd * dd )
			* (   z[1][i + 1][j] + z[1][i - 1][j]
			    + z[1][i][j + 1] + z[1][i][j - 1]
			    - 4.0 * z[1][i][j] );
		}
	    }
	    // cells on edge
	    for( int i = 0; i < N; i++ ) {
		z[2][i][0]     = 0.0;
		z[2][i][N - 1] = 0.0;
		z[2][0][i]     = 0.0;
		z[2][N - 1][i] = 0.0;
	    }

	    // shift z[1][][] to z[0][][], shift[2][][] to z[1][][] for
	    // the next simulation step
	    for( int i = 0; i < N; i++ ) {
		for( int j = 0; j < N; j++ ) {
		    z[0][i][j] = z[1][i][j];
		    z[1][i][j] = z[2][i][j];
		}
	    }

	    // intermediate results on a window
	    if ( time % interval != 0 )
		continue;
	    writeToGraphics( );
	}

	writeToFile( );
    }

    private void startGraphics( ) {
	// check if show
	if ( !show )
	    return;

	// the cell width in a window
	cellWidth = defaultCellWidth / ( N / defaultN );
	if ( cellWidth == 0 )
	    cellWidth = 1;

	// initialize window and graphics:
	gWin = new Frame( "Wave Simulation" );
	gWin.setLocation( 50, 50 );  // screen coordinates of top left corner
	gWin.setResizable( false );
	gWin.setVisible( true );     // show it!
	theInsets = gWin.getInsets();
        gWin.setSize( N * cellWidth + theInsets.left + theInsets.right,
		      N * cellWidth + theInsets.top + theInsets.bottom );

	// wait for frame to get initialized	
        long resumeTime = System.currentTimeMillis() + 1000;
        do {} while (System.currentTimeMillis() < resumeTime);  

	Graphics g = gWin.getGraphics( );
	g.setColor( bgColor );
        g.fillRect( theInsets.left, 
		    theInsets.top, 
		    N * cellWidth,
		    N * cellWidth );

	wvColor = new Color[21];
	wvColor[0] = new Color( 0x0000FF );
	wvColor[1] = new Color( 0x0033FF );
	wvColor[2] = new Color( 0x0066FF );
	wvColor[3] = new Color( 0x0099FF );
	wvColor[4] = new Color( 0x00CCFF );
	wvColor[5] = new Color( 0x00FFFF );
	wvColor[6] = new Color( 0x00FFCC );
	wvColor[7] = new Color( 0x00FF99 );
	wvColor[8] = new Color( 0x00FF66 );
	wvColor[9] = new Color( 0x00FF33 );
	wvColor[10] = new Color( 0x00FF00 );
	wvColor[11] = new Color( 0x33FF00 );
	wvColor[12] = new Color( 0x66FF00 );
	wvColor[13] = new Color( 0x99FF00 );
	wvColor[14] = new Color( 0xCCFF00 );
	wvColor[15] = new Color( 0xFFFF00 );
	wvColor[16] = new Color( 0xFFCC00 );
	wvColor[17] = new Color( 0xFF9900 );
	wvColor[18] = new Color( 0xFF6600 );
	wvColor[19] = new Color( 0xFF3300 );
	wvColor[20] = new Color( 0xFF0000 );
    }

    private void writeToGraphics( ) {
	System.out.println( "time = " + time );

	// check if show
	if ( !show )
	    return;

	Graphics g = gWin.getGraphics( );
	for ( int i = 0; i < N; i++ ) {
	    for ( int j = 0; j < N; j++ ) {
		// convert a wave height to a color index ( 0 through to 20 )
		int index = ( int )( z[2][i][j] / 2 + 10 );
		index = ( index > 20 ) ? 20 : ( ( index < 0 ) ? 0 : index );

		g.setColor( wvColor[index] );
		g.fill3DRect( theInsets.left + i * cellWidth, 
			      theInsets.top  + j * cellWidth, 
			      cellWidth, cellWidth, true );
	    }
	}
    }

    private void writeToFile( ) {
	// write results at maxTime
	try {
	    FileWriter fw = new FileWriter( "output.data" );

	    for( int i = 0; i < N; i++ ) {
		for( int j = 0; j < N; j++ ) {
		    fw.write( i + " " + j + " " + z[2][i][j] + "\n" );
		}
		fw.write( "\n" );
	    }
	    fw.write( "\n" );
	    fw.close( );
	} catch( Exception e ) {
	    System.err.println( e );
        }
    }

    public static void main( String args[] ) {
	
	// check arguments
	int argv[] = new int[3];
	if ( args.length >= 3 ) {
	    try {
		for ( int i = 0; i < 3; i++ )
		    argv[i] = Integer.parseInt( args[i] );
	    } catch ( Exception e ) { }
	}
	if ( argv[0] == 0 || argv[1] < 3 || argv[2] < 1 ) {
	    System.err.println( "usage: java Wave2DSingle size time interval [show]");
	    System.err.println( "       where " +
				"size > 0 && time >= 3 && interval >= 1" );
	    return;
	}

	// instantiate a program
	boolean show = ( args.length == 4 && args[3].equals( "show" ) );
	Wave2DSingle program = new Wave2DSingle( argv, show );

	// start the program
	program.compute( );

    }
}
