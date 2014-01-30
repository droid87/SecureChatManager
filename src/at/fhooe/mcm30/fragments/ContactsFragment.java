package at.fhooe.mcm30.fragments;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.List;
import java.util.Vector;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.fhooe.mcm30.MainActivity;
import at.fhooe.mcm30.R;
import at.fhooe.mcm30.R.layout;
import at.fhooe.mcm30.concersation.Contact;
import at.fhooe.mcm30.keymanagement.SecureChatManager;

public class ContactsFragment extends Fragment {

	private NfcAdapter mNfcAdapter;

	private static final int MESSAGE_SENT = 1;

	private ListView listView;
	private ContactsAdapter adapter;
//	 private Handler handler;

	private SecureChatManager securityManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		listView = (ListView) inflater.inflate(R.layout.fragment_contacts,
				container, false);

		securityManager = SecureChatManager.getInstance(getActivity());
		adapter = new ContactsAdapter(getActivity(),
				securityManager.getContacts());
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long pos) {
				Toast.makeText(
						getActivity(),
						"Contact "
								+ securityManager.getContacts().get((int) pos)
										.getName() + " was pressed.",
						Toast.LENGTH_SHORT).show();
			}
		});
//		 handler = new Handler() {
//		 @Override
//		 public void handleMessage(Message msg) {
//			 adapter.clear();
//			 adapter.addAll(securityManager.getContacts());
//			 adapter.notifyDataSetChanged();
//		 }
//		
//		 };

		return listView;

	}
	
	public void invalidateAdapter() {
		if (adapter != null) {
			adapter.clear();
			adapter.addAll(securityManager.getContacts());
			adapter.notifyDataSetChanged();
		}
	}
}
