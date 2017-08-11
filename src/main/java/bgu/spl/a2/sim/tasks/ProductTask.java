package bgu.spl.a2.sim.tasks;

import java.util.ArrayList;

import bgu.spl.a2.Task;
import bgu.spl.a2.sim.Product;
import bgu.spl.a2.sim.Warehouse;

public class ProductTask extends Task<Product> {
	private Product _product;
	private Warehouse _wareHouse;
	private String _productName;
	private long _startId;
	
	/**
	 * Constructor
	 * @param productName
	 * @param startId
	 * @param wareHouse
	 */
	public ProductTask(String productName, long startId, Warehouse wareHouse){
		_wareHouse=wareHouse;
		_productName=productName;
		_startId=startId;
		_product=new Product(_startId, _productName);
	}
	
	/**
	 * creates a new tasks for the parts that the product depend on.
	 * each part will get id bigger by 1 from the main product.
	 * if there is parts that this product depends on, when they will be manufactured,
	 * we will change the product finalId by performing a ToolTasks.
	 */
	public void start(){
		ArrayList<ProductTask> productTasksIDependOn=new ArrayList<ProductTask>();
		String[] parts=_wareHouse.getPlan(_product.getName()).getParts();
		for(int i=0;i<parts.length;i++){
			ProductTask productTask=new ProductTask(parts[i], _startId+1,_wareHouse);
			productTasksIDependOn.add(productTask);
			spawn(productTask);
		}
		whenResolved(productTasksIDependOn, ()->{
			for(int i=0;i<productTasksIDependOn.size();i++){
				_product.addPart(productTasksIDependOn.get(i).getResult().get());
			}
			if(productTasksIDependOn.size()>0){
				ArrayList<ToolTask> toolTasksIDependOn=new ArrayList<ToolTask>();
				String[] tools=_wareHouse.getPlan(_product.getName()).getTools();
				for(int i=0;i<tools.length;i++){
					ToolTask toolTask=new ToolTask(tools[i], _product, _wareHouse);
					toolTasksIDependOn.add(toolTask);
					spawn(toolTask);		
				}
				whenResolved(toolTasksIDependOn, ()->{
					complete(_product);
				});
			}
			else
				complete(_product);
		});
	}
}
