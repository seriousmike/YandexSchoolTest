package ru.seriousmike.schooltestyandex.db;

import android.content.ContentValues;
import android.database.Cursor;

import ru.seriousmike.schooltestyandex.data.CategoryItem;

/**
 * Created by SeriousM on 17.08.2015.
 */
public class TblCategory {

	public static final String TABLE_NAME = "categories";

	public static final String FLD_ID = "_id";
	public static final String FLD_YANDEX_ID = "yandex_id";
	public static final String FLD_TITLE = "title";
	public static final String FLD_PARENT_ID = "parent_id";

	public static final String FLD_CHILDREN_COUNT = "children_count";

	public static String getCreateTableQuery() {
		return "CREATE TABLE "+TABLE_NAME+" (" +
				FLD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				FLD_PARENT_ID + " INTEGER, " +
				FLD_YANDEX_ID + " INTEGER, " +
				FLD_TITLE + " TEXT" +
				");";
	}

	public static ContentValues getCVToInsert(CategoryItem item, long parentId) {
//	public static ContentValues getCVToInsert(CategoryItem item, long innerId, long parentId) {
		ContentValues cv = new ContentValues();
//		cv.put(FLD_ID, innerId);
		cv.put(FLD_PARENT_ID, parentId);
		cv.put(FLD_YANDEX_ID, item.id);
		cv.put(FLD_TITLE, item.title);
		return cv;
	}

	public static CategoryItem createFromCursor(Cursor c) {
		return new CategoryItem(
				c.getLong( c.getColumnIndex( TblCategory.FLD_ID ) ),
				c.getLong( c.getColumnIndex( TblCategory.FLD_PARENT_ID ) ),
				c.getLong( c.getColumnIndex( TblCategory.FLD_YANDEX_ID ) ),
				c.getString( c.getColumnIndex( TblCategory.FLD_TITLE ) )
		);
	}

}
