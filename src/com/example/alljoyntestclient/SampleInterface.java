package com.example.alljoyntestclient;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;

@BusInterface (name = "com.sample.interface", announced="true")
public interface SampleInterface {

    @BusMethod(name = "Echo", annotation=0)
    public String echo(String str) throws BusException;
}
