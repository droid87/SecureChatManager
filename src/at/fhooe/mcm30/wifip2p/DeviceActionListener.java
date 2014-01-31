package at.fhooe.mcm30.wifip2p;

import android.net.wifi.p2p.WifiP2pConfig;

/**
 * An interface-callback for the activity to listen to fragment interaction
 * events.
 */
public interface DeviceActionListener {

    void cancelDisconnect();

    void connect(WifiP2pConfig config);

    void disconnect();
}
