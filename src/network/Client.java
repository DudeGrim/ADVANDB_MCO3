package network;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client extends Thread{
	String name;
	String ipadd;
	final int PORT = 1996;
    InputStreamReader input;
    BufferedReader inFromServer;
    
    String message = "initial";
    Socket socket;
    
    DataOutputStream out;
    DataInputStream in;
    
    
    
    public void run() {
       try {
           socket = new Socket(ipadd, PORT);
           
           in = new DataInputStream(socket.getInputStream());
           out = new DataOutputStream(socket.getOutputStream());

          // ip =socket.getInetAddress().getHostAddress();
           
  
       } catch (ConnectException connectException) {
           System.out.println(connectException.getMessage());
       } catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

       
   }
    

   
}


