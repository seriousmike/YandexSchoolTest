package ru.seriousmike.schooltestyandex.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.seriousmike.schooltestyandex.data.CategoryItem;
import ru.seriousmike.schooltestyandex.data.CategoryItemNested;

/**
 * Created by SeriousM on 17.08.2015.
 * Синглтон для работы с БД
 */
public class DbHelper extends SQLiteOpenHelper {

	private static final String TAG = "sm_Db";

	private static final int VERSION = 1;
	private static final String DB_NAME = "yandex_test_db.sqlite";

	private static DbHelper sInstance;

	private DbHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	public static synchronized DbHelper getInstance(Context context) {
		if( sInstance == null ) {
			sInstance = new DbHelper( context.getApplicationContext() );
		}
		return sInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TblCategory.getCreateTableQuery());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	/**
	 * олучает список дочерних категорий по айдишнику родителя
	 * @param parentId - иденитификатор родителя в БД ( 0 - корневой уровень )
	 * @return список категорий
	 */
	public List<CategoryItem> getCategoryByParentId(long parentId) {
		Cursor c = getReadableDatabase().query( TblCategory.TABLE_NAME, null, TblCategory.FLD_PARENT_ID+"="+parentId, null, null, null, TblCategory.FLD_ID );
		List<CategoryItem> list = new ArrayList<>();
		if(c.getCount() > 0 ) {
			c.moveToFirst();
			while ( !c.isAfterLast() ) {
				list.add( TblCategory.createFromCursor(c) );
				c.moveToNext();
			}
			countChildren(list);
		}
		c.close();
		return list;
	}

	/**
	 * подсчитывает количество детей для списка категорий
	 * результат будет записан в CategoryItem.childrenCount аждого элемента списка
	 * @param itemList категории для подсчёта
	 */
	public void countChildren(List<CategoryItem> itemList) {
		if(itemList.size()==0) return;

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < itemList.size(); i++) {
			sb.append( itemList.get(i).innerId );
			if( i < itemList.size()-1 ) {
				sb.append(",");
			}
		}
		Log.i(TAG, sb.toString());
		String sql = "SELECT "+TblCategory.FLD_PARENT_ID+", COUNT(*) FROM "+TblCategory.TABLE_NAME+" WHERE "+TblCategory.FLD_PARENT_ID+" IN ("+sb.toString()+") GROUP BY "+TblCategory.FLD_PARENT_ID;
		Cursor c = getReadableDatabase().rawQuery(sql, null);
		if(c.getCount()>0) {
			c.moveToFirst();
			HashMap<Long, Integer> counts = new HashMap<>();
			while ( !c.isAfterLast() ) {
				counts.put(c.getLong(0), c.getInt(1));
				c.moveToNext();
			}
			for(CategoryItem item : itemList) {
				item.childrenCount = ( counts.containsKey( item.innerId ) ? counts.get( item.innerId ) : 0 );
			}
		}
		c.close();
	}



	/**
	 * переписывает список категорий в бд
	 * @param categoryItems - вложенный список категорий для записи
	 */
	public void categoriesReset(List<CategoryItemNested> categoryItems) {
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.beginTransaction();
			db.delete(TblCategory.TABLE_NAME, null, null);
			insertCategoryItem(db, categoryItems, 0);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * отправляет Insert-запрос в БД для каждого элемента списка
	 * @param db - WriteableDatabase
	 * @param nestedItemList - вложенный список для записи
	 * @param parentId - айдишник родителя
	 */
	private void insertCategoryItem(SQLiteDatabase db, List<CategoryItemNested> nestedItemList, long parentId) {
		for(CategoryItemNested item : nestedItemList) {
			long insertedId = db.insert(TblCategory.TABLE_NAME, null, TblCategory.getCVToInsert(item, parentId));
			if(item.subs!=null && item.subs.size() > 0) {
				insertCategoryItem(db, item.subs, insertedId);
			}
		}
	}
}
