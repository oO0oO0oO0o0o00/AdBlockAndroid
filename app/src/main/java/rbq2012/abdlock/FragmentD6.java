package rbq2012.abdlock;
import android.app.Fragment;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import org.json.JSONException;

public class FragmentD6 extends BaseFragment{

	private View root;
	
	@Override
	public void onContinue(BlockSolutionsActivity act,BaseFragment fmgr){
		RadioGroup rg=(RadioGroup) root.findViewById(R.id.fbsd6RadioGroup1);
		switch(rg.getCheckedRadioButtonId()){
		case R.id.fbsd6RadioButton1:
			try{
				act.proc.put("d6",2);
			}
			catch(JSONException e){}
			break;
		case R.id.fbsd6RadioButton2:
			try{
				act.proc.put("d6",1);
			}
			catch(JSONException e){}
			break;
		default:
			act.proc.remove("d6");
			break;
		}
		act.testDAndCh(6);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		root=inflater.inflate(R.layout.fbsd6,container,false);
		((RadioButton) root.findViewById(R.id.fbsd6RadioButton3)).setChecked(true);
		return root;
	}
}
