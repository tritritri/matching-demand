package tri;

import java.util.ArrayList;
import java.util.Random;


public class Util{

	public static boolean allZeroes(int[] arr){
		boolean result = true; 
		for (int i=0; i<arr.length; i++){
			if (arr[i] != 0)
				return false;
		}
		return result;
	}
	
	public static String arrToStr(int[] arr){
		String result = "";
		for (int i=0; i<arr.length; i++){
			result = result + i+"="+arr[i]+", ";
		}
		return result;
	}

	public static String arrToStrWithoutIdx(int[] arr){
		String result = "";
		for (int i=0; i<arr.length; i++){
			result = result +arr[i]+",";
		}
		return result.substring(0, result.length()-1);
	}

	public static String fastArrToStr(int[] arr){
		String result = "";
		for (int i=0; i<arr.length; i++){
			result = result +arr[i]+",";
		}
		return result;
	}

	/*
	public static int[] getStrategyCount(float[] partcpRate, ArrayList<Customer> custLst) {
		int[] result = new int[partcpRate.length];
		for (int i=0; i<custLst.size(); i++){
			int searchIdx = 0;
			while (searchIdx<partcpRate.length && partcpRate[searchIdx]!=custLst.get(i).partcpRate) searchIdx ++;
			if (partcpRate[searchIdx]==custLst.get(i).partcpRate) {
				result[searchIdx] ++;
			} else {
				System.err.printf("[Util::getStrategyCount] Error: cust %d strategy/partcpRate: %.3f not found.\n", i, custLst.get(i).partcpRate);
				System.exit(1);
			}
		}
		return result; 		
	}
	*/
	
	public static float[] createProbDistTable(float[] Label, float[] Dist, int TabLen) {
		
		// check dist sum
		float distSum = 0;
		for (int i=0; i<Dist.length; i++){
			distSum += Dist[i]; 
		}
		if (distSum < 0.99f || distSum > 1.001) {
			System.err.printf("[Util::createProbDistTable] Error: The sum of the distribution = %.3f, it should be 1.00", distSum);
			System.exit(1);
		}
		
		// create the table lookup
		float[] distTab = new float[TabLen];
		int st = 0;
		for (int i=0; i<Dist.length-1; i++){
			// how many element shall we set now 
			int lNow = (int) (Dist[i] * TabLen);
			for (int j=st; j<st+lNow; j++){
				distTab[j] = Label[i];
			}
			st = st+lNow;
		}
		for (int j=st; j<TabLen; j++){
			distTab[j] = Label[Label.length-1];
		}
		
		return distTab;

	}
	
	public static String utilitySummary(float[] idxValuations, ArrayList<Customer> custLst) {
		String shiftS = "";
		String priceS = "";
		for (int i=0; i<idxValuations.length; i++){
			float shiftU = 0;
			float priceU = 0;
			int ctr = 0;
			for (int j=0; j<custLst.size(); j++){				
				if (custLst.get(j).getIdxValuation() == idxValuations[i]) {
					shiftU += custLst.get(j).getNormalizeShift();
					priceU += custLst.get(j).getPricePerKWh();
					ctr++;
				}
			}
			shiftS = shiftS + (shiftU/ctr)+",";
			priceS = priceS + (priceU/ctr)+",";
		}
		shiftS = shiftS.substring(0, shiftS.length() - 1);
		priceS = priceS.substring(0, priceS.length() - 1);
				
		return "shiftU=["+shiftS+"], priceUPerKWh=[" + priceS+"]";
	}
	
	
	public static String savingSummary(float[] idxValuations, ArrayList<Customer> custLst, int[] initTotalLoads) {
		String savingS = "";
		for (int i=0; i<idxValuations.length; i++){
			float saving = 0;
			int ctr = 0;
			for (int j=0; j<custLst.size(); j++){
				if (custLst.get(j).getIdxValuation() == idxValuations[i]) {
					saving += (custLst.get(j).getOrigCost(initTotalLoads) - custLst.get(j).getTotalBill())/custLst.get(j).getOrigCost(initTotalLoads);
					ctr++;
				}
			}
			savingS = savingS + (saving/ctr)+",";			
		}
		savingS = savingS.substring(0, savingS.length() - 1);
		return "saving by group=["+savingS+"]";

	}
	
