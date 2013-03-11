package tri;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 * TODO: 
 * V define how many games, how many cust-strategy-run, and parameter imperfect, also in XML?
 * setting output, perturbed data, 
 * calculate price differences in perturbed data
 */

/*
 * TODO: ABOUT UTILITY
 * v specify the population, i.e. for each customer, specify the participation rate
 * v set the dev of the apps-usage
 * timeUtility: what about the including the initial time? with decaying or not?
 *  
 */

public class DRBidding{
	
	public static void main(String argv[]) throws ParserConfigurationException, SAXException, IOException  {
		
		int RandomSeed = 2;

		String CustTypeFile = "cons10000.xml";
		String ApplsFile = "appliances.xml";


		DRSimulation sim = new DRSimulation(ApplsFile, CustTypeFile);

		//float[] par1 = sim.run(RandomSeed);
		float CUT = 0.50f;
		int MINLOAD = 100;
		
		//float[] idxValuations = {1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f, 1.7f, 1.8f, 1.9f};
		//float[] valDist       = {0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f};
		
		float[] idxValuations = {2.0f, 1.6f, 1.5f, 1.3f, 1.0f};
		float[] valDist       = {0.1f, 0.1f, 0.2f, 0.2f, 0.4f};
		
		sim.expRun(CUT, MINLOAD, idxValuations, valDist, 1, "log.log", 1, RandomSeed);
		
	}
}

class DRSimulation {
	
	private float CUT;
	private int MINLOAD;
	
	private int EXPNO;
	private String LOGFILENAME;
	private String CUSTTYPEFILE;
	PrintWriter pLog;
	
	private int VERBOSE=1;
	
	private Random rnd;
	
	private int MAXTIME;
	private float TIMEPRICECOEFF; 
	
	
	private ArrayList<CustomerType> custTypeLst;
	private ArrayList<Appliance> appLst;
	
	
	public DRSimulation(String AppliancesFile, String CustTypeFile) throws ParserConfigurationException, SAXException, IOException{
		
		// put default value
		CUT = 0.5f;
		MINLOAD = 100;
		
		EXPNO = 0;
		LOGFILENAME = "";
		CUSTTYPEFILE = CustTypeFile;
		
		// TODO:
		// set this using XML: general-setting.xml	
		MAXTIME = 24;
		TIMEPRICECOEFF = 0.0f; // coeff for netbenefit, which is: coeff*timeU + priceU 
		
		// TODO: put this in XML: general-setting.xml
		// read and parse customers file
		custTypeLst = readCustomersFile(CUSTTYPEFILE);

		// TODO: put this in XML: general-setting.xml
		// read and parse appliances file
		appLst = readAppliancesFile(AppliancesFile);
		
		if (VERBOSE == 1)
		System.out.println(appLst);			
		
 	}
	
	
	// run for experiment
	public float[] expRun(float Cut, int MinLoad, float[] IdxValuations, float[] ValDist, int ExpNo, String LogFileName, int Verbose, int RandomSeed) throws FileNotFoundException{
		CUT = Cut;
		MINLOAD = MinLoad;
		EXPNO = ExpNo;
		LOGFILENAME = LogFileName;
		VERBOSE = Verbose;
		if (LogFileName.length()>0) 		
			pLog = new PrintWriter(new File(LOGFILENAME));
		return run(IdxValuations, ValDist, RandomSeed);
	}
	
	/*..
	public void setAppDeviation(int Percentage){
		DEVUSAGE = Percentage;
	}
	*/
	
