RobotSim repository - RobotSim (used for simulation of the robots)
================

This RobotSim repository contains a RobotSim project. 
This project is responsible for communicating with the SimCity frontend and for dispatching the robot JARs.
In fact, we can think of this module as a deployer module for simulated robots.
Communication to the SimCity front-end is established through TCP sockets; this RobotSim project works analogous to the  F1 cores, when it comes to receiving simulation commands over TCP.  

It communicates with the RobotBackend to get the map, which is necessary for the simulation. A simple mock for the mapService is available, which can be enabled by adding the profile ``mocks`` to the run configuration.  
The JAR-file of the robotCore is not included en needs to be built from it's own project and put in the project root-directory.

Developed by
============

Huybrechts Thomas,
Janssens Arthur,
Vervliet Niels

University of Antwerp - 2016

Nick De Clerck,
Thomas Molkens

University of Antwerp - 2017

Imre Liessens,
Yunus Emre Yigit

University of Antwerp - 2018
