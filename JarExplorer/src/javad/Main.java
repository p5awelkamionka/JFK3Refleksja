package javad;

//
// local utility directory, not to be confused with
// java.lang.javad.util.*
//
import javad.util.*;


/** 

The <b>javad</b> class contains the <i>main</i> for the <i>javad</i>
program.  The <i>javad</i> program reads a Java class file and
writes out a Java like source representation of the class that
generated the file. 

*/
public class Main {

  static void usage() {
    errorMessage.errorPrint(" this program takes a set of one or more" +
			    " .class file names as its argument");
  }

  public static void main(String[] args ) {

    errorMessage.setProgName( "javad" );

    if (args.length == 0) {
      usage();
    }
    else {
      for (int i = 0; i < args.length; i++) {
	  	new jvmDump( args[i] );
      }
    }
  }
} // main
