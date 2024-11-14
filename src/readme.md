# MarkdownDoclet
Experiment with Javadoc Doclet which writes markdown instead of html.  The
two environment variables, "mddoc" and "htmldoc" are the locations of 
directories where a HTML version of the documentation and a MarkDown version
of the documentation should be created.

This is kludged a little bit for Autoplot, where the class Ops is so large it
was causing GitLabs to fail.  Also, several familiar classes like QDataSet
and Datum have special handling as well.

https://docs.oracle.com/javase/7/docs/jdk/api/javadoc/doclet/

Note that I found later that there are issues with using Markdown for the 
popup in Autoplot, so a simplified HTML version is created as well.

<hr>

# Running at Cottage Systems Headquarters or on Campus

To make sources.txt file:

~~~~~
spot9> cd /home/jbf/temp/autoplot
spot9> rm -rf Autoplot/temp-volatile-src/
spot9> find * -name '*.java' -exec echo `pwd`/{} \; > /home/jbf/ct/netbeansProjects/MarkdownDoclet/sources.txt
~~~~~

Build using Netbeans, and then test like so on nudnik.physics.uiowa.edu:

nudnik> javadoc -docletpath /home/jbf/git/MarkdownDoclet/dist/MarkdownDoclet.jar \
   -doclet tips.DocletTip \
   /home/jbf/project/autoplot/autoplot-code/QDataSet/src/org/das2/qds/ops/Ops.java

And to debug from Netbeans:
~~~~~
spot9> cd /home/jbf/ct/netbeansProjects/MarkdownDoclet/ 
spot9> javadoc -J-Xrunjdwp:server=y,transport=dt_socket,address=12345,suspend=y \
   -docletpath /home/jbf/ct/netbeansProjects/MarkdownDoclet/dist/MarkdownDoclet.jar \
   -doclet tips.DocletTip \
   @sources.txt
~~~~~
~~~~~

~~~~~
nudnuk> cd /home/jbf/ct/netbeansProjects/MarkdownDoclet/
nudnuk> javadoc -docletpath /home/jbf/git/MarkdownDoclet/dist/MarkdownDoclet.jar 
  -J-Dmddoc=/home/jbf/project/rbsp/git/autoplot/doc/ 
  -J-Dhtmldoc=/home/jbf/Linux/public_html/autoplot/doc/  
  -doclet tips.DocletTip    @sources.txt
~~~~~

To run the entire lot:
~~~~~
spot7> cd /home/jbf/git/MarkdownDoclet
spot7> find /home/jbf/temp/autoplot/ -name '*.java' | grep -v temp-src | grep -v temp-classes > sources.txt
spot7> javadoc \
   -docletpath /home/jbf/git/MarkdownDoclet/dist/MarkdownDoclet.jar \
   -doclet tips.DocletTip @sources.txt
~~~~~

~~~~~

~~~~~
nudnik> cd /home/jbf/git/MarkdownDoclet
nudnik> export htmldoc=/tmp/htmldoc/
nudnik> export mddoc=/tmp/mddoc/
nudnik> find /home/jbf/project/autoplot/autoplot-code/ -name '*.java' | grep -v temp-src \
        | grep -v temp-classes | grep -v "/test/" | grep -v  "ProGAL" | grep -v "NetCdfDataSource" | grep -v VATesting > sources.txt
nudnuk> javadoc -docletpath dist/MarkdownDoclet.jar -doclet tips.DocletTip -sourcepath @sources.txt
~~~~~

The output from any of these is at     <br>
/home/jbf/project/rbsp/git/autoplot/doc/ for markdown, and <br>
/home/jbf/Linux/public_html/autoplot/doc/ for html.


<hr>
To build from command line, use just "ant jar".

# 2024-11-14
I found this is much easier to debug if working with a small project.  See
/home/jbf/ct/netbeansProjects/TestJavaDoc and @sources-simple.txt

