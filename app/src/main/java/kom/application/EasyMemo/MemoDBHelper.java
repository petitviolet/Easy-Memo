package kom.application.EasyMemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MemoDBHelper extends SQLiteOpenHelper {
	static final String name="memos.db";
	static final int version = 1;
	static final CursorFactory factory = null;

	public MemoDBHelper(Context context) {
		super(context, name, factory, version);
		// TODO ©®¶¬³ê½RXgN^[EX^u
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO ©®¶¬³ê½\bhEX^u
		String sql = "CREATE TABLE memoDB("
				+ android.provider.BaseColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, title Text, memo TEXT, date Text);";
				//+ " INTEGER PRIMARY KEY AUTOINCREMENT, title Text, memo TEXT);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO ©®¶¬³ê½\bhEX^u
	}

}
