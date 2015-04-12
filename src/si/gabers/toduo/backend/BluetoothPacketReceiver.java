package si.gabers.toduo.backend;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import si.gabers.toduo.activity.MainActivity;
import si.gabers.toduo.model.InterfaceAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import at.fhooe.automate.logger.android.event.GenericPacketWrapperEvent;
import at.fhooe.automate.logger.android.network.GenericPacket;
import at.fhooe.automate.logger.base.action.EventAction;
import at.fhooe.automate.logger.base.event.Event;
import at.fhooe.automate.logger.base.kernel.KernelBase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

public class BluetoothPacketReceiver extends SAAgent {

	private final IBinder mBinder = new LocalBinderAutomate();
	public static final String TAG = "BluetoothPacketReceiverAUToMAteService";
	HashMap<Integer, BluetoothPacketReceiverConnection> mConnectionsMap = null;

	// XML file provided the info
	private static final int CHANNEL_ID = 166;
	private SA mAccessory;

	/**
     * 
     */
	public BluetoothPacketReceiver() {
		super(TAG, BluetoothPacketReceiverConnection.class);
	}

	public class LocalBinderAutomate extends Binder {
		public BluetoothPacketReceiver getService() {
			return BluetoothPacketReceiver.this;
		}
	}

	/*
	 * @param intent
	 * 
	 * @return IBinder
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate BluetootheReceiverImpl");

		mAccessory = new SA();
		try {
			mAccessory.initialize(this);
			boolean isFeatureEnabled = mAccessory
					.isFeatureEnabled(SA.DEVICE_ACCESSORY);
		} catch (SsdkUnsupportedException e) {
			if (processUnsupportedException(e) == true) {
				return;
			}
		} catch (Exception e1) {
			Log.e(TAG, "Cannot initialize SAccessory package.");
			e1.printStackTrace();
			/*
			 * Your application can not use Samsung Accessory SDK. You
			 * application should work smoothly without using this SDK, or you
			 * may want to notify user and close your app gracefully (release
			 * resources, stop Service threads, close UI thread, etc.)
			 */
			stopSelf();
		}

	}

	/**
	 * 
	 * @param peerAgent
	 * @param result
	 */
	@Override
	protected void onFindPeerAgentResponse(SAPeerAgent peerAgent, int result) {

		/*
		 * Log.i(TAG,
		 * "onPeerAgentAvailable: Use this info when you want provider to initiate peer id = "
		 * + peerAgent.getPeerId()); Log.i(TAG,
		 * "onPeerAgentAvailable: Use this info when you want provider to initiate peer name= "
		 * + peerAgent.getAccessory().getName());
		 */
	}

	private boolean processUnsupportedException(SsdkUnsupportedException e) {

		e.printStackTrace();
		int errType = e.getType();

		if (errType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED
				|| errType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
			Log.e(TAG, "This device does not support SAccessory.");
			/*
			 * Your application can not use Samsung Accessory SDK. You
			 * application should work smoothly without using this SDK, or you
			 * may want to notify user and close your app gracefully (release
			 * resources, stop Service threads, close UI thread, etc.)
			 */

			stopSelf();
		} else if (errType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) {
			Log.e(TAG, "You need to install SAccessory package"
					+ " to use this application.");
		} else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) {

			Log.e(TAG, "You need to update SAccessory package"
					+ " to use this application.");
		} else if (errType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) {
			Log.e(TAG,
					"We recommend that you update your"
							+ " Samsung Accessory software before using this application.");
			return false;
		}
		return true;
	}

	/**
     * 
     */
	@Override
	public void onLowMemory() {
		Log.e(TAG, "onLowMemory  has been hit better to do  graceful  exit now");
		// Toast.makeText(getBaseContext(), "!!!onLowMemory!!!",
		// Toast.LENGTH_LONG)
		// .show();
		closeConnection();
		super.onLowMemory();
	}

	/**
     * 
     */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service Stopped.");

	}

	/**
	 * @return boolean
	 */
	public boolean closeConnection() {

		if (mConnectionsMap != null) {
			final List<Integer> listConnections = new ArrayList<Integer>(
					mConnectionsMap.keySet());
			for (final Integer s : listConnections) {
				Log.i(TAG, "KEYS found are" + s);
				mConnectionsMap.get(s).close();
				mConnectionsMap.remove(s);
			}
		}
		return true;
	}

	/**
	 * 
	 * @param uThisConnection
	 * @param result
	 */
	@Override
	protected void onServiceConnectionResponse(SASocket uThisConnection,
			int result) {
		if (result == CONNECTION_SUCCESS) {
			if (uThisConnection != null) {
				final BluetoothPacketReceiverConnection myConnection = (BluetoothPacketReceiverConnection) uThisConnection;
				if (mConnectionsMap == null) {
					mConnectionsMap = new HashMap<Integer, BluetoothPacketReceiverConnection>();
				}
				myConnection.mConnectionId = (int) (System.currentTimeMillis() & 255);
				Log.d(TAG, "onServiceConnection connectionID = "
						+ myConnection.mConnectionId);

				MainActivity.peerId = myConnection.mConnectionId;
				mConnectionsMap.put(myConnection.mConnectionId, myConnection);
				// String toastString = R.string.ConnectionEstablishedMsg + ":"
				// + uThisConnection.getRemotePeerId();
				Toast.makeText(getBaseContext(), "Connected AUToMAte",
						Toast.LENGTH_LONG)

				.show();

				Intent myIntent = new Intent(getApplicationContext(),
						MainActivity.class);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(myIntent);

			} else {
				Log.e(TAG, "SASocket object is null");
			}
		} else {
			Log.e(TAG, "onServiceConnectionResponse result error =" + result);
		}
	}

	/**
	 * 
	 * @param connectedPeerId
	 * @param channelId
	 * @param data
	 */
	private void onDataAvailableonChannel(String connectedPeerId,
			long channelId, GenericPacket data) {

		Log.i(TAG, "incoming data AUTOmaTe on channel = " + channelId
				+ ": from peer =" + connectedPeerId);

		// sendListMsg(connectedPeerId, data);

		// if (data.contains(Model.THUMBNAIL_LIST_RQST)) {
		//
		// sendThumbnails(connectedPeerId, data);
		// } else if (data.contains(Model.DOWNSCALE_IMG_RQST)) {
		// sendDownscaledImage(connectedPeerId, data);
		// } else {
		// Log.e(TAG, "onDataAvailableonChannel: Unknown jSon PDU received");
		// }
		Toast.makeText(getApplicationContext(),
				"automate data received:" + data.toString(), Toast.LENGTH_LONG)
				.show();
		// Log.d(TAG, data);

		GenericPacket gp = data;

		GenericPacketWrapperEvent gpevent = new GenericPacketWrapperEvent(gp);

		new EventAction(KernelBase.getKernel(), gpevent).execute();
		// Gson gson = new GsonBuilder().registerTypeAdapter(
		// ItemListInterface.class,
		// new InterfaceAdapter<ItemListInterface>()).create();
		// ItemRootElement irt = new ItemRootElement();
		// irt = gson.fromJson(data, ItemRootElement.class);
		//
		// MainActivity.root = irt;

		// mImageListReceiverRegistered.onItemListReceived(irt);
	}

	public class BluetoothPacketReceiverConnection extends SASocket {

		public static final String TAG = "BluetoothPacketReceiverConnection";
		private int mConnectionId;

		/**
	     * 
	     */
		public BluetoothPacketReceiverConnection() {
			super(BluetoothPacketReceiverConnection.class.getName());
		}

		/**
		 * 
		 * @param channelId
		 * @param data
		 * @return
		 */
		@Override
		public void onReceive(int channelId, byte[] data) {

			Log.i(TAG, "onReceive ENTER channel = " + channelId);

			// GenericPacket receivedPacket = null;
			// ByteArrayInputStream bis = new ByteArrayInputStream(data);
			// ObjectInput in = null;
			// try {
			// in = new ObjectInputStream(bis);
			// Object o = in.readObject();
			// receivedPacket = (GenericPacket) o;
			// } catch (StreamCorruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (ClassNotFoundException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } finally {
			// try {
			// bis.close();
			// } catch (IOException ex) {
			// // ignore close exception
			// }
			// try {
			// if (in != null) {
			// in.close();
			// }
			// } catch (IOException ex) {
			// // ignore close exception
			// }
			// }

			final String jsonString = new String(data);
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(Event.class,
					new InterfaceAdapter<GenericPacket>());

			Gson gson = gsonBuilder.create();
			GenericPacket receivedPacket = gson.fromJson(jsonString,
					GenericPacket.class);

			// final String strToUpdateUI = new String(data);
			onDataAvailableonChannel(String.valueOf(mConnectionId), channelId, // getRemotePeerId()
					receivedPacket);

		}

		// @Override
		// public void onSpaceAvailable(int channelId) {
		// Log.v(TAG, "onSpaceAvailable: " + channelId);
		// }

		/**
		 * 
		 * @param channelId
		 * @param errorString
		 * @param error
		 */
		@Override
		public void onError(int channelId, String errorString, int error) {
			Log.e(TAG, "Connection is not alive ERROR: " + errorString + "  "
					+ error);
		}

		/**
		 * 
		 * @param errorCode
		 */
		@Override
		public void onServiceConnectionLost(int errorCode) {

			Log.e(TAG, "onServiceConectionLost  for peer = " + mConnectionId
					+ "error code =" + errorCode);
			if (mConnectionsMap != null) {
				mConnectionsMap.remove(mConnectionId);

			}

		}

		public class EventInstanceCreator implements InstanceCreator<Event> {

			@Override
			public Event createInstance(Type arg0) {
				// TODO Auto-generated method stub
				return null;
			}

		}

	}
}
