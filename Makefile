JC = javac
SFILES = SingleSocketServer.java 
CFILES = Client.java BarChart.java ClientTest.java Sensor.java LineGraph.java
CLIBS = -cp .:clibs/*:./
SLIBS = -cp .:slibs/*:./

All: $(CFILES) $(SFILES)
	$(JC) $(CLIBS) $(CFILES)
	$(JC) $(SLIBS) $(SFILES)

client: compileClient
	java $(CLIBS) ClientTest

compileClient: $(CFILES)
	$(JC) $(CLIBS) $(CFILES)

server: compileServer
	java $(SLIBS) SingleSocketServer

compileServer: $(SFILES)
	$(JC) $(SLIBS) $(SFILES)

clean:
	rm -f *.class
