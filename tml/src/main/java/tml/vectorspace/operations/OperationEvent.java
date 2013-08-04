/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tml.vectorspace.operations;

import java.util.EventObject;

/**
 *
 * @author Jorge Villalon
 */
public class OperationEvent extends EventObject {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6181484640186835815L;
	private int maximum;
    private int current;

    public int getCurrent() {
        return current;
    }

    public int getMaximum() {
        return maximum;
    }

    public OperationEvent(Object source, int max, int curr) {
        super(source);
        this.current = curr;
        this.maximum = max;
    }
}
