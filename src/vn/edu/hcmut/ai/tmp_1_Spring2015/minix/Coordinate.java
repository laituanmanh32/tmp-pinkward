package vn.edu.hcmut.ai.tmp_1_Spring2015.minix;

public class Coordinate
{
     protected double x,y;

	 public Coordinate( double x, double y ){
		 this.x = x;
		 this.y = y;
	 }

	 public Coordinate() {
		// TODO Auto-generated constructor stub
	}

	 public void set(double x, double y){
		 this.x = x;
		 this.y = y;
	 }

	public double getX(){ return x; }
	 public double getY(){ return y; }
}
