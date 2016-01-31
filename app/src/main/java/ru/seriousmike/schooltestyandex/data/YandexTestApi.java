package ru.seriousmike.schooltestyandex.data;

import java.util.List;

import retrofit.http.GET;

/**
 * Yandex api interface for Retrofit
 * Created by SeriousM on 17.08.2015.
 */
public interface YandexTestApi {
	@GET("/categories-list")
	List<CategoryItemNested> getCategories();
}