	/**
	 * Summary of accumulative utility of customers group by participation rate
	 * @param partcpRate
	 * @param custLst
	 * @return
	 */
	/*
	public static String accUtilitySummary(float[] partcpRate, ArrayList<Customer> custLst) {
		String timeS = "";
		String priceS = "";
		for (int i=0; i<partcpRate.length; i++){
			float timeU = 0;
			float priceU = 0;
			int ctr = 0;
			for (int j=0; j<custLst.size(); j++){				
				if (custLst.get(j).partcpRate == partcpRate[i]) {
					timeU += custLst.get(j).accTimeUtility;
					priceU += custLst.get(j).accPriceUtility;
					ctr++;
				}
			}
			timeS = timeS + (timeU/ctr)+",";
			priceS = priceS + (priceU/ctr)+",";
		}
		timeS = timeS.substring(0, timeS.length() - 1);
		priceS = priceS.substring(0, priceS.length() - 1);
				
		return "timeU=["+timeS+"], priceU=[" + priceS+"]";
	}
	*/
	
	/**
	 * Summary of current utility of customers group by participation rate
	 * @param partcpRate
	 * @param custLst
	 * @return
	 */
	/*
	public static String currUtilitySummary(float[] partcpRate, ArrayList<Customer> custLst) {
		String timeS = "";
		String priceS = "";
		for (int i=0; i<partcpRate.length; i++){
			float timeU = 0;
			float priceU = 0;
			int ctr = 0;
			for (int j=0; j<custLst.size(); j++){				
				if (custLst.get(j).partcpRate == partcpRate[i]) {
					timeU += custLst.get(j).currTimeUtility;
					priceU += custLst.get(j).currPriceUtility;
					ctr++;
				}
			}
			timeS = timeS + (timeU/ctr)+",";
			priceS = priceS + (priceU/ctr)+",";
		}
		String result = timeS+priceS;
		result = result.substring(0, result.length() - 1);
		return result;
	}
	*/
	
	public static String arrToStr(float[] arr){
		String result = "";
		for (int i=0; i<arr.length; i++){
			result = result + i+"="+arr[i]+", ";
		}
		return result;
	}

	public static int arrayMax(int[] arr){
		int maxValue = arr[0];
		for (int i = 1; i<arr.length; i++){
			if (arr[i] > maxValue)
				maxValue = arr[i];
		}
		return maxValue;				
	}

	public static float arrayMax(float[] arr){
		float maxValue = arr[0];
		for (int i = 1; i<arr.length; i++){
			if (arr[i] > maxValue)
				maxValue = arr[i];
		}
		return maxValue;				
	}
	
	public static float arrayMin(float[] arr){
		float minValue = arr[0];
		for (int i = 1; i<arr.length; i++){
			if (arr[i] < minValue)
				minValue = arr[i];
		}
		return minValue;						
	}
	
	public static double arrayAvg(int[] arr){
		double avgValue = 0.0;
		for (int i = 0; i<arr.length; i++){
			avgValue = avgValue + arr[i];
		}		
		return avgValue/arr.length;				
	}

	public static float arraySum(float[] arr){
		float totalValue = 0.0f;
		for (int i = 0; i<arr.length; i++){
			totalValue = totalValue + arr[i];
		}		
		return totalValue;				
	}
	
	public static int arraySum(int[] arr){
		int totalValue = 0;
		for (int i = 0; i<arr.length; i++){
			totalValue = totalValue + arr[i];
		}		
		return totalValue;				
	}

	public static double getLoadCost(int load){
		return Math.pow((load+100)/100000,2);
	}
	
	public static double getPrice(int myWh, int LoadOthers){
		int totalLoad = myWh + LoadOthers;
		int totalCost = (int) getLoadCost(totalLoad);
		if (totalLoad > 0)			
			return (myWh+0.0)/(totalLoad+0.0)*totalCost;
		else
			return 0.0;		
	}
	
	
	/**
	 * Calculate customer's price for one day 
	 * @param currWh Customer consumption for one day
	 * @param overAllLoadMinus
	 * @return
	 */
	public static float getPriceFromLoadMinus(int[] currWh, int[] overAllLoadMinus){
		// calculate price
		float price = 0;
		for (int j=0; j<currWh.length; j++){
			// this is the price	
			
			// leveling per 100
			
			/* before the meeting 
			int coeff = (int) Math.pow((overAllLoadMinus[j] + currWh[j])/100,2)+10;
			price = price + (currWh[j]*coeff/100);
			*/
			
			//CHANGE 03.04.2012 price = price + (float) getPrice(currWh[j], overAllLoadMinus[j], 25, 100);
			price = price + (float) getPrice(currWh[j], overAllLoadMinus[j]); // WE HAVE 3 PRICES FOR CRYIN' OUT LOUD
		}
		
		return price;
	}
	
