package com.activeandroid;

/*
 * Copyright (C) 2010 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import com.activeandroid.util.Log;
import net.sqlcipher.database.SQLiteDatabase;

public final class ActiveAndroid {
  public static String SEC_DB_ALIAS = DbLang.dbEng.name();
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public static void initialize(Context context) {
		initialize(new Configuration.Builder(context).create());
	}

	public static void initialize(Configuration configuration) {
		initialize(configuration, true);
	}

	public static void initialize(Context context, boolean loggingEnabled) {
		initialize(new Configuration.Builder(context).create(), loggingEnabled);
	}

	public static void initialize(Configuration configuration, boolean loggingEnabled) {
		// Set logging enabled first
		setLoggingEnabled(loggingEnabled);
		Cache.initialize(configuration);

	}

	public static void clearCache() {
		Cache.clear();
	}

	public static void dispose() {
		Cache.dispose();
	}

	public static void setLoggingEnabled(boolean enabled) {
		Log.setEnabled(enabled);
	}

	public static SQLiteDatabase getDatabase() {
		return Cache.openDatabase();
	}

	public static void beginTransaction() {
		try {
			Cache.openDatabase().beginTransaction();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public static void endTransaction() {
		try {
			Cache.openDatabase().endTransaction();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public static void setTransactionSuccessful() {
		try {
			Cache.openDatabase().setTransactionSuccessful();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public static boolean inTransaction() {
		return Cache.openDatabase().inTransaction();
	}

	public static void execSQL(String sql) {
		Cache.openDatabase().execSQL(sql);
	}

	public static void execSQL(String sql, Object[] bindArgs) {
		Cache.openDatabase().execSQL(sql, bindArgs);
	}

  public static void setSecDb(DbLang alias){
    SEC_DB_ALIAS=alias.name();
  }
  public static void attachDb(String path, DbLang alias) {
    attachDb(path,"",alias);
  }

  public static void attachDb(String path, String password,DbLang alias) {
    try {
			Log.i("before detach");
      detachDb(alias);
			Log.i("after detach");
      Cache.openDatabase().execSQL("ATTACH DATABASE '" + path + "' AS "+alias+" KEY '" + password + "'");
			Log.i("attached successfully");
    } catch (Exception e) {
			Log.i("attach failed");
      e.printStackTrace();
    }
  }

  public static void detachDb(DbLang alias) {
    try {
      Cache.openDatabase().execSQL("DETACH DATABASE "+alias);
    } catch (Exception e) {
			Log.i("detachDb exception");
      e.printStackTrace();
    }
  }

  public enum DbLang {
    dbEng , dbHi
  }

}
