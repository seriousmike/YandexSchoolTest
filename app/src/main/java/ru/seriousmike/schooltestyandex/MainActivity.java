package ru.seriousmike.schooltestyandex;

import android.content.SharedPreferences;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import ru.seriousmike.schooltestyandex.data.CategoryItem;
import ru.seriousmike.schooltestyandex.loaders.CategoryWebLoader;

public class MainActivity extends AppCompatActivity implements
		LoaderManager.LoaderCallbacks<CategoryWebLoader.Answer>, CategoryListFragment.OnCategoryClickListener {

	//region -------------------------------- Variables and constants

	private static final String TAG = "sm_A_Main";

	private static final int LOADER_ID_WEB = 1;

	private static final String FRAGMENT_TAG_PREFIX = "list-level-";

	private static final String STATE_LEVEL = "sis_level";
	private static final String STATE_TITLE = "sis_title";
	private static final String STATE_SUBTITLE = "sis_subtitle";
	private static final String STATE_LOADING = "sis_loading";

	private int mLevel = 0;

	private View mProgressBar;

	//endregion

	//region -------------------------------- Lifecycle methods

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mProgressBar = findViewById(R.id.progressBar);

		final FragmentManager fm = getSupportFragmentManager();
		fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				final Fragment fragment = fm.findFragmentByTag(FRAGMENT_TAG_PREFIX + fm.getBackStackEntryCount());
				if(fragment != null) {
					setTitle(((CategoryListFragment) fragment).getCategoryName());
				}
			}
		});

		if(savedInstanceState!=null) {
			mLevel = savedInstanceState.getInt(STATE_LEVEL);
			setTitle(savedInstanceState.getString(STATE_TITLE));
			setStatus(savedInstanceState.getString(STATE_SUBTITLE));
			mProgressBar.setVisibility( savedInstanceState.getBoolean(STATE_LOADING, false) ? View.VISIBLE : View.GONE );
			getSupportLoaderManager().initLoader(LOADER_ID_WEB, null, this);
			setMenuNavigation();
		} else {
			initialLoad();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() ) {
			case android.R.id.home: // обрабатываем взаимодействие с бэкстэком фрагментов
				removeFragment();
				return true;
			case R.id.menuSync: // апускаем синхронизацию с сервером по клику на MenuItem
				synchronizeCategoriesWithApi();
				return true;
			default: return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		// самостоятельно обрабатываем взаимодействие с бэк-стэком фрагментов
		if(!removeFragment()) {
			finish(); // если нет фрагмента на удаление из бэкстэка, завершаем активность
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_LEVEL, mLevel);
		outState.putString(STATE_TITLE, getTitle().toString());
		outState.putBoolean(STATE_LOADING, mProgressBar.getVisibility() == View.VISIBLE);
		if(getSupportActionBar() != null && getSupportActionBar().getSubtitle() != null) {
			outState.putString(STATE_SUBTITLE, getSupportActionBar().getSubtitle().toString() );
		}
		super.onSaveInstanceState(outState);
		Log.i(TAG, "onSaveInstanceState(outState); " + outState );
	}

	//endregion

	//region -------------------------------- UI methods

	/**
	 * устанавливает контрол возврата на уровень вверх в меню
	 */
	private void setMenuNavigation() {
		// показываем навигацию назад, только для второго и далее списков
		// в ином случае скрываем навигацию вверх в меню
		if(getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled( mLevel > 1);
		}
	}

	/**
	 * создаёт фрагмент списка
	 * @param parentId - айдишник элемента-родителя, чьи дочерние элементы будут показываться в списке
	 * @param parentName - название категории для тайтла
	 * @param forceEmpty - если true, не запускает загрузку элементов
	 */
	private void showLevel(long parentId, String parentName, boolean forceEmpty) {
		mLevel++;
		final FragmentManager fm = getSupportFragmentManager();
		fm.beginTransaction()
				.setCustomAnimations(R.anim.enter_slide_from_bottom, R.anim.idle, 0, R.anim.exit_slide_to_bottom)
				.add(R.id.container, CategoryListFragment.newInstance(mLevel, parentId, parentName, forceEmpty), FRAGMENT_TAG_PREFIX + mLevel)
				.addToBackStack(FRAGMENT_TAG_PREFIX + mLevel)
				.commit();

		setMenuNavigation();
	}

	/**
	 * удаляет фрагмент из бэкстэка
	 * @return - true если фрагмент был удалённ
	 */
	private boolean removeFragment() {
		if(mLevel>1) {
			final FragmentManager fm = getSupportFragmentManager();
			fm.popBackStack();
			mLevel--;
			setMenuNavigation();
			return true;
		}
		return false;
	}

	@Override
	public void onCategoryClick(CategoryItem item, int listLevel) {
		if(listLevel==mLevel && item.childrenCount>0) {
			showLevel(item.innerId, item.title, false);
		}
	}

	@Override
	public void setStatus(String status) {
		if(getSupportActionBar() != null) {
			getSupportActionBar().setSubtitle(status);
		}
	}

	public void setStatus(@StringRes int stringId) {
		setStatus(getString(stringId));
	}

	//endregion

	//region -------------------------------- Loaders and api-oriented methods

	/**
	 * стартует загрузку из бд или синхронизацию с сервером, если синхронизации ешё не было
	 */
	private void initialLoad() {
		final SharedPreferences preferences = getSharedPreferences(CategoryWebLoader.PREF_FILE, MODE_PRIVATE);
		if(preferences.getBoolean( CategoryWebLoader.PREF_SYNCED, false )) {
			setStatus(R.string.status_loading);
			showLevel(0L, getString(R.string.categories), false);
		} else {
			showLevel(0L, getString(R.string.categories), true);
			synchronizeCategoriesWithApi();
		}
	}

	/**
	 * запускает синхронизацию с сервером yandex
	 */
	private void synchronizeCategoriesWithApi() {
		mProgressBar.setVisibility(View.VISIBLE);
		final FragmentManager fm = getSupportFragmentManager();
		Log.i(TAG, "Level " + mLevel);
		while( mLevel > 1 ) {
			removeFragment();
		}
		final Fragment fragment = fm.findFragmentByTag(FRAGMENT_TAG_PREFIX + "1");
		if(fragment!=null) {
			((CategoryListFragment)fragment).setProgressBarVisible(true);
		}

		setStatus(R.string.status_synchronizing);
		Loader loader = getSupportLoaderManager().getLoader(LOADER_ID_WEB);
		if(loader==null) {
			loader = getSupportLoaderManager().initLoader(LOADER_ID_WEB, null, this);
		}
		loader.forceLoad();
	}


	@Override
	public Loader<CategoryWebLoader.Answer> onCreateLoader(int id, Bundle args) {
		return new CategoryWebLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<CategoryWebLoader.Answer> loader, CategoryWebLoader.Answer data) {
		Log.i(TAG, "onLoadFinished " + data);
		setStatus(data.getMessage());

		final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_PREFIX + "1");
		if(data.isSuccess()) { // если всё ок, получаем список из лоадера, после импорта в бд, полученные данные передаём созданном фрагменту первого уровня
			((CategoryListFragment)fragment).setItems(data.getList());
		} else {
			((CategoryListFragment)fragment).setProgressBarVisible(false);
		}

		mProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<CategoryWebLoader.Answer> loader) {
		//do nothing
	}

	//endregion

}
