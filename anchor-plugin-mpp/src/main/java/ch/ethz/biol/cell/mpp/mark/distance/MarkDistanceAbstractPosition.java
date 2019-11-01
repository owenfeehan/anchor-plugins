package ch.ethz.biol.cell.mpp.mark.distance;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractPosition;
import org.anchoranalysis.anchor.mpp.mark.UnsupportedMarkTypeException;

public class MarkDistanceAbstractPosition extends MarkDistance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1919575942780482503L;

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkAbstractPosition;
	}

	@Override
	public double distance(Mark mark1, Mark mark2) throws UnsupportedMarkTypeException {
		
		if (!(mark1 instanceof MarkAbstractPosition)) {
			throw new UnsupportedMarkTypeException("mark1 is not MarkAbstractPosition");
		}
		
		if (!(mark2 instanceof MarkAbstractPosition)) {
			throw new UnsupportedMarkTypeException("mark2 is not MarkAbstractPosition");
		}
		
		MarkAbstractPosition mark1Cast = (MarkAbstractPosition) mark1;
		MarkAbstractPosition mark2Cast = (MarkAbstractPosition) mark2;
		return mark1Cast.getPos().distance( mark2Cast.getPos() );
	}
}
	