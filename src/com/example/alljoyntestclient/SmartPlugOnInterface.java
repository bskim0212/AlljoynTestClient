package com.example.alljoyntestclient;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;
import org.alljoyn.ioe.controlpanelservice.communication.interfaces.ActionControl;

@BusInterface( name = "org.alljoyn.ControlPanel.Action" )
public interface SmartPlugOnInterface  {

	@BusMethod(name="/ControlPanel/SmartPlug/rootContainer/en/ControlsContainer/On")
	public void setStateOn() throws BusException;
	
	
}
