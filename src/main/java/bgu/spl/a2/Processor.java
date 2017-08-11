package bgu.spl.a2;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * this class represents a single work stealing processor, it is
 * {@link Runnable} so it is suitable to be executed by threads.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 *
 */
public class Processor implements Runnable {

    private final WorkStealingThreadPool pool;
    private final int id;

    /**
     * constructor for this class
     *
     * IMPORTANT:
     * 1) this method is package protected, i.e., only classes inside
     * the same package can access it - you should *not* change it to
     * public/private/protected
     *
     * 2) you may not add other constructors to this class
     * nor you allowed to add any other parameter to this constructor - changing
     * this may cause automatic tests to fail..
     *
     * @param id - the processor id (every processor need to have its own unique
     * id inside its thread pool)
     * @param pool - the thread pool which owns this processor
     */
    /*package*/ Processor(int id, WorkStealingThreadPool pool) {
        this.id = id;
        this.pool = pool;
    }
	/**
	* runs as long as the thread that running this class is not interrupted
	* while there is a tasks in the current processor queue, remove them from the processor queue and handle them
	* if the queue is empty, steal tasks from another processor.
	*/
    @Override
    public void run() {
    	ConcurrentLinkedDeque<Task<?>> myQueue=pool.getQueueById(id);
    	int oldVersion=pool.getVersionMonitor().getVersion();
    	boolean interrupted=false;
    	while(!interrupted){// checks if there are tasks on this processor queue of tasks
    		if(Thread.currentThread().isInterrupted()){
    			interrupted=true;
    		}
	    	while(!myQueue.isEmpty()){// executing tasks as long as the queue is no empty
	    		oldVersion=pool.getVersionMonitor().getVersion();
	    		Task<?> task=myQueue.pollLast();
	    		if(task!=null){
	    			task.handle(this);
//	    			System.out.println("good");
	    		}
	    		else
	    			break;
	    	}
	    	
	    	pool.steal(id, oldVersion);
    	}
    }
    
    /**
     * adding a task to this processor through pool
     * @param task - the task to be added
     */
    /*package*/ void addTask(Task<?> task){
    	pool.addToTheSameProcessor(task, id);
    }
    
    /**
     * @return a referece to the WorkStealingThreadPool which hold this processor
     */
    /*package*/ WorkStealingThreadPool getPool(){
    	return pool;
    }
    
}
