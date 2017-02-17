package botnet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import weka.clusterers.XMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class Main {
	private static int a = 0;
	public static final  String INPUT1 = "data8.arff";
	public static final  String INPUT2 = "data52.arff";
	public static final  String INPUT3 = "AResult.csv";
	public static final  String INPUT4 = "CResult.csv";
	
	public static void main(String arg[]) {
		C_Plane(INPUT1,INPUT2);
		Cross_Plane(INPUT3,INPUT4);
	}
	public static void C_Plane(String inputEight, String inputFifty){
		int min=10;
		int max=10000;
		File firstFile = new File(inputEight);
		File secondFile = new File(inputFifty);
		int [] firstResult = firstCluster(max, min, firstFile);
		secondCluster(max, min, firstResult,secondFile);
		makeResult();
		resultByClusterIndex();
	}
	public static void Cross_Plane(String AResult, String CResult){
		try {
			Set<Host> H = new HashSet<Host>();
			ArrayList<Set<Host>> Alist = new ArrayList<Set<Host>>();
			ArrayList<Set<Host>> Clist = new ArrayList<Set<Host>>();
			getAlist(Alist,H,new File(AResult));
			getClist(Clist,new File(CResult));
			//System.out.println(Alist.size());
			//System.out.println(Clist.size());

			getAllBotScore(H,Alist,Clist);
			outputScoreToFile(H);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static int[]  firstCluster(int max, int min, File firstFile){
	    Instances ins = null;
        XMeans XM = null;  
        System.out.println("					--------------------------------第一次聚类-----------------------------------\n\n\n");
		try {
			ArrayList<Integer> list = new ArrayList<Integer>();//记录序号
            ArffLoader loader = new ArffLoader();  
            loader.setFile(firstFile);  
            ins = loader.getDataSet();  
            XM = new XMeans();  
            XM.setMinNumClusters(min);     
            XM.setMaxNumClusters(max);
            XM.setMaxIterations(100);
            XM.buildClusterer(ins); 
            int[] r = new int[ins.numInstances()]; //建一个数组保存聚类结果

            
            System.out.println(XM.toString());

        	for(int i=0;i<ins.numInstances();i++){
        		Instance instance = ins.instance(i);
        		int result = XM.clusterInstance(instance);
        		r[i] = result;
        	}
        	//加入聚类结果那列
            Attribute cluster = new Attribute("cluster");
            ins.insertAttributeAt(cluster,0);
        	for(int i=0;i<ins.numInstances();i++){
        		Instance instance = ins.instance(i);
        		instance.setValue(0, r[i]);
        	}
        	//加入索引
            Attribute index = new Attribute("index");
            ins.insertAttributeAt(index,1);
            for(int k=0;k<ins.numInstances();k++){
        		Instance instance = ins.instance(k);
        		instance.setValue(1, k);
        	}
        	//将第一步聚类结果写入到文件中
        	ArffSaver saver = new ArffSaver();
        	saver.setInstances(ins);
        	saver.setFile(new File("Firstcluster.arff"));
        	saver.writeBatch();
        	
        	return r;
		} catch (Exception e) {
			System.out.println("读取文件失败");
			e.printStackTrace();
		}
		return null;
		
	}
	/*
	 * 从第一步的instances里拿到具有同一聚类号的所有instance,放入instances中
	 */
	public static void secondCluster(int max, int min,int[] result, File secondFile) {
		 System.out.println("						----------------------------第二次聚类---------------------------------------\n\n\n");
		
		try {
		    Instances souIns = null;
	        XMeans XM = null;  
	        ArffLoader loader = new ArffLoader();  
	        loader.setFile(secondFile);  
	        souIns = loader.getDataSet();
	        
	        //统一输出结果结果
	        BufferedWriter bWriter = new BufferedWriter(new PrintWriter(new File("cout.txt")));
	        int oldNum = 0;//记录前面已记录的聚类数量
	        //计算第一次聚的类的数量
	        int firstClusterNum = 1;
	        int [] arr = Arrays.copyOf(result, result.length);
	        Arrays.sort(arr);
	        int temp = arr[0];
	        int num=1;
	        for(int i:arr){
		         if(temp!=i){
		        	 firstClusterNum++;
		        	 temp = i;
		         }
	        }
	        
	        for(int i=0; i<firstClusterNum; i++){
	        	Instances ins = new Instances(souIns);
	        	ins.delete();
	        	ArrayList<Integer> list = new ArrayList<Integer>();//记录序号
	        	for(int j=0; j<result.length; j++){
	        		if(result[j] == i){
	        			ins.add(souIns.instance(j));
	        			list.add(j);
	        		}
	        	}
	        	System.out.println("									类别"+i+"的数量     "+ins.numInstances());
	        	
	        	//
	            XM = new XMeans();  
	            XM.setMinNumClusters(min);     
	            XM.setMaxNumClusters(max);
	            XM.setMaxIterations(100);
	            XM.buildClusterer(ins); 
	            System.out.println(XM.toString());
	            int[] r = new int[ins.numInstances()]; //建一个数组保存聚类结果
	            int currentNum = XM.getClusterCenters().numInstances();//记录当前聚类数量
	           
	        	for(int k=0;k<ins.numInstances();k++){
	        		Instance instance = ins.instance(k);
	        		int re = XM.clusterInstance(instance);
	        		r[k] = re;
	        	}
	        	//加入聚类结果那列
	            Attribute cluster = new Attribute("cluster");
	            ins.insertAttributeAt(cluster,0);
	        	//加入索引
	            Attribute index = new Attribute("index");
	            ins.insertAttributeAt(index,1);
	        	for(int k=0;k<ins.numInstances();k++){
	        		Instance instance = ins.instance(k);
	        		instance.setValue(0, r[k]);
	        		instance.setValue(1, list.get(k));
	        		
	        		bWriter.write((list.get(k)+1)+","+(r[k]+oldNum+1));
	        		bWriter.newLine();
	        	}
	        	oldNum += currentNum;
	        	//将第一步聚类结果写入到文件中
	        	ArffSaver saver = new ArffSaver();
	        	saver.setInstances(ins);
	        	saver.setFile(new File("secondcluster"+i+".arff"));
	        	saver.writeBatch();
	        
	        }
	        bWriter.close();
		} catch (Exception e) {
			System.out.println("读取文件失败");
			e.printStackTrace();
		}
	}
	public static void makeResult() {
		try {
			BufferedReader bReader = new BufferedReader(new InputStreamReader( new FileInputStream(new File("cout.txt"))));
			BufferedWriter bWriter = new BufferedWriter(new PrintWriter(new File("out.txt")));
			String line = null;
			ArrayList<Host> list = new ArrayList<Host>();
			while((line = bReader.readLine()) != null){
				String [] strings = line.split(",");
				int index = Integer.parseInt(strings[0]);
				int clusterIndex = Integer.parseInt(strings[1]);
				Host host = new Host();
				host.setID(index);
				host.setClusterIndex(clusterIndex);
				list.add(host);
			}
			bReader.close();
			Collections.sort(list);
			for(Host host:list){
				bWriter.write(host.toString());
				bWriter.newLine();
			}
			bWriter.flush();
			bWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void resultByClusterIndex() {
		try {
			BufferedReader bReader = new BufferedReader(new InputStreamReader( new FileInputStream(new File("cout.txt"))));
			BufferedWriter bWriter = new BufferedWriter(new PrintWriter(new File("CResult.csv")));
			String line = null;
			ArrayList<Host> list = new ArrayList<Host>();
			while((line = bReader.readLine()) != null){
				String [] strings = line.split(",");
				int index = Integer.parseInt(strings[0]);
				int clusterIndex = Integer.parseInt(strings[1]);
				Host host = new Host();
				host.setID(index);
				host.setClusterIndex(clusterIndex);
				list.add(host);
			}
			bReader.close();
			Collections.sort(list, new Comparator<Host>() {

				@Override
				public int compare(Host o1, Host o2) {
					// TODO Auto-generated method stub
					if(o1.getClusterIndex() > o2.getClusterIndex())
						return 1;
					else if(o1.getClusterIndex() == o2.getClusterIndex())
						return 0;
					else
						return -1;
				}
			});;
			for(Host host:list){
				bWriter.write(host.getClusterIndex()+","+host.getID());
				bWriter.newLine();
			}
			bWriter.flush();
			bWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void getClist(ArrayList<Set<Host>> Clist, File file) throws NumberFormatException, IOException{
		int CIndex = -1;
		String line = null;
		BufferedReader bReaderC = new BufferedReader(new InputStreamReader( new FileInputStream(file)));
		while((line = bReaderC.readLine())!=null){
			String [] arr = line.split(",");
			if(	Integer.parseInt(arr[0]) != CIndex){
				Set<Host> set = new HashSet<Host>();
				Clist.add(set);
				CIndex = Integer.parseInt(arr[0]);
			}
			Host host = new Host();
			host.setID(Integer.parseInt(arr[1])-1);
			Clist.get(Clist.size()-1).add(host);
		}
		bReaderC.close();
	}
	public static void getAlist(ArrayList<Set<Host>> Alist, Set<Host> H, File file) throws NumberFormatException, IOException{
		BufferedReader bReaderA = new BufferedReader(new InputStreamReader( new FileInputStream(file)));
		String line = null;
		int hostIndex = 0;
		while((line = bReaderA.readLine())!=null){
			String [] arr = line.split(",");
			if(hostIndex == 0){
				
				for(int i = 0;i<arr.length;i++){
					Set<Host> aSet = new HashSet<Host>();
					Alist.add(aSet);
				}
			}
			
			Host host = new Host();
			host.setID(hostIndex);
			H.add(host);
			
			for(int i = 0;i<arr.length;i++){
				if(Integer.parseInt(arr[i])==1){
					Alist.get(i).add(host);
				}
			}
			hostIndex++;
		}
		bReaderA.close();
	}

	public static double botScore(Host h,ArrayList<Set<Host>> Alist,ArrayList<Set<Host>> Clist){
		double score = 0;
		int w = 1;
		for(int i = 0;i<Alist.size();i++){
			Set<Host> Ai = Alist.get(i);
			for(int j=i+1;j<Alist.size();j++){
				Set<Host> Aj = Alist.get(j);
				Set<Host> intersection = new HashSet<Host>();
				intersection.addAll(Ai);
				intersection.retainAll(Aj);
				Set<Host> union = new HashSet<Host>();
				union.addAll(Ai);
				union.addAll(Aj);
				score += w*w*(((double)intersection.size())/union.size());
				
				
			}
		}
		System.out.print(++a+" "+score);
		for(int i = 0;i<Alist.size();i++){
			Set<Host> Ai = Alist.get(i);
			for(int k=0;k<Clist.size();k++){
				Set<Host> Ck = Clist.get(k);
				Set<Host> intersection = new HashSet<Host>();
				intersection.addAll(Ai);
				intersection.retainAll(Ck);
				Set<Host> union = new HashSet<Host>();
				union.addAll(Ai);
				union.addAll(Ck);
				score += w*(((double)intersection.size())/union.size());
				
			}
		}
		System.out.println("__"+score);
		return score;
	}
	public static void outputScoreToFile(Set<Host> H){
		try {
			BufferedWriter bWriter = new BufferedWriter(new PrintWriter(new File("score.txt")));
			for(Host h: H){
				bWriter.write((h.getID()+1)+","+h.getScore());
				bWriter.newLine();
			}
			bWriter.flush();
			bWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void getAllBotScore(Set<Host> H, ArrayList<Set<Host>> Alist, ArrayList<Set<Host>> Clist) {
		for(Host h: H){		
			ArrayList<Set<Host>> TempAlist = new ArrayList<Set<Host>>();
			ArrayList<Set<Host>> TempClist = new ArrayList<Set<Host>>();
			for(Set<Host> set:Alist){
				if (set.contains(h)) {
					TempAlist.add(set);
				}
			}
			//System.out.println(Clist.size());
			for(Set<Host> set:Clist){
				if (set.contains(h)) {
					TempClist.add(set);
				}
			}
			double score = botScore(h, TempAlist, TempClist);
			h.setScore(score);
		}
	}

	public static int similarity(Host h1,Host h2,ArrayList<Set<Host>> AClist,int mb){
		int sim = 0;
		StringBuilder host1 = new StringBuilder();
		StringBuilder host2 = new StringBuilder();
		for(Set<Host> set : AClist){
			if(set.contains(host1)){
				host1.append("1");
			}else{
				host1.append("0");
			}
			
			if(set.contains(host2)){
				host2.append("1");
			}else{
				host2.append("0");
			}
		}
		char [] c1 = host1.toString().toCharArray();
		char [] c2 = host2.toString().toCharArray();
		
		for(int i = 0;i<mb;i++){
			if(c1[i] == c2[i]){
				sim++;
			}
		}
		
		int temp = 0;
		for(int i = mb;i<AClist.size();i++){
			if(c1[i] == c2[i]){
				temp++;
			}
		}
		if (temp>=1) {
			sim++;
		}
		
		return sim;
	}
}