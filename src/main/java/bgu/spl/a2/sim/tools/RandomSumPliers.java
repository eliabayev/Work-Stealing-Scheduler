package bgu.spl.a2.sim.tools;

import java.util.Random;
import bgu.spl.a2.sim.Product;

public class RandomSumPliers implements Tool {
	
	/**
	 * @return the type of this tool
	 */
	@Override
	public String getType() {
		return "rs-pliers";
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
	 * @param id - the id of the product which this product is working on
	 * @return sum of id%10000 random integers
	 */
    public long func(long id){
    	Random r = new Random(id);
        long  sum = 0;
        for (long i = 0; i < id % 10000; i++) {
            sum += r.nextInt();
        }

        return sum;
    }

}
