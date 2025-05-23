
package tips;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Doclet which creates both simplified HTML version and MarkDown version.
 * From https://www.zdnet.com/article/customize-javadoc-output-with-doclets/
 * To re-run the source for http://autoplot.org/wiki/index.php?title=developer.scripting&amp;action=edit&amp;section=68
 * you would: javadoc -docletpath /home/jbf/eg/java/javadoclet/DocletTip/dist/DocletTip.jar -doclet tips.DocletTip /home/jbf/project/autoplot/autoplot-code/QDataSet/src/org/das2/qds/ops/Ops.java
 *
 * The environment variables can be set:
 * export mddoc=/tmp/mddoc
 * export htmldoc=/tmp/htmldoc

 * @author jbf
 */
public class DocletTip {

    private static Logger logger= Logger.getLogger("doclettip");
    
    private static File mddoc= new File("/");
    private static File htmldoc= new File("/");
    
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
            case "ProgressMonitor":
                return "org.das2.util.monitor.ProgressMonitor";
            case "Units":
                return "org.das2.datum.Units";
            case "Datum":
                return "org.das2.datum.Datum";
            case "DatumRange":
                return "org.das2.datum.DatumRange";
            case "Object":
                return "java.lang.Object";
            case "String":
                return "java.lang.String";
            case "Number":
                return "java.lang.Number";
            case "Double":
                return "java.lang.Double";
            default:
                return type;
        }
    }
    
    private static String colloquialName( String name ) {
        String suffix="";
        int i= name.indexOf("[");
        if ( i>-1 ) {
            suffix= name.substring(i);
            name= name.substring(0,i);
        }
        switch (name) {
            case "org.das2.qds.QDataSet":
                return "QDataSet"+suffix;
            case "org.das2.util.monitor.ProgressMonitor":
                return "ProgressMonitor"+suffix;
            case "org.das2.datum.Units":
                return "Units"+suffix;
            case "org.das2.datum.Datum":
                return "Datum"+suffix;
            case "org.das2.datum.DatumRange":
                return "DatumRange"+suffix;
            case "java.lang.Object":
                return "Object"+suffix;
            case "java.lang.String":
                return "String"+suffix;
            case "java.lang.Number":
                return "Number"+suffix;
            case "java.lang.Double":
                return "Double"+suffix;
            case "PyFunction":
                return "org.python.core.PyFunction"; // Kludge--don't know why...
            default:
                return name+suffix;
        }
    }
    
    private static String seeAlsoLabel( String n ) {
        if ( n.startsWith("#") ) {
            n= n.substring(1);
        }
        n= n.replaceAll("org.das2.qds.QDataSet", "QDataSet");
        int ispace= n.indexOf(" ");
        if ( ispace>-1 && !n.endsWith(")")) {
            n= n.substring(0,ispace);
        }
        return n;
    }
    
    /**
     * ensure no characters are found which would mess up markdown.
     * @param s
     * @return 
     */
    private static String markDownSafeSummary( String s ) {
        if ( s.length()==0 ) return s;
        if ( s.length()>120 ) s= s.substring(0,120);
        int i= s.indexOf(". ");
        if ( i>-1 ) s= s.substring(0,i+1);
        if ( !s.endsWith(".") && s.length()==120 ) {
            i= s.lastIndexOf(" ");
            if ( i>-1 ) s= s.substring(0,i) + "...";
        }
        
        s= s.replaceAll("\\n"," ");
        s= s.replaceAll("\\*","&ast;");
        Pattern p= Pattern.compile("[^0-9a-zA-Z,\\.\\-\\[\\]\\(\\)\\:\\+\\\" _&;*/]");
        Matcher m= p.matcher(s);
        if ( m.find() ) {
            s=  s.substring(0,m.start());
            return s + "...";
        } else {
            return s;
        }
        
    }
    
    /**
     * return null or a URL for source.
     * @param s the class name
     * @param linenum the line number of the documentation.
     * @return 
     * //TODO: I have a nice Map class somewhere that does hierarchical lookup on IP, which could be used here. 
     */
    private static String findLinkForSource( String s, int linenum ) {
        int i= s.lastIndexOf("/");
        String sline="#l"+linenum;
        String sLine="#L"+linenum;
        if ( s.startsWith("org/autoplot/datasource") ) {
            String path= "https://sourceforge.net/p/autoplot/code/HEAD/tree/autoplot/trunk/DataSource/src/";
            return path + s + ".java" + sline;
        } else if ( s.startsWith("org/autoplot/jythonsupport") ) {
            String path= "https://sourceforge.net/p/autoplot/code/HEAD/tree/autoplot/trunk/JythonSupport/src/";
            return path + s + ".java"+ sline;
        } else if ( s.startsWith("org/autoplot/dom") ) {
            String path= "https://sourceforge.net/p/autoplot/code/HEAD/tree/autoplot/trunk/Autoplot/src/";
            return path + s + ".java"+ sline;
        } else if ( s.substring(0,i).equals("org/autoplot") ) {
            String path= "https://sourceforge.net/p/autoplot/code/HEAD/tree/autoplot/trunk/Autoplot/src/";
            return path + s + ".java"+ sline;
            
        } else if ( s.startsWith("org/das2/util") ) {
            String path= "https://github.com/das-developers/das2java/tree/main/dasCoreUtil/src/";
            return path + s + ".java"+ sLine;
        } else if ( s.startsWith("org/das2/datum") ) {
            String path= "https://github.com/das-developers/das2java/tree/main/dasCoreDatum/src/";
            return path + s + ".java"+ sLine;
        } else if ( s.startsWith("org/das2/qds") ) {
            String path= "https://github.com/das-developers/das2java/tree/main/QDataSet/src/";
            return path + s + ".java"+ sLine;
        } else if ( s.startsWith("org/das2") ) {
            String path= "https://github.com/das-developers/das2java/tree/main/dasCore/src/";
            return path + s + ".java"+ sLine;
        } else {
            return null;
        }
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
     * introduced to handle {&at;code } blocks.
     * @param commentText
     * @return 
     */
    private static String handleText(String commentText) {
        Pattern p= Pattern.compile("(.*)\\{\\@code(.*)\\}(\\s*)\\</pre>(.*)");
        int i= commentText.indexOf("{@code");
        while ( i!=-1 ) {
            StringBuilder b;
            b= new StringBuilder(commentText.substring(0,i));
            int i2= commentText.indexOf("}",i);
            if ( i2==-1 ) {
                i2= commentText.length();
            }
            try {
                b.append(commentText.substring(i+6,i2));
                if ( i2<commentText.length() ) b.append(commentText.substring(i2+1));
            } catch ( StringIndexOutOfBoundsException ex ) {
                b.append(commentText.substring(i+6,i2));
                if ( i2<commentText.length() ) b.append(commentText.substring(i2+1));
            }
            commentText= b.toString();
            i= commentText.indexOf("{@code");
        } 
        return commentText;
    }
    
    private static void signature( 
            StringBuilder sb, 
            StringBuilder ahrefBuilder, 
            StringBuilder signature,
            ExecutableMemberDoc m ) {
        
        String name= m.name();
        
        sb.append(name).append("( ");
        ahrefBuilder.append(name); //.append("(");
        signature.append(name).append("(");
        for ( int k=0; k<m.parameters().length; k++ ) {
            if ( k>0 ) sb.append(", ");
            ahrefBuilder.append("-");
            if ( k>0 ) signature.append(",");
            Parameter pk= m.parameters()[k];
            
            String sarg= colloquialName(pk.type().toString());
            String link= getHtmlLinkFor(pk.type().qualifiedTypeName());
            
            if ( link!=null ) {
                sb.append("<a href='").append(link).append("'>").append( sarg ).append("</a>").append(" ").append(pk.name());
            } else {
                sb.append(sarg).append(" ").append(pk.name());
            }
            String stype= pk.type().toString();
            if ( stype.contains(".") ) {
                ahrefBuilder.append( pk.type().toString() );
            } else if ( stype.equals("PyFunction") ) {
                ahrefBuilder.append( "org.python.core.PyFunction" ); //TODO: why is this different?  Will change with Jython3
            } else {
                ahrefBuilder.append( pk.type().toString() );
            }
            signature.append(pk.name());
        }                    
        
        signature.append(")");
        
        if ( m instanceof MethodDoc ) {
            MethodDoc md= (MethodDoc)m;
            String sreturn;
            String link= getHtmlLinkFor(md.returnType().qualifiedTypeName());
            sreturn= colloquialName(md.returnType().toString() );
            if ( link!=null ) {
                sb.append(" ) &rarr; <a href='").append(link).append("'>").append( sreturn ).append("</a>");
            } else {
                sb.append(" ) &rarr; ").append( sreturn );
            }
        } else {
            if ( m.parameters().length>0 ) {
                sb.append(" )");
            } else {
                sb.append(")");
            }
            
        }
                    
    }
    
    /**
     * return true if the method should be in the grand index.
     * @param classFullName class name, like org.das2.qds.ops.Ops
     * @param methodName name of the method, like DataSetBuilder
     * @return true if the method should be in the grand index.
     */
    public static boolean includeGrandIndex( String classFullName, String methodName ) {
//from org.das2.qds.ops.Ops import *
//from org.autoplot.jythonsupport.JythonOps import *
//from org.autoplot.jythonsupport.Util import *
//from org.das2.qds import QDataSet
//from org.das2.qds.util.BinAverage import *
//from org.das2.qds.util import DataSetBuilder
//
//_autoplot_jython_version= 2.00
//#_autoplot_jython_version= float(getAutoplotScriptingVersion()[1:])
//
//from org.das2.datum import DatumRange, Units, DatumRangeUtil, TimeUtil
//from java.net import URL, URI
//from org.das2.datum import TimeParser
//
//# security concerns
//#from java.io import File
//#from org.das2.util.filesystem import FileSystem
//#from org.das2.fsm import FileStorageModel
//from org.autoplot.datasource.DataSetURI import getFile
//from org.autoplot.datasource.DataSetURI import downloadResourceAsTempFile
//#import java
//#import org
//# end, security concerns.
//
//# jython is tricky with single-jar releases, and using star imports to find classes doesn't work.
//#import org.das2
//#import org.das2.dataset
//#import org.das2.dataset.NoDataInIntervalException
//#import org.das2.graph        
        
        if ( classFullName.startsWith("org.das2.qds") ) {
            String rest= classFullName.substring(12);
            switch (rest) {
                case ".ops.Ops":
                    return true;
                case ".util.BinAverage":
                    return true;
                case ".DataSetBuilder":
                    return methodName.equals("DataSetBuilder");
                default:
                    return false;
            }
        } else if ( classFullName.startsWith( "org.autoplot" ) ) {
            if ( classFullName.startsWith( "org.autoplot.jythonsupport") ) {
                String rest= classFullName.substring(26);
                switch (rest) {
                    case ".JythonOps":
                        return true;
                    case ".Util":
                        return true;
                    default:
                        return false;
                }
            } else if ( classFullName.startsWith( "org.autoplot.ScriptContext") ) {
                return true;
            } else if ( classFullName.equals( "org.autoplot.simpleproject.Util") ) {
                return true;
            } else {
                return false;
            }
            
        } else if ( classFullName.startsWith( "org.das2.datum" ) ) {
            String rest= classFullName.substring(14);
            switch (rest) {
                case ".TimeParser":
                    return false;
                case ".Datum":
                    return false;
                case ".DatumRangeUtil":
                    return false;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }
    
    /**
     * Create a link to the known location of the JavaDoc.  This should
     * be externally configurable, but presently it is hard-coded here.
     * "org.das2.qds.examples.Schemes#boundingBox"
     * "http://www-pw.physics.uiowa.edu/~jbf/autoplot/doc/org/das2/qds/examples/Schemes.html#boundingBox"
     * 
     * "Schemes#boundingBox"
     * "Schemes.html#boundingBox"
     * @param clas the class name, like org.das2.qds.examples.Schemes
     * @return the html code containing the link.
     */
    public static String getHtmlLinkFor( String clas ) {
        if ( clas.startsWith("#") ) {
            return clas;
        }
        if ( clas.startsWith("http") ) {
            return clas;
        }
        if ( clas.equals("void") ) {
            return null;
        }
        if ( !clas.contains(".") ) { // int, double, ...
            return null;
        }
        String postHash= null;
        int ihash= clas.indexOf("#");
        if ( ihash>0 ) {
            postHash= clas.substring(ihash);
            clas= clas.substring(0,ihash);
        } else if ( ihash==0 ) {
            return clas;
        }
        String[] ss= clas.split("\\.",-2);
        if ( ss.length==1 ) {
            return ss[0]+".html" + ( postHash!=null ? postHash : "" );
        } else if ( clas.startsWith("java.") ) {
            String base= "https://docs.oracle.com/javase/8/docs/api/";
            return base + String.join( "/", ss ) + ".html" + ( postHash!=null ? postHash : "" );
        } else {
            String base= "https://cottagesystems.com/~jbf/autoplot/doc/";
            return base + String.join( "/", ss ) + ".html" + ( postHash!=null ? postHash : "" );
        }
    }
    
    /**
     * Create a link to the known location of the MarkDown version of the
     * JavaDoc.  This should be externally configurable, but presently it is 
     * hard-coded here.
     * "org.das2.qds.examples.Schemes#boundingBox"
     * "hhttps://cottagesystems.com/~jbf/autoplot.doc/org/das2/qds/examples/Schemes.md#boundingBox"
     * 
     * "Schemes#boundingBox"
     * "Schemes.html#boundingBox"
     * @param clas the class name, like org.das2.qds.examples.Schemes
     * @return the html code containing the link.
     */    
    public static String getMDLinkFor( String clas ) {
        if ( clas.startsWith("http") ) {
            return clas;
        }
        String postHash= null;
        int ihash= clas.indexOf("#");
        if ( ihash>0 ) {
            postHash= clas.substring(ihash);
            clas= clas.substring(0,ihash);
        } else if ( ihash==0 ) {
            return clas;
        }
        String[] ss= clas.split("\\.",-2);
        if ( ss.length==1 ) {
            return ss[0]+".md" + ( postHash!=null ? postHash : "" );
        } else {
            String base= "https://git.uiowa.edu/jbf/autoplot/-/blob/master/doc/";
            return base + String.join( "/", ss ) + ".md" + ( postHash!=null ? postHash : "" );
        }
    }
    
    Map<String,String> grandIndex= new HashMap<>();
    Map<String,String> grandIndexFirstLine= new HashMap<>();
    Map<String,String> grandIndexClass= new HashMap<>();
    Map<String,String> grandIndexSignature= new HashMap<>();
    
    /**
     * the current class being processed.
     */
    ClassDoc classDoc= null;
    
    public boolean doStart( RootDoc root ) {
            
        ClassDoc[] classes = root.classes();

        if ( !mddoc.exists() ) {
            if ( !mddoc.mkdirs() ) throw new IllegalStateException("can't make dir: "+mddoc);
        }

        if ( !htmldoc.exists() ) {
            if ( !htmldoc.mkdirs() ) throw new IllegalStateException("can't make dir: "+htmldoc);
        }
        
        //boolean seePlotElement= false;
        
        for (ClassDoc classe : classes) {
            
            classDoc = classe;
            
            String fullName= classe.qualifiedName();
            
            if ( fullName.startsWith("org.autoplot") || fullName.startsWith("org.das2") ) {
                // do nothing.
            } else {
                continue;
            }

            //if ( fullName.startsWith("org.autoplot.jythonsupport.Util" ) ) {
            //    System.err.println("found class: "+fullName);
            //} else {
            //    continue;
            //}
            
            boolean toPrint=false;
            
            PrintStream mdout= null;
            PrintStream htmlout= null;
            try {
                String s = classe.qualifiedName();
                
                //if ( s.contains("AsciiParser") ) {
                //    toPrint=true;
                //}
                                    
                if ( toPrint ) System.err.println("class: "+s);
                
                if ( s.endsWith(".HtmlUtil") ) {
                    //seePlotElement= true;
                    System.err.println("here at 481");
                }
                    
                int is= 0;
                for ( int j=0; j<s.length(); j++ ) {
                    char c= s.charAt(j);
                    if ( c>='A' && c<='Z' ) {
                        is= j;
                        break;
                    }
                }
                String classNameNoPackage;
                if ( is<s.length() ) {
                    s= s.substring(0,is).replaceAll("\\.","/") + s.substring(is);
                    classNameNoPackage= s.substring(is);
                } else {
                    throw new IllegalStateException("didn't find upper case letter");
                }
                MethodDoc[] methods = classe.methods();   
                
                Arrays.sort( methods, (MethodDoc o1, MethodDoc o2) -> o1.name().compareTo(o2.name()) );
                int nmethod= methods.length;
                boolean byAlpha;
                char currentLetter= 'a';
                File mdf; // the current file to which we are writing
                File htmlf;
                String loc;
                if ( fullName.equals("org.das2.qds.ops.Ops") ) {
                    byAlpha= true;
                    loc=  s + "_" + currentLetter + ".md";
                    mdf= new File( mddoc.toString() + "/" + s + "_" + currentLetter + ".md" );
                    htmlf= new File( htmldoc.toString() + "/" + s + "_" + currentLetter + ".html" );
                } else {
                    byAlpha= false;
                    loc=  s + ".md";
                    mdf= new File( mddoc.toString() + "/" + s + ".md" );
                    htmlf= new File( htmldoc.toString() + "/" + s + ".html" );
                }
                
                File d= mdf.getParentFile();
                if ( !d.exists() ) {
                    if ( !d.mkdirs() ) {
                        throw new IllegalStateException("can't make dir: "+d);
                    }
                }
                d= htmlf.getParentFile();
                if ( !d.exists() ) {
                    if ( !d.mkdirs() ) {
                        throw new IllegalStateException("can't make dir: "+d);
                    }
                }

                mdout = new PrintStream(mdf);
                htmlout= new PrintStream(htmlf);
                
                mdout.append("# "+fullName + "\n\n" );
                htmlout.append("<h2>"+fullName+"</h2>");
                
                mdout.append( handleText( classe.commentText() ) ).append("\n\n");
                htmlout.append("<p>").append(classe.commentText()).append("</p>\n");
                
                if ( !mdf.getParentFile().exists() ) {
                    if ( !mdf.getParentFile().mkdirs() ) throw new IllegalStateException("can't make dir");
                }
                
                // constructors
                ConstructorDoc[] constructors= classe.constructors();
                for ( int j=0; j<constructors.length; j++ ) {
                    ConstructorDoc c= constructors[j];
                    if ( !c.isPublic() ) continue;                                               
                    doOneConstructor(c, mdout, htmlout, fullName, byAlpha, s, currentLetter, classe);
                }
                
                // loop over fields
                FieldDoc[] fields= classe.fields();

                int nfields= fields.length;                

                for (int j = 0; j < Math.min( 20000, nfields ); j++) {
                    FieldDoc f= fields[j];
                    if ( !f.isPublic() ) continue;
                    doOneField(f, mdout, htmlout, fullName, byAlpha, s, currentLetter, classe);
                }
                
                // ** loop over methods **
                if (toPrint) {
                    if ( nmethod==0 ) System.err.println("  #################");
                    System.err.println("  # methods: "+nmethod + "  byAlpha: "+byAlpha + "  toFile: "+mdf.toString() );
                    if ( nmethod==0 ) System.err.println("  #################");
                }
                for (int j = 0; j < Math.min( 20000, nmethod ); j++) {
                    MethodDoc m= methods[j];
                    
                    if ( !m.isPublic() ) continue;
                    
                    String name= m.name();
                    
                    //if ( name.equals("addMouseModule") ) {
                    //    System.err.println("line 585, found "+name);
                    //}
                    
                    if (toPrint) System.err.println("  method: " + name );
                    if ( byAlpha ) {
                        if ( name.charAt(0)!=currentLetter ) {
                            mdout.close();
                            htmlout.close();
                            currentLetter= name.charAt(0);
                            mdf= new File( mddoc.toString() + "/" + s + "_"+ currentLetter + ".md" );
                            htmlf= new File( htmldoc.toString() + "/" + s + "_"+ currentLetter + ".html" );
                            loc=  s + "_" + currentLetter + ".md";
                            mdout = new PrintStream(mdf);
                            htmlout = new PrintStream(htmlf);
                        }
                    }               

                    StringBuilder signature= new StringBuilder();
                    StringBuilder jySignature= new StringBuilder();
                    StringBuilder ahrefBuilder= new StringBuilder();
                    
                    signature( jySignature, ahrefBuilder, signature, m );
                    
                    String sb1= jySignature.toString();
                    
                    int linenum= m.position().line();
                    String p= findLinkForSource(s,linenum);
                                        
                    if ( haveIndicated( fullName + "." + name )!=null ) {
                        if ( m.commentText().trim().length()==0 ) { // this is just an alternate form not worth additional comments.
                            String src= String.format( " <a href=\"%s\">[view source]</a>", p );
                            mdout.println(sb1+" "+src+"<br>"); 
                            htmlout.println(sb1+" "+src+"<br>");
                            continue;
                        }
                    }
                    
                    indicated.put( fullName + "." + name, ahrefBuilder.toString() );
                    
                    String href= ahrefBuilder.toString();
                    
                    // here is the bulk of the work
                    if ( doOneMethod(mdout, htmlout, href, m, name, sb1, byAlpha, classNameNoPackage) ) continue;
                    
                    
                    mdout.println( String.format( "\n<a href=\"https://github.com/autoplot/dev/search?q=%s&unscoped_q=%s\">[search for examples]</a>", name, name ) );
                    mdout.println( String.format( "<a href=\"https://github.com/autoplot/documentation/blob/master/javadoc/index-all.md\">[return to index]</a>", name, name ) );
                    htmlout.println( String.format( "<br><br>\n<a href=\"https://github.com/autoplot/dev/search?q=%s&unscoped_q=%s\">[search for examples]</a>", name, name ) );
                    htmlout.println( String.format( " <a href=\"https://github.com/autoplot/documentation/tree/master/javadoc/%s\">[view on GitHub]</a>", loc ) );
                    String htmlLoc;
                    if ( byAlpha ) {
                        htmlLoc= loc.substring(0,loc.length()-5)+".html#"+href;
                    } else {
                        htmlLoc= loc.substring(0,loc.length()-3)+".html#"+href;
                    }
                    htmlout.println( String.format( " <a href=\"https://cottagesystems.com/~jbf/autoplot/doc2018/%s\">[view on old javadoc]</a>",htmlLoc ) );
                    if ( p!=null ) {
                        htmlout.println( String.format( " <a href=\"%s\">[view source]</a>", p ) );
                    }
                    mdout.println("");
                    htmlout.println("<br>");
                    htmlout.println("<br>");
                    
                    String key= htmlLoc;
                    
                    if ( includeGrandIndex( fullName, name ) ) {
                        if ( byAlpha ) {
                            int i= htmlLoc.lastIndexOf("#");
                            grandIndex.put( key, htmlLoc.substring(0,i-5) + "_"+ currentLetter + ".md" + htmlLoc.substring(i) );
                        } else {
                            grandIndex.put( key, htmlLoc.replaceAll("\\.html#",".md#") );
                        }
                        String firstSentence= m.commentText();
                        grandIndexFirstLine.put( key, firstSentence );
                        grandIndexClass.put( key, classe.qualifiedName() );
                        grandIndexSignature.put( key, signature.toString() );
                    }
                    if (toPrint) System.err.println("  done method: " + name );
                }
                
            }catch (FileNotFoundException ex) {
                Logger.getLogger(DocletTip.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (mdout!=null) mdout.close();
                if (htmlout!=null) htmlout.close();
            }
        }
        
        classDoc= null;
        
        File grandIndexFile= new File( mddoc.toString() + "/index-all.md" );
        try ( PrintStream indexOut=new PrintStream(grandIndexFile) ) {
            List<String> keys= new ArrayList( grandIndex.keySet() );
            Collections.sort(keys);
            for ( String k: keys ) {
                if ( k.length()==0 ) continue;
                indexOut.print("<a href=\""+grandIndex.get(k)+"\">");
                if ( grandIndexSignature.containsKey(k) ) {
                    indexOut.print( maybeQualifySignature(k) );
                } else {
                    indexOut.print(k);
                }
                String s= markDownSafeSummary(grandIndexFirstLine.get(k));
                if ( s.length()>0 ) {
                    indexOut.print("</a>"+maybeQualify( k ) + " - " );
                    indexOut.print(s);
                    indexOut.println("<br>");
                } else {
                    indexOut.print("</a> "+maybeQualify( k ) );
                    indexOut.println("<br>");
                }
            }
        } catch ( IOException out ) {
            throw new IllegalStateException("could not write to "+grandIndexFile);
        }

        grandIndexFile= new File( htmldoc.toString() + "/index-all.html" );
        try ( PrintStream indexOut=new PrintStream(grandIndexFile) ) {
            List<String> keys= new ArrayList( grandIndex.keySet() );
            Collections.sort(keys);
            for ( String k: keys ) {
                if ( k.length()==0 ) continue;
                char firstChar= k.charAt(0);
                if ( firstChar=='_' || Character.isUpperCase(firstChar) ) continue;
                //if ( k.equals("getresolution") ) {
                //    System.err.println("here line 531");
                //}
                indexOut.print("<a href=\""+grandIndex.get(k).replaceAll("\\.md",".html")+"\">");
                indexOut.print( grandIndexSignature.get(k) );
                String s= markDownSafeSummary(grandIndexFirstLine.get(k));
                if ( s.length()>0 ) {
                    indexOut.print("</a> "+ maybeQualify( k ) + " - " );
                    indexOut.print(s);
                    indexOut.println("<br>");
                } else {
                    indexOut.print("</a> " + maybeQualify(k) );
                    indexOut.println("<br>");
                }
            }
        } catch ( IOException out ) {
            throw new IllegalStateException("could not write to "+grandIndexFile);
        }
        
        System.err.println("****");
        System.err.println("v20250409_0748");
        System.err.println("htmldoc documentation written to "+htmldoc);
        System.err.println("mddoc documentation written to "+mddoc);
        System.err.println("****");
        
        return true;
        
    }

    private String aAn( String typeName ) {
        String vowel= "aeiou";
        if ( vowel.indexOf( Character.toLowerCase(typeName.charAt(0)) )>-1 ) {
            return "an "+typeName;
        } else {
            return "a "+typeName;
        }
                
    }
    
    /**
     * In the index, only say "of the.java.package" when the thing is not automatically imported.
     * @param k
     * @return 
     */
    private String maybeQualify( String k ) {
        String full= grandIndexClass.get(k);
        if ( "org.das2.qds.ops.Ops".equals(full) ) {
            return "";
        } else if ( "org.autoplot.ScriptContext".equals(full) ) {
            return "";
        } else if ( "org.autoplot.jythonsupport.JythonOps".equals(full) ) {
            return "";
        } else {
            return " of "+ full;
        }
    }
    
    private String maybeQualifySignature( String k ) {
        String full= grandIndexSignature.get(k);
        if ( full.startsWith( "org.das2.qds.ops.Ops" ) ) {
            return k;
        } else {
            return full;
        }
    }
    
    /**
     * create documentation for one method.  Two versions are output to 
     * mdout and htmlout.
     * @param mdout
     * @param htmlout
     * @param ahref the href to use for the method.
     * @param m
     * @param name
     * @param sb1
     * @param byAlpha
     * @param classNameNoPackage
     * @return false if processing should continue
     */
    private boolean doOneMethod(PrintStream mdout, PrintStream htmlout, String ahref, MethodDoc m, 
            String name, String sb1, boolean byAlpha, String classNameNoPackage) {
        mdout.println("***");
        htmlout.println("<hr>");        
        mdout.println("<a name=\""+ahref+"\"></a>");
        htmlout.println("<a name=\""+ahref+"\"></a>");
        Tag[] deprecatedTags= m.tags("deprecated");
        boolean isDeprecated=  deprecatedTags.length>0 ;
        if (isDeprecated) {
            mdout.println("# <del>"+name + "</del>");
            mdout.println("Deprecated: " + deprecatedTags[0].text());
            htmlout.println("<h2><del>"+name+"</del></h2>");
            htmlout.println("Deprecated: " + deprecatedTags[0].text());
            return true;
        } else {
            mdout.println("# "+name);
            htmlout.println("<h2>"+name+"</h2>");
        }
        mdout.println(sb1);
        htmlout.println(sb1);
        mdout.println("");
        htmlout.println("");
        String comments= handleText(m.commentText());
        mdout.println(comments);
        htmlout.println("<p>"+comments+"</p>");
        if ( m.parameters().length>0 ) {
            Map<String,ParamTag> pat= new HashMap<>();
            for ( ParamTag pt: m.paramTags() ) {
                pat.put( pt.parameterName(), pt );
            }
            mdout.println("");
            htmlout.println("");
            mdout.println("### Parameters:" );
            htmlout.println("<h3>Parameters</h3>" );
            for ( int k=0; k<m.parameters().length; k++ ) {
                Parameter parameter= m.parameters()[k];
                if ( k>0 ) {
                    mdout.print("<br>");
                    htmlout.println("<br>");
                }
                ParamTag pt1= pat.get(parameter.name());
                String comment= pt1==null ? "" : pt1.parameterComment();
                if ( comment.length()==0 ) {
                    mdout.println(""+parameter.name() + " - " + aAn(parameter.typeName()) );
                    htmlout.println(""+parameter.name() + " - " + aAn(parameter.typeName()) );
                } else {
                    mdout.println(""+parameter.name() + " - " + comment );
                    htmlout.println(""+parameter.name() + " - " + comment );
                }
            }
        }
        mdout.println("");
        htmlout.println("");
        mdout.println("### Returns:" );
        htmlout.println("<h3>Returns:</h3>" );
        Tag[] tags= m.tags("return");
        if ( tags.length>0 ) {
            String s1= tags[0].text();
            if ( s1.trim().length()>0 ) {
                mdout.println( s1.trim() );
                htmlout.println( s1.trim() );
            } else {
                mdout.println( aAn( colloquialName( m.returnType().toString() ) ) );
                htmlout.println( aAn( colloquialName( m.returnType().toString() ) ) );
                mdout.println("");
                htmlout.println( "" );
            }
        } else {
            String s1= m.returnType().toString();
            if ( s1.equals("void") ) {
                mdout.println( "void (returns nothing)" );
                htmlout.println( "void (returns nothing)" );
            } else {
                mdout.println( s1 );
                htmlout.println( s1 );
            }
            mdout.println("");
            htmlout.println("");
        }
        Tag[] seeTags= m.tags("see");
        if ( seeTags.length>0 ) {
            mdout.println("### See Also:\n");
            htmlout.println("<h3>See Also:</h3>\n");
        }
        for (Tag seeTag : seeTags) {
            SeeTag t = (SeeTag) seeTag;
            StringBuilder mdout1= new StringBuilder();
            StringBuilder htmlout1= new StringBuilder();
            String see= doOneSeeTag(t, byAlpha, classNameNoPackage, mdout1, htmlout1);
            mdout.print(mdout1.toString());
            htmlout.print(htmlout1.toString());
        }
        return false;
    }

    /**
     * returns links for the html and markdown like 
     * <pre>
     * .../org/das2/qds/ops/Ops.html#add-int[]-int[]
     * </pre>
     * @param t
     * @param byAlpha
     * @param classNameNoPackage
     * @param mdout
     * @param htmlout 
     */
    private String doOneSeeTag(SeeTag t, boolean byAlpha, String classNameNoPackage, StringBuilder mdout, StringBuilder htmlout) {
        int it= t.text().indexOf(')');
        String l;
        if ( it>-1 ) {
            l = t.text().substring(0,it+1);
        } else {
            l = t.text();
        }
        
        logger.log(Level.FINE, "see {0} {1}", new Object[]{l, byAlpha});
        
        String link= l;
        int ii= link.indexOf("(");
        if ( ii==-1 ) ii=link.indexOf(" ");
        if ( ii==-1 ) ii= link.length();
        if ( ii>-1 ) {
            link= link.substring(0,ii);
        }
        link= link.replaceAll("[\\(\\)\\,]", "-");
        link= link.replaceAll("\\s", "");
        if ( link.endsWith("-") ) link= link.substring(0,link.length()-1);
        
        if ( byAlpha ) {
            if ( l.startsWith("http") ) {
                // do nothing
            } else {
                int i= link.indexOf('#');
                if ( i>0 ) {
                    link= link.substring(0,i) + "_" + link.charAt(i+1) + link.substring(i);
                } else if ( i==0 ) {
                    link= classNameNoPackage + "_" + link.charAt(i+1) + link.substring(i);
                }
            }
            
        } else {
            
            if ( l.startsWith("http") ) {
                // do nothing
            } else {
                int i= link.indexOf("#");
                if ( i>0 ) {
                    link= link.substring(0,i) + link.substring(i);
                } else if ( i==0 ) {
                    //do nothing;
                }
            }
        }
        
        String result;
        if ( t.label()==null ) {
            mdout.append("<a href='").append(getMDLinkFor( link )).append("'>").append(seeAlsoLabel(l)).append("</a><br>\n");
            htmlout.append("<a href='").append(getHtmlLinkFor(link)).append("'>").append(seeAlsoLabel(l)).append("</a><br>\n");
        } else {
            String mdLink= getMDLinkFor(link);
            mdout.append("<a href='").append(mdLink).append("'>")
                    .append(seeAlsoLabel(l))
                    .append("</a> ").append(t.label()).append("<br>\n");
            String htmlLink= getHtmlLinkFor(link);
            htmlout.append("<a href='").append(htmlLink).append("'>")
                    .append(seeAlsoLabel(l))
                    .append("</a> ").append(t.label()).append("<br>\n");
            
        }
        result= htmlout.toString();
        return result;
    }

    private boolean doOneField(FieldDoc f, PrintStream mdout, PrintStream htmlout, String fullName, boolean byAlpha, String s, char currentLetter, ClassDoc classe) {
        String name= f.name();

        StringBuilder ahrefBuilder= new StringBuilder();
        // <a name='accum(org.das2.qds.QDataSet,org.das2.qds.QDataSet)'></a> // note not standard JavaDoc.

        ahrefBuilder.append(name);

        mdout.println("***");
        htmlout.println("<hr>");
        mdout.println("<a name=\""+ahrefBuilder.toString()+"\"></a>");
        htmlout.println("<a name=\""+ahrefBuilder.toString()+"\"></a>");
        mdout.println("# "+name);
        htmlout.println("<h2>"+name+"</h2>");
        mdout.println("");
        mdout.println(f.commentText());
        htmlout.println("<p>"+f.commentText()+"</p>");
        mdout.println("");
        htmlout.println("");
        if ( includeGrandIndex( fullName, name ) ) {
            if ( byAlpha ) {
                grandIndex.put( name, s + "_"+ currentLetter + ".md#"+name );
            } else {
                grandIndex.put( name, s + ".md#"+name );
            }
            String firstSentence= f.commentText();
            grandIndexFirstLine.put( name, firstSentence );
            grandIndexClass.put( name, classe.qualifiedName() );
            grandIndexSignature.put( name, classe.qualifiedName() + "." + name );
        }
        return false;
    }

    private void doOneConstructor(ConstructorDoc c, PrintStream mdout, PrintStream htmlout, String fullName, boolean byAlpha, String s, char currentLetter, ClassDoc classe) {
        StringBuilder signature= new StringBuilder();
        StringBuilder sb= new StringBuilder();
        StringBuilder ahrefBuilder= new StringBuilder();
        
        signature( sb, ahrefBuilder, signature, c);
        
        mdout.println( "# "+sb.toString() );
        htmlout.println("<h2>"+sb.toString() +"</h2>");
        
        mdout.println( handleText(c.commentText()) );
        htmlout.println("<p>"+c.commentText()+"</p>");
        
        mdout.println("");
        htmlout.println("");
        
        String name= c.name();
        
        if ( includeGrandIndex( fullName, c.name() ) ) {
            if ( byAlpha ) {
                grandIndex.put( name, s + "_"+ currentLetter + ".md#"+name );
            } else {
                grandIndex.put( name, s + ".md#"+name );
            }
            String firstSentence= c.commentText();
            grandIndexFirstLine.put( name, firstSentence );
            grandIndexClass.put( name, classe.qualifiedName() );
            grandIndexSignature.put( name, classe.qualifiedName() );
        }
    }
    
    /**
     * Start the generation of Javadoc.  The output is 
     * created in mddoc, which can be set using the 
     * environment variable mddoc or the command line property mddoc.
     * Likewise, htmldoc can be set this way too.
     * @param root the root which is set at the command line.
     * @return true if things worked.
     */
    public static boolean start(RootDoc root) {
        String s;
        
        String sdoc;
 
        sdoc= System.getenv("mddoc"); 
        if ( sdoc!=null ) mddoc= new File( sdoc );
        
        sdoc= System.getenv("htmldoc");
        if ( sdoc!=null ) htmldoc= new File( sdoc );
        
        s = System.getProperty("mddoc");
        if (s != null) {
            mddoc = new File(s);
            if (!(mddoc.exists() || mddoc.getParentFile().canWrite())) {
                throw new RuntimeException("Unable to write to " + mddoc);
            }
        }
        s = System.getProperty("htmldoc");
        if (s != null) {
            htmldoc = new File(s);
            if (!(htmldoc.exists() || htmldoc.getParentFile().canWrite())) {
                throw new RuntimeException("Unable to write to " + htmldoc);
            }
        }
        
        if ( mddoc.getAbsolutePath().equals("/") || htmldoc.getAbsolutePath().equals("/") ) {
            System.err.println("****");
            System.err.println("Instructions: ");
            System.err.println("htmldoc must be set to the target location for html documentation (export htmldoc=/tmp/htmldoc/)");
            System.err.println("mddoc must be set to the target location for markdown documentation (export mddoc=/tmp/mddoc/)");
            System.err.println("");
            System.err.println("ABNORMAL EXIT, htmldoc or mddoc not set.");
            System.err.println("");
            return false;
        } else {
            System.err.println("****");
            System.err.println("Writing htmldoc documentation to " + htmldoc);
            System.err.println("Writing mddoc documentation to " + mddoc);
        }
        
        return new DocletTip().doStart(root);
    }
} 
