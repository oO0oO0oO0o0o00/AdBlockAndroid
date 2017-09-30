package rbq2012.abdlock;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import dalvik.system.DexClassLoader;
import eu.chainfire.libsuperuser.Shell;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import org.json.JSONObject;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.SharedPreferences;

import static rbq2012.abdlock.Constants.*;
import android.content.Intent;

public class MainActivity extends Activity
	implements CompoundButton.OnCheckedChangeListener,
View.OnClickListener{
	
	static public MainActivity instance;

	private TextView tvLog;
	private StringBuilder sbLog;

	private TextView tvTap2add;
	private SharedPreferences spref;

	@Override
	public void onCheckedChanged(CompoundButton p1,boolean p2){
		switch(p1.getId()){
		case R.id.mainSwitch1:{
				SharedPreferences.Editor edit=spref.edit();
				edit.putBoolean(SPREF_MAIN_NAME_MAIN,p2);
				edit.apply();
				break;
			}
		case R.id.mainSwitch2:{
				SharedPreferences.Editor edit=spref.edit();
				edit.putBoolean(SPREF_MAIN_NAME_T2ADD,p2);
				edit.apply();
				if(p2) tvTap2add.setVisibility(View.VISIBLE);
				else tvTap2add.setVisibility(View.GONE);
				break;
			}
		}
	}

	@Override
	public void onClick(View p1){
		startActivity(
			new Intent(this,RulesManageActivity.class)
		);
	}
	

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		spref=getSharedPreferences(SPREF_MAIN,MODE_WORLD_READABLE);
		{
			Switch sw=(Switch) findViewById(R.id.mainSwitch1);
			sw.setChecked(spref.getBoolean(SPREF_MAIN_NAME_MAIN,true));
			sw.setOnCheckedChangeListener(this);
			sw=(Switch) findViewById(R.id.mainSwitch2);
			sw.setChecked(spref.getBoolean(SPREF_MAIN_NAME_T2ADD,false));
			tvTap2add=(TextView) findViewById(R.id.mainTextView1);
			if(!sw.isChecked())tvTap2add.setVisibility(View.GONE);
			TextView tv=(TextView) findViewById(R.id.mainTextView2);
			tv.setOnClickListener(this);
			sw.setOnCheckedChangeListener(this);
		}
		instance=this;
    }

	public void log(String s){
		sbLog.append(">>> ");
		sbLog.append(s);
		sbLog.append("\n");
		tvLog.setText(sbLog);
	}

	public void log(Throwable e){
		StringWriter sw=new StringWriter();
		PrintWriter pw=new PrintWriter(sw);
		e.printStackTrace(pw);
		log(sw.toString());
		pw.close();
	}

	public static void travViews(StringBuilder log,View root,int depth){
		String title,indent;
		{
			//root.setVisibility(View.INVISIBLE);
			StringBuilder sb=new StringBuilder();
			for(int i=depth;i>0;i--){
				sb.append(">");
			}
			indent=sb.toString();
			if(root instanceof EditText){
				title="EditText";
			}else if(root instanceof TextView){
				title="TextView";
			}else if(root instanceof Button){
				title="Button";
			}else if(root instanceof ImageView){
				title="Image";
			}else if(root instanceof ViewGroup){
				title="Container";
			}else{
				title="View";
			}
		}
		log.append("\n");
		log.append(indent);
		log.append("Begin ");
		log.append(title);
		log.append("\n");
		log.append(indent);
		log.append("ID: ");
		int id=root.getId();
		log.append(id);
		log.append(", ");
		try{
			log.append(root.getResources().getResourceName(id));
		}
		catch(Exception e){
			log.append("[Unknown]");
		}
		log.append("\n");
		log.append(indent);
		log.append("Type: ");
		log.append(root.getClass().getName());
		log.append("\n");
		if(root instanceof TextView){
			TextView tv=(TextView) root;
			log.append(indent);
			log.append("Text: ");
			String text=tv.getText().toString();
			boolean dots=false;
			if(text.length()>80){
				text=text.substring(0,79);
				dots=true;
			}
			log.append(text.replace("\n","\\n"));
			if(dots)log.append("...");
			log.append("\n");
		}
		{
			log.append(indent);
			log.append("Left Top Width Height: ");
			log.append(root.getLeft());log.append(",");
			log.append(root.getTop());log.append(",");
			log.append(root.getWidth());log.append(",");
			log.append(root.getHeight());log.append(",");
			log.append("\n");
		}
		if(root instanceof ViewGroup){
			depth++;
			ViewGroup vg=(ViewGroup) root;
			int c=vg.getChildCount();
			for(int i=0;i<c;i++){
				travViews(log,vg.getChildAt(i),depth);
			}
		}
		log.append(indent);
		log.append("End ");
		log.append(title);
		log.append("\n");
	}
}
