package si.gabers.toduo.activity;

import java.util.ArrayList;

import si.gabers.toduo.R;
import si.gabers.toduo.backend.BluetoothPacketReceiver;
import si.gabers.toduo.backend.BluetoothPacketReceiver.LocalBinderAutomate;
import si.gabers.toduo.backend.SAToDuoProviderImpl;
import si.gabers.toduo.backend.SAToDuoProviderImpl.LocalBinder;
import si.gabers.toduodata.model.ImageItem;
import si.gabers.toduodata.model.InterfaceAdapter;
import si.gabers.toduodata.model.ItemIF;
import si.gabers.toduodata.model.ItemRootElement;
import si.gabers.toduodata.model.MainActivityItemList;
import si.gabers.toduodata.model.TextItem;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import at.fhooe.automate.logger.android.services.workflow.Workflow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MainActivity extends ListActivity implements OnClickListener {

	// public static ArrayLwist<MainItemListModel> items;
	public static ItemRootElement root;

	public static int peerId = 0;

	// SAToDuoProviderImpl mBackendService = null;
	// List<String> mDTBListReceived = null;

	@Override
	public void onStart() {
		super.onStart();

		Intent intent = new Intent(this, SAToDuoProviderImpl.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		if (!mBound) {
			bindService(intent, mConnection, BIND_AUTO_CREATE);
		}

		Intent intentAutomate = new Intent(this, BluetoothPacketReceiver.class);
		bindService(intentAutomate, mConnectionAutomate,
				Context.BIND_AUTO_CREATE);
		if (!mBoundAutomate) {
			bindService(intentAutomate, mConnectionAutomate, BIND_AUTO_CREATE);
		}

	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		unbindService(mConnection);
		mConnection = null;
		unbindService(mConnectionAutomate);
		mConnectionAutomate = null;
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		super.onResume();
		Workflow.prepareAndCommitState("MainActivity", "MainActivity");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);
		//
		// if (savedInstanceState == null) {
		// getFragmentManager().beginTransaction()
		// .add(R.id.container, new MainList()).commit();
		// }

		root = new ItemRootElement();
		root.items = new ArrayList<MainActivityItemList>();

		MainActivityItemList list1 = new MainActivityItemList("Today");
		list1.addItem(new TextItem("Call Anna"));
		list1.addItem(new TextItem("Send email to Frank"));

		MainActivityItemList list2 = new MainActivityItemList("Places to visit");
		list2.addItem(new TextItem("Barcelona"));
		list2.addItem(new TextItem("Rome"));

		MainActivityItemList list3 = new MainActivityItemList("Groceries");
		list3.addItem(new ImageItem("Ketchup"));
		list3.addItem(new ImageItem("Soap"));
		list3.addItem(new ImageItem("Kinder bueno"));
		list3.addItem(new ImageItem("Lemons"));

		root.items.add(list1);
		root.items.add(list2);
		root.items.add(list3);

		// MainItemListAdapter Ad = new MainItemListAdapter(this,
		// android.R.layout.simple_list_item_1, root.items);

		ArrayAdapter Ad = new ArrayAdapter<MainActivityItemList>(this,
				android.R.layout.simple_list_item_1, root.items);

		setListAdapter(Ad);

	}

	private boolean mBound = false;
	private SAToDuoProviderImpl mService;

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mService = null;
			mBound = false;
		}
	};

	private boolean mBoundAutomate = false;
	private BluetoothPacketReceiver mServiceAutomate;

	private ServiceConnection mConnectionAutomate = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinderAutomate binder = (LocalBinderAutomate) service;
			mServiceAutomate = binder.getService();
			mBoundAutomate = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mServiceAutomate = null;
			mBoundAutomate = false;
		}
	};

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// String item = (String) getListAdapter().getItem(position);

		Context context = this.getApplicationContext();
		Intent intent = new Intent(context, OpenListActivity.class);

		Bundle b = new Bundle();
		b.putLong("id", id);
		intent.putExtras(b);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		(context).startActivity(intent);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_sync) {

			Button button = new Button(this);
			button.setText("Sync");
			button.setId(R.id.sync);
			onClick(button);

			Gson gson = new GsonBuilder()
					.registerTypeAdapter(ItemIF.class,
							new InterfaceAdapter<ItemIF>())
					.excludeFieldsWithoutExposeAnnotation().create();

			String json = gson.toJson(root).toString();
			mService.sendListMsg(peerId + "", json);
			return true;
		}
		if (id == R.id.action_add_new) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			Button button = new Button(this);
			button.setText("AddList");
			button.setId(R.id.addList);
			onClick(button);

			LinearLayout ll = new LinearLayout(this);
			ll.setOrientation(1);

			alert.setTitle("Enter list name");

			// Set an EditText view to get user input
			final EditText input = new EditText(this);

			input.setText("");

			ll.addView(input);

			alert.setView(ll);

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();

							MainActivityItemList list = new MainActivityItemList(
									value);
							root.items.add(list);
							// getListAdapter().notifyDataSetChanged();
							// Do something with value!
						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// Canceled.
						}
					});

			alert.show();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		System.out.println(" view" + v.getId());

	}
}
