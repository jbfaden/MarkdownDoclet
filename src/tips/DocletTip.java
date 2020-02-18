
package tips;

import com.sun.javadoc.ClassDoc;
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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * From https://www.zdnet.com/article/customize-javadoc-output-with-doclets/
 * To re-run the source for http://autoplot.org/wiki/index.php?title=developer.scripting&action=edit&section=68
 * you would: javadoc -docletpath /home/jbf/eg/java/javadoclet/DocletTip/dist/DocletTip.jar -doclet tips.DocletTip /home/jbf/project/autoplot/autoplot-code/QDataSet/src/org/das2/qds/ops/Ops.java
 *
 * @author jbf
 */
public class DocletTip {

    private static File mddoc= new File("/home/jbf/project/rbsp/git/autoplot/doc/");
    private static File htmldoc= new File("/home/jbf/Linux/public_html/autoplot/doc/");
    
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
        switch (name) {
            case "org.das2.qds.QDataSet":
                return "QDataSet";
            case "org.das2.util.monitor.ProgressMonitor":
                return "ProgressMonitor";
            case "org.das2.datum.Units":
                return "Units";
            case "org.das2.datum.Datum":
                return "Datum";
            case "org.das2.datum.DatumRange":
                return "DatumRange";
            case "java.lang.Object":
                return "Object";
            case "java.lang.String":
                return "String";
            case "java.lang.Number":
                return "Number";
            case "java.lang.Double":
                return "Double";
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
    
    /**
     * ensure no characters are found which would mess up markdown.
     * @param s
     * @return 
     */
    private static String markDownSafeSummary( String s ) {
        Pattern p= Pattern.compile("[^0-9a-zA-Z,\\. _]");
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
     * @param s
     * @return 
     */
    private static String findLinkFor( String s ) {
        int i= s.lastIndexOf("/");
        if ( s.substring(0,i).equals("org/autoplot") ) {
            String path= "https://sourceforge.net/p/autoplot/code/HEAD/tree/autoplot/trunk/Autoplot/src/";
            return path + s + ".java";
        } else if ( s.startsWith("org/das2/util") ) {
            String path= "https://saturn.physics.uiowa.edu/svn/das2/dasCore/community/autoplot2011/trunk/dasCoreUtil/src/";
            return path + s + ".java";
        } else if ( s.startsWith("org/das2/datum") ) {
            String path= "https://saturn.physics.uiowa.edu/svn/das2/dasCore/community/autoplot2011/trunk/dasCoreDatum/src/";
            return path + s + ".java";
        } else if ( s.startsWith("org/das2") ) {
            String path= "https://saturn.physics.uiowa.edu/svn/das2/dasCore/community/autoplot2011/trunk/dasCore/src/";
            return path + s + ".java";
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
    
    /**
     * 
     * @param root
     * @return 
     */
     public static boolean start(RootDoc root) {
         
        String sdoc;
        sdoc= System.getenv("mddoc"); 
        if ( sdoc!=null )  mddoc= new File( sdoc );
        
        sdoc= System.getenv("htmldoc");
        if ( sdoc!=null ) htmldoc= new File( sdoc );
            
        ClassDoc[] classes = root.classes();

        if ( !mddoc.exists() ) {
            if ( !mddoc.mkdirs() ) throw new IllegalStateException("can't make dir: "+mddoc);
        }

        if ( !htmldoc.exists() ) {
            if ( !htmldoc.mkdirs() ) throw new IllegalStateException("can't make dir: "+htmldoc);
        }
        
        Map<String,String> grandIndex= new HashMap<>();
        Map<String,String> grandIndexFirst= new HashMap<>();
        Map<String,String> grandIndexClass= new HashMap<>();
        Map<String,String> grandIndexSignature= new HashMap<>();
        
        boolean seePlotElement= false;
        
        for (ClassDoc classe : classes) {
            
            String fullName= classe.qualifiedName();
            
            if ( fullName.startsWith("org.autoplot") || fullName.startsWith("org.das2") ) {
                // do nothing.
            } else {
                continue;
            }
            
            //if ( fullName.startsWith("org.autoplot.AutoplotUI" ) ) {
            //    System.err.println("found it");
            //} else {
            //    continue;
            //}
            
            PrintStream mdout= null;
            PrintStream htmlout;
            try {
                String s = classe.qualifiedName();
                if ( s.endsWith("PlotElement") ) {
                    seePlotElement= true;
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
                FieldDoc[] fields= classe.fields();
                
                Arrays.sort( methods, (MethodDoc o1, MethodDoc o2) -> o1.name().compareTo(o2.name()) );
                int nmethod= methods.length;
                boolean byAlpha;
                char currentLetter= 'a';
                File mdf; // the current file to which we are writing
                File htmlf;
                String loc;
                if ( nmethod>200 ) {
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
                
                int nfields= fields.length;
                
                File d= mdf.getParentFile();
                if ( !d.exists() ) {
                    if ( !d.mkdirs() ) throw new IllegalStateException("can't make dir: "+d);
                }
                d= htmlf.getParentFile();
                if ( !d.exists() ) {
                    if ( !d.mkdirs() ) throw new IllegalStateException("can't make dir: "+d);
                }

                mdout = new PrintStream(mdf);
                htmlout= new PrintStream(htmlf);
                
                mdout.append("# "+fullName );
                htmlout.append("<h1>"+fullName+"</h1>");
                
                mdout.append( classe.commentText() ).append("\n");
                htmlout.append("<p>").append(classe.commentText()).append("</p>\n");
                
                if ( !mdf.getParentFile().exists() ) {
                    if ( !mdf.getParentFile().mkdirs() ) throw new IllegalStateException("can't make dir");
                }
                
                for (int j = 0; j < Math.min( 20000, nfields ); j++) {
                    FieldDoc f= fields[j];
                    
                    if ( !f.isPublic() ) continue;
                    
                    String name= f.name();
                    
                    if ( seePlotElement ) {
                        seePlotElement= false; // breakpoint here for debugging.      
                    }
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
                    
                    StringBuilder sb= new StringBuilder();
                    StringBuilder ahrefBuilder= new StringBuilder();

                    ahrefBuilder.append("field: ").append(name);
                    
                    // <a name='accum(org.das2.qds.QDataSet,org.das2.qds.QDataSet)'></a> // note not standard JavaDoc.
                    if ( haveIndicated(ahrefBuilder.toString())!=null ) {
                        mdout.println("<a name=\""+ahrefBuilder.toString()+"\"></a>");
                        htmlout.println("<a name=\""+ahrefBuilder.toString().replaceAll("\\.md",".html")+"\"></a>");
                        continue;
                    } else {
                        indicated.put( ahrefBuilder.toString(), ahrefBuilder.toString() );
                    }
                    mdout.println("***");
                    htmlout.println("<hr>");
                    mdout.println("<a name=\""+ahrefBuilder.toString()+"\"></a>");
                    htmlout.println("<a name=\""+ahrefBuilder.toString()+"\"></a>");
                    mdout.println("# "+name);
                    htmlout.println("<h1>"+name+"</h1>");
                    
                    mdout.println(sb.toString());
                    htmlout.println(sb.toString());
                    
                    mdout.println("");
                    htmlout.println("");
                    mdout.println(f.commentText());
                    htmlout.println("<p>"+f.commentText()+"</p>");
                    
                    mdout.println("");
                    htmlout.println("");

                }
                
                for (int j = 0; j < Math.min( 20000, nmethod ); j++) {
                    MethodDoc m= methods[j];
                    
                    if ( !m.isPublic() ) continue;
                    
                    String name= m.name();

                    //if ( name.equals("createDummyPlot") ) {
                    //    System.err.println("handling the method: "+name);
                    //}
                    
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
                    StringBuilder sb= new StringBuilder();
                    StringBuilder ahrefBuilder= new StringBuilder();
                    sb.append(name).append("( ");
                    ahrefBuilder.append(name); //.append("(");
                    signature.append(name).append("(");
                    for ( int k=0; k<m.parameters().length; k++ ) {
                        if ( k>0 ) sb.append(", ");
                        if ( k>0 ) ahrefBuilder.append(",");
                        if ( k>0 ) signature.append(",");
                        Parameter pk= m.parameters()[k];
                        sb.append(colloquialName(pk.type().toString())).append(" ").append(pk.name());
                        //ahrefBuilder.append( pk.type().toString() );
                        signature.append(pk.name());
                    }
                    //ahrefBuilder.append(")");
                    signature.append(")");
                    // <a name='accum(org.das2.qds.QDataSet,org.das2.qds.QDataSet)'></a> // note not standard JavaDoc.
                    sb.append(" ) &rarr; ").append( colloquialName(m.returnType().simpleTypeName() ) );
                    if ( haveIndicated( name )==null ) {
                        mdout.println("<a name=\""+ahrefBuilder.toString()+"\"></a>");
                        htmlout.println("<a name=\""+ahrefBuilder.toString().replaceAll("\\.md",".html")+"\"></a>");
                        indicated.put( name, ahrefBuilder.toString() );
                    } else {
                        continue;
                    }
                    
                    mdout.println("***");
                    htmlout.println("<hr>");
                    mdout.println("<a name=\""+ahrefBuilder.toString()+"\"></a>");
                    htmlout.println("<a name=\""+ahrefBuilder.toString()+"\"></a>");
                    
                    
                    Tag[] deprecatedTags= m.tags("deprecated");
                    boolean isDeprecated=  deprecatedTags.length>0 ;
                    
                    if ( isDeprecated ) {
                        mdout.println("# <del>"+name + "</del>");
                        mdout.println("Deprecated: " + deprecatedTags[0].text());
                        htmlout.println("<h1><del>"+name+"</del></h1>");
                        htmlout.println("Deprecated: " + deprecatedTags[0].text());
                        continue;
                    } else {
                        mdout.println("# "+name);
                        htmlout.println("<h1>"+name+"</h1>");
                    }
                    
                    mdout.println(sb.toString());
                    htmlout.println(sb.toString());
                    
                    mdout.println("");
                    htmlout.println("");
                    
                    String comments= handleText(m.commentText());
                    mdout.println(comments);
                    htmlout.println("<p>"+comments+"</p>");
                    
                    if ( m.paramTags().length>0 ) {
                        mdout.println("");
                        htmlout.println("");
                        mdout.println("### Parameters:" );
                        htmlout.println("<h3>Parameters</h3>" );
                        for ( int k=0; k<m.paramTags().length; k++ ) {
                            ParamTag pt= m.paramTags()[k];
                            if ( k>0 ) {
                                mdout.print("<br>");
                                htmlout.println("<br>");
                            }
                            mdout.println(""+pt.parameterName() + " - " + pt.parameterComment() );
                            htmlout.println(""+pt.parameterName() + " - " + pt.parameterComment() );
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
                            mdout.println( m.returnType().toString() );
                            htmlout.println( m.returnType().toString() );
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
                        mdout.println("### See Also:");
                        htmlout.println("<h3>See Also:</h3>");
                    }
                     
                    //if ( name.equals("setLeftPanel") ) {
                    //    System.err.println("here setLeft");
                    //}
                    
                    for (Tag seeTag : seeTags) {
                        SeeTag t = (SeeTag) seeTag;
                        int it= t.text().indexOf(')');
                        String l;
                        if ( it>-1 ) {
                            l = t.text().substring(0,it+1);
                        } else {
                            l = t.text();
                        }
                        
                        if ( byAlpha && l.charAt(1)!=currentLetter ) {
                            l= classNameNoPackage + "_" + l.charAt(1) + ".md" + l;
                        }
                        
                        String link= l;
                        int ii= link.indexOf("(");
                        if ( ii>-1 ) {
                            link= link.substring(0,ii);
                        }
                        
                        int i= link.indexOf("#");
                        if ( i>0 ) {
                            link= link.substring(0,i) + ".md" + link.substring(i);
                        } else if ( i==0 ) {
                            //do nothing;
                        } else {
                            link= link + ".md";
                        }
                        
                        if ( t.label()==null ) {
                            mdout.println("<a href='"+link+"'>" + seeAlsoLabel(l) +"</a><br>" );
                            htmlout.println( "<a href='"+link.replaceAll("\\.md",".html")+"'>" + seeAlsoLabel(l) +"</a><br>" );
                        } else {
                            mdout.println("<a href='"+link+"'>" + seeAlsoLabel(l) +"</a> "+t.label()+"<br>" );
                            htmlout.println("<a href='"+link.replaceAll("\\.md",".html")+"'>" + seeAlsoLabel(l) +"</a> "+t.label()+"<br>" );
                        }
                    }
                    mdout.println( String.format( "\n<a href=\"https://github.com/autoplot/dev/search?q=%s&unscoped_q=%s\">search for examples</a>", name, name ) );
                    htmlout.println( String.format( "<br><br>\n<a href=\"https://github.com/autoplot/dev/search?q=%s&unscoped_q=%s\">search for examples</a>", name, name ) );
                    htmlout.println( String.format( " <a href=\"https://github.com/autoplot/documentation/wiki/doc/%s\">view on GitHub</a>", loc ) );
                    String p= findLinkFor(s);
                    if ( p!=null ) {
                        htmlout.println( String.format( " <a href=\"%s\">view source</a>", p ) );
                    }
                    mdout.println("");
                    htmlout.println("<br>");
                    htmlout.println("<br>");
                    
                    if ( byAlpha ) {
                        grandIndex.put( name, s + "_"+ currentLetter + ".md#"+name );
                    } else {
                        grandIndex.put( name, s + ".md#"+name );
                    }
                    String firstSentence= m.commentText();
                    grandIndexFirst.put( name, firstSentence.substring(0,Math.min(60,firstSentence.length()) ) );
                    grandIndexClass.put( name, classe.qualifiedName() );
                    grandIndexSignature.put( name, signature.toString() );
                }
            }catch (FileNotFoundException ex) {
                Logger.getLogger(DocletTip.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                mdout.close();
            }
        }
        
        File grandIndexFile= new File( mddoc.toString() + "/index-all.md" );
        try ( PrintStream indexOut=new PrintStream(grandIndexFile) ) {
            List<String> keys= new ArrayList( grandIndex.keySet() );
            Collections.sort(keys);
            for ( String k: keys ) {
                if ( k.length()==0 ) continue;
                indexOut.print("<a href=\""+grandIndex.get(k)+"\">");
                if ( grandIndexSignature.containsKey(k) ) {
                    indexOut.print(grandIndexSignature.get(k));
                } else {
                    indexOut.print(k);
                }
                String s= markDownSafeSummary(grandIndexFirst.get(k));
                if ( s.length()>0 ) {
                    indexOut.print("</a> of "+ grandIndexClass.get(k) + " - " );
                    indexOut.print(s);
                    indexOut.println("<br>");
                } else {
                    indexOut.print("</a> of "+ grandIndexClass.get(k) );
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
                if ( k.equals("getresolution") ) {
                    System.err.println("here line 531");
                }
                indexOut.print("<a href=\""+grandIndex.get(k).replaceAll("\\.md",".html")+"\">");
                indexOut.print( grandIndexSignature.get(k) );
                String s= markDownSafeSummary(grandIndexFirst.get(k));
                if ( s.length()>0 ) {
                    indexOut.print("</a> of "+ grandIndexClass.get(k) + " - " );
                    indexOut.print(s);
                    indexOut.println("<br>");
                } else {
                    indexOut.print("</a> of "+ grandIndexClass.get(k) );
                    indexOut.println("<br>");
                }
            }
        } catch ( IOException out ) {
            throw new IllegalStateException("could not write to "+grandIndexFile);
        }
        
        System.err.println("****");
        System.err.println("v20200218_0817");
        System.err.println("htmldoc documentation written to "+htmldoc);
        System.err.println("mddoc documentation written to "+mddoc);
        System.err.println("****");
        
        return true;
    }
} 