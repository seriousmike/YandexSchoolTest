package ru.seriousmike.schooltestyandex.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.List;

import ru.seriousmike.schooltestyandex.data.CategoryItem;
import ru.seriousmike.schooltestyandex.db.DbHelper;

/**
 * Загружает список категорий из БД
 */
public class CategoryDBLoader extends AsyncTaskLoader<List<CategoryItem>> {

	private static final String TAG = "sm_L_Category";

	private long mParentId;

	public CategoryDBLoader(Context context, long parentId) {
		super(context);
		mParentId = parentId;
	}

	@Override
	public List<CategoryItem> loadInBackground() {
		Log.d(TAG, "loadInBackGround");
		DbHelper dbHelper = DbHelper.getInstance(getContext());
		List<CategoryItem> listLevel = dbHelper.getCategoryByParentId(mParentId);
		dbHelper.close();
		return listLevel;
	}
}
