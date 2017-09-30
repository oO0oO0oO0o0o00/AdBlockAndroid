package rbq2012.abdlock;
import android.app.Fragment;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.app.Activity;

public class FragmentD7 extends BaseFragment{

	private View root;
	
	@Override
	public void onContinue(BlockSolutionsActivity act,BaseFragment fmgr){
		
	}

	@Override
	public void onDetach(){
		BlockSolutionsActivity a=(BlockSolutionsActivity) getActivity();
		a.setBtn(true);
		super.onDetach();
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		BlockSolutionsActivity a=(BlockSolutionsActivity) activity;
		a.setBtn(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		root=inflater.inflate(R.layout.fbsd7,container,false);
		BlockSolutionsActivity bsa=(BlockSolutionsActivity) getActivity();
		try{
			BlockSolutionsActivity act=(BlockSolutionsActivity) getActivity();
		JSONObject res=new JSONObject();
		JSONObject settings=bsa.proc;
			UiTree.Element tar=bsa.tree.getTarget();
		if(settings.has("d2")){
			res.put("xonscr",tar.xonscr);
			res.put("yonscr",tar.yonscr);
		}
		if(settings.has("d3")){
			res.put("msdw",tar.msdw);
			res.put("msdh",tar.msdh);
		}
		if(settings.has("d4")){
			JSONArray arr=settings.getJSONArray("d4");
			for(int i=arr.length()-1;i>=0;i--){
				UiTree.PathTo pt=act.tree.new PathTo(arr.getString(i));
				recursive_settext(pt,tar,res);
			}
		}
		if(settings.has("d5")){
			res.put("idchar",tar.idchar);
			setclss(tar,res);
		}
		if(settings.has("d6")){
			int i=settings.get("d6");
			if(i==2)res=new JSONObject();
			res.put("id",tar.id);
		}Logger.log(res.toString(1));
		settings.put("res",res);
		}catch(Exception e){Logger.log(e);}
		return root;
	}
	
	private void recursive_settext(UiTree.PathTo path,UiTree.Element ele,JSONObject jso) throws JSONException{
		int op=path.enter();
		if(op==-2){
			if(!(ele instanceof UiTree.TextElement))return;
			UiTree.TextElement te=(UiTree.TextElement) ele;
			jso.put("text",te.text);
			return;
		}
		else if(op==-1){
			ele=ele.parent;
			if(!jso.has("parent"))jso.put("parent",new JSONObject());
			jso=jso.getJSONObject("parent");
		}
		else{
			if(!(ele instanceof UiTree.Container))return;
			UiTree.Container c=(UiTree.Container) ele;
			if(c.getChildCount()<=op)return;
			ele=c.getChildAt(op);
			if(!jso.has("children"))jso.put("children",new JSONArray());
			JSONArray jsa=jso.getJSONArray("children");
			for(int i=jsa.length();i<=op;i++){
				jsa.put(new JSONObject());
			}
			jso=jsa.getJSONObject(op);
		}
		if(ele==null)return;
		recursive_settext(path,ele,jso);
	}
	
	private void setclss(UiTree.Element ele,JSONObject jso) throws JSONException{
		jso.put("class",ele.clazz);
		if(ele.parent!=null){
			if(!jso.has("parent"))jso.put("parent",new JSONObject());
			JSONObject jp=jso.getJSONObject("parent");
			jp.put("class",ele.parent.clazz);
			if(ele.parent.clazz.endsWith("ListView")){
				jso.put("class",ele.clazz);
			}else{
				UiTree.Container ep=(UiTree.Container) ele.parent;
				if(!jp.has("children"))jp.put("children",new JSONArray());
				JSONArray jsa=jp.getJSONArray("children");
				for(int i=jsa.length();i<ep.getChildCount();i++){
					jsa.put(new JSONObject());
				}
				for(int i=0;i<ep.getChildCount();i++){
					UiTree.Element el=ep.getChildAt(i);
					if(el==ele)continue;
					JSONObject sub=jsa.getJSONObject(i);
					sub.put("class",el.clazz);
				}
			}
		}
		if(ele.clazz.endsWith("ListView"))return;
		if(ele instanceof UiTree.Container){
			UiTree.Container ct=(UiTree.Container) ele;
			if(!jso.has("children"))jso.put("children",new JSONArray());
			JSONArray jsa=jso.getJSONArray("children");
			for(int i=jsa.length();i<ct.getChildCount();i++){
				jsa.put(new JSONObject());
			}
			for(int i=0;i<ct.getChildCount();i++){
				JSONObject sub=jsa.getJSONObject(i);
				sub.put("class",ct.getChildAt(i).clazz);
			}
		}
	}
}
