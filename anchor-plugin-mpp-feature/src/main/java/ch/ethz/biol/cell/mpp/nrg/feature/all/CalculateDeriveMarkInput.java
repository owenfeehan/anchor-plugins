package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoCollection;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateDeriveMarkInput extends FeatureCalculation<FeatureInputMark, FeatureInputAllMemo> {

	@Override
	protected FeatureInputMark execute(FeatureInputAllMemo params) throws FeatureCalcException {
		
		MemoCollection list = params.getPxlPartMemo();
		
		if (list.size()==0) {
			throw new FeatureCalcException("No mark exists in the list");
		}
		
		if (list.size()>1) {
			throw new FeatureCalcException("More than one mark exists in the list");
		}
		
		Mark mark = list.getMemoForIndex(0).getMark();
		
		return new FeatureInputMark(
			mark,
			params.getResOptional()
		);	
	}
	
	@Override
	public boolean equals(Object other) {
		return (other instanceof CalculateDeriveMarkInput);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}
}
