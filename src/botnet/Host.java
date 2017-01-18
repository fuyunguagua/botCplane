package botnet;

public class Host implements Comparable<Host>{
	private int ID;
	private double score;//½©Ê¬µÃ·Ö
	private String ip;
	private int clusterIndex;
	public int getClusterIndex() {
		return clusterIndex;
	}
	public void setClusterIndex(int clusterIndex) {
		this.clusterIndex = clusterIndex;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Host){
			if(this.ip.equals(((Host)obj).getIp())){
				return true;
			}
		}
		return false;
	}
	@Override
	public int hashCode() {
		char [] arr = this.getIp().toCharArray();
		int code = 0;
		for(int i =0;i<arr.length;i++){
			code += (int)arr[i];
		}
		return code;
	}
	@Override
	public int compareTo(Host o) {
		if(this.getID()>o.getID()){
			return 1;
		}else if (this.getID() == o.getID()) {
			return 0;
		}else {
			return -1;
		}
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.ID+","+this.clusterIndex;
	}
	
}
