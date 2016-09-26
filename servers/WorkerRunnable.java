package servers;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.*;
import java.util.*;
import java.io.*;


public class WorkerRunnable implements Runnable{

    protected Socket clientSocket = null;
    protected String serverText   = null;
    final static String CRLF = "\r\n";//For convenience

    public WorkerRunnable(Socket clientSocket, String serverText)
    {      
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }

    private static String contentType(String fileName)
    {
       if(fileName.endsWith(".htm") ||fileName.endsWith(".html"))
        return "text/html";
       if(fileName.endsWith(".jpg"))
        return "image/jpg";
       if(fileName.endsWith(".gif"))
        return "image/gif";
       return "application/octet-stream";
    }
    
    // It sends data from file to output stream 
    private static void sendBytes(FileInputStream fis, OutputStream os)
    {
       //create a buffer to hold the bytes on the way to socket
       byte[] buffer = new byte[1024];
       int bytes = 0;
     
      
       try{
         while((bytes = fis.read(buffer)) != -1 ) {
          os.write(buffer, 0, bytes);
         }
       } catch (IOException ie){
       }
    }
          

 public void run()  
 {
   try{
        InputStream input  = clientSocket.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
            
        String requestLine = br.readLine();
               
        System.out.println(); 
        System.out.println(requestLine);
               
        InetAddress ipAddress = clientSocket.getInetAddress();
        String ipStr= ipAddress.getHostAddress();
        System.out.println("The incoming address is:  " + ipStr);
              
        StringTokenizer tokens = new   StringTokenizer(requestLine);
        tokens.nextToken(); 
        String fName = tokens.nextToken();
           
        fName = "." + fName;
               
         String headerLine = null;
         while ((headerLine = br.readLine()).length() != 0) { 
                System.out.println(headerLine);
            }
           // Open the requested file.
           FileInputStream fis = null;
           boolean fileExists = true;
           try {
             fis = new FileInputStream(fName);
            } catch (FileNotFoundException e) {
               fileExists = false;
           }   
           
           //Constructing the response message
           String statusLine = null; 
           String contentTypeLine = null;
           String entityBody = null;
           if (fileExists) {
             statusLine = "HTTP/1.1 200 OK: ";
             contentTypeLine = "Content-Type: " +
             contentType(fName) + CRLF;
           } else {
             statusLine = "HTTP/1.1 404 Not Found: ";
             contentTypeLine = "Content-Type: text/html" + CRLF;
             entityBody = "<HTML> <HEAD><TITLE>Not Found</TITLE></HEAD> <BODY>File not Found on this Multithreaded WebServer</BODY></HTML>";
           }
          
           
           DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
           
           output.writeBytes(statusLine);
                    
           output.writeBytes(contentTypeLine);
         
           // Send a blank line to output indicate the end of the header lines.
           output.writeBytes(CRLF);
           
           // Send the response body.
           if (fileExists) {
            sendBytes(fis, output);
            fis.close();
           } else {
            output.writeBytes(entityBody);
           }
           //closing the streams
           output.close();
           input.close();
           System.out.println("\n\nRequest processed: " + fName + "\n\n");
        } catch (IOException e) {
              e.printStackTrace();
        }
    }
}
