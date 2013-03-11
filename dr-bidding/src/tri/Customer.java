package tri;

import java.util.ArrayList;
import java.util.Random;

//working data structure for each customer
//used for day-to-day simulation
public class Customer{
	
	// customer type
	String type;
	
	// list of appliance owned, based on index on appsLst 
	ArrayList<Integer> appsIndex;
	
	// list of usageGroup of each appliances correspond to apps index
	// the difference with timeslots in customerType is that
	// in here, we processed 'dev' and fixed the 'start' and 'end'
	// for several appliances, for each appliances: several appUsageGroup
	ArrayList<ArrayList<UsageGroup>> appUsages;
	
	// to store initial schedule
	ArrayList<ArrayList<Integer>> initSchedule;

	// to store previous day schedule
	ArrayList<ArrayList<Integer>> prevSchedule;

	// to store best schedule so far
	ArrayList<ArrayList<Integer>> currSchedule;
	
	// total loads (wh) per hour 
	// this is int[24]
	ArrayList<ArrayList<Float>> loadReqs;

	ArrayList<ArrayList<BidSubmit>> BidsSubmit;
	
	ArrayList<Bill> Bills;
	
	int[] origLoads;
	int[] neededLoads;
	int[] assignedLoads;
	
	float accTimeUtility;
	float currTimeUtility;
	
	float accPriceUtility;
	float currPriceUtility;
	
	float timePriceCoeff;
	
	float idxValuation; // how this consumer value the electricity compare to the orig.price 
	
	public Customer(String Type, float TimePriceCoeff, int MAXTIME, float IdxValuations, Random rnd){
		type = Type;
		currSchedule = new ArrayList<ArrayList<Integer>>();
		accTimeUtility = 0;		
		currTimeUtility = 0;		
		accPriceUtility = 0;
		currPriceUtility = 0;
		timePriceCoeff = TimePriceCoeff;
		origLoads = new int[MAXTIME];
		assignedLoads = new int[MAXTIME]; 
		// idxValuation = (float) (1.0 + (int) (rnd.nextDouble() * 10) / 10.0 );
		idxValuation = IdxValuations;
		//..System.out.println(idxValuation);
		BidsSubmit = new ArrayList<ArrayList<BidSubmit>>();
		for (int i=0; i<MAXTIME; i++){
			ArrayList<BidSubmit> BidsSubmitElement = new ArrayList<BidSubmit>();
			BidsSubmit.add(BidsSubmitElement);
		}
		Bills = new ArrayList<Bill>();
	}	
	
	public void copyOrigToNeeded(){
		neededLoads = new int[origLoads.length];
		for (int i=0; i<origLoads.length; i++){
			neededLoads[i] = origLoads[i];
		}
	}
	
	public float getIdxValuation(){
		return idxValuation;
	}
	
	/*
	public int getOrigLoads(int t){
		return origLoads[t];
	}
	*/
	
	public int getNeededLoads(int t){
		return neededLoads[t];
	}

	public int getAssignedLoads(int t){
		return assignedLoads[t];
	}
	
	public void setAssignedLoads(int t, int load){
		if (load >= 0)
			assignedLoads[t] = load;
		else 
		{
			System.err.println("[Customer::setAssignedLoads] err: load must be >= 0");
			System.exit(1);
		}
	}
	
	public void decreaseNeededLoads(int t, int amount){
		if ( neededLoads[t] < amount ) {
			System.err.println("[Customer::decreaseNeededLoads] err: amount > neededLoads["+t+"]");
			System.exit(1);
		}
		neededLoads[t] -= amount;
	}
	
