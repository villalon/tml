package tml.vectorspace.operations.visualizations;

import tml.vectorspace.operations.Operation;

public interface Visualization {
	public Operation<?> getOperation();

	public void setOperation(Operation<?> operation);
	
	public String getHTML();
}
