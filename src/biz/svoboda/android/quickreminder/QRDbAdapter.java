package biz.svoboda.android.quickreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import biz.svoboda.android.utils.MyLogger;

/**
 * @author Kamil Svoboda
 * 
 */
public class QRDbAdapter {

	private static final String TAG = "QRDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "data";
	private static final String TABLE_NAME = "reminder";
	private static final int DATABASE_VERSION = 2;

	public static final String KEY_ROWID = "_id";
	public static final String KEY_DATETIME = "datetime";
	public static final String KEY_TEXT = "text";

	MyLogger mLogger = new MyLogger(QRController.class);
	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_CREATE = "create table " + TABLE_NAME
			+ " (" + KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_DATETIME + " long not null, " + KEY_TEXT + " text not null);";

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public QRDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the database. If it cannot be opened, try to create a new instance
	 * of the database. If it cannot be created, throw an exception to signal
	 * the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public QRDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Close database
	 */
	public void close() {
		mDbHelper.close();
	}

	/**
	 * Create a new reminder using the datetime and text provided. If the note
	 * is successfully created return the new rowId for that reminder, otherwise
	 * return a -1 to indicate failure.
	 * 
	 * @param datetime
	 *            the date and time of the reminder
	 * @param text
	 *            the text of reminder
	 * @return rowId or -1 if failed
	 */
	public long createReminder(Long datetime, String text) {
		mLogger.Debug("Create new reminder in DB");
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DATETIME, datetime);
		initialValues.put(KEY_TEXT, text);

		return mDb.insert(TABLE_NAME, null, initialValues);
	}

	/**
	 * Delete the reminder with the given rowId
	 * 
	 * @param rowId
	 *            id of reminder to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteReminder(long rowId) {
		mLogger.Debug("Delete reminder from DB");
		return mDb.delete(TABLE_NAME, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all reminders in the database
	 * 
	 * @return Cursor over all reminders
	 */
	public Cursor fetchAllReminders() {
		mLogger.Debug("Get all reminders from DB");
		return mDb.query(TABLE_NAME, new String[] { KEY_ROWID, KEY_DATETIME,
				KEY_TEXT }, null, null, null, null, null);
	}

	/**
	 * Return a Cursor positioned at the reminder that matches the given rowId
	 * 
	 * @param rowId
	 *            id of reminder to retrieve
	 * @return Cursor positioned to matching reminder, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchReminder(long rowId) throws SQLException {
		mLogger.Debug("Get reminder with id = " + String.valueOf(rowId)
				+ " from DB");
		Cursor mCursor =

		mDb.query(true, TABLE_NAME, new String[] { KEY_ROWID, KEY_DATETIME,
				KEY_TEXT }, KEY_ROWID + "=" + rowId, null, null, null, null,
				null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	/**
	 * Update the reminder using the details provided. The reminder to be
	 * updated is specified using the rowId, and it is altered to use the
	 * datetime and text values passed in
	 * 
	 * @param rowId
	 *            id of reminder to update
	 * @param datetime
	 *            value to set reminder datetime to
	 * @param text
	 *            value to set reminder text to
	 * @return true if the reminder was successfully updated, false otherwise
	 */
	public boolean updateReminder(long rowId, Long datetime, String text) {
		mLogger.Debug("Update reminder with id = " + String.valueOf(rowId)
				+ " in DB");
		ContentValues args = new ContentValues();
		args.put(KEY_DATETIME, datetime);
		args.put(KEY_TEXT, text);

		return mDb.update(TABLE_NAME, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Namapuje hodtnoty z cursoru (kam aktuálně ukazuje) do objektu Reminder
	 * 
	 * @param cursor
	 * @return Reminder
	 */
	public Reminder negotiateCursor(Cursor cursor) {
		Reminder result = new Reminder();

		int colIdIndex = cursor.getColumnIndex(QRDbAdapter.KEY_ROWID);
		result.setId(cursor.getLong(colIdIndex));

		int colDatetimeIndex = cursor.getColumnIndex(QRDbAdapter.KEY_DATETIME);
		result.setDatetime(cursor.getLong(colDatetimeIndex));

		int colTextIndex = cursor.getColumnIndex(QRDbAdapter.KEY_TEXT);
		result.setText(cursor.getString(colTextIndex));

		return result;
	}
}