	public static float getTotalPrice(int[] TotalLoad){
		// calculate price
		float totalPrice = 0;
		for (int j=0; j<TotalLoad.length; j++){
			totalPrice = totalPrice + (float) getPrice(TotalLoad[j], 0);	// WE HAVE 3 PRICES FOR CRYIN' OUT LOUD		
		}		
		return totalPrice;		
	}
	
	public static float getPricePerWh(int[] currWh, int[] overAllLoad){
		// calculate price
		float totalPrice = 0;
		float totalWh = 0;
		for (int j=0; j<currWh.length; j++){
			// this is the price

			// leveling per 100

			/* before the meeting 
			int coeff = (int) Math.pow((overAllLoad[j])/100,2)+10;
			totalPrice = totalPrice + (currWh[j]*coeff/100);
			*/
			//CHANGE totalPrice = totalPrice + (float) getPrice(currWh[j], overAllLoad[j]-currWh[j], 25, 100);
			totalPrice = totalPrice + (float) getPrice(currWh[j], overAllLoad[j]-currWh[j]); // WE HAVE 3 PRICES FOR CRYIN' OUT LOUD
			
			totalWh = totalWh + currWh[j];
		}		
		return totalPrice / totalWh;
	}
	
	public static int[] getTotalLoad(ArrayList<Customer> custLst){
		if (custLst.size()>0) {
			int LEN = custLst.get(0).origLoads.length;
			int[] totalLoads = new int[LEN];
			for (int i=0;i<custLst.size();i++){
				for (int j=0; j<LEN; j++){
					totalLoads[j] = totalLoads[j] + custLst.get(i).origLoads[j];
				}
			}					
			return totalLoads;
		} else return null;
	}
	
	
	/**
	 * simply apply uniform noise distribution from 0-NOISERANGE
	 * @param custLst
	 * @param NOISERANGE
	 * @param rnd
	 * @return
	 */
	public static int[] getTotalLoadPerturbed(ArrayList<Customer> custLst, int NOISERANGE, Random rnd){
		if (custLst.size()>0) {
			int LENTIME = custLst.get(0).origLoads.length;
			int[] totalLoads = new int[LENTIME];
			
			// not perturbed, for testing purposes only
			// TODO: should be commented later
			/*
			for (int i=0;i<custLst.size();i++){
				for (int j=0; j<LENTIME; j++){
					totalLoads[j] = totalLoads[j] + custLst.get(i).loads[j];
				}
			}	
			System.out.println("i="+arrToStr(totalLoads));
			*/
			
			totalLoads = new int[LENTIME];
			// perturbed
			for (int i=0;i<custLst.size();i++){
				for (int j=0; j<LENTIME; j++){
					totalLoads[j] = totalLoads[j] + custLst.get(i).origLoads[j] + (int) (rnd.nextDouble()*NOISERANGE);
				}
			}	
			//..System.out.println("p="+arrToStr(totalLoads));
			
			// try to recover the noise
			for (int i=0;i<custLst.size();i++){
				for (int j=0; j<LENTIME; j++){
					totalLoads[j] = totalLoads[j] - (int) (rnd.nextDouble()*NOISERANGE);
				}
			}
			
			for (int j=0; j<LENTIME; j++){
				if (totalLoads[j] < 0) {
					totalLoads[j] = 0;
				}
			}
			
			//..System.out.println("r="+arrToStr(totalLoads));
			//..System.exit(0);
			return totalLoads;
		} else return null;
	}

	public static int[] consumeElectricity(int[] currWh, int startingTime, float usageTime, int appPower, int MAXTIME){
		// consume the electricity
		// the result is accumulated into currWh
		
		int currTime = startingTime;
		while (usageTime>0) {
			float consumeTime = Math.min(1, usageTime);
			int wh = (int) (appPower * consumeTime);
			
			currWh[currTime] = currWh[currTime] + wh; 
			usageTime = usageTime-1;
			currTime++;
			if (currTime>=MAXTIME) currTime = 0;
		}
		return currWh;
	}
}
