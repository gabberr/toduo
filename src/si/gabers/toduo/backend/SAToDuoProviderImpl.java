/*    
 * Copyright (c) 2014 Samsung Electronics Co., Ltd.   
 * All rights reserved.   
 *   
 * Redistribution and use in source and binary forms, with or without   
 * modification, are permitted provided that the following conditions are   
 * met:   
 *   
 *     * Redistributions of source code must retain the above copyright   
 *        notice, this list of conditions and the following disclaimer.  
 *     * Redistributions in binary form must reproduce the above  
 *       copyright notice, this list of conditions and the following disclaimer  
 *       in the documentation and/or other materials provided with the  
 *       distribution.  
 *     * Neither the name of Samsung Electronics Co., Ltd. nor the names of its  
 *       contributors may be used to endorse or promote products derived from  
 *       this software without specific prior written permission.  
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS  
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT  
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR  
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT  
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,  
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT  
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,  
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY  
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT  
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE  
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package si.gabers.toduo.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import si.gabers.toduo.activity.MainActivity;
import si.gabers.toduo.model.InterfaceAdapter;
import si.gabers.toduodata.model.ItemIF;
import si.gabers.toduodata.model.ItemRootElement;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

public class SAToDuoProviderImpl extends SAAgent {
	public static final String TAG = "SAToDuoProviderService";

	private SA mAccessory;
	// String[] mProjection = { MediaStore.Images.Media._ID,
	// MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE,
	// MediaStore.Images.Media.DISPLAY_NAME,
	// MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT };

	HashMap<Integer, SAToDuoProviderConnection> mConnectionsMap = null;

	// XML file provided the info
	private static final int GALLERY_CHANNEL_ID = 105;

	// Keeps track of all current registered clients.
	// ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	// List<String> mTb = new ArrayList<String>();
	// String mImgData = "";
	// int mValue = 0; // Holds last value set by a client.

	public static final String ACTION_ADD_DEVICE = "android.appcessory.device.ADD_DEVICE";
	private final String mResult = "failure";

	private final IBinder mBinder = new LocalBinder();

	/**
	 * @author s.amit
	 * 
	 */
	public class LocalBinder extends Binder {
		public SAToDuoProviderImpl getService() {
			return SAToDuoProviderImpl.this;
		}
	}

	/**
	 * 
	 * @param intent
	 * @return IBinder
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
     * 
     */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate of smart view Provider Service");

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
     * 
     */
	public SAToDuoProviderImpl() {
		super(TAG, SAToDuoProviderConnection.class);
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
				final SAToDuoProviderConnection myConnection = (SAToDuoProviderConnection) uThisConnection;
				if (mConnectionsMap == null) {
					mConnectionsMap = new HashMap<Integer, SAToDuoProviderConnection>();
				}
				myConnection.mConnectionId = (int) (System.currentTimeMillis() & 255);
				Log.d(TAG, "onServiceConnection connectionID = "
						+ myConnection.mConnectionId);

				MainActivity.peerId = myConnection.mConnectionId;
				mConnectionsMap.put(myConnection.mConnectionId, myConnection);
				// String toastString = R.string.ConnectionEstablishedMsg + ":"
				// + uThisConnection.getRemotePeerId();
				Toast.makeText(getBaseContext(), "Connected", Toast.LENGTH_LONG)

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
			long channelId, String data) {

		Log.i(TAG, "incoming data on channel = " + channelId + ": from peer ="
				+ connectedPeerId);

		// sendListMsg(connectedPeerId, data);

		// if (data.contains(Model.THUMBNAIL_LIST_RQST)) {
		//
		// sendThumbnails(connectedPeerId, data);
		// } else if (data.contains(Model.DOWNSCALE_IMG_RQST)) {
		// sendDownscaledImage(connectedPeerId, data);
		// } else {
		// Log.e(TAG, "onDataAvailableonChannel: Unknown jSon PDU received");
		// }

		Gson gson = new GsonBuilder().registerTypeAdapter(ItemIF.class,
				new InterfaceAdapter<ItemIF>()).create();
		ItemRootElement irt = new ItemRootElement();
		irt = gson.fromJson(data, ItemRootElement.class);

		MainActivity.root = irt;

		// mImageListReceiverRegistered.onItemListReceived(irt);
	}

	/**
	 * 
	 * @param connectedPeerId
	 */
	public void sendListMsg(String connectedPeerId, String data) {

		// Log.d(TAG, "sendTbListMsg : Enter");
		// final TBListRespMsg uRMessage = new TBListRespMsg(mResult, mReason,
		// mTb.size(), mTb);
		String uJsonStringToSend = data;
		// try {
		// uJsonStringToSend = uRMessage.toJSON().toString();
		// } catch (final JSONException e) {
		//
		// Log.e(TAG, "sendThumbnails() Cannot convert json to string");
		// e.printStackTrace();
		// }
		Log.d(TAG, "tb rsp msg size = " + uJsonStringToSend.length());
		if (mConnectionsMap != null) {
			final SAToDuoProviderConnection uHandler = mConnectionsMap
					.get(Integer.parseInt(connectedPeerId));
			//
			try {
				uHandler.send(GALLERY_CHANNEL_ID, data.getBytes());
			} catch (final IOException e) {
				Log.e(TAG, "I/O Error occured while send");
				e.printStackTrace();
			} catch (final Exception e) {
				Log.e(TAG, "Exception raised:");
				e.printStackTrace();
			}
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

	/**
	 * 
	 * @param error
	 * @param errorCode
	 */
	@Override
	protected void onError(String error, int errorCode) {
		// TODO Auto-generated method stub
		Log.e(TAG, "ERROR: " + errorCode + ": " + error);
	}

	// service connection inner class

	/**
	 * 
	 * @author amit.s5
	 * 
	 */
	public class SAToDuoProviderConnection extends SASocket {

		public static final String TAG = "SAToDuoProviderConnection";
		private int mConnectionId;

		/**
	     * 
	     */
		public SAToDuoProviderConnection() {
			super(SAToDuoProviderConnection.class.getName());
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
			final String strToUpdateUI = new String(data);
			onDataAvailableonChannel(String.valueOf(mConnectionId), channelId, // getRemotePeerId()
					strToUpdateUI);

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

	}

	@Override
	protected void onPeerAgentUpdated(SAPeerAgent peerAgent, int result) {
		Log.i(TAG, "Peer Updated with status : " + result);
	}
}
