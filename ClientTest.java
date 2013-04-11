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
