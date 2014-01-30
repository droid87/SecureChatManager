package at.fhooe.mcm30;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;

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
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.fhooe.mcm30.concersation.Contact;

public class NFCActivity extends Activity implements CreateNdefMessageCallback,
		OnNdefPushCompleteCallback {

	private NfcAdapter mNfcAdapter;
	private TextView mInfoText;
	private ListView mLvContacts;
	private ArrayAdapter<String> mAdapterContacts;
	private List<String> mListContacts;
	private static final int MESSAGE_SENT = 1;
	
	private Contact myContact = null;
	private Contact partnerContact = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nfc);

		mInfoText = (TextView) findViewById(R.id.tvInfo);
		
		mLvContacts = (ListView)findViewById(R.id.lvContacts);
		mListContacts = new ArrayList<String>();
		mAdapterContacts = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,mListContacts);
		mLvContacts.setAdapter(mAdapterContacts);
		
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		
		String name = android.os.Build.MODEL;
		String btAddress = "";
		
		if (btAdapter != null) {
			btAddress = btAdapter.getAddress();
		}
		
		myContact = new Contact(name, btAddress, MainActivity.mSecureChatManager.getPublicKey());
//		mInfoText.setText("MyContact:\n\n" + myContact.toString());
		
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
	}
	
	/**
     * Implementation for the CreateNdefMessageCallback interface
     */
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
          * receives a push with an AAR in it, the application specified in the AAR
          * is guaranteed to run. The AAR overrides the tag dispatch system.
          * You can add it back in to guarantee that this
          * activity starts when receiving a beamed message. For now, this code
          * uses the tag dispatch system.
          */
          //,NdefRecord.createApplicationRecord("com.example.android.beam")

        return msg;
	}

	/**
     * Implementation for the OnNdefPushCompleteCallback interface
     */
	@Override
	public void onNdefPushComplete(NfcEvent arg0) {
		// A handler is needed to send messages to the activity when this
        // callback occurs, because it happens from a binder thread
        mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
    }

    /** This handler receives a message from onNdefPushComplete */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_SENT:
                Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
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
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        
        // record 0 contains the MIME type, record 1 is the AAR, if present
        // mInfoText.setText(new String(msg.getRecords()[0].getPayload()));
        
        byte[] data = msg.getRecords()[0].getPayload();
        partnerContact = (Contact) SerializationUtils.deserialize(data);
        
//        mInfoText.setText(mInfoText.getText() + "\n\nOther Contact:\n\n" + partnerContact.toString());
        mListContacts.add(partnerContact.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // If NFC is not available, we won't be needing this menu
        if (mNfcAdapter == null) {
            return super.onCreateOptionsMenu(menu);
        }
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.nfc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_settings:
//                Intent intent = new Intent(Settings.ACTION_NFCSHARING_SETTINGS);
//                startActivity(intent);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
    	
    	return super.onOptionsItemSelected(item);
    }

}
