package bgu.spl.a2.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class that represents a product produced during the simulation.
 */
public class Product implements java.io.Serializable {
	private long _startId;
	private long _finalId;
	private AtomicInteger _index;
	private String _name;
	private List<Product> _partsList;
	
	/**
	* Constructor 
	* @param startId - Product start id
	* @param name - Product name
	*/
    public Product(long startId, String name){
    	_index=new AtomicInteger();
    	_name=name;
    	_startId=startId;
    	_finalId=startId;
    	_partsList=new ArrayList<Product>();
    }

	/**
	* @return The product name as a string
	*/
    public String getName(){
    	return _name;
    }
    
    /**
     * @param index - replace the field _index with the value of index
     * a variable which helps to keep track of the order of this product on the manufacturing line.
     */
    public void setIndex(int index){
    	_index.set(index);
    }
    
    /**
     * @return the product index.
     * a variable which helps to keep track of the order of this product on the manufacturing line.
     */
    public AtomicInteger getIndex(){
    	return _index;
    }

	/**
	* @return The product start ID as a long. start ID should never be changed.
	*/
    public long getStartId(){
    	return _startId;
    }
    
	/**
	* @return The product final ID as a long. 
	* final ID is the ID the product received as the sum of all UseOn(); 
	*/
    public long getFinalId(){
    	return _finalId;
    }
    
    /**
     * 
     * @param finalId - set the finalId of this product to be the value of finalId
     */
    public void setFinalId(long finalId){
    	_finalId+=finalId;
    }

	/**
	* @return Returns all parts of this product as a List of Products
	*/
    public List<Product> getParts(){
    	return _partsList;
    }

	/**
	* Add a new part to the product
	* @param p - part to be added as a Product object
	*/
    public void addPart(Product p){
    	_partsList.add(p);
    }
}
