package rbq2012.abdlock;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import dalvik.system.DexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import eu.chainfire.libsuperuser.Shell;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static de.robv.android.xposed.XposedHelpers.*;
import static rbq2012.abdlock.Logger.*;
import static rbq2012.abdlock.Constants.*;

public class MainHook extends XC_MethodHook
implements IXposedHookLoadPackage{

	//On debug mode, logger is enabled.
	private boolean debug=false;

	//Current package name.
	private String pm=null;

	/* Real magic is in UiHook.java and DetectorHook.java.
	 * We load them dynamically so rebooting is no longer
	 * needed after an usual update of this module.
	 * UiHook checks any view and decide whether to
	 * block it according to user-selected rules.
	 * DetectorHook deals with "tap to block", that when
	 * user pressed the hotkey, we capture the next screen
	 * touch, record the UI structure, take a screenshot
	 * and find out all possible clicked views, such as 
	 * a button and its containers. Then we popup a dialog
	 * allowing user to decide which to block, then switch
	 * to AbDlock's guidance activity.
	 */
	private Class clsUiHook;
	private Class clsDetectorHook;
	private Method metGetUiHook;
	private Method metCheckAndBlockView;
	private Method metCheckContentView;
	private Method metGetDetectorHook;
	private Method metDetRunTest;
	private Method metDetOnHotKeyPressed;
	private Method metDetOnTouched;

	/* Xposed calls this method when an app was started.
	 */
	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p1) throws Throwable{

		/* HandleLoadPackage was often called more 
		 * than once. We shouldnt repeat the same
		 * things.
		 */
		if(pm!=null) return;

		/* We get this fxxking package name in any apps sometimes
		 * for unknown reason. It sux.
		 */
		if(p1.packageName.startsWith("com.google.android."))return;

		pm=p1.packageName;

		try{
			/* If test file exists enable debug.
			 * Notice that if current app cant access sdcard we cant debug at all.
			 */
			if(new File("/sdcard/##日了狗了").exists())debug=true;
		}
		catch(Exception e){debug=false;}
		if(debug)Logger.enable();

		Logger.setupDefaultIfNeeded();

		try{
			/* Dynamically load classes.
			 * First we need to get the path of our apk.
			 */
			String pd=null;
			for(String s:Shell.SH.run("pm path rbq2012.abdlock")){
				if(s.startsWith("package:")){
					pd=s.substring(8);
					break;
				}
			}
			DexClassLoader cl=new DexClassLoader(pd,p1.appInfo.dataDir,
												 "",p1.getClass().getClassLoader());
			clsUiHook=cl.loadClass("rbq2012.abdlock.UiHook");
			metCheckAndBlockView=clsUiHook.getDeclaredMethod("checkAndBlockView",View.class,Integer.TYPE);
			metCheckContentView=clsUiHook.getDeclaredMethod("checkContentView",Activity.class);
			metGetUiHook=clsUiHook.getDeclaredMethod("getUiHook");
			clsDetectorHook=cl.loadClass("rbq2012.abdlock.DetectorHook");
			metGetDetectorHook=clsDetectorHook.getDeclaredMethod("getDetectorHook");
			metDetRunTest=clsDetectorHook.getDeclaredMethod("detRunTest",Activity.class);
			metDetOnHotKeyPressed=clsDetectorHook.getDeclaredMethod("detOnHotKeyPressed",Activity.class);
			metDetOnTouched=clsDetectorHook.getDeclaredMethod("detOnTouched",MotionEvent.class,String.class);
		}
		catch(Exception e){
			/* If it fails we cant do anything.
			 * The reason is that this module is uninstalled but
			 * user hasn't reboot the phone.
			 */
			return;
		}
		
		/* Hook to capture key events
		 * not working wechat but no one need to block anything
		 * on wechat's user interface so we won't fix it.
		 */
		XC_MethodHook hook=new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable{
				int kc=param.args[0];
				KeyEvent ke=(KeyEvent) param.args[1];
				if(ke.getRepeatCount()>0) return;

				if(kc==KeyEvent.KEYCODE_VOLUME_UP){
					//Run adblock test.
					if(!debug)return;
					Object dethook=metGetDetectorHook.invoke(clsDetectorHook);
					metDetRunTest.invoke(dethook,(Activity) param.thisObject);
				}
				else if(kc==KeyEvent.KEYCODE_VOLUME_DOWN){
					//
					XSharedPreferences xpref=new XSharedPreferences(PACKAGE_NAME_ME,SPREF_MAIN);
					if(!xpref.getBoolean(SPREF_MAIN_NAME_T2ADD,false))return;
					Object dethook=metGetDetectorHook.invoke(clsDetectorHook);
					metDetOnHotKeyPressed.invoke(dethook,(Activity) param.thisObject);
					param.setResult(true);
				}
			}
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable{
			}
		};
		findAndHookMethod(Activity.class,"onKeyDown",Integer.TYPE,KeyEvent.class,hook);

		hook=new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable{
				//
				MotionEvent me=(MotionEvent) param.args[0];
				if(me.getAction()!=MotionEvent.ACTION_DOWN)return;
				Object dethook=metGetDetectorHook.invoke(clsDetectorHook);
				if((boolean)metDetOnTouched.invoke(dethook,me,pm))param.setResult(true);
			}
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable{
			}
		};
		findAndHookMethod(ViewGroup.class,"onInterceptTouchEvent",MotionEvent.class,hook);

		//Only if the main switch is on.
		XSharedPreferences xpref=new XSharedPreferences(PACKAGE_NAME_ME,SPREF_MAIN);
		if(xpref.getBoolean(SPREF_MAIN_NAME_MAIN,true)){
			
			//Add all rules related with current app.
			BlockRulesMgr rmgr=BlockRulesMgr.get();
			Method metAddRule=clsUiHook.getMethod("addRule",JSONObject.class);
			Object uihook=metGetUiHook.invoke(clsUiHook);
			for(int i=rmgr.getCount()-1;i>=0;i--){
				JSONObject jso=rmgr.getRuleContent(pm,i);
				if(jso==null)continue;
				metAddRule.invoke(uihook,jso);
			}

			/* Hook many of UI-related functions that are
			 * likely to be called when a view is newly 
			 * created and added to UI, in order to have
			 * a larger chance of successfully decide if
			 * a view should be blocked or not. Yes it
			 * consumes cpu and battery so we would try to
			 * reduce the number of hooks in the future.
			 * Unfortunately such an update will require
			 * a reboot.
			 */
			hook=new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable{
					View view=(View)param.thisObject;
					checkAndBlockView(view,0);
				}
			};
			findAndHookConstructor(View.class,Context.class,hook);
			findAndHookConstructor(View.class,Context.class,AttributeSet.class,hook);
			findAndHookConstructor(View.class,Context.class,AttributeSet.class,Integer.TYPE,hook);
			findAndHookConstructor(View.class,Context.class,AttributeSet.class,Integer.TYPE,Integer.TYPE,hook);

			hook=new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable{
					if(param.thisObject instanceof ViewGroup){
						View view=(View)param.thisObject;
						checkAndBlockView(view,0);
					}
					View view=(View)param.args[0];
					checkAndBlockView(view,0);
				}
			};
			findAndHookMethod(ViewGroup.class,"addView",View.class,Integer.TYPE,Integer.TYPE,hook);
			findAndHookMethod(ViewGroup.class,"addView",View.class,Integer.TYPE,hook);
			findAndHookMethod(ViewGroup.class,"addView",View.class,hook);
			findAndHookMethod(ViewGroup.class,"addView",View.class,Integer.TYPE,ViewGroup.LayoutParams.class,hook);
			findAndHookMethod(ViewGroup.class,"addView",View.class,ViewGroup.LayoutParams.class,hook);

			hook=new XC_MethodHook(){
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable{
				}
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable{
					if(!(param.thisObject instanceof TextView))return;
					TextView view=(TextView)param.thisObject;
					checkAndBlockView(view,0);
				}
			};
			findAndHookMethod(TextView.class,"setText",CharSequence.class,hook);

			hook=new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable{
					Activity act=(Activity) param.thisObject;
					checkContentView(act);
				}
			};
			findAndHookMethod(Activity.class,"onResume",hook);
		}
	}

	private void checkAndBlockView(View view,int ev){
		try{
			Object uihook=metGetUiHook.invoke(clsUiHook);
			metCheckAndBlockView.invoke(uihook,view,ev);
		}
		catch(Throwable e){log(e);}
	}

	private void checkContentView(Activity activity){
		try{
			Object uihook=metGetUiHook.invoke(clsUiHook);
			metCheckContentView.invoke(uihook,activity);
		}
		catch(Throwable e){log(e);}
	}
}
