
package tips;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

/**
 * From https://www.zdnet.com/article/customize-javadoc-output-with-doclets/
 * To re-run the source for http://autoplot.org/wiki/index.php?title=developer.scripting&action=edit&section=68
 * you would: javadoc -docletpath /home/jbf/eg/java/javadoclet/DocletTip/dist/DocletTip.jar -doclet tips.DocletTip /home/jbf/project/autoplot/autoplot-code/QDataSet/src/org/das2/qds/ops/Ops.java

 * @author jbf
 */
public class DocletTip {

    /**
     * 
     * @param root
     * @return 
     */
     public static boolean start(RootDoc root) {
        ClassDoc[] classes = root.classes();

        for (int i = 0; i < classes.length; i++) {
            System.out.println( "=== " + classes[i] + " === (" + i + ")");
            
            MethodDoc methods[] = classes[i].methods();

            int nmethod= methods.length;
            
            for (int j = 0; j < nmethod; j++) {
                MethodDoc m= methods[j];
                if ( !m.name().endsWith("eventsConjunction") ) {
                    continue;
                }
                System.out.println("=="+m.name()+"==");
                System.out.print( m.name() + "( " );
                for ( int k=0; k<m.parameters().length; k++ ) {
                    if ( k>0 ) System.out.print(", ");
                    Parameter pk= m.parameters()[k];
                    System.out.print( pk.type() + " " + pk.name() );
                }
                System.out.println(" )");
                System.out.println("");
                System.out.println(m.commentText());
                System.out.println("");
                System.out.println("Parameters: " );
                for ( int k=0; k<m.paramTags().length; k++ ) {
                    ParamTag pt= m.paramTags()[k];
                    System.out.println("* "+pt.parameterName() + " - " + pt.parameterComment() );
                }
                Tag[] seeTags= m.tags("see");
                if ( seeTags.length>0 ) {
                    System.out.println("");
                }
                for ( int k=0; k<seeTags.length; k++ ) {
                    Tag t= seeTags[k];
                    System.out.println("See  [" +t.text()+"]" );
                }
                System.out.println("");
            }
        }

        return true;
    }
} 