/**
 * 
 */
package tml.vectorspace.operations.visualizations;

import tml.vectorspace.operations.Operation;

/**
 * @author Jorge
 *
 */
public abstract class AbstractVisualization implements Visualization {

	protected Operation<?> operation;
	
	@Override
	public Operation<?> getOperation() {
		return operation;
	}

	@Override
	public void setOperation(Operation<?> operation) {
		this.operation = operation;
	}

	@Override
	public abstract String getHTML();
}
