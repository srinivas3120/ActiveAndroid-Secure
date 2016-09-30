//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.activeandroid.util;

import android.database.Cursor;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.TableInfo;
import com.activeandroid.annotation.Column;
import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;
import com.activeandroid.util.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class SQLiteUtils {
	public static final boolean FOREIGN_KEYS_SUPPORTED;
	private static final HashMap<Class<?>, SQLiteUtils.SQLiteType> TYPE_MAP;

	static {
		FOREIGN_KEYS_SUPPORTED = VERSION.SDK_INT >= 8;
		TYPE_MAP = new HashMap() {
			{
				this.put(Byte.TYPE, SQLiteUtils.SQLiteType.INTEGER);
				this.put(Short.TYPE, SQLiteUtils.SQLiteType.INTEGER);
				this.put(Integer.TYPE, SQLiteUtils.SQLiteType.INTEGER);
				this.put(Long.TYPE, SQLiteUtils.SQLiteType.INTEGER);
				this.put(Float.TYPE, SQLiteUtils.SQLiteType.REAL);
				this.put(Double.TYPE, SQLiteUtils.SQLiteType.REAL);
				this.put(Boolean.TYPE, SQLiteUtils.SQLiteType.INTEGER);
				this.put(Character.TYPE, SQLiteUtils.SQLiteType.TEXT);
				this.put(Byte.class, SQLiteUtils.SQLiteType.INTEGER);
				this.put(Short.class, SQLiteUtils.SQLiteType.INTEGER);
				this.put(Integer.class, SQLiteUtils.SQLiteType.INTEGER);
				this.put(Long.class, SQLiteUtils.SQLiteType.INTEGER);
				this.put(Float.class, SQLiteUtils.SQLiteType.REAL);
				this.put(Double.class, SQLiteUtils.SQLiteType.REAL);
				this.put(Boolean.class, SQLiteUtils.SQLiteType.INTEGER);
				this.put(Character.class, SQLiteUtils.SQLiteType.TEXT);
				this.put(String.class, SQLiteUtils.SQLiteType.TEXT);
			}
		};
	}

	public SQLiteUtils() {
	}

	public static void execSql(String sql) {
		Cache.openDatabase().execSQL(sql);
	}

	public static void execSql(String sql, Object[] bindArgs) {
		Cache.openDatabase().execSQL(sql, bindArgs);
	}

	public static <T extends Model> List<T> rawQuery(Class<? extends Model> type, String sql, String[] selectionArgs) {
		Cursor cursor = Cache.openDatabase().rawQuery(sql, selectionArgs);
		List entities = processCursor(type, cursor);
		cursor.close();
		return entities;
	}

	public static <T extends Model> T rawQuerySingle(Class<? extends Model> type, String sql, String[] selectionArgs) {
		List<T> entities = rawQuery(type, sql, selectionArgs);
		if (entities.size() > 0) {
			return entities.get(0);
		}

		return null;
	}

	public static String createTableDefinition(TableInfo tableInfo) {
		ArrayList definitions = new ArrayList();
		Iterator var3 = tableInfo.getFields().iterator();

		while(var3.hasNext()) {
			Field field = (Field)var3.next();
			String definition = createColumnDefinition(tableInfo, field);
			if(!TextUtils.isEmpty(definition)) {
				definitions.add(definition);
			}
		}

		return String.format("CREATE TABLE IF NOT EXISTS %s (%s);", new Object[]{tableInfo.getTableName(), TextUtils.join(", ", definitions)});
	}

	public static String createColumnDefinition(TableInfo tableInfo, Field field) {
		String definition = null;
		Class type = field.getType();
		String name = tableInfo.getColumnName(field);
		TypeSerializer typeSerializer = Cache.getParserForType(tableInfo.getType());
		Column column = (Column)field.getAnnotation(Column.class);
		if(typeSerializer != null) {
			definition = name + " " + typeSerializer.getSerializedType().toString();
		} else if(TYPE_MAP.containsKey(type)) {
			definition = name + " " + ((SQLiteUtils.SQLiteType)TYPE_MAP.get(type)).toString();
		} else if(ReflectionUtils.isModel(type)) {
			definition = name + " " + SQLiteUtils.SQLiteType.INTEGER.toString();
		}

		if(definition != null) {
			if(column.length() > -1) {
				definition = definition + "(" + column.length() + ")";
			}

			if(name.equals("Id")) {
				definition = definition + " PRIMARY KEY AUTOINCREMENT";
			}

			if(column.notNull()) {
				definition = definition + " NOT NULL ON CONFLICT " + column.onNullConflict().toString();
			}

			if(FOREIGN_KEYS_SUPPORTED && ReflectionUtils.isModel(type)) {
				definition = definition + " REFERENCES " + tableInfo.getTableName() + "(Id)";
				definition = definition + " ON DELETE " + column.onDelete().toString().replace("_", " ");
				definition = definition + " ON UPDATE " + column.onUpdate().toString().replace("_", " ");
			}
		} else {
			Log.e("No type mapping for: " + type.toString());
		}

		return definition;
	}

	private static <T extends Model> List<T> processCursor(Class<? extends Model> type, Cursor cursor) {
		ArrayList entities = new ArrayList();

		try {
			Constructor e = type.getConstructor(new Class[0]);
			if(cursor.moveToFirst()) {
				do {
					Model entity = (Model)e.newInstance(new Object[0]);
					entity.loadFromCursor(type, cursor);
					entities.add(entity);
				} while(cursor.moveToNext());
			}
		} catch (Exception var5) {
			Log.e("Failed to process cursor.", var5);
		}

		return entities;
	}

	public static enum SQLiteType {
		INTEGER,
		REAL,
		TEXT,
		BLOB;

		private SQLiteType() {
		}
	}
}
