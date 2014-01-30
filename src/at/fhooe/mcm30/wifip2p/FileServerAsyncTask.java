package at.fhooe.mcm30.wifip2p;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class FileServerAsyncTask extends
		AsyncTask<Void, Void, String> {

	private Context context;
	private ArrayList<FileLoadingCompleteListener> mListener;

	/**
	 * @param context
	 * @param statusText
	 */
	public FileServerAsyncTask(Context context,
			FileLoadingCompleteListener listener) {
		this.context = context;
		mListener = new ArrayList<FileLoadingCompleteListener>();
		if (listener != null) {
			mListener.add(listener);
		}
		// this.statusText = (TextView) statusText;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			ServerSocket serverSocket = new ServerSocket(8988);
			Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
			Socket client = serverSocket.accept();
			Log.d(WiFiDirectActivity.TAG, "Server: connection done");
			final File f = new File(
					Environment.getExternalStorageDirectory() + "/"
							+ context.getPackageName() + "/wifip2pshared-"
							+ System.currentTimeMillis() + ".jpg");

			File dirs = new File(f.getParent());
			if (!dirs.exists())
				dirs.mkdirs();
			f.createNewFile();

			Log.d(WiFiDirectActivity.TAG,
					"server: copying files " + f.toString());
			InputStream inputstream = client.getInputStream();
			CopyFileHelper.copyFile(inputstream, new FileOutputStream(f));
			serverSocket.close();
			return f.getAbsolutePath();
		} catch (IOException e) {
			Log.e(WiFiDirectActivity.TAG, e.getMessage());
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			for (FileLoadingCompleteListener listener : mListener) {
				listener.onFileLoadingComplete(result);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		// statusText.setText("Opening a server socket");
	}
}
