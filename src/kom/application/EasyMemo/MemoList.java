package kom.application.EasyMemo;

import android.view.*;
import kom.application.EasyMemo.R;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ListActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;


@TargetApi(11)
public class MemoList extends ListActivity {
	static final String[] cols = {"title", "memo", android.provider.BaseColumns._ID, "date"};
	MemoDBHelper memos;
	Cursor cursor;
	//int IDs[];	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO
		Log.d( "MemoList", "onListItem_start" );
		super.onListItemClick(l, v, position, id);
		memos = new MemoDBHelper(this);
        //DatabaseHelper dbHelper = new DatabaseHelper(this);  
        //SQLiteDatabase db=dbHelper.getReadableDatabase();  
		SQLiteDatabase db = memos.getWritableDatabase();
		//SQLiteDatabase db = memos.getReadableDatabase();
		Cursor cursor = db.query("memoDB", cols, "_ID="+String.valueOf(id), null, null, null, null);
		//Cursor query(String table, String[] columns, String selection, 
		//					String[] selectionArgs, String groupBy, String having, String orderBy); 
		//startManagingCursor(cursor);
		int idx_memo = cursor.getColumnIndex("memo");
		int idx_title = cursor.getColumnIndex("title");
		int idx_id = cursor.getColumnIndex("_id");
		cursor.moveToFirst();
		Intent i = new Intent();
		i.putExtra("text", cursor.getString(idx_memo));
		i.putExtra("title", cursor.getString(idx_title));
		i.putExtra("_id", cursor.getString(idx_id));
		setResult(RESULT_OK, i);
		memos.close();
		finish();		
		cursor.close();
		Log.d( "MemoList", "onListItem_finish" );
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		Cursor cursor = getMemos();
		// startManagingCursor(cursor);
		cursor.moveToPosition(info.position);
		final int columnid = cursor.getInt(2);
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle(R.string.memodb_delete);
		ab.setMessage(R.string.memodb_confirm_delete);
		ab.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				SQLiteDatabase db = memos.getWritableDatabase();
				db.delete("memoDB", "_id="+columnid, null);
				db.close();
				showMemos(getMemos());
			}
		});
		ab.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
			}
		});
		ab.show();
		cursor.close();
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.context, menu);
	}
    
	/*@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO
		Log.d("MemoList", "onCreate_start");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memolist);
		showMemos(getMemos());
		Log.d("MemoList", "onCreate_finish");
	}*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		Log.d("MemoList", "onCreate_start");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memolist);
		showMemos(getMemos());
		//ListView lv = (ListView) this.findViewById(android.R.id.list);
		ListView lv = (ListView) this.findViewById(android.R.id.list);
		//lv.setItemsCanFocus(false);
		/*lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		getResources().getColor(R.color.black);
		lv.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener(){			
			ArrayList<Integer> IDs = new ArrayList<Integer>();	
			
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// �A�N�V�����A�C�e���I����
				return true;
			}		 		        
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// �A�N�V�������[�h������	
				return true;
			}		        
			public void onDestroyActionMode(ActionMode mode) {
				// ����{�^��������
				Log.d("MemoList", "DestroyAction-start");
				//deleteList�ɂ�memolist���̉��Ԗڂ̗v�f���I������Ă��邩�Atrue���i�[����Ă���B
				AlertDialog.Builder ab = new AlertDialog.Builder(MemoList.this);
				final int checkedCount = getListView().getCheckedItemCount();
				ab.setTitle(R.string.memodb_delete);
				ab.setMessage("�I������"+Integer.toString(checkedCount)+"�̃������폜���܂���?");
				ab.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which){
						int i;
						SparseBooleanArray deleteList = getListView().getCheckedItemPositions();
						Log.d("MemoList", "deleteList_size = " + Integer.toString(deleteList.size()));
						SQLiteDatabase db = memos.getWritableDatabase();
						//for (i = 0; i < deleteList.size(); i++){
						for (i = 0; i < IDs.size(); i++){
							//2012�N11��22��
							//�����I�������������폜������
							//cursor.moveToPosition(deleteList[i].position);
							int j;
							for(j=0;j<=i;j++){
								db.delete("memoDB", "_id="+IDs.get(j), null);
							}
						} 
						//final int columnid = cursor.getInt(2);    		    		    					
						//db.delete("memoDB", "_id="+columnid, null);
						db.close();
						showMemos(getMemos());
					}					
				});
				ab.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which){
					}
				});
				ab.show();
				//cursor.close();
				Log.d("MemoList", "DestroyAction-finish");
			}
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO
				return false;
			}
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
				// TODO
				Log.d("MemoList", "onItem,id:"+id+",position:"+position);
				//IDs[i] = (int) id;
				IDs.add((int)id);
			}
		});		
		lv.setSelector(new PaintDrawable(R.color.firebrick)); */
		registerForContextMenu(lv);		
		Log.d("MemoList", "onCreate_finish");
	}

	@TargetApi(11)
	private Cursor getMemos(){
		Log.d("MemoList", "getMemos_start");
		memos = new MemoDBHelper(this);
		SQLiteDatabase db = memos.getReadableDatabase();
		//Cursor cursor = db.query("memoDB", cols, null, null, null, null, null);
		Cursor cursor = db.query("memoDB", cols, null, null, null, null, cols[3] +" DESC, " + cols[2] + " DESC");
		Log.d("MemoList", "getMemos_finish");
		//startManagingCursor(cursor);
		return cursor;
	}
	/*
	public class OriginalCursorAdapter extends SimpleCursorAdapter {
		  private int mTo[];
		  @SuppressWarnings("deprecation")
		public OriginalCursorAdapter(Context context,
		      int layout, Cursor c, String from[], int to[]) {
		    super(context, layout, c, from, to);
		    mTo = to;
		  }
		  @Override
		  public void bindView(View view, Context context, Cursor cursor) {
		    for(int i = 0; i<mTo.length; i++) {
		      View v = view.findViewById(mTo[i]);
		      if (v == null) continue;
		      String text = cursor.getString(mFrom[i]);
		      if (text == null) text = "";
		      if (v instanceof TextView) {
		        setViewText((TextView)v, text);
		      } else if (v instanceof ImageView) {
		      }
		    }
		  }
		}
	*/
	
	
	@TargetApi(11)
	private void showMemos(Cursor cursor){
		Log.d("MemoList", "showMemos_start");
		if(cursor != null){
			String[] from = {"title", "date", "memo"};
			int[] to = {R.id.info_title, R.id.info_date, R.id.info_memo};
			//int[] to = {android.R.id.text1};
			//int[] to = {android.R.id.text1, android.R.id.text2};
			//ListAdapter adapter = new ListAdapter(this, )
			SimpleCursorAdapter adapter =
					new SimpleCursorAdapter(this, R.layout.memo_line, cursor, from, to, 0){
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent){
                            final View row = super.getView(position, convertView, parent);
                            row.setBackgroundResource(R.drawable.list_background_selector);
                            return row;
                        }
                    };
					//new SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice, cursor, from ,to, 0){
			adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                    Log.d("MemoList", "columnIndex = " + columnIndex);
                    if (columnIndex == 0) { //���̂ւ��M��Ε����I���̍폜���ł�����11/27
                        int Color = getResources().getColor(R.color.black);
                        ((TextView) view).setTextColor(Color);

                    } else {
                        int Color = getResources().getColor(R.color.darkgray);
                        ((TextView) view).setTextColor(Color);
                    }
                    return false;
                }
            });
			setListAdapter(adapter);
		}
		memos.close();
		Log.d("MemoList", "showMemos_finish");
	}

	/*@TargetApi(11)
	private void showMemos(Cursor cursor){
		Log.d("MemoList", "showMemos_start");
		if(cursor != null){
			String[] from = {"title", "date"};
			int[] to = {R.id.info_title, R.id.info_date};
			//int[] to = {android.R.id.text1, android.R.id.text2};
			//ListAdapter adapter = new ListAdapter(this, )
			ListView lv = (ListView)findViewById(android.R.id.list);
			ArrayAdapter<String> adapter =
					new ArrayAdapter<String>(this, R.layout.memo_line);
					//new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, from ,to, 0);
			//adapter��DB���當�����ǉ�
			adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {  
	              
	            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {  
	                if (columnIndex == COLUMN_TYPE) {  
	                    int type = cursor.getInt(COLUMN_TYPE);  
	                    String text = (String) Phone.getTypeLabel(getResources(), type, null);  
	                      
	                    ((TextView) view).setText(text);  
	                    ((TextView) view).setTextColor(COLOR_LIST[type % 5]);  
	                    return true;  
	                }  
	                return false;  
	            }  
	        });  
			boolean isEof = true;
			while(isEof){
			adapter.add(cursor.getString(0));
			isEof = cursor.moveToNext();
			}
			//setListAdapter(adapter);
			lv.setAdapter(adapter);
		}
		memos.close();
		Log.d("MemoList", "showMemos_finish");
	}*/
	
	public void searchMemo(View v){
		EditText et = (EditText)findViewById(R.id.search_Text);
		String q = et.getText().toString();
		SQLiteDatabase db = memos.getReadableDatabase();
		String where = "memo like ? or title like ?";
		q = "%" + q + "%";
		Cursor cursor = db.query("memoDB", cols, where, new String[]{q, q}, null, null, cols[3] +" DESC, " + cols[2] + " DESC");
		cursor.moveToFirst();
		showMemos(cursor);
	}
	
	/*
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO
    	MenuInflater mi = getMenuInflater();
    	mi.inflate(R.menu.list_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO
        Log.d( "MemoPadActivity", "onOptions" );
		switch(item.getItemId()){
		case R.id.list_menu_delete:
			//delete();
			break;
		}
		Log.d( "MemoPadActivity", "onOptions_finish" );
		return super.onOptionsItemSelected(item);
	}

	
	@Override
	protected void onDestroy() {
		// TODO
		super.onDestroy();
		Log.d("MemoList", "onDestroy");
		//cursor.close();
	} 
}