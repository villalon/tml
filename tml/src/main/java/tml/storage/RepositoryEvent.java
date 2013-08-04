/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tml.storage;

import java.util.EventObject;

/**
 * This class represents an event that was fired by a Repository
 * and indicates the current step of the running process and the
 * maximum number of steps.
 * It also includes a descriptive name of the operation being
 * executed.
 * 
 * @author Jorge Villalon
 */
public class RepositoryEvent extends EventObject {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4688981006009818932L;
	private String action = null;
    private int current = 0;
    private int maximum = 100;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getMaximum() {
        return maximum;
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }
    
    public RepositoryEvent(Object source, String action, int curr, int max) {
        super(source);
        this.action = action;
        this.current = curr;
        this.maximum = max;
    }

    @Override
    public String toString() {
        return "Action:" + this.action + " " + this.current + " of " + this.maximum;
    }
}
