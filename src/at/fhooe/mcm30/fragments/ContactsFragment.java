package at.fhooe.mcm30.fragments;

import java.util.List;
import java.util.Vector;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import at.fhooe.mcm30.R;
import at.fhooe.mcm30.R.layout;
import at.fhooe.mcm30.concersation.Contact;
import at.fhooe.mcm30.keymanagement.SecureChatManager;

public class ContactsFragment extends Fragment {

	private ListView listView;
	private List<Contact> list;
	private ContactsAdapter adapter;
	private Handler handler;
	
	private SecureChatManager securityManager;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		listView = (ListView) inflater.inflate(R.layout.fragment_contacts,
				container, false);

		list = new Vector<Contact>();

		securityManager = SecureChatManager.getInstance(getActivity());
		list.addAll(securityManager.getContacts());
		adapter = new ContactsAdapter(getActivity(), list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long pos) {
				Toast.makeText(getActivity(), "Contact " + list.get((int)pos).getName() + " was pressed." , Toast.LENGTH_SHORT).show();
			}
		});
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				adapter.clear();
				adapter.addAll(list);
				adapter.notifyDataSetChanged();
			}

		};

		return listView;

	}
}
