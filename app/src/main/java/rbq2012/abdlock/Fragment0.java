package rbq2012.abdlock;
import android.app.Fragment;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import org.json.JSONException;

public class Fragment0 extends BaseFragment{

	private View root;
	
	@Override
	public void onContinue(BlockSolutionsActivity act,BaseFragment fmgr){
		RadioGroup rg=(RadioGroup) root.findViewById(R.id.fbs0RadioGroup1);
		switch(rg.getCheckedRadioButtonId()){
		case R.id.fbs0RadioButton1:
			break;
		case R.id.fbs0RadioButton4:
			try{act.proc.put("mode",4);
			act.testDAndCh(0);
			}
			catch(Exception e){Logger.log(e);}
			break;
		default:
			break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		root=inflater.inflate(R.layout.fbs0,container,false);
		((RadioButton) root.findViewById(R.id.fbs0RadioButton4)).setChecked(true);
		return root;
	}
}
