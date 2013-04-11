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