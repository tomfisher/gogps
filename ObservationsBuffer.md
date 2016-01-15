# Description #

In live processing observations may arrive from a mobile device, from a serial connection, from RTCM source or other asynchronous sources. Before goGPS can use them they have to be synchronized by GPS epoch and must be kept locally. ObservationsBuffer act as a buffer of received observations.

ObservationsBuffer can also act ad NavigationProducer in case the source provide this information (ie. a serial connection from uBlox)

# Logging #

ObservationsBuffer may also log received messages in a file for post processing purposes. Logfiles can be read by same ObservationsBuffer class.

# Streaming #
ObservationsBuffer implements StreamEventListener interface to enable diffent observation sources ( StreamEventProducer ) to feed the buffer.

StreamEventProducer interface is implemented by:
  * RTCM3Client - connects to a RTCM source to gather master data on Fixed Station or Virtual Reference Station (VRS)
  * UBXReader - Reads from InputStream UBX RAW,AID messages
  * UBXSerialConnection - Connects a uBloxwith serial interface, require RxTx lib
  * UBXSerailReader - used in UBXSerialConnection

Note: a StreamEventProducer can feed more StreamEventListener, this enable to perform several processing at the same time using same live data