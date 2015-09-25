package com.example.alljoyntestclient;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;

@BusInterface( name = "com.lge.alljoyn.simulator.ResponseInterface", announced="true" )
public interface ResponseInterface{
 
 @BusMethod
 public String request(String msg) throws BusException;
  
}
