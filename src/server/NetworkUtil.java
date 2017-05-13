package server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Network utilities. The primary one is a method to get the server's external
 * IP address so clients know where to connect.
 * 
 * @author James Brucker
 */
public class NetworkUtil {
	static final String IPv4_PATTERN = "\\d+(\\.\\d+){3}";
	

	/**
	 * Return this server's InetAddress
	 * @return InetAddress of this server or null address cannot be obtained.
	 */
	public static InetAddress getMyAddress( ) {
		InetAddress inetAddr = null;
		List<InetAddress> addrs = getAllAddresses();
		// try to choose a non-local IPv4 address
		for(InetAddress addr: addrs) {
			if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) continue;
			if ( addr.getHostAddress().matches(IPv4_PATTERN) ) return addr;
		}
		// didn't find a match. Try LocalHost address.
		try {
			inetAddr = InetAddress.getLocalHost();
		} catch (Exception e) {
			System.out.println("NetworkUtil.getMyAddress: " + e.getMessage());
		}
		return inetAddr;
	}

	/**
	 * Return all active addresses of this server, except loopback address.
	 */
	public static List<InetAddress> getAllAddresses() {
		List<InetAddress> addrlist = new ArrayList<InetAddress>();
	    try {
		    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				// filters out 127.0.0.1 and inactive interfaces
				if (iface.isLoopback() || !iface.isUp()) continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while(addresses.hasMoreElements()) {
					InetAddress ipaddr = addresses.nextElement();
					addrlist.add( ipaddr );
//					System.out.println(iface.getDisplayName() + " " + ipaddr);
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		return addrlist;
	}
	
	/** For testing. */
	public static void main(String[] args) {
		List<InetAddress> addrs = getAllAddresses();
		System.out.print("IP Addresses: ");
		for(InetAddress addr: addrs) {
			System.out.print(" " + addr.getHostAddress());
			if (addr.isLinkLocalAddress()) System.out.print(" (LinkLocal)");
			if (addr.isLoopbackAddress()) System.out.print(" (Loopback)");
		}
		System.out.println();
	}

}
