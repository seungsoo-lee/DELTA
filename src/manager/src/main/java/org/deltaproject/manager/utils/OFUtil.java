package org.deltaproject.manager.utils;

import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.U32;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class OFUtil {
	/**
	 * Based on the list of OFVersions provided as input (or from Loxi), create
	 * a list of bitmaps for use in version negotiation during a cross-version
	 * OpenFlow handshake where both parties support OpenFlow versions >= 1.3.1.
	 * 
	 * Type Set is used as input to guarantee all unique versions.
	 * 
	 * @param ofVersions,
	 *            the list of bitmaps. Supply to an OFHello message.
	 * @return list of bitmaps for the versions of OpenFlow we support
	 */
	public static List<U32> computeOurVersionBitmaps(Set<OFVersion> ofVersions) {
		/* This should NEVER happen. Double-checking. */
		if (ofVersions == null || ofVersions.isEmpty()) {
			throw new IllegalStateException(
					"OpenFlow version list should never be null or empty at this point. Make sure it's set in the OFSwitchManager.");
		}

		int pos = 1; /* initial bitmap in list */
		int size = 32; /* size of a U32 */
		int tempBitmap = 0; /* maintain the current bitmap we're working on */
		List<U32> bitmaps = new ArrayList<U32>();
		ArrayList<OFVersion> sortedVersions = new ArrayList<OFVersion>(ofVersions);
		Collections.sort(sortedVersions);
		for (OFVersion v : sortedVersions) {
			/* Move on to another bitmap */
			if (v.getWireVersion() > pos * size - 1) {
				bitmaps.add(U32.ofRaw(tempBitmap));
				tempBitmap = 0;
				pos++;
			}
			tempBitmap = tempBitmap | (1 << (v.getWireVersion() % size));
		}
		if (tempBitmap != 0) {
			bitmaps.add(U32.ofRaw(tempBitmap));
		}
		return bitmaps;
	}
}
