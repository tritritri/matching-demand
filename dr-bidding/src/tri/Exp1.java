package tri;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Exp1 {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		

		String CustTypeFile = "cons10000.xml";
		String ApplsFile = "appliances.xml";


		DRSimulation sim = new DRSimulation(ApplsFile, CustTypeFile);

		// float[] par1 = sim.run(RandomSeed);
		// float CUT = 0.50f;
		int MINLOAD = 100;
		
		float[] idxValUS  = {1.0f, 1.3f, 1.5f, 1.6f, 1.9f};
		float[] valDistUS = {0.4f, 0.2f, 0.2f, 0.1f, 0.1f};

		float[] idxValNorm  = {1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f, 1.7f, 1.8f, 1.9f};
		float[] valDistNorm = {0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f};
		
		ArrayList<float[]> idxVals = new ArrayList<float[]>();
		idxVals.add(idxValUS);
		idxVals.add(idxValNorm);
		String[] iName = {"U", "N"};
		
		ArrayList<float[]> valDist = new ArrayList<float[]>();
		valDist.add(valDistUS);
		valDist.add(valDistNorm);
		
		
		
		float[] Cut = {0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
		
		for (int r=1; r<=10; r++){ // random seed
			for (int c=0; c<Cut.length; c++){ // different cut
				for (int i=0; i<idxVals.size(); i++){					
					sim.expRun(Cut[c], MINLOAD, idxVals.get(i), valDist.get(i), 1, "log\\log-"+iName[i]+"-c"+Cut[c]+"-r"+r+".log", 1, r);
					
				}
			}
		}
		
		
	}

}
