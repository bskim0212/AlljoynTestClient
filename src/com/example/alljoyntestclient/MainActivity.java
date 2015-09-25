package com.example.alljoyntestclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.alljoyn.about.AboutKeys;
import org.alljoyn.about.AboutService;
import org.alljoyn.about.AboutServiceImpl;
import org.alljoyn.bus.AboutListener;
import org.alljoyn.bus.AboutObjectDescription;
import org.alljoyn.bus.AnnotationBusException;
import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.OnJoinSessionListener;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.Variant;
import org.alljoyn.bus.VariantUtil;
import org.alljoyn.bus.ifaces.Introspectable;
import org.alljoyn.ioe.controlpanelservice.ControlPanelException;
import org.alljoyn.ioe.controlpanelservice.communication.IntrospectionNode;
import org.alljoyn.ioe.controlpanelservice.communication.interfaces.ActionControl;
import org.alljoyn.ioe.controlpanelservice.communication.interfaces.ControlPanel;
import org.alljoyn.ioe.controlpanelservice.communication.interfaces.PropertyControl;
import org.alljoyn.services.common.AnnouncementHandler;
import org.alljoyn.services.common.BusObjectDescription;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

	private final String APPLICATION_NAME = "com.TestClient";
	
	private final String TEST_TEXT = "test1";

	static {
		System.loadLibrary("alljoyn_java");
	} // static

	public static final String SERVICE_NAME = "org.alljoyn.simulator";
	public static final String SERVICE_APP_NAME = "alljoyn simulator";
	public static final short CONTACT_PORT = 1000;

	private BusAttachment bus;

	private FrameLayout fl_main;// ArrayList<TestInterface>

	public static Map<String, ArrayList<TestInterface>> ACMAP;

	private String ifacesAction[] = { ActionControl.IFNAME };
	private String ifacesProperty[] = { PropertyControl.IFNAME };
	private String ifacesControl[] = { ControlPanel.IFNAME };

	private boolean isConnected = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ACMAP = new HashMap<String, ArrayList<TestInterface>>();
		// myNodeName = "node" + UUID.randomUUID().toString().hashCode();
		// connectAJ();
		fl_main = (FrameLayout) findViewById(R.id.fl_main);

		if (connectAJ()) {
			Log.e("bskim", "connectAJ complete");
		} else {
			Log.e("bskim", "connectAJ error");
		}

	}

	private boolean connectAJ() {

		boolean ok = org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(getApplicationContext());
		if (!ok) {
			Log.e("bskim", "PrepareDaemon fail : " + ok);
			return false;
		}
		bus = new BusAttachment(APPLICATION_NAME, BusAttachment.RemoteMessage.Receive);

		bus.registerBusListener(new BusListener() {
			@Override
			public void foundAdvertisedName(final String name, short transport, final String namePrefix) {
				Log.e("bskim",
						"foundAdvertisedName : " + name + " transport=" + transport + " namePrefix=" + namePrefix);
				if (!name.startsWith(SERVICE_NAME)) {

					return;
				}

				SessionOpts sessionOpts = new SessionOpts();
				sessionOpts.transports = transport;
				Mutable.IntegerValue sessionId = new Mutable.IntegerValue();
				bus.enableConcurrentCallbacks();

				// Status status = bus.joinSession(name, CONTACT_PORT,
				// sessionId, sessionOpts, new SessionListener() {
				// @Override
				// public void sessionLost(int sessionId, int reason) {
				// Log.e("bskim", "sessionLost");
				// }
				// });

				bus.joinSession(name, CONTACT_PORT, sessionOpts, new SessionListener() {
					@Override
					public void sessionLost(int sessionId, int reason) {
					}
				}, new OnJoinSessionListener() {
					@Override
					public void onJoinSession(Status status, int sessionId, SessionOpts opts, Object context) {

						if (status == Status.OK) {
							Log.e("bskim", "onJoinSession success : " + status);
							// ProxyBusObject proxyObj =
							// bus.getProxyBusObject(name, SERVICE_PATH,
							// sessionId,
							// new Class<?>[] { ResponseInterface.class });
							// ResponseInterface rsp =
							// proxyObj.getInterface(ResponseInterface.class);
							bus.enableConcurrentCallbacks();

							String deviceName = name.substring(name.lastIndexOf("."), name.length());

							if (ACMAP.containsKey(deviceName) && !isConnected) {
								Log.e("onJoinSession", "containsKey true");
								ArrayList<TestInterface> tifaceList = ACMAP.get(deviceName);
								for (int i = 0; i < tifaceList.size(); i++) {
									TestInterface tiface = tifaceList.get(i);

									
									ProxyBusObject proxyObj;

									if (tiface.getiName().equalsIgnoreCase(ActionControl.IFNAME)
											&& tiface.getAction() != null) {
										proxyObj = bus.getProxyBusObject(name, tiface.getiPath(), sessionId,
												new Class<?>[] { ActionControl.class });
										ActionControl aci = proxyObj.getInterface(ActionControl.class);
										tiface.setAction(aci);
										Log.e("bskim", "add Action interface " + tiface.getiPath());
										tifaceList.set(i, tiface);
									} else if (tiface.getiName().equalsIgnoreCase(PropertyControl.IFNAME)
											&& tiface.getProperty() != null) {
										proxyObj = bus.getProxyBusObject(name, tiface.getiPath(), sessionId,
												new Class<?>[] { PropertyControl.class });
										PropertyControl pci = proxyObj.getInterface(PropertyControl.class);
										tiface.setProperty(pci);
										Log.e("bskim", "add Property interface " + tiface.getiPath());
										tifaceList.set(i, tiface);
										
									}
								}

								for (int i = 0; i < tifaceList.size(); i++) {
									try {
										if (tifaceList.get(i).getiName().equalsIgnoreCase(ActionControl.IFNAME)) {
											ActionControl aci = tifaceList.get(i).getAction();
											bus.enableConcurrentCallbacks();
											Log.e("bskim", "ActionControl : " + tifaceList.get(i).getiPath());
											aci.Exec();
										} else
											if (tifaceList.get(i).getiName().equalsIgnoreCase(PropertyControl.IFNAME)) {
											PropertyControl pci = tifaceList.get(i).getProperty();
											bus.enableConcurrentCallbacks();
											Log.e("bskim", "ActionProperty : " + tifaceList.get(i).getiPath());
											Variant vt = pci.getValue();
											String devIdSig;
											devIdSig = (vt != null) ? vt.getSignature() : "";

											// test case : string
											if (devIdSig.equals("s")) {
												Log.e("bskim", "Received getValue() : " + vt.getObject(String.class));

											}
										}

									} catch (BusException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

								ACMAP.put(deviceName, tifaceList);

							} else {
								Log.e("onJoinSession", "containsKey false");
							}

							// try {
							// //ACMAP
							// //ACMAP.put("", aci);
							// if (aci != null) {
							// Log.e("bskim", "ActionControl On : On");
							// aci.Exec();
							// }
							//
							// } catch (BusException e) {
							// e.printStackTrace();
							// }

							connectedSSID = sessionId;
							isConnected = true;

						} else {
							Log.e("bskim", "onJoinSession fail : " + status);
						}

					}
				}, new Object());

			}

			@Override
			public void lostAdvertisedName(String name, short transport, String namePrefix) {

				Log.e("bskim", "lostAdvertisedName");
				isConnected = false;
			}
		});

		Status status = bus.connect();
		if (status != Status.OK) {
			Log.e("bskim", "connect false : " + status);
			return false;
		}

		MyAboutListener aboutL = new MyAboutListener();
		
		 bus.registerAboutListener(aboutL);
		 status = bus.whoImplements(ifacesAction);
		 if (status != Status.OK) {
		 return false;
		 }
		
		 bus.registerAboutListener(aboutL);
		 status = bus.whoImplements(ifacesProperty);
		 if (status != Status.OK) {
		 return false;
		 }
		 bus.registerAboutListener(aboutL);
		 status = bus.whoImplements(ifacesControl);
		 if (status != Status.OK) {
		 return false;
		 }
		 
		 

		
		
//		try {
//			AboutServiceImpl.getInstance().startAboutClient(bus);
//			AboutServiceImpl.getInstance().addAnnouncementHandler(new AnnouncementHandler() {
//
//				@Override
//				public void onDeviceLost(String arg0) {
//					// TODO Auto-generated method stub
//
//				}
//
//				@Override
//				public void onAnnouncement(String busName, short port, BusObjectDescription[] objectDescriptions,
//						Map<String, Variant> aboutMap) {
//					Log.e("onAnnouncement", "onAnnouncement");
//					// TODO Auto-generated method stub
//					try {
//						Variant varDeviceId = aboutMap.get(AboutKeys.ABOUT_DEVICE_ID);
//						String devIdSig;
//
//						devIdSig = VariantUtil.getSignature(varDeviceId);
//
//						if (!devIdSig.equals("s")) {
//							return;
//						}
//						String deviceId = varDeviceId.getObject(String.class);
//						Log.e("onAnnouncement", "deviceId=" + deviceId);
//
//						for (int i = 0; i < objectDescriptions.length; ++i) {
//							BusObjectDescription description = objectDescriptions[i];
//							String[] supportedInterfaces = description.getInterfaces();
//							for (int j = 0; j < supportedInterfaces.length; ++j) {
//								Log.e("onAnnouncement", "supportedInterfaces[" + i + "]=" + supportedInterfaces[j]);
//							}
//							
//							String iPath = description.getPath();
//							Log.e("onAnnouncement", "iPath[" + i + "]=" + iPath);
//						}
//					} catch (BusException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}, new String[] { "org.alljoyn.ControlPanel.*" });
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		status = bus.findAdvertisedName("");
		if (status != Status.OK) {
			Log.e("bskim", "findAdvertisedName false : " + status);
			return false;
		}

		return true;
	}

	private int mSessionId;

	private void findAN() {
		Status status = bus.findAdvertisedName(SERVICE_NAME);
		if (status != Status.OK) {
			Log.e("bskim", "findAdvertisedName false : " + status);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Status status = bus.leaveSession(mSessionId);
		try {
			AboutServiceImpl.getInstance().stopAboutClient();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bus.disconnect();

	}

	int connectedSSID = 0;

	private AboutService aboutClient;

	static class MyAboutListener implements AboutListener {
		@Override
		public void announced(String busName, int version, short port, AboutObjectDescription[] objectDescriptions,
				Map<String, Variant> aboutData) {
			Log.e("bskim", "AboutListener announced : busName=" + busName);
			try {

				Variant varDeviceId = aboutData.get(AboutKeys.ABOUT_APP_NAME);
				String devIdSig;
				devIdSig = (varDeviceId != null) ? varDeviceId.getSignature() : "";
				if (!devIdSig.equals("s")) {
					Log.e("bskim", "Received '" + AboutKeys.ABOUT_APP_NAME + "', that has an unexpected signature: '"
							+ devIdSig + "', the expected signature is: 's'");
					return;
				}

				String appName = varDeviceId.getObject(String.class);

				varDeviceId = aboutData.get(AboutKeys.ABOUT_DEVICE_NAME);
				devIdSig = (varDeviceId != null) ? varDeviceId.getSignature() : "";
				if (!devIdSig.equals("s")) {
					Log.e("bskim", "Received '" + AboutKeys.ABOUT_DEVICE_NAME + "', that has an unexpected signature: '"
							+ devIdSig + "', the expected signature is: 's'");
					return;
				}

				String deviceName = varDeviceId.getObject(String.class);

				Log.e("bskim", "ABOUT_APP_NAME=" + appName);
				Log.e("bskim", "ABOUT_DEVICE_NAME=" + deviceName);
				try {
					IntrospectionNode n = new IntrospectionNode("/josh");
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (!ACMAP.containsKey(deviceName)) {
					if (objectDescriptions != null && objectDescriptions.length > 0) {

						// ArrayList<TestInterface>
						ArrayList<TestInterface> tiList = new ArrayList<TestInterface>();
						for (int i = 0; i < objectDescriptions.length; i++) {
							TestInterface ti = new TestInterface();
							Log.e("bskim", "objectDescriptions[" + i + "] path=" + objectDescriptions[i].path);
							ti.setiPath(objectDescriptions[i].path);
							for (int ii = 0; ii < objectDescriptions[i].interfaces.length; ii++) {
								Log.e("bskim", "objectDescriptions[" + i + "] interfaces[" + ii + "]="
										+ objectDescriptions[i].interfaces[ii]);
								ti.setiName(objectDescriptions[i].interfaces[ii]);
							}

							tiList.add(ti);
						}

						ACMAP.put(deviceName, tiList);
					}
				}
			} catch (AnnotationBusException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BusException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
