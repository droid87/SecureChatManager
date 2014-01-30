package at.fhooe.mcm30.fragments;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import at.fhooe.mcm30.R;
import at.fhooe.mcm30.concersation.Contact;

public class ContactsAdapter extends ArrayAdapter<Contact>{

	private Context context;
	
	public ContactsAdapter(Context context, List<Contact> contacts) {
		super(context, R.layout.list_item, contacts);
	    this.context = context;
	    addAll(contacts);
	}
	
	 @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    View rowView = inflater.inflate(R.layout.list_item, parent, false);
	    
	    TextView textViewName = (TextView) rowView.findViewById(R.id.title);
	    TextView textViewDesc = (TextView) rowView.findViewById(R.id.desc);
	    
	    textViewName.setText(getItem(position).getName());
	    textViewDesc.setText(getItem(position).getBTAddress());	    	    

	    return rowView;
	  }

}
