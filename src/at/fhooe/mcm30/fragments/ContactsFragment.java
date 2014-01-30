package at.fhooe.mcm30.fragments;

import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import at.fhooe.mcm30.R;
import at.fhooe.mcm30.keymanagement.SecureChatManager;

public class ContactsFragment extends Fragment {

	private NfcAdapter mNfcAdapter;
	private static final int MESSAGE_SENT = 1;

	private ListView listView;
	private ContactsAdapter adapter;

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

		return listView;

	}
	
	public void invalidateAdapter() {
		if(securityManager!=null){
			adapter = new ContactsAdapter(getActivity(), securityManager.getContacts());
			listView.setAdapter(adapter);
		}
	}
}
