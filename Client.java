import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.sql.Timestamp;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.Rotation;

@SuppressWarnings("serial")
public class Client extends JFrame{
	
	private static final int PORT = 8087;
	private static final int ID = 10;
	
	private JTextField enterField; // enters information from user
	private JTextArea displayArea; // display information to user
	private OutputStreamWriter output; // output stream to server
	private InputStreamReader input; // input stream from server
	private String message = ""; // message from server
	private String server; // host server for this application
	private Socket client; // socket to communicate with server
	
	enum cmd {all, bar, clear, close, exit, get, graph, id, light, login, logs, mean, median, mode, raw, temp, upload, variance, std};
	private String dataStringTemp = "Group 4=3:Group 8=1:Group 1=4";  // group1=y1:group2=y2: ... :groupn=yn
	private String dataStringLight = "Group 4=4:Group 8=1:Group 1=3"; // group1=y1:group2=y2: ... :groupn=yn
	private BarChart chart;
	private LineGraph xychart;
	private Sensor group4;
	private Sensor group8;
	private Sensor group10;
	private String raw4= "";
	private String raw8= "";
	private String raw10= "";
	// initialize server and set up GUI
	public Client( String host )
	{
		super( "Client" );
		
		server = host; // set server to which this client connects
		
		enterField = new JTextField();
		enterField.setEditable( false ); // only set editable true when a connection is established
		enterField.addActionListener(
				new ActionListener()
				{
					// every time text is entered the client sends it to server
					public void actionPerformed( ActionEvent event ){
						parseMessage( event.getActionCommand(), "client" );
						enterField.setText( "");
					}
				}
		);
	
		add( enterField,BorderLayout.SOUTH);
	
		displayArea = new JTextArea();// create console display
		add(new JScrollPane( displayArea ), BorderLayout.CENTER);
	    setSize(300,350);
	    setVisible( true);
	    
	    //create sensor objects used to store aggregated data
	    group4 = new Sensor();
	    group8 = new Sensor();
	    group10 = new Sensor();
	    
	} 
	
	// connect to server and process messages from server
	public void runClient()
	{
		try
		{
			connectToServer();
			getStreams();
			processConnection();
		}
		catch ( EOFException eofException )
		{
			displayMessage( "\nClient terminated connection");
		}
		catch ( IOException ioException )
		{
			System.out.println("Client Runtime Error");
			//ioException.printStackTrace();
		}
		finally
		{
			closeConnection();
		}
	}
	
	// connects to server
	private void connectToServer()throws IOException
	{
		displayMessage( "Attempting connection\n" );
		// create Socket to make connection to server
		client = new Socket( InetAddress.getByName( server ), PORT);
		displayMessage( "Connected to: " + client.getInetAddress().getHostName() );
	}
	
	// set up streams to communicate between server and client
	private void getStreams()throws IOException
	{
		output = new OutputStreamWriter( new BufferedOutputStream(client.getOutputStream()) );
		output.flush();
		input = new InputStreamReader( new BufferedInputStream(client.getInputStream() ));
		displayMessage( "\nServer I/O streams created\n" );
	}
	
	// process connection with server
	private void processConnection()throws IOException
	{
		// enable enterField so client user can send messages
		setTextFieldEditable(true);
		do // process messages sent from server
		{

		} while( !message.equals("exit"));
	}
	
	// closes all open streams and the socket connection to the server
	private void closeConnection()
	{
		displayMessage( "\nClosing connection to server");
		setTextFieldEditable(false);
		try
		{
			output.close();
			input.close();
			client.close();
		}
		catch ( IOException ioException )
		{
			//ioException.printStackTrace();
			System.out.println("Connection close error");
		}
	}
	
	// sends a message to the server
	private void sendData( String message )
	{
		try
		{
			output.write(message + "\n");
			output.flush(); // flush data to output
		}
		catch ( IOException ioException )
		{
			displayArea.append("\nError writing to server");
		}
	}

