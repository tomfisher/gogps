# Introduction #

In the current configuration goGPS is a library implementing the algorithms of its [MATLAB](http://www.gogps-project.org) counterpart that are considered stable and ready for a production environment.
We are working on a CommandLine version; at the current stage an IDE (e.g. [Eclipse](http://www.eclipse.org/) ) is needed to integrate the library.


goGPS needs 3 input sources to work
  * one ObservationsProducer for rover data
  * one ObservationsProducer for master data
  * one NavigationProducer for ephemeris (needed for computing satellite orbits)

and one or more PositionConsumer where to send computed position.

Note: some ObservationProducer classes may also act as NavigationProducer (e.g. ObservationsBuffer if fed by UBXSerialConnection).

Once the sources have been defined, goGPS can run in batch mode or in background (threaded) mode, depending on whether the task is live processing or post-processing.

Currently goGPS can apply the following processing algorithms:
  * Kalman filter on code and phase observations
  * Least squares adjustment on code double differences
  * Least squares adjustment on code standalone (i.e. without base station)

The two least-squares algorithms are mainly intended for diagnosing possible anomalous results obtained with the main (Kalman filter) algorithm.

# Tutorials #

LiveGoGps tutorial

PostProcessGoGps tutorial