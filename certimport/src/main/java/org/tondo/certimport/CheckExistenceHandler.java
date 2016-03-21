package org.tondo.certimport;

import org.tondo.certimport.handlers.CertStoreResult;
import org.tondo.certimport.handlers.CertStoreResult.CertificateEntry;

public class CheckExistenceHandler implements ResultHandler {

	@Override
	public void printResultInfo(CertStoreResult result) {
		CertificateEntry  matchingCertificate = result.getMatchingCertificate();
		if (matchingCertificate != null) {
			System.out.println("Trusted: true");
			System.out.println("Alias: " + matchingCertificate.getAlias());
		} else {
			System.out.println("Trusted: false");
		}
	}
}
