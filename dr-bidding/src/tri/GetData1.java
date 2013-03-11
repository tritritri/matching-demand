package tri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class GetData1 {

	/**
	 * Get data for the system cost
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {		
		
		String dirName = "log2\\";
		
		float[] Cut = {0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
		int randomCount = 10;
		
		float[][] data = new float[Cut.length][randomCount];
		//float[] avg = new float[Cut.length];
		//float[] stddev = new float[Cut.length];
		
		
		// get the data
		for (int c=0; c<Cut.length; c++){
			for (int r=1; r<=randomCount; r++){
				String fileName = dirName+"log-U-c"+Cut[c]+"-r"+r+".log";
				BufferedReader fInput = new BufferedReader(new FileReader(fileName));
				System.out.println("processed: " + fileName); 

				String line = "";
				boolean found=false;
				while (found==false){
					line = fInput.readLine();
					if (line.indexOf("Company expenses") >= 0) found = true;
				}
				//for (int i=0; i<10; i++) line = fInput.readLine();
				
				// line 11
				//line = fInput.readLine();
				String[] lineArray = line.split(",");
				
				String[] initial = lineArray[0].split(": ");
				data[c][r-1] = Float.parseFloat(initial[1]);
				System.out.println(initial[1]);
			}
		}
		
		
		// take the avg 
		float[] avgData = new float[Cut.length];		
		for (int c=0; c<Cut.length; c++){
			avgData[c] = 0;
			for (int r=0; r<randomCount; r++) {
				avgData[c] = avgData[c] + data[c][r];					
			}
			avgData[c] = avgData[c] / randomCount;
		}

		// take the std dev
		float[] stdDev = new float[Cut.length];		
		for (int c=0; c<Cut.length; c++){
			for (int r=0; r<randomCount; r++) {					
				stdDev[c] = (float) Math.pow(data[c][r] - avgData[c], 2);					
			}
			stdDev[c] = (float) Math.sqrt( stdDev[c] / randomCount );			
		}
		
		// write the data
		String fileOutput = "..\\..\\paper-result\\Summ-cost.txt";
		PrintWriter pSumm = new PrintWriter (new File(fileOutput));
		pSumm.printf("cut,cost,conf\n");
		double fixedTerm = 1.96 / Math.sqrt(randomCount); // conf interval
		for (int c=0; c<Cut.length; c++){
			pSumm.printf("%d %.3f %3f\n", (int) (Cut[c]*100), avgData[c], fixedTerm*stdDev[c]);
		}
		pSumm.close();
		System.out.println("finish");


	}

}
