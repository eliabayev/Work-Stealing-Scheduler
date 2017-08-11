/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.sim;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import bgu.spl.a2.Deferred;
import bgu.spl.a2.WorkStealingThreadPool;
import bgu.spl.a2.sim.conf.ManufactoringPlan;
import bgu.spl.a2.sim.tasks.ProductsTask;
import bgu.spl.a2.sim.tools.GcdScrewDriver;
import bgu.spl.a2.sim.tools.NextPrimeHammer;
import bgu.spl.a2.sim.tools.RandomSumPliers;
import bgu.spl.a2.sim.tools.Tool;

/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {
	private static WorkStealingThreadPool _pool;
	private static ProductsTask[][] _waves;
	private static Warehouse _wareHouse;
	/**
	* Begin the simulation
	* Should not be called before attachWorkStealingThreadPool()
	*/
    public static ConcurrentLinkedQueue<Product> start(){
    	ConcurrentLinkedQueue<Product> finalProductQueue=new ConcurrentLinkedQueue<Product>();
    	for(int i=0;i<_waves.length;i++){
    		CountDownLatch countDown= new CountDownLatch(_waves[i].length);
    		for(int j=0;j<_waves[i].length;j++){
    			_pool.submit(_waves[i][j]);
    			Deferred<ConcurrentLinkedQueue<Product>> products=_waves[i][j].getResult();
    			products.whenResolved(()->{
    				finalProductQueue.addAll(products.get());
    				countDown.countDown();
    			});
    		}
			try{
				countDown.await();
			}
			catch(InterruptedException e){}
    	}
    	return finalProductQueue;
    }
	
	/**
	* attach a WorkStealingThreadPool to the Simulator, this WorkStealingThreadPool will be used to run the simulation
	* @param myWorkStealingThreadPool - the WorkStealingThreadPool which will be used by the simulator
	*/
	public static void attachWorkStealingThreadPool(WorkStealingThreadPool myWorkStealingThreadPool){
		_pool=myWorkStealingThreadPool;
	}
	
	/**
	 * convert jsonArray to String array
	 * @param jsonArray
	 * @return stringArray
	 */
	public static String[] convertJsonArrayToStringArray(JsonArray jsonArray){
		String[] stringArray=new String[jsonArray.size()];
		for(int i=0;i<jsonArray.size();i++)
			stringArray[i]=jsonArray.get(i).getAsString();
		return stringArray;
	}
	

	/**
	 * read from json
	 * add tools to the warehouse
	 * @param initialTools
	 */
	public static void initialToolsFunc(JsonArray initialTools){
		for(int i=0;i<initialTools.size();i++){
			JsonObject currentJsonTool=(JsonObject)initialTools.get(i); //get JsonObject from JSONArray
			int currentToolQuantity=currentJsonTool.get("qty").getAsInt();
			String currentToolName=currentJsonTool.get("tool").getAsString(); //read JsonObject
			Tool currentTool = null;
			switch(currentToolName){
				case "rs-pliers" : currentTool=new RandomSumPliers(); break;
				case "gs-driver" : currentTool=new GcdScrewDriver(); break;
				case "np-hammer" : currentTool=new NextPrimeHammer(); break;
			}
			_wareHouse.addTool(currentTool, currentToolQuantity);	
		}
	}
	
	/**
	 * read from json
	 * add plans to the warehouse
	 * @param initialPlans
	 */
	public static void initialPlansFunc(JsonArray initialPlans){
		for(int i=0;i<initialPlans.size();i++){
			JsonObject currentJsonPlan=initialPlans.get(i).getAsJsonObject();
			String currentPlanProductName=currentJsonPlan.get("product").getAsString();
			String[] currentPlanToolsNames=convertJsonArrayToStringArray(currentJsonPlan.get("tools").getAsJsonArray());	
			String[] currentPlanPartsNames=convertJsonArrayToStringArray(currentJsonPlan.get("parts").getAsJsonArray());
			_wareHouse.addPlan(new ManufactoringPlan(currentPlanProductName, currentPlanPartsNames, 
					currentPlanToolsNames));
		}
	}

	/**
	 * read from json
	 * create a tasks array according to the json file
	 * @param initialWaves
	 */
	public static void initialWavesFunc(JsonArray initialWaves){
		_waves=new ProductsTask[initialWaves.size()][];
		AtomicInteger index=new AtomicInteger();
		for(int i=0;i<initialWaves.size();i++){
			JsonArray currentJsonWave=initialWaves.get(i).getAsJsonArray();
			ProductsTask[] wave=new ProductsTask[currentJsonWave.size()];
			for(int j=0;j<currentJsonWave.size();j++){
				JsonObject currentJsonWaveTask=currentJsonWave.get(j).getAsJsonObject();
				String currentWaveProductName=currentJsonWaveTask.get("product").getAsString();  
				int currentWaveProductQuantity=currentJsonWaveTask.get("qty").getAsInt(); 
				long currentWaveProductStartId=currentJsonWaveTask.get("startId").getAsLong(); 
				ProductsTask productsTask=new ProductsTask(currentWaveProductName, currentWaveProductStartId,
						currentWaveProductQuantity, _wareHouse, index);
				index.set(index.get()+currentWaveProductQuantity);
				wave[j]=productsTask;
			}
			_waves[i]=wave;
		}
	}
	/**
	 * read the whole json file using the suitable functions, and parse it. 
	 * perform all the instructions on the json file.
	 * starts the pool.
	 * sorts the final products queue.
	 * shutdown the pool at the end of the manufactoring.
	 * @param args - a json file that contains product that should be conducted.
	 */
	public static int main(String [] args){
    	int nthreads = 0;
    	_wareHouse = new Warehouse();
    	try{
    		//String filepath="/users/studs/bsc/2016/snirka/A2 Help/in2.json";
    		//FileReader fileReader=new FileReader(filepath);
    		JsonParser jsonParser=new JsonParser();
    		Object jsonObject=jsonParser.parse(new FileReader(args[0]));
    		//Object jsonObject=jsonParser.parse(fileReader);
    		JsonObject factory=(JsonObject)jsonObject;
    		
    		nthreads=factory.get("threads").getAsInt();
    		attachWorkStealingThreadPool(new WorkStealingThreadPool(nthreads));
    		
    		JsonArray initialTools=factory.get("tools").getAsJsonArray();
    		initialToolsFunc(initialTools);
    		JsonArray initialPlans=factory.get("plans").getAsJsonArray();
    		initialPlansFunc(initialPlans);
    		JsonArray initialWaves=factory.get("waves").getAsJsonArray();
    		initialWavesFunc(initialWaves);
    		
    		_pool.start();
    		
    		ConcurrentLinkedQueue<Product>  finalProductQueue=start();
    		Product[] sortedFinalProductArray=new Product[finalProductQueue.size()];
    		
    		for(int i=0;i<sortedFinalProductArray.length;i++){
    			Product p=finalProductQueue.poll();
    			int index=p.getIndex().intValue();
    			sortedFinalProductArray[index]=p;
    		}
    		
    		ConcurrentLinkedQueue<Product> SimulationResult=new ConcurrentLinkedQueue<Product>();
    		for(int i=0;i<sortedFinalProductArray.length;i++){
    			SimulationResult.add(sortedFinalProductArray[i]);
    		}

    		FileOutputStream fout=new FileOutputStream("result.ser");
    		ObjectOutputStream oos=new ObjectOutputStream(fout);
    		oos.writeObject(SimulationResult);
    		oos.close();

    		_pool.shutdown();
    	}
    	catch(FileNotFoundException e) {
    		e.printStackTrace();
    	} 
    	catch (IOException e) {
    		e.printStackTrace();
    	} 
    	catch (InterruptedException e) {
    		e.printStackTrace();
    	} 
    	return 0;
	}
}
