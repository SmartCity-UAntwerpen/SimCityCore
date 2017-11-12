SimCityCore repository - SimCityCore (used for simulation)
================

This SimCityCore repository contains a SimCityCore project. 
This project is responsible for communicating with the SimCity frontend and for dispatching the robot JARs.
In fact, we can think of this module as a deployer module for simulated robots.
Communication to the SimCity front-end is established through TCP sockets; this SimCityCore works analogous to the drone and F1 cores, when it comes to receiving simulation commands over TCP.

NOTE: The robot simulation has not been updated for the current version of our project and therefore does not use our new backbone (SmartCity Core) but still communicates with the elder implementation found in the SmartCity Core module in the SmartCityProject repository.

Developed by
============

Huybrechts Thomas,
Janssens Arthur,
Vervliet Niels

University of Antwerp - 2016

Nick De Clerck,
Thomas Molkens

University of Antwerp - 2017
