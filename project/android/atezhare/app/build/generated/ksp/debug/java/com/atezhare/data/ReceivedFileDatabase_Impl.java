package com.atezhare.data;

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
public final class ReceivedFileDatabase_Impl extends ReceivedFileDatabase {
  private volatile ReceivedFileDao _receivedFileDao;

  private volatile SentFileDao _sentFileDao;

  private volatile LibrarianDao _librarianDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `received_files` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fileId` TEXT NOT NULL, `fileName` TEXT NOT NULL, `mimeType` TEXT NOT NULL, `fileSize` INTEGER NOT NULL, `localPath` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `senderId` TEXT NOT NULL, `receivedAt` INTEGER NOT NULL, `isViewed` INTEGER NOT NULL, `mode` TEXT NOT NULL, `isDeleted` INTEGER NOT NULL, `expiresAt` INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sent_files` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fileId` TEXT NOT NULL, `fileName` TEXT NOT NULL, `mimeType` TEXT NOT NULL, `fileSize` INTEGER NOT NULL, `localPath` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `receiverId` TEXT NOT NULL, `sentAt` INTEGER NOT NULL, `mode` TEXT NOT NULL, `expiresAt` INTEGER, `isActive` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `librarian` (`fileName` TEXT NOT NULL, `localPath` TEXT NOT NULL, `fileId` TEXT NOT NULL, `deleteAt` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`fileName`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '97624eb536e164aa828cc33eabd794f5')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `received_files`");
        db.execSQL("DROP TABLE IF EXISTS `sent_files`");
        db.execSQL("DROP TABLE IF EXISTS `librarian`");
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
        final HashMap<String, TableInfo.Column> _columnsReceivedFiles = new HashMap<String, TableInfo.Column>(13);
        _columnsReceivedFiles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedFiles.put("fileId", new TableInfo.Column("fileId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedFiles.put("fileName", new TableInfo.Column("fileName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedFiles.put("mimeType", new TableInfo.Column("mimeType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedFiles.put("fileSize", new TableInfo.Column("fileSize", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedFiles.put("localPath", new TableInfo.Column("localPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedFiles.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedFiles.put("senderId", new TableInfo.Column("senderId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedFiles.put("receivedAt", new TableInfo.Column("receivedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedFiles.put("isViewed", new TableInfo.Column("isViewed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedFiles.put("mode", new TableInfo.Column("mode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedFiles.put("isDeleted", new TableInfo.Column("isDeleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedFiles.put("expiresAt", new TableInfo.Column("expiresAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReceivedFiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReceivedFiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReceivedFiles = new TableInfo("received_files", _columnsReceivedFiles, _foreignKeysReceivedFiles, _indicesReceivedFiles);
        final TableInfo _existingReceivedFiles = TableInfo.read(db, "received_files");
        if (!_infoReceivedFiles.equals(_existingReceivedFiles)) {
          return new RoomOpenHelper.ValidationResult(false, "received_files(com.atezhare.data.ReceivedFile).\n"
                  + " Expected:\n" + _infoReceivedFiles + "\n"
                  + " Found:\n" + _existingReceivedFiles);
        }
        final HashMap<String, TableInfo.Column> _columnsSentFiles = new HashMap<String, TableInfo.Column>(12);
        _columnsSentFiles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentFiles.put("fileId", new TableInfo.Column("fileId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentFiles.put("fileName", new TableInfo.Column("fileName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentFiles.put("mimeType", new TableInfo.Column("mimeType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentFiles.put("fileSize", new TableInfo.Column("fileSize", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentFiles.put("localPath", new TableInfo.Column("localPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentFiles.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentFiles.put("receiverId", new TableInfo.Column("receiverId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentFiles.put("sentAt", new TableInfo.Column("sentAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentFiles.put("mode", new TableInfo.Column("mode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentFiles.put("expiresAt", new TableInfo.Column("expiresAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentFiles.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSentFiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSentFiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSentFiles = new TableInfo("sent_files", _columnsSentFiles, _foreignKeysSentFiles, _indicesSentFiles);
        final TableInfo _existingSentFiles = TableInfo.read(db, "sent_files");
        if (!_infoSentFiles.equals(_existingSentFiles)) {
          return new RoomOpenHelper.ValidationResult(false, "sent_files(com.atezhare.data.SentFile).\n"
                  + " Expected:\n" + _infoSentFiles + "\n"
                  + " Found:\n" + _existingSentFiles);
        }
        final HashMap<String, TableInfo.Column> _columnsLibrarian = new HashMap<String, TableInfo.Column>(5);
        _columnsLibrarian.put("fileName", new TableInfo.Column("fileName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLibrarian.put("localPath", new TableInfo.Column("localPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLibrarian.put("fileId", new TableInfo.Column("fileId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLibrarian.put("deleteAt", new TableInfo.Column("deleteAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLibrarian.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLibrarian = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLibrarian = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLibrarian = new TableInfo("librarian", _columnsLibrarian, _foreignKeysLibrarian, _indicesLibrarian);
        final TableInfo _existingLibrarian = TableInfo.read(db, "librarian");
        if (!_infoLibrarian.equals(_existingLibrarian)) {
          return new RoomOpenHelper.ValidationResult(false, "librarian(com.atezhare.data.LibrarianEntry).\n"
                  + " Expected:\n" + _infoLibrarian + "\n"
                  + " Found:\n" + _existingLibrarian);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "97624eb536e164aa828cc33eabd794f5", "e27f5696e7adb9077fc79d5f3b6f749c");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "received_files","sent_files","librarian");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `received_files`");
      _db.execSQL("DELETE FROM `sent_files`");
      _db.execSQL("DELETE FROM `librarian`");
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
    _typeConvertersMap.put(ReceivedFileDao.class, ReceivedFileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SentFileDao.class, SentFileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(LibrarianDao.class, LibrarianDao_Impl.getRequiredConverters());
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
  public ReceivedFileDao receivedFileDao() {
    if (_receivedFileDao != null) {
      return _receivedFileDao;
    } else {
      synchronized(this) {
        if(_receivedFileDao == null) {
          _receivedFileDao = new ReceivedFileDao_Impl(this);
        }
        return _receivedFileDao;
      }
    }
  }

  @Override
  public SentFileDao sentFileDao() {
    if (_sentFileDao != null) {
      return _sentFileDao;
    } else {
      synchronized(this) {
        if(_sentFileDao == null) {
          _sentFileDao = new SentFileDao_Impl(this);
        }
        return _sentFileDao;
      }
    }
  }

  @Override
  public LibrarianDao librarianDao() {
    if (_librarianDao != null) {
      return _librarianDao;
    } else {
      synchronized(this) {
        if(_librarianDao == null) {
          _librarianDao = new LibrarianDao_Impl(this);
        }
        return _librarianDao;
      }
    }
  }
}
