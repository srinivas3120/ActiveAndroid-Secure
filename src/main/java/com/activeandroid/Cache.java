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
import android.content.SharedPreferences;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import net.sqlcipher.database.SQLiteDatabase;

public final class Cache {
  //////////////////////////////////////////////////////////////////////////////////////
  // PUBLIC CONSTANTS
  //////////////////////////////////////////////////////////////////////////////////////

  public static final int DEFAULT_CACHE_SIZE = 1024;

  //////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE MEMBERS
  //////////////////////////////////////////////////////////////////////////////////////

  private static Context sContext;

  private static ModelInfo sModelInfo;
  private static DatabaseHelper sDatabaseHelper;

  private static DatabaseHelper sSecDatabaseHelper;

  private static LruCache<String, Model> sEntities;

  private static boolean sIsInitialized = false;

  //////////////////////////////////////////////////////////////////////////////////////
  // CONSTRUCTORS
  //////////////////////////////////////////////////////////////////////////////////////

  private Cache() {
  }

  //////////////////////////////////////////////////////////////////////////////////////
  // PUBLIC METHODS
  //////////////////////////////////////////////////////////////////////////////////////

  public static synchronized void initialize(Configuration configuration) {
    if (sIsInitialized) {
      Log.v("ActiveAndroid already initialized.");
      sDatabaseHelper.close();
    }
      sContext = configuration.getContext();
      sModelInfo = new ModelInfo(configuration);
      Log.i("Configuration: " + configuration);
      sDatabaseHelper =
          new DatabaseHelper(configuration.getContext(), configuration.getDatabaseName(),
              configuration.getDatabaseVersion(), configuration.getSqlParser(), "migrations");
      // TODO: It would be nice to override sizeOf here and calculate the memory
      // actually used, however at this point it seems like the reflection
      // required would be too costly to be of any benefit. We'll just set a max
      // object size instead.
      sEntities = new LruCache<String, Model>(configuration.getCacheSize());
      encrypt(configuration.getDatabaseName(), configuration.getNewPassword());
      Log.i("before openDatabase Configuration: " + configuration);
      openDatabase();
      encrypt(configuration.getDatabaseName(), configuration.getNewPassword());
      Log.i("after openDatabase Configuration: " + configuration);

      sIsInitialized = true;
      Log.v("ActiveAndroid initialized successfully.");


    if (configuration.secDatabase()) {
      if (sSecDatabaseHelper != null) {
        sSecDatabaseHelper.close();
      }
      sSecDatabaseHelper =
          new DatabaseHelper(configuration.getContext(), configuration.getSecDatabaseName(),
              configuration.getSecDatabaseVersion(), configuration.getSqlParser(), "secMigrations");
      ActiveAndroid.setSecDb(configuration.getDbLang());
      openSecDatabase();
      encryptSec(configuration.getSecDatabaseName(), configuration.getNewPassword());
      Log.v("Open sec database initialized successfully.");
      ActiveAndroid.attachDb(
          configuration.getContext().getDatabasePath(configuration.getSecDatabaseName()).getPath(),
          getSecPassword(), configuration.getDbLang());
    }
  }

  public static synchronized void clear() {
    sEntities.evictAll();
    Log.v("Cache cleared.");
  }

  public static synchronized void dispose() {
    closeDatabase();
    closeSecDatabase();

    sEntities = null;
    sModelInfo = null;
    sDatabaseHelper = null;
    sSecDatabaseHelper = null;

    sIsInitialized = false;

    Log.v("ActiveAndroid disposed. Call initialize to use library.");
  }

  // Database access

  public static boolean isInitialized() {
    return sIsInitialized;
  }

  public static synchronized SQLiteDatabase openDatabase() {
    return sDatabaseHelper.getWritableDatabase(getPassword());
  }

  public static synchronized void closeDatabase() {
    sDatabaseHelper.close();
  }

  public static synchronized SQLiteDatabase openSecDatabase() {
    return sSecDatabaseHelper.getWritableDatabase(getSecPassword());
  }

  public static synchronized void closeSecDatabase() {
    sSecDatabaseHelper.close();
  }

  // Context access

  public static Context getContext() {
    return sContext;
  }

  // Entity cache

  public static String getIdentifier(Class<? extends Model> type, Long id) {
    return getTableName(type) + "@" + id;
  }

  public static String getIdentifier(Model entity) {
    return getIdentifier(entity.getClass(), entity.getId());
  }

  public static synchronized void addEntity(Model entity) {
    sEntities.put(getIdentifier(entity), entity);
  }

  public static synchronized Model getEntity(Class<? extends Model> type, long id) {
    return sEntities.get(getIdentifier(type, id));
  }

  public static synchronized void removeEntity(Model entity) {
    sEntities.remove(getIdentifier(entity));
  }

  // Model cache

  public static synchronized Collection<TableInfo> getTableInfos() {
    return sModelInfo.getTableInfos();
  }

  public static synchronized TableInfo getTableInfo(Class<? extends Model> type) {
    return sModelInfo.getTableInfo(type);
  }

  public static synchronized TypeSerializer getParserForType(Class<?> type) {
    return sModelInfo.getTypeSerializer(type);
  }

  public static synchronized String getTableName(Class<? extends Model> type) {
    return sModelInfo.getTableInfo(type).getTableName();
  }

