package ru.seriousmike.schooltestyandex.loaders;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.List;

import retrofit.RestAdapter;
import ru.seriousmike.schooltestyandex.R;
import ru.seriousmike.schooltestyandex.data.CategoryItem;
import ru.seriousmike.schooltestyandex.data.CategoryItemNested;
import ru.seriousmike.schooltestyandex.data.YandexTestApi;
import ru.seriousmike.schooltestyandex.db.DbHelper;

/**
 * Осуществляет загрузку и импорт списка категорий с сервера в БД
 */
public class CategoryWebLoader extends AsyncTaskLoader<CategoryWebLoader.Answer> {

	private static final String TAG = "sm_L_CategoryWeb";

	public static final String PREF_SYNCED = "synchronized";
	public static final String PREF_FILE = "common";

	private YandexTestApi mYandexTestApi;

	public CategoryWebLoader(Context context) {
		super(context);
		RestAdapter restAdapter = new RestAdapter.Builder()
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.setEndpoint("https://money.yandex.ru/api")
				.build();
		mYandexTestApi = restAdapter.create( YandexTestApi.class );
	}

	@Override
	public Answer loadInBackground() {
		Log.d(TAG, "loadInBackground");
		Answer answer = new Answer();
		ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if( networkInfo!=null && networkInfo.isConnectedOrConnecting()) {
			Log.d(TAG, "network ok");
			try {
				List<CategoryItemNested> listNested = mYandexTestApi.getCategories();
				if( listNested!=null ) {
					DbHelper dbHelper = DbHelper.getInstance(getContext());
					dbHelper.categoriesReset(listNested);
					getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit().putBoolean(PREF_SYNCED, true).commit();
					List<CategoryItem> listLevel = dbHelper.getCategoryByParentId(0);
					dbHelper.close();
					answer.list = listLevel;
					answer.message = getContext().getString( R.string.status_data_synchronized );
					answer.status = true;
				} else {
					answer.message = getContext().getString(R.string.status_sync_failed );
				}
				Log.i(TAG, "got list "+listNested);
			} catch (Exception e) {
				Log.e(TAG, "bad request", e);
				answer.message = getContext().getString(R.string.status_sync_failed );
			}

		} else {
			answer.message = getContext().getString( R.string.status_connection_failed );
		}

		return answer;
	}

	public static class Answer {
		public boolean status;
		public String message;
		public List<CategoryItem> list;
	}
}