	public int[] makeBid(int[] loadsBid){
		
		// initialize data structure
		BidsSubmit = new ArrayList<ArrayList<BidSubmit>>();
		for (int i=0; i<loadsBid.length; i++){
			ArrayList<BidSubmit> BidsSubmitElement = new ArrayList<BidSubmit>();
			BidsSubmit.add(BidsSubmitElement); 
		}
		
		// bid per time slot
		/*
		for (int i=0; i<neededLoads.length; i++){
			int idx = findNearestAvail(loadsBid, i, neededLoads[i]);
			BidSubmit bidSubmit = new BidSubmit(i, neededLoads[i]); 
			BidsSubmit.get(idx).add(bidSubmit);
		}
		*/
		// bid per time slot
		//..System.out.println("start to decide the bids...");
		for (int i=0; i<neededLoads.length; i++){
			
			int neededLoad = neededLoads[i];
			if (neededLoad>0) {
				int d=0;
				while (neededLoad > 0) {						
					if (i + d < loadsBid.length && loadsBid[i + d] >= 0) {
						int bidLoad = Math.min(loadsBid[i+d], neededLoad);
						BidSubmit bidSubmit = new BidSubmit(i, bidLoad);
						BidsSubmit.get(i+d).add(bidSubmit);
						//..System.out.printf("t:%d, neededLoad=%d, loadsBid[%d]=%d, bidLoad: %d \n",i,neededLoad,i+d,loadsBid[i+d],bidLoad);
						neededLoad -= bidLoad;
					}
					if (neededLoad>0) {
						if (i - d > 0 && loadsBid[i - d] >= 0) {
							int bidLoad = Math.min(loadsBid[i-d], neededLoad);
							BidSubmit bidSubmit = new BidSubmit(i, bidLoad);
							BidsSubmit.get(i-d).add(bidSubmit);
							//..System.out.printf("t:%d, neededLoad=%d, loadsBid[%d]=%d, bidLoad: %d \n",i,neededLoad,i-d,loadsBid[i-d],bidLoad);
							neededLoad -= bidLoad;													
						}						
					}
					d++;
				}

			}
		}
		
		// accumulate the bid for each time slot
		int[] bid = new int[loadsBid.length];
		for (int i=0; i<loadsBid.length; i++){
			int acc = 0;
			for (int j=0; j<BidsSubmit.get(i).size(); j++){
				acc += BidsSubmit.get(i).get(j).amount;
			}
			bid[i] = acc;
		}
		return bid;
	}
	
	public int findNearestAvail(int[] loadsBid, int tOrg, int amount){
		//.. System.err.println(loadsBid[tOrg] + ", "+ tOrg + ", " + amount);		
		if (loadsBid[tOrg] >= amount) { 
			return tOrg;
		} 
		int d = 1;
		boolean outOfTime = false;
		while (outOfTime == false) {
			if (tOrg + d >= loadsBid.length && tOrg-d < 0 ) outOfTime = true;
			if (tOrg + d < loadsBid.length && loadsBid[tOrg + d] >= amount) return tOrg+d;
			if (tOrg - d > 0               && loadsBid[tOrg - d] >= amount) return tOrg-d;
			d++;
		}
		if (outOfTime==true){
			// this is because there is not enough load
			return -1;
			/*
			System.err.println("amount: "+amount);
			System.err.println("[Customer::findNearestAvail] err: not enough load available for t: " + tOrg);
			System.exit(1);
			*/
		}		
		return -1;
	}
	

