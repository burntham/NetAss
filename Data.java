//CSC3002F
//Network Assignment 1
//Gerard Nothnagel|Jared Norman|Daniel Burnham-King
//NTHGER001|NRMJAR001|BRNDAN022
//Group 10
//Data

public class Data {
	
	String id;  // Group ID 	
	double value; // the data value
	public double x;
	public double y;
	
	public Data(String id, double value){
		this.id = id;
		this.value = value;
	}
	public Data(double x, double y){
		this.x = x;
		this.y = y;
	}
}
