# Introduction #

ObservationsProducer is a class interface that mask a producer of satellite observations, master or rover.

# Details #

Classes that implements ObservationsProducer are:
  * ObservationsBuffer - this class act as intermediate to effective ObservationsProducer
  * RinexObservationParser - read observations from RINEX file
  * UBXFileReader - read observations from UBX binary file (ie captured with uCenter)