package ru.seriousmike.schooltestyandex.data;


/**
 * Created by SeriousM on 17.08.2015.
 * Категория https://money.yandex.ru/api/categories-list
 */

public class CategoryItem {

	public long innerId; // айдишник в бд
	public long parentId; // айдишник родителя в бд
	public long id; // айдишник от яндекса
	public String title; // заголовок
	public int childrenCount; // количество дочерних элементов в БД

	public CategoryItem(long innerId, long parentId, long id, String title) {
		this.innerId = innerId;
		this.parentId = parentId;
		this.id = id;
		this.title = title;
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{");
		if(id>0) {
			stringBuilder.append("\"id\":").append(id).append(",");
		}
		stringBuilder.append("\"title\":\"").append(title).append("\"");
		stringBuilder.append("}");
		return stringBuilder.toString();
	}

}
