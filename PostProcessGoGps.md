# Introduction #

Here the steps to perform post processing with goGPS using RINEX files or UBX binary files (e.g. generated with uCenter)


# Define inputs #

Simply define ObservationsProducer classes by using the file parser needed for the corresponding file format, in this case ubx file for the rover and RINEX for master and navigation:

```
ObservationsProducer roverIn = new UBXFileReader(new File("./data/ublox.ubx"));
ObservationsProducer masterIn = new RinexObservationParser(new File("./data/vrs.11o"));
NavigationProducer navigationIn = new RinexNavigationParser(new File("./data/vrs.11n"));
```

# Define output #

```
double goodDopThreshold = 2.5;
int timeSampleDelaySec = 30; // should be tuned according to the dataset; use '0' to disable timestamps in the KML 
String outPath = "./test/out.kml";
KmlProducer kml = new KmlProducer(outPath, goodDopThreshold, timeSampleDelaySec);
```

# Initialize the input streams #

```
navigationIn.init();
roverIn.init();
masterIn.init();
```

# Run #

Run goGPS in batch mode, with a constant velocity dynamic model for the Kalman filter

```
int dynamicModel = GoGPS.DYN_MODEL_CONST_SPEED; //may be also set to constant acceleration or static
GoGPS goGPS = new GoGPS(navigationIn, roverIn, masterIn);
goGPS.addPositionConsumerListener(kml);
goGPS.setDynamicModel(dynamicModel);
goGPS.runKalmanFilter();
```

# End properly #

```
roverIn.release(true,10000);
masterIn.release(true,10000);
navigationIn.release(true,10000);
```