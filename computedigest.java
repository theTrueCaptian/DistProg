import java.security.*;
import java.io.*;
/*
 * Maeda Hanafi
 * CSC563 Multithreaded Distributed Programming
 * Computes Digest
 * */
public class computedigest{
  public static void main(String arg[])throws Exception{
    FileOutputStream out = new FileOutputStream("messagedigest.txt");//stream for which you are creating the digest for
    
    MessageDigest sha = MessageDigest.getInstance("SHA-1");
    DigestOutputStream dout = new DigestOutputStream(out, sha);
    
    FileInputStream in = new FileInputStream("document.txt");
    byte[] buffer = new byte[64]; //allows for reading 128 bytes at a time
    while(true){
      int bytesRead = in.read(buffer); //reads 
      if(bytesRead<0) break; //continue to loop if there are more bytes to read
      dout.write(buffer, 0, bytesRead); //writes to the digest stream
    }
    dout.flush();
    dout.close();
    byte[] result = dout.getMessageDigest().digest(); //finds the computation of the digest
    System.out.print(bytesToHex(result) +" ");
  } 
  
  //converts a byte array into hex
   public static String bytesToHex(byte[] b) {
      char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',
                         '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
      StringBuffer buf = new StringBuffer();
      for (int j=0; j<b.length; j++) {
         buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
         buf.append(hexDigit[b[j] & 0x0f]);
      }
      return buf.toString();
   }
  
}