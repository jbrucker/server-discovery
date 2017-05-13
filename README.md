# Service Discover using UDP Broadcasts

Java code for finding a local server using UDP broadcasts.

* `DiscoveryServer` - runs on the server machine to receive and respond to discovery requiests
* `DiscoveryClient` - run on client to get server's IP address
* `DiscoveryConfig` - constants used by both client and server

## How to Run

1. After cloning the project, check `DISCOVERY_PORT` in `server/DiscoveryConfig.java`.  Use any available port above 1024.
2. Run the DiscoveryServer on one machine.
3. On a different machine on the same LAN run DiscoveryClient.  It should print the server's address.  It sends a broadcast every 2 seconds until a response is received. You can run server and client on the same machine, too.

Both client and server print several log messages on the console.  You can reconfigure the Logger (`java.util.logging.Logger`) to either print messages to a file or not log anything.

Example Server Output
```
2017-05-13 14:37:07 DiscoveryServer INFO    My IP Address 10.2.23.174
Server listening on port 8888
2017-05-13 14:37:22 DiscoveryServer INFO    Packet received from 10.2.23.111:58852
```

Example Client Output
```
2017-05-13 14:37:22 DiscoveryClient INFO    Sent packet to 255.255.255.255:8888
2017-05-13 14:37:22 DiscoveryClient INFO    Received reply from 10.2.23.174
2017-05-13 14:37:22 DiscoveryClient INFO    Reply data: FOO_SERVER_IP 10.2.23.174
```

## Issues That May Prevent This From Working

1. If client and server are on different LAN or VLAN the broadcasts won't be forwarded by intervening router.
2. If client and server are on a WiFi network with "WiFi Isolation" enabled, the router won't allow direct communication between them.
3. If client or server have more than one IP address, the broadcast might not be sent on the logical network that the server is listening on.  This can be fixed by sending broadcasts on all IP addresses (excluding loopback and other special addresses).


## Reference

* [Network Discovery using UDP Broadcast](https://demey.io/network-discovery-using-udp-broadcast/) by De Mey.
* [Writing a Datagram Client and Server](https://docs.oracle.com/javase/tutorial/networking/datagrams/clientServer.html) in the *Java Tutorial*.
