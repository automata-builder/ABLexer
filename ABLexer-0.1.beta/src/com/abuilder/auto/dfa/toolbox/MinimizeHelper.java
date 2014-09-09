package com.abuilder.auto.dfa.toolbox;

import java.util.HashMap;
import java.util.Map;

import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFABaseMatrix;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFAMxRow;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFARngMatrix;

public class MinimizeHelper {

	private final DFAProduct product;
	private final DFAMxCollBldr dfaMxCollBldr;
	
	public MinimizeHelper(DFAProduct product){
		this.product = product;
		this.dfaMxCollBldr = product.getDFAMxCollBldr();
	}
	
	void fixBadTrnMx(DFABaseMatrix minimizedMx, Map<Integer, Integer> remRowID_NewRowID) {

		for (int mxID = 0; mxID < dfaMxCollBldr.getMxCount(); mxID++) {

			DFABaseMatrix badTrnMx = dfaMxCollBldr.getDFAMatrix(mxID);

			if (badTrnMx.mxID != DFABaseMatrix.BASE_MXID) {
				
				DFARngMatrix rBadTrnMx = (DFARngMatrix) badTrnMx;
				
				if(minimizedMx.mxID != DFABaseMatrix.BASE_MXID){
					
					if(((DFARngMatrix)minimizedMx).getRuleID() != rBadTrnMx.getRuleID()){
						continue;
					}
				}
				
				replaceReAndTail(minimizedMx, rBadTrnMx, remRowID_NewRowID);
			}

			for (DFAMxRow row : badTrnMx.getRows()) {

				for (DFAMxCell cell : row.values()) {

					HashMap<DFAMxTrn, DFAMxTrn> remTrn_newTrn = new HashMap<>();

					for (DFAMxTrn trn : cell.values()) {

						if (trn.getToMxID() == minimizedMx.mxID) {

							Integer newRowID = remRowID_NewRowID.get(trn.getToDfaID());

							if (newRowID != null) {
								remTrn_newTrn.put(trn, new DFAMxTrn(product, minimizedMx.mxID, newRowID));
							}
						}
					}

					for (DFAMxTrn remTrn : remTrn_newTrn.keySet()) {
						cell.remDFATran(remTrn);
						cell.addDFATran(remTrn_newTrn.get(remTrn));
					}
				}
			}
		}
	}

	void replaceReAndTail(DFABaseMatrix minimizedMx, DFARngMatrix badTrnMx, Map<Integer, Integer> remRowID_newRowID) {

		DFAMxTrn trn = badTrnMx.getReTranCell().getValue();
		
		if (trn.getToMxID() == minimizedMx.mxID) {

			Integer newRowID = remRowID_newRowID.get(trn.getToDfaID());
			if (newRowID != null) {
				badTrnMx.replaceReTran(newRowID);
			}
		}

		trn = badTrnMx.getTailTranCell().getValue();
		
		if (trn.getToMxID() == minimizedMx.mxID) {

			Integer newRowID = remRowID_newRowID.get(badTrnMx.getTailTranCell().getValue().getToDfaID());
			if (newRowID != null) {
				badTrnMx.replaceTailTran(newRowID);
			}
		}
	}
}
