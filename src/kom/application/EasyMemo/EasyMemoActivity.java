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

public class EasyMemoActivity extends Activity implements OnClickListener{
	boolean memoChanged = false;
	String fn, temp_fn, id;
	String encode = "SHIFT-JIS";
	private Button button_new;
	private Button button_save;
	private Button button_open;
	boolean flag = false;
	int temp = 0;
	static final String[] cols = {"title", "memo", android.provider.BaseColumns._ID, "date"};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_pad);
        EditText et_memo = (EditText)findViewById(R.id.easy_memo);
        EditText et_title = (EditText)findViewById(R.id.easy_memo_title);
        SharedPreferences pref = this.getSharedPreferences("MemoPrefs", MODE_PRIVATE);
        memoChanged = pref.getBoolean("memoChanged", false);
        //memoChanged = false;
        et_title.setText(pref.getString("title", ""));
        et_memo.setText(pref.getString("memo", ""));
        id = pref.getString("id", "");
        et_memo.setSelection(pref.getInt("cursor", 0));
        button_new = (Button)findViewById(R.id.new_button);
        button_new.setOnClickListener(this);
        button_save = (Button)findViewById(R.id.save_button);
        button_save.setOnClickListener(this);
        button_open = (Button)findViewById(R.id.open_button);
        button_open.setOnClickListener(this);
        fn = pref.getString("fn", "");
        if (fn.length() == 0){
        	fn = et_title.getText().toString();
        }
    	
    	Log.d("onCreate_1", "fn ="+fn+";");
        encode = pref.getString("encode", "SHIFT_JIS");
        Intent i = getIntent();
        Uri uri = i.getData();
        String tempFn = "";
        if (uri != null){
        	tempFn = i.getData().getPath();
        }
        if (tempFn.length() > 0){
        	if (memoChanged){
        		saveText();
        	}
        	fn = tempFn;
        	/*et_memo.setText(readFile());
        	et_title.setText(fn); */
        	readText();
        	Log.d("onCreate_2", "fn ="+fn+";");
        	memoChanged = false;
        }
        TextWatcher tw = new TextWatcher(){
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (flag == false){ //newText()のあとはmemoChangedじゃない
					memoChanged = true;
				} else {
					flag = false;
					memoChanged = false;
				}				
			}
        };
        et_memo.addTextChangedListener(tw);
        Log.d("MemoPadActivity", "Create MemoPadActivity" );
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {// 戻るボタン
            if (event.getAction() == KeyEvent.ACTION_DOWN) {// 押された時
            	if (temp == 2){
            		if (memoChanged == true){
            			saveText();
            		}
            		temp = 0;
            		this.moveTaskToBack(true);
            	} else {
            		temp = 2;
            		temp_fn = fn;
            		Intent i = new Intent(this, MemoList.class);
            		startActivityForResult(i, 2);
            	}
    			//startActivity(i);
                //webView.goBack();// WebViewを戻す
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

	@Override
	public void onClick(View v) {
		if(v == button_new){
		//新規
			flag = true;
			newText();
		}else if(v == button_save){
			//
			try{
				saveText();
			}catch(Exception e){e.printStackTrace();};
		}else if(v == button_open){
			//
			readMemo();
		};
	}
	
    @Override
	protected void onStop() {
		super.onStop();
		Log.d("MemoPadActivity", "onStop_start");
		EditText et_memo = (EditText)findViewById(R.id.easy_memo);
        EditText et_title = (EditText)findViewById(R.id.easy_memo_title);
        SharedPreferences pref = this.getSharedPreferences("MemoPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String title = et_title.getText().toString();
        //title.replaceAll("\\n|\\s", " ");
        //editor.putString("title", et_title.getText().toString());
        editor.putString("title", title);
        editor.putString("id", id);
        editor.putString("memo", et_memo.getText().toString());
        editor.putInt("cursor", Selection.getSelectionStart(et_memo.getText()));
        editor.putBoolean("memoChanged", memoChanged);
        editor.putString("fn", fn);
        editor.putString("encode", encode);
        editor.commit();
		Log.d("MemoPadActivity", "onStop_finish");
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater mi = getMenuInflater();
    	mi.inflate(R.menu.menu, menu);
    	if(encode.equals("SHIFT_JIS")){
    		menu.findItem(R.id.menu_sjis).setChecked(true);
    	}
		return super.onCreateOptionsMenu(menu);
	}
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			EditText et_memo = (EditText)findViewById(R.id.easy_memo);
	        EditText et_title = (EditText)findViewById(R.id.easy_memo_title);
	        switch(requestCode){
	        case 0:
	        	Log.d("onActivityResult", "case:0");
	        	temp = 0;
	        	et_title.setText(data.getStringExtra("title"));
	        	et_memo.setText(data.getStringExtra("text"));
	        	id = data.getStringExtra("_id");
	        	memoChanged = false;
	        	fn = et_title.getText().toString();
	        	break;
	        case 1:
	        	Log.d("onActivityResult", "case:1");
	        	temp = 0;
	        	fn = data.getStringExtra("fn");
	        	if (fn.length() > 0){
	        		et_title.setText(fn);
	        		et_memo.setText(readFile());
	        		memoChanged = false;
	        	}
	        	break;
	        case 2:	        	
	        	// Back
	        	et_title.setText(data.getStringExtra("title"));
	        	et_memo.setText(data.getStringExtra("text"));
	        	memoChanged = false;
	        	fn = et_title.getText().toString();
	        	if (temp_fn.equals(fn)){
	        		temp = 2;
	        	} else {
	        		temp_fn = fn;
	        		temp = 0;
	        	}	        	
	        	break;
	        }
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//EditText et_memo = (EditText)findViewById(R.id.easy_memo);
        EditText et_title = (EditText)findViewById(R.id.easy_memo_title);
        //Toast _toast = Toast.makeText(this,  Environment.getExternalStorageState(), Toast.LENGTH_SHORT);
		//_toast.show();
        if (fn.length() == 0){
        	fn = et_title.getText().toString();
        }
        Log.d( "MemoPadActivity", "onOptions" );
		switch(item.getItemId()){
		case R.id.menu_delete:
			delete();
			break;
		case R.id.menu_export:
			try{
				if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
					writeFile();
					memoChanged = false;
				} else {
					Toast toast = Toast.makeText(this, R.string.toast_no_external_storage, Toast.LENGTH_LONG);
					toast.show();
				};
			} catch (Exception e){
				e.printStackTrace();
			}
			break;
		case R.id.menu_sjis:
			if(item.isChecked()){
				item.setChecked(false);
				encode = "UTF-8";
			} else {
				item.setChecked(true);
				encode = "SHIFT_JIS";
			}
			break;
		}
		Log.d( "MemoPadActivity", "onOptions_finish" );
		return super.onOptionsItemSelected(item);
	}

	void newText(){
		if (memoChanged == true){
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle(R.string.memodb_save);
			ab.setMessage(R.string.memodb_confirm_save);
			ab.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					saveText();
					EditText et_memo = (EditText)findViewById(R.id.easy_memo);
					EditText et_title = (EditText)findViewById(R.id.easy_memo_title);     
					et_title.setText("");
					et_memo.setText("");
				}
			});
			ab.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					EditText et_memo = (EditText)findViewById(R.id.easy_memo);
					EditText et_title = (EditText)findViewById(R.id.easy_memo_title);     
					et_title.setText("");
					et_memo.setText("");
				}
			});
			ab.setCancelable(true);
			final AlertDialog ad = ab.create();
			ad.show();
		} else {
			EditText et_memo = (EditText)findViewById(R.id.easy_memo);
			EditText et_title = (EditText)findViewById(R.id.easy_memo_title);     
			et_title.setText("");
			et_memo.setText("");
		}
		//id = "";
		memoChanged = false;  // ���ʂɕۑ������肵�Ȃ��悤��
	}
	
	void saveText(){
        EditText et_memo = (EditText)findViewById(R.id.easy_memo);
        EditText et_title = (EditText)findViewById(R.id.easy_memo_title);
        String title = et_title.getText().toString();
        String memo = et_memo.getText().toString();
        Log.d( "MemoPadActivity.java", "saveText_start" );
        if(memo.length() > 0){
        	if (title.length() == 0){
        		title = "����";
        	}
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd' 'kk':'mm");
    		//String ts = sdf.format(date);
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
        	try{
        		Toast toast;
        		if (c.moveToFirst() == true && id.length() > 0){
        			//db.update("memoDB", values, "title=? and date=?", new String[]{title});
        			db.update("memoDB", values, "_id=?", new String[]{id});
        			Log.d("saveText", "update _id=" + id);
        			toast = Toast.makeText(this,  R.string.complete_update, Toast.LENGTH_SHORT);
        		} else {
                	db.insertOrThrow("memoDB", null, values);
                	Log.d("saveText", "insert");
                	toast = Toast.makeText(this,  R.string.complete_save, Toast.LENGTH_SHORT);
        		}				
				toast.show();
        	} catch (SQLiteException sql_e) {
        			sql_e.printStackTrace(); 
        			Log.d("saveText", "error");
        	}
        	memos.close();
        } else {
        	// title
        	if (memo.length() <= 0){
				Toast toast = Toast.makeText(this,  R.string.not_save_no_memo, Toast.LENGTH_SHORT);
				toast.show();
			}
        }
        memoChanged = false;
        temp = 2;
    	Log.d( "MemoPadActivity", "saveText_finish" );
    }
	
	protected void readMemo(){
		if(memoChanged == true){
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setTitle(R.string.memodb_save);
			ab.setMessage(R.string.memodb_confirm_save);
			ab.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
				//Intent i = new Intent(this, MemoList.class);
				public void onClick(DialogInterface dialog, int which){
					saveText();
					startMemoList();
				}
			});
			ab.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
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
	
	protected void startMemoList(){
		Intent i = new Intent(this, MemoList.class);
		startActivityForResult(i, 0);
	}
	
	protected void readText(){
        EditText et_memo = (EditText)findViewById(R.id.easy_memo);
        EditText et_title = (EditText)findViewById(R.id.easy_memo_title);
		try{
			FileInputStream fis = openFileInput(fn);
			byte[] readBytes = new byte[fis.available()];
			fis.read(readBytes);
			et_memo.setText(new String(readBytes));
			et_title.setText(fn.replaceAll(".txt", ""));
			fis.close();
		}catch(Exception e){}
	}
	
	String readFile(){
		Log.d("readFile", "readFile_start");
		String str = " ";
		String l = null;
		Log.d("readFile", "fn = " + fn + ";");
		if (fn != null){
			Log.d("MemoPadActivity", "readFile_start");
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(fn), encode));
				do {
					l = br.readLine();
					if (l != null){
						str = str + l + "\n";}
				} while ( l != null);
				br.close();
				fn = fn.replaceAll(".txt", "");
			} catch (IOException e){
				Toast toast = Toast.makeText(this, "Error", Toast.LENGTH_LONG);
				toast.show();
				e.printStackTrace();
			}
		}
		return str;
	}
	
	void writeFile(){
		Log.d("writeFile", "writeFile_start");
		EditText et_title = (EditText)findViewById(R.id.easy_memo_title);
		EditText et_memo = (EditText)findViewById(R.id.easy_memo);
		String memo = et_memo.getText().toString();
		String title = et_title.getText().toString();
		if (fn.length() != 0){
			String dn = Environment.getExternalStorageDirectory() + "/EasyMemo/";
			Log.d("writeFile", "title = " + title);
			title = title.replaceAll("\\.|\\/|:|\\*|\\?|\"|<|>|\\n| |\\|", "_").trim();
			fn = dn + title + ".txt";
			Log.d("writeFile", "fn = " + fn);
			File dir = new File(dn);
			if(!dir.exists()){
				dir.mkdir();
			}
			BufferedWriter bw1;
			try{
				bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fn), encode));
				bw1.write(memo);
				bw1.close();
				Toast toast = Toast.makeText(this, fn, Toast.LENGTH_LONG);
				toast.show();
				Log.d("writeFile", "writeFile Done.");
			} catch (IOException e){
				Toast toast = Toast.makeText(this, R.string.toast_file_name_error, Toast.LENGTH_LONG);
				toast.show();
				e.printStackTrace();
			}
			Log.d("writeFile", "writeFile_finish");
		} else {
			Toast toast = Toast.makeText(this, R.string.toast_file_name_error, Toast.LENGTH_LONG);
			toast.show();
		}
	}
	
	void delete(){
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.memodb_delete);
		ab.setMessage(R.string.memodb_confirm_delete);
		ab.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				_delete();
			}
		});
		ab.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
			}
		});
		ab.show();
	}
	
	void _delete(){
		Log.d("MemoPadActivity", "delete_start");
		EditText et_title = (EditText)findViewById(R.id.easy_memo_title);
		//EditText et_memo = (EditText)findViewById(R.id.easy_memo);
		fn = et_title.getText().toString();
		//String text = et_memo.getText().toString();
		MemoDBHelper memos = new MemoDBHelper(this);
		SQLiteDatabase db = memos.getWritableDatabase();
		Log.d("MemoPadActivity", "fn = "+fn+ ";");
		//String where = "title =  " + "\"" + fn + "\" and memo = \"" + text + "\"";
		String where = "_ID = " + id;   
		try{
			db.delete("memoDB", where, null);
		} catch (SQLiteException sql_e) {
			sql_e.printStackTrace();
		}
		Log.d("MemoPadActivity", "delete_finish");
		db.close();
		newText();
	}
	
	protected void onDestroy(){
		super.onDestroy();
		// cursor.close();		
	}
}