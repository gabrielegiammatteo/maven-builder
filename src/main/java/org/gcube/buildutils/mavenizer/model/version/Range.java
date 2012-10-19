package org.gcube.buildutils.mavenizer.model.version;

import java.util.Iterator;
import java.util.List;

public class Range {
	
	private final ArtifactVersion recommendedVersion;

	private final List<Restriction> restrictions;
	
	Range(ArtifactVersion recommendedVersion, List<Restriction> restrictions) {
		this.recommendedVersion = recommendedVersion;
		this.restrictions = restrictions;
	}

	public ArtifactVersion getRecommendedVersion() {
		return recommendedVersion;
	}

	public List<Restriction> getRestrictions() {
		return restrictions;
	}
	
	public String toString() {
		if (recommendedVersion != null) {
			return recommendedVersion.toString();
		} else {
			StringBuffer buf = new StringBuffer();
			for (Iterator<Restriction> i = restrictions.iterator(); i.hasNext();) {
				Restriction r = (Restriction) i.next();

				buf.append(r.toString());

				if (i.hasNext()) {
					buf.append(",");
				}
			}
			return buf.toString();
		}
	}


}