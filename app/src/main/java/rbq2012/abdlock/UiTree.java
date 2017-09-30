package rbq2012.abdlock;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

public class UiTree{
	
	private Element root;
	private Element target;
	
	public UiTree(Element root){
		this.root=root;
	}
	
	private UiTree(){}
	private void setRoot(Element root){
		this.root=root;
	}
	
	static public UiTree build(String json){
		try{
			JSONObject jso=new JSONObject(json);
			UiTree uitree=new UiTree();
			Element root=uitree.new Container();
			uitree.root=root;
			uitree.recursive_build(root,jso,null);
			return uitree;
		}catch(Exception e){
			Logger.log(e);
			return null;
		}
	}
	
	public boolean locateTarget(String json){
		try{
			JSONObject jso=new JSONObject(json);
			String guid=jso.getString("blocked");
			return recursive_locate(root,guid);
		}
		catch(JSONException e){
			return false;
		}
	}
	
	public Element getTarget(){
		return target;
	}
	
	private boolean recursive_locate(Element root,String guid){
		if(root.guid.equals(guid)){
			target=root;return true;
		}
		if(root instanceof Container){
			Container ct=(UiTree.Container) root;
			for(Element e:ct.children){
				if(recursive_locate(e,guid))return true;
			}
		}
		return false;
	}
	
	private boolean recursive_build(Element root,JSONObject jso,Container parent){
		try{
			root.left=jso.getInt("left");
			root.top=jso.getInt("top");
			root.width=jso.getInt("width");
			root.height=jso.getInt("height");
		}//These fields are required.
		catch(JSONException e){return false;}
		try{
			root.xonscr=jso.getDouble("xonscr");
			root.yonscr=jso.getDouble("yonscr");
			root.msdw=jso.getDouble("msdw");
			root.msdh=jso.getDouble("msdh");
		}catch(JSONException e){}
		root.parent=parent;
		try{
			root.clazz=jso.getString("class");
			root.guid=jso.getString("guid");
			root.id=jso.getInt("id");
			root.idchar=jso.getString("idchar");
		}
		catch(JSONException e){return false;}
		if(root instanceof Container)try{
			JSONArray jsa=jso.getJSONArray("children");
			for(int i=0;i<jsa.length();i++){
				JSONObject sub=jsa.getJSONObject(i);
				Element ch;
				if(sub.has("text"))ch=new TextElement();
				else if(sub.has("children"))ch=new Container();
				else ch=new Element();
				Container ct=(UiTree.Container) root;
				ct.addChild(ch);
				recursive_build(ch,sub,ct);
			}
		}
		catch(JSONException e){}
		else if(root instanceof TextElement)try{
			TextElement te=(UiTree.TextElement) root;
			te.text=jso.getString("text");
		}catch(Exception e){}
		return true;
	}
	
	class Element{
		public double xonscr,yonscr,msdw,msdh;
		public int left,top,width,height,id;
		public String clazz,guid,idchar;
		public Element parent;
	}
	
	class TextElement extends Element{
		public String text;
	}
	
	class Container extends Element{
		public List<Element> children;
		public Container(){
			children=new ArrayList<Element>();
		}
		public int getChildCount(){
			return children.size();
		}
		public Element getChildAt(int i){
			return children.get(i);
		}
		public void addChild(Element ele){
			children.add(ele);
		}
	}
	
	class PathTo{
		private List<Integer> list;
		private int pos;
		//-1=parent 0~...=child at 0~...
		public PathTo(int seg){
			list=new ArrayList<Integer>();
			list.add(seg);pos=0;
		}
		public PathTo(PathTo prev,int seg){
			list=new ArrayList<Integer>();
			for(int i:prev.list){
				list.add(i);
			}list.add(seg);pos=0;
		}
		public PathTo(String data){
			list=new ArrayList<Integer>();
			String[] arr=data.split(",");
			for(String s:arr){
				list.add(Integer.parseInt(s));
			}pos=0;
		}
		public int getSegCount(){
			return list.size();
		}
		public int getSegAt(int i){
			return list.get(i);
		}
		public String serialize(){
			StringBuilder sb=new StringBuilder();
			sb.append(list.get(0));
			for(int i=1;i<list.size();i++){
				sb.append(",").append(list.get(i));
			}return sb.toString();
		}
		public int enter(){
			if(pos>=list.size())return -2;
			int i=list.get(pos);pos++;
			return i;
		}
		public void reset(){
			pos=0;
		}
	}
	
}