	public float[] run(float[] IdxValuations, float[] ValDist, int RandomSeed) throws FileNotFoundException{
		pLog.printf("IdxValuations: %s\n",Util.arrToStr(IdxValuations));
		pLog.printf("ValDist: %s\n",Util.arrToStr(ValDist));
		
		// if there is some exp going on
		
		/*..
		if (EXPNO == 1){
			pLog.printf("// EXPNO=%d, TAKETURN=%d, STOCHOPT=%d, NSTOCH=%d, NRound=%d, RANDOMSEED=%d, DEVUSAGE=%d, EVOLUTION=%d, CUSTTYPEFILE=%s,  \n", EXPNO, TAKETURN, STOCHOPT, NSTOCH, NROUND, RandomSeed, DEVUSAGE, EVOLUTION, CUSTTYPEFILE);			
			pLog.flush();
		}
		 */
		
		rnd = new Random(RandomSeed);
		
		ArrayList<Customer> custLst = createCustomers(IdxValuations, ValDist);
		
		// area under PAR = sum over all PAR
		float AUP = 0.0f;
		// float initialSystemCost = 0.0f;
		float systemSaving = 0.0f;
		float systemCost = 0.0f;
		
		// now, initialize the load
		// loop over all customers
		for (int i=0; i<custLst.size(); i++){
			
			// fill up each customer load
			Customer currCust = custLst.get(i);
			
			// initialize the load
			// currCust.origLoads = new int[MAXTIME];
			currCust.initSchedule = new ArrayList<ArrayList<Integer>>();
			currCust.prevSchedule = new ArrayList<ArrayList<Integer>>();
			currCust.currSchedule = new ArrayList<ArrayList<Integer>>();
			
			// loop over all appliances this customer has			
			for (int j=0; j<currCust.appUsages.size(); j++){
				
				// get appliances number j for customer i
				ArrayList<UsageGroup> currAppUsage = currCust.appUsages.get(j);

				// get power of the appliances
				int appIdx = currCust.appsIndex.get(j);					
				int appPower = appLst.get(appIdx).power;
				
				// loop over all usage group
				ArrayList<Integer> startsTime = new ArrayList<Integer>();
				
				for (int k=0; k<currAppUsage.size(); k++){
					UsageGroup currUsage = currAppUsage.get(k);
					//  get the starting time
					// TODO: must be fixed?
					//int timeslotPick = (int) (rnd.nextDouble() * currUsage.timeslots.size());
					int timeslotPick = 0;
					int startingTime = currUsage.timeslots.get(timeslotPick).start;
					if (startingTime<0) startingTime = 0;
					if (startingTime>=MAXTIME) startingTime = MAXTIME-1;
					startsTime.add(startingTime);
					
					// get the usage time
					float usageTime = (float)((rnd.nextDouble()*2) - 1) * currUsage.duration.devUsage + currUsage.duration.iDuration;
					
					// consume!
					Util.consumeElectricity(currCust.origLoads, startingTime, usageTime, appPower, MAXTIME);										
				
				}// end for all usage group
				
				currCust.initSchedule.add(startsTime);
				currCust.prevSchedule.add(startsTime);
				currCust.currSchedule.add(startsTime);
				
			}// end for all appliances

		} // end for all customer
		
		// now, get overall loads
		int[] initTotalLoads = Util.getTotalLoad(custLst);			

		if (VERBOSE == 1) {
			// for output purposes
			System.out.println("initTotalLoads="+Util.arrToStr(initTotalLoads));
		}

		int[] newTotalLoads = new int[initTotalLoads.length];
		System.arraycopy(initTotalLoads, 0, newTotalLoads, 0, initTotalLoads.length);
		
		int initPeakLoad = Util.arrayMax(initTotalLoads);
		double initAvgLoad = Util.arrayAvg(initTotalLoads);
		
		float PAR = (float) ((initPeakLoad+0.0) / initAvgLoad);
		if (VERBOSE == 1){
			System.out.println("init: par=" + PAR + ", maxLoad="+ initPeakLoad + ", avgLoads=" + initAvgLoad);
		}
		
		// end of customers' initialization
		
		// copy the origLoads to neededLoads
		for (int i=0; i<custLst.size(); i++){
			custLst.get(i).copyOrigToNeeded();
		}
		
		
		// implement the cut
		
		int newPeak = (int) ((1.0 - CUT) * initPeakLoad);
		
		for (int t=0; t<MAXTIME; t++){
			if (newTotalLoads[t] > newPeak){
				int d = 1;
				boolean outOfTime=false;
				int excessLoad = newTotalLoads[t] - newPeak;
				while ( (excessLoad > 0) && (outOfTime == false)) {
					if (t+d < MAXTIME) {
						if (newTotalLoads[t+d] < newPeak){
							int loadtransfer = Math.min(excessLoad, newPeak - newTotalLoads[t+d]);							
							newTotalLoads[t] = newTotalLoads[t] - loadtransfer;
							newTotalLoads[t+d] = newTotalLoads[t+d] + loadtransfer;
							excessLoad = excessLoad - loadtransfer;
						}
					}
					if (t-d > 0) {
						if (newTotalLoads[t-d] < newPeak){
							int loadtransfer = Math.min(excessLoad, newPeak - newTotalLoads[t-d]);							
							newTotalLoads[t] = newTotalLoads[t] - loadtransfer;
							newTotalLoads[t-d] = newTotalLoads[t-d] + loadtransfer;
							excessLoad = excessLoad - loadtransfer;
						}						
					}
					if (excessLoad<0){
						System.err.println("[run] err: excessLoad should not < 0");
						System.exit(1);
					}
					d++;
					if ( (t+d >= MAXTIME) && (t-d < 0) ) outOfTime = true;					
				}
				if (outOfTime == true){
					System.err.println("[run] err: unable to cut for "+CUT+" of original PAR");
					System.exit(1);
				}
			}
		}
		
		int peakLoad = Util.arrayMax(newTotalLoads);
		double avgLoad = Util.arrayAvg(newTotalLoads);
		
		float newPAR = (float) (peakLoad / avgLoad);
		System.out.printf("newPAR: %.3f\n", newPAR);
				
		// cut finish
		
		int[] loadsBid = new int[MAXTIME];
		float[] initPriceBid = new float[MAXTIME];
		
		for (int t=0; t<MAXTIME; t++){
			loadsBid[t] = -1;
		}
		
		// start to put assignedLoad
		// for not shifted loads
		for (int t=0; t<MAXTIME; t++){
			initPriceBid[t] = (float) (Util.getLoadCost(newTotalLoads[t]) / newTotalLoads[t]);
			if (newTotalLoads[t] >= initTotalLoads[t]) { 
				// all load distributed to consumers
				loadsBid[t] = newTotalLoads[t];
				for (int i=0; i<custLst.size(); i++) {
					loadsBid[t] -= custLst.get(i).getNeededLoads(t);
					custLst.get(i).setAssignedLoads(t, custLst.get(i).getNeededLoads(t));
					custLst.get(i).putBill(custLst.get(i).getNeededLoads(t), initPriceBid[t], t);
					custLst.get(i).decreaseNeededLoads(t, custLst.get(i).getNeededLoads(t));
				}				
			} else {				
				// the load is bidded
				loadsBid[t] = newTotalLoads[t];
				for (int i=0; i<custLst.size(); i++) {
					int loadAssigned = Math.min(MINLOAD, custLst.get(i).getNeededLoads(t)); 
					custLst.get(i).setAssignedLoads(t, loadAssigned);
					custLst.get(i).putBill(loadAssigned, initPriceBid[t], t);
					custLst.get(i).decreaseNeededLoads(t, loadAssigned);
					loadsBid[t] -= loadAssigned;
				}
				
			}
			// set the price
		}
		if (EXPNO==1)
		pLog.printf("initPrice: %s\n", Util.arrToStr(initPriceBid));
		System.out.println(Util.arrToStr(loadsBid));
		
		
		// start the bidding	
		// Auction auction = new Auction(loadsBid, rnd); 
		int phase=0;
		while (Util.allZeroes(loadsBid)==false){
			phase ++;
			
			Auction auction = new Auction(loadsBid, rnd); 

			// first, put the bids from all consumers
			for (int i=0; i<custLst.size(); i++){
				int[] bid = custLst.get(i).makeBid(loadsBid);
				for (int t=0; t<MAXTIME; t++){
					if ( bid[t] > 0 ) {
						auction.addBid(t, i, custLst.get(i).getIdxValuation() * initPriceBid[t], bid[t]);
					}
				}
			}
			
			// determine the winner
			ArrayList<ArrayList<BidWon>> winners = auction.auctioning(custLst);			
			pLog.printf("Phase-%d: %s\n",phase,Util.arrToStrWithoutIdx(loadsBid));
			loadsBid = auction.loadsBid;
			pLog.printf("Phase-%d: %s\n",phase,Util.arrToStrWithoutIdx(loadsBid));
			System.out.println("...");
			for (int t=0; t<MAXTIME; t++) {
				for (int i=0; i<winners.get(t).size(); i++) {					
					// try to decrease consumer needed load
					int consumerWon = winners.get(t).get(i).consumerId;
					int loadsWon = winners.get(t).get(i).amount;
					float paidPrice = winners.get(t).get(i).paidPrice;
					//..System.out.println(paidPrice);
					// now consume the load
					custLst.get(consumerWon).distributeLoadsWon(t, loadsWon);
					custLst.get(consumerWon).putBill(loadsWon, paidPrice, t); // put price for each load for this time slot
				}
			}			
		} // end while
		
		/* for printing purposes
		for (int i=0; i<custLst.size();i++){			
			pLog.printf("\nConsumer-%d, idxValuation: %.2f\n",i,custLst.get(i).getIdxValuation());
			pLog.printf("originalLoads: %s\n", Util.arrToStr(custLst.get(i).origLoads));
			pLog.printf("assignedLoads: %s\n", Util.arrToStr(custLst.get(i).assignedLoads));
			pLog.printf("neededLoads  : %s\n", Util.arrToStr(custLst.get(i).neededLoads));
			pLog.printf("bill: %s\n", custLst.get(i).billsToStr());
			pLog.printf("pricePaidPerKwh: %.5f\n", custLst.get(i).getPricePerKWh());			
			pLog.printf("totalShift: %.3f\n", custLst.get(i).getNormalizeShift());
		}
		*/
		
		// summary for company
		pLog.printf("UtilitySummary: %s\n", Util.utilitySummary(IdxValuations, custLst));
		// total company expense:
		float initTotalCost = 0;
		for (int t=0; t<initTotalLoads.length; t++)	initTotalCost += Util.getLoadCost(initTotalLoads[t]);
		float totalCost = 0;
		for (int t=0; t<newTotalLoads.length; t++)	totalCost += Util.getLoadCost(newTotalLoads[t]);
		float totalWhCompany = Util.arraySum(newTotalLoads);
		float totalWhInitial = Util.arraySum(initTotalLoads);
		// total income
		float totalIncome = 0;
		float totalWhConsumers = 0;
		for (int i=0; i<custLst.size(); i++) {
			totalIncome += custLst.get(i).getTotalBill();
			totalWhConsumers += custLst.get(i).getTotalWh();
		}
		pLog.printf("Company expenses: %.3f, income: %.3f, initialCost: %.3f\n", totalCost, totalIncome, initTotalCost);
		pLog.printf("Total wh from company: %.3f, from consumer: %.3f, initial: %.3f\n", totalWhCompany, totalWhConsumers, totalWhInitial);
		
		// summary for consumers
		/*
		for (int i=0; i<custLst.size(); i++){
			pLog.printf("Consumer-%d, idx: %.1f, origPaid: %.3f, bidPaid:%.3f\n", i, custLst.get(i).getIdxValuation(), custLst.get(i).getOrigCost(initTotalLoads), custLst.get(i).getTotalBill());
		}
		*/
		pLog.println(Util.savingSummary(IdxValuations, custLst, initTotalLoads));
		
		if (VERBOSE == 1){
			// print count of strategy
			System.out.println("Total Customers=" + custLst.size());
			System.out.println("init: maxLoad=" + initPeakLoad + ", avgLoads=" + initAvgLoad);
			System.out.println(Util.arrToStr(initTotalLoads));
			System.out.println("end: maxLoad=" + Util.arrayMax(newTotalLoads)+ ", avgLoads="+ Util.arrayAvg(newTotalLoads));
			System.out.println(Util.arrToStr(newTotalLoads));
			// presents the utility summary
			
		}

		float[] results = {PAR, AUP, systemSaving, systemCost};
		if (VERBOSE == 1) System.out.printf("PAR: %.3f, AUP: %.3f, systemSaving: %.3f, systemCost: %.3f", PAR, AUP, systemSaving, systemCost);
		pLog.close();
		return results; 
	}
	
	
	// TODO: make partcpRate, pRdist, DEVUSAGE, and TIMEPRICECOEFF tidy!
	private ArrayList<Customer> createCustomers(float[] IdxValuations, float[] ValDist){
		
				
		// create data structure for customers
		ArrayList<Customer> custLst = new ArrayList<Customer>();
		int distTableLen = 100;
		float[] distTable = Util.createProbDistTable(IdxValuations, ValDist, distTableLen);
		
		//.. System.out.println(Util.arrToStr(distTable));
		// creating customers
		// loop over all customer type
		for (int i=0; i<custTypeLst.size(); i++){
			// TODO create accessor for this .type
			// get the customer type
			String type = custTypeLst.get(i).type;
			
			// how many customer we should create
			int count = custTypeLst.get(i).count;
			
			// get the list of apps
			ArrayList<ApplianceUsage> appUsage = custTypeLst.get(i).appliances;
			ArrayList<Integer> apps = new ArrayList<Integer>();
			for (int j=0; j<appUsage.size(); j++){
				String appId = appUsage.get(j).id; 
				// search for id in appLst
				int idx = 0;
				while (!appLst.get(idx).id.equalsIgnoreCase(appId)) {
					idx ++;
				}
				if ( appLst.get(idx).id.equalsIgnoreCase(appId) ) {
					apps.add(idx);
				}
			}
			
			// create customer for this type one by one
			for (int j=0; j<count; j++){
				// assign the type and participation rate				
				Customer cust = new Customer(type, TIMEPRICECOEFF, MAXTIME, distTable[(int) (rnd.nextDouble() * distTableLen)], rnd);
				// define the list of apps owned by this customer
				cust.addApps(apps);		
				// now set the time for each apps
				cust.addAppUsages(custTypeLst.get(i).appliances, rnd, 0);
				// add to the customer list
				custLst.add(cust);								
			}
			
		} // end for all customer type
		
		// end of creating customers 
		// all customers data is now stored in custLst
		return custLst;

	}
	
