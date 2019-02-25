package genesys;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client{

	public static void main(String args[]) throws UnknownHostException, IOException{
		
		Socket s = new Socket("127.0.0.1", 9999);
		DataInputStream dis = new DataInputStream(s.getInputStream()); 
        DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));


        String clientMessage ="", serverMessage = "";
 
        while(!serverMessage.equals("GG")) {
        	// reads from the server
        	serverMessage=dis.readUTF();
			System.out.println(serverMessage);
			while(!serverMessage.contains("turn")){
				// reads from the server
				serverMessage=dis.readUTF();
				System.out.println(serverMessage);
			}
			// writes to the server
			clientMessage=br.readLine();
			dos.writeUTF(clientMessage);
			dos.flush();
        }
    }
  
}
