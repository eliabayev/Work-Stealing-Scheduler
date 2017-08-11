package bgu.spl.a2.sim.tools;

import java.math.BigInteger;
import bgu.spl.a2.sim.Product;

public class GcdScrewDriver implements Tool {
	
	/**
	 * @return the type of this tool
	 */
	@Override
	public String getType() {
		return "gs-driver";
	}
	/**
	 * @param p - the product which the useOn function will be executed on
	 * @return value - the number that this function is calculating
	 */
	@Override
	public long useOn(Product p){
    	long value=0;
    	for(Product part : p.getParts()){
    		value+=Math.abs(func(part.getFinalId()));
    		
    	}
      return value;
    }
	
	/**
	 * 
	 * @param id - the id of the product which this tool is working on
	 * @return the greatest common divisor between the id of the product which this 
	 * tool is working on and the reverse
	 * 
	 */
	private long func(long id){
		BigInteger b1 = BigInteger.valueOf(id);
		BigInteger b2 = BigInteger.valueOf(reverse(id));
		long value= (b1.gcd(b2)).longValue();
		return value;
	}
	
	/**
	 * 
	 * @param n - the id of the product which this tool is working on 
	 * @return the reverse id
	 */
	private long reverse(long n){
		long reverse=0;
		while( n != 0 ){
			reverse = reverse * 10;
			reverse = reverse + n%10;
			n = n/10;
		}
		return reverse;
	}
	

}