	private ArrayList<Appliance> readAppliancesFile(String filename) throws ParserConfigurationException, SAXException, IOException {

		// assign some string constants
		String appString = "appliance";
		String idString = "id";
		String nameString = "name";
		String powerString = "power";
		
		// create the data structure
		ArrayList<Appliance> apps = new ArrayList<Appliance>();

		// open and read the file
		File file = new File(filename);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();			
		NodeList nodeLst = doc.getElementsByTagName(appString);		
		
		for (int i = 0; i < nodeLst.getLength(); i++) {
			Element appElmnt = (Element) nodeLst.item(i);
			String idValue = appElmnt.getAttribute(idString);
			
			NodeList nameElmntLst = appElmnt.getElementsByTagName(nameString);
			Element nameElmnt = (Element) nameElmntLst.item(0);
			String nameValue = nameElmnt.getFirstChild().getNodeValue();
			
			NodeList powerElmntLst = appElmnt.getElementsByTagName(powerString);
			Element powerElmnt = (Element) powerElmntLst.item(0);
			String powerValue = powerElmnt.getFirstChild().getNodeValue();
			
			Appliance app = new Appliance(idValue, nameValue, Integer.parseInt(powerValue));
			apps.add(app);
		}
		
		return apps;
		
	}
	
	private ArrayList<CustomerType> readCustomersFile(String filename) throws ParserConfigurationException, SAXException, IOException {
		String typeString = "type";
		String countString = "count";
		String appsString = "appliances";
		String appString = "appliance";
		String appidString = "appid";
		String usageString = "usagegroup";
		String timeslotString = "timeslot";
		String startString = "start";
		String endString = "end";
		String devString = "dev";
		String devUsageString = "devUsage";
		String durationString = "duration";
		
		// create the data structure for the customer types
		ArrayList<CustomerType> custTypeLst = new ArrayList<CustomerType>();
				
		File file = new File(filename);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		NodeList nodeLst = doc.getElementsByTagName("customer");
	 		
		for (int i = 0; i < nodeLst.getLength(); i++) {
			Node custNode = nodeLst.item(i);
			if (custNode.getNodeType() == Node.ELEMENT_NODE) {
					
				Element custElmnt = (Element) custNode;	
				
				// get 'type'
				NodeList typeElmntLst = custElmnt.getElementsByTagName(typeString);
				if (typeElmntLst.getLength()==0 ){
					System.err.println("[main::readCustomersFile] Error: Element '"+ typeString +"' not found in customer #" + (i+1));
					System.exit(1);
				}
				Element typeElmnt = (Element) typeElmntLst.item(0);
				String typeValue = typeElmnt.getFirstChild().getNodeValue();								
				
				// get 'count'
				NodeList countElmntLst = custElmnt.getElementsByTagName(countString);
				if (countElmntLst.getLength()==0 ){
					System.err.println("[main::readCustomersFile] Error: Element '"+ countString +"' not found in customer #" + (i+1));
					System.exit(1);
				}
				Element countElmnt = (Element) countElmntLst.item(0);
				String countValue = countElmnt.getFirstChild().getNodeValue();						
				
				// create CustomerType data structure
				CustomerType custType = new CustomerType(typeValue, Integer.parseInt(countValue));

				// get 'appliances'
				NodeList appsElmntLst = custElmnt.getElementsByTagName(appsString);
				if (countElmntLst.getLength()==0 ){
					System.err.println("[main::readCustomersFile] Error: Element '" + appsString + "' not found in customer #" + (i+1));
					System.exit(1);
				}
				Element appsElmnt = (Element) appsElmntLst.item(0);
				
				//  get all 'appliance'
				NodeList appElmntLst = appsElmnt.getElementsByTagName(appString);					
				for (int j=0;j<appElmntLst.getLength();j++ ){
					Element appElmnt = (Element) appElmntLst.item(j);
					
					// get 'appid'
					NodeList appidElmntLst = appElmnt.getElementsByTagName(appidString);
					Element appidElmnt = (Element) appidElmntLst.item(0);
					String appid = appidElmnt.getFirstChild().getNodeValue();
					
					// create data structure for each appliance
					ApplianceUsage appliance = new ApplianceUsage(appid);
					
					// get all 'usage'
					NodeList usageElmntLst = appElmnt.getElementsByTagName(usageString);
					for (int k=0; k<usageElmntLst.getLength(); k++){
						Element usageElmnt = (Element) usageElmntLst.item(k);
						
						// create a data structure for a UsageGroup
						UsageGroup usageGroup = new UsageGroup();
						
						// get 'timeslot'
						NodeList timeslotElmntLst = usageElmnt.getElementsByTagName(timeslotString);
						for (int m=0; m<timeslotElmntLst.getLength(); m++){								
							Element timeslotElmnt = (Element) timeslotElmntLst.item(m);
							String startValue = timeslotElmnt.getAttribute(startString);
							String endValue = timeslotElmnt.getAttribute(endString);
							String devValue = timeslotElmnt.getAttribute(devString);
							
							usageGroup.addTimeslot(startValue, endValue, devValue);
						}
						
						// get 'duration'
						NodeList durationElmntLst = usageElmnt.getElementsByTagName(durationString);
						Element durationElmnt = (Element) durationElmntLst.item(0);
						String durationValue = durationElmnt.getFirstChild().getNodeValue();
						String devValue = durationElmnt.getAttribute(devString);
						String devUsageValue = durationElmnt.getAttribute(devUsageString);
						usageGroup.setDuration(devValue, devUsageValue, durationValue);
						appliance.addUsageGroup(usageGroup);
						
					} // end for usageGroup iteration
					
					custType.addAppliance(appliance);
					
				} // end for appliance iteration
				
				custTypeLst.add(custType);
				
			} // end if
			
		} // end for iterate over all customer Type
		
		return custTypeLst;
			
	}
}


