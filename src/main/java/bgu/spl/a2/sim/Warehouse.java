package bgu.spl.a2.sim;

import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.Task;

/**
 * A class representing the warehouse in your simulation
 * 
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!!!
 *
 */
public class Warehouse {
	private ConcurrentHashMap<String, AtomicInteger> _toolsMap;
	private ConcurrentHashMap<String, ManufactoringPlan> _plansMap;
	
	private ConcurrentLinkedQueue<Deferred<Tool>> _pliersWaitingList;
	private ConcurrentLinkedQueue<Deferred<Tool>> _driverWaitingList;
	private ConcurrentLinkedQueue<Deferred<Tool>> _hammerWaitingList;
	/**
	* Constructor
	*/
    public Warehouse(){
    	_toolsMap=new ConcurrentHashMap<String, AtomicInteger>();
    	_plansMap=new ConcurrentHashMap<String, ManufactoringPlan>();
    	
    	_pliersWaitingList=new ConcurrentLinkedQueue<Deferred<Tool>>();
    	_driverWaitingList=new ConcurrentLinkedQueue<Deferred<Tool>>();
    	_hammerWaitingList=new ConcurrentLinkedQueue<Deferred<Tool>>();  	
    }

	/**
	* this method is synchronized because there is a case when one thread gets the number of the tool from the warehouse,
	* then the scheduler gives a CPU time to a second thread. the second thread gets the number of the tool as well, 
	* and both threads change the value of the product to the same value, while the value should be smaller.
	* 
	* Tool acquisition procedure
	* Note that this procedure is non-blocking and should return immediatly
	* @param type - string describing the required tool
	* @return a deferred promise for the  requested tool
	*/
    public synchronized Deferred<Tool> acquireTool(String type){
    	AtomicInteger newValue=_toolsMap.get(type);
    	Deferred<Tool> tool=new Deferred<Tool>(); 
    	if(newValue.intValue()>0){
    		newValue.decrementAndGet();
    		_toolsMap.replace(type, newValue);
			switch(type){
			case "rs-pliers" : tool.resolve(new RandomSumPliers()); break;
			case "gs-driver" : tool.resolve(new GcdScrewDriver()); break;
			case "np-hammer" : tool.resolve(new NextPrimeHammer()); break;
			}
    	}
    	else{
    		switch(type){
				case "rs-pliers" : _pliersWaitingList.add(tool); break;
				case "gs-driver" : _driverWaitingList.add(tool); break;
				case "np-hammer" : _hammerWaitingList.add(tool); break;
			}
    	}
    	return tool;
    }

	/**
	* this method is synchronized because there is a case when one thread gets the number of the tool from the warehouse,
	* then the scheduler gives a CPU time to a second thread. the second thread gets the number of the tool as well, 
	* and both threads change the value of the product to the same value, while the value should be bigger.  
	* 
	* Tool return procedure - releases a tool which becomes available in the warehouse upon completion.
	* @param tool - The tool to be returned
	*/
    public synchronized void releaseTool(Tool tool){
    	String type = tool.getType();
    	if(type=="rs-pliers"){
    		if(!_pliersWaitingList.isEmpty()){
    			Deferred<Tool> promiseTool=_pliersWaitingList.remove();
    			promiseTool.resolve(tool);
    			return;
    		}
    	}
    	else if(type=="gs-driver"){
    			if(!_driverWaitingList.isEmpty()){
        			Deferred<Tool> promiseTool=_driverWaitingList.remove();
        			promiseTool.resolve(tool);
        			return;
    			}
    	}
    	else if(type=="np-hammer"){
    			if(!_hammerWaitingList.isEmpty()){
        			Deferred<Tool> promiseTool=_hammerWaitingList.remove();
        			promiseTool.resolve(tool);
        			return;
    			}
    	}
    	AtomicInteger newValue=_toolsMap.get(tool.getType());
    	newValue.incrementAndGet();
    	_toolsMap.replace(tool.getType(), newValue);

    }

	
	/**
	* Getter for ManufactoringPlans
	* @param product - a string with the product name for which a ManufactoringPlan is desired
	* @return A ManufactoringPlan for product
	*/
    public ManufactoringPlan getPlan(String product){
    	return _plansMap.get(product);
    }
	
	/**
	* Store a ManufactoringPlan in the warehouse for later retrieval
	* @param plan - a ManufactoringPlan to be stored
	*/
    public void addPlan(ManufactoringPlan plan){
    	_plansMap.putIfAbsent(plan.getProductName(), plan);
    }
    
	/**
	* Store a qty Amount of tools of type tool in the warehouse for later retrieval
	* @param tool - type of tool to be stored
	* @param qty - amount of tools of type tool to be stored
	*/
    public void addTool(Tool tool, int qty){
    	_toolsMap.putIfAbsent(tool.getType(), new AtomicInteger(qty));
    }

}
