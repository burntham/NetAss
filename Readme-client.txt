CSC3002F
Network Assignment 1
Gerard Nothnagel|Jared Norman|Daniel Burnham-King
NTHGER001|NRMJAR001|BRNDAN022
Group 10

Compilation + Execution:
	To compile the client, run the command 'make client' in terminal to both compile and run the client.

	Optionally, run 'make compileClient' to compile
	the client can then be executed as: java -cp .:clibs/*:./ ClientTest.java <optional args>
	Optional args can be used to speccify the server address (when left blank, client automatically connects to nightmare)
	
Use:
	Users interact with the client via text input box at the bottom of the window - with the following commands:
		login <username> <password>
			:logs the user in
		upload <textfilename>
			:uploads a specified files data to the server
		clear
			:clears the window
		get logs
			:Prints the server log
		get id
			:Returns the id of the clients group (10)
		get id all
			:Returns the ids of all the groups that have sent data to the server
		get <group id>
			:Returns aggregate data for specified group
		get <group id> raw <data_type>
			:Draws a linegraph of the groups raw data.
		get all
			:Prints all groups aggregate data
		get all raw <data_type>
			:Draws a line graph representing all the groups data (colour coded by group)
		graph <stat_type> <data_type>
			:Draws a bar chart allowing for the comparison of the 3 groups' data
		close bar
			:closes an open bar chart

	<data_type> can be any either:
		light
		temp

	<stat_type> can be any of the following:
		mean, median, mode, variance, std
		std is for standard deviation


FileList:
	ClientTest.java (driver), BarChart.java, Client.java, Data.java, LineGraph.java, Makefile, Readme-client.txt, Sensor.java, Trial.txt, minitrial.txt

	/clibs (contains required client Libraries):
		gunjaxp.jar, jcommon-1.0.17.jar, jfreechart-1.014-experimental.jar, jfreechart-1.14-swt.jar, jfreechart-1.14.jar, junit.jar, servlet.jar, swtgraphics.jar


File Descriptions:
	ClientTest.java:

	BarChart.java:

	Client.java:

	Data.java:

	LineGraph.java:

	Sensor.java:

	Trial.txt:
		Stored Captured Sensor data
		format must be as:  light=<value> <Timestamp>
							temp=<value> <Timestamp>

Notes:
	Server files and client files can exist together peacefully, the same makefile can be used to compile both - refer to Readme-server.txt for more info

	Do not close graph windows with the cross button. For bar charts, type close in the client. For linegraphs, pretend they are closed and run other commands
