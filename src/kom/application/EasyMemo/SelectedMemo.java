package kom.application.EasyMemo;

public class SelectedMemo {

	public SelectedMemo() {
		// TODO
	}
	private int memoID;
	private int selectedflag;

	public void setMemoID(int memoID){
	this.memoID = memoID;
	}
	public int getMemoID(){
		return memoID;
	}
	public void setFlag(int selectedflag){
		this.selectedflag = selectedflag;
	}
	public int getFinishFlag() {
		return selectedflag;
	}
}
