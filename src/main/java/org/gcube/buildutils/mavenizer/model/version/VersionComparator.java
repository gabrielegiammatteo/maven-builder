package org.gcube.buildutils.mavenizer.model.version;

public class VersionComparator {

	public static boolean isVersionInRange(Range range, ArtifactVersion version){
		Restriction rangeRestriction= range.getRestrictions().get(0);
		if (rangeRestriction.getLowerBound()==null && rangeRestriction.getUpperBound()==null)				
		   return range.getRecommendedVersion().equals(version);
		else {
			if (rangeRestriction.getLowerBound()==null || rangeRestriction.getLowerBound().compareTo(version)<0 || 
					(rangeRestriction.isLowerBoundInclusive() && rangeRestriction.getLowerBound().compareTo(version)==0)){
				if (rangeRestriction.getUpperBound()==null || rangeRestriction.getUpperBound().compareTo(version) >0 || 
						(rangeRestriction.isUpperBoundInclusive() && rangeRestriction.getUpperBound().compareTo(version)==0))
					return true;
			}
			return false;
		}
		
	}
}