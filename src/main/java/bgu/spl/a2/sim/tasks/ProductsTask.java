package bgu.spl.a2.sim.tasks;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.a2.Task;
import bgu.spl.a2.sim.Product;
import bgu.spl.a2.sim.Warehouse;

public class ProductsTask extends Task<ConcurrentLinkedQueue<Product>> {
	private String _productName;	
	private long _startId;
	private int _quantity;
	private Warehouse _wareHouse;
	private ConcurrentLinkedQueue<Product> _products;
	private AtomicInteger _index;
	
	/**
	 * Constructor
	 * @param productName
	 * @param startId
	 * @param quantity
	 * @param wareHouse
	 * @param index
	 */
	public ProductsTask(String productName, long startId, int quantity, Warehouse wareHouse, AtomicInteger index){
		_products=new ConcurrentLinkedQueue<Product>();
		_productName=productName;
		_startId=startId;
		_quantity=quantity;
		_wareHouse=wareHouse;
		_index=new AtomicInteger();
		_index.set(index.get());
	}
	
	/**
	 * 
	 * @return the name of the product that we want to manufacture
	 */
	public String getProductName(){
		return _productName;
	}
	
	/**
	 * 
	 * @return the id of the product that we want to manufacture
	 */
	public long getStartId(){
		return _startId;
	}
	
	/**
	 * 
	 * @return the quantity of the products that we want to manufacture
	 */
	public int getQuantity(){
		return _quantity;
	}
	
	/**
	 * creates a new tasks of the same product according to the quantity needed.
	 * each task has different id (which raises by 1 for every product manufactured)
	 * when all the products manufactured, this task is completed. 
	 */
	protected void start(){
		ArrayList<ProductTask> tasksIDependOn=new ArrayList<ProductTask>();
		for(int i=0;i<_quantity;i++){
			ProductTask productTask=new ProductTask(_productName, _startId+i, _wareHouse);
			tasksIDependOn.add(productTask);
			spawn(productTask);
		}
		whenResolved(tasksIDependOn,()->{
			for(int i=0;i<tasksIDependOn.size();i++){
				tasksIDependOn.get(i).getResult().get().setIndex(_index.intValue());
				_index.incrementAndGet();
				_products.add(tasksIDependOn.get(i).getResult().get());
			}
			complete(_products);
		});
		
	}
}
