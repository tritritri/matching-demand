package tri;

import java.util.ArrayList;

/// start of public class CustomerType 
public class CustomerType {
	String type;
	int count;
	ArrayList<ApplianceUsage> appliances;
	
	public CustomerType(){
		type = "";
		count = 0;
		appliances = new ArrayList<ApplianceUsage>();
	}
	
	public CustomerType(String Type, int Count){
		type = Type;
		count = Count;
		appliances = new ArrayList<ApplianceUsage>();
	}
	
	public void addAppliance(ApplianceUsage AppUsage){
		appliances.add(AppUsage);
	}
	
	public String toString(){
		return "CustomerType type=" + type + ", count=" + count + ", appUsage=" + appliances;
	}
}


class ApplianceUsage{
	String id;
	ArrayList<UsageGroup> usages;
	
	public ApplianceUsage(String Id){
		id = Id;
		usages = new ArrayList<UsageGroup>();
	}
	
	public void addUsageGroup(UsageGroup usageGroup){
		usages.add(usageGroup);
	}
	
	public String toString(){
		return "ApplianceUsage id=" + id + ", " + usages; 
	}
}


class UsageGroup{
	
	ArrayList<Timeslot> timeslots;
	Duration duration;
	
	public UsageGroup(){
		timeslots = new ArrayList<Timeslot>();
		duration = new Duration();
	}
	
	public void setDuration(String dev, String devUsage, String iDuration){
		duration.setAll(dev, devUsage, iDuration);
	}
	
	public void addTimeslot(String start, String end, String dev){
		Timeslot timeslot = new Timeslot(start, end, dev);
		timeslots.add(timeslot);
	}
	
	public String toString(){
		return "UsageGroup " + timeslots + ", " + duration;
	}
	
}


class Timeslot{
	// for now on, let us consider us this as 'hour'
	// TODO: change this into more general definition
	int start; 
	int end;
	int dev;
	
	public Timeslot(){
		start = 0;
		end = 0;
		dev = 0;
	}
	
	public Timeslot(int Start, int End){
		start = Start;
		end = End;
		dev = 0;
	}
	public Timeslot(String Start, String End, String Dev){
		String[] stringArr = Start.split(":");
		start = Integer.parseInt(stringArr[0]);
		stringArr = End.split(":");
		end = Integer.parseInt(stringArr[0]);
		stringArr = Dev.split(":");
		dev = Integer.parseInt(stringArr[0]);
	}
	
	public String toString(){
		return "Timeslot start=" + start + ", end=" + end + ", dev=" + dev;
	}
}


class Duration{
	
	float dev;
	float devUsage;
	float iDuration;
	
	public Duration(){
		dev = 0; 
		iDuration = 0;
	}
	
	public void setAll(String Dev, String DevUsage, String IDuration){
		
		String[] stringArr = Dev.split(":");
		int hr = Integer.parseInt(stringArr[0]);
		int mn = Integer.parseInt(stringArr[1]);
		dev = hr * 60 + mn;
		dev = dev / 60;
		
		stringArr = DevUsage.split(":");
		hr = Integer.parseInt(stringArr[0]);
		mn = Integer.parseInt(stringArr[1]);
		devUsage = hr * 60 + mn;
		devUsage = devUsage / 60;
		
		stringArr = IDuration.split(":");
		hr = Integer.parseInt(stringArr[0]);
		mn = Integer.parseInt(stringArr[1]);		
		iDuration = hr * 60 + mn;
		iDuration = iDuration / 60;
		
	}
	
	public String toString(){
		return "Duration dev=" + dev + ", devUsage=" + devUsage + ", duration=" + iDuration;
	}
	
}


