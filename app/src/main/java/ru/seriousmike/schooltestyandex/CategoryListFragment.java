package ru.seriousmike.schooltestyandex;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import ru.seriousmike.schooltestyandex.data.CategoryItem;
import ru.seriousmike.schooltestyandex.loaders.CategoryDBLoader;


/**
 * Фрагмент списка категорий
 */
public class CategoryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<CategoryItem>> {

	private static final String TAG = "sm_F_CategoryList";

	private static final String ARG_ID = "parent_id";
	private static final String ARG_LEVEL = "list_level";
	private static final String ARG_NAME = "parent_name";
	private static final String ARG_EMPTY = "empty_data";

	private int mLevel;
	private long mParentId;
	private String mParentName;
	private boolean mNoLoadOnStartUp;

	private CategoryAdapter mAdapter;

	private OnCategoryClickListener mListener;

	private View mProgressBar;


	public CategoryListFragment() {}

	public static CategoryListFragment newInstance(int level, long parentId, String parentName, boolean noData) {
		Bundle args = new Bundle();
		args.putInt( ARG_LEVEL, level );
		args.putLong( ARG_ID, parentId );
		args.putString(ARG_NAME, parentName);
		args.putBoolean(ARG_EMPTY, noData);
		CategoryListFragment fragment = new CategoryListFragment();
		fragment.setArguments(args);
		return fragment;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate. bundle " + savedInstanceState);
		mLevel = getArguments().getInt(ARG_LEVEL);
		mParentId = getArguments().getLong(ARG_ID);
		mParentName = getArguments().getString(ARG_NAME);
		mNoLoadOnStartUp = getArguments().getBoolean(ARG_EMPTY, false);
		setRetainInstance(true);
	}

	@Override
	public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
		if(!enter) {
			// воркэраунд для бага платформы в потере анимации бэкстэка фрагментов после переворота экрана
			return AnimationUtils.loadAnimation(getActivity(), R.anim.exit_slide_to_bottom);
		} else {
			return super.onCreateAnimation(transit, enter, nextAnim);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// активность должна имплементировать интерфейс коллбэков
		try {
			mListener = (OnCategoryClickListener) activity;
		} catch (ClassCastException e) {
			throw new RuntimeException("Activity must implement CategoryListFragment.OnCategoryClickListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View layout = inflater.inflate(R.layout.fragment_category_list, container, false);
		mProgressBar = layout.findViewById(R.id.progressBar);

		ListView listView = (ListView) layout.findViewById(R.id.lvCategories);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mListener.onCategoryClick(mAdapter.getItem(position), mLevel);
			}
		});

		// если фрагмент пришёл из состояния retained, то не пересоздаём адаптер
		if(mAdapter==null) mAdapter = new CategoryAdapter();

		listView.setAdapter(mAdapter);
		listView.setEmptyView(layout.findViewById(R.id.emptyView));

		if(!mNoLoadOnStartUp) {
			Loader loader = getLoaderManager().initLoader(0, null, this);
			loader.forceLoad();
			mNoLoadOnStartUp = true;
			setProgressBarVisible(true);
		}

		return layout;
	}

	public String getCategoryName() {
		return mParentName;
	}

	public void setItems(List<CategoryItem> items) {
		mAdapter.clear();
		if(items != null) mAdapter.addAll(items);
		mAdapter.notifyDataSetChanged();
		setProgressBarVisible(false);
	}

	public void setProgressBarVisible(boolean isVisible) {
		mProgressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}

	@Override
	public Loader<List<CategoryItem>> onCreateLoader(int id, Bundle args) {
		return new CategoryDBLoader(getActivity(), mParentId);
	}

	@Override
	public void onLoadFinished(Loader<List<CategoryItem>> loader, List<CategoryItem> data) {
		Log.d(TAG, "onLoadFinished "+data);
		setItems(data);
		mListener.setStatus(getString(R.string.status_data_source_db));
	}

	@Override
	public void onLoaderReset(Loader<List<CategoryItem>> loader) {

	}

	/**
	 * Интерфейс для взаимодействия фрагмента с активностью
	 */
	public interface OnCategoryClickListener {
		/**
		 * обрабатывает клик на элмементе списка
		 * @param item - элемент списка
		 * @param listLevel - уровень списка, передаваемый из активности при создании фрагмента
		 */
		void onCategoryClick(CategoryItem item, int listLevel);

		/**
		 * сообщает строковой статус загружено/загружается
		 * @param status - текст статуса
		 */
		void setStatus(String status);
	}

}
