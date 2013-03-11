package tri;


public class Appliance{
	String id;
	String name;
	int power;
	
	public Appliance(){
		id = "";
		name = "";
		power = 0;
	}
	
	public Appliance(String Id, String Name, int Power){
		id = Id;
		name = Name;
		power = Power;				
	}
	
	public String toString(){
		return "Appliance id=" + id + ", name=" + name + ", power= " + power;
	}
		
}
