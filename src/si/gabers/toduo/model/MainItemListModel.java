package si.gabers.toduo.model;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

public class MainItemListModel {
	@Expose
	String listName;
	@Expose
	ArrayList<ItemListInterface> items;

	public MainItemListModel(String name) {
		listName = name;
		items = new ArrayList<ItemListInterface>();
	}

	public void addItem(ItemListInterface item) {
		items.add(item);
	}

	public void removeItem(ItemListInterface item) {
		items.remove(item);
	}

	@Override
	public String toString() {
		return listName;
	}

	public ArrayList<ItemListInterface> getItemList() {

		return items;
	}

}
