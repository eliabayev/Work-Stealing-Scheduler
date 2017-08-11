package bgu.spl.a2.sim.tasks;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.Task;
import bgu.spl.a2.sim.Product;
import bgu.spl.a2.sim.Warehouse;
import bgu.spl.a2.sim.tools.Tool;

public class ToolTask extends Task<Tool> {
	private String _toolName;
	private Product _product;
	private Warehouse _wareHouse;
	
	/**
	 * Constructor
	 * @param toolName
	 * @param product
	 * @param wareHouse
	 */
	public ToolTask(String toolName, Product product, Warehouse wareHouse){
		_toolName=toolName;
		_product=product;
		_wareHouse=wareHouse;
	}
	
	/**
	 * if the tool is available at the warehouse, acquire it, and perform the useOn method
	 * of the tool on them.
	 * else, wait until the tool will be available, and them perform the useOn method
	 * of the tool on them.
	 */
	public void start(){
		Deferred<Tool> promise=_wareHouse.acquireTool(_toolName);
		if(promise.isResolved()){
			long currentFinalId=promise.get().useOn(_product);
			_wareHouse.releaseTool(promise.get());
			_product.setFinalId(currentFinalId);
			complete(promise.get());
		}
		else{
			promise.whenResolved(()->{
				long currentFinalId=promise.get().useOn(_product);
				_wareHouse.releaseTool(promise.get());
				_product.setFinalId(currentFinalId);
				complete(promise.get());
			});	
		}
	}
}
