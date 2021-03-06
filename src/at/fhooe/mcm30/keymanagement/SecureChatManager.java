package at.fhooe.mcm30.keymanagement;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import at.fhooe.mcm30.R;
import at.fhooe.mcm30.concersation.Contact;
import at.fhooe.mcm30.concersation.Conversation;


public class SecureChatManager implements SessionKeyExpired {
	
	private static final String RSA_KEY_FILE = "rsa_key";
	private static final String CONVERSATIONS_FILE = "conversations";
	private static final String CONTACTS_FILE = "contacts";
	
	private List<Contact> mContacts;
	private List<Conversation> mConversations;
	private RSAKeyPair mRSAKeyPair;
	private Context mContext;
	
	private static SecureChatManager instance;
	
	public SecureChatManager(Context _context) {
		mContext = _context;
		
		if(!loadContacts())
			mContacts = new ArrayList<Contact>();
		if(!loadConversations())
			mConversations = new ArrayList<Conversation>();
		if(!loadRSaKey()) {
			mRSAKeyPair = new RSAKeyPair();
			storeRSAKey();
		}
	}
	
	
	public static SecureChatManager getInstance(Context context) {
		if(instance == null) {
			instance = new SecureChatManager(context);
		}
		return instance;
	}
	
	
	public SecureChatManager(Context _context, int _keySizeRSA) {
		mContext = _context;
		
		if(!loadContacts())
			mContacts = new ArrayList<Contact>();
		if(!loadConversations())
			mConversations = new ArrayList<Conversation>();
		if(!loadRSaKey())
		{
			mRSAKeyPair = new RSAKeyPair(_keySizeRSA);
			storeRSAKey();
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean loadContacts() {
		FileInputStream fis;
		ObjectInputStream ois;
		
		try {
			fis = mContext.openFileInput(CONTACTS_FILE);
			ois = new ObjectInputStream(fis);

			mContacts = (List<Contact>) ois.readObject();
			ois.close();
			fis.close();
			
		} catch (Exception e) {
			Log.w(SecureChatManager.class.getName(), "cant load conversations");
			
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private boolean loadConversations() {
		FileInputStream fis;
		ObjectInputStream ois;
		
		try {
			fis = mContext.openFileInput(CONVERSATIONS_FILE);
			ois = new ObjectInputStream(fis);

			mConversations = (List<Conversation>) ois.readObject();
			ois.close();
			fis.close();
			
		} catch (Exception e) {
			Log.w(SecureChatManager.class.getName(), "cant load conversations");
			
			return false;
		}
		
		for(Conversation con : mConversations) {
			con.initCipher(con.getSessionKey());
			con.registerExpiredSessionKey(this);
		}
		return true;
	}
	
	private boolean loadRSaKey() {
		FileInputStream fis;
		ObjectInputStream ois;
		
		try {
			fis = mContext.openFileInput(RSA_KEY_FILE);
			ois = new ObjectInputStream(fis);

			mRSAKeyPair = (RSAKeyPair) ois.readObject();
			
			ois.close();
			fis.close();
			
		} catch (Exception e) {
			Log.w(SecureChatManager.class.getName(), "cant load RSA key");
			
			return false;
		}
		
		return true;
	}
	
	private boolean storeConversations() {
		FileOutputStream fos;
		ObjectOutputStream oos;
		
		try {
			fos = mContext.openFileOutput(CONVERSATIONS_FILE, Context.MODE_PRIVATE);
			
			oos = new ObjectOutputStream(fos);
			oos.writeObject(mConversations);
			
			oos.flush();
			oos.close();
			fos.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(SecureChatManager.class.getName(), "cant store conversations");
			
			return false;
		}
		
		return true;
	}
	
	private boolean storeContacts() {
		FileOutputStream fos;
		ObjectOutputStream oos;
		
		try {
			fos = mContext.openFileOutput(CONTACTS_FILE, Context.MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			
			oos.writeObject(mContacts);
			
			oos.flush();
			oos.close();
			fos.close();
			
		} catch (Exception e) {
			Log.w(SecureChatManager.class.getName(), "cant store contacts");
			
			return false;
		}
		
		return true;
	}
	
	private boolean storeRSAKey() {
		FileOutputStream fos;
		ObjectOutputStream oos;
		
		try {
			fos = mContext.openFileOutput(RSA_KEY_FILE, Context.MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			
			oos.writeObject(mRSAKeyPair);
			
			oos.flush();
			oos.close();
			fos.close();
			
		} catch (Exception e) {
			Log.w(SecureChatManager.class.getName(), "cant store rsa key");
			
			return false;
		}
		
		return true;
	}
	
	@Override
	protected void finalize() throws Throwable {
		saveSecureChatManager();
		super.finalize();
	}
	
	public boolean saveSecureChatManager() {
		if(!storeContacts())
			return false;
		if(!storeConversations())
			return false;
		if(!storeRSAKey())
			return false;
		
		return true;
	}
	
	public Key getPublicKey() {
		return mRSAKeyPair.getPublicKey();
	}
	
	public List<Contact> getContacts() {
		return mContacts;
	}
	
	public List<Conversation> getConversations() {
		return mConversations;
	}
	
	public void addContact(Contact _contact) {
		mContacts.add(_contact);
		storeContacts();
	}
	
	public boolean isContactInList(Contact _cont) {
		for (Contact contact : mContacts) {
			if (contact.getBTAddress().equalsIgnoreCase(_cont.getBTAddress())) {
				return true;
			}
		}
		return false;
	}
	
	public void addConversation(Conversation _conversation) {
		if(isConversationInList(_conversation))
			return;
		
		mConversations.add(_conversation);
		storeConversations();
	}
	
	public boolean isConversationInList(Conversation _conv) {
		for (Conversation conv : mConversations) {
			if (conv.getContact().getBTAddress().equalsIgnoreCase(conv.getContact().getBTAddress())) {
				return true;
			}
		}
		return false;
	}
	
	public SignedSessionKey encryptSessionKey(int _conversationIndex) {
		Conversation conversation = mConversations.get(_conversationIndex);
		byte[] encrypted = CipherUtil.encryptRSA(conversation.getContact().getPuKey(), conversation.getSessionKeyBase64());
//		byte[] encrypted = CipherUtil.encryptRSA(mRSAKeyPair.getPublicKey(), conversation.getSessionKeyBase64()); //TODO DEBUGGING
		byte[] signature = CipherUtil.signData(conversation.getSessionKeyBase64(), mRSAKeyPair.getPrivateKey());
		
		return new SignedSessionKey(encrypted, signature);
	}
	
	public SignedSessionKey encryptSessionKey(Conversation _conversation) {
		byte[] encrypted = CipherUtil.encryptRSA(_conversation.getContact().getPuKey(), _conversation.getSessionKeyBase64());
		byte[] signature = CipherUtil.signData(_conversation.getSessionKeyBase64(), mRSAKeyPair.getPrivateKey());
		
		return new SignedSessionKey(encrypted, signature);
	}
	
	public byte[] decryptSessionKey(Key _publicKey, SignedSessionKey _signedKey) {
//		Conversation conversation = mConversations.get(_conversationIndex);
		byte[] plain = CipherUtil.decryptRSA(mRSAKeyPair.getPrivateKey(), _signedKey.message);
		
		if( CipherUtil.verifyData(plain, _signedKey.signature, _publicKey))
//		if( CipherUtil.verifyData(plain, _signedKey.signature, mRSAKeyPair.getPublicKey()))//TODO DEBUGGING
			return plain;
		else
			return null; //TODO
	}
	
	public byte[] decryptSessionKey(Conversation _conversation, SignedSessionKey _signedKey) {
		byte[] plain = CipherUtil.decryptRSA(mRSAKeyPair.getPrivateKey(), _signedKey.message);
		Log.i("test","signature: " + new String(_signedKey.signature));
		if( CipherUtil.verifyData(plain, _signedKey.signature, _conversation.getContact().getPuKey()))
//		if( CipherUtil.verifyData(plain, _signedKey.signature, mRSAKeyPair.getPublicKey()))//TODO DEBUGGING
			return plain;
		else
			return null; //TODO
	}

	@Override
	public void sessionKeyExpired(Conversation _conversation) {
		//renew session key
		_conversation.renewSessionKey();
	}
	
	public Contact getMyContact() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		String name = sharedPrefs.getString(mContext.getString(R.string.pref_key_name),
				"-");
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		String btAdress = "";
		if (bluetoothAdapter != null) {
			btAdress = bluetoothAdapter.getAddress();
		}
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		String wifiMacAddress = "";
		if(wifiManager!=null){
			wifiMacAddress = wifiManager.getConnectionInfo().getMacAddress();
		}
		Contact contact = new Contact(name, btAdress, wifiMacAddress, getPublicKey());
		return contact;
	}
}