	public void distributeLoadsWon(int timeSlot, int loadsWon){
		int idxBids = 0;
		//..System.out.printf("Loads won: %d\n", loadsWon);
		while (loadsWon>0) {
			int loadsConsume = Math.min(loadsWon, BidsSubmit.get(timeSlot).get(idxBids).amount);
			//..System.out.println("amount: "+BidsSubmit.get(timeSlot).get(idxBids).amount);
			//..System.out.println(Util.arrToStr(neededLoads));
			neededLoads[BidsSubmit.get(timeSlot).get(idxBids).orgTimeSlot] -= loadsConsume;
			assignedLoads[timeSlot] += loadsConsume;
			if (neededLoads[BidsSubmit.get(timeSlot).get(idxBids).orgTimeSlot] < 0) {
				System.err.println("[Customer::distributeLoadsWon] err: neededLoad["+BidsSubmit.get(timeSlot).get(idxBids).orgTimeSlot+"]: "+neededLoads[BidsSubmit.get(timeSlot).get(idxBids).orgTimeSlot] +" should not be negative");
				System.exit(1);
			}
			loadsWon -= loadsConsume;
			//..System.out.printf("Loads consume: %d, loads won rest %d\n", loadsConsume, loadsWon);			
			idxBids++;
		}
	}
	
	
	public void putBill(int LoadAmount, float Price, int TimeUse){
		Bill bill = new Bill(LoadAmount, Price, TimeUse);
		Bills.add(bill);
	}
	
	public float getPricePerWh(){
		float totalCost = 0;
		float totalWh = 0;
		for (int i=0; i<Bills.size(); i++){
			totalWh += Bills.get(i).getLoadAmount();
			totalCost += (Bills.get(i).getLoadAmount()*Bills.get(i).getPrice());
		}		
		return totalCost / totalWh;
	}
	
	public float getPricePerKWh(){
		return getPricePerWh()*1000;
	}

	public float getTotalBill(){
		float totalCost = 0;		
		for (int i=0; i<Bills.size(); i++){
			totalCost += (Bills.get(i).getLoadAmount()*Bills.get(i).getPrice());
		}		
		return totalCost;
	}
	
	public float getTotalWh(){
		float totalWh = 0;
		for (int i=0; i<Bills.size(); i++){
			totalWh += Bills.get(i).getLoadAmount();
		}		
		return totalWh;
	}
	
	// get the total difference between origLoads and assignedLoads
	public float getNormalizeShift(){
		int diff = 0;
		int totalLoad = 0;
		for (int t=0; t<origLoads.length; t++){
			totalLoad += origLoads[t];
			diff += Math.abs(origLoads[t] - assignedLoads[t]);
		}
		return (float) ((diff+0.0) / 2.0 / (totalLoad + 0.0));
	}
	
	public String billsToStr(){
		String result = ""; 
		for (int i=0; i<Bills.size(); i++){
			result = result + "[" + Bills.get(i).timeUse + "," + Bills.get(i).loadAmount + "," + Bills.get(i).getPrice() + "]";
		}
		return result;
	}
	
	public void addTimeUtility(float TimeUtility){
		//System.out.printf("TimeU=%.3f\n", TimeUtility);
		currTimeUtility = TimeUtility;
		accTimeUtility = accTimeUtility + currTimeUtility;
	}
	
	public void addPriceUtility(float PriceUtility){
		//System.out.printf("PriceU=%.3f\n", PriceUtility);
		currPriceUtility = PriceUtility;
		accPriceUtility = accPriceUtility + currPriceUtility;
	}

	public void addApps(ArrayList<Integer> Apps){
		appsIndex = new ArrayList<Integer>();
		for (int i=0; i<Apps.size(); i++){
			appsIndex.add(Apps.get(i));
		}		
	}
	
	public float getOrigCost(int[] initTotalLoads){
		float totalCost = 0;
		for (int t=0; t<initTotalLoads.length; t++){
			totalCost += Util.getLoadCost(initTotalLoads[t]) / initTotalLoads[t] * origLoads[t]; 
		}
		return totalCost;
	}
	/*
	public float getCurrNetBenefit(){
		return timePriceCoeff*currTimeUtility + currPriceUtility;
	}
	*/
	
