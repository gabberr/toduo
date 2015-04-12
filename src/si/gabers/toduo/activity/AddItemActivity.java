package si.gabers.toduo.activity;

import si.gabers.toduo.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

public class AddItemActivity extends Activity {

	private EditText et_name;
	private EditText et_description;
	private CheckBox cb_isImageItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.additem_activity_layout);

		et_name = (EditText) findViewById(R.id.ed_item_name);
		et_description = (EditText) findViewById(R.id.ed_item_description);
		cb_isImageItem = (CheckBox) findViewById(R.id.cb_isImageItem);

		et_name.requestFocus();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {

		super.onResume();
	}

	@Override
	protected void onStart() {

		super.onStart();
	}

	@Override
	protected void onStop() {

		super.onStop();
	}

}
