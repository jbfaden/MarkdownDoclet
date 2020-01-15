
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
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    private static final File apdoc= new File("/home/jbf/tmp/autoplot/2020/20200115_apdoc/");
    
    
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
    
    private static String colloquialName( String name ) {
        switch (name) {
            case "org.das2.qds.QDataSet":
                return "QDataSet";
            case "java.lang.Object":
                return "Object";
            case "java.lang.Number":
                return "Number";
            default:
                return name;
        }
    }
    
    private static String seeAlsoLabel( String n ) {
        if ( n.startsWith("#") ) {
            n= n.substring(1);
        }
        n= n.replaceAll("org.das2.qds.QDataSet", "QDataSet");
        return n;
    }
    
    private static final Map<String,String> indicated= new HashMap<>();
    
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
        
        Map<String,String> grandIndex= new HashMap<>();
        Map<String,String> grandIndexFirst= new HashMap<>();
        
        for (ClassDoc classe : classes) {
            
            PrintStream out= null;
            try {
                String s = classe.qualifiedName();
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
                MethodDoc[] methods = classe.methods();                
                Arrays.sort( methods, (MethodDoc o1, MethodDoc o2) -> o1.name().compareTo(o2.name()) );
                int nmethod= methods.length;
                boolean byAlpha= false;
                char currentLetter= 'a';
                File f; // the current file to which we are writing
                if ( nmethod>200 ) {
                    byAlpha= true;
                    f= new File( apdoc.toString() + "/" + s + "_" + currentLetter + ".md" );
                } else {
                    f= new File( apdoc.toString() + "/" + s + ".md" );
                }
                File d= f.getParentFile();
                if ( !d.exists() ) {
                    if ( !d.mkdirs() ) throw new IllegalStateException("can't make dir: "+d);
                }
                out = new PrintStream(f);
                if ( !f.getParentFile().exists() ) {
                    if ( !f.getParentFile().mkdirs() ) throw new IllegalStateException("can't make dir");
                }
                for (int j = 0; j < Math.min( 20000, nmethod ); j++) {
                    MethodDoc m= methods[j];
                    
                    String name= m.name();
                    
                    if ( byAlpha ) {
                        if ( name.charAt(0)!=currentLetter ) {
                            out.close();
                            currentLetter= name.charAt(0);
                            f= new File( apdoc.toString() + "/" + s + "_"+ currentLetter + ".md" );
                            out = new PrintStream(f);
                        }
                    }
                    
                    StringBuilder sb= new StringBuilder();
                    StringBuilder ahrefBuilder= new StringBuilder();
                    sb.append(name).append("( ");
                    ahrefBuilder.append(name).append("(");
                    for ( int k=0; k<m.parameters().length; k++ ) {
                        if ( k>0 ) sb.append(", ");
                        if ( k>0 ) ahrefBuilder.append(",");
                        Parameter pk= m.parameters()[k];
                        sb.append(colloquialName(pk.type().toString())).append(" ").append(pk.name());
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
                    out.println("# "+name);
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
                    for (Tag seeTag : seeTags) {
                        SeeTag t = (SeeTag) seeTag;
                        int it= t.text().indexOf(')');
                        String l;
                        if ( it>-1 ) {
                            l = t.text().substring(0,it+1);
                        } else {
                            l = t.text();
                        }
                        if ( t.label()==null ) {
                            out.println("<a href='"+l+"'>" + seeAlsoLabel(l) +"</a><br>" );
                        } else {
                            out.println("<a href='"+l+"'>" + seeAlsoLabel(l) +"</a> "+t.label()+"<br>" );
                        }
                    }
                    out.println( String.format( "\n<a href=\"https://github.com/autoplot/dev/search?q=%s&unscoped_q=%s\">search for examples</a>", name, name ) );
                    grandIndex.put( name, s + "_"+ currentLetter + ".md#"+name );
                    String firstSentence= m.commentText();
                    grandIndexFirst.put( name, firstSentence.substring(0,Math.min(60,firstSentence.length()) ) );
                }
            }catch (FileNotFoundException ex) {
                Logger.getLogger(DocletTip.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                out.close();
            }
        }
        
        File grandIndexFile= new File( apdoc.toString() + "/index-all.md" );
        try ( PrintStream indexOut=new PrintStream(grandIndexFile) ) {
            List<String> keys= new ArrayList( grandIndex.keySet() );
            Collections.sort(keys);
            for ( String k: keys ) {
                indexOut.print("<a href=\""+grandIndex.get(k)+"\">");
                indexOut.print(k);
                indexOut.print("</a>");
                indexOut.print(grandIndexFirst.get(k));
                indexOut.println("<br>");
            }
        } catch ( IOException out ) {
            throw new IllegalStateException("could not write to "+grandIndexFile);
        }

        return true;
    }
} 