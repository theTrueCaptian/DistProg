import java.util.ArrayList; // ArrayList is in java.util
import java.util.Random;
import java.util.concurrent.atomic.*;


/*
 * Maeda Hanafi
 * CSC563
 * Homework 3 Problem 1
 * Sorry for submitting this problem late. 
 * This program finally terminated when all the values are consecutive, normally after 20-30 minutes.
 * 
 */
public class ThreadCoordVers2 extends Thread{
  //Shared array, volatile is used here to enforce happens-before qualifier, and forces the shared array to be latest
  volatile int[] shared = new int[10];
  
  //Signal that indicates if the values in the shared array are consecutive
  //Coordinates action between global thread and the rest of the threads
  AtomicInteger consecutive = new AtomicInteger();
  
  //This refers to the number of writes into shared
  AtomicInteger count = new AtomicInteger();
  
  //This keeps tracks of threads...
  ArrayList<SlotThread> allThreads = new ArrayList<SlotThread>();
  
  public static void main(String arg[]){
    //This is the thread that will do the checking
    ThreadCoordVers2 global = new ThreadCoordVers2();   
    //Start this thread
    global.start();
  }
  
  public ThreadCoordVers2(){
    //Shared array initialize
    for(int j=0; j<shared.length; j++){
      shared[j] = 0;
    }    
  }
  
  //Overrides run method
  public void run(){
    boolean stopScanning = false;
    createAll();  
    consecutive.set(0);
    do{
      //command the waiting threads on consecuticity to continue work
      commandWork();      
      //Continue waiting for a thread to signal consecutivity
      waitSignal();          
      //Once global thread reaches here, it must scan the array and pause other threads  
      pauseAll();    
      //Print out content
      printShared();
      //Check if array values are all consecutive, if false, then continue work 
      stopScanning = scanArray();
      //Resume the threads that have yielded
      resumeAll();
    }while(!stopScanning);
    
    terminateAll();        
    //Print out content
    printShared();
    //Display number of writes
    System.out.println("\nNumber of writes:"+count);
  }
  
  //Method that notifies threads that are waiting for consecutive to be untrue
  public void commandWork(){
    try{
        synchronized(consecutive){
          consecutive.set(0);
          consecutive.notifyAll();
        }
      }catch(IllegalMonitorStateException e){}
  }
  
  public synchronized void printShared(){
    //Print out content of shared
    for(int t=0; t<shared.length; t++){
      System.out.print(shared[t] + " ");
    }
    System.out.println();
  }
  
  //Method that makes the global thread wait for a worker thread to signal consecutivity in its niehgborhood
  public void waitSignal(){
     synchronized(consecutive){
      while(consecutive.get()!=1){
        try{
          //System.out.println("GLOBAL WAIT ON consecutive:"+consecutive.get()+"*************************");
          consecutive.wait();
        }catch(InterruptedException ex){}
        catch(IllegalMonitorStateException e){break;}
      }      
    }
  }
  
  //Create all threads
  public void createAll(){
    allThreads = new ArrayList<SlotThread>();
    //Create  ten threads as there are array slots
    for(int i=0; i<10; i++){
      allThreads.add(new SlotThread(i)); //Add all threads to the array list
    }  

    for(int i=0; i<10; i++){
      allThreads.get(i).start(); //start thread
    }      
  }
  
  //this pauses all threads for global thread to check the array values 
  public void pauseAll(){
    for(int i=0; i<allThreads.size(); i++){  
      allThreads.get(i).yield();
      //System.out.println("thead pauses:"+i+"******************************************************************************************************");  
    }
  }
   //this pauses all threads for global thread to check the array values 
  public void resumeAll(){
    for(int i=0; i<allThreads.size(); i++){        
      allThreads.get(i).resume();
      //System.out.println("thead resumes:"+i+"******************************************************************************************************");  
      
    }
  }
  //kill all threads
  public void terminateAll(){
    //terminate threads
    for(int i=0; i<allThreads.size(); i++){        
      allThreads.get(i).stop();
      allThreads.set(i, null);
      System.out.println("thead terminates:"+i+"******************************************************************************************************");  
    }
    allThreads = new ArrayList<SlotThread>();
  }
  //This method scans if the array is all filled with consecutive values
  //Returns if true if it is consecutive and notifies waiting threads to go back to work
  public boolean scanArray(){
    boolean done = isConsecutive(0,9);
    if(!done){
    //Reset the consecutive variable to tell all threads to get back to work
      synchronized(consecutive){
        try{
          consecutive.set(0);
          consecutive.notifyAll();
        }catch(IllegalMonitorStateException e){}
      }
    }
    System.out.println("SCAN ARRAY DONE:"+done +" consecutive: "+consecutive.get());
    return done;
  }
  
