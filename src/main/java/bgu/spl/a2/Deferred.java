package bgu.spl.a2;
import java.util.ArrayList;

/**
 * this class represents a deferred result i.e., an object that eventually will
 * be resolved to hold a result of some operation, the class allows for getting
 * the result once it is available and registering a callback that will be
 * called once the result is available.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 *
 * @param <T> the result type
 */
public class Deferred<T> {
	private T _result=null;
	private boolean _resolved=false;
	private ArrayList<Runnable> _callsList=new ArrayList<Runnable>();
	private boolean _startedAlready=false;
	
    /**
     *
     * @return the resolved value if such exists (i.e., if this object has been
     * {@link #resolve(java.lang.Object)}ed yet
     * @throws IllegalStateException in the case where this method is called and
     * this object is not yet resolved
     */
    public T get() {
        if(isResolved())
        	return _result;
        throw new IllegalStateException("This object is not yet resolved");
    }

    /**
     *
     * @return true if this object has been resolved - i.e., if the method
     * {@link #resolve(java.lang.Object)} has been called on this object before.
     */
    public boolean isResolved() {
    	return _resolved;
    }

    /**
     * resolve this deferred object - from now on, any call to the method
     * {@link #get()} should return the given value
     *
     * Any callbacks that were registered to be notified when this object is
     * resolved via the {@link #whenResolved(java.lang.Runnable)} method should
     * be executed before this method returns
     *
     * This method is synchronized because resolving this object should only get
     * one value to resolve this object. A case when this function is called twice
     * or more in parallel will cause this object _result variable to be set more 
     * than once, and this is a contradiction
     *
     * @param value - the value to resolve this deferred object with
     * @throws IllegalStateException in the case where this object is already
     * resolved
     */
    public synchronized void resolve(T value) {
    	if(_resolved)
    		throw new IllegalStateException("This object is already resolved");    		
    	_result = value;
    	_resolved=true;
    	for(int i=0; i<_callsList.size();i++)// active all the callbacks on _callsList because this object is resolve
    		_callsList.get(i).run();
    	_callsList.clear();//clear the _callsList from all the callbacks     
    }

    /**
     * add a callback to be called when this object is resolved. if while
     * calling this method the object is already resolved - the callback should
     * be called immediately
     *
     * Note that in any case, the given callback should never get called more
     * than once, in addition, in order to avoid memory leaks - once the
     * callback got called, this object should not hold its reference any
     * longer.
     *
     * This method is synchronized because there is a case when this object is
     * not yet resolved, one thread is passing the if condition and then the scheduler stops
     * him, and giving CPU time to another thread. the other thread might resolve the 
     * object and operate the callback list, while the first thread is holding a callback, that should be called,
     * and never will. 
     * 
     * @param callback the callback to be called when the deferred object is
     * resolved
     */
    public synchronized void whenResolved(Runnable callback) {
        if(!_resolved)
        	_callsList.add(callback);
        else
        	callback.run();
    }
    
    /**
     * @return boolean, true if this object has started already or not if it has'nt  
     */
    /*package*/ boolean getStartedAlready(){
    	return _startedAlready;
    }
    
    /**
     * change the state of this object to being started already
     */
    /*package*/void setStartedAlready(){
    	_startedAlready=true;
    }

}
