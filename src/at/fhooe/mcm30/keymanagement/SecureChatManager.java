package at.fhooe.mcm30.keymanagement;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import at.fhooe.mcm30.concersation.Contact;
import at.fhooe.mcm30.concersation.Conversation;


public class SecureChatManager implements SessionKeyExpired {
	
	private static final long serialVersionUID = -2674366968374272043L;
	private static final String RSA_KEY_FILE = "rsa_key";
	private static final String CONVERSATIONS_FILE = "conversations";
	private static final String CONTACTS_FILE = "contacts";
	
	private List<Contact> mContacts;
	private List<Conversation> mConversation;
	private RSAKeyPair mRSAKeyPair;
	private Context mContext;
	
	private static SecureChatManager instance;
	
	public SecureChatManager(Context _context) {
		mContext = _context;
		
		if(!loadContacts())
			mContacts = new ArrayList<Contact>();
		if(!loadConversations())
			mConversation = new ArrayList<Conversation>();
		if(!loadRSaKey())
			mRSAKeyPair = new RSAKeyPair();
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
			mConversation = new ArrayList<Conversation>();
		if(!loadRSaKey())
			mRSAKeyPair = new RSAKeyPair(_keySizeRSA);
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
			e.printStackTrace();
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

			mConversation = (List<Conversation>) ois.readObject();
			ois.close();
			fis.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(SecureChatManager.class.getName(), "cant load conversations");
			
			return false;
		}
		
		for(Conversation con : mConversation) {
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
			e.printStackTrace();
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
			
			oos.writeObject(mConversation);
			
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
			e.printStackTrace();
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
			e.printStackTrace();
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
		return mConversation;
	}
	
	public void addContact(Contact _contact) {
		mContacts.add(_contact);
	}
	
	public void addConversation(Conversation _conversation) {
		mConversation.add(_conversation);
		mConversation.get(mConversation.size()-1).registerExpiredSessionKey(this);
	}
	
	public SignedSessionKey encryptSessionKey(int _conversationIndex) {
		Conversation conversation = mConversation.get(_conversationIndex);
//		byte[] encrypted = CipherUtil.encryptRSA(conversation.getContact().getPuKey(), conversation.getSessionKeyBase64());
		byte[] encrypted = CipherUtil.encryptRSA(mRSAKeyPair.getPublicKey(), conversation.getSessionKeyBase64()); //TODO DEBUGGING
		byte[] signature = CipherUtil.signData(conversation.getSessionKeyBase64(), mRSAKeyPair.getPrivateKey());
		
		return new SignedSessionKey(encrypted, signature);
	}
	
	public SignedSessionKey encryptSessionKey(Conversation _conversation) {
		byte[] encrypted = CipherUtil.encryptRSA(_conversation.getContact().getPuKey(), _conversation.getSessionKeyBase64());
		byte[] signature = CipherUtil.signData(_conversation.getSessionKeyBase64(), mRSAKeyPair.getPrivateKey());
		
		return new SignedSessionKey(encrypted, signature);
	}
	
	public SignedSessionKey decryptSessionKey(int _conversationIndex, SignedSessionKey _signedKey) {
		Conversation conversation = mConversation.get(_conversationIndex);
		byte[] plain = CipherUtil.decryptRSA(mRSAKeyPair.getPrivateKey(), _signedKey.message);
		
//		return new SignedSessionKey(plain, CipherUtil.verifyData(plain, _signedKey.signedHash, conversation.getContact().getPuKey()));
		return new SignedSessionKey(plain, CipherUtil.verifyData(plain, _signedKey.signedHash, mRSAKeyPair.getPublicKey())); //TODO DEBUGGING
	}
	
	public SignedSessionKey decryptSessionKey(Conversation _conversation, SignedSessionKey _signedKey) {
		byte[] plain = CipherUtil.decryptRSA(mRSAKeyPair.getPrivateKey(), _signedKey.message);
		
		return new SignedSessionKey(plain, CipherUtil.verifyData(plain, _signedKey.signedHash, _conversation.getContact().getPuKey()));
	}

	@Override
	public void sessionKeyExpired(Conversation _conversation) {
		//renew session key
		_conversation.renewSessionKey();
		
		//TODO send on other contact
		//_conversation.getContact().getBTAddress()
	}
}