  //This method checks if shared is sorted
  //Takes in the indices, from and to
  //and returns true is sorted
  public synchronized boolean isConsecutive(int from, int to){
    synchronized(shared){
      for(int i = from; i < to; i++) {
        if((shared[i+1]-shared[i])!=1){
          return false;          
        }
      }
      return true;
    }
  }
  
  //Method is used by the working threads to set consecutive to 1 and signal the waiting global thread to scan the whole array
  public void setConsecutive(){
    try{
      synchronized(consecutive){
        consecutive.set(1);
        consecutive.notify();
      }
    }catch(IllegalMonitorStateException e){}
  }
  
  //An atomic operation that working threads must access to update shared, and check for consecutivity in its neighbor
  //Making this operation atomic insures that every time an update occurs, consecutivity will always be detected.
  public synchronized boolean updateCheckSignal(int id, Random gen){
    boolean indicate =false;
    if(consecutive.get()==0){//A slot is modified only if consecutive is zero
      shared[id]=(gen.nextInt(20)+1);      
      //Print out content
      for(int t=0; t<shared.length; t++){
        System.out.print(shared[t] + " ");
      }
      System.out.println();
      //Check if the neighboring values are consecutive
      if(consecutive.get()==0){//This method is only executed when there are no consecutives; otherwise it means global thread is checking the values.
        if(id==0){//If this slot is 0
          if(isConsecutive(0,1)){
            System.out.println("consecutive at "+id+" with vals: "+shared[id]+" "+shared[id+1]);
            setConsecutive();
            indicate = true;
          }
        }else if(id==9){//if last slot 
          if(isConsecutive(8,9)){
            System.out.println("consecutive at "+id+" with vals: "+shared[id-1]+" "+shared[id]);
            setConsecutive();
            indicate = true;                  
          }
        }else if(id>0 && id<9){//all other slots are dealt here
          if(isConsecutive(id-1,id+1)){
            System.out.println("consecutive at "+id+" with vals: "+shared[id-1]+" "+shared[id]+" "+shared[id+1]);
            setConsecutive();
            indicate = true;
          }
        }
        
        count.getAndIncrement();
      }
    }
    return indicate;
    
  }
  
  //Class for the threads that randomly generate the values for an array slot
  class SlotThread extends Thread{
    int id;
    Random gen;

    public SlotThread(int id){ 
      this.id = id;
      gen = new Random(System.currentTimeMillis());
    }
    
    //Override the run method in thread
    public void run(){    
       //Thread must sleep to allow for others to have their turn
       try{this.sleep(5); }catch(InterruptedException ex){}
       //Write a random number into the designated array slot
       slotNumber();
    }
    
    public void slotNumber(){
      int loops = 0;
      boolean indicate = false;
      do{
        //Generate and write random number
        indicate = updateCheckSignal(id, gen);
        if(indicate){ //If method returns true, then pause this thread        
          //this method stops this thread and waits for global signal         
          allThreads.get(id).waitUntilNotConsecutive();
          try{
            //These sleep make neighboring threads stop for a few sec. This is important to allow a chance for the program to terminate
            if(id==0) allThreads.get(id+1).sleep(2500);
            else if(id==9) allThreads.get(id-1).sleep(2500);
            else{
              allThreads.get(id+1).sleep(2500);
              allThreads.get(id-1).sleep(2500);
            }
            this.sleep(5000); 
          }catch(InterruptedException ex){}
        }
        try{this.sleep(5); }catch(InterruptedException ex){}
      } while(true); //The thread stops only through Global thread
    
    }
    
    //This method makes this thread wait on consecutive to be zero, so that it can continue
    public void waitUntilNotConsecutive(){
      synchronized(consecutive){
        while(consecutive.get()!=0){
          try{
            //System.out.println("THREAD "+id+" WAIT conecutive ="+consecutive.get()+"************************************************");
            consecutive.wait();
          }catch(InterruptedException ex){}
          catch(IllegalMonitorStateException e){break;}
        }        
        //System.out.println("THREAD "+id+" CONTINUE************************************************");
      }
      
    }
  }
  
}