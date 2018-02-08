# srm-correlation-process

## Description

This project contains the implementation of Correlation Process to validate the SRM validation results with legacy system.

It holds two processes: 
1. Correlation Request Process - which sends the SRM validation results to Mainframe
2. Correlation Response Process - which consumes and persists the Mainframe validation results back to SRM

## To Run

To start the application use the following command line form (what is in the [...] brackets are optional):
> java -Xms256 -Xmx256 -Dlog4j.configurationFile=log4j2.xml -Dspring.profiles.active='<environment'> -jar srm-correlation-process-0.0.0.jar

## TO DO List

1. Correlation Response Process
2. Operational Event Support
3. 1PC Transaction Propagation
4. Execution spring boot with local profile