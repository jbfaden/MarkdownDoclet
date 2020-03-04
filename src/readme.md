# MarkdownDoclet
Experiment with Javadoc Doclet which writes markdown instead of html.  This is 
prints the markdown to stdout while processing, which is probably not the "right"
way to do this.  I'm not sure if this can be done cleanly, but worse case I'll 
ignore the html output.

https://docs.oracle.com/javase/7/docs/jdk/api/javadoc/doclet/

Note that I found later that there are issues with using Markdown for the 
popup in Autoplot, so a simplified HTML version is created as well.

<hr>

# Running at Cottage Systems Headquarters or on Campus

Build using Netbeans, and then test like so on nudnik.physics.uiowa.edu:

nudnik> javadoc -docletpath /home/jbf/git/MarkdownDoclet/dist/MarkdownDoclet.jar \
   -doclet tips.DocletTip \
   /home/jbf/project/autoplot/autoplot-code/QDataSet/src/org/das2/qds/ops/Ops.java

And to debug from Netbeans:
~~~~~
spot7> cd /home/jbf/git/MarkdownDoclet/ 
spot7> javadoc -J-Xrunjdwp:server=y,transport=dt_socket,address=12345,suspend=y \
   -docletpath /home/jbf/git/MarkdownDoclet/dist/MarkdownDoclet.jar \
   -doclet tips.DocletTip \
   @sources.txt
~~~~~
~~~~~

~~~~~
nudnuk> cd /home/jbf/git/MarkdownDoclet/
nudnuk> javadoc -J-Xrunjdwp:server=y,transport=dt_socket,address=12345,suspend=y \
   -docletpath /home/jbf/git/MarkdownDoclet/dist/MarkdownDoclet.jar \
   -doclet tips.DocletTip \
   @sources.txt
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

