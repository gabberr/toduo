package si.gabers.toduo.activity;

import si.gabers.toduo.R;
import si.gabers.toduo.model.ImageItemList;
import si.gabers.toduo.model.ItemArrayAdapter;
import si.gabers.toduo.model.MainItemListModel;
import si.gabers.toduo.model.TextItemList;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import at.fhooe.automate.logger.android.services.workflow.Workflow;

public class ItemList extends ListActivity implements OnItemLongClickListener,
		OnItemClickListener, OnClickListener, OnLongClickListener {
	MainItemListModel ml;
	static final int UPDATE_LIST = 333;

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		super.onResume();
		Workflow.prepareAndCommitState("OpenListActivity", "OpenListActivity");
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onCreate(Bundle _bundle) {
		super.onCreate(_bundle);

		// get items from main by passed id
		Bundle extras = getIntent().getExtras();
		long id = extras.getLong("id");

		ml = MainActivity.root.items.get((int) id);
		getListView().requestFocus();
		this.setTitle(ml.toString());
		ItemArrayAdapter adapter = new ItemArrayAdapter(this, ml.getItemList());
		setListAdapter(adapter);

		// add click listeners
		this.getListView().setLongClickable(true);
		this.getListView().setOnItemClickListener(this);
		this.getListView().setOnItemLongClickListener(this);

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View v, int arg2,
			long id) {

		// rename an item

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		CheckBox chbox = (CheckBox) v.findViewById(R.id.checkBoxItem);

		onLongClick(chbox);

		input.setText(chbox.getText());

		alert.setView(input);
		final int idd = (int) id;
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

				String value = input.getText().toString();

				ItemArrayAdapter adapter = (ItemArrayAdapter) getListAdapter();
				boolean isImage = adapter.getItem(idd).isImageItem();
				if (isImage)
					adapter.setItem(idd, new ImageItemList(value, adapter));
				else
					adapter.setItem(idd, new TextItemList(value));
				adapter.notifyDataSetChanged();

			}
		});

		alert.setNegativeButton("Delete",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						ItemArrayAdapter adapter = (ItemArrayAdapter) getListAdapter();
						adapter.removeItem(idd);
						adapter.notifyDataSetChanged();
						// Deleted.
					}
				});
		alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();

		return true;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_add_new) {

			Button button = new Button(this);
			button.setText("AddItem");
			button.setId(R.id.addItem);

			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			onClick(button);

			alert.setTitle("Item name");
			// alert.setMessage("Message");

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			final CheckBox checkBox = new CheckBox(this);

			input.setText("");
			checkBox.setText("Get image");

			LinearLayout linearLayout = new LinearLayout(this);
			linearLayout.setOrientation(1);
			linearLayout.addView(input);
			linearLayout.addView(checkBox);
			alert.setView(linearLayout);

			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();

							ItemArrayAdapter adapter = (ItemArrayAdapter) getListAdapter();

							boolean isImage = checkBox.isChecked();
							if (isImage)
								adapter.add(new ImageItemList(value, adapter));
							else
								adapter.add(new TextItemList(value));

							adapter.notifyDataSetChanged();
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

			return true;
		}

		if (id == R.id.action_remove_ticked) {
			Button button = new Button(this);
			button.setText("RemoveTicked");
			button.setId(R.id.clearItems);
			onClick(button);
			ItemArrayAdapter adapter = (ItemArrayAdapter) getListAdapter();
			adapter.removeTicked();
			adapter.notifyDataSetChanged();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int arg2, long id) {
		CheckBox chbox = (CheckBox) v.findViewById(R.id.checkBoxItem);
		chbox.toggle();
		boolean value = chbox.isChecked();
		ItemArrayAdapter adapter = (ItemArrayAdapter) getListAdapter();
		adapter.setTickedItem(arg2, value);
		onClick(chbox);

	}

	/**
	 * For automate framework injection
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return true;
	}

}
