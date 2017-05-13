# Service Discover using UDP Broadcasts

Java code for finding a local server using UDP broadcasts.

* `DiscoveryServer` - runs on the server machine to receive and respond to discovery requiests
* `DiscoveryClient` - run on client to get server's IP address
* `DiscoveryConfig` - constants used by both client and server


## Issues That May Prevent From Working

1. If client and server are on different LAN or VLAN the broadcasts won't be forwarded by intervening router.
2. If client and server are on a Wifi network with "Wifi Isolation" enabled, the router won't allow direct connection.
3. If client or server have more than one IP address, the broadcast might not be sent on the logical network that the server is listening on.  This can be fixed by testing for all IP addresses (and excluding loopback and other special addresses).

## Alternative: Multicast instead of Broadcast

## Reference

* [Network Discovery using UDP Broadcast](https://demey.io/network-discovery-using-udp-broadcast/) by De Mey.
* [Writing a Datagram Client and Server](https://docs.oracle.com/javase/tutorial/networking/datagrams/clientServer.html) in the *Java Tutorial*.
