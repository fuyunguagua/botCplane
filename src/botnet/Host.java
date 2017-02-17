package botnet;

public class Host implements Comparable<Host>{
	private int ID;
	private double score;//½©Ê¬µÃ·Ö

	private int clusterIndex;
	public int getClusterIndex() {
		return clusterIndex;
	}
	public void setClusterIndex(int clusterIndex) {
		this.clusterIndex = clusterIndex;
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
			if(this.ID == ((Host)obj).getID()){
				return true;
			}
		}
		return false;
	}
	@Override
	public int hashCode() {
		return ID;
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
		return this.ID+","+this.clusterIndex;
	}
	
}
