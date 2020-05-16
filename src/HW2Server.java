import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

public class HW2Server {
	public static void main(String[] args) throws IOException {

		if (args.length != 1) {
			System.err.println("Usage: java EchoServer <port number>");
			System.exit(1);
		}

		int portNumber = Integer.parseInt(args[0]);

		try (ServerSocket serverSocket = new ServerSocket(portNumber);
				DatagramSocket udpSocket = new DatagramSocket(portNumber); // incoming socket 9999

		) {

			while (true) {
				Socket clientSocket = serverSocket.accept(); // For TCP
				Socket clientSocket2 = serverSocket.accept(); // For retransmission
				System.out.println("TCP Client Connected");
				byte[] buffer = new byte[1024];
				DatagramPacket init = new DatagramPacket(buffer, buffer.length);
				udpSocket.receive(init); // To obtain UDP Port
				System.out.println("UDP Connected to:" + init.getPort()); // .getPort()->gives the port number
				// Create 2 thread so that TCP and UDP run concurrently
				ClientWorker w = new ClientWorker(clientSocket);
				Thread t = new Thread(w);
				t.start(); // thread0 to execute tcp
				ClientWorker w2 = new ClientWorker(clientSocket2, init.getPort(), udpSocket, init.getAddress());
				Thread t1 = new Thread(w2);
				t1.start(); // thread1 to execute udp
			}

		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		}
	}
}

class ClientWorker implements Runnable {
	private Socket client;
	private DatagramSocket udpSocket;
	private int udpPort;
	private DatagramSocket recSoc;;
	private InetAddress ip;
	// Constructor for TCP
	ClientWorker(Socket client) {
		this.client = client;

	}

	// Constructor for UDP
	ClientWorker(Socket client, int port, DatagramSocket udpSocket,InetAddress ip) {
		this.client = client;
		this.udpSocket = udpSocket;
		this.udpPort = port;
		this.ip=ip;
	}

	public void run() {

		byte[] buf = new byte[100];
		int i = 1; // Counter
		String req = "This is msg ";

		if (udpSocket == null) { // i.e.,the Thread called is TCP
			performTCP();
		}
		
		if (udpSocket != null) { // For UDP
			try {
				Thread.sleep(500);
				performUDP();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}

	private void performUDP() {
		// TODO Auto-generated method stub
		//InetAddress ip = null;
		byte[] buf = new byte[100];
		int i = 51; // Counter
		String req = "This is msg ";
		
		i = 51;
		while (i < 101) {
			try {
				
				buf = new byte[100];
				String val = req + i;
				buf = val.getBytes();

				//System.out.println("----");
				System.out.println("Sending UDP Packet" + val);
				DatagramPacket dp1 = new DatagramPacket(buf, buf.length, ip, udpPort);
				buf = new byte[100]; //delete buffer
				long current = System.currentTimeMillis(); //current time before sending packet
				
				udpSocket.send(dp1);
				
				//send last packet with delay
				if(i==100) {
					try {
						Thread.sleep(1500);
						
						//compute the time before which packet is rec. If there is a long delay send from tcp
						if ((System.currentTimeMillis() - current) > 1000) { 
							String value = req + i;
							System.out.println("Resend msg:"+value);
							PrintWriter outto = new PrintWriter(client.getOutputStream(), true);
							outto.println(value);

						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					if ((System.currentTimeMillis() - current) > 1000) { 
						String value = req + i;
						System.out.println("Resend msg:"+value);
						PrintWriter outto = new PrintWriter(client.getOutputStream(), true);
						outto.println(value);

					}
				}
				
				byte[] rec = new byte[100];
				DatagramPacket pp = new DatagramPacket(rec, rec.length); 
				
				//accept packet from client as ack
				udpSocket.receive(pp);
				
				
				
				i++;     			//increment the counter
				
			} catch (IOException o) {
				System.out.println(o.getMessage());
				System.out.println("in or out failed");
				System.exit(-1);
			} 
		}

	
		
	}

	private void performTCP() {
		// TODO Auto-generated method stub
		//BufferedReader in = null, into = null;

		PrintWriter out = null;
		URL url = null;
		int i = 1;
		Socket s = null;
		String req = "This is msg ";
		try {

			//in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);

		} catch (IOException e) {
			System.out.println("in or out failed");
			System.exit(-1);
		}

		while (i < 51) {

			String val = req + i;
			System.out.println("Sending TCP msg:"+val);
			out.println(val);
			//String str = null;
			i++;

		}
		// in.close();
		out.close();

	}	

}
