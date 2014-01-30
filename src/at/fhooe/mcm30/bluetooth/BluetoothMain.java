package at.fhooe.mcm30.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothMain {
	
	public static final UUID MY_UUID = UUID
			.fromString("f895eaf0-867f-11e3-baa7-0800200c9a66");
	
	private static final int SOCKET_CONNECTED = 1;
	private static final int DATA_RECEIVED = 2;
	
	private static final int REQEUEST_ENABLE_BT = 3;
	private static final int LIST_DEVICE = 4;
	
	private TextView mTvStatus;
	private TextView mTvDeviceName;
	private TextView mTvData;
	private Button mBtnList;
	private Button mBtnMakeDiscoverable;
	
	private BluetoothAdapter mBluetoothAdapter = null;
	
	private ConnectionThread mBluetoothConnection;
	
	private Context mContext;
	
	public BluetoothMain(Context _context) {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mContext = _context;
		
		if (mBluetoothAdapter == null) {
			Toast.makeText(mContext, "Bluetooth is not available", Toast.LENGTH_LONG)
				.show();
//			finish();
		} else {
			int btState = mBluetoothAdapter.getState();
			
			if (btState == BluetoothAdapter.STATE_OFF) {
				mTvStatus.setText("Bluetooth is off");
				if (!mBluetoothAdapter.isEnabled()) {
					mBluetoothAdapter.enable();
//					Intent enableIntent = new Intent(
//							BluetoothAdapter.ACTION_REQUEST_ENABLE);
//					startActivityForResult(enableIntent, REQEUEST_ENABLE_BT);
				}
			} else if (btState == BluetoothAdapter.STATE_ON) {
				initializeBluetooth();
			}
		}		
	}

//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//		
//		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//		
//		if (mBluetoothAdapter == null) {
//			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG)
//				.show();
//			finish();
//		} else {
//			int btState = mBluetoothAdapter.getState();
//			
//			if (btState == BluetoothAdapter.STATE_OFF) {
//				mTvStatus.setText("Bluetooth is off");
//				if (!mBluetoothAdapter.isEnabled()) {
//					Intent enableIntent = new Intent(
//							BluetoothAdapter.ACTION_REQUEST_ENABLE);
//					startActivityForResult(enableIntent, REQEUEST_ENABLE_BT);
//				}
//			} else if (btState == BluetoothAdapter.STATE_ON) {
//				initializeBluetooth();
//			}
//		}
//		
//		mBtnMakeDiscoverable.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent discoverableIntent = new Intent(
//						BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//				discoverableIntent.putExtra(
//						BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
//						300);
//				startActivity(discoverableIntent);
//			}
//		});
//		
//		mBtnList.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Intent discoverintent = new Intent(
//						BluetoothMain.this, ListActivity.class);
//				startActivityForResult(discoverintent, LIST_DEVICE);
//			}
//		});
//	}

//	@Override
//	protected void onActivityResult(int requestCode, int resultCode,
//			Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		
//		switch (requestCode) {
//		case REQEUEST_ENABLE_BT:
//			if (resultCode == RESULT_OK) {
//				initializeBluetooth();
//			} else if (resultCode == RESULT_CANCELED) {
//				Toast.makeText(this, "Bluetooth is required", Toast.LENGTH_LONG)
//					.show();
//				finish();
//			}
//			break;
//		case LIST_DEVICE:
//			if (resultCode == RESULT_OK) {
//				String address = data.getStringExtra(
//						ListActivity.EXTRA_DEVICE_ADDRESS);
//				Toast.makeText(this, "Connect to " + address, Toast.LENGTH_SHORT)
//				.show();
//				new ConnectThread(address, mHandler).start();
//			}
//			break;
//		}
//	}

	private void initializeBluetooth() {
//		mTvStatus.setText("Bluetooth is on");
//		mTvDeviceName.setText("My device name: " +
//					mBluetoothAdapter.getName() + " (" +
//					mBluetoothAdapter.getAddress() + ")");
		new AcceptThread(mHandler).start();
	}
	
	private Handler mHandler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case SOCKET_CONNECTED:
				mBluetoothConnection = (ConnectionThread) msg.obj;
				BluetoothDevice device = mBluetoothConnection.mmBluetoothSocket
						.getRemoteDevice();
				mTvStatus.setText("Connected to " + device.getName() +
						" (" + device.getAddress() + ")");
				String hello = "Hello from " + mBluetoothAdapter.getName();
				mBluetoothConnection.write(hello.getBytes());
				addNewMessage(mBluetoothAdapter.getName(), hello);
				break;
			case DATA_RECEIVED:
				String data = (String) msg.obj;
				addNewMessage(mBluetoothConnection.mmBluetoothSocket
						.getRemoteDevice().getName()
						, data);
			}
			return true;
		}
	});
	
	private void addNewMessage(String name, String message) {
		mTvData.append(name + ": " + message + "\n");
	}
	
	class AcceptThread extends Thread {
		private final Handler mmHandler;
		private BluetoothServerSocket mmServerSocket;
		private BluetoothSocket mmSocket = null;
		
		public AcceptThread(Handler handler) {
			mmHandler = handler;
			try {
				mmServerSocket = mBluetoothAdapter
						.listenUsingRfcommWithServiceRecord
						("Bluetooth demo", MY_UUID);
			} catch (IOException e) {
				
			}
		}
		
		public void run() {
			while (true) {
				try {
					mmSocket = mmServerSocket.accept();
					ConnectionThread conn = new ConnectionThread
							(mmSocket, mmHandler);
					mmHandler.obtainMessage(SOCKET_CONNECTED, conn)
						.sendToTarget();
					conn.start();
					mmServerSocket.close();
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
		
		public ConnectionThread(BluetoothSocket socket, Handler handler) {
			super();
			mmBluetoothSocket = socket;
			mmHandler = handler;
			try {
				mmInStream = mmBluetoothSocket.getInputStream();
				mmOutStream = mmBluetoothSocket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			byte[] buffer = new byte[1024];
			int len;
			while (true) {
				try {
					len = mmInStream.read(buffer);
					String data = new String(buffer, 0, len);
					mmHandler.obtainMessage(DATA_RECEIVED, data)
						.sendToTarget();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
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
				ConnectionThread conn = new ConnectionThread(
						mmBluetoothSocket, mmHandler);
				mmHandler.obtainMessage(SOCKET_CONNECTED, conn)
						.sendToTarget();
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
