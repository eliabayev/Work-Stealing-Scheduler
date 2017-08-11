package bgu.spl.a2;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * represents a work stealing thread pool - to understand what this class does
 * please refer to your assignment.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class WorkStealingThreadPool {
	private final int _nthreads;
	private Processor[] _processors;
	private Thread[] _threads;
	private ArrayList<ConcurrentLinkedDeque<Task<?>>> _tasks;
	private VersionMonitor _versionMonitor;

    /**
     * creates a {@link WorkStealingThreadPool} which has nthreads
     * {@link Processor}s. Note, threads should not get started until calling to
     * the {@link #start()} method.
     *
     * Implementors note: you may not add other constructors to this class nor
     * you allowed to add any other parameter to this constructor - changing
     * this may cause automatic tests to fail..
     *
     * @param nthreads the number of threads that should be started by this
     * thread pool
     */
    public WorkStealingThreadPool(int nthreads) {
    	_nthreads=nthreads;
    	_processors=new Processor[_nthreads];
    	_threads=new Thread[_nthreads];
    	_tasks=new ArrayList<ConcurrentLinkedDeque<Task<?>>> (_nthreads);
    	_versionMonitor=new VersionMonitor();
    	
    	for(int i=0;i<_nthreads;i++){
    		_processors[i]=new Processor(i, this);
    		_threads[i]=new Thread(_processors[i]);
    		_tasks.add(new ConcurrentLinkedDeque<Task<?>>());
    	}
    }

    /**
     * submits a task to be executed by a processor belongs to this thread pool
     *
     * @param task the task to execute
     */
    public void submit(Task<?> task) {
    	int random=(int)((Math.random())*(_nthreads));    	
    	_tasks.get(random).addFirst(task);
    	_versionMonitor.inc();
    }

    /**
     * closes the thread pool - this method interrupts all the threads and wait
     * for them to stop - it is returns *only* when there are no live threads in
     * the queue.
     *
     * after calling this method - one should not use the queue anymore.
     *
     * @throws InterruptedException if the thread that shut down the threads is
     * interrupted
     * @throws UnsupportedOperationException if the thread that attempts to
     * shutdown the queue is itself a processor of this queue
     */
    public void shutdown() throws InterruptedException {
    	if(Thread.currentThread().isInterrupted())//added need to check
    		throw new InterruptedException();
    	for(int i=0; i<_threads.length;i++){
    		if(Thread.currentThread()!=_threads[i])// checks if the thread isn't Interrupting itself
    			_threads[i].interrupt();
    		else
    			throw new UnsupportedOperationException("This Thread is trying to shut down itself");
    	}
    	for(int i=0; i<_nthreads;i++){
    		_versionMonitor.inc();
    		_threads[i].join();
    	}
    }

    /**
     * start the threads that belongs to this thread pool
     */
    public void start() {
    	for(int i=0; i<+_threads.length;i++){
    		_threads[i].start();    		
    	}
    }
    
    /**
     * @param task, the task to be added
     * @param id, the id of the processor which the task will be added
     */
    /*package*/ void addToTheSameProcessor(Task<?> task, int id){ // add a task to to the processor with certain id number
    	_tasks.get(id).addFirst(task);
    	_versionMonitor.inc();
    }
    
    /**
     * 
     * @param id, an index of a processor
     * @return the ConcurrentLinkedDeque<Task<?>> of the id's processor
     */
    /*package*/ ConcurrentLinkedDeque<Task<?>> getQueueById(int id){
    	return _tasks.get(id);
    }
    
    /**
     * @return the current version monitor
     */
    /*package*/ VersionMonitor getVersionMonitor(){
    	return _versionMonitor;
    }
    
    /**
     * this method is invoked when processor has no tasks. the processor will attempt to
     * get tasks from another processor, and wait if there is no tasks to steal.
     * @param id, the id of the processor which will perform the the steal action
     * @param oldVersion, the version before the steal action has started
     */
    /*package*/ void steal(int id, int oldVersion){
    	AtomicInteger nextVictim=new AtomicInteger();
    	if(id==_nthreads-1)//out of boundaries
    		nextVictim.set(0);
    	else
    		nextVictim.set(id+1);
		ConcurrentLinkedDeque<Task<?>> myQueue=_tasks.get(id);//Need to check: is this is a reference to my Queue?
		ConcurrentLinkedDeque<Task<?>> victimQueue;
		int victimQueueSize;
		boolean interrupted=false;
		
	    while((!interrupted)&&(myQueue.isEmpty())){
	    	if(Thread.currentThread().isInterrupted())
	    		interrupted=true;
	    	victimQueue=_tasks.get(nextVictim.intValue());
		    victimQueueSize=victimQueue.size();
		    if(victimQueueSize>1){//can i steal?
		    	AtomicInteger TaskThatStealed=new AtomicInteger();// count the number of tasks that will be stealed
		    	while((!victimQueue.isEmpty())&&(TaskThatStealed.intValue()<victimQueueSize/2)){
		    			Task<?> task=victimQueue.pollLast();
		    			if(task!=null)
		    				myQueue.addFirst(task);//the stealing action
		    			else
		    				break;
		    		if(myQueue.size()>1)
		    			_versionMonitor.inc();
		    		TaskThatStealed.incrementAndGet();
		    	}
		    	return;
		    }
		    else
	    		if(nextVictim.intValue()+1==_nthreads)//out of boundaries
	    			nextVictim.set(0);
	    		else
	    			nextVictim.incrementAndGet();
		    
		    	if(nextVictim.intValue()==id){//completed a full circle
		    		if(id==_nthreads-1)
		    			nextVictim.set(0);
		    		else
		    			nextVictim.incrementAndGet();
		    		try{// go to sleep because there is no one to steal from
		    			_versionMonitor.await(oldVersion);
		    			oldVersion=_versionMonitor.getVersion();
		    		} catch(InterruptedException e){
		    			Thread.currentThread().interrupt();;
		    			}
	    	}
	    }
    }
}
