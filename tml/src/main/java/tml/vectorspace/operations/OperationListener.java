/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tml.vectorspace.operations;

import java.util.EventListener;

/**
 *
 * @author Jorge Villalon
 */
public interface OperationListener extends EventListener {
    void operationAction(OperationEvent evt);
}
