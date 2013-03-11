package tri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class GetData4 {

	/**
	 * Get data for cost saving
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		doit();
		
	}
	public static void doit() throws IOException{
		String dirName = "log2\\";
		
		String[] Dist = {"N", "U"};
		float[] Cut = {0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f};


		int randomCount = 10;

		float[][][] data = new float[Dist.length][Cut.length][randomCount];

		for (int i=0; i<Dist.length; i++){
			for (int j=0; j<Cut.length; j++){
				for (int r=0; r<randomCount; r++){
					data[i][j][r] = 0;					
				}
			}
		}
		
		// get the data
		for (int d=0; d<Dist.length; d++){
			for (int c=0; c<Cut.length; c++){
				for (int r=0; r<randomCount; r++){
					String fileName = dirName+"log-"+ Dist[d] + "-c"+Cut[c]+"-r"+(r+1)+".log";
					BufferedReader fInput = new BufferedReader(new FileReader(fileName));
					System.out.println("processed: " + fileName); 
	
					String line = "";
					boolean found=false;
					while (found==false){
						line = fInput.readLine();
						if (line.indexOf("Company expenses:") >= 0) found = true;
					}
					
					String[] lineArray = line.split(",");
					float expense = Float.parseFloat(lineArray[0].substring(18));
					float income = Float.parseFloat(lineArray[1].substring(9));
					float diff = income - expense;
					
					data[d][c][r] = diff / expense * 100;
					
					
					/*
					int expense
					int income
					
					String[] initial = lineArray[0].split(": ");
					data[c][r-1] = Float.parseFloat(initial[1]);
					System.out.println(initial[1]);

					
					String s1 = line.substring(17);
					String s2 = s1.substring(0, s1.length()-1);
					String[] s3 = s2.split(",");
					for (int v=0; v<idxVals.length; v++){
						data[c][v][r] = Float.parseFloat(s3[v]); 
					}				
					*/
				}
			}
		}
		
		
		// take the avg 
		float[][] avgData = new float[Dist.length][Cut.length];		
		for (int i=0; i<Dist.length; i++){
			for (int j=0; j<Cut.length; j++){
				for (int r=0; r<randomCount; r++) {
					avgData[i][j] = avgData[i][j] + data[i][j][r];					
				}
				avgData[i][j] = avgData[i][j] / randomCount;
			}
		}

		// take the std dev
		float[][] stdDev = new float[Dist.length][Cut.length];		
		for (int i=0; i<Dist.length; i++){
			for (int j=0; j<Cut.length; j++){
				for (int r=0; r<randomCount; r++) {					
					stdDev[i][j] = (float) Math.pow(data[i][j][r] - avgData[i][j], 2);					
				}
				stdDev[i][j] = (float) Math.sqrt( stdDev[i][j] / randomCount );
			}
		}

		// write the data
		String fileOutput = "..\\..\\paper-result\\Summ-company-add.txt";
		PrintWriter pSumm = new PrintWriter (new File(fileOutput));
		pSumm.printf("cut,savingN,conf,savingU,conf\n");
		
		double fixedTerm = 1.96 / Math.sqrt(randomCount); // conf interval
		for (int i=0; i<Cut.length; i++){
			
			pSumm.printf("%d", (int) (Cut[i]*100));
			
			for (int c=0; c<Dist.length; c++){
				pSumm.printf(" %.3f %3f", avgData[c][i], fixedTerm*stdDev[c][i]);
			}
			pSumm.printf("\n");
		}
		pSumm.close();
		System.out.println("finish");
		
	}
}
