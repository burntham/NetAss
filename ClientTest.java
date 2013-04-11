//CSC3002F
//Network Assignment 1
//Gerard Nothnagel|Jared Norman|Daniel Burnham-King
//NTHGER001|NRMJAR001|BRNDAN022
//Group 10
//Driver Class

//Client Test - this is the driver class for the client, preconfigured to connect to nightmare.cs.uct.ac.za

import javax.swing.JFrame;

public class ClientTest
{
	public static void main( String[] args ){
		Client application; // declare client application
		
		// if no command line args
		if ( args.length == 0 ){
			application = new Client( "nightmare.cs.uct.ac.za");// connect to localhost
		}
		else{
			application = new Client( args[0]);// use args to connect
		}
		
		application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);
		application.runClient(); // run client application
	
	} // end main
}// end class ClientTest
