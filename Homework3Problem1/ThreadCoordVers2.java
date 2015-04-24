import java.util.ArrayList; // ArrayList is in java.util
import java.util.Random;
import java.util.concurrent.atomic.*;

/*
 * Maeda Hanafi
 * CSC563
 * Homework 3 Problem 1
 * 
 */
public class ThreadCoordVers2 extends Thread{
  //shared array
  int[] shared = new int[10];
  //array that indicates if the values in the shared array are consecutive
  AtomicInteger consecutive = new AtomicInteger();
  AtomicInteger count = new AtomicInteger();
  
  public static void main(String arg[]){
    //creates this object
    //this is the thread that will do the checking
    ThreadCoordVers2 global = new ThreadCoordVers2();   
    // start this thread
    global.start();
  }
  
  public ThreadCoordVers2(){
    //shared array initialize
    for(int j=0; j<shared.length; j++){
      shared[j] = 0;
    }    
  }
  
  //overrides run method
  public void run(){
    //this keeps tracks of threads...
    ArrayList<SlotThread> allThreads = new ArrayList<SlotThread>();
    
         
    //create tht ten threads as there are array slots
    for(int i=0; i<10; i++){
      allThreads.add(new SlotThread(i)); //add all threads to the array list
      allThreads.get(i).start(); //start thread
    }  
    /*for(int i=0; i<allThreads.size(); i++){
      try{
        //wait till all threads to terminate before printing out contents of array
        allThreads.get(i).join();
      }catch(InterruptedException ex){System.out.println("INTERUPt!");}
    }*/
    
   //continue waiting
    synchronized(consecutive){
      while(consecutive.get()!=1){
        try{
          consecutive.wait();
        }catch(InterruptedException ex){}
        catch(IllegalMonitorStateException e){break;}
      }
    }
    //terminate threads
    for(int i=0; i<allThreads.size(); i++){        
        allThreads.get(i).stop();
        allThreads.set(i, null);
        System.out.println("thead terminates:"+i+"******************************************************************************************************");  
      
    }
    
    //print out content
    for(int t=0; t<shared.length; t++){
      System.out.print(shared[t] + " ");
      if(t%10==0) System.out.println();
    }
    System.out.println();
    //display number of writes
    System.out.println("\nNumber of writes:"+count);
  }
  
  //class for the threads that randomly generate the values for an array slot
  class SlotThread extends Thread{
    int id;
    Random gen;
    public SlotThread(int id){ 
      this.id = id;
      gen = new Random(id);
    }
    //override the run method in thread
    public void run(){
       System.out.println("thread started:"+id);
       //thread must sleep to allow for others to have their turn
       try{this.sleep(5); }catch(InterruptedException ex){}
       //write a random number into the designated array slot
       slotNumber();
    }
    
    public void slotNumber(){
      int loops = 0;
      do{
        //generate random number
            loops++;
            synchronized(shared){
              shared[id] = gen.nextInt(20)+1; 
            }
              System.out.println("shared["+id+"]="+shared[id]);
              count.getAndIncrement();
              //indicate global thread
              
              try{this.sleep(5); }catch(InterruptedException ex){}
      } while(!indicate());
    
    }
    
    public boolean indicate(){
      //check if the neighboring values are consecutive
            if(id==0){//if this slot is 0
              //synchronized(shared){
                if(shared[id+1]-shared[id]==1){
                  System.out.println("consecutive at "+id+" with vals: "+shared[id+1]+" "+shared[id]);
                  setConsecutive();
                  return true;
                }
              //}
            }else if(id==9){//if last slot 
              //synchronized(shared){
                if(shared[id]-shared[id-1]==1){
                  System.out.println("consecutive at "+id+" with vals: "+shared[id-1]+" "+shared[id]);
                  setConsecutive();
                  return true;                  
                }
              //}
            }else if(id>0 && id<9){//all other slots are dealt here
              //synchronized(shared){
                if(shared[id+1]-shared[id]==1 && shared[id]-shared[id-1]==1){
                  System.out.println("consecutive at "+id+" with vals: "+shared[id+1]+" "+shared[id]);
                  setConsecutive();
                  return true;
                }
              //}
            }
            return false;
    }
    public void setConsecutive(){
      try{
        synchronized(consecutive){
          consecutive.set(1);
          consecutive.notify();
        }
      }catch(IllegalMonitorStateException e){}
    }
   
  }
  
}