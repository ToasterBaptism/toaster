package com.nexus.controllerhub.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.nexus.controllerhub.data.model.Macro;
import com.nexus.controllerhub.data.model.MacroAction;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MacroDao_Impl implements MacroDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Macro> __insertionAdapterOfMacro;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<Macro> __deletionAdapterOfMacro;

  private final EntityDeletionOrUpdateAdapter<Macro> __updateAdapterOfMacro;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMacroById;

  public MacroDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMacro = new EntityInsertionAdapter<Macro>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `macros` (`id`,`name`,`description`,`createdAt`,`updatedAt`,`actions`,`totalDuration`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Macro entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDescription());
        statement.bindLong(4, entity.getCreatedAt());
        statement.bindLong(5, entity.getUpdatedAt());
        final String _tmp = __converters.fromMacroActionList(entity.getActions());
        statement.bindString(6, _tmp);
        statement.bindLong(7, entity.getTotalDuration());
      }
    };
    this.__deletionAdapterOfMacro = new EntityDeletionOrUpdateAdapter<Macro>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `macros` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Macro entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfMacro = new EntityDeletionOrUpdateAdapter<Macro>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `macros` SET `id` = ?,`name` = ?,`description` = ?,`createdAt` = ?,`updatedAt` = ?,`actions` = ?,`totalDuration` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Macro entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDescription());
        statement.bindLong(4, entity.getCreatedAt());
        statement.bindLong(5, entity.getUpdatedAt());
        final String _tmp = __converters.fromMacroActionList(entity.getActions());
        statement.bindString(6, _tmp);
        statement.bindLong(7, entity.getTotalDuration());
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteMacroById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM macros WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMacro(final Macro macro, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMacro.insertAndReturnId(macro);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMacro(final Macro macro, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMacro.handle(macro);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMacro(final Macro macro, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMacro.handle(macro);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMacroById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMacroById.acquire();
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
          __preparedStmtOfDeleteMacroById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Macro>> getAllMacros() {
    final String _sql = "SELECT * FROM macros ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"macros"}, new Callable<List<Macro>>() {
      @Override
      @NonNull
      public List<Macro> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfActions = CursorUtil.getColumnIndexOrThrow(_cursor, "actions");
          final int _cursorIndexOfTotalDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDuration");
          final List<Macro> _result = new ArrayList<Macro>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Macro _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final List<MacroAction> _tmpActions;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfActions);
            _tmpActions = __converters.toMacroActionList(_tmp);
            final long _tmpTotalDuration;
            _tmpTotalDuration = _cursor.getLong(_cursorIndexOfTotalDuration);
            _item = new Macro(_tmpId,_tmpName,_tmpDescription,_tmpCreatedAt,_tmpUpdatedAt,_tmpActions,_tmpTotalDuration);
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
  public Object getMacroById(final long id, final Continuation<? super Macro> $completion) {
    final String _sql = "SELECT * FROM macros WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Macro>() {
      @Override
      @Nullable
      public Macro call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfActions = CursorUtil.getColumnIndexOrThrow(_cursor, "actions");
          final int _cursorIndexOfTotalDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDuration");
          final Macro _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final List<MacroAction> _tmpActions;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfActions);
            _tmpActions = __converters.toMacroActionList(_tmp);
            final long _tmpTotalDuration;
            _tmpTotalDuration = _cursor.getLong(_cursorIndexOfTotalDuration);
            _result = new Macro(_tmpId,_tmpName,_tmpDescription,_tmpCreatedAt,_tmpUpdatedAt,_tmpActions,_tmpTotalDuration);
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
  public Object getMacroCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM macros";
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
  public Flow<List<Macro>> searchMacros(final String searchQuery) {
    final String _sql = "SELECT * FROM macros WHERE name LIKE ? ORDER BY updatedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, searchQuery);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"macros"}, new Callable<List<Macro>>() {
      @Override
      @NonNull
      public List<Macro> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfActions = CursorUtil.getColumnIndexOrThrow(_cursor, "actions");
          final int _cursorIndexOfTotalDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDuration");
          final List<Macro> _result = new ArrayList<Macro>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Macro _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final List<MacroAction> _tmpActions;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfActions);
            _tmpActions = __converters.toMacroActionList(_tmp);
            final long _tmpTotalDuration;
            _tmpTotalDuration = _cursor.getLong(_cursorIndexOfTotalDuration);
            _item = new Macro(_tmpId,_tmpName,_tmpDescription,_tmpCreatedAt,_tmpUpdatedAt,_tmpActions,_tmpTotalDuration);
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
  public Object getMacrosByIds(final List<Long> macroIds,
      final Continuation<? super List<Macro>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM macros WHERE id IN (");
    final int _inputSize = macroIds.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (long _item : macroIds) {
      _statement.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Macro>>() {
      @Override
      @NonNull
      public List<Macro> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfActions = CursorUtil.getColumnIndexOrThrow(_cursor, "actions");
          final int _cursorIndexOfTotalDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "totalDuration");
          final List<Macro> _result = new ArrayList<Macro>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Macro _item_1;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final List<MacroAction> _tmpActions;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfActions);
            _tmpActions = __converters.toMacroActionList(_tmp);
            final long _tmpTotalDuration;
            _tmpTotalDuration = _cursor.getLong(_cursorIndexOfTotalDuration);
            _item_1 = new Macro(_tmpId,_tmpName,_tmpDescription,_tmpCreatedAt,_tmpUpdatedAt,_tmpActions,_tmpTotalDuration);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
