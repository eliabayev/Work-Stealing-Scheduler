/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl.a2.test;

import bgu.spl.a2.Task;
import bgu.spl.a2.WorkStealingThreadPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class MergeSort extends Task<int[]> {
    private final int[] array;

    /**
     * Constructor
     * @param array
     */
    public MergeSort(int[] array) {
        this.array = array;
    }

    /**
     * sorts the array using merge sort algorithm and using multi-threading and tasks. 
     */
    @Override
    protected void start() {
    		sort(array);
    }
    
    /**
     * if the arr is null, or his size is <2, the task is completed
     * else, the array will be splited to 2 parts, and a task for sorting each for those parts will be created
     * when all of those tasks will finish, merge between those arrays.
     * @param arr - this array will be sorted
     */
    private void sort(int[] arr){
		if (arr==null || arr.length<2)
			complete(arr);
		else{
			ArrayList<Task<int[]>> tasks = new ArrayList<Task<int[]>>();
			MergeSort m1=new MergeSort(splitLeft(arr));
			MergeSort m2=new MergeSort(splitRight(arr));
			spawn(m1, m2);
			tasks.add(m1);
			tasks.add(m2);
			whenResolved(tasks,()->{
				complete(merge(tasks.get(0).getResult().get(),tasks.get(1).getResult().get()));
			});			
		}
	}

    /**
     * 
     * @param arr - an array
     * @return new array that contains the left side of arr
     */
	private int[] splitLeft(int[] arr){
	    int[] ans = new int[arr.length/2];
	    for (int i=0; i<arr.length/2; i=i+1)
	        ans[i]=arr[i];
	    return ans;
	}
	
	/**
     * 
     * @param arr - an array
     * @return new array that contains the right side of arr
     */
	private int[] splitRight(int[] arr){
	    int[] ans = new int[arr.length-arr.length/2];
	    for (int i=arr.length/2; i<arr.length; i=i+1)
	        ans[i-arr.length/2]=arr[i];
	    return ans;
	}
	
	/**
	 * 
	 * @param arr1
	 * @param arr2
	 * @return array that is the merge of arr1 and arr2
	 */
	private int[] merge(int[] arr1, int[] arr2) {
		int ind = 0, i1=0, i2=0;
		int len1 = arr1.length, len2=arr2.length;
		int[] ans = new int[len1+len2];
		while(i1<len1 & i2 < len2) {	
			if(arr1[i1] <arr2[i2]) {
				ans[ind] = arr1[i1]; 
				i1=i1+1; 
			}	
			else {	
				ans[ind] = arr2[i2]; 
				i2=i2+1; 
			}	
			ind=ind+1;
		}
		for(int i=i1;i<len1;i=i+1) {
			ans[ind] = arr1[i]; 
			ind=ind+1;
		}	
		for(int i=i2;i<len2;i=i+1) {
			ans[ind] = arr2[i]; 	
			ind=ind+1;
		}		
		return ans;
	}

    public static void main(String[] args) throws InterruptedException {
        WorkStealingThreadPool pool = new WorkStealingThreadPool(4);
        int n = 1000000; //you may check on different number of elements if you like
        int[] array = new Random().ints(n).toArray();

        MergeSort task = new MergeSort(array);

        CountDownLatch l = new CountDownLatch(1);
        pool.start();
        pool.submit(task);
        task.getResult().whenResolved(() -> {
            //warning - a large print!! - you can remove this line if you wish
            System.out.println(Arrays.toString(task.getResult().get()));
            l.countDown();
        });
        
        l.await();
        pool.shutdown();
    }

}
