package at.fhooe.mcm30.fragments;

import java.util.List;
import java.util.Vector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import at.fhooe.mcm30.R;

public class ConversationFragment extends Fragment {

	private View root;
	
	private ListView listView;
	private List<ConversationMessage> list;
	private MessagesAdapter adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.fragment_conversation,
				container, false);		
		listView = (ListView) root.findViewById(R.id.lst_conversation);
		
		list = new Vector<ConversationMessage>();

		//TODO: delete when real messages are there
//		Message m = new Message();
//		m.setAuthor("A User");
//		m.setMsg("This is another TestMessage");
//		
//		list.add(m);
//		Message m1 = new Message();
//		m1 = new Message();
//		m1.setAuthor("Anderer User");
//		m1.setMsg("This is a Test-Message");
//		list.add(m1);
		//TODO: deletion end
		
		
		adapter = new MessagesAdapter(getActivity());
		adapter.addAll(list);
		listView.setAdapter(adapter);
		return root;
		
	}
	
	public void addMessage(ConversationMessage _message ) {
		adapter.add(_message);
		adapter.notifyDataSetChanged();		
	}
	
}
