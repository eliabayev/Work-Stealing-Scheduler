package bgu.spl.a2.sim.tools;

import bgu.spl.a2.sim.Product;

public class NextPrimeHammer implements Tool {
	
	/**
	 * @return the type of this tool
	 */
	@Override
	public String getType() {
		return "np-hammer";
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
	  * @param id - the id of the product which this tool is working on
	  * @return the first prime number following the product id
	  */
    public long func(long id) {	
        long v =id + 1;
        while (!isPrime(v)) {
            v++;
        }
        return v;
    }
    
    /**
     * @param value which is a number start from the id of the product that this tool
     * is working on
     * @return if the variable value is a prime number
     */
    private boolean isPrime(long value) {
        if(value < 2) return false;
    	if(value == 2) return true;
        long sq = (long) Math.sqrt(value);
        for (long i = 2; i <= sq; i++) {
            if (value % i == 0) {
                return false;
            }
        }

        return true;
    }
}
