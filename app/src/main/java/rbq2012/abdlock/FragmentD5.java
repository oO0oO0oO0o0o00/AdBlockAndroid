package rbq2012.abdlock;
import android.app.Fragment;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import org.json.JSONException;

public class FragmentD5 extends BaseFragment{

	private View root;
	
	@Override
	public void onContinue(BlockSolutionsActivity act,BaseFragment fmgr){
		RadioGroup rg=(RadioGroup) root.findViewById(R.id.fbsd5RadioGroup1);
		switch(rg.getCheckedRadioButtonId()){
		case R.id.fbsd5RadioButton1:
			try{
				act.proc.put("d5",true);
			}
			catch(JSONException e){}
			break;
		case R.id.fbsd5RadioButton2:
			act.proc.remove("d5");
			break;
		default:
			break;
		}
		act.testDAndCh(5);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		root=inflater.inflate(R.layout.fbsd5,container,false);
		((RadioButton) root.findViewById(R.id.fbsd5RadioButton1)).setChecked(true);
		return root;
	}
}
