package botnet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import weka.clusterers.XMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class Main {
	public static void main(String arg[]) {
		int min=2;
		int max=10000;
		File firstFile = new File("data.arff");
		File secondFile = new File("data.arff");
		int [] firstResult = firstCluster(max, min, firstFile);
		secondCluster(max, min, firstResult,secondFile);
		
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
	        //计算第一次聚的类的数量
	        int firstClusterNum = 1;
	        Arrays.sort(result);
	        int temp = result[0];
	        int num=1;
	        for(int i:result){
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
	            XM.buildClusterer(ins); 
	            System.out.println(XM.toString());
	            int[] r = new int[ins.numInstances()]; //建一个数组保存聚类结果
	
	           
	        	for(int k=0;k<ins.numInstances();k++){
	        		Instance instance = ins.instance(k);
	        		int re = XM.clusterInstance(instance);
	        		r[k] = re;
	        	}
	        	//加入聚类结果那列
	            Attribute cluster = new Attribute("cluster");
	            ins.insertAttributeAt(cluster,0);
	        	for(int k=0;k<ins.numInstances();k++){
	        		Instance instance = ins.instance(k);
	        		instance.setValue(0, r[k]);
	        	}
	        	//加入索引
	            Attribute index = new Attribute("index");
	            ins.insertAttributeAt(index,1);
	            for(int k=0;k<ins.numInstances();k++){
	        		Instance instance = ins.instance(k);
	        		instance.setValue(1, list.get(k));
	        	}
	        	//将第一步聚类结果写入到文件中
	        	ArffSaver saver = new ArffSaver();
	        	saver.setInstances(ins);
	        	saver.setFile(new File("secondcluster"+i+".arff"));
	        	saver.writeBatch();
	        
	        }

		} catch (Exception e) {
			System.out.println("读取文件失败");
			e.printStackTrace();
		}
	}
}