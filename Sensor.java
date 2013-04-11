public class Sensor {
	
	public String meanT;
	public String medianT;
	public String modeT;
	public String varT;
	public 	String stdT;
	public 	String meanL;
	public 	String medianL;
	public String modeL;
	public String varL;
	public String stdL;
	
	public Sensor(String mean, String median, String mode, String var, String std,
			String meanR, String medianR, String modeR, String varR, String stdR){
		this.meanT = mean;
		this.medianT = median;
		this.modeT = mode;
		this.varT  = var;
		this.stdT = std;
		this.meanL = meanR;
		this.medianL = medianR;
		this.modeL = modeR;
		this.varL  = varR;
		this.stdL = stdR;
	}
	public Sensor(){
		this.meanT= "0";
		this.medianT = "0";
		this.modeT = "0";
		this.varT  = "0";
		this.stdT = "0";
		this.meanL = "0";
		this.medianL = "0";
		this.modeL = "0";
		this.varL  = "0";
		this.stdL = "0";
	}
}
