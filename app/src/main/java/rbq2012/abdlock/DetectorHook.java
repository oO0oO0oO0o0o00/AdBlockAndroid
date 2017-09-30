package rbq2012.abdlock;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static rbq2012.abdlock.Logger.*;

public class DetectorHook{

	static private DetectorHook instance=null;

	private boolean detecting=false;
	private String pm;
	private Activity activity=null;
	private List<View> listPressedViews;

	private DetectorHook(){
		listPressedViews=new ArrayList<View>();
	}

	static public DetectorHook getDetectorHook(){
		if(null==instance)instance=new DetectorHook();
		return instance;
	}

	public void detRunTest(Activity activity){
		try{
			String strRule=FSUtils.readFile("/sdcard/_#xp/demo.json");
			JSONObject rule=new JSONObject(strRule);
			UiHook.getUiHook().runTest(activity,rule);
		}
		catch(Throwable e){
			Logger.enable();
			Logger.setupDefaultIfNeeded();
			Logger.log("Failed running test due to: ");
			Logger.log(e);
		}
	}

	public void detOnHotKeyPressed(Activity activity){
		this.activity=activity;
		detecting=true;
		Toast.makeText(activity,"现在点击要屏蔽的东西",0).show();
	}

	public boolean detOnTouched(MotionEvent me,String pm){
		Activity activity=this.activity;
		if(!detecting)return false;
		detecting=false;
		try{
			this.pm=pm;
			View v=activity.getWindow().getDecorView();
			screamshot(v,"0");
			saveJson(traverse_views(v),"0");
			listPressedViews.clear();
			getClicked(listPressedViews,v,me.getRawX(),me.getRawY());
			if(listPressedViews.size()<=0){
				Toast.makeText(activity,"没找到任何东西…",0).show();
				return true;
			}
			Dialog dia=new Dialog(activity);
			LinearLayout ll=new LinearLayout(activity);
			ll.setOrientation(LinearLayout.VERTICAL);
			ll.setLayoutParams(new ViewGroup.LayoutParams(
								   ViewGroup.LayoutParams.MATCH_PARENT,
								   ViewGroup.LayoutParams.MATCH_PARENT
							   ));
			TextView tv=new TextView(activity);
			tv.setText("拖拽滚动条，直到看不到要屏蔽的东西，同时能看到其他东西。滑块要尽可能靠右。");
			ll.addView(tv);
			SeekBar sb=new SeekBar(activity);
			sb.setMax(listPressedViews.size()-1);
			sb.setOnSeekBarChangeListener(
				new SeekBar.OnSeekBarChangeListener(){
					@Override
					public void onProgressChanged(SeekBar p1,int p2,boolean p3){
						for(View vv:listPressedViews)vv.setVisibility(View.VISIBLE);
						listPressedViews.get(p2).setVisibility(View.GONE);
					}
					@Override
					public void onStartTrackingTouch(SeekBar p1){
						// TODO: Implement this method
					}
					@Override
					public void onStopTrackingTouch(SeekBar p1){
						// TODO: Implement this method
					}
				}
			);
			sb.setProgress(listPressedViews.size()-1);
			ll.addView(sb);
			LinearLayout lll=new LinearLayout(activity);
			lll.setOrientation(LinearLayout.HORIZONTAL);
			lll.setLayoutParams(new LinearLayout.LayoutParams(
									LinearLayout.LayoutParams.MATCH_PARENT,
									LinearLayout.LayoutParams.WRAP_CONTENT
								));
			Button btn=new Button(activity);
			btn.setText("确定");
			btn.setTag(dia);
			btn.setOnClickListener(
				new OnClickListener(){
					@Override
					public void onClick(View p1){
						Dialog dia=(Dialog) p1.getTag();
						p1.setTag(null);
						dia.dismiss();
					}
				}
			);
			lll.addView(btn);
			btn=new Button(activity);
			btn.setText("永久屏蔽");
			btn.setTag(dia);
			btn.setOnClickListener(
				new OnClickListener(){
					@Override
					public void onClick(View p1){
						Dialog dia=(Dialog) p1.getTag();
						p1.setTag(null);
						dia.dismiss();
						try{
							screamshot(DetectorHook.this.activity.getWindow().getDecorView(),"1");
							for(View v:listPressedViews){
								if(v.getVisibility()==View.GONE){
									JSONObject jso=new JSONObject();
									String des=v.toString();
									jso.put("blocked",des.substring(des.indexOf("{")+1,des.indexOf(" ")));
									saveJson(jso,"blocked");
								}
							}
						}
						catch(Exception e){Logger.log(e);}
						ComponentName cm=new ComponentName(
							"rbq2012.abdlock","rbq2012.abdlock.BlockSolutionsActivity"
						);
						Intent in=new Intent();
						in.setComponent(cm);
						in.putExtra("pkg",DetectorHook.this.pm);
						DetectorHook.this.activity.startActivity(in);
					}
				}
			);
			lll.addView(btn);
			btn=new Button(activity);
			btn.setText("取消");
			btn.setTag(dia);
			btn.setOnClickListener(
				new OnClickListener(){
					@Override
					public void onClick(View p1){
						for(View v:listPressedViews){
							v.setVisibility(View.VISIBLE);
						}
						Dialog dia=(Dialog) p1.getTag();
						p1.setTag(null);
						dia.dismiss();
					}
				}
			);
			lll.addView(btn);
			ll.addView(lll);
			dia.setContentView(ll);
			dia.show();
			dia.setCancelable(false);
			dia.setCanceledOnTouchOutside(false);
		}
		catch(Exception e){
			Toast.makeText(activity,"无法记录当前界面信息",0).show();
			log(e);
		}
		return true;
	}

