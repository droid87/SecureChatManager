package at.fhooe.mcm30.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang.SerializationUtils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class BluetoothMain {

	public static final UUID MY_UUID = UUID
			.fromString("f895eaf0-867f-11e3-baa7-0800200c9a66");

	public static final int SOCKET_CONNECTED = 1;
	public static final int DATA_RECEIVED = 2;

	private BluetoothAdapter mBluetoothAdapter = null;

	private ConnectionThread mBluetoothConnection;

	private Context mContext;

	public BluetoothMain(Context _context, Handler _handler) {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mContext = _context;

		if (mBluetoothAdapter == null) {
			Toast.makeText(mContext, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			// finish();
		} else {
			int btState = mBluetoothAdapter.getState();

			if (btState == BluetoothAdapter.STATE_OFF) {
				// mTvStatus.setText("Bluetooth is off");
				if (!mBluetoothAdapter.isEnabled()) {
					mBluetoothAdapter.enable();
					// Intent enableIntent = new Intent(
					// BluetoothAdapter.ACTION_REQUEST_ENABLE);
					// startActivityForResult(enableIntent, REQEUEST_ENABLE_BT);
				}
			} else if (btState == BluetoothAdapter.STATE_ON) {
				initializeBluetooth(_handler);
			}
		}
	}
	
	public boolean isEnabled() {
		return isEnabled();
	}
	
	public boolean hasConnection() {
		if(mBluetoothConnection!=null)
			return mBluetoothConnection.isAlive();
		else
			return false;
	}

	public void connect(String address, Handler handler) {
		new ConnectThread(address, handler).start();
	}

	private void initializeBluetooth(Handler handler) {
		new AcceptThread(handler).start();
	}

	class AcceptThread extends Thread {
		private final Handler mmHandler;
		private BluetoothServerSocket mmServerSocket;
		private BluetoothSocket mmSocket = null;

		public AcceptThread(Handler handler) {
			mmHandler = handler;
			try {
				mmServerSocket = mBluetoothAdapter
						.listenUsingRfcommWithServiceRecord("Bluetooth demo",
								MY_UUID);
			} catch (IOException e) {

			}
		}

		public void run() {
			while (true) {
				try {
					if(mmServerSocket!=null){
						mmSocket = mmServerSocket.accept();
						ConnectionThread conn = new ConnectionThread(mmSocket,
								mmHandler);
						mmHandler.obtainMessage(SOCKET_CONNECTED, conn)
								.sendToTarget();
						conn.start();
						mmServerSocket.close();
					}
					break;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class ConnectionThread extends Thread {
		BluetoothSocket mmBluetoothSocket;
		private final Handler mmHandler;
		private InputStream mmInStream;
		private OutputStream mmOutStream;
		private ObjectOutputStream mmObjectOutputStream;
		private ObjectInputStream mmObjectInputStream;

		public ConnectionThread(BluetoothSocket socket, Handler handler) {
			super();
			mmBluetoothSocket = socket;
			mmHandler = handler;
			try {
				mmInStream = mmBluetoothSocket.getInputStream();
				mmOutStream = mmBluetoothSocket.getOutputStream();

				mmObjectOutputStream = new ObjectOutputStream(mmOutStream);
				mmObjectInputStream = new ObjectInputStream(mmInStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public BluetoothSocket getBluetoothSocket() {
			return mmBluetoothSocket;
		}

		@Override
		public void run() {
			try {
				byte[] buffer = new byte[1024];
				while (true) {

					synchronized (mmObjectInputStream) {
						mmObjectInputStream.read(buffer);
					}

					// byte[] receivedBytes = Base64.decode(buffer, 0);

					// String data = new String(buffer, 0, len);

					mmHandler.obtainMessage(DATA_RECEIVED, buffer)
							.sendToTarget();

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public synchronized void write(Serializable _object) {
			try {
				byte[] sendBytes = SerializationUtils.serialize(_object);

				// byte[] sendBytesEncoded = Base64.encode(sendBytes, 0);
				mmObjectOutputStream.write(sendBytes);
				mmObjectOutputStream.flush();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public class ConnectThread extends Thread {
		private BluetoothSocket mmBluetoothSocket;
		private final BluetoothDevice mmDevice;
		private final Handler mmHandler;

		public ConnectThread(String address, Handler handler) {
			mmHandler = handler;
			mmDevice = mBluetoothAdapter.getRemoteDevice(address);
			try {
				mmBluetoothSocket = mmDevice
						.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			mBluetoothAdapter.cancelDiscovery();
			try {
				mmBluetoothSocket.connect();
				ConnectionThread conn = new ConnectionThread(mmBluetoothSocket,
						mmHandler);
				mmHandler.obtainMessage(SOCKET_CONNECTED, conn).sendToTarget();
				conn.start();
				
			} catch (IOException e) {
				try {
					mmBluetoothSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}

}
