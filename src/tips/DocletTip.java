
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
import java.util.HashMap;
import java.util.Map;
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
     * convert colloquial "QDataSet" to "org.das2.qds.QDataSet" and
     * similar.
     * @param type
     * @return 
     */
    private static String fullTypeName( String type ) {
        switch (type) {
            case "QDataSet":
                return "org.das2.qds.QDataSet";
            case "Object":
                return "java.lang.Object";
            default:
                return type;
        }
    }
    
    private static Map<String,String> indicated= new HashMap<>();
    
    /**
     * many routines have Objects as arguments which are converted
     * to QDataSet using standard code.  This was probably a mistake, but
     * we're stuck with it, and let's at least make the documentation efficient.
     * @param signature signature like "diff(java.lang.Object)"
     * @return null if not indicated, the text otherwise.
     */
    private static String haveIndicated( String signature ) {
        String key= signature.replaceAll( "java\\.lang\\.Object","org.das2.qds.QDataSet" );
        String haveIt= indicated.get(key);
        return haveIt;        
    }
    
    /**
     * 
     * @param root
     * @return 
     */
     public static boolean start(RootDoc root) {
        ClassDoc[] classes = root.classes();

        if ( !apdoc.exists() ) {
            if ( !apdoc.mkdirs() ) throw new IllegalStateException("can't make dir: "+apdoc);
        }
        
        for (int i = 0; i < classes.length; i++) {
            
            PrintStream out= null;
            try {
                String s= classes[i].qualifiedName();
                System.err.println("# "+s);
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
                }   
                File f= new File( apdoc.toString() + "/" + s + ".md" );
                File d= f.getParentFile();
                if ( !d.exists() ) {
                    if ( !d.mkdirs() ) throw new IllegalStateException("can't make dir: "+d);
                }
                out = new PrintStream(f);
                if ( !f.getParentFile().exists() ) {
                    if ( !f.getParentFile().mkdirs() ) throw new IllegalStateException("can't make dir");
                }
                
                MethodDoc methods[] = classes[i].methods();
                
                Arrays.sort( methods, (MethodDoc o1, MethodDoc o2) -> o1.name().compareTo(o2.name()) );
                
                int nmethod= methods.length;
                for (int j = 0; j < Math.min( 200, nmethod ); j++) {
                    MethodDoc m= methods[j];
                    StringBuilder sb= new StringBuilder();
                    StringBuilder ahrefBuilder= new StringBuilder();
                    sb.append(m.name()).append("( ");
                    ahrefBuilder.append(m.name()).append("(");
                    for ( int k=0; k<m.parameters().length; k++ ) {
                        if ( k>0 ) sb.append(", ");
                        if ( k>0 ) ahrefBuilder.append(",");
                        Parameter pk= m.parameters()[k];
                        sb.append(pk.type()).append(" ").append(pk.name());
                        ahrefBuilder.append( fullTypeName(pk.typeName()) );
                    }
                    ahrefBuilder.append(")");
                    // <a name='accum(org.das2.qds.QDataSet,org.das2.qds.QDataSet)'></a> // note not standard JavaDoc.
                    sb.append(" ) &rarr; ").append(m.returnType());
                    if ( haveIndicated(ahrefBuilder.toString())!=null ) {
                        out.println("<a name=\""+ahrefBuilder.toString()+"\"></a>");
                        continue;
                    } else {
                        indicated.put( ahrefBuilder.toString(), ahrefBuilder.toString() );
                    }
                    out.println("***");
                    out.println("<a name=\""+ahrefBuilder.toString()+"\"></a>");
                    out.println("# "+m.name());
                    out.println(sb.toString());
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
                    Tag[] tags= m.tags("return");
                    if ( tags.length>0 ) {
                        String s1= tags[0].text();
                        if ( s1.trim().length()>0 ) {
                            out.println( s1 );
                        } else {
                            out.println( m.returnType().toString() );
                        }
                    } else {
                        out.println( m.returnType().toString() );
                    }
                    
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
                            out.println("<a href='"+l+"'>" +l +"</a> "+t.label()+"<br>" );
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