package ru.seriousmike.schooltestyandex.data;

import java.util.List;

/**
 * Категории с вложенностью
 * Класс для удобного парсинга Ретрофитом
 */
public class CategoryItemNested extends CategoryItem {

	public List<CategoryItemNested> subs; // список дочерних категорий (используется при импорте)

	public CategoryItemNested(long innerId, long parentId, long id, String title) {
		super(innerId, parentId, id, title);
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{");
		if(id>0) {
			stringBuilder.append("\"id\":").append(id).append(",");
		}
		stringBuilder.append("\"title\":\"").append(title).append("\"");
		if(subs!=null && subs.size()>0) {
			stringBuilder.append(",\"subs\":").append( subs.toString() );
		}
		stringBuilder.append("}");
		return stringBuilder.toString();
	}

}
