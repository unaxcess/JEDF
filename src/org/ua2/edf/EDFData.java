package org.ua2.edf;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author brian
 *
 */
public class EDFData {
    public enum ValueType { STRING, INTEGER, NONE };
    
    public String name;
    public String sValue;
    public Integer iValue;
    public ValueType type;
    public ArrayList<EDFData> children;
    
    private static String PRETTY_EOL = "\r\n";
    
    private int childcounter = 0;

    /**
     * Constructs an EDF object with a name, no value and no children
     * @param name  Name of the object
     */
    public EDFData(String name)
    {
        this.name = name;
        type = ValueType.NONE;
        children = new ArrayList<EDFData>();
    }

    /**
     * Contructs a new EDF data structure, with no children
     * @param name  Name of the EDF object
     * @param value Value of the EDF object
     */
    public EDFData(String name, String value)
    {
        this(name);
        sValue = value;
        type = ValueType.STRING;
    }

    /**
     * Contructs a new EDF data structure, with no children
     * @param name  Name of the EDF object
     * @param value Value of the EDF object
     */
    public EDFData(String name, Integer value)
    {
        this(name);
        iValue = value;
        type = ValueType.INTEGER;
    }


    // Access methods
    
    public String getName() {
        return name;
    }
    
    public Object getValue() {
        if(type.equals(ValueType.STRING)) {
            return sValue;
        } else if(type.equals(ValueType.INTEGER)) {
            return iValue;
        }
        
        return null;
    }
    
    public String getString() {
        if(!type.equals(ValueType.STRING)) {
            throw new EDFTypeException("Expecting " + ValueType.STRING + " type value, found " + type);
        }
        
        return sValue;
    }
    
    public Integer getInteger() {
        if(!type.equals(ValueType.INTEGER)) {
            throw new EDFTypeException("Expecting " + ValueType.INTEGER + " type value, found " + type);
        }
        
        return iValue;
    }
    
    /**
     * Get the number of children in the EDF data structure
     * @return  Number of children
     */
    public int getChildCount()
    {
        return children.size();
    }
    
    /**
     * Reset the iterating child pointer to the first child
     */
    public void resetChild()
    {
        childcounter = 0;
    }
    
    /**
     * Get the next child in the EDF structure
     * @return  EDF data for child
     */
    public EDFData getNext()
    {
        if(childcounter >= children.size())
        {
            return null;
        }
        
        EDFData data = children.get(childcounter);
        childcounter++;

        return data;
    }
    
    /**
     * Append an EDF object to the children of this object
     * @param child EDF object to add
     */
    public void add(EDFData child)
    {
        children.add(child);
    }
    
    /**
     * Gets the first match of a child element
     * @param childname Name of the element
     * @return EDFData object representing the child (and it's children, if any), nor null on no match
     */
    public EDFData getChild(String childname)
    {
        if(getChildCount()> 0)
        {
            for(EDFData child : children)
            {
                if(child.name.equals(childname))
                {
                    return child;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Convenience method for returning a list of childen of the specified type
     * @param name
     * @return
     */
    public List<EDFData> getChildren(String name) {
       List<EDFData> childList = new ArrayList<EDFData>();
       
       for(EDFData child : children) {
           if(child.getName().equals(name)) {
               childList.add(child);
           }
       }
       
       return childList;
    }
    
    /**
     * Return the EDF object, including children, as a string suitable for passing to the UA server
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return format(false);
    }
        
    /**
     * Print the EDF to the standard output stream, for debugging
     */
    public void print()
    {
        System.out.println(format(true));
    }
    
    /**
     * Unescape string values sent over the wire, turning \" into " and
     * \\ into \
     * @param in    Escaped string
     * @return  Unescaped string
     */
    public static String unescape(String in)
    {
        String s;
        
        s = in.replaceAll("\\\\\"", "\"");      // Replace \" with "
        return s.replaceAll("\\\\\\\\", "\\\\");        // Replace \\ with \
    }
    
    /**
     * Escape string values sent over the wire, turning " into \" and
     * \ into \\
     * @param in    Unescaped string
     * @return  Escaped string suitable for sending to server
     */
    public static String escape(String in)
    {
        String s;
        
        s = in.replaceAll("\\\\", "\\\\\\\\");      // Replace \ with \\
        return s.replaceAll("\"", "\\\\\"");        // Replace " with \"
    }
    
    public String format(boolean pretty) {
        StringBuffer data = new StringBuffer();
        format(data, pretty, "");
        return data.toString();
    }
    
    protected void format(StringBuffer data, boolean pretty, String indent) {
        data.append((pretty ? indent : "") + "<" + name);
        
        switch(type)
        {
            case STRING: data.append("=\"" + escape(sValue) + "\"");     break;
            case INTEGER: data.append("=" + iValue.toString());  break;
            case NONE: /* Nothing to do */
        }
        
        if(getChildCount()> 0)
        {
            data.append(">");
            if(pretty) {
                data.append(PRETTY_EOL);
            }
            
            String childIndent = indent + "  ";
            for(EDFData child : children)
            {
                child.format(data, pretty, childIndent);
            }
            
            if(pretty) {
                data.append(indent + "</" + name + ">");
            } else {
                data.append("</>");
            }
        } else {
            data.append("/>");
        }
        
        if(pretty) {
            data.append(PRETTY_EOL);
        }
    }
}
