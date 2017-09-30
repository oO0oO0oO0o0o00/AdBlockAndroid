package rbq2012.abdlock;

import java.io.*;

import android.os.Environment;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import android.content.res.AssetManager;

public class FSUtils {
	public static boolean makeSureThisDirExists(String dir, boolean allowAutocreate) {
		File dirf=new File(dir);
		return makeSureThisDirExists(dirf, allowAutocreate);
	}

	public static boolean makeSureThisDirExists(File dir, boolean allowAutocreate) {
		if (dir.isDirectory())return true;
		try {
			dir.mkdirs();
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}

	public static String readFile(String path) {
		return readFile(new File(path));
	}

	public static String readFile(File file) {
		String text;
		try {
			BufferedReader br=new BufferedReader(new FileReader(file));
			String temp=null;
			StringBuffer sb=new StringBuffer();
			temp = br.readLine();
			while (temp != null) {
				sb.append(temp + "\n");
				temp = br.readLine();
			}
			if (sb.length() == 0)return "";
			text = sb.substring(0, sb.length() - 1);
			//text=sb.toString();
			return text;
		}
		catch (IOException e) {
			return null;
		}
	}
	
	public static boolean appendFile(String name,String content){
		try {
			File f=new File(name);
			if (!f.exists()) {
				f.createNewFile();
			}
			FileWriter fw=new FileWriter(name, true);
			BufferedWriter bw=new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public static boolean writeFile(String name, String content) {
		try {
			File f=new File(name);
			if (!f.exists()) {
				f.createNewFile();
			}
			FileWriter fw=new FileWriter(name, false);
			BufferedWriter bw=new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public static boolean writeFile(File file, String content) {
		try {
			//File f=new File(name);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw=new FileWriter(file.getAbsolutePath(), false);
			BufferedWriter bw=new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public static boolean appendFile(File file, String content) {
		try {
			//File f=new File(name);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw=new FileWriter(file.getAbsolutePath(), true);
			BufferedWriter bw=new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public static void fileChannelCopy(File s, File t) throws IOException {
		FileInputStream fi = null;
		FileOutputStream fo = null;
		FileChannel in = null;
		FileChannel out = null;
		fi = new FileInputStream(s);
		fo = new FileOutputStream(t);
		in = fi.getChannel();//得到对应的文件通道
		out = fo.getChannel();//得到对应的文件通道
		in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
		fi.close();
		in.close();
		fo.close();
		out.close();
	}
	
	public static void copyFolder(String src,String dst) throws IOException{
		new File(dst).mkdir();
		copyFolderIter(new File(src),src.length(),dst);
	}
	
	public static void copyFolder(File src,File dst) throws IOException{
		dst.mkdir();
		copyFolderIter(src,src.getAbsolutePath().length(),dst.getAbsolutePath());
	}
	
	private static void copyFolderIter(File folder,int s,String d) throws IOException{
		File[] files=folder.listFiles();
		for (File file:files){
			File newfile=new File(d+file.getAbsolutePath().substring(s));
			if (file.isDirectory()) {
				newfile.mkdir();
				copyFolderIter(file,s,d);
			}
			else {
				FSUtils.fileChannelCopy(file,newfile);
			}
		}
	}
	
	public static File extractAsset(AssetManager am,String name,File dir){
		InputStream is;
		try {
			is = am.open(name);
			FileOutputStream fos;
			File full=new File(dir,name);
			full.getParentFile().mkdirs();
			fos = new FileOutputStream(full);
			int len;
			byte[] buf=new byte[1024];
			while ((len = is.read(buf)) != -1) {
				fos.write(buf, 0, len);
			}
			//fos.flush();

			return full;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getGreet(){
		List<String> QQs=new ArrayList<String>();
		File sd卡=Environment.getExternalStorageDirectory();
		File qq=new File(sd卡,"tencent/mobileQQ");
		if (qq.exists()){
			for (File qqf:qq.listFiles()){
				String qqfname=qqf.getName();
				if (qqfname.matches("[0-9]{9,10}") && new File(qqf,"ptt").isDirectory()){
					QQs.add(qqfname);
				}
			}
		}
		for(String QQ:QQs){
			if (QQ.equals("1226123914")) return "亲爱的alpha AE，你好~~";
			if (QQ.equals("940261351")) return "亲爱的小枫，你好~~";
			if (QQ.equals("2603877429")) return "QAQ";
		}
		return null;
	}
	
	public static int hexChar2int(char a){
		if (48<=a && a<=57){
			return a-48;
		}
		if (65<=a && a<=90){
			return a-55;
		}
		if (97<=a && a<=122){
			return a-87;
		}
		return -1;
	}
	
	public static char int2hexLowerChar(int a){
		if (0<=a && a<=9){
			return (char)(a+48);
		}
		if (10<=a && a<=15){
			return (char)(a+87);
		}
		return (char)-1;
	}
	
	public static File getUsablePath(String ori){
		File a;
		String aS=ori;
		int count=0;
		while ((a=new File(aS)).exists()){
			aS=ori+"_"+count;
			count++;
		}
		return a;
	}
}
