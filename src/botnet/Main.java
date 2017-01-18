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

import weka.clusterers.XMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class Main {
	public static void main(String arg[]) {
		int min=10;
		int max=10000;
		File firstFile = new File("data8.arff");
		File secondFile = new File("data52.arff");
		int [] firstResult = firstCluster(max, min, firstFile);
		secondCluster(max, min, firstResult,secondFile);
		makeResult();
		resultByClusterIndex();
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
			BufferedWriter bWriter = new BufferedWriter(new PrintWriter(new File("OutByClusterIndex.txt")));
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
}