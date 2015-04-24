import java.util.zip.*;
import java.io.*;
/*
 * Maeda Hanafi
 * CSC536
 * Decompressor
 * */
public class decompressor{
  public static void main(String arg[]) throws FileNotFoundException, IOException{
    FileInputStream fin = new FileInputStream("archive.zip"); //creates FileInputStream the zip
    ZipInputStream zin = new ZipInputStream(fin); //Then ZipInputStream wraps around the FileInputStream
    ZipEntry ze= null;   //this is for the file(document.txt) insize archive.zip
    int b=0;
    while((ze=zin.getNextEntry())!=null){ //extracts entry one by one
      FileOutputStream fout = new FileOutputStream(ze.getName());   //this is where we are decompressing to
      while((b=zin.read())!= -1){ //this inner loop extracts each bit
        fout.write(b);
      }
      zin.closeEntry();
      fout.flush();
      fout.close();
    }
  }
  
  
}