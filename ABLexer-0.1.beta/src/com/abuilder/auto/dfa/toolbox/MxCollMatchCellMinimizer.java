package com.abuilder.auto.dfa.toolbox;

import java.util.HashMap;
import java.util.Map;

import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFABaseMatrix;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFAMxRow;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFARngMatrix;

public class MxCollMatchCellMinimizer {

	private final DFAProduct product;
	private final DFAMxCollBldr dfaMxCollBldr;
	private final MinimizeHelper minHelp;
	
	public MxCollMatchCellMinimizer(DFAProduct product, MinimizeHelper minHelp) {
		this.product = product;
		this.dfaMxCollBldr = product.getDFAMxCollBldr();
		this.minHelp = minHelp;
	}

	public void remove() throws Exception {

		for (int mxID = 0; mxID < dfaMxCollBldr.getMxCount(); mxID++) {

			DFABaseMatrix mx = dfaMxCollBldr.getDFAMatrix(mxID);

			System.out.println("Mx = " + mxID + "\n");

			HashMap<Integer, Integer> remRowID_newRowID = new HashMap<>();

			for (int r1 = 0; r1 < mx.getRowCount(); r1++) {

				if (remRowID_newRowID.containsKey(r1)) {
					continue;
				}

				DFAMxRow row1 = mx.getRow(r1);

				for (int r2 = r1 + 1; r2 < mx.getRowCount(); r2++) {

					DFAMxRow row2 = mx.getRow(r2);

					if (row1.equals(row2)) {

						remRowID_newRowID.put(r2, r1);

						System.out.println("Row=" + r1 + " equals row=" + r2);
					}
				}
			}

			if (remRowID_newRowID.size() > 0) {
				minHelp.fixBadTrnMx(mx, remRowID_newRowID);
			}
		}
	}

//	void fixBadTrnMx(DFABaseMatrix targetMx, Map<Integer, Integer> remRowID_NewRowID) {
//
//		for (int mxID = 0; mxID < dfaMxCollBldr.getMxCount(); mxID++) {
//
//			DFABaseMatrix mx = dfaMxCollBldr.getDFAMatrix(mxID);
//
//			if (mx.mxID != DFABaseMatrix.BASE_MXID) {
//				
//				DFARngMatrix rMx = (DFARngMatrix) mx;
//				
//				if(targetMx.mxID != DFABaseMatrix.BASE_MXID){
//					
//					if(rMx.getRuleID() != ((DFARngMatrix)targetMx).getRuleID()){
//						continue;
//					}
//				}
//				
//				replaceReAndTail(targetMx, rMx, remRowID_NewRowID);
//			}
//
//			for (DFAMxRow row : mx.getRows()) {
//
//				for (DFAMxCell cell : row.values()) {
//
//					HashMap<DFAMxTrn, DFAMxTrn> remTrn_newTrn = new HashMap<>();
//
//					for (DFAMxTrn trn : cell.values()) {
//
//						if (trn.getToMxID() == targetMx.mxID) {
//
//							Integer newRowID = remRowID_NewRowID.get(trn.getToDfaID());
//
//							if (newRowID != null) {
//								remTrn_newTrn.put(trn, new DFAMxTrn(product, targetMx.mxID, newRowID));
//							}
//						}
//					}
//
//					for (DFAMxTrn remTrn : remTrn_newTrn.keySet()) {
//						cell.remDFATran(remTrn);
//						cell.addDFATran(remTrn_newTrn.get(remTrn));
//					}
//				}
//			}
//		}
//	}
//
//	void replaceReAndTail(DFABaseMatrix targetMx, DFARngMatrix mx, Map<Integer, Integer> remRowID_newRowID) {
//
//		DFAMxTrn trn = mx.getReTranCell().getValue();
//		
//		if (trn.getToMxID() == targetMx.mxID) {
//
//			Integer newRowID = remRowID_newRowID.get(trn.getToDfaID());
//			if (newRowID != null) {
//				mx.replaceReTran(newRowID);
//			}
//		}
//
//		trn = mx.getTailTranCell().getValue();
//		
//		if (trn.getToMxID() == targetMx.mxID) {
//
//			Integer newRowID = remRowID_newRowID.get(mx.getTailTranCell().getValue().getToDfaID());
//			if (newRowID != null) {
//				mx.replaceTailTran(newRowID);
//			}
//		}
//	}
}
