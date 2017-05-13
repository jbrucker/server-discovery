package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import server.DiscoveryConfig;


/**
 * A client that tries to discover a service by sending a UDP broadcast
 * on a known port, containing a given request string.
 * Waits for a reply from the server.
 * 
 * Some issues that may affect this:
 * 1. Server/client on different LAN or VLAN.  Router generally does not
 *    forward broadcasts.
 * 2. On a wireless network with "Wifi Isolation" enabled, which prevents
 *    direct connections between Wifi clients.
 * 3. Client has more than one LAN IP address (at KU, machines have both
 *    IPv4 and IPv6 addresses). Might broadcast on the wrong address.
 *    This can be fixed by getting all IP addresses except localhost and loopback.
 *    See NetworkUtil class for example.
 *    
 * @see https://demey.io/network-discovery-using-udp-broadcast/
 */
public class DiscoveryClient {
	private static final int MAX_PACKET_SIZE = 16000;
	private static final Logger logger;
	
	/** Set an environment variable for logging format.  This is for 1-line messages. */
	static {
		// %1=datetime %2=methodname %3=loggername %4=level %5=message
		System.setProperty("java.util.logging.SimpleFormatter.format", 
				"%1$tF %1$tT %3$s %4$-7s %5$s%n");
		logger = Logger.getLogger("DiscoveryClient");
	}
	
	public static void main(String[] args) {
		DiscoveryClient client = new DiscoveryClient();
		client.run();
	}
	
	public void run() {
		// Create a Datagram (UDP) socket on any available port
		DatagramSocket socket = null;
		// Packet for receiving response from server
		byte[] receiveBuffer = new byte[MAX_PACKET_SIZE];
		DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
		
		// Create a socket for sending UDP broadcast packets
		try {
			socket = new DatagramSocket( );
			socket.setBroadcast(true);
		} catch (SocketException sex) {
			logger.severe("SocketException creating broadcast socket: "+sex.getMessage());
			throw new RuntimeException( sex );
		}
		
		// send a known request string (server checks this)
		//TODO is this correct or do we need to apply network byte order function?
		byte[] packetData = DiscoveryConfig.DISCOVERY_REQUEST.getBytes();
		
		// Java throws a lot of RIDICULOUS exceptions. Wrap the whole block
		
		try {
			// try the widest broadcast address first
			InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
			int servicePort = DiscoveryConfig.DISCOVERY_PORT;
			DatagramPacket packet = new DatagramPacket(packetData, packetData.length,
				               broadcastAddress, servicePort);
			socket.send(packet);
			logger.info( String.format("Sent packet to %s:%d", broadcastAddress.getHostAddress(), servicePort));
			
			// wait for reply
			//TODO should use a thread with time-out
			socket.receive(receivePacket);
			logger.info("Received reply from "+receivePacket.getAddress().getHostAddress() );
			logger.info("Reply data: "+new String(receivePacket.getData()));
		} catch (IOException ioe) {
			logger.log(Level.SEVERE, "IOException during send/receive", ioe);
		}
	}
}
