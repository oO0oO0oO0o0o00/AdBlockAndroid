package rbq2012.abdlock;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import org.json.JSONArray;
import org.json.JSONException;

public class FragmentD4 extends BaseFragment{

	private View root;
	private BlockSolutionsActivity act;
	private List<UiTree.PathTo> paths;
	private Adapter ada;

	@Override
	public void onContinue(BlockSolutionsActivity act,BaseFragment fmgr){Logger.log("23r");
		JSONArray jsa=new JSONArray();
		for(int i=0;i<paths.size();i++){
			if(ada.checked[i])jsa.put(paths.get(i).serialize());
		}
		if(jsa.length()>0){
			try{
				act.proc.put("d4",jsa);
			}
			catch(Exception e){
			}
		}else{
			act.proc.remove("d4");
		}
		act.testDAndCh(4);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
		root=inflater.inflate(R.layout.fbsd4,container,false);
		UiTree.Element eroot=act.tree.getTarget().parent;
		paths=new ArrayList<UiTree.PathTo>();
		List<String> texts=new ArrayList<String>();
		recursive_getTexts(paths,texts,eroot,act.tree.new PathTo(-1));
		String[] sa=new String[texts.size()];
		for(int i=0;i<texts.size();i++){
			String s=texts.get(i);
			sa[i]=s;
		}
		ListView list=(ListView) root.findViewById(R.id.fbsd4ListView1);
		ada=new Adapter(getActivity(),sa);
		list.setAdapter(ada);
		return root;
	}

	private void recursive_getTexts(List<UiTree.PathTo> paths,List<String> texts,UiTree.Element root,UiTree.PathTo path){

		if(root instanceof UiTree.TextElement){
			UiTree.TextElement te=(UiTree.TextElement) root;
			//if(te.text.replaceAll("\\s*","").equals(""))return;
			paths.add(path);String text=te.text.replace("\r","").replace("\n"," ");
			if(text.length()>50){
				text=text.substring(0,20)+"..."+text.substring(text.length()-20);
			}texts.add(text);return;
		}
		if(root instanceof UiTree.Container){
			UiTree.Container ct=(UiTree.Container) root;
			for(int i=0;i<ct.getChildCount();i++){
				recursive_getTexts(paths,texts,ct.getChildAt(i),act.tree.new PathTo(path,i));
			}
		}
	}

	public FragmentD4(BlockSolutionsActivity act){
		this.act=act;
	}

	class Adapter extends ArrayAdapter<String>{

		boolean[] checked;

		public Adapter(Context ctx,String[] values){
			super(ctx,R.layout.efsd3l0,values);
			checked=new boolean[values.length];
		}
		@Override
		public View getView(int position,View convertView,ViewGroup parent){
			LayoutInflater inflater=LayoutInflater.from(getContext());
			LinearLayout view=(LinearLayout) inflater.inflate(R.layout.efsd3l0,parent,false);
			CheckBox tv=(CheckBox) view.getChildAt(0);
			tv.setText(getItem(position));
			checked[position]=tv.isChecked();
			tv.setTag(position);
			tv.setOnCheckedChangeListener(
				new OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton p1,boolean p2){
						int ind=p1.getTag();
						checked[ind]=p2;
					}
				}
			);
			return view;
		}
	}
}
