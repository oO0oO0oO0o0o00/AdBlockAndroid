package rbq2012.abdlock;
import android.app.Fragment;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import org.json.JSONException;

public class FragmentD2 extends BaseFragment{

	private View root;
	
	@Override
	public void onContinue(BlockSolutionsActivity act,BaseFragment fmgr){
		RadioGroup rg=(RadioGroup) root.findViewById(R.id.fbsd2RadioGroup1);
		switch(rg.getCheckedRadioButtonId()){
		case R.id.fbsd2RadioButton1:
			try{
				act.proc.put("d2",true);
			}
			catch(JSONException e){}
			break;
		case R.id.fbsd2RadioButton2:
				act.proc.remove("d2");
			break;
		default:
			break;
		}
		act.testDAndCh(2);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		root=inflater.inflate(R.layout.fbsd2,container,false);
		((RadioButton) root.findViewById(R.id.fbsd2RadioButton1)).setChecked(true);
		return root;
	}
}
