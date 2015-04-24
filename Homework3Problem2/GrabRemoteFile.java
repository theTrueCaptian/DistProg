 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import javax.swing.*;
 /*
  * 
  * CSC 563
  * Maeda Hanafi
  * Homework 3 Problem 2: Grab remote file and parse it with executor
  **/
 public class GrabRemoteFile {
       
   //this program takes in arguments in the following format: <URL> <a search string>
        public static void main(String[] arg) {
          if(arg.length!=2){
            System.out.println("argument format must be: <url> <a search string>");
          }else{
            executor(arg[0], arg[1]);
          }
        }
      
        private static void executor(String URL, String search){
          grabFile(URL);
          
        }
        private static void grabFile(String URLSTRING) {
          // Declare buffered stream for reading text for the URL
          BufferedReader infile = null;
          URL url = null;
          String result="";
          String status = "";
          try {
            // Obtain URL from the text field
            url = new URL(URLSTRING);
    
           // Create a buffered stream
            InputStream is = url.openStream();
            infile = new BufferedReader(new InputStreamReader(is));
    
            // Get file name from the text field
            String inLine;
    
            // Read a line and append the line to the text area
            while ((inLine = infile.readLine()) != null) {
              result=result+(inLine + '\n');
            }
    
            status = ("File loaded successfully");
          }catch (FileNotFoundException e) {
            status = ("URL " + url + " not found.");
          }catch (IOException e) {
            status = e.getMessage(); 
          }finally {
            try {
              if (infile != null) infile.close();
            }
            catch (IOException ex) {}
          }
          System.out.println(status);
          System.out.println(result);
        }
  }