	private void screamshot(View v,String name) throws FileNotFoundException, IOException{
		Bitmap bmp=Bitmap.createBitmap(
			v.getWidth(),v.getHeight(),
			Bitmap.Config.RGB_565
		);
		Canvas can=new Canvas(bmp);
		v.draw(can);
		File dir=new File("/sdcard/Android/data",pm);
		dir.mkdirs();
		dir=new File(dir,"viewloc");
		dir.mkdir();
		dir=new File(dir,name+".png");
		FileOutputStream fos=new FileOutputStream(dir);
		bmp.compress(Bitmap.CompressFormat.PNG,80,fos);
		fos.flush();fos.close();
	}

	private void saveJson(JSONObject jso,String name) throws IOException, JSONException{
		File dir=new File("/sdcard/Android/data",pm);
		dir.mkdirs();
		dir=new File(dir,"viewloc");
		dir.mkdir();
		dir=new File(dir,name+".json");
		FileWriter fw=new FileWriter(dir);
		fw.write(jso.toString(1));
		fw.close();
	}

	private void getClicked(List<View> list,View root,float tx,float ty){
		if(root.getVisibility()!=View.VISIBLE)return;
		int[] loc=new int[2];
		root.getLocationOnScreen(loc);
		if(tx<loc[0])return;
		if(ty<loc[1])return;
		if(tx>loc[0]+root.getMeasuredWidth())return;
		if(ty>loc[1]+root.getMeasuredHeight())return;
		list.add(root);
		if(!(root instanceof ViewGroup))return;
		ViewGroup g=(ViewGroup) root;
		int c=g.getChildCount();
		for(int i=0;i<c;i++){
			getClicked(list,g.getChildAt(i),tx,ty);
		}
	}

	private static JSONObject traverse_views(View root) throws JSONException{
		if(root==null)return null;
		JSONObject res=new JSONObject();
		String des=root.toString();
		try{res.put("guid",des.substring(des.indexOf("{")+1,des.indexOf(" ")));}
		catch(Exception e){log(e);res.put("guid","[NONE]");}
		res.put("class",root.getClass().getName());
		int id=root.getId();
		res.put("id",id);
		try{
			res.put("idchar",root.getResources().getResourceName(id));
		}
		catch(Exception e){
			res.put("idchar","[NONE]");
		}
		res.put("left",root.getLeft());
		res.put("top",root.getTop());
		res.put("width",root.getWidth());
		res.put("height",root.getHeight());
		int[] loc=new int[2];
		root.getLocationOnScreen(loc);
		res.put("xonscr",loc[0]);
		res.put("yonscr",loc[1]);
		res.put("msdw",root.getMeasuredWidth());
		res.put("msdh",root.getMeasuredHeight());
		if(root instanceof TextView){
			TextView tv=(TextView) root;
			res.put("text",tv.getText());
		}
		if(root instanceof ViewGroup){
			JSONArray ja=new JSONArray();
			ViewGroup vg=(ViewGroup) root;
			int ct=vg.getChildCount();
			for(int i=0;i<ct;i++){
				ja.put(traverse_views(vg.getChildAt(i)));
			}
			res.put("children",ja);
		}
		return res;
	}

}
