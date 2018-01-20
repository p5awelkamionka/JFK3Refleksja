package javad.attr;

import java.util.Vector;
import java.io.*;
import javad.util.*;
import javad.jconst.*;


/**

<p>
   This object represents the local variable table.  The local
   variable table is an attribute that may be in the attribute table
   of the code attribute (codeAttr object).  This attribute is
   generated by the Java compiler when debug is turned on (-g).
<p>
   The JVM Spec. states:

<blockquote>
     It may be used by debuggers to determine the value of a given
     local variable during the execution of a method. If
     LocalVariableTable attributes are present in the attributes table
     of a given Code attribute, then they may appear in any
     order. There may be no more than one LocalVariableTable attribute
     per local variable in the Code attribute. (JVM 4.7.9)
</blockquote>

<p>
   This is rather misleading.  The local variable table contains
   information about the local variable declarations.  There is one
   entry for each local variable (the table includes the class
   reference variable "this").
<p>
   The LocalVariableTable attribute has the format

<pre>
    LocalVariableTable_attribute {

        u2 attribute_name_index;
        u4 attribute_length;
        u2 local_variable_table_length;
        localVarEnt local_variable_table[local_variable_table_length];
    }
</pre>

<p>
   The attribute_name_index and attribute length are read by the
   attrFactory.allocAttr method.  These values are passed into the
   class constructor.
<p>
   The localVarEnt is

<pre>
    localVarEnt {  
        u2 start_pc;
	u2 length;
	u2 name_index;
	u2 descriptor_index;
	u2 index;
    }
</pre>

<p>
   There is one localVarEnt structure for each local variable.

<p>
   The JVM Spec (4.7.9) states:

<blockquote>
      The given local variable must have a value at indices into the
      code array in the interval [start_pc, start_pc+length], that is,
      between start_pc and start_pc+length inclusive. The value of
      start_pc must be a valid index into the code array of this Code
      attribute of the opcode of an instruction. The value of
      start_pc+length must be either a valid index into the code array
      of this Code attribute of the opcode of an instruction, or the
      first index beyond the end of that code array.
</blockquote>

<p>
   So the byte code from start_pc to start_pc+length seems to indicate
   the live range for the variable.  That is, the variable is assigned
   a value at the start of the range and may be assigned other values
   through out the range.  The range ends with the last reference to
   the variable.
<p>
   If an optimizing compiler generated the code and the variable is
   "dead" at a point in the source where a a debugger asks for its
   value or attempts to write the value then the debugger can report
   that the value is unavailable.
<p>
   The start_pc (the offset into the code array) is 16-bits.  However,
   the size of the code array is 32-bits.  In theory this means that
   there may be indices that can't be referenced.  In practice the
   size of Java objects seems to be limited (at least up through
   release 1.2).
<p>
   The name_index is an index into the constant table for the
   constUtf8 object for the variable name.
<p>
   The descriptor_index is an index into the constant table for the
   constUtf8 object for the variable descriptor that describes the
   object.
<p>
   The index field is the frame offset location for the local
   variable.
<p>
   The JVM Spec states for the index:

<blockquote>
     The given local variable must be at index in its method's local
     variables. If the local variable at index is a two-word type
     (double or long), it occupies both index and index+1.
</blockquote>

<p>
   By local variables, the JVM Spec is referring to the local frame.
   Note that frames are not necessarily allocated on the stack.

 */
class localVarTabAttr extends attrInfo {
  localVarEnt localVarTab[];

  /**

     Local variable entry in the local variable table.

   */
  class localVarEnt {
    int start_pc;
    int length;
    constUtf8 memberName = null;
    constUtf8 memberDesc = null;
    int index;

    localVarEnt( DataInputStream dStream, constPool constPoolSec ) {
      int name_index;
      int descriptor_index;
      constBase obj;

      start_pc = readU2( dStream );
      length = readU2( dStream );
      name_index = readU2( dStream );
      descriptor_index = readU2( dStream );
      index = readU2( dStream );

      if (name_index > 0) {
	obj = constPoolSec.constPoolElem( name_index );
	if (obj != null && obj instanceof constUtf8) {
	  memberName = (constUtf8)obj;
	}
      }

      if (descriptor_index > 0) {
	obj = constPoolSec.constPoolElem( descriptor_index );
	if (obj != null && obj instanceof constUtf8) {
	  memberDesc = (constUtf8)obj;
	}
      }
    } // localVarEnt class constructor


    /**
      Return a String for the local variable declaration or
      null if memberDesc or member name are null.

     */
    String getLocalVarDecl() {
      String localDecl = null;

      if (memberDesc != null && memberName != null) {
	String type;
	String name;
	
	type = typeDesc.decodeFieldDesc( memberDesc.getString() );
	name = memberName.getString();
	localDecl = type + " " + name + ";" + " // index = " + index;
      }

      return localDecl;
    } // getLocalVarDecl

    
  } // localVarEnt class


  localVarTabAttr( String name, int length, 
		   DataInputStream dStream, constPool constPoolSec ) {
    super( name, length );
    int numVarEnt = readU2( dStream );

    if (numVarEnt > 0) {
      localVarTab = new localVarEnt[ numVarEnt ];

      for (int i = 0; i < numVarEnt; i++) {
	localVarTab[i] = new localVarEnt( dStream, constPoolSec );
      }
    }
  }


  public Vector<String> getLocalVarVec() {
    Vector<String> localVarVec = null;
    String localDecl;

    if (localVarTab != null) {
      for (int i = 0; i < localVarTab.length; i++) {
	if (localVarVec == null)
	  localVarVec = new Vector<String>();
	
	localDecl = localVarTab[i].getLocalVarDecl();
	localVarVec.addElement( localDecl );
      }
    }
    return localVarVec;
  } // getLocalVarVec


} // localVarTabAttr
