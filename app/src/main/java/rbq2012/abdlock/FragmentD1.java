package rbq2012.abdlock;
import android.app.Fragment;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import org.json.JSONException;
import android.app.Activity;

public class FragmentD1 extends BaseFragment{

	private View root;
	
	@Override
	public void onContinue(BlockSolutionsActivity act,BaseFragment fmgr){
		act.testDAndCh(1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		root=inflater.inflate(R.layout.fbsd1,container,false);
		return root;
	}
}
