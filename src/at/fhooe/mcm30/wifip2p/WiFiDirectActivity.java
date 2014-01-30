/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.fhooe.mcm30.wifip2p;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.fhooe.mcm30.R;

/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class WiFiDirectActivity extends Activity implements ChannelListener,
		DeviceActionListener, ConnectionInfoListener,
		FileLoadingCompleteListener, PeerListListener {

	public static final String TAG = "wifip2p";
	private static final int CHOOSE_FILE_RESULT_CODE = 19;
	private static final int SHOW_FILE_RESULT_CODE = 85;

	private final IntentFilter intentFilter = new IntentFilter();

	private WifiP2pManager manager;
	private boolean isWifiP2pEnabled = false;
	private Channel channel;
	private BroadcastReceiver receiver = null;
	private WifiP2pDevice device;
	private Uri mFileUri = null;
	private boolean mDiscoverPeers;
	private boolean mStartNewWifiP2pConnection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		WifiManager wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);

		// add intent actions
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHOOSE_FILE_RESULT_CODE && data != null
				&& data.getData() != null) {
			mDiscoverPeers = true;
			mFileUri = data.getData();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDiscoverPeers) {
			mStartNewWifiP2pConnection = true;
			discoverPeers();
			mDiscoverPeers = false;
		}
		receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
		registerReceiver(receiver, intentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_items, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.atn_direct_enable:
			if (manager != null) {
				startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
			} else {
				Log.e(TAG, "manager is null");
			}
			return true;

		case R.id.atn_direct_discover:
			if (!isWifiP2pEnabled) {
				Toast.makeText(WiFiDirectActivity.this,
						R.string.p2p_off_warning, Toast.LENGTH_SHORT).show();
				return true;
			}
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void startNewWifiP2pConnection() {
		WifiP2pConfig config = new WifiP2pConfig();
		config.groupOwnerIntent = 0;
		device = new WifiP2pDevice();
		// config.deviceAddress = "04:46:65:FD:93:78";
		// config.deviceAddress = "CC:3A:61:82:EC:D9";
	
		config.deviceAddress = MacAddressHelper
				.changeMacAddress("CC:3A:61:82:EC:D9");
		device.deviceAddress = config.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		connect(config);
	}

	public void discoverPeers() {
		channel = manager.initialize(this, getMainLooper(), null);
		manager.discoverPeers(channel, null);
	}

	@Override
	public void onFileLoadingComplete(String fileName) {
		disconnect();
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + fileName), "image/*");
		this.startActivityForResult(intent, SHOW_FILE_RESULT_CODE);
	}

	@Override
	public void onConnectionInfoAvailable(final WifiP2pInfo info) {
		if (info.groupFormed && info.isGroupOwner) {
			new FileServerAsyncTask(this, this).execute();
		} else if (info.groupFormed && mFileUri != null) {
			// The other device acts as the client
			Log.d(WiFiDirectActivity.TAG, "Intent----------- " + mFileUri);
			Intent serviceIntent = new Intent(this, FileTransferService.class);
			serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
			serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH,
					mFileUri.toString());
			serviceIntent.putExtra(
					FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
					info.groupOwnerAddress.getHostAddress());
			serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT,
					8988);
			this.startService(serviceIntent);
		}
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList peerList) {
		if (mStartNewWifiP2pConnection) {
			mStartNewWifiP2pConnection = false;
			startNewWifiP2pConnection();
		}
	}

	/**
	 * @param isWifiP2pEnabled
	 *            the isWifiP2pEnabled to set
	 */
	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}

	@Override
	public void connect(WifiP2pConfig config) {
		manager.connect(channel, config, new ActionListener() {
	
			@Override
			public void onSuccess() {
				// WiFiDirectBroadcastReceiver will notify us
			}
	
			@Override
			public void onFailure(int reason) {
				Toast.makeText(WiFiDirectActivity.this,
						"Connect failed. Retry.", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void disconnect() {
		manager.removeGroup(channel, new ActionListener() {
	
			@Override
			public void onFailure(int reasonCode) {
				Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
	
			}
	
			@Override
			public void onSuccess() {
			}
	
		});
	}

	@Override
	public void onChannelDisconnected() {
		manager.removeGroup(channel, new ActionListener() {
	
			@Override
			public void onFailure(int reasonCode) {
				Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
	
			}
	
			@Override
			public void onSuccess() {
			}
	
		});
	}

	@Override
	public void cancelDisconnect() {
		if (manager != null) {
			if (device == null || device.status == WifiP2pDevice.CONNECTED) {
				disconnect();
			} else if (device.status == WifiP2pDevice.AVAILABLE
					|| device.status == WifiP2pDevice.INVITED) {
	
				manager.cancelConnect(channel, new ActionListener() {
	
					@Override
					public void onSuccess() {
						Toast.makeText(WiFiDirectActivity.this,
								"Aborting connection", Toast.LENGTH_SHORT)
								.show();
					}
	
					@Override
					public void onFailure(int reasonCode) {
						Toast.makeText(
								WiFiDirectActivity.this,
								"Connect abort request failed. Reason Code: "
										+ reasonCode, Toast.LENGTH_SHORT)
								.show();
					}
				});
			}
		}
	
	}
}
