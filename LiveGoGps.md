# Introduction #

Here the steps to perform a complete live tracking using serial connected uBlox, RTCM base station and output to a KML file.

# Defining rover and navigation sources #

Open serial connection to the ublox (in this case ulox must be already configured to output RXM-RAW, AID-HUI and AID-EPH messages, NMEA messages can remain on, they are ignored)

```
UBXSerialConnection ubxSerialConn = new UBXSerialConnection("COM1", 9600);
ubxSerialConn.init();
```

redirect the stream into a buffer and use same resource as NavigationProducer (as long it produces AID-EPH):

```
ObservationsBuffer roverIn = new ObservationsBuffer(ubxSerialConn,"./roverOut.dat");
NavigationProducer navigationIn = roverIn;
roverIn.init();
```

Note: in the same time we log messages into roverOut.dat

# Find approx position #

In order to setup a master station (VRS) we need to know current approximative position. We will use goGPS in standalone running in batch mode, it will return the approx position when the DOP value goes under 10 (fast and aproximative, does not need to be precise):

```
GoGPS goGPSstandalone = new GoGPS(navigationIn, roverIn, null);
goGPSstandalone.setDynamicModel(GoGPS.DYN_MODEL_STATIC);
// retrieve initial position, do not need to be precise
Coordinates initialPosition = goGPSstandalone.runCodeStandalone(10);
```

# Setup master station #

Once we know current approximative position we may setup the VRS on that position:

```
RTCM3Client rtcmClient = RTCM3Client.getInstance("server.rtcm.org", 8080 /*port*/, "user", "password", "mount_point");
rtcmClient.setVirtualReferenceStationPosition(initialPosition);
rtcmClient.setReconnectionPolicy(RTCM3Client.CONNECTION_POLICY_RECONNECT);
rtcmClient.setExitPolicy(RTCM3Client.EXIT_ON_LAST_LISTENER_LEAVE);
			rtcmClient.init();
```

and (same as rover) redirect the output into a buffer (and log messages in masterOut.dat):

```
ObservationsBuffer masterIn = new ObservationsBuffer(rtcmClient,"./masterOut.dat");
masterIn.init();
```

# Setup output #

Define the output of computed positions:

```
String outPath = "./out.kml";
KmlProducer kml = new KmlProducer(outPath, 2.5, 1);
```

# Live computing #

all input and output are ready, now we setup real precise position goGPS:

```
GoGPS goGPS = new GoGPS(navigationIn, roverIn, masterIn);
goGPS.setDynamicModel(GoGPS.DYN_MODEL_CONST_SPEED);
goGPS.addPositionConsumerListener(kml);
```

and run in background:
```
goGPS.runThreadMode(GoGPS.RUN_MODE_KALMAN_FILTER); // will run and exit immediately
```

in this mode it will process data untill data comes from the sources. To stop it is needed to close the sources:

```
Thread.sleep(120*1000); // stop after 2 minutes
roverIn.release(true,10000); // release and close rover
masterIn.release(true,10000);  // release and close master
navigationIn.release(true,10000);  // release and close navigation...well since is same as roverIn it was already closed
```

In sources you may find the LiveTracking test class, that is more complete and generates live output to Google Earth.