	// this should be the same order as in time
	public void addAppUsages(ArrayList<ApplianceUsage> appliances, Random rnd, int DEVUSAGE) {
		
		appUsages = new ArrayList<ArrayList<UsageGroup>>();		
		// loop for all appliances
		for (int i=0; i<appliances.size(); i++){
			
			// loop for all timeslots available
			ArrayList<UsageGroup> newUsages = new ArrayList<UsageGroup>();
			
			for (int j=0; j<appliances.get(i).usages.size(); j++){
				
				UsageGroup newUsage = new UsageGroup();
				UsageGroup Usage = appliances.get(i).usages.get(j);
				
				// fix the timeslot
				ArrayList<Timeslot> Timeslots = Usage.timeslots;
				ArrayList<Timeslot> newTimeslots = new ArrayList<Timeslot>(); 
				for (int k=0; k<Timeslots.size(); k++){					
					int start = (int) (Timeslots.get(k).start + ((rnd.nextDouble()*2-1)*Timeslots.get(k).dev));
					int end = (int) (Timeslots.get(k).end + (rnd.nextDouble()*Timeslots.get(k).dev));
					Timeslot t = new Timeslot(start, end);
					newTimeslots.add(t);
				}				
				newUsage.timeslots = newTimeslots;
				
				// TODO NOW fix the duration
				Duration newDuration = new Duration();
				//  add deviation from usage-duration
				
				float devAdd = (float) (Usage.duration.iDuration * ((DEVUSAGE+0.0)/100.0));				
				newDuration.devUsage = (float) Usage.duration.devUsage + devAdd;
				//..System.out.println(newDuration.devUsage);
				// random between -dev and +dev
				float dev = (float) (rnd.nextDouble() * Usage.duration.dev * 2);				
				dev = dev - Usage.duration.dev;
				newDuration.iDuration = Usage.duration.iDuration + dev;

				// set the duration
				newUsage.duration = newDuration;
				
				// add to the list
				newUsages.add(newUsage);
			}
			
			appUsages.add(newUsages);
		}
	}
	
	/*
	public float calculateTimeUtility(){
		float absCost = 0; 
		float changeCost = 0;
		for (int i=0; i<initSchedule.size(); i++){
			for (int j=0; j<initSchedule.get(i).size(); j++){
				absCost = absCost + Math.abs(initSchedule.get(i).get(j) - currSchedule.get(i).get(j) );
				//changeCost = (float) (changeCost + Math.pow(Math.abs(prevSchedule.get(i).get(j) - currSchedule.get(i).get(j)),2) );
			}
		}
		//return -absCost - changeCostCoeff*changeCost;
		//return  - changeCost;
		return  1/(absCost+1);
		
	}
	*/
	
	public String toString(){
		return "Customer type=" + type + ", appsIndex=" + appsIndex + ", appUsage=" + appUsages; 
	}
	
	

	public void generateLoadReqs(Random rnd){
		// per appliances, generate the load requirement for this iteration
		loadReqs = new ArrayList<ArrayList<Float>>();						
		for (int j=0; j<appUsages.size(); j++){
			// j = index of appliances
			ArrayList<UsageGroup> currApp = appUsages.get(j);
			ArrayList<Float> appReq = new ArrayList<Float>(); 
			for (int k=0; k<currApp.size();k++){
				// k = index of usage group
				float usageTime = (float)((rnd.nextDouble()*2) - 1) * currApp.get(k).duration.devUsage + currApp.get(k).duration.iDuration;
				appReq.add(usageTime);
			}
			loadReqs.add(appReq);
		}
		
	}	
	
}

class BidSubmit{
	int orgTimeSlot;
	int amount;
	
	public BidSubmit(int OrgTimeSlot, int Amount){
		orgTimeSlot = OrgTimeSlot;
		amount = Amount; 
	}
	
}

class Bill{
	int loadAmount;
	float price;
	int timeUse;
	
	public Bill(int LoadAmount, float Price, int TimeUse){
		loadAmount = LoadAmount;
		price = Price;
		timeUse = TimeUse;
	}
	
	public int getLoadAmount(){
		return loadAmount;
	}
	
	public float getPrice(){
		return price;
	}

	public int getTimeUse(){
		return timeUse;
	}
}


