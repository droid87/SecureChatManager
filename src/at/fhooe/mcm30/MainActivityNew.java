package at.fhooe.mcm30;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang.SerializationUtils;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
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
import at.fhooe.mcm30.keymanagement.SessionKey;
import at.fhooe.mcm30.keymanagement.SignedSessionKey;

public class MainActivityNew extends FragmentActivity implements
		ActionBar.TabListener, CreateNdefMessageCallback,
		OnNdefPushCompleteCallback {

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
	
//	private Conversation mCurrentConversation;
	private byte[] mSessionKey;
	
	private SecureChatManager secureChatManager = SecureChatManager.getInstance(MainActivityNew.this);
	//----------------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_activity_new);

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
		
		//NFC ---------------------------------------------------------------------
		
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

		String name = android.os.Build.MODEL;
		String btAddress = "";

		if (btAdapter != null) {
			btAddress = btAdapter.getAddress();
		}

		myContact = new Contact(name, btAddress, SecureChatManager.getInstance(
				this).getPublicKey());
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
		
		//Bluetooth ---------------------------------------------------------------------
		mBluetoothMain = new BluetoothMain(this, mHandler);
		//Bluetooth ---------------------------------------------------------------------
	}
	
	private Handler mHandler = new Handler(new Handler.Callback() {
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case BluetoothMain.SOCKET_CONNECTED:
			mBluetoothConnection = (ConnectionThread) msg.obj;
			
			
			Toast.makeText(MainActivityNew.this, "socket connected", Toast.LENGTH_LONG).show();
			
			if (mFragmentContacts.isInitiator) {
				Contact myContact = secureChatManager.getMyContact();
				Wrapper wrapper = new Wrapper(MessageCodes.CONTACT, myContact);
				mBluetoothConnection.write(wrapper);
			}
			mFragmentContacts.isInitiator = false;
			break;
		case BluetoothMain.DATA_RECEIVED:
			byte[] data = (byte[])msg.obj;
			
			Wrapper receivedWrapper = (Wrapper)SerializationUtils.deserialize(data);
			
			switch (receivedWrapper.messageCode) {
			case CONTACT:
				Contact receivedContact = (Contact)receivedWrapper.messageObject;
				secureChatManager.addConversation(new Conversation(receivedContact));
				
				Log.i("test", "initial created session key: " + new String(secureChatManager.getConversations().get(0).getSessionKey()));
				
				Toast.makeText(MainActivityNew.this,
						"received message: " + receivedContact.toString(), Toast.LENGTH_LONG).show();
				
				mViewPager.setCurrentItem(1,true);
				
				SignedSessionKey signedSessionKey = secureChatManager.encryptSessionKey(0);
				Wrapper sessionKeyWrapper = new Wrapper(MessageCodes.SIGNED_SESSIONKEY, signedSessionKey);
				mBluetoothConnection.write(sessionKeyWrapper);
				
				Log.i("test","sent session key: " + new String(secureChatManager.getConversations().get(0).getSessionKeyBase64()));
				
				break;
			case SIGNED_SESSIONKEY:
				SignedSessionKey recSignedSessionKey = (SignedSessionKey)receivedWrapper.messageObject;
				Contact contact = secureChatManager.getContacts().get(0);
				
				byte[] sessionKey = secureChatManager.decryptSessionKey(contact.getPuKey(), recSignedSessionKey);
				
				if (sessionKey != null) {
					secureChatManager.addConversation(new Conversation(contact, sessionKey));
					mSessionKey = sessionKey;
//					secureChatManager.getConversations().get(0).setNewSessionKey(sessionKey);
					
					Log.i("test","received session key: " + new String(secureChatManager.getConversations().get(0).getSessionKey()));
				} else {
					Log.i("test","received session key is null");
					//TODO: send NACK
				}
				break;
			case CHAT_MESSAGE:
				byte[] ciphertext = (byte[])receivedWrapper.messageObject;
				
				Log.i("test","count conversations: " + secureChatManager.getConversations().size());
				
				Log.i("test","received encrypted message: " + new String(ciphertext));
				
				
				byte[] pt = secureChatManager.getConversations().get(0).decrypt(ciphertext);
				
				Log.i("test","used session key for decryption: " + new String(secureChatManager.getConversations().get(0).getSessionKey()));
				String plaintext = new String(pt);
				
				Log.i("test","received decrypted message: " + plaintext);
				
				Toast.makeText(MainActivityNew.this, plaintext, Toast.LENGTH_LONG).show();
				
				ConversationMessage message = new ConversationMessage(secureChatManager.getConversations().get(0).getContact().getName(), plaintext);
				conversationFragment.addMessage(message);
				break;
			}
			
			
//			byte[] decrypt = mCurrentConversation.decrypt(data.getBytes());
//			String decryptedMessage = new String(decrypt);
			
//			ConversationMessage message = new ConversationMessage(mCurrentConversation.getContact().getName(), data);
//			conversationFragment.addMessage(message);
		}
		return true;
	}
});
	
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
		if(item.getItemId() == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
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
	private final Handler mNFCHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SENT:
				Toast.makeText(getApplicationContext(), "Message sent!",
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
		
		if (partnerContact != null && !SecureChatManager.getInstance(this).isContactInList(partnerContact)) {
			SecureChatManager.getInstance(this).addContact(partnerContact);

			if (mFragmentContacts != null) {
				mFragmentContacts.invalidateAdapter();
			}
		}
	}
}
