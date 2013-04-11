import java.net.*;
import java.io.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SingleSocketServer {

	static ServerSocket socket;
	protected final static int port = 8087;
	private static LogThread logger;
	private static DBThread dbThread;
	static boolean first;
	static StringBuffer process;
	static String TimeStamp;

	public static void main(String[] args) {
		try{
			socket = new ServerSocket(port);
			System.out.println("SingleSocketServer Initialized");

			System.out.println("Starting DB thread...");
			dbThread = new DBThread();
			dbThread.start();

			System.out.println("Starting Logging thread...");
			logger = new LogThread();
			logger.start();

			while (true) {
				ClientThread newThread = new ClientThread(socket.accept());
				newThread.start();
			}
		}
		catch (IOException e) {e.printStackTrace();}
	}

	private static class ClientThread extends Thread {

		private boolean loggedIn = false;
		private Socket socket;
		public RequestHandler request;
		private ResponseHandler response;
		private BufferedReader in;
		private PrintWriter out;
		private String username;
		private String queryString;
		private ResultSet rs;
		private int group_id;
		public boolean hasResult;
		public ClientThread(Socket socketP) {
			this.socket = socketP;
		}

		public void run() {
			try {
				// Create character streams for the socket.
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));//handle requests from client
				out = new PrintWriter(socket.getOutputStream(), true);//handle responses to client
				request = new RequestHandler(in);
				response = new ResponseHandler(out);
			}catch(Exception e){e.printStackTrace();}

			//now we have a while loop and await instructions from the client.
			while (!socket.isInputShutdown() && !socket.isOutputShutdown()){
				request.getNext();
				System.out.println("Server fetched a request. Parsing...");

				/*
				 * @login
				 * returns: success, message
				 * success: 0 or 1
				 * message: string, some relevant message you can print in your client.
				 * */
				if (request.getMethod().equals("login")){
					boolean valid = false;
					try{
						String [] args = request.args.split("[\\s]*,[\\s]*");
						String trialUsername=args[0], trialPassword=args[1];

						this.hasResult=false;//must reset this for every method...
						this.queryString = "SELECT * " + "from users WHERE username ='" +trialUsername+"' AND password='" + trialPassword + "'";
						DBThread.executeQuery(this);//this adds this thread to DBThreads processing queue

						while(!this.hasResult){
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {e.printStackTrace();}
						}

						//If result set has an entry, the user/pass combination exists
						if (rs.next())
						{
							valid = true;
						}

						response.responseString="";      
						//If user logged in successfully
						if (valid){
							System.out.println("User logged in!");
							this.username = trialUsername;
							loggedIn=true;
							response.success=true;
							response.responseString="Login was succesful. Welcome "+username+"!";
							group_id = rs.getInt("group_id");
							System.out.println("The group ID of " + username + " is " + group_id);
						}
						//Login failed
						else{
							System.out.println("Login failed!");
							response.success=false;
							response.responseString="Login was not succesful. Incorrect username or password.";
						}
					}catch(Exception e){e.printStackTrace();response.success=false; response.responseString="Whaaaat?";}//incorrect parsing of args
					finally{response.send();}
				}
				
				/*
				 * @getAll
				 * returns: success, [mean_temp,mode_temp,median_temp,variance_temp,standard_dev_temp],
				 *          [mean_light,mode_light,median_light,variance_light,standard_dev_light]
				 * success: 0 or 1
				 * */
				else if (request.method.equals("getAll")){
					if (!loggedIn){response.responseString="You are not authenticated. Please log to continue.";response.success=false;response.send();continue;}
					this.queryString = "SELECT * " + "FROM weather ORDER BY time_stamp";
					this.hasResult=false;//must reset this for every method...
					DBThread.executeQuery(this);//this adds this thread to DBThreads processing queue
					//When DBThread is ready, it'll look at this.request.method and process the request.
					//Then it'll return a ResultSet from the DB... which we must process here.
					//so now we wait...
/*					while(!this.hasResult){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {e.printStackTrace();}
					}*/
					//cool, the DB got back to us to we process the data here...
					try{
						System.out.println("this is interesting...");
						ArrayList<Float> tempReadings = new ArrayList<Float>();
						ArrayList<Float> lightReadings = new ArrayList<Float>();
						float [] tempStats = null;
						float [] lightStats = null;//to be returned
						if (rs==null){response.success=false;response.responseString="Error occured in fetching result set from the database.";continue;}
						int count =0;
						while(rs.next()){
							float temp = rs.getFloat("heat");
							tempReadings.add(temp);
							float light = rs.getFloat("light");
							lightReadings.add(light);
							count++;
						}//end while loop
						
						tempStats = getStats(tempReadings);
						lightStats = getStats(lightReadings);
						float [][] result = {tempStats,lightStats};//may be null
						if (result[0]==null||result[1]==null){response.success=false; response.responseString="There are no entries in the database.";response.send();continue;}
						response.responseString="";
						for (float[]stats : result){
							response.responseString+="["+join(stats,", ")+"]";
						}
						response.responseString=response.responseString.replaceAll("\\]\\[", "],[");
						response.success=true;
						
						// logging
						String logstring = username + ", downloaded " + count + " aggregated entries from all, "+System.currentTimeMillis()+"\n";
						logger.add(logstring);
					}catch(SQLException e){e.printStackTrace();}
					response.send();
				}
				
				/*
				 * @getRawAll
				 * returns: success, [temp1, light1, timestamp1], [temp2, light2, timestamp2] ...
				 * success: 0 or 1
				 * temp: float, mantissa is a period
				 * light: float, mantissa is a period
				 * timestamp: number of millis since Jan 1 1970 as in System.currentTimeMillis()
				 * */
				else if (request.method.equals("getRawAll")){
					if (!loggedIn){response.responseString="You are not authenticated. Please log to continue.";response.success=false;response.send();continue;}
					this.queryString = "SELECT * " + "FROM weather ORDER BY time_stamp";
					this.hasResult=false;//must reset this for every method...
					DBThread.executeQuery(this);//this adds this thread to DBThreads processing queue
					//When DBThread is ready, it'll look at this.request.method and process the request.
					//Then it'll return a ResultSet from the DB... which we must process here.
					//so now we wait...

					while(!this.hasResult){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {e.printStackTrace();}
					}
					//cool, the DB got back to us to we process the data here...
					
					try{
						response.responseString="";
						int count = 0;
						while(rs.next()){
							float heat = rs.getFloat("heat");
							float light  = rs.getFloat("light");
							long timestamp = rs.getTimestamp("time_stamp").getTime();//millis since Jan 1 1970
							response.responseString+="["+heat+", "+light+", "+timestamp+"]";
							count++;
						}//end while loop
						response.responseString=response.responseString.replaceAll("\\]\\[","],[");//commas
						response.success=true;

						//Logging
						String logstring = username + ", downloaded " + count + " raw entries from all, "+System.currentTimeMillis()+"\n";
						logger.add(logstring);
					}catch(SQLException e){e.printStackTrace();}
					response.send();
				}
				
				/*
				 * @getGroup
				 *returns: success, [mean_temp,mode_temp,median_temp,variance_temp,standard_dev_temp],
				 *          		[mean_light,mode_light,median_light,variance_light,standard_dev_light]
				 * see @getAll
				 * */
				else if(request.method.equals("getGroup")){
					if (!loggedIn){response.responseString="You are not authenticated. Please log to continue.";response.success=false;response.send();continue;}
					try{Integer.parseInt(request.args);}
					catch(NumberFormatException e){
						System.out.println("Invalid group ID");
						response.success=false;
						response.responseString="Invalid group ID entered.";
						continue;
					}//return that only numbers are accepted.
					this.queryString = "SELECT * " + "FROM weather WHERE group_id ='"+request.args+"' ORDER BY time_stamp ";//we know its a string
					this.hasResult=false;//must reset this for every method...
					DBThread.executeQuery(this);//this adds this thread to DBThreads processing queue
					//When DBThread is ready, it'll look at this.request.method and process the request.
					//Then it'll return a ResultSet from the DB... which we must process here.
					//so now we wait...
					while(!this.hasResult){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {e.printStackTrace();}
					}
					//cool, the DB got back to us to we process the data here...
					try{
						ArrayList<Float> tempReadings = new ArrayList<Float>();
						ArrayList<Float> lightReadings = new ArrayList<Float>();
						float [] tempStats = null;
						float [] lightStats = null;//to be returned
						response.success=true;
						int count = 0;
						while(rs.next()){
							float temp = rs.getFloat("heat");
							tempReadings.add(temp);
							float light = rs.getFloat("light");
							lightReadings.add(light);
							count++;
						}//end while loop
						tempStats = getStats(tempReadings);
						lightStats = getStats(lightReadings);
						float [][] result = {tempStats,lightStats};//may be null
                                                if (result[0]==null||result[1]==null){response.success=false; response.responseString="There are no entries in the database.";response.send();continue;}
						response.responseString="";
						for (float[]stats : result){
							response.responseString+="[";
							for (int i = 0; i<stats.length-1;i++){
								response.responseString+=stats[i]+", ";
							}
							response.responseString+=stats[stats.length-1];
							response.responseString+="]";
						}

						response.responseString=response.responseString.replaceAll("\\]\\[", "],[");
						response.success=true;

						//Logging
						String logstring = username + ", downloaded " + count + " aggregated entries from " + request.args + ", "+System.currentTimeMillis()+"\n";
						logger.add(logstring);
					}
					catch(SQLException e){e.printStackTrace();response.success=false;response.responseString="Error with the database.";}
					response.send();
				}
				
				/*
				 * @getRawGroups
				 * returns: success, [temp1, light1, timestamp1], [temp2, light2, timestamp2] ...
				 * see @getRawAll
				 * */
				else if(request.method.equals("getRawGroup")){
					if (!loggedIn){response.responseString="You are not authenticated. Please log to continue.";response.success=false;response.send();continue;}
					this.queryString = "SELECT * " +
							"FROM weather WHERE group_id ='"+request.args+"' ORDER BY time_stamp ";
					this.hasResult=false;
					DBThread.executeQuery(this);
					while(!this.hasResult){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {e.printStackTrace();}
					}

					try{
						response.responseString="";
						int count = 0;
						while(rs.next()){
							float heat = rs.getFloat("heat");
							float light  = rs.getFloat("light");
							long timestamp = rs.getTimestamp("time_stamp").getTime();//millis since Jan 1 1970
							response.responseString+="["+heat+", "+light+", "+timestamp+"]";
							count++;
						}//end while loop
						response.responseString=response.responseString.replaceAll("\\]\\[","],[");//commas
						response.success=true;

						String logstring = username + ", downloaded " + count + " raw entries from " + request.args + ", "+System.currentTimeMillis()+"\n";
						logger.add(logstring);
					}catch(SQLException e){e.printStackTrace();}
					response.send();
				}
				/*
				 * @getGroupIds
				 * returns: success, [groupid1, groupid2, ...]
				 * success: 0 or 1
				 * groupid: int, a group id
				 * */
				else if(request.method.equals("getGroupIds")){
					if (!loggedIn){response.responseString="You are not authenticated. Please log to continue.";response.success=false;response.send();continue;}
					this.queryString = "SELECT DISTINCT group_id FROM weather ORDER BY group_id";
					this.hasResult=false;
					DBThread.executeQuery(this);

					while(!this.hasResult){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {e.printStackTrace();}
					}

					try{
						ArrayList<String>group_ids = new ArrayList<String>();
						while(rs.next()){
							String id = ""+rs.getInt("group_id");
							group_ids.add(id);
						}
						String [] v = new String[group_ids.size()];
						v=group_ids.toArray(v);
						response.responseString = "[" + join(v,", ") + "]";
						response.success=true;
					}catch(SQLException e){e.printStackTrace();}
					response.send();
				}
				
				/*
				 * @sendData
				 * returns: success, message
				 * success: 0 or 1
				 * message: String, either a œsuccess or error message, relevant to whatever the value of success was.
				 * */
				/*
				 * @sendData
				 * returns: success, message
				 * success: 0 or 1
				 * message: String, either a success or error message, relevant to whatever the value of success was.
				 * */
				else if(request.method.equals("sendData")){
					if (!loggedIn){response.responseString="You are not authenticated. Please log to continue.";response.success=false;response.send();continue;}
					ArrayList<String>values=new ArrayList<String>();

					int counter = 0;
					for (String d : request.args.split("\\[")){
						if (d.length()==0)continue;//ignore the 1st (empty) string from the split
						d=d.split("\\]")[0];
						String [] entryData = d.split("[\\s]*,[\\s]*");
						TimeZone.setDefault(TimeZone.getTimeZone("GMT+2:00"));
						java.sql.Timestamp time_stamp = new java.sql.Timestamp(Long.parseLong(entryData[2]));
						String t = time_stamp.toString();//new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(time_stamp);
						values.add("("+entryData[0]+", "+entryData[1]+", '"+t+"', "+group_id+")");
						counter++;
					}

					String [] v = new String[values.size()];
					this.queryString = "INSERT INTO weather (heat, light, time_stamp, group_id) VALUES" + join(values.toArray(v),", ");
					this.hasResult=false;
					DBThread.executeQuery(this);

					while(!this.hasResult){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					response.responseString = counter + " entries were added to the database.";
					response.success=true;

					//Logging
					String logstring = username+", uploaded "+counter+" entries from "+group_id+", "+System.currentTimeMillis()+"\n";
					logger.add(logstring);
					response.send();
				}
				else if(request.method.equals("resetData")){
					if (!loggedIn){response.responseString="You are not authenticated. Please log to continue.";response.success=false;response.send();continue;}
					if (!username.equals("nrmjar001")){response.responseString="You are not Jared. Please be Jared to reset the logs.";response.success=false;response.send();continue;}
					response.responseString = "You cleared the DB, hope you know what you're doing!";
					response.success=true;
					this.queryString = "TRUNCATE weather";
					this.hasResult=false;
                                	DBThread.executeQuery(this);

                                	while(!this.hasResult){
                                        	try {
                                        	        Thread.sleep(100);
                                        	} catch (InterruptedException e) {
                                                	e.printStackTrace();
                                        	}
                                	}
					response.send();
				}	
				/*
				 * @getLogs
				 * returns: success, [user1,action1,time1],[user2,action2,time2]...
				 * success: 0 or 1
				 * user: String, the username of the person committing the action.
				 * action: String, either â€œuploaded n entries from mâ€ or â€œdownloaded n entries from mâ€ where n is the amount of entries, and m is the group (or all).
				 * time is the number of milliseconds since 1 January 1970 (see Javas currentTimeMillis() method).
				 * */
				else if(request.method.equals("getLogs")){
					if (!loggedIn){response.responseString="You are not authenticated. Please log to continue.";response.success=false;response.send();continue;}
					response.success=true;
					String msg="";
					String everything = logger.readFile();
					for (String i : everything.split("\n"))
						msg+="["+i+"]";
					msg=msg.replaceAll("\\]\\[", "],[");
					response.responseString=msg;
					response.send();
				}
				else {
					System.out.println("Bad API Query: \""+request.method+"\"");
					response.success=false;
					response.responseString="Bad API function. Did you read the API? Are you sending something like method(arg1,arg2,...)?";
					response.send();
				}
			}


		}

		//returns arraylist of mmean, median, mode, sd, variance
		public float [] getStats(ArrayList<Float> list){

			// Mean
			float mean=0;
			int n=list.size();
			if (n==0){return null;}//catch the case of no data.
			for (int i=0; i<n; i++){
				mean+=list.get(i);
			}
			mean/=n;

			// Median
			float median = list.get(list.size()/2);

			// Mode (Most frequently occurring reading) - This uses the floor value of each reading. So for a data set:
			// [1.321 , 1.753, 2.231 , 3.734, 3.897, 3.412]         The mode is 3.0.
			// I feel this is the best approach for a limited data set with 3 decimal places per reading.
			HashMap<Integer,Integer> frequency = new HashMap<Integer,Integer>();
			for (int i = 0; i < list.size(); i ++){
				int val = (int) Math.floor(list.get(i));
				Integer freq = frequency.get(val);
				frequency.put(val, (freq == null ? 1 : freq+1));
			}

			Integer mode = null;
			int max = 0;

			for (Entry<Integer, Integer> entry : frequency.entrySet()) {
				int freq = entry.getValue();
				if (freq > max){
					max = freq;
					mode = entry.getKey();
				}
			}

			// Variance
			float variance=0;
			for (int i=0; i<n; i++){
				variance+=Math.pow(list.get(i)-mean,2);
			}
			variance/=n;//http://en.wikipedia.org/wiki/Variance

			// Standard Deviation
			float sd=(float)Math.sqrt(variance);


			Collections.sort(list);
			float [] stats = {mean, median, mode, sd, variance};
			return stats;
		}

		//called by DBThread to set the DBResult for queries.
		public void setDBResult(ResultSet rsP){
			this.rs = rsP;
		}

		//python inspired join helper method.
		//joins the strings in arr with the spacer,
		//eg join([a,b,c],", ") returns "a, b, c"
		private synchronized String join(float[] stats, String string){
			String result="";
			for (int i = 0; i<stats.length-1;i++){
				result+=stats[i]+string;
			}
			result+=stats[stats.length-1];
			return result;
		}
		
		private synchronized String join(String[] stats, String string){
			String result="";
			for (int i = 0; i<stats.length-1;i++){
				result+=stats[i]+string;
			}
			result+=stats[stats.length-1];
			return result;
		}

		private class RequestHandler{
			private BufferedReader in;
			private String requestString;
			private String method;
			private String args;
			protected boolean success;

			private RequestHandler(BufferedReader inP) {
				in = inP;
				System.out.println("in = "+in.toString());
			}

			protected void setRequest(String request) {
				this.requestString = request;
			}
			protected String getRequest() {
				return requestString;
			}
			protected String getMethod() {
				return method;
			}
			protected String getArgs() {
				return args;
			}
			protected boolean isSuccess() {
				return success;
			}
			
			//get and parse the next request.
			protected void getNext() {
				String next="";
				try{
					while (!in.ready()){Thread.sleep(100);}//wait until there's a command to be parsed.
					next = in.readLine();
					System.out.println("request -> "+next);
				}catch(IOException e){
					e.printStackTrace();
				} catch (InterruptedException e) { //in case the thread is interrupted while sleeping
					e.printStackTrace();
				}
				//parse the content
				try {
					String [] parts = next.split("[\\s]*\\([\\s]*"); //left is method right is args with a ")" at the end
					this.method = parts[0].trim();
					args=parts[1].equals(")")?"":parts[1].split("\\)")[0];
					System.out.println("method: "+this.method+" and args: "+args);
					requestString=next;
				}
				catch (ArrayIndexOutOfBoundsException e){this.method="invalidMethod"; this.args="invalidArgs";}//this error will bubble up
				catch (Exception e){e.printStackTrace();}//most probably some other parsing error.
			}//get and parse the next request.
		}

		private class ResponseHandler{
			PrintWriter out;
			String responseString;
			boolean success;

			private ResponseHandler(PrintWriter outP) {
				out = outP;
				responseString = "NULL";
			}

			protected void send(){
				String response = (success?"1,":"0,")+responseString+"\n";
				System.out.println("response: "+response);
				out.write(response);
				out.flush();
			}
		}

		public String getQueryString() {
			return this.queryString;
		}
	}

	private static class LogThread extends Thread {
		BufferedWriter logfile;
		ConcurrentLinkedQueue<String> logQueue;
		public LogThread(){

			String oldFile = readFile();
			//read the old log file
			BufferedReader inKb;
			logQueue = new ConcurrentLinkedQueue<String>();
			try {
				//setup the new filewriter
				this.logfile = new BufferedWriter(new FileWriter("log.txt"));
				//write to file again
				System.out.print("--->");
				for (char c : oldFile.toCharArray()){
					//System.out.print((int)c+"="+c+",");
					if ((int)c<1000){
					logfile.write(c);}
				}
				System.out.println("<---");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();

			}
			//print that all went well
			System.out.println("Log Thread was created!");
		}

		public void run(){
			while(true){
				try{
					//look into queue for new items
					if(!logQueue.isEmpty()){
						String val = logQueue.poll();
						System.out.println("Polled "+val);
						log(val);
					}
					//log them
					Thread.sleep(100);
				}catch(InterruptedException e){e.printStackTrace();}
			}
		}

		public synchronized void add(String logMe){
			System.out.println("Adding "+logMe+" to the queue.");
			logQueue.add(logMe);
		}

		public synchronized String readFile(){
			BufferedReader inKb;
			File f = new File("./log.txt");
			try{
				if (!f.exists()){
					f.createNewFile();
				}
				inKb = new BufferedReader(new FileReader(f));
				String text = "";
				int nextChar;
				do{
					nextChar = inKb.read();
					text += (char)nextChar;
				}while (inKb.ready());
				inKb.close();
				return text;
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();} 
			catch (IOException e) {
				e.printStackTrace();}
			return "";
		}

		private void log(String logMe){
			System.out.print("Writing to log file: \"");
			try {
				for (char c : logMe.toCharArray()){
					System.out.print(c);
					this.logfile.write((char)c);
				}
				logfile.flush();                               
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("\"");
		}
	}
	private static class DBThread extends Thread {
		static ConcurrentLinkedQueue<ClientThread> threads;
		Statement stmt;
		Connection con;
		public DBThread(){
			System.out.println("DB Thread created!");
			threads = new ConcurrentLinkedQueue<ClientThread>();
		}

		public static void executeQuery(ClientThread clientThread) {
			threads.add(clientThread);
		}

		@Override
		public void run() {
			try {
				//Register JDBC driver for MySQL.
				Class.forName("com.mysql.jdbc.Driver");

				//DB url, default port for MySQL is 3306
				String url = "jdbc:mysql://137.158.59.46:3306/nrmjar001";

				//Get a connection to the database for a
				// user named root with a password.
				System.out.println("Trying connection to MySQL instance in nightmare.cs.uct.ac.za");
				con = DriverManager.getConnection(url,"nrmjar001", "devaawae");
				System.out.println("Success!");
				//Display URL and connection information
				System.out.println("DB URL: " + url);
				System.out.println("DB Connection: " + con);
				
				while (true){
					while (!threads.isEmpty()){
						ClientThread thread = threads.poll();
						String method = thread.request.method;
						System.out.println("DB executing statement for "+method);
						String queryString = thread.getQueryString();
						//if we can to write
						if (thread.request.method.equals("sendData")||thread.request.method.equals("resetData")){
							try{
								System.out.println("SQL<--"+queryString);
								stmt = con.createStatement();
								stmt.executeUpdate(queryString);
							}catch(SQLException e){e.printStackTrace();}
						}
						else{//otherwise we just query
							thread.setDBResult(getResultSet(queryString));//set the rs variable in ClientThread to the ResultSet we get back from the SQL query
						}
						thread.hasResult=true;//tell the client it can stop waiting
					}
					Thread.sleep(100);
				}
			}
			catch(InterruptedException e){e.printStackTrace();}
			catch(Exception e){e.printStackTrace();}
		}

		//returns null on error
		private synchronized ResultSet getResultSet(String query){
			try{
				//now try a query
				stmt = con.createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);

				//Query the database, storing the result
				// in an object of type ResultSet
				ResultSet rs = stmt.executeQuery(query);
				return rs;
			}catch(SQLException e){e.printStackTrace();}
			return null;
		}
	}
}