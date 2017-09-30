package rbq2012.abdlock;
import android.content.Context;
import java.io.File;
import java.util.List;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Random;

public class BlockRulesMgr{

	static private BlockRulesMgr instance;

	private File dir;
	private List<JSONObject> rules;

	static public BlockRulesMgr get(){
		if(instance==null)instance=new BlockRulesMgr();
		return instance;
	}

	private BlockRulesMgr(){
		dir=new File("/sdcard/Android/data/rbq2012.abdlock/files/block_rules");
		rules=new ArrayList<JSONObject>();
		refresh();
	}

	public void refresh(){
		try{
			dir.mkdirs();
			rules.clear();
			for(File f:dir.listFiles()){
				JSONObject jso=new JSONObject(FSUtils.readFile(f));
				if(!jso.has("rule_content")){
					if(jso.has("rule_name"))continue;
					JSONObject inner=jso;
					jso=new JSONObject();
					jso.put("rule_name","[未命名]");
					jso.put("rule_content",inner);
					FSUtils.writeFile(f,jso.toString(1));
				}rules.add(jso);
			}
		}
		catch(Exception e){}
	}

	public boolean add(JSONObject jso){
		try{
			Random rnd=new Random(System.currentTimeMillis());
			File fn;
			do{
				fn=new File(dir,""+rnd.nextLong()+".json");
			}while(fn.exists());
			FSUtils.writeFile(fn,jso.toString(1));
			rules.add(jso);
			return true;
		}
		catch(Exception e){
			return false;
		}
	}

	public boolean add(String name,String pkg,JSONObject jso){
		try{
			JSONObject outta=new JSONObject();
			outta.put("rule_name",name);
			outta.put("rule_for",pkg);
			outta.put("rule_content",jso);
			return this.add(outta);
		}
		catch(Exception e){
			return false;
		}
	}
	
	public int getCount(){
		return rules.size();
	}
	
	public JSONObject getRule(int ind){
		return rules.get(ind);
	}
	
	public JSONObject getRuleContent(String pkg,int ind){
		try{
			JSONObject jso=rules.get(ind);
			String foor;
			if(jso.has("rule_for"))foor=jso.getString("rule_for");
			else foor="";
			if((!foor.equals(pkg)) && (!foor.equals("")))return null;
			return jso.getJSONObject("rule_content");
		}catch(Exception e){
			Logger.log("ifb");
			Logger.log(e);
			return null;
		}
	}

}
