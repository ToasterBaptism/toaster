package com.nexus.controllerhub.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ControllerDatabase_Impl extends ControllerDatabase {
  private volatile ControllerProfileDao _controllerProfileDao;

  private volatile MacroDao _macroDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `controller_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `buttonMappings` TEXT NOT NULL, `analogSettings` TEXT NOT NULL, `macroAssignments` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `macros` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `actions` TEXT NOT NULL, `totalDuration` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '7951781107eabe898ba96a52e124c2d0')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `controller_profiles`");
        db.execSQL("DROP TABLE IF EXISTS `macros`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsControllerProfiles = new HashMap<String, TableInfo.Column>(9);
        _columnsControllerProfiles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsControllerProfiles.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsControllerProfiles.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsControllerProfiles.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsControllerProfiles.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsControllerProfiles.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsControllerProfiles.put("buttonMappings", new TableInfo.Column("buttonMappings", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsControllerProfiles.put("analogSettings", new TableInfo.Column("analogSettings", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsControllerProfiles.put("macroAssignments", new TableInfo.Column("macroAssignments", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysControllerProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesControllerProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoControllerProfiles = new TableInfo("controller_profiles", _columnsControllerProfiles, _foreignKeysControllerProfiles, _indicesControllerProfiles);
        final TableInfo _existingControllerProfiles = TableInfo.read(db, "controller_profiles");
        if (!_infoControllerProfiles.equals(_existingControllerProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "controller_profiles(com.nexus.controllerhub.data.model.ControllerProfile).\n"
                  + " Expected:\n" + _infoControllerProfiles + "\n"
                  + " Found:\n" + _existingControllerProfiles);
        }
        final HashMap<String, TableInfo.Column> _columnsMacros = new HashMap<String, TableInfo.Column>(7);
        _columnsMacros.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMacros.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMacros.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMacros.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMacros.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMacros.put("actions", new TableInfo.Column("actions", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMacros.put("totalDuration", new TableInfo.Column("totalDuration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMacros = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMacros = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMacros = new TableInfo("macros", _columnsMacros, _foreignKeysMacros, _indicesMacros);
        final TableInfo _existingMacros = TableInfo.read(db, "macros");
        if (!_infoMacros.equals(_existingMacros)) {
          return new RoomOpenHelper.ValidationResult(false, "macros(com.nexus.controllerhub.data.model.Macro).\n"
                  + " Expected:\n" + _infoMacros + "\n"
                  + " Found:\n" + _existingMacros);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "7951781107eabe898ba96a52e124c2d0", "122f93830713207cbc32000576fbbd40");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "controller_profiles","macros");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `controller_profiles`");
      _db.execSQL("DELETE FROM `macros`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ControllerProfileDao.class, ControllerProfileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MacroDao.class, MacroDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ControllerProfileDao profileDao() {
    if (_controllerProfileDao != null) {
      return _controllerProfileDao;
    } else {
      synchronized(this) {
        if(_controllerProfileDao == null) {
          _controllerProfileDao = new ControllerProfileDao_Impl(this);
        }
        return _controllerProfileDao;
      }
    }
  }

  @Override
  public MacroDao macroDao() {
    if (_macroDao != null) {
      return _macroDao;
    } else {
      synchronized(this) {
        if(_macroDao == null) {
          _macroDao = new MacroDao_Impl(this);
        }
        return _macroDao;
      }
    }
  }
}
