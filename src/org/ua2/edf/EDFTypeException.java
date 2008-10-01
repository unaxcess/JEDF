package org.ua2.edf;

/**
 * Used by get methods when you try to get the wrong sort of value
 * 
 * @author techno
 *
 */
public class EDFTypeException extends RuntimeException {
    public EDFTypeException(String msg) {
        super(msg);
    }
}
