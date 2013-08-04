/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tml.storage;

import java.util.EventListener;
/**
 * This interface defines the required methods to implement
 * a Repository listener, which will be called everytime
 * the Repository performs a step in its process.
 * 
 * @author Jorge Villalon
 */
public interface RepositoryListener extends EventListener {
    public void repositoryAction(RepositoryEvent evt);
}
