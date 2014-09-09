package com.abuilder.auto.dfa.toolbox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.abuilder.auto.dfa.nfa.Alphabet;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFABaseMatrix;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFAMxRow;
import com.abuilder.auto.dfa.toolbox.DFAMxCollBldr.DFARngMatrix;

public class MxCollRedundantEpsCellMinimizer {

	private final DFAProduct product;
	private final DFAMxCollBldr dfaMxCollBldr;

	public MxCollRedundantEpsCellMinimizer(DFAProduct product) {
		this.product = product;
		this.dfaMxCollBldr = product.getDFAMxCollBldr();
	}

	public void remove() {

		System.out.println("\nMxCollRedundantEpsCellMinimizer \n");
		
		for (int mxID = 0; mxID < dfaMxCollBldr.getMxCount(); mxID++) {

			DFABaseMatrix mx = dfaMxCollBldr.getDFAMatrix(mxID);
			
			System.out.println("Mx = " + mxID + "\n");
			
			
			ArrayList<DFAMxRow> stk = new ArrayList<>();
			
			Set<Integer> remRows = new HashSet<Integer>();
			
			for (DFAMxRow row : mx.getRows()) {
			
				if(row.isAccept() || row.size() > 1){
					continue;
				}
				
				stk.add(row);
		
				while(true){
					
					DFAMxRow currRow = stk.get(stk.size()-1);

					DFAMxCell currCell = currRow.getDFATran(Alphabet.EpsCharEdge);
					
					if(currCell == null || currCell.size() > 1){
						break;
					}
					
					DFAMxTrn trn = currCell.values().iterator().next();
					if(trn.getToMxID() != mxID){
						break;
					}
					
					if(mx instanceof DFARngMatrix){
						if(trn.getToDfaID() == DFAState.DFAID.REENTRY_DFAID ){
							break;
						}
					}
					
					System.out.println("EPS Elim candidate: row= " + currRow.getRowID());
					stk.add(mx.getRow(trn.getToDfaID()));
				}
				
				stk.remove(stk.size()-1);
				if(stk.size() > 0){
					remRows.addAll(replace(mx, stk));
					stk.clear();
				}
			}
			
			
			for (Integer rowIndx : remRows) {
				System.out.println("Removing row: " + rowIndx);
				mx.removeRow(rowIndx);
			}
			
		}
	}
	
	Set<Integer> replace(DFABaseMatrix targetMx, ArrayList<DFAMxRow> stk){
		
		int newRowPtr = stk.get(stk.size()-1).getDFATran(Alphabet.EpsCharEdge).values().iterator().next().getToDfaID();
		HashSet<Integer> remRows = new HashSet<>();
		
		for (int i = 0; i < stk.size(); i++) {
			remRows.add(stk.get(i).getRowID());
		}
		
		for (int mxID = 0; mxID < dfaMxCollBldr.getMxCount(); mxID++) {
			
			DFABaseMatrix mx = dfaMxCollBldr.getDFAMatrix(mxID);
			
			for(DFAMxRow row : mx.getRows()){
				
				if(mx.mxID == targetMx.mxID && remRows.contains(row.getRowID())){
					continue;
				}
				
				for(DFAMxCell cell : row.values()){
					
					HashSet<DFAMxTrn> remTrns = new HashSet<>();
					
					for(DFAMxTrn trn : cell.values()){
						if(trn.getToMxID() == targetMx.getMxID()){
							
							if(remRows.contains(trn.getToDfaID())){
								remTrns.add(trn);
							}
							
						}
					}
					
					for(DFAMxTrn trn :remTrns){
						cell.remDFATran(trn);
						cell.addDFATran(new DFAMxTrn(cell.getProduct(), targetMx.mxID, newRowPtr));
					}
				}
			}
			
			if(mxID > 0){
				DFARngMatrix rMx = (DFARngMatrix)mx;
				if(rMx.getReTranCell().getValue().getToMxID() == targetMx.mxID){
					if(remRows.contains(rMx.getReTranCell().getValue().getToDfaID())){
						rMx.replaceReTran(newRowPtr);
					}
				}
				
				if(rMx.getTailTranCell().getValue().getToMxID() == targetMx.mxID){
					if(remRows.contains(rMx.getTailTranCell().getValue().getToDfaID())){
						rMx.replaceTailTran(newRowPtr);
					}
				}
			}
			
		}
		return remRows;
	}
}
