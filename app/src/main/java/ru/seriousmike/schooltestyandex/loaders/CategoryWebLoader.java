package ru.seriousmike.schooltestyandex.loaders;

import android.content.Context;
import android.content.SharedPreferences;
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

	//region -------------------------------- Constants and variables

	private static final String TAG = "sm_L_CategoryWeb";
	private static final String ENDPOINT = "https://money.yandex.ru/api";

	public static final String PREF_SYNCED = "synchronized";
	public static final String PREF_FILE = "common";

	private final YandexTestApi mYandexTestApi;

	//endregion

	//region -------------------------------- Constructor

	public CategoryWebLoader(Context context) {
		super(context);
		final RestAdapter restAdapter = new RestAdapter.Builder()
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.setEndpoint(ENDPOINT)
				.build();
		mYandexTestApi = restAdapter.create( YandexTestApi.class );
	}

	//endregion

	//region -------------------------------- Logic methods

	@Override
	public Answer loadInBackground() {
		Log.d(TAG, "loadInBackground");
		final Answer answer;
		if( isNetworkAvailable() ) {
			Log.d(TAG, "network ok");
			answer = processImport();
		} else {
			Log.d(TAG, "network unavailable");
			answer = new Answer();
			answer.setMessage(getContext().getString( R.string.status_connection_failed ));
		}

		return answer;
	}

	private Answer processImport() {

		final Answer answer = new Answer();
		try {
			final List<CategoryItemNested> listNested = mYandexTestApi.getCategories();
			if( listNested != null ) {
				final DbHelper dbHelper = DbHelper.getInstance(getContext());
				dbHelper.categoriesReset(listNested);

				final SharedPreferences preferences = getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
				preferences.edit().putBoolean(PREF_SYNCED, true).commit();

				final List<CategoryItem> listLevel = dbHelper.getCategoryByParentId(0);
				dbHelper.close();

				answer.setList(listLevel);
				answer.setMessage(getContext().getString( R.string.status_data_synchronized ));
				answer.setSuccess(true);
			} else {
				answer.setMessage(getContext().getString(R.string.status_sync_failed ));
			}
			Log.i(TAG, "got list "+listNested);
		} catch (Exception e) {
			Log.e(TAG, "bad request", e);
			answer.setMessage(getContext().getString(R.string.status_sync_failed ));
		}
		return answer;
	}

	private boolean isNetworkAvailable() {
		final ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo!=null && networkInfo.isConnectedOrConnecting();
	}

	//endregion

	//region -------------------------------- Inner classes

	public static class Answer {
		private boolean mSuccess = false;
		private String mMessage;
		private List<CategoryItem> mList;

		public boolean isSuccess() {
			return mSuccess;
		}

		public void setSuccess(boolean success) {
			this.mSuccess = success;
		}

		public String getMessage() {
			return mMessage;
		}

		public void setMessage(String message) {
			this.mMessage = message;
		}

		public List<CategoryItem> getList() {
			return mList;
		}

		public void setList(List<CategoryItem> list) {
			this.mList = list;
		}
	}

	//endregion
}
