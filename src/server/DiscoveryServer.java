package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import static server.DiscoveryConfig.*;

/**
 * A server that listens for broadcast UDP packets on a given port.
 * When a packet is received, it checks for discover string and (if match)
 * sends a reply packet containing the server's IP address.
 * 
 * Some issues that may affect this:
 * 1. Server/client on different LAN or VLAN.  Router generally does not
 *    forward broadcasts.
 * 2. On a wireless network with "Wifi Isolation" enabled, which prevents
 *    direct connections between Wifi clients.
 * 3. Server has more than one LAN IP address.  Server might respond with 
 *    wrong IP address. This could be fixed by checking all IP addresses
 *    and choosing best match to the client's IP address.
 *    
 * @see https://demey.io/network-discovery-using-udp-broadcast/
 */
public class DiscoveryServer implements Runnable {
	// how much data to accept from a broadcast client.
	private static final int MAX_PACKET_SIZE = 16000;
	private static final Logger logger;
	private DatagramSocket socket;
	
	/** Set an environment variable for logging format.  This is for 1-line messages. */
	static {
		// %1=datetime %2=methodname %3=loggername %4=level %5=message
		System.setProperty("java.util.logging.SimpleFormatter.format", 
				"%1$tF %1$tT %3$s %4$-7s %5$s%n");
		logger = Logger.getLogger("DiscoveryServer");
	}
	
	public static void main(String[] args) {
		DiscoveryServer server = new DiscoveryServer();
		server.run();
	}

	@Override
	public void run() {
		// quit if we get this many consecutive receive errors.
		// reset the counter after successfully receiving a packet.
		final int max_errors = 5;
		int errorCount = 0;
		
		// this is weak - address could be null or wrong address
		final String MY_IP = NetworkUtil.getMyAddress().getHostAddress();
		
		// Keep a socket open to listen to all UDP trafic that is
		// destined for this port.
		try {
			socket = new DatagramSocket(DISCOVERY_PORT, InetAddress.getByName("0.0.0.0"));
			// set flag to enable receipt of broadcast packets
			socket.setBroadcast(true);
		} catch (Exception ex) {
			String msg = "Could not create UDP socket on port " + DISCOVERY_PORT;
			logger.log(Level.SEVERE, msg);
			System.err.println(msg);  // delete this after testing (redundant)
			return;
		}
			
		System.out.println("Server listening on port "+DISCOVERY_PORT);
			
		while (true) {
			// Receive a packet
			byte[] recvBuf = new byte[MAX_PACKET_SIZE];
			DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
			try {
				// wait for a packet
				socket.receive(packet);
			} catch (IOException ioe) {
				logger.log(Level.SEVERE, ioe.getMessage(), ioe);
				// this is to avoid infinite loops when exception is raised.
				errorCount++;
				if (errorCount >= max_errors) return;
				// try again
				continue;
			}

			// Packet received
			errorCount = 0;    // reset error counter 
			InetAddress clientAddress = packet.getAddress();
			int clientPort = packet.getPort();
			
			logger.info(String.format("Packet received from %s:%d",
					clientAddress.getHostAddress(), clientPort) );
			
			logger.info("Received data: " + new String(packet.getData()) );
	
			// See if the packet holds the correct signature string
			String message = new String(packet.getData()).trim();
			if (message.startsWith(DISCOVERY_REQUEST)) {
				String reply =  DISCOVERY_REPLY + MY_IP;
				byte[] sendData = reply.getBytes();

				// Send the response
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, clientAddress, clientPort);
				try {
					socket.send(sendPacket);
					logger.info(String.format("Reply sent to %s:%d",
							clientAddress.getHostAddress(), clientPort) );
				} catch(IOException ioe) {
					logger.log(Level.SEVERE, "IOException sending service reply", ioe);
				}
			}
			else {
				logger.info(String.format("Packet from %s:%d not a discovery packet",
						clientAddress.getHostAddress(), clientPort) );
			}
		}
	}	

}
