package kom.application.EasyMemo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import kom.application.EasyMemo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
//import java.util.Date;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.Editable;
import android.text.Selection;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextWatcher;
import android.util.Log;

public class EasyMemoActivity extends Activity implements OnClickListener {
	private boolean memoChanged = false;
	private final int HEAD_LENGTH = 10;  // メモ本文冒頭からタイトルを切り出す長さ
	private String fileName;
	private String fileId;
	private String encoding = "SHIFT-JIS";
	private Button button_new;
	private Button button_save;
	private Button button_open;	
	private boolean flag = false;
	private EditText et_memo;
	private EditText et_title;
	private String title;
	private String memo;
	static final String[] cols = { "title", "memo",
			android.provider.BaseColumns._ID, "date" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_memo_pad);
		et_memo = (EditText) findViewById(R.id.easy_memo);
		et_title = (EditText) findViewById(R.id.easy_memo_title);
		SharedPreferences pref = this.getSharedPreferences("MemoPrefs",
				MODE_PRIVATE);
		memoChanged = pref.getBoolean("memoChanged", false);
		et_title.setText(pref.getString("title", ""));
		et_memo.setText(pref.getString("memo", ""));
		fileId = pref.getString("id", "");
		et_memo.setSelection(pref.getInt("cursor", 0));
		button_new = (Button) findViewById(R.id.new_button);
		button_new.setOnClickListener(this);
		button_save = (Button) findViewById(R.id.save_button);
		button_save.setOnClickListener(this);
		button_open = (Button) findViewById(R.id.open_button);
		button_open.setOnClickListener(this);
		fileName = pref.getString("fn", "");
		if (fileName.length() == 0) {
			fileName = et_title.getText().toString();
		}

		Log.d("onCreate_1", "fn =" + fileName + ";");
		encoding = pref.getString("encode", "SHIFT_JIS");
		Intent i = getIntent();
		String action = i.getAction();

		if (Intent.ACTION_VIEW.equals(action)) {
			// ファイルを開く時
			Uri uri = i.getData();
			if (uri != null) {
				String tempFn = i.getData().getPath();
				Log.d("uri not null", uri.toString());
				if ("file".equals(uri.getScheme()) && tempFn.length() > 0) {
					fileName = tempFn;
					readText();
					Log.d("onCreate_2", "fn =" + fileName + ";");
					memoChanged = false;
				}
			}
		} else if (Intent.ACTION_SEND.equals(action)) {
			// テキストをシェアする時
			Bundle bundle = i.getExtras();
			String shareText = bundle.getString(Intent.EXTRA_TEXT, "");
			String shareSubject = bundle.getString(Intent.EXTRA_SUBJECT, "");
			Log.d("contents", bundle.toString());						
			et_title.setText(shareSubject);
			et_memo.setText(shareText);
			memoChanged = true;
			fileId = "";
		}

