package at.fhooe.mcm30.fragments;

public class ConversationMessage {

	private String author;
	private String msg;
	
	public ConversationMessage(String _author, String _msg) {
		author = _author;
		_msg = msg;
	}
	
//	private 
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
