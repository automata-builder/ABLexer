package com.abuilder.auto.dfa.toolbox;

import java.util.LinkedHashMap;
import java.util.Map;

import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFABaseMatrix;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFAMxRow;

public class NullDFARowRemover {

	private final DFAProduct product;
	private final DFAMxCollBldr dfaMxCollBldr;
	private final MinimizeHelper minHelp;
	
	public NullDFARowRemover(DFAProduct product, MinimizeHelper minHelp) {
		this.product = product;
		this.dfaMxCollBldr = product.getDFAMxCollBldr();
		this.minHelp = minHelp;
	}

	public void remove() {

		System.out.println("\nNullDFARowRemover\n");

		for (int mxID = 0; mxID < dfaMxCollBldr.getMxCount(); mxID++) {

			DFABaseMatrix mx = dfaMxCollBldr.getDFAMatrix(mxID);

			if (mx.getRowCount() == mx.getRowMap().lastKey() + 1) {
				continue;
			}

			System.out.println("Mx " + mx.mxID);

			int currRowIndx = 0;

			Map<Integer, Integer> remRowID_NewRowID = new LinkedHashMap<>();

			for (DFAMxRow row : mx.getRows()) {

				if (row.getRowID() != currRowIndx) {
					remRowID_NewRowID.put(row.getRowID(), currRowIndx);

				}

				++currRowIndx;
			}

			if (remRowID_NewRowID.size() == 0) {
				throw new RuntimeException();
			}

			for (Integer remRowID : remRowID_NewRowID.keySet()) {
				System.out.println("Replace row: " + remRowID + " -> " + remRowID_NewRowID.get(remRowID));
				mx.swapRowID(remRowID, remRowID_NewRowID.get(remRowID));
			}

			minHelp.fixBadTrnMx(mx, remRowID_NewRowID);
		}
	}	
}