		et_memo.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (flag == false) { // newText()のあとはmemoChangedじゃない
					memoChanged = true;
				} else {
					flag = false;
					memoChanged = false;
				}
			}
		});
		Log.d("MemoPadActivity", "Create MemoPadActivity");
	}

	@Override
	protected void onResume() {
		// TODO 自動生成されたメソッド・スタブ
		super.onResume();
		if (memoChanged) {
			saveText();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {// 戻るボタン
			if (memoChanged == true) {
				saveText();
			}
//			if (event.getAction() == KeyEvent.ACTION_DOWN) {// 押された時
//				if (temp == 2) {
//					if (memoChanged == true) {
//						saveText();
//					}
//					temp = 0;
//					this.moveTaskToBack(true);
//				} else {
//					temp = 2;
//					temp_fn = fn;
//					Intent i = new Intent(this, MemoList.class);
//					startActivityForResult(i, 2);
//				}
//				// startActivity(i);
//				// webView.goBack();// WebViewを戻す
//			}
//			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onClick(View v) {
		if (v == button_new) {
			// 新規
			flag = true;
			newText();
		} else if (v == button_save) {
			// 保存
			try {
				saveText();
			} catch (Exception e) {
				e.printStackTrace();
			}
			;
		} else if (v == button_open) {
			// 読み込み
			readMemo();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("MemoPadActivity", "onStop_start");
		SharedPreferences pref = this.getSharedPreferences("MemoPrefs",
				MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		title = et_title.getText().toString();
		// title.replaceAll("¥¥n|¥¥s", " ");
		// editor.putString("title", et_title.getText().toString());
		editor.putString("title", title);
		editor.putString("id", fileId);
		editor.putString("memo", et_memo.getText().toString());
		editor.putInt("cursor", Selection.getSelectionStart(et_memo.getText()));
		editor.putBoolean("memoChanged", memoChanged);
		editor.putString("fn", fileName);
		editor.putString("encode", encoding);
		editor.commit();
		Log.d("MemoPadActivity", "onStop_finish");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu, menu);
		if (encoding.equals("SHIFT_JIS")) {
			menu.findItem(R.id.menu_sjis).setChecked(true);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 0:
				Log.d("onActivityResult", "case:0");
				et_title.setText(data.getStringExtra("title"));
				et_memo.setText(data.getStringExtra("text"));
				fileId = data.getStringExtra("_id");
				memoChanged = false;
				fileName = et_title.getText().toString();
				break;
			case 1:
				Log.d("onActivityResult", "case:1");
				fileName = data.getStringExtra("fn");
				if (fileName.length() > 0) {
					et_title.setText(fileName);
					et_memo.setText(readFile());
					memoChanged = false;
				}
				break;
			case 2:
				// Backしてもう一度同じファイルを開いてまたBackすると終了
				// 違うファイルを開けばもう一度Back出来る
				et_title.setText(data.getStringExtra("title"));
				et_memo.setText(data.getStringExtra("text"));
				memoChanged = false;
				fileName = et_title.getText().toString();
				break;
			}
		}
	}
	
	private void clearInputedTexts() {
		et_title.setText("");
		et_memo.setText("");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (fileName.length() == 0) {
			fileName = et_title.getText().toString();
		}
		Log.d("MemoPadActivity", "onOptions");
		switch (item.getItemId()) {
		case R.id.menu_delete:
			delete();
			break;
		case R.id.menu_export:
			try {
				if (Environment.MEDIA_MOUNTED.equals(Environment
						.getExternalStorageState())) {
					writeFile();
					memoChanged = false;
				} else {
					Toast toast = Toast.makeText(this,
							R.string.toast_no_external_storage,
							Toast.LENGTH_LONG);
					toast.show();
				}
				;
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.menu_sjis:
			if (item.isChecked()) {
				item.setChecked(false);
				encoding = "UTF-8";
			} else {
				item.setChecked(true);
				encoding = "SHIFT_JIS";
			}
			break;
		}
		Log.d("MemoPadActivity", "onOptions_finish");
		return super.onOptionsItemSelected(item);
	}

	/**
	 * called new button tapped
	 * clear inputed texts at title and memo
	 * befor clear, show a dialog to confirm save a memo if memo is edited
	 */
	private void newText() {
		if (memoChanged == true) {
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle(R.string.memodb_save);
			ab.setMessage(R.string.memodb_confirm_save);
			ab.setPositiveButton(R.string.button_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							saveText();
							clearInputedTexts();
						}
					});
			ab.setNegativeButton(R.string.button_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							clearInputedTexts();
						}
					});
			ab.setCancelable(true);
			final AlertDialog ad = ab.create();
			ad.show();
		} else {
			clearInputedTexts();
		}
		// id = "";
		memoChanged = false; // 無駄に保存したりしないように
	}

	/**
	 * called when save button tapped
	 */
	private void saveText() {
		title = et_title.getText().toString();
		memo = et_memo.getText().toString();
		setTitleByMemoHead();
		Log.d("MemoPadActivity.java", "saveText_start");
		if (memo.length() > 0) {
			// 現在の時刻を取得
			// 表示形式を設定
			if (title.length() == 0) {
				title = getApplicationContext().getString(R.string.no_title);
			}
			SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy'-'MM'-'dd' 'kk':'mm");
			// String ts = sdf.format(date);
			Calendar calendar = Calendar.getInstance();
			String ts = sdf.format(calendar.getTime());
			MemoDBHelper memos = new MemoDBHelper(this);
			SQLiteDatabase db = memos.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("title", title);
			values.put("memo", memo);
			values.put("date", ts);
			String where = "title = \"" + title + "\"";
			Cursor c = db.query("memoDB", cols, where, null, null, null, null);
			int toastStringId;
			try {
				if (c.moveToFirst() == true && fileId.length() > 0) {
					// db.update("memoDB", values, "title=? and date=?", new
					// String[]{title});
					db.update("memoDB", values, "_id=?", new String[] { fileId });
					Log.d("saveText", "update _id=" + fileId);
					toastStringId = R.string.complete_update;
				} else {
					db.insertOrThrow("memoDB", null, values);
					Log.d("saveText", "insert");
					toastStringId = R.string.complete_save;
				}
				Toast.makeText(this, this.getString(toastStringId), Toast.LENGTH_SHORT).show();
			} catch (SQLiteException sql_e) {
				sql_e.printStackTrace();
				Log.d("saveText", "error");
			}
			memos.close();
		} else {
			// titleが無くても「無題」にするだけ
			// 本文が無ければ保存はしない
			if (memo.length() <= 0) {
				Toast.makeText(this, R.string.not_save_no_memo, Toast.LENGTH_SHORT).show();
			}
		}
		memoChanged = false;
		Log.d("MemoPadActivity", "saveText_finish");
	}

	protected void readMemo() {
		if (memoChanged == true) {
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle(R.string.memodb_save);
			ab.setMessage(R.string.memodb_confirm_save);
			ab.setPositiveButton(R.string.button_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							saveText();
							startMemoList();
						}
					});
			ab.setNegativeButton(R.string.button_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							startMemoList();
						}
					});
			ab.setCancelable(true);
			AlertDialog ad = ab.create();
			ad.show();
		} else {
			startMemoList();
		}
		memoChanged = false;
	}

	protected void startMemoList() {
		Intent i = new Intent(this, MemoList.class);
		startActivityForResult(i, 0);
	}

	protected void readText() {
		try {
			FileInputStream fis = openFileInput(fileName);
			byte[] readBytes = new byte[fis.available()];
			fis.read(readBytes);
			et_memo.setText(new String(readBytes));
			et_title.setText(fileName.replaceAll(".txt", ""));
			fis.close();
		} catch (Exception e) {
		}
	}

	private String readFile() {
		Log.d("readFile", "readFile_start");
		String str = " ";
		String l = null;
		Log.d("readFile", "fn = " + fileName + ";");
		if (fileName != null) {
			Log.d("MemoPadActivity", "readFile_start");
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(fileName), encoding));
				do {
					l = br.readLine();
					if (l != null) {
						str = str + l + "¥n";
					}
				} while (l != null);
				br.close();
				fileName = fileName.replaceAll(".txt", "");
			} catch (IOException e) {
				Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
		return str;
	}
	private void setTitleByMemoHead() {
		int headLength = HEAD_LENGTH;
		if (title.length() == 0) {
			if (memo.length() <= HEAD_LENGTH) {
				headLength = memo.length();
			}
			Log.d("memo length", String.valueOf(headLength));
			title = memo.substring(0, headLength);
			et_title.setText(title);
		}
	}
	
	void writeFile() {
		Log.d("writeFile", "writeFile_start");
		memo = et_memo.getText().toString();
		title = et_title.getText().toString();
		setTitleByMemoHead();
		if (fileName.length() != 0) {
			String dn = Environment.getExternalStorageDirectory()
					+ "/EasyMemo/";
			Log.d("writeFile", "title = " + title);
			title = title.replaceAll("\\.|\\/|:|\\*|\\?|\"|<|>|\\n| |\\|", "_")
					.trim();
			fileName = dn + title + ".txt";
			Log.d("writeFile", "fn = " + fileName);
			File dir = new File(dn);
			if (!dir.exists()) {
				dir.mkdir();
			}
			BufferedWriter bw1;
			try {
				bw1 = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(fileName), encoding));
				bw1.write(memo);
				bw1.close();
				Toast toast = Toast.makeText(this, "Saved to " + fileName,
						Toast.LENGTH_LONG);
				toast.show();
				Log.d("writeFile", "writeFile Done.");
			} catch (IOException e) {
				Toast toast = Toast.makeText(this,
						R.string.toast_file_name_error, Toast.LENGTH_LONG);
				toast.show();
				e.printStackTrace();
			}
			Log.d("writeFile", "writeFile_finish");
		} else {
			Toast toast = Toast.makeText(this, R.string.toast_file_name_error,
					Toast.LENGTH_LONG);
			toast.show();
		}
	}

	void delete() {
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.memodb_delete);
		ab.setMessage(R.string.memodb_confirm_delete);
		ab.setPositiveButton(R.string.button_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						_delete();
					}
				});
		ab.setNegativeButton(R.string.button_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		ab.show();
	}

	void _delete() {
		Log.d("MemoPadActivity", "delete_start");
		fileName = et_title.getText().toString();
		// String text = et_memo.getText().toString();
		MemoDBHelper memos = new MemoDBHelper(this);
		SQLiteDatabase db = memos.getWritableDatabase();
		Log.d("MemoPadActivity", "fn = " + fileName + ";");
		// String where = "title =  " +
		// "¥"" + fn + "¥" and memo = ¥"" + text + "¥"";
		String where = "_ID = " + fileId;
		try {
			db.delete("memoDB", where, null);
		} catch (SQLiteException sql_e) {
			sql_e.printStackTrace();
		}
		Log.d("MemoPadActivity", "delete_finish");
		db.close();
		newText();
	}
}