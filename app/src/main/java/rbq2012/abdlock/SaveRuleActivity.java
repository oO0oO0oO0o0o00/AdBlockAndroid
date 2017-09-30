package rbq2012.abdlock;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;
import android.widget.CheckBox;

public class SaveRuleActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.asaverule);
	}

	public void onFinish(View v){
		try{
			EditText et=(EditText) findViewById(R.id.asaveruleEditText1);
			String name=et.getText().toString().trim();
			if(name.equals("")){
				Toast.makeText(this,"0",0).show();
				return;
			}
			CheckBox cb=(CheckBox) findViewById(R.id.asaveruleCheckBox1);
			if(BlockRulesMgr.get().add(
				name,cb.isChecked() ? "" : getIntent().getStringExtra("pkg"),
				new JSONObject(getIntent().getStringExtra("rule"))
			)){
				Toast.makeText(this,"已保存",0).show();
				finish();
				return;
			}
		}
		catch(Throwable e){
		}
		Toast.makeText(this,"保存失败",0).show();
	}
}
