package at.fhooe.mcm30;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import at.fhooe.mcm30.concersation.Contact;
import at.fhooe.mcm30.concersation.Conversation;
import at.fhooe.mcm30.keymanagement.SecureChatManager;
import at.fhooe.mcm30.keymanagement.SignedSessionKey;

public class MainActivity extends Activity {
	
	private TextView mKeySession;
	private TextView mRSAEncrypt;
	private TextView mRSADecrypt;
	private EditText mText;
	
	public static SecureChatManager mSecureChatManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSecureChatManager = new SecureChatManager(getApplicationContext());

		mKeySession = (TextView) super.findViewById(R.id.tvSessionKey);
		mRSAEncrypt = (TextView) super.findViewById(R.id.tvEncryptedRSAPu);
		mRSADecrypt = (TextView) super.findViewById(R.id.tvDecryptedRSAPr);
		
		mText = (EditText) super.findViewById(R.id.etPlainText);
		
		((Button) super.findViewById(R.id.bCreateSessionKey))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// add new conversation (but first we need a new key)
						KeyPairGenerator key=null;
						try {key = KeyPairGenerator.getInstance("RSA");} catch (NoSuchAlgorithmException e) {}
						key.initialize(2048);
						KeyPair pair = key.genKeyPair();
						
						Contact testContact = new Contact("MyDick", "BT_MAC", key.genKeyPair().getPublic());
						mSecureChatManager.addContact(testContact);
						mSecureChatManager.addConversation(new Conversation(testContact));
						
						mSecureChatManager.saveSecureChatManager();
						//show session key
						mKeySession.setText(new String(mSecureChatManager.getConversations().get(0).getSessionKeyBase64()));
						
						//get encrypted session key
						SignedSessionKey sKey = mSecureChatManager.encryptSessionKey(0);
						mRSAEncrypt.setText(new String(sKey.message));
						
						//get decrypted session key
						SignedSessionKey ssKey = mSecureChatManager.decryptSessionKey(0, sKey);
						mRSADecrypt.setText(new String(ssKey.message) + " " + ssKey.verified);
					}

				});
		((Button) super.findViewById(R.id.bNFCMenu))
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this,NFCActivity.class);
				startActivity(i);				
			}
			
		});
		
		((Button) super.findViewById(R.id.bEncrypt))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// encrypts plain text
						if (mText.getText().length() > 0 && mSecureChatManager.getConversations().get(0) != null)
							mText.setText(new String(mSecureChatManager.getConversations().get(0).encrypt(mText.getText().toString().getBytes())));
					}

				});
		((Button) super.findViewById(R.id.bDecrypt))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// decrypts cipher text
						if (mText.getText().length() > 0 && mSecureChatManager.getConversations().get(0) != null)
							mText.setText(new String(mSecureChatManager.getConversations().get(0).decrypt(mText.getText().toString().getBytes())));
					}

				});
	}
}
