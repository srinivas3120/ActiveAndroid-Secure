//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.activeandroid;

import android.content.ContentValues;
import android.database.Cursor;
import com.activeandroid.Cache;
import com.activeandroid.TableInfo;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;
import com.activeandroid.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import net.sqlcipher.database.SQLiteDatabase;

public abstract class Model {
	@Column(
			name = "Id"
	)
	private Long mId = null;
	private TableInfo mTableInfo = Cache.getTableInfo(this.getClass());

	public Model() {
		Cache.addEntity(this);
	}

	public final Long getId() {
		return this.mId;
	}

	public final void delete() {
		Cache.openDatabase().delete(this.mTableInfo.getTableName(), "Id=?", new String[]{this.getId().toString()});
		Cache.removeEntity(this);
	}

	public final void save() {
		SQLiteDatabase db = Cache.openDatabase();
		ContentValues values = new ContentValues();
		Iterator var4 = this.mTableInfo.getFields().iterator();

		while(var4.hasNext()) {
			Field field = (Field)var4.next();
			String fieldName = this.mTableInfo.getColumnName(field);
			Class fieldType = field.getType();
			field.setAccessible(true);

			try {
				Object e = field.get(this);
				if(e != null) {
					TypeSerializer typeSerializer = Cache.getParserForType(fieldType);
					if(typeSerializer != null) {
						e = typeSerializer.serialize(e);
						if(e != null) {
							fieldType = e.getClass();
						}
					}
				}

				if(e == null) {
					values.putNull(fieldName);
				} else if(!fieldType.equals(Byte.class) && !fieldType.equals(Byte.TYPE)) {
					if(!fieldType.equals(Short.class) && !fieldType.equals(Short.TYPE)) {
						if(!fieldType.equals(Integer.class) && !fieldType.equals(Integer.TYPE)) {
							if(!fieldType.equals(Long.class) && !fieldType.equals(Long.TYPE)) {
								if(!fieldType.equals(Float.class) && !fieldType.equals(Float.TYPE)) {
									if(!fieldType.equals(Double.class) && !fieldType.equals(Double.TYPE)) {
										if(!fieldType.equals(Boolean.class) && !fieldType.equals(Boolean.TYPE)) {
											if(!fieldType.equals(Character.class) && !fieldType.equals(Character.TYPE)) {
												if(fieldType.equals(String.class)) {
													values.put(fieldName, e.toString());
												} else if(ReflectionUtils.isModel(fieldType)) {
													values.put(fieldName, ((Model)e).getId());
												}
											} else {
												values.put(fieldName, e.toString());
											}
										} else {
											values.put(fieldName, (Boolean)e);
										}
									} else {
										values.put(fieldName, (Double)e);
									}
								} else {
									values.put(fieldName, (Float)e);
								}
							} else {
								values.put(fieldName, (Long)e);
							}
						} else {
							values.put(fieldName, (Integer)e);
						}
					} else {
						values.put(fieldName, (Short)e);
					}
				} else {
					values.put(fieldName, (Byte)e);
				}
			} catch (IllegalArgumentException var9) {
				Log.e(var9.getClass().getName(), var9);
			} catch (IllegalAccessException var10) {
				Log.e(var10.getClass().getName(), var10);
			}
		}

		if(this.mId == null) {
			this.mId = Long.valueOf(db.insert(this.mTableInfo.getTableName(), (String)null, values));
		} else {
			db.update(this.mTableInfo.getTableName(), values, "Id=" + this.mId, (String[])null);
		}

	}

	public static void delete(Class<? extends Model> type, long id) {
		(new Delete()).from(type).where("Id=?", new Object[]{Long.valueOf(id)}).execute();
	}

	public static <T extends Model> T load(Class<? extends Model> type, long id) {
		return (new Select()).from(type).where("Id=?", new Object[]{Long.valueOf(id)}).executeSingle();
	}

	public final void loadFromCursor(Class<? extends Model> type, Cursor cursor) {
		Iterator var4 = this.mTableInfo.getFields().iterator();

		while(true) {
			Field field;
			Class fieldType;
			int columnIndex;
			do {
				if(!var4.hasNext()) {
					return;
				}

				field = (Field)var4.next();
				String fieldName = this.mTableInfo.getColumnName(field);
				fieldType = field.getType();
				columnIndex = cursor.getColumnIndex(fieldName);
			} while(columnIndex < 0);

			field.setAccessible(true);

			try {
				boolean e = cursor.isNull(columnIndex);
				TypeSerializer typeSerializer = Cache.getParserForType(fieldType);
				Object value = null;
				if(typeSerializer != null) {
					fieldType = typeSerializer.getDeserializedType();
				}

				if(e) {
					field = null;
				} else if(!fieldType.equals(Byte.class) && !fieldType.equals(Byte.TYPE)) {
					if(!fieldType.equals(Short.class) && !fieldType.equals(Short.TYPE)) {
						if(!fieldType.equals(Integer.class) && !fieldType.equals(Integer.TYPE)) {
							if(!fieldType.equals(Long.class) && !fieldType.equals(Long.TYPE)) {
								if(!fieldType.equals(Float.class) && !fieldType.equals(Float.TYPE)) {
									if(!fieldType.equals(Double.class) && !fieldType.equals(Double.TYPE)) {
										if(!fieldType.equals(Boolean.class) && !fieldType.equals(Boolean.TYPE)) {
											if(!fieldType.equals(Character.class) && !fieldType.equals(Character.TYPE)) {
												if(fieldType.equals(String.class)) {
													value = cursor.getString(columnIndex);
												} else if(ReflectionUtils.isModel(fieldType)) {
													long entityId = cursor.getLong(columnIndex);
													Model entity = Cache.getEntity(fieldType, entityId);
													if(entity == null) {
														entity = (new Select()).from(fieldType).where("Id=?", new Object[]{Long.valueOf(entityId)}).executeSingle();
													}

													value = entity;
												}
											} else {
												value = Character.valueOf(cursor.getString(columnIndex).charAt(0));
											}
										} else {
											value = Boolean.valueOf(cursor.getInt(columnIndex) != 0);
										}
									} else {
										value = Double.valueOf(cursor.getDouble(columnIndex));
									}
								} else {
									value = Float.valueOf(cursor.getFloat(columnIndex));
								}
							} else {
								value = Long.valueOf(cursor.getLong(columnIndex));
							}
						} else {
							value = Integer.valueOf(cursor.getInt(columnIndex));
						}
					} else {
						value = Integer.valueOf(cursor.getInt(columnIndex));
					}
				} else {
					value = Integer.valueOf(cursor.getInt(columnIndex));
				}

				if(typeSerializer != null && !e) {
					value = typeSerializer.deserialize(value);
				}

				if(value != null) {
					field.set(this, value);
				}
			} catch (IllegalArgumentException var15) {
				Log.e(var15.getMessage());
			} catch (IllegalAccessException var16) {
				Log.e(var16.getMessage());
			} catch (SecurityException var17) {
				Log.e(var17.getMessage());
			}
		}
	}

	protected final <E extends Model> List<E> getMany(Class<? extends Model> type, String foreignKey) {
		return (new Select()).from(type).where(this.mTableInfo.getTableName() + "." + foreignKey + "=?", new Object[]{this.getId()}).execute();
	}

	public boolean equals(Object obj) {
		Model other = (Model)obj;
		return this.mId != null && this.mTableInfo.getTableName() == other.mTableInfo.getTableName() && this.mId == other.mId;
	}
}