  public static String getTableNameWithDb(Class<? extends Model> mType) {
    return (!sModelInfo.getTableInfo(mType).isPrimary() && !ActiveAndroid.SEC_DB_ALIAS.equals(
        ActiveAndroid.DbLang.dbEng.name())) ? ActiveAndroid.SEC_DB_ALIAS + "." + Cache.getTableName(
        mType) : Cache.getTableName(mType);
  }

  public static String getTableNameWithDb(TableInfo mTableInfo) {
    return (!mTableInfo.isPrimary() && !ActiveAndroid.SEC_DB_ALIAS.equals(
        ActiveAndroid.DbLang.dbEng.name())) ? ActiveAndroid.SEC_DB_ALIAS
        + "."
        + mTableInfo.getTableName() : mTableInfo.getTableName();
  }

  public static void encrypt(String dbName, String newPass) {
    Log.e("encrypt newPass: " + newPass);
    File originalFile = sContext.getDatabasePath(dbName);
    if (originalFile.exists() && !TextUtils.isEmpty(newPass)) {
      SharedPreferences settings = sContext.getSharedPreferences("CuroPreFile", 0);
      if (!TextUtils.isEmpty(settings.getString("com.lifeincontrol.dbpass", ""))) {
        Log.e(
            "encrypt password already set: " + settings.getString("com.lifeincontrol.dbpass2", ""));
        return;
      }
      File newFile = null;
      try {
        newFile = File.createTempFile("sqlcipherutils", "tmp", sContext.getCacheDir());
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
      SQLiteDatabase db = SQLiteDatabase.openDatabase(originalFile.getAbsolutePath(), "", null,
          SQLiteDatabase.OPEN_READWRITE);

      db.rawExecSQL(
          String.format("ATTACH DATABASE '%s' AS encrypted KEY '%s';", newFile.getAbsolutePath(),
              newPass));
      db.rawExecSQL("SELECT sqlcipher_export('encrypted')");
      db.rawExecSQL("DETACH DATABASE encrypted;");

      /*// already encrypted database
      final String PRAGMA_KEY = String.format("PRAGMA key = '%s';", oldPass);
      final String PRAGMA_REKEY = String.format("PRAGMA rekey = '%s';", newPass);
      db.rawExecSQL("BEGIN IMMEDIATE TRANSACTION;");
      db.rawExecSQL(PRAGMA_KEY);
      db.rawExecSQL(PRAGMA_REKEY);*/

      int version = db.getVersion();
      db.close();
      db = SQLiteDatabase.openDatabase(newFile.getAbsolutePath(), newPass, null,
          SQLiteDatabase.OPEN_READWRITE);
      db.setVersion(version);
      db.close();
      originalFile.delete();
      newFile.renameTo(originalFile);

      SharedPreferences.Editor editor = settings.edit();
      editor.putString("com.lifeincontrol.dbpass", newPass);
      editor.apply();
      Log.e("encrypt newPass set: " + newPass);
    }
  }

  public static void encryptSec(String dbName, String newPass) {
    Log.e("encryptSec newPass: " + newPass);
    File originalFile = sContext.getDatabasePath(dbName);
    if (originalFile.exists() && !TextUtils.isEmpty(newPass)) {
      SharedPreferences settings = sContext.getSharedPreferences("CuroPreFile", 0);
      if (!TextUtils.isEmpty(settings.getString("com.lifeincontrol.dbpass2", ""))) {
        Log.e("encryptSec password already set: " + settings.getString("com.lifeincontrol.dbpass2",
            ""));
        return;
      }
      File newFile = null;
      try {
        newFile = File.createTempFile("sqlcipherutils", "tmp", sContext.getCacheDir());
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
      SQLiteDatabase db = SQLiteDatabase.openDatabase(originalFile.getAbsolutePath(), "", null,
          SQLiteDatabase.OPEN_READWRITE);

      db.rawExecSQL(
          String.format("ATTACH DATABASE '%s' AS encrypted KEY '%s';", newFile.getAbsolutePath(),
              newPass));
      db.rawExecSQL("SELECT sqlcipher_export('encrypted')");
      db.rawExecSQL("DETACH DATABASE encrypted;");

      /*// already encrypted database
      final String PRAGMA_KEY = String.format("PRAGMA key = '%s';", oldPass);
      final String PRAGMA_REKEY = String.format("PRAGMA rekey = '%s';", newPass);
      db.rawExecSQL("BEGIN IMMEDIATE TRANSACTION;");
      db.rawExecSQL(PRAGMA_KEY);
      db.rawExecSQL(PRAGMA_REKEY);*/

      int version = db.getVersion();
      db.close();
      db = SQLiteDatabase.openDatabase(newFile.getAbsolutePath(), newPass, null,
          SQLiteDatabase.OPEN_READWRITE);
      db.setVersion(version);
      db.close();
      originalFile.delete();
      newFile.renameTo(originalFile);

      SharedPreferences.Editor editor = settings.edit();
      editor.putString("com.lifeincontrol.dbpass2", newPass);
      editor.apply();
      Log.e("encryptSec newPass set: " + newPass);
    }
  }

  private static String getPassword() {
    SharedPreferences settings = sContext.getSharedPreferences("CuroPreFile", 0);
    return settings.getString("com.lifeincontrol.dbpass", "");
  }

  private static String getSecPassword() {
    SharedPreferences settings = sContext.getSharedPreferences("CuroPreFile", 0);
    return settings.getString("com.lifeincontrol.dbpass2", "");
  }
}
