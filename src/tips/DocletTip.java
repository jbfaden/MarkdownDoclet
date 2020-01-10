
package tips;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * From https://www.zdnet.com/article/customize-javadoc-output-with-doclets/
 * To re-run the source for http://autoplot.org/wiki/index.php?title=developer.scripting&action=edit&section=68
 * you would: javadoc -docletpath /home/jbf/eg/java/javadoclet/DocletTip/dist/DocletTip.jar -doclet tips.DocletTip /home/jbf/project/autoplot/autoplot-code/QDataSet/src/org/das2/qds/ops/Ops.java

 * @author jbf
 */
public class DocletTip {

    private static final File apdoc= new File("/tmp/apdoc/");
    
    /**
     * 
     * @param root
     * @return 
     */
     public static boolean start(RootDoc root) {
        ClassDoc[] classes = root.classes();

        if ( !apdoc.exists() ) {
            if ( !apdoc.mkdirs() ) throw new IllegalStateException("can't make dir "+apdoc);
        }
        
        for (int i = 0; i < classes.length; i++) {
            
            PrintStream out= null;
            try {
                String s= classes[i].qualifiedName();
                int is= 0;
                for ( int j=0; j<s.length(); j++ ) {
                    char c= s.charAt(j);
                    if ( c>='A' && c<='Z' ) {
                        is= j;
                        break;
                    }
                }
                if ( is<s.length() ) {
                    s= s.substring(0,is).replaceAll("\\.","/") + s.substring(is);
                } else {
                    throw new IllegalStateException("didn't find upper case letter");
                }   File f= new File( apdoc.toString() + "/" + s + ".md" );
                out = new PrintStream(f);
                if ( !f.getParentFile().exists() ) {
                    if ( !f.getParentFile().mkdirs() ) throw new IllegalStateException("can't make dir");
                }
                
                MethodDoc methods[] = classes[i].methods();
                
                Arrays.sort( methods, (MethodDoc o1, MethodDoc o2) -> o1.name().compareTo(o2.name()) );
                
                int nmethod= methods.length;
                for (int j = 0; j < Math.min( 100, nmethod ); j++) {
                    MethodDoc m= methods[j];
                    out.println("# "+m.name()+"\n");
                    out.print( m.name() + "( " );
                    for ( int k=0; k<m.parameters().length; k++ ) {
                        if ( k>0 ) out.print(", ");
                        Parameter pk= m.parameters()[k];
                        out.print( pk.type() + " " + pk.name() );
                    }
                    out.println(" ) &rarr; " + m.returnType() );
                    out.println("");
                    out.println(m.commentText());
                    out.println("");
                    out.println("### Parameters:" );
                    for ( int k=0; k<m.paramTags().length; k++ ) {
                        ParamTag pt= m.paramTags()[k];
                        if ( k>0 ) out.print("<br>");
                        out.println(""+pt.parameterName() + " - " + pt.parameterComment() );
                    }
                    out.println("");
                    out.println("### Returns:" );
                    out.println(""+m.returnType().toString() + " ???<COMMENT>???" );
                    
                    out.println("");
                    Tag[] seeTags= m.tags("see");
                    if ( seeTags.length>0 ) {
                        out.println("### See Also:");
                    }
                    for ( int k=0; k<seeTags.length; k++ ) {
                        SeeTag t= (SeeTag)seeTags[k];
                        int it= t.text().indexOf(')');
                        String l;
                        if ( it>-1 ) {
                            l = t.text().substring(0,it+1);
                        } else {
                            l = t.text();
                        }
                        if ( t.label()==null ) {
                            out.println("<a href='"+l+"'>" +l +"</a><br>" );
                        } else {
                            out.println("<a href='"+l+"'>" +l +"</a>"+t.label()+"<br>" );
                        }
                    }
                    out.println("");
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DocletTip.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                out.close();
            }
        }

        return true;
    }
} 