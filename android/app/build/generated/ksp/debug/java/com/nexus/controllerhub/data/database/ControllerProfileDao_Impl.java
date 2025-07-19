package com.nexus.controllerhub.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.nexus.controllerhub.data.model.AnalogSettings;
import com.nexus.controllerhub.data.model.ControllerProfile;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ControllerProfileDao_Impl implements ControllerProfileDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ControllerProfile> __insertionAdapterOfControllerProfile;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<ControllerProfile> __deletionAdapterOfControllerProfile;

  private final EntityDeletionOrUpdateAdapter<ControllerProfile> __updateAdapterOfControllerProfile;

  private final SharedSQLiteStatement __preparedStmtOfDeleteProfileById;

  private final SharedSQLiteStatement __preparedStmtOfDeactivateAllProfiles;

  private final SharedSQLiteStatement __preparedStmtOfActivateProfile;

  public ControllerProfileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfControllerProfile = new EntityInsertionAdapter<ControllerProfile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `controller_profiles` (`id`,`name`,`description`,`isActive`,`createdAt`,`updatedAt`,`buttonMappings`,`analogSettings`,`macroAssignments`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ControllerProfile entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDescription());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getCreatedAt());
        statement.bindLong(6, entity.getUpdatedAt());
        final String _tmp_1 = __converters.fromStringMap(entity.getButtonMappings());
        statement.bindString(7, _tmp_1);
        final String _tmp_2 = __converters.fromAnalogSettings(entity.getAnalogSettings());
        statement.bindString(8, _tmp_2);
        final String _tmp_3 = __converters.fromLongMap(entity.getMacroAssignments());
        statement.bindString(9, _tmp_3);
      }
    };
    this.__deletionAdapterOfControllerProfile = new EntityDeletionOrUpdateAdapter<ControllerProfile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `controller_profiles` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ControllerProfile entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfControllerProfile = new EntityDeletionOrUpdateAdapter<ControllerProfile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `controller_profiles` SET `id` = ?,`name` = ?,`description` = ?,`isActive` = ?,`createdAt` = ?,`updatedAt` = ?,`buttonMappings` = ?,`analogSettings` = ?,`macroAssignments` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ControllerProfile entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDescription());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getCreatedAt());
        statement.bindLong(6, entity.getUpdatedAt());
        final String _tmp_1 = __converters.fromStringMap(entity.getButtonMappings());
        statement.bindString(7, _tmp_1);
        final String _tmp_2 = __converters.fromAnalogSettings(entity.getAnalogSettings());
        statement.bindString(8, _tmp_2);
        final String _tmp_3 = __converters.fromLongMap(entity.getMacroAssignments());
        statement.bindString(9, _tmp_3);
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteProfileById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM controller_profiles WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeactivateAllProfiles = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE controller_profiles SET isActive = 0";
        return _query;
      }
    };
    this.__preparedStmtOfActivateProfile = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE controller_profiles SET isActive = 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertProfile(final ControllerProfile profile,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfControllerProfile.insertAndReturnId(profile);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProfile(final ControllerProfile profile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfControllerProfile.handle(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProfile(final ControllerProfile profile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfControllerProfile.handle(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object setActiveProfile(final long id, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> ControllerProfileDao.DefaultImpls.setActiveProfile(ControllerProfileDao_Impl.this, id, __cont), $completion);
  }

  @Override
  public Object deleteProfileById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteProfileById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteProfileById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deactivateAllProfiles(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeactivateAllProfiles.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeactivateAllProfiles.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object activateProfile(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfActivateProfile.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfActivateProfile.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ControllerProfile>> getAllProfiles() {
    final String _sql = "SELECT * FROM controller_profiles ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"controller_profiles"}, new Callable<List<ControllerProfile>>() {
      @Override
      @NonNull
      public List<ControllerProfile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfButtonMappings = CursorUtil.getColumnIndexOrThrow(_cursor, "buttonMappings");
          final int _cursorIndexOfAnalogSettings = CursorUtil.getColumnIndexOrThrow(_cursor, "analogSettings");
          final int _cursorIndexOfMacroAssignments = CursorUtil.getColumnIndexOrThrow(_cursor, "macroAssignments");
          final List<ControllerProfile> _result = new ArrayList<ControllerProfile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ControllerProfile _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Map<String, String> _tmpButtonMappings;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfButtonMappings);
            _tmpButtonMappings = __converters.toStringMap(_tmp_1);
            final AnalogSettings _tmpAnalogSettings;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfAnalogSettings);
            _tmpAnalogSettings = __converters.toAnalogSettings(_tmp_2);
            final Map<String, Long> _tmpMacroAssignments;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfMacroAssignments);
            _tmpMacroAssignments = __converters.toLongMap(_tmp_3);
            _item = new ControllerProfile(_tmpId,_tmpName,_tmpDescription,_tmpIsActive,_tmpCreatedAt,_tmpUpdatedAt,_tmpButtonMappings,_tmpAnalogSettings,_tmpMacroAssignments);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getProfileById(final long id,
      final Continuation<? super ControllerProfile> $completion) {
    final String _sql = "SELECT * FROM controller_profiles WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ControllerProfile>() {
      @Override
      @Nullable
      public ControllerProfile call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfButtonMappings = CursorUtil.getColumnIndexOrThrow(_cursor, "buttonMappings");
          final int _cursorIndexOfAnalogSettings = CursorUtil.getColumnIndexOrThrow(_cursor, "analogSettings");
          final int _cursorIndexOfMacroAssignments = CursorUtil.getColumnIndexOrThrow(_cursor, "macroAssignments");
          final ControllerProfile _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Map<String, String> _tmpButtonMappings;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfButtonMappings);
            _tmpButtonMappings = __converters.toStringMap(_tmp_1);
            final AnalogSettings _tmpAnalogSettings;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfAnalogSettings);
            _tmpAnalogSettings = __converters.toAnalogSettings(_tmp_2);
            final Map<String, Long> _tmpMacroAssignments;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfMacroAssignments);
            _tmpMacroAssignments = __converters.toLongMap(_tmp_3);
            _result = new ControllerProfile(_tmpId,_tmpName,_tmpDescription,_tmpIsActive,_tmpCreatedAt,_tmpUpdatedAt,_tmpButtonMappings,_tmpAnalogSettings,_tmpMacroAssignments);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getActiveProfile(final Continuation<? super ControllerProfile> $completion) {
    final String _sql = "SELECT * FROM controller_profiles WHERE isActive = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ControllerProfile>() {
      @Override
      @Nullable
      public ControllerProfile call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfButtonMappings = CursorUtil.getColumnIndexOrThrow(_cursor, "buttonMappings");
          final int _cursorIndexOfAnalogSettings = CursorUtil.getColumnIndexOrThrow(_cursor, "analogSettings");
          final int _cursorIndexOfMacroAssignments = CursorUtil.getColumnIndexOrThrow(_cursor, "macroAssignments");
          final ControllerProfile _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Map<String, String> _tmpButtonMappings;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfButtonMappings);
            _tmpButtonMappings = __converters.toStringMap(_tmp_1);
            final AnalogSettings _tmpAnalogSettings;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfAnalogSettings);
            _tmpAnalogSettings = __converters.toAnalogSettings(_tmp_2);
            final Map<String, Long> _tmpMacroAssignments;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfMacroAssignments);
            _tmpMacroAssignments = __converters.toLongMap(_tmp_3);
            _result = new ControllerProfile(_tmpId,_tmpName,_tmpDescription,_tmpIsActive,_tmpCreatedAt,_tmpUpdatedAt,_tmpButtonMappings,_tmpAnalogSettings,_tmpMacroAssignments);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<ControllerProfile> getActiveProfileFlow() {
    final String _sql = "SELECT * FROM controller_profiles WHERE isActive = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"controller_profiles"}, new Callable<ControllerProfile>() {
      @Override
      @Nullable
      public ControllerProfile call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfButtonMappings = CursorUtil.getColumnIndexOrThrow(_cursor, "buttonMappings");
          final int _cursorIndexOfAnalogSettings = CursorUtil.getColumnIndexOrThrow(_cursor, "analogSettings");
          final int _cursorIndexOfMacroAssignments = CursorUtil.getColumnIndexOrThrow(_cursor, "macroAssignments");
          final ControllerProfile _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Map<String, String> _tmpButtonMappings;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfButtonMappings);
            _tmpButtonMappings = __converters.toStringMap(_tmp_1);
            final AnalogSettings _tmpAnalogSettings;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfAnalogSettings);
            _tmpAnalogSettings = __converters.toAnalogSettings(_tmp_2);
            final Map<String, Long> _tmpMacroAssignments;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfMacroAssignments);
            _tmpMacroAssignments = __converters.toLongMap(_tmp_3);
            _result = new ControllerProfile(_tmpId,_tmpName,_tmpDescription,_tmpIsActive,_tmpCreatedAt,_tmpUpdatedAt,_tmpButtonMappings,_tmpAnalogSettings,_tmpMacroAssignments);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getProfileCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM controller_profiles";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ControllerProfile>> searchProfiles(final String searchQuery) {
    final String _sql = "SELECT * FROM controller_profiles WHERE name LIKE ? ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, searchQuery);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"controller_profiles"}, new Callable<List<ControllerProfile>>() {
      @Override
      @NonNull
      public List<ControllerProfile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfButtonMappings = CursorUtil.getColumnIndexOrThrow(_cursor, "buttonMappings");
          final int _cursorIndexOfAnalogSettings = CursorUtil.getColumnIndexOrThrow(_cursor, "analogSettings");
          final int _cursorIndexOfMacroAssignments = CursorUtil.getColumnIndexOrThrow(_cursor, "macroAssignments");
          final List<ControllerProfile> _result = new ArrayList<ControllerProfile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ControllerProfile _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final Map<String, String> _tmpButtonMappings;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfButtonMappings);
            _tmpButtonMappings = __converters.toStringMap(_tmp_1);
            final AnalogSettings _tmpAnalogSettings;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfAnalogSettings);
            _tmpAnalogSettings = __converters.toAnalogSettings(_tmp_2);
            final Map<String, Long> _tmpMacroAssignments;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfMacroAssignments);
            _tmpMacroAssignments = __converters.toLongMap(_tmp_3);
            _item = new ControllerProfile(_tmpId,_tmpName,_tmpDescription,_tmpIsActive,_tmpCreatedAt,_tmpUpdatedAt,_tmpButtonMappings,_tmpAnalogSettings,_tmpMacroAssignments);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