	// prints a message to the GUI display area
	private void displayMessage(final String message)
	{
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						displayArea.append(message);
					}
				} 
		); 
	}
	
	// sets the text field editable or not
	private void setTextFieldEditable(final boolean editable )
	{
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						enterField.setEditable( editable );
					}
				}
		);
	} 
	
	// process the messages sent from the server
	private void parseMessage(String message, String source){
		
		if(source.equalsIgnoreCase("server")){
			
			return;
		}
		
		displayMessage( "\nCLIENT> "+ message );
		
		try {
			//upload
			if(message.contains(cmd.upload.toString())){
				String filename = message.split(" ")[1];
				sendSensorData(filename);//Upload sensor data
			}
			//close
			else if(message.contains(cmd.close.toString())){
				if(chart != null){
					chart.dispose();//close bar chart
				}
				if(xychart != null){
					xychart.dispose();//attempt to close line chart - does not work
				}
			}
			//login
			else if(message.contains(cmd.login.toString())){
				String[] login = message.split(" ");
				try{
					String name = login[1];
					String password = login[2];
					login(name, password);
				}
				catch(Exception e){
					displayMessage("\nInvalid login, please try again.");
				}
				
			}
			//clear
			else if(message.contains(cmd.clear.toString())){
				displayArea.append("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
			}
			//logs
			else if(message.contains(cmd.logs.toString())){
				getLogs();
				System.out.println("Method called: getLogs()");
			}
			//getid
			else if(message.contains(cmd.id.toString())){
				if(message.contains(cmd.all.toString())){
					getGroupIDs();
					System.out.println("Method called: getGroupIDs()");
				}
				else{
					displayMessage("\nID = " + ID);
				}
			}
			
			//get
			else if(message.contains(cmd.get.toString())){
				if(message.contains(cmd.all.toString())){
					if(message.contains(cmd.raw.toString())){//graphs it!
						if(xychart != null){
							xychart.dispose();
						}
						getRawAll(message);
						System.out.println("Method called: getRawAll()");
					}
					else{
						getAll();
						System.out.println("Method called: getAll()");
					}
				}
				else{
					String id = "";
					try {
						id = message.split(" ")[1];
						if(message.contains(cmd.raw.toString())){
							getRawGroup(id, source, message);
							System.out.println("Method called: getRawGroup("+id+")");
						}
						else{
							getGroup(id, source);
							System.out.println("Method called: getGroup("+id+")");
						}
					} catch (Exception e) {
						displayMessage("\nError processing command: "+ message);
						//e.printStackTrace();
					}
				}
			}
			
			//graph
			if(message.contains(cmd.graph.toString())){
					if(chart != null){
						chart.dispose();
					}
					drawGraph(message);
			}
		} catch (Exception e) {
			displayMessage("\nError processing command: "+ message);
			//e.printStackTrace();
		}
	}
	
	private void login(String user, String password){
		String response = "";
		sendData("login("+user+","+password+")");
		response = getMessage();
		if(response.length() < 1){
			displayMessage("\nNo message received from server.");
			return;
		}
		else if(message.substring(0, 1).equalsIgnoreCase("0")){
			//server returns login error - this is not needed here
			//displayMessage("\nInvalid Login: please try again.");
		}
		displayMessage("\n" + response.substring(2));
	}
	
	private void getLogs(){
		String response = "";
		sendData("getLogs()");
		response = getMessage();
		if(response.length() < 1){
			displayMessage("\nNo message received from server.");
			return;
		}
		else if(response.substring(0, 1).equalsIgnoreCase("0")){
			displayMessage("\nError on server.");
			return;
		}
		//process server response of logs, format output and display in gui
		String [] lines = response.substring(3,response.length()-1).split("\\],[\\s]*\\[");
		String message = "";
		for (String line : lines){
			String [] row = line.split(",");
			String user = row[0];
			String action = row[1];
			Timestamp stamp = new Timestamp(Long.parseLong(row[2].trim()));
			Date date = new Date(stamp.getTime());
			String timestamp = date.toString();
			message += timestamp + ": " + user + action + "\n";
		}
		displayMessage("\n" + message);
	}

	private String join(String [] strs, String spacer){
		if (strs.length==0){return strs[0];}
		String result = strs[0]+spacer;
		for (int i = 1; i<strs.length; i++){
			result += spacer+strs[i];
		}
		return result;
	}
	
	private void getRawAll(String message){
		parseMessage("get 10 raw", "graph");
		parseMessage("get 8 raw", "graph");
		parseMessage("get 4 raw", "graph");
		String type;
		if(message.contains(cmd.temp.toString())){type = "Temperature";}
		else if(message.contains(cmd.light.toString())){type = "Light";}
		else{displayMessage("\nNo valid graph arguments supplied.");return;}
		
		drawGraphRaw("4 8 10", type);
	}
	
	private void getAll(){
		String response = "";
		sendData("getAll()");
		response = getMessage();
		if(response.length() < 1){
			displayMessage("\nNo message received from server.");
			return;
		}
		else if(response.substring(0, 1).equalsIgnoreCase("0")){
			displayMessage("\nError on server.");
			return;
		}
		
		String a = response.substring(3);//[mean_temp,mode_temp,median_temp,variance_temp,standard_dev_temp],[mean_light,mode_light,median_light,variance_light,standard_dev_light]" 
		String b = a.replace("[", "");
		String c = b.replace("]","");//mean_temp,mode_temp,median_temp,variance_temp,standard_dev_temp,mean_light,mode_light,median_light,variance_light,standard_dev_light" 
		String[] d = c.split(",");
		String toPrint1 = "\nTemperature:\nMean = " + d[0] + "\nMode = " + d[1] + "\nMedian = " + d[2] + "\nVariance = " + d[3] + "\nStandard Deviation = " + d[4];
		String toPrint2 = "\nLight:\nMean = " + d[5] + "\nMode = " + d[6] + "\nMedian = " + d[7] + "\nVariance = " + d[8] + "\nStandard Deviation = " + d[9];
	
		displayMessage("\n" + toPrint1 + toPrint2);
	}
	
	private void getRawGroup(String id, String source, String message){
		String response = "";
		sendData("getRawGroup("+id+")");
		response = getMessage();
		if(response.length() < 1){
			displayMessage("\nNo message received from server.");
			return;
		}
		else if(response.substring(0, 1).equalsIgnoreCase("0")){
			displayMessage("\nError on server.");
			return;
		}
		try{
			if(id.equals("10")){ raw10 = response.substring(3);}
			else if(id.equals("8")){ raw8 = response.substring(3);}
			else if(id.equals("4")){ raw4 = response.substring(3);}
		}catch(Exception e){
			displayMessage("\nClient tried to be good for user, but data was not there - please no hate client :'(");
		}
		if(source.contains("graph")){
			return;
		}
		else{
			String type;
			if(message.contains(cmd.temp.toString())){type = "Temperature";}
			else if(message.contains(cmd.light.toString())){type = "Light";}
			else{displayMessage("\nNo valid graph arguments supplied.");return;}
			
			drawGraphRaw(id, type);
		}
		
	}
	
	private void getGroup(String id, String source){
		String response = "";
		sendData("getGroup("+id+")");
		response = getMessage();
		if(response.length() < 1){
			displayMessage("\nNo message received from server.");
			return;
		}
		else if(response.substring(0, 1).equalsIgnoreCase("0")){
			displayMessage("\nError on server.");
			return;
		}
		
		String a = response.substring(3);//[mean_temp,mode_temp,median_temp,variance_temp,standard_dev_temp],[mean_light,mode_light,median_light,variance_light,standard_dev_light]" 
		String b = a.replace("[", "");
		String c = b.replace("]","");//mean_temp,mode_temp,median_temp,variance_temp,standard_dev_temp,mean_light,mode_light,median_light,variance_light,standard_dev_light" 
		String d = c.replace(" ", "");
		String[] e = d.split(",");
		
		if(source.contains("graph")){
			editLocalData(e, id, source);
			return;
		}
		
		String toPrint1 = "\nTemperature:\nMean = " + e[0] + "\nMode = " + e[1] + "\nMedian = " + e[2] + "\nVariance = " + e[3] + "\nStandard Deviation = " + e[4];
		String toPrint2 = "\nLight:\nMean = " + e[5] + "\nMode = " + e[6] + "\nMedian = " + e[7] + "\nVariance = " + e[8] + "\nStandard Deviation = " + e[9];
	
		displayMessage("\n" + toPrint1 + toPrint2);
	}
	
	private void getGroupIDs() {
		String response = "";
		sendData("getGroupIds()");
		response = getMessage();
		if(message.length() < 1){
			displayMessage("\nNo message received from server.");
			return;
		}
		else if(message.substring(0, 1).equalsIgnoreCase("0")){
			displayMessage("\nError on server.");
			return;
		}
		displayMessage("\n" + response.substring(2));
	}
	
	private void editLocalData(String[] input, String id, String graph){
		displayMessage("\nEditing local data...");
		if(id.equals("4")){
			group4.meanT =  input[0];
			group4.medianT = input[1];
			group4.modeT = input[2];
			group4.varT = input[3];
			group4.stdT = input[4];
			group4.meanL =  input[5];
			group4.medianL = input[6];
			group4.modeL = input[7];
			group4.varL = input[8];
			group4.stdL = input[9];
		}
		else if(id.equals("8")){
			group8.meanT =  input[0];
			group8.medianT = input[1];
			group8.modeT = input[2];
			group8.varT = input[3];
			group8.stdT = input[4];
			group8.meanL =  input[5];
			group8.medianL = input[6];
			group8.modeL = input[7];
			group8.varL = input[8];
			group8.stdL = input[9];
		}
		else if(id.equals("10")){
			group10.meanT =  input[0];
			group10.medianT = input[1];
			group10.modeT = input[2];
			group10.varT = input[3];
			group10.stdT = input[4];
			group10.meanL =  input[5];
			group10.medianL = input[6];
			group10.modeL = input[7];
			group10.varL = input[8];
			group10.stdL = input[9];
		}
		String d1 = ""; String d2 = ""; String d3 = "";
		String e1 = ""; String e2 = ""; String e3 = "";
		if(graph.contains("Mean")){
			d1 = group4.meanT;
			d2 = group8.meanT;
			d3 = group10.meanT;
			e1 = group4.meanL;
			e2 = group8.meanL;
			e3 = group10.meanL;
		}
		else if(graph.contains("Median")){
			d1 = group4.meanT;
			d2 = group8.meanT;
			d3 = group10.meanT;
			e1 = group4.meanL;
			e2 = group8.meanL;
			e3 = group10.meanL;
		}
		else if(graph.contains("Mode")){
			d1 = group4.meanT;
			d2 = group8.meanT;
			d3 = group10.meanT;
			e1 = group4.meanL;
			e2 = group8.meanL;
			e3 = group10.meanL;
		}
		else if(graph.contains("Variance")){
			d1 = group4.meanT;
			d2 = group8.meanT;
			d3 = group10.meanT;
			e1 = group4.meanL;
			e2 = group8.meanL;
			e3 = group10.meanL;
		}
		else if(graph.contains("Standard Deviation")){
			d1 = group4.meanT;
			d2 = group8.meanT;
			d3 = group10.meanT;
			e1 = group4.meanL;
			e2 = group8.meanL;
			e3 = group10.meanL;
		}
		
		dataStringTemp = "Group 4=" + d1 + ":Group 8="+ d2 + ":Group 10=" + d3;
		dataStringLight = "Group 4=" + e1 + ":Group 8="+ e2 + ":Group 10=" + e3;
		
		displayMessage("Done");
	}
	
	private void sendSensorData(String filename){
		displayMessage("\nSending to server... ");
		Scanner scan = null;
		try {
			scan = new Scanner(new FileInputStream(filename));
			String[] line;
			int counter = 0;
			int success = 0;
			int failed = 0;
			float temp = 0;
			float light = 0;
			long timestamp = 0;
			String z="";
			while(scan.hasNext()){
				line = scan.nextLine().split(" ");
				try {
					String token = line[0].split("=")[1];
					if(counter % 2 == 0){
						temp = Float.parseFloat(token);
						timestamp = Long.parseLong(line[1]);
					}
					else{
						light = Float.parseFloat(token);
						z+="["+temp + ", " + light + ", " + timestamp+"]";
						++success;
					}
				}
				catch (Exception e) {
					++failed;
					System.out.println("1 failed to send");
					//e.printStackTrace();
				}
				

				++counter;
				
			}

			z=z.replaceAll("\\]\\[", "],[");
			sendData("sendData("+z+")");
			String response = getMessage();
			if(response.substring(0,1).equals("1")){
				displayMessage("\nSending completed.");
				displayMessage("\n" + success+ " successfull.");
				displayMessage("\n" + failed +" failed.");
			}
			if(scan != null)
				scan.close();
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
			displayMessage("\nError while sending. ");
			
		}	
		if(scan != null)
			scan.close();
	}
	
	private String getMessage() {
		
		message = "";
		String temp="";
		int counter = 0;
		int timeToWait = 70;
		try {
			while(!input.ready() && counter < timeToWait){
				try{
				Thread.sleep(1000);}catch(InterruptedException e){System.out.println("Threads sleep no longer, but in eternal slumber");} //e.printStackTrace();}
				counter++;
				System.out.println("Timeout: "+(timeToWait-counter)+" sec...");
			}char c;
			//System.out.print("Message is: ");
			while(input.ready() && (c=(char)input.read())!='\n'){
				temp+=c;
				//System.out.print(c);
			}
			//System.out.println("");
		} catch (IOException e) {
			System.out.println("Error getting message");
			//e.printStackTrace();
		}
		
		System.out.println("Stopped waiting: " + counter);
		message = temp;
		return message;
	}
	
	private void drawGraph(String message){
		
		String type = "";
		String reading = "";
		if(message.contains(cmd.mean.toString())){type = "Mean";}
		else if(message.contains(cmd.median.toString())){type = "Median";}
		else if(message.contains(cmd.mode.toString())){type = "Mode";}
		else if(message.contains(cmd.std.toString())){type = "Standard Deviation";}
		else if(message.contains(cmd.variance.toString())){type = "Variance";}
		else{displayMessage("\nNo valid graph arguments supplied.");return;}

		displayMessage("\nFetching data from server...");
		parseMessage("get 10", "graph " + type);
		parseMessage("get 8", "graph " + type);
		parseMessage("get 4", "graph "+ type );
		
		String[] dataSet;
		if(message.contains(cmd.temp.toString())){
			dataSet = dataStringTemp.split(":");
			reading = "Temperature" ;
		}
		else if(message.contains(cmd.light.toString())){
			dataSet = dataStringLight.split(":");
			reading = "Light" ;
		}
		else{displayMessage("\nData type specified not located in server.");return;}
		 
		String title = type + " of Sensor Readings (" + reading + ")";
		String category = "Sensor ID";
		String value = type;
		List<Data> dataList = new ArrayList<Data>(3);
		
		if(dataSet.length <1){
			displayMessage("\nNo data to draw.");
		}
		else{
			for(int i = 0; i < dataSet.length; ++i){
				try {
					String[] pair = dataSet[i].split("=");
					Data d = new Data(pair[0], Double.parseDouble(pair[1]));
					dataList.add(d);
				}catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
			if(dataList.size() < 1){
				displayMessage("\nNo data to draw.");
				return;
			}
			
			chart = new BarChart(title, category, value, dataList);
			chart.pack();
			RefineryUtilities.centerFrameOnScreen(chart);
			chart.setVisible(true);
			//chart.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		}
	}

	private void drawGraphRaw(String id, String type){
		
		boolean draw4 = false, draw8= false, draw10 = false;
		
		List<Data> data4 = new ArrayList<>();
		List<Data> data8 = new ArrayList<>();
		List<Data> data10 = new ArrayList<>();
		
		if(id.contains("10")){
			draw10 = true;
			fillDataList(data10, raw10, type);
		}
		if(id.contains("8")){
			draw8 = true;
			fillDataList(data8, raw8, type);
		}
		if(id.contains("4")){
			draw4 = true;
			fillDataList(data4, raw4, type);
		}
		
		LineGraph xychart = new LineGraph("Line Chart", "Raw "+ type + " Readings", "Timestamp", type, 
				draw4, draw8, draw10, data4, data8, data10);
		xychart.pack();
		RefineryUtilities.centerFrameOnScreen(xychart);
		xychart.setVisible(true);
		
	}
	
	private void fillDataList(List<Data> data, String dataString, String type){
		String a = dataString.replace("[", "");
		String b = a.replace("]", "");
		String[] c = b.split(",");
		
		if(c.length%3 != 0){
			System.out.println("Some data cannot be converted");
		}
		
		double temp = 0;
		double light = 0;
		double time = 0;
		
		for(int i = 0; i < c.length; ++i){
			try{
				if(i% 3 == 0){
					temp = Double.parseDouble(c[i]);
				}
				else if(i% 3 == 1){
					light = Double.parseDouble(c[i]);
				}
				else if(i% 3 == 2){
					time = Double.parseDouble(c[i]);
					Data d;
					if(type.equals("Temperature")){
						d = new Data(time, temp);
					}
					else{
						d = new Data(time, light);
					}
					data.add(d);
				}
			}
			catch(Exception e){
				System.out.println("Error generating graph data");
				//e.printStackTrace();
			}
		}
		
	}
	
}// end class Client

