package spider.bean;
/**
 * 进度控制类
 * @author Cenby7
 */
public class State {
	private int allState = 0;// 0-停止 1-进行  2-暂停
	
	public final int _STOP = 0;
	public final int _START = 1;
	public final int _SLEEP = 2;
	
	private int mount = 100;//总数
	private int log;//记录已完成数
	public int getAllState() {
		return allState;
	}
	public void setAllState(int allState) {
		this.allState = allState;
	}
	public int getMount() {
		return mount;
	}
	public void setMount(int mount) {
		this.mount = mount;
	}
	
	public boolean ifEnd(){
		log++;
		if(log>=mount){
			allState=_STOP;
			return true;
		}else{
			return false;
		}
	}
}
