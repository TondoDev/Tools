package org.tondo.certimport.handlers;

import org.tondo.certimport.CertStoringOption;

public class StoringConfiguration {

	public static class ConfBuilder {
		private String alias = null;
		private boolean addEvenIfTrustedFlag = false;
		private CertStoringOption option;
		
		
		public ConfBuilder setAlias(String alias) {
			this.alias = alias;
			return this;
		}
		
		public ConfBuilder setAddEvenIfTrusted(boolean flag) {
			this.addEvenIfTrustedFlag = flag;
			return this;
		}
		
		public ConfBuilder setOption(CertStoringOption option) {
			this.option = option;
			return this;
		}
		
		
		public StoringConfiguration create() {
			validate();
			StoringConfiguration sc = new StoringConfiguration();
			sc.setAlias(alias);
			sc.setAddEvenIfTrusted(addEvenIfTrustedFlag);
			sc.setOption(option);
			return sc;
		}
		
		private void validate() {
			if (option != CertStoringOption.DONT_ADD && alias == null) {
				throw new IllegalArgumentException("So far till alias handler not exists, is alias field mandatory");
			}
			
			if (option == null || option == CertStoringOption.CHAIN) {
				throw new IllegalArgumentException("CHAIN or null option is not supported so far!");
			}
		}
	}
	
	public static ConfBuilder builder () {
		return new ConfBuilder();
	}
	
	
	private CertStoringOption option;
	private String alias;
	private boolean addEvenIfTrusted = false;
	
	public String getAlias() {
		return alias;
	}
	
	private void setAlias(String alias) {
		this.alias = alias;
	}
	
	public boolean isAddEvenIfTrusted() {
		return addEvenIfTrusted;
	}
	
	private void setAddEvenIfTrusted(boolean flag) {
		this.addEvenIfTrusted = flag;
	}
	
	public CertStoringOption getOption() {
		return option;
	}
	
	private void setOption(CertStoringOption option) {
		this.option = option;
	}
	
}
