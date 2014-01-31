package at.fhooe.mcm30;


import java.nio.ByteBuffer;
import java.security.Key;
import java.util.List;
import java.util.Locale;


import org.apache.commons.lang.SerializationUtils;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import at.fhooe.mcm30.bluetooth.BluetoothMain;
import at.fhooe.mcm30.bluetooth.BluetoothMain.ConnectionThread;
import at.fhooe.mcm30.bluetooth.Wrapper;
import at.fhooe.mcm30.bluetooth.Wrapper.MessageCodes;
import at.fhooe.mcm30.concersation.Contact;
import at.fhooe.mcm30.concersation.Conversation;
import at.fhooe.mcm30.fragments.ContactsFragment;
import at.fhooe.mcm30.fragments.ConversationFragment;
import at.fhooe.mcm30.fragments.ConversationMessage;
import at.fhooe.mcm30.keymanagement.SecureChatManager;
import at.fhooe.mcm30.keymanagement.RSAKeyPair;
import at.fhooe.mcm30.keymanagement.SecureChatManager;
import at.fhooe.mcm30.keymanagement.SessionKey;
import at.fhooe.mcm30.keymanagement.SignedSessionKey;
import at.fhooe.mcm30.wifip2p.DeviceActionListener;
import at.fhooe.mcm30.wifip2p.FileLoadingCompleteListener;
import at.fhooe.mcm30.wifip2p.FileServerAsyncTask;
import at.fhooe.mcm30.wifip2p.FileTransferService;
import at.fhooe.mcm30.wifip2p.MacAddressHelper;
import at.fhooe.mcm30.wifip2p.WiFiDirectBroadcastReceiver;
import at.fhooe.mcm30.wifip2p.WifiP2pUtils;

