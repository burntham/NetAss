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
