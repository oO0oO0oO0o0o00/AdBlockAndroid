package rbq2012.abdlock;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class UiHook{
	
	static private UiHook instance=null;
	
	private List<JSONObject> rules;
	
	private UiHook(){
		rules=new ArrayList<JSONObject>();
	}
	
	static public UiHook getUiHook(){
		if(instance==null)instance=new UiHook();
		return instance;
	}
	
	public void enableDebug(){
		Logger.enable();
	}
	
	public void addRule(JSONObject rule){
		rules.add(rule);
	}
	
	public void runTest(Activity activity,JSONObject rule) throws Throwable{
		List<JSONObject> bak=rules;
		rules=new ArrayList<JSONObject>();
		rules.add(rule);
		Throwable err=null;
		try{
			checkContentView(activity);
		}catch(Throwable e){
			err=e;
		}
		rules.clear();
		rules=bak;
		if(err!=null)throw err;
	}
	
	public void checkAndBlockView(View view,int event){
		for(JSONObject rule:rules){
			if(traverse_match_property(view,rule)){
				view.setVisibility(View.GONE);
				view.setAlpha(0.01f);
				break;
			}
		}
	}
	
	public void checkContentView(Activity activity){
		new UiProcThread(activity,new int[]{
			100,1000
		}).start();
	}
	
	static private boolean traverse_match_property(View view,JSONObject patt){
		Logger.setupIfNeed(new File("/sdcard/_#xp/logg.txt"));
		if(view==null)return false;//Logger.log("iui");
		for(Iterator<String> it=patt.keys();it.hasNext();){
			String s=it.next();
			try{
				switch(s){
				case "xonscr":{
						int[] loc=new int[2];
						view.getLocationOnScreen(loc);
						if(patt.getDouble(s)-loc[0]>=0.1)return false;
					}
					break;
				case "yonscr":{
						int[] loc=new int[2];
						view.getLocationOnScreen(loc);
						if(patt.getDouble(s)-loc[1]>=0.1)return false;
					}
					break;
				case "msdw":
					if(patt.getDouble(s)-view.getMeasuredWidth()>=0.1)return false;
					break;
				case "msdh":
					if(patt.getDouble(s)-view.getMeasuredHeight()>=0.1)return false;
					break;
				case "idchar":
				case "idstring":
				case "idstr":
					try{
						String res=patt.getString(s);
						String id;
						try{
							id=view.getResources().getResourceName(view.getId());
						}
						catch(Exception e){
							id="[NONE]";
						}
						if(!res.equals(id)&&(res.contains(":")||!res.endsWith(id)
						   ))return false;
					}
					catch(Exception e){return false;}
					break;
				case "class":{
						String res=patt.getString(s);
						String cl=view.getClass().getName();
						if(!res.equals(cl)&&(res.contains(".")||!cl.endsWith(res)))return false;
					}
					break;
				case "text":{
						if(!(view instanceof TextView))return false;
						String txt=patt.getString(s);
						TextView tv=(TextView) view;
						if(tv.getText()==null)return false;
						if(!tv.getText().toString().equals(txt))return false;
					}break;
				case "parent":{
						ViewParent vp=view.getParent();
						if(!(vp instanceof View))return false;
						if(!traverse_match_property(
							   (View)(view.getParent()),patt.getJSONObject(s)
						   ))return false;
						break;
					}
				case "children":{
						if(!(view instanceof ViewGroup))return false;
						ViewGroup vg=(ViewGroup) view;
						JSONArray jsa=patt.getJSONArray(s);
						if(vg.getChildCount()!=jsa.length())return false;
						for(int i=jsa.length()-1;i>=0;i--){
							if(!traverse_match_property(
								   vg.getChildAt(i),jsa.getJSONObject(i)
							   ))return false;
						}
						break;
					}
				}
			}
			catch(Exception e){
				Logger.log("UiLocator failed:");
				Logger.log(e);
			}
		}
		return true;
	}
	
	static private void traverse_find(View root,List<View> views,JSONObject pattern){
		Logger.setupIfNeed(new File("/sdcard/_#xp/logg.txt"));
		if(root==null)return;
		try{
			if(traverse_match_property(root,pattern))views.add(root);
		}
		catch(Throwable e){
			Logger.log(e);
		}
		if(root instanceof ViewGroup){
			ViewGroup vg=(ViewGroup) root;
			int max=vg.getChildCount();
			for(int i=0;i<max;i++){
				traverse_find(vg.getChildAt(i),views,pattern);
			}
		}Logger.log(""+views.size());
	}
	
	private class UiProcThread extends Thread{

		private Activity act;
		private int[] times;

		public UiProcThread(Activity act,int[] timesMillis){
			this.act=act;
			this.times=timesMillis;
		}

		@Override
		public void run(){
			int prev=0;
			for(int i:times){
				try{
					Thread.currentThread().sleep(i-prev);
				}
				catch(InterruptedException e){}
				prev=i;
				act.runOnUiThread(new UiProcPostedRunnable());
				//Logger.log("isj.");
			}
		}

		private class UiProcPostedRunnable implements Runnable{
			@Override
			public void run(){
				Activity act=UiProcThread.this.act;
				List<View> list=new ArrayList<View>();
				try{
					for(JSONObject rule:rules){
						traverse_find(act.getWindow().getDecorView(),list,rule);
					}
				}
				catch(Exception e){Logger.log(e);}
				for(View v:list){
					checkAndBlockView(v,0);
					//Logger.log("aoj."+v);
					//lis.onViewFound(param,v,UiLocatorEvent.ACTICREATED);
				}
			}
		}
	}
}
