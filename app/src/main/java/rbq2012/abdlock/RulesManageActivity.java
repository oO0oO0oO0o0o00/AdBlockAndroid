package rbq2012.abdlock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

import static rbq2012.abdlock.Constants.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Random;

public class RulesManageActivity extends Activity{

	private MAdapter ada;
	static private String DIR_RULES="/sdcard/Android/data/rbq2012.abdlock/files/block_rules";
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.arulesmanage);
		SharedPreferences spref=getSharedPreferences(SPREF_INTERNAL,MODE_PRIVATE);
		if(!spref.getBoolean(SPREF_INTERNAL_NAME_EXTRACTEDBUILTINRULES,false)){
			AssetManager am=getAssets();
			try{
				Random rnd=new Random(System.currentTimeMillis());
				File dir=new File(DIR_RULES);
				for(String s:am.list("built_in_rules")){
					InputStream ins=am.open("built_in_rules/"+s);
					InputStreamReader reader=new InputStreamReader(ins);
					BufferedReader br=new BufferedReader(reader);
					StringBuilder sb=new StringBuilder();
					String line;
					while((line=br.readLine())!=null){
						sb.append(line).append("\n");
					}
					FSUtils.writeFile(new File(dir,""+rnd.nextLong()+".json"),sb.toString());
					spref.edit().putBoolean(SPREF_INTERNAL_NAME_EXTRACTEDBUILTINRULES,true).apply();
				}
			}
			catch(IOException e){}
		}
		ListView lv=(ListView) findViewById(R.id.arulesmanageListView1);
		ada=new MAdapter(this);
		lv.setAdapter(ada);
	}
	
	class MAdapter extends BaseAdapter
		implements CompoundButton.OnCheckedChangeListener,
		View.OnClickListener,
	DialogInterface.OnClickListener{

		private Context ctx;
		private List<JSONObject> rules;
		private List<File> persists;
		
		private int tobedeleted;

		@Override
		public void onClick(DialogInterface p1,int p2){
			int tobedeleted=this.tobedeleted;
			if(tobedeleted<0)return;
			if(tobedeleted>=rules.size())return;
			rules.remove(tobedeleted);
			persists.get(tobedeleted).delete();
			persists.remove(tobedeleted);
			notifyDataSetChanged();
		}
		
		@Override
		public void onCheckedChanged(CompoundButton p1,boolean p2){
			int ind=p1.getTag();
			JSONObject jso=rules.get(ind);
			try{
				jso.put(JSON_RULE_KEY_DISABLED,!p2);
				FSUtils.writeFile(persists.get(ind),jso.toString(1));
			}
			catch(JSONException e){}
		}

		@Override
		public void onClick(View p1){
			int ind=p1.getTag();
			AlertDialog dia=new AlertDialog.Builder(ctx)
				.setMessage("确定删除？")
				.setPositiveButton("嗯嗯",this)
				.create();
			tobedeleted=ind;
			dia.show();
		}

		public MAdapter(Context ctx){
			this.rules=new ArrayList<JSONObject>();
			this.persists=new ArrayList<File>();
			this.ctx=ctx;
			tobedeleted=-1;
			update();
		}
		
		public void update(){
			File dir=new File(
				DIR_RULES
			);
			dir.mkdirs();
			rules.clear();persists.clear();
			for(File f:dir.listFiles()){
				if(!f.getName().endsWith(".json"))continue;
				try{
					JSONObject jso=new JSONObject(FSUtils.readFile(f));
					if(!jso.has(JSON_RULE_KEY_CONTENT)){
						JSONObject inner=jso;
						jso=new JSONObject();
						jso.put(JSON_RULE_KEY_CONTENT,inner);
					}if(!jso.has(JSON_RULE_KEY_NAME))jso.put(JSON_RULE_KEY_NAME,"[未命名]");
					rules.add(jso);
					persists.add(f);
				}
				catch(JSONException e){}
			}
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount(){
			return rules.size();
		}

		@Override
		public JSONObject getItem(int p1){
			if(p1<0)return null;
			if(p1>=rules.size())return null;
			return rules.get(p1);
		}

		@Override
		public long getItemId(int p1){
			return p1;
		}

		@Override
		public View getView(int p1,View p2,ViewGroup p3){
			View view=LayoutInflater.from(ctx).inflate(R.layout.erulesmanage0,p3,false);
			CheckBox ckb=(CheckBox) view.findViewById(R.id.erulesmanage0CheckBox1);
			ckb.setTag(p1);
			ImageView imv=(ImageView) view.findViewById(R.id.erulesmanage0ImageView1);
			imv.setOnClickListener(this);
			imv.setTag(p1);
			TextView tv=(TextView) view.findViewById(R.id.erulesmanage0TextView1);
			JSONObject jso=rules.get(p1);
			try{
				ckb.setText(jso.getString(JSON_RULE_KEY_NAME));
				if(jso.has(JSON_RULE_KEY_FOR))tv.setText(jso.getString(JSON_RULE_KEY_FOR));
				else tv.setText("全部应用");
				boolean en=true;
				if(jso.has(JSON_RULE_KEY_DISABLED))
					if(jso.getBoolean(JSON_RULE_KEY_DISABLED))en=false;
				if(en)ckb.setChecked(true);
			}
			catch(JSONException e){}
			ckb.setOnCheckedChangeListener(this);
			return view;
		}
		
		
	}
	
}
