package at.fhooe.mcm30.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import at.fhooe.mcm30.R;

public class MessagesAdapter extends ArrayAdapter<ConversationMessage>{

	private Context context;
	
	public MessagesAdapter(Context context) {
		super(context, R.layout.list_item_conversation);
	    this.context = context;
	}
	
	 @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    View rowView = inflater.inflate(R.layout.list_item_conversation, parent, false);
	    
	    TextView textViewName = (TextView) rowView.findViewById(R.id.conversation_list_item_name);
	    TextView textViewText = (TextView) rowView.findViewById(R.id.conversation_list_item_text);
	    
	    textViewName.setText(getItem(position).getAuthor() + ":");
	    textViewText.setText(getItem(position).getMsg());
	    
	    return rowView;
	  }

}
