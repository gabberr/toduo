package si.gabers.toduo.model;

import java.util.Iterator;
import java.util.List;

import si.gabers.toduo.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

public class ItemArrayAdapter extends BaseAdapter {

	private final Context context;
	private final List<ItemListInterface> entries;

	ImageView imageView;

	public void add(ItemListInterface item) {
		if (entries.contains(item))
			return;
		entries.add(item);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return entries.size();
	}

	public ItemArrayAdapter(Context context, List<ItemListInterface> entries) {

		this.context = context;
		this.entries = entries;
	}

	@Override
	public ItemListInterface getItem(int arg0) {
		return entries.get(arg0);
	}

	public void setItem(long id, ItemListInterface item) {
		entries.set((int) id, item);
	}

	public void setTickedItem(int arg0, boolean value) {
		entries.get(arg0).setTicked(value);
	}

	public void removeItem(int id) {
		entries.remove(id);
	}

	public void removeTicked() {

		Iterator<ItemListInterface> i = entries.iterator();
		while (i.hasNext()) {
			ItemListInterface s = i.next(); // must be called before you can
											// call i.remove()
			if (s.isTicked())
				i.remove();
		}
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater
				.inflate(R.layout.textitem_layout, parent, false);
		CheckBox chbox = (CheckBox) rowView.findViewById(R.id.checkBoxItem);
		chbox.setText(entries.get(position).toString());
		chbox.setChecked(entries.get(position).isTicked());

		if (entries.get(position).isImageItem()) {

			rowView = inflater
					.inflate(R.layout.imageitem_layout, parent, false);
			chbox = (CheckBox) rowView.findViewById(R.id.checkBoxItem);
			chbox.setText(entries.get(position).toString());
			chbox.setChecked(entries.get(position).isTicked());

			imageView = (ImageView) rowView.findViewById(R.id.imageView1);
			ImageItemList it = (ImageItemList) entries.get(position);

			imageView.setImageBitmap(it.getImage());
			imageView.invalidate();

		}

		return rowView;
	}

}