public class MainActivityNew extends FragmentActivity implements
		ActionBar.TabListener, CreateNdefMessageCallback,
		OnNdefPushCompleteCallback, ChannelListener, DeviceActionListener,
		ConnectionInfoListener, FileLoadingCompleteListener, PeerListListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private ConversationFragment conversationFragment;

	private ContactsFragment mFragmentContacts;

	//NFC-----------------------------------------------------------------------
	private NfcAdapter mNfcAdapter;
	private TextView mInfoText;
	private static final int MESSAGE_SENT = 1;

	private Contact myContact = null;
	private Contact partnerContact = null;
	//---------------------------------------------------------------------------
	
	//Bluetooth ---------------------------------------------------------------
	private BluetoothMain mBluetoothMain;
	private ConnectionThread mBluetoothConnection;
	
	private SecureChatManager secureChatManager = null;
	//----------------------------------------------------------------------------
	// WifiP2P
	// ---------------------------------------------------------------------
	private final IntentFilter mWifiP2pIntentFilter = new IntentFilter();

	private WifiP2pManager mWifiP2pManager;
	private boolean isWifiP2pEnabled = false;
	private Channel mWifiP2pChannel;
	private BroadcastReceiver mWifiP2pReceiver = null;
	private WifiP2pDevice mWifiDevice;
	private Uri mWifiP2pImageUri = null;
	private boolean mWifiP2pDiscoverPeers;
	private boolean mStartNewWifiP2pConnection;
	// WifiP2P
	// ---------------------------------------------------------------------

	// private Conversation mCurrentConversation;
	private byte[] mSessionKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_activity_new);
		
		//set secureChatManager
		secureChatManager = SecureChatManager.getInstance(MainActivityNew.this);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		//BT ----------------------------------------------------------------------
		enableBluetooth();
		
		//NFC ---------------------------------------------------------------------

		// NFC
		// ---------------------------------------------------------------------
		WifiManager wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		
		// mInfoText.setText("MyContact:\n\n" + myContact.toString());

		// Check for available NFC Adapter
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		if (mNfcAdapter == null) {
			mInfoText.setText("NFC is not available on this device.");
		} else {
			// Register callback to set NDEF message
			mNfcAdapter.setNdefPushMessageCallback(this, this);

			// Register callback to listen for message-sent success
			mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
		}
		//NFC ---------------------------------------------------------------------
	}
	
	public void enableBluetooth() {
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

		String name = android.os.Build.MODEL;
		String btAddress = "";

		if (btAdapter != null) {
			btAddress = btAdapter.getAddress();
		}
		WifiManager wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		String wifiMacAddress = "";
		if (wifiManager != null) {
			wifiMacAddress = wifiManager.getConnectionInfo().getMacAddress();
		}
		myContact = new Contact(name, btAddress, wifiMacAddress,
				SecureChatManager.getInstance(this).getPublicKey());	
		
		mBluetoothMain = new BluetoothMain(this, mHandler);
	}
	
	private Handler mHandler = new Handler(new Handler.Callback() {
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case BluetoothMain.SOCKET_CONNECTED:
			mBluetoothConnection = (ConnectionThread) msg.obj;
			Log.d("MAIN", "socket connected");
			
			if (!mFragmentContacts.isInitiator) {
				Wrapper contactWrapper = new Wrapper(MessageCodes.CONTACT, myContact);
				mBluetoothConnection.write(contactWrapper);
			}
			mFragmentContacts.isInitiator = false;
			break;
		case BluetoothMain.DATA_RECEIVED:
			byte[] data = (byte[])msg.obj;
			
			Wrapper receivedWrapper = (Wrapper)SerializationUtils.deserialize(data);
			
			switch (receivedWrapper.messageCode) {
			case CONTACT:
				Contact receivedContact = (Contact)receivedWrapper.messageObject;
				secureChatManager.addContact(receivedContact);
				if (mFragmentContacts != null)
					mFragmentContacts.invalidateAdapter();
				secureChatManager.addConversation(new Conversation(secureChatManager.getContacts().get(0)));
				Log.i("contact","count contact: " + secureChatManager.getConversations().size());
				mViewPager.setCurrentItem(1,true);
				
				SignedSessionKey signedSessionKey = secureChatManager.encryptSessionKey(0);
				Wrapper sessionKeyWrapper = new Wrapper(MessageCodes.SIGNED_SESSIONKEY, signedSessionKey);
				mBluetoothConnection.write(sessionKeyWrapper);
				
				break;
			case SIGNED_SESSIONKEY:
				SignedSessionKey recSignedSessionKey = (SignedSessionKey)receivedWrapper.messageObject;
				Log.i("test","count sessionkey: " + secureChatManager.getConversations().size());
				Contact contact = secureChatManager.getContacts().get(0);
				
				byte[] sessionKey = secureChatManager.decryptSessionKey(contact.getPuKey(), recSignedSessionKey);
				
				if (sessionKey != null) {
					secureChatManager.addConversation(new Conversation(contact, sessionKey));
					
				} else {
					Log.i("test","received session key is null");
					//TODO: send NACK
				}
				break;
			case CHAT_MESSAGE:
				if(mViewPager.getCurrentItem()!=1)
					mViewPager.setCurrentItem(1, true);
				
				byte[] ciphertext = (byte[])receivedWrapper.messageObject;
				byte[] pt = secureChatManager.getConversations().get(1).decrypt(ciphertext);
				String plaintext = new String(pt);
				
				ConversationMessage message = new ConversationMessage(secureChatManager.getConversations().get(1).getContact().getName(), plaintext);
				conversationFragment.addMessage(message);
				break;
			case ACK:
				break;
			case NACK:
				break;
			default:
				break;
			}
		}
			return true;
		}
	});

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// WifiP2P
		// ---------------------------------------------------------------------
		if (requestCode == WifiP2pUtils.CHOOSE_FILE_RESULT_CODE && data != null
				&& data.getData() != null) {
			mWifiP2pDiscoverPeers = true;
			mWifiP2pImageUri = data.getData();
		}
	}
	
	public BluetoothMain getBluetoothMain() {
		return mBluetoothMain;
	}
	
	public ConnectionThread getBluetoothConnection() {
		return mBluetoothConnection;
	}
	
	public ViewPager getViewPager() {
		return mViewPager;
	}
	
	public void connectBluetooth(Contact _contact) {
		if (mBluetoothMain != null) {
			partnerContact = _contact;
			mBluetoothMain.connect(_contact.getBTAddress(), mHandler);
		}
	}
	
	public void sendMessage(String _message) {
//		if (mSessionKey != null) {
//			secureChatManager.getConversations().get(0).setNewSessionKey(mSessionKey);
//			
//			Log.i("test", "in send message -> mSessionKey: " + new String(mSessionKey));
//		} else {
//			Log.i("test", "in send message -> mSessionKey is null");
//		}
		
		byte[] sendObject = secureChatManager.getConversations().get(0).encrypt(_message.getBytes());
		Log.i("test","count: " + secureChatManager.getConversations().size());
		Log.i("test","USED session key: " + new String(secureChatManager.getConversations().get(0).getSessionKey()));
//		byte[] decryptSendObject = secureChatManager.getConversations().get(0).decrypt(sendObject);
		
		Log.i("test","sent encrypted message: " + new String(sendObject));
//		Log.i("test","local decrypted message: " + new String(decryptSendObject));
		Log.i("test","used session key: " + new String(secureChatManager.getConversations().get(0).getSessionKey()));
		
		Wrapper wrapper = new Wrapper(MessageCodes.CHAT_MESSAGE, sendObject);
		mBluetoothConnection.write(wrapper);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.action_gallery:
			if (!isWifiP2pEnabled) {
				Toast.makeText(MainActivityNew.this, "Enable WiFi!",
						Toast.LENGTH_SHORT).show();
				return true;
			}
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(intent, WifiP2pUtils.CHOOSE_FILE_RESULT_CODE);
			return true;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity_new, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
			case 0:
				if (mFragmentContacts == null) {
					mFragmentContacts = new ContactsFragment();
					mFragmentContacts.invalidateAdapter();
				} else {
					mFragmentContacts.invalidateAdapter();
				}
				return mFragmentContacts;
			case 1:
				if(conversationFragment == null) {
					conversationFragment = new ConversationFragment();
				}
				return conversationFragment;
			}

			return null;

		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater
					.inflate(R.layout.fragment_main_activity_new_dummy,
							container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}
	
	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		NdefMessage msg = null;

		if (myContact != null) {
			byte[] data = SerializationUtils.serialize(myContact);
			msg = new NdefMessage(NdefRecord.createMime(
					"application/at.fhooe.mcm30", data));
		} else {
			String text = ("myContact is null!");
			msg = new NdefMessage(NdefRecord.createMime(
					"application/at.fhooe.mcm30", text.getBytes()));
		}

		/**
		 * The Android Application Record (AAR) is commented out. When a device
		 * receives a push with an AAR in it, the application specified in the
		 * AAR is guaranteed to run. The AAR overrides the tag dispatch system.
		 * You can add it back in to guarantee that this activity starts when
		 * receiving a beamed message. For now, this code uses the tag dispatch
		 * system.
		 */
		// ,NdefRecord.createApplicationRecord("com.example.android.beam")

		return msg;
	}

	@Override
	public void onNdefPushComplete(NfcEvent event) {
		// A handler is needed to send messages to the activity when this
		// callback occurs, because it happens from a binder thread
		mNFCHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}

	/** This handler receives a message from onNdefPushComplete */
	@SuppressLint("HandlerLeak")
	private final Handler mNFCHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SENT:
				Toast.makeText(getApplicationContext(), "Contact sent!",
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}

		// WifiP2P
		// ---------------------------------------------------------------------
		if (mWifiP2pDiscoverPeers) {
			mStartNewWifiP2pConnection = true;
			discoverWifiP2pPeers();
			mWifiP2pDiscoverPeers = false;
		}
		Log.i("onResume", "onResume");
		mWifiP2pReceiver = new WiFiDirectBroadcastReceiver(mWifiP2pManager,
				mWifiP2pChannel, this);
		registerReceiver(mWifiP2pReceiver, mWifiP2pIntentFilter);
		// WifiP2P
		// ---------------------------------------------------------------------
	}

	@Override
	protected void onPause() {
		super.onPause();
		// WifiP2P
		// ---------------------------------------------------------------------
		unregisterReceiver(mWifiP2pReceiver);
		// WifiP2P
		// ---------------------------------------------------------------------
	}

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	/**
	 * Parses the NDEF Message from the intent and prints to the TextView
	 */
	void processIntent(Intent intent) {
		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];

		// record 0 contains the MIME type, record 1 is the AAR, if present
		// mInfoText.setText(new String(msg.getRecords()[0].getPayload()));

		byte[] data = msg.getRecords()[0].getPayload();
		partnerContact = (Contact) SerializationUtils.deserialize(data);

		if (partnerContact != null
				&& !SecureChatManager.getInstance(this).isContactInList(
						partnerContact)) {
			secureChatManager.addContact(partnerContact);
			Log.d("test","count: " + secureChatManager.getContacts().size());

			if (mFragmentContacts != null)
				mFragmentContacts.invalidateAdapter();
			
			//send contact data via bluetooth
			enableBluetooth();
			connectBluetooth(partnerContact);
		}
	}

	// WifiP2P
	// -----------------------------------------------------------------
	private void startNewWifiP2pConnection() {
		SecureChatManager secureChatManager = SecureChatManager
				.getInstance(getApplicationContext());
		List<Conversation> conversations = secureChatManager.getConversations();
		if (!conversations.isEmpty()) {
			String macAddress = conversations.get(0).getContact()
					.getWifiMacAddress();
			WifiP2pConfig config = new WifiP2pConfig();
			config.groupOwnerIntent = 0;
			mWifiDevice = new WifiP2pDevice();
			// config.deviceAddress = "04:46:65:FD:93:78";
			// config.deviceAddress = "CC:3A:61:82:EC:D9";
			config.deviceAddress = MacAddressHelper
					.changeMacAddress(macAddress);
			mWifiDevice.deviceAddress = config.deviceAddress;
			config.wps.setup = WpsInfo.PBC;
			connect(config);
		}
	}

	public void discoverWifiP2pPeers() {
		mWifiP2pChannel = mWifiP2pManager.initialize(this, getMainLooper(),
				null);
		mWifiP2pManager.discoverPeers(mWifiP2pChannel, null);
	}

	@Override
	public void onFileLoadingComplete(String fileName) {
		disconnect();
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + fileName), "image/*");
		this.startActivityForResult(intent, WifiP2pUtils.SHOW_FILE_RESULT_CODE);
	}

	@Override
	public void onConnectionInfoAvailable(final WifiP2pInfo info) {
		if (info.groupFormed && info.isGroupOwner) {
			new FileServerAsyncTask(this, this).execute();
		} else if (info.groupFormed && mWifiP2pImageUri != null) {
			// The other device acts as the client
			Log.d(WifiP2pUtils.TAG, "Intent----------- " + mWifiP2pImageUri);
			Intent serviceIntent = new Intent(this, FileTransferService.class);
			serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
			serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH,
					mWifiP2pImageUri.toString());
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
		mWifiP2pManager.connect(mWifiP2pChannel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				// WiFiDirectBroadcastReceiver will notify us
			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(MainActivityNew.this, "Connect failed. Retry.",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void disconnect() {
		mWifiP2pManager.removeGroup(mWifiP2pChannel, new ActionListener() {

			@Override
			public void onFailure(int reasonCode) {
				Log.d(WifiP2pUtils.TAG, "Disconnect failed. Reason :"
						+ reasonCode);

			}

			@Override
			public void onSuccess() {
			}

		});
	}

	@Override
	public void onChannelDisconnected() {
		mWifiP2pManager.removeGroup(mWifiP2pChannel, new ActionListener() {

			@Override
			public void onFailure(int reasonCode) {
				Log.d(WifiP2pUtils.TAG, "Disconnect failed. Reason :"
						+ reasonCode);

			}

			@Override
			public void onSuccess() {
			}

		});
	}

	@Override
	public void cancelDisconnect() {
		if (mWifiP2pManager != null) {
			if (mWifiDevice == null
					|| mWifiDevice.status == WifiP2pDevice.CONNECTED) {
				disconnect();
			} else if (mWifiDevice.status == WifiP2pDevice.AVAILABLE
					|| mWifiDevice.status == WifiP2pDevice.INVITED) {

				mWifiP2pManager.cancelConnect(mWifiP2pChannel,
						new ActionListener() {

							@Override
							public void onSuccess() {
								Toast.makeText(MainActivityNew.this,
										"Aborting connection",
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onFailure(int reasonCode) {
								Toast.makeText(
										MainActivityNew.this,
										"Connect abort request failed. Reason Code: "
												+ reasonCode,
										Toast.LENGTH_SHORT).show();
							}
						});
			}
		}

	}

	// WifiP2P
	// ---------------------------------------------------------------------

}