/**
 * Important: once used (method auctioning), constructor and addBid has to be called again.
 * Method auctioning removes winner bids and decrease resources bid.
 * 
 * @author wijaya
 *
 */
class Auction{
	
	// for each timeslot, there are several bids
	ArrayList<ArrayList<Bid>> bids;
	int[] loadsBid;
	Random rnd;
	
	public Auction(int[] LoadsBid, Random Rnd){
		rnd = Rnd;
		// loadsBid.length = the number of time slot available		
		bids = new ArrayList<ArrayList<Bid>>();
		for (int i=0; i<LoadsBid.length; i++){
			ArrayList<Bid> bid = new ArrayList<Bid>();
			bids.add(bid);
		}
		loadsBid = new int[LoadsBid.length];
		System.arraycopy(LoadsBid, 0, loadsBid, 0, LoadsBid.length);
	}
	
	
	public void addBid(int timeslot, int consumer, float valuation, int amount){
		Bid bid = new Bid(consumer, valuation, amount);
		bids.get(timeslot).add(bid);		
	}
	
	
	/**
	 * 
	 * @return list of winner (consumerId) per time slot
	 */
	public ArrayList<ArrayList<BidWon>> auctioning(ArrayList<Customer> custLst){		
		
		//..System.out.println("Enter auctioning...");
		
		ArrayList<ArrayList<BidWon>> winners = new ArrayList<ArrayList<BidWon>>();
		// for each time slot
		//..System.out.println(bids);
		for (int t=0; t<loadsBid.length; t++){
			
			ArrayList<BidWon> winnersT = new ArrayList<BidWon>(); 
			if (bids.get(t).size() > 0) {
				int resourcesLeft = loadsBid[t];
				//..System.out.printf("resourcesLeft at time %d: %d \n", t,resourcesLeft);
				//..System.out.printf("bids size: %d\n",bids.get(t).size());
				boolean finish = false;
				int initialSize = bids.get(t).size();
				int initialResources = resourcesLeft;
				while (finish == false && bids.get(t).size()>0 ) {
					// get the max bid
					float maxBid = bids.get(t).get(0).valuation;
					int idxMax = 0;
					int consMax = bids.get(t).get(0).consumerId;
					for (int i=1; i<bids.get(t).size(); i++){
						if (bids.get(t).get(i).valuation > maxBid){
							maxBid = bids.get(t).get(i).valuation;
							idxMax = i;
							consMax = bids.get(t).get(i).consumerId;
						}
					}
					//..System.out.println(maxBid);
					if (resourcesLeft < bids.get(t).get(idxMax).amount && (rnd.nextDouble() > 0.5) ) {
						// not accepted
						finish = true;
						//..System.out.println("rejected");
					} else {					
						int bidGot = Math.min(resourcesLeft, bids.get(t).get(idxMax).amount);
						consMax = bids.get(t).get(idxMax).consumerId;
						BidWon bidWon = new BidWon(consMax, bids.get(t).get(idxMax).valuation, bidGot);
						winnersT.add(bidWon);
						resourcesLeft = resourcesLeft - bidGot;
						// remove the winner from the pool
						bids.get(t).remove(idxMax);
					}					
					if (resourcesLeft <= 0 ) {
						finish = true;
					}
					if (finish) {					
						System.out.println("t: "+ t + ", resourcesToBid: "+ initialResources + ", lastWon: "+custLst.get(consMax).getIdxValuation() + ", size: "+ initialSize + " to " + bids.get(t).size());
					}
				}	
				// update all price, get the maxbid now
				//.. System.out.printf("After process, resourcesLeft at time %d: %d \n", t,resourcesLeft);
				if (winnersT.size()>0) {
					float price = winnersT.get(winnersT.size()-1).getPaidPrice();
					if (bids.get(t).size()>0) {
						float maxBid = bids.get(t).get(0).valuation;
						int idxMax = 0;
						for (int i=1; i<bids.get(t).size(); i++){
							if (bids.get(t).get(i).valuation > maxBid){
								maxBid = bids.get(t).get(i).valuation;
								idxMax = i;
							}
						}
						price = bids.get(t).get(idxMax).valuation;
					}
					for (int i=0; i<winnersT.size(); i++){
						winnersT.get(i).setPaidPrice(price);
					}
				}
				//\\ update price finished
				
				//..System.out.printf("t: %d, resourcesLeft: %d, bidLeft: %d \n", t, resourcesLeft, bids.get(t).size());
				loadsBid[t] = resourcesLeft;
			}
			winners.add(winnersT);
			
		}
		//..System.out.println("Exit auctioning...");
		return winners; 
	}
}


class Bid{
	int consumerId;
	float valuation;
	int amount;
	
	public Bid(int ConsumerID, float Valuation, int Amount){
		consumerId = ConsumerID;
		valuation = Valuation;
		amount = Amount;
	}	
	
}

class BidWon{
	int consumerId;
	float paidPrice;
	int amount;
	
	public BidWon(int ConsumerID, float PaidPrice, int Amount){
		consumerId = ConsumerID;
		paidPrice = PaidPrice;
		amount = Amount;
	}	
	
	public float getPaidPrice(){
		return paidPrice;
	}
	
	public void setPaidPrice(float price){
		paidPrice = price;
	}

}
