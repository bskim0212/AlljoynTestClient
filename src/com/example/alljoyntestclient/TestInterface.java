package com.example.alljoyntestclient;

import org.alljoyn.ioe.controlpanelservice.communication.interfaces.ActionControl;
import org.alljoyn.ioe.controlpanelservice.communication.interfaces.PropertyControl;

public class TestInterface {
	private String iName;
	private String iPath;
	private ActionControl action;
	private PropertyControl property;

	public String getiName() {
		return iName;
	}

	public void setiName(String iName) {
		this.iName = iName;
	}

	public ActionControl getAction() {
		return action;
	}

	public void setAction(ActionControl action) {
		this.action = action;
	}

	public String getiPath() {
		return iPath;
	}

	public void setiPath(String iPath) {
		this.iPath = iPath;
	}

	public PropertyControl getProperty() {
		return property;
	}

	public void setProperty(PropertyControl property) {
		this.property = property;
	}

}
