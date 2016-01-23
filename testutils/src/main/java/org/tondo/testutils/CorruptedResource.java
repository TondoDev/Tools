package org.tondo.testutils;


/**
 * Mock class for demonstration of situation when AutoCloseable resource can
 * fail in closing and also in logic.
 * @author TondoDev
 *
 */
public class CorruptedResource implements AutoCloseable {
	private boolean badLogic;
	private boolean badClose;
	private boolean badCreate;
	private String id;
	
	/**
	 * Constructor
	 * @param id
	 * 		identifier of resource for better identification when more resources will be used
	 * @param corruptedCreate
	 * 		flag if resource will fail during creation
	 * @param corruptedLogic
	 * 		flag if resource will fail during logic execution
	 * @param corruptedClose
	 * 		flag if resource will fail during close operation
	 */
	public CorruptedResource(String id, boolean corruptedCreate, boolean corruptedLogic, boolean corruptedClose) {
		this.badCreate = corruptedCreate;
		this.badClose = corruptedClose;
		this.badLogic = corruptedLogic;
		this.id = id;
		
		if (this.badCreate) {
			throw new IllegalStateException("[" +id +"]: create failed!");
		}
	}

	@Override
	public void close() throws Exception {
		if (badClose) {
			throw new IllegalStateException("[" +id +"]: close failed!");
		}
		
		System.out.println("[" +id +"]: close successful!");
	}
	
	public void doSomething() {
		if (badLogic) {
			throw new IllegalStateException("[" +id +"]: logic failed!");
		}
		
		System.out.println("[" +id +"]: logic successful!");
	}

}
