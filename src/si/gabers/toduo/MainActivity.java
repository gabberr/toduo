package si.gabers.toduo;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import si.gabers.toduo.backend.SAToDuoProviderImpl;
import si.gabers.toduo.backend.SAToDuoProviderImpl.LocalBinder;
import si.gabers.toduo.model.ImageItemList;
import si.gabers.toduo.model.InterfaceAdapter;
import si.gabers.toduo.model.ItemListInterface;
import si.gabers.toduo.model.ItemRootElement;
import si.gabers.toduo.model.MainItemListAdapter;
import si.gabers.toduo.model.MainItemListModel;
import si.gabers.toduo.model.TextItemList;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import at.fhooe.automate.logger.android.services.heatmap.TouchEvent;
import at.fhooe.automate.logger.android.services.interaction.ViewTouchEvent;

public class MainActivity extends ListActivity  {
@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindService(mConnection);
		mConnection = null;
	}

	//	public static ArrayList<MainItemListModel> items;
	public static ItemRootElement root;
	public static boolean active = false;
	public static int peerId = 0;
//	SAToDuoProviderImpl mBackendService = null;
//	List<String> mDTBListReceived = null;
	
	@Override
    public void onStart() {
       super.onStart();
       
       Intent intent = new Intent(this, SAToDuoProviderImpl.class);
       bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
       if (!mBound) {
           bindService(intent, mConnection, BIND_AUTO_CREATE);
       }
    } 

    @Override
    public void onStop() {
       super.onStop();
       active = false;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//
//		if (savedInstanceState == null) {
//			getFragmentManager().beginTransaction()
//					.add(R.id.container, new MainList()).commit();
//		}
		
		
		
	
		 
		root = new ItemRootElement();
		root.items = new ArrayList<MainItemListModel>();
	    
	    MainItemListModel list1 = new MainItemListModel("List 1");
	    list1.addItem(new TextItemList("Andrej"));
	    list1.addItem(new TextItemList("Miha"));
	    
	    MainItemListModel list2 = new MainItemListModel("List 2");
	    list2.addItem(new TextItemList("Ana"));
	    list2.addItem(new TextItemList("Tina"));
	    
	    MainItemListModel list3 = new MainItemListModel("Image list");
	    list3.addItem(new ImageItemList("ketchup"));
	    list3.addItem(new ImageItemList("soap"));
	    list3.addItem(new ImageItemList("kinder"));
	    
	    
	    root.items.add(list1);
	    root.items.add(list2);
	    root.items.add(list3);
	    
	    MainItemListAdapter Ad = new MainItemListAdapter(this, 
	    		android.R.layout.simple_list_item_1, 
	    		root.items);
	 
	    setListAdapter(Ad);
	    

	}
	
	 private boolean mBound = false;
	 private SAToDuoProviderImpl mService;

	 private ServiceConnection mConnection = new ServiceConnection() {
	        public void onServiceConnected(ComponentName className, IBinder service) {
	            LocalBinder binder = (LocalBinder) service;
	            mService = binder.getService();
	            mBound = true;
	    	}

	        public void onServiceDisconnected(ComponentName className) {
	            mService = null;
	            mBound = false;
	    	}
	    };
	
	

	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
//	    String item = (String) getListAdapter().getItem(position);    
	    Intent intent = new Intent(this, ItemList.class);
	    
	    Bundle b = new Bundle();
	    b.putLong("id", id);
	    intent.putExtras(b);
	    startActivity(intent);

	 
	    	
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
			
			
			Gson gson = new GsonBuilder().registerTypeAdapter(ItemListInterface.class, new InterfaceAdapter<ItemListInterface>())
					.excludeFieldsWithoutExposeAnnotation()
                    .create();
			
        	String json = gson.toJson(root).toString();
			mService.sendListMsg(peerId+"", json);
			return true;
		}
		if(id == R.id.action_add_new) {
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				
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
	
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				  String value = input.getText().toString();
				  
				  MainItemListModel list = new MainItemListModel(value);
				  root.items.add(list);
	//			  getListAdapter().notifyDataSetChanged();
				  // Do something with value!
				  }
				});
	
				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				});
	
				alert.show();
		}
		
		
		
		return super.onOptionsItemSelected(item);
	}

	
	
			

}
