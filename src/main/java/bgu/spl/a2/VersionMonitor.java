package bgu.spl.a2;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Describes a monitor that supports the concept of versioning - its idea is
 * simple, the monitor has a version number which you can receive via the method
 * {@link #getVersion()} once you have a version number, you can call
 * {@link #await(int)} with this version number in order to wait until this
 * version number changes.
 *
 * you can also increment the version number by one using the {@link #inc()}
 * method.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class VersionMonitor {
	private AtomicInteger _version=new AtomicInteger();
	
	/**
	 * @return the current value of this monitor's version
	 */
    public int getVersion() {
        return _version.intValue();
    }

    /**
     * This method is synchronized because only one thread should increase this monitor's
     * version at any time. A case when thread 1 is right before the wait action, after he passed the
     * while condition. then there is a context switch and thread 2 is getting
     * CPU time and invoking the {@link #inc()} method. then thread 1 is back into action
     * and because he passed the while condition he will get into wait mode, despite notifyAll() has been called
     * by thread 2, even though he should continue running.
     * increase by one the value of this monitor's version.
     */
    public synchronized void inc() {
    	_version.incrementAndGet();
    	notifyAll();
    }

    /**
     * This method is synchronized because wait action should always be on a lock. in this situation,
     * the thread who is going to wait, want to wait on the VesionMonitor lock, because he would 
     * want that another thread will wake him by the inc() action.
     * another reason is the same as explained on {@link #inc()} javadoc.
     * in this case our lock is this object.
     * @param vesrion, the version which could be the current version or a newer one
     * @throws InterruptedException if this thread is already in wait state and wait()
     * action is invoked
     */
    public synchronized void await(int version) throws InterruptedException {
    	while(_version.intValue()==version)
    		wait();
    }
}
