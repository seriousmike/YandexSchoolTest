package ru.seriousmike.schooltestyandex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import ru.seriousmike.schooltestyandex.data.CategoryItem;

/**
 * Адаптер списка категорий
 */
public class CategoryAdapter extends BaseAdapter {

	private static final String TAG = "sm_A_Category";

	private List<CategoryItem> mItems = new ArrayList<>();

	public void clear() {
		mItems.clear();
	}

	public void addAll(List<CategoryItem> items) {
		mItems.addAll(items);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public CategoryItem getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).innerId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView==null) {
			LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
		}
		CategoryItem item = getItem(position);
		TextView textView = (TextView) convertView;
		textView.setText(item.title);
		if( item.childrenCount>0 ) {
			textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_chevron_right_grey600_18dp, 0);
		} else {
			textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		return convertView;
	}
}
