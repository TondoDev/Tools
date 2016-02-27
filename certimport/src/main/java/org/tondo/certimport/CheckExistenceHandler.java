package org.tondo.certimport;

import org.tondo.certimport.handlers.CertStoreResult;

public class CheckExistenceHandler implements ResultHandler {

	@Override
	public void printResultInfo(CertStoreResult result) {
		String matchingAlias = result.getMatchingAlias();
		System.out.println("Trusted: " + (matchingAlias != null));
		System.out.println("Alias: " + (matchingAlias != null ? matchingAlias : ""));
	}
}
