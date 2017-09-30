package rbq2012.abdlock;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import java.io.File;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.Stack;

public class BlockSolutionsActivity extends Activity{
	
	public UiTree tree;
	private File sav;
	public JSONObject proc;
	private Stack<BaseFragment> fstack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Logger.setup(new File("/sdcard/#_tmp/vl.txt"));
		if(getIntent().getStringExtra("pkg")==null){
			finish();return;
		}
		setContentView(R.layout.ablocksolutions);
		FragmentManager fmgr=getFragmentManager();
		fstack=new Stack<BaseFragment>();
		FragmentTransaction ft=fmgr.beginTransaction();
		ft.add(R.id.ablocksolutionsFrag,new Fragment0(),"");
		proc=new JSONObject();
		ft.commit();
		sav=new File("/sdcard/Android/data",getIntent().getStringExtra("pkg"));
		sav=new File(sav,"viewloc");
		if(!sav.isDirectory()){
			finish();return;
		}try{
		File j=new File(sav,"0.json");
		tree=UiTree.build(FSUtils.readFile(j));
		j=new File(sav,"blocked.json");
		Logger.log(""+tree.locateTarget(FSUtils.readFile(j)));
		Logger.log("123");}catch(Exception e){Logger.log(e);}
	}
	
	public void changeStep(BaseFragment bfrag){
		FragmentTransaction ft=getFragmentManager().beginTransaction();
		ft.replace(R.id.ablocksolutionsFrag,bfrag,"");
		ft.addToBackStack(null);
		ft.commit();
	}
	
	public void setBtn(boolean b){
		int ctn,fns;
		if(b){
			ctn=View.VISIBLE;
			fns=View.GONE;
		}else{
			ctn=View.GONE;
			fns=View.VISIBLE;
		}
		findViewById(R.id.ablocksolutionsButton1).setVisibility(ctn);
		findViewById(R.id.ablocksolutionsButton2).setVisibility(fns);
	}
	
	private boolean testDStep(int step){
		switch(step){
		case 1:return true;
		case 2:{
			UiTree.Element ele=tree.getTarget();
			for(UiTree.Element e=ele;e!=null;e=e.parent){
				String clz=e.clazz.toLowerCase();
				if(clz.contains("scrollview") || clz.contains("listview")){
					return false;
				}
			}return true;
		}
		case 3:return true;
		case 4:return true;
		case 5:{
			int score=0;
			if(proc.has("d1"))score+=25;
			if(proc.has("d2"))score+=40;
			if(proc.has("d3"))score+=20;
			return(score<50);
		}
		case 6:{
				int score=0;
				if(proc.has("d1"))score+=1;
				if(proc.has("d2"))score+=1;
				if(proc.has("d3"))score+=1;
				if(proc.has("d4"))score+=2;
				return(score<3);
		}
		case 7:return true;
		default:return false;
		}
	}
	
	public int testDStepsFrom(int from){
		for(int i=from+1;i<8;i++){
			if(testDStep(i))return i;
		}return -1;
	}
	
	public void testDAndCh(int from){
		int nex=testDStepsFrom(from);
		if(nex==-1)return;
		BaseFragment fr;
		switch(nex){
		case 1:fr=new FragmentD1();break;
		case 2:fr=new FragmentD2();break;
		case 3:fr=new FragmentD3();break;
		case 4:fr=new FragmentD4(this);break;
		case 5:fr=new FragmentD5();break;
		case 6:fr=new FragmentD6();break;
		case 7:fr=new FragmentD7();break;
		default:fr=null;break;
		}
		if(fr!=null)changeStep(fr);
	}

	@Override
	public void onBackPressed(){
		super.onBackPressed();
	}
	
	public void btnContinue(View v){try{
		BaseFragment frag=(BaseFragment) getFragmentManager().findFragmentByTag("");
		frag.onContinue(this,frag);}catch(Exception e){Logger.log(e);}
	}
	
	public void btnFinish(View v){
		try{
			startActivity(
				new Intent(this,SaveRuleActivity.class)
				.putExtra("rule",proc.getJSONObject("res").toString(1))
				.putExtra("pkg",getIntent().getStringExtra("pkg"))
			);
			finish();
		}
		catch(JSONException e){}
	}
}
