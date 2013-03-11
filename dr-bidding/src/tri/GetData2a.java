package tri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class GetData2a {

	/**
	 * Get data for shift 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		float[] idxValNorm  = {1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f, 1.7f, 1.8f, 1.9f};
		float[] idxValUS  = {1.0f, 1.3f, 1.5f, 1.6f, 1.9f};
		ArrayList<float[]> idxVals = new ArrayList<float[]>();
		idxVals.add(idxValNorm);
		idxVals.add(idxValUS);		
		String[] names = {"N", "U"};
		
		for (int i=0; i<names.length; i++){
			doit(names[i], idxVals.get(i));
		}
		
	}
	public static void doit(String name, float[] idxVals) throws IOException{
		String dirName = "log2\\";
		
		float[] Cut = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f};


		int randomCount = 10;

		float[][][] data = new float[Cut.length][idxVals.length][randomCount];

		for (int i=0; i<Cut.length; i++){
			for (int j=0; j<idxVals.length; j++){
				for (int r=0; r<randomCount; r++){
					data[i][j][r] = 0;					
				}
			}
		}
		
		// get the data
		for (int c=0; c<Cut.length; c++){
			for (int r=0; r<randomCount; r++){
				String fileName = dirName+"log-"+ name + "-c"+Cut[c]+"-r"+(r+1)+".log";
				BufferedReader fInput = new BufferedReader(new FileReader(fileName));
				System.out.println("processed: " + fileName); 

				String line = "";
				boolean found=false;
				while (found==false){
					line = fInput.readLine();
					if (line.indexOf("UtilitySummary:") >= 0) found = true;
				}
				String[] s1 = line.split(": ");
				String[] s2 = s1[1].split("], ");
				String[] s3 = s2[0].split("=");
				String[] s4 = s3[1].substring(1).split(",");
				for (int v=0; v<idxVals.length; v++){
					data[c][v][r] = Float.parseFloat(s4[v]); 
				}				

			}
				
		}

		// take the avg 
		float[][] avgData = new float[Cut.length][idxVals.length];		
		for (int i=0; i<Cut.length; i++){
			for (int j=0; j<idxVals.length; j++){
				for (int r=0; r<randomCount; r++) {
					avgData[i][j] = avgData[i][j] + data[i][j][r];					
				}
				avgData[i][j] = avgData[i][j] / randomCount;
			}
		}

		// take the std dev
		float[][] stdDev = new float[Cut.length][idxVals.length];		
		for (int i=0; i<Cut.length; i++){
			for (int j=0; j<idxVals.length; j++){
				for (int r=0; r<randomCount; r++) {					
					stdDev[i][j] = (float) Math.pow(data[i][j][r] - avgData[i][j], 2);					
				}
				stdDev[i][j] = (float) Math.sqrt( stdDev[i][j] / randomCount );
			}
		}

		// write the data
		String fileOutput = "..\\..\\paper-result\\Summ-shift-"+name+".txt";
		PrintWriter pSumm = new PrintWriter (new File(fileOutput));
		pSumm.printf("idxVal");
		for (int c=0; c<Cut.length; c++){
			pSumm.printf(",cut=%.1f,conf",Cut[c]);
		}
		pSumm.printf("\n");
		
		double fixedTerm = 1.96 / Math.sqrt(randomCount); // conf interval
		for (int i=0; i<idxVals.length; i++){
			
			pSumm.printf("%.1f", idxVals[i]);
			
			for (int c=0; c<Cut.length; c++){
				pSumm.printf(" %.3f %3f", avgData[c][i], fixedTerm*stdDev[c][i]);
			}
			pSumm.printf("\n");
		}
		pSumm.close();
		System.out.println("finish");
		
	}
}
