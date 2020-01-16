
Build using Netbeans, and then test like so on nudnik.physics.uiowa.edu:

nudnik> javadoc -docletpath /home/jbf/git/MarkdownDoclet/dist/MarkdownDoclet.jar \
   -doclet tips.DocletTip \
   /home/jbf/project/autoplot/autoplot-code/QDataSet/src/org/das2/qds/ops/Ops.java

And to debug from Netbeans:
~~~~~
spot7> javadoc -J-Xrunjdwp:server=y,transport=dt_socket,address=12345,suspend=y \
   -docletpath /home/jbf/ct/netbeansProjects/MarkdownDoclet/dist/MarkdownDoclet.jar \
   -doclet tips.DocletTip \
   /home/jbf/temp/autoplot/QDataSet/src/org/das2/qds/ops/Ops.java
~~~~~
nudnuk> javadoc -J-Xrunjdwp:server=y,transport=dt_socket,address=12345,suspend=y \
   -docletpath dist/MarkdownDoclet.jar -doclet tips.DocletTip -sourcepath @sources.txt
~~~~~

To run the entire lot:
~~~~~
spot7> cd ~
spot7> find temp/autoplot/ -name '*.java' | grep -v temp-src | grep -v temp-classes > sources.txt
spot7> javadoc -docletpath /home/jbf/ct/netbeansProjects/MarkdownDoclet/dist/MarkdownDoclet.jar\
   -doclet tips.DocletTip -sourcepath temp/autoplot/ @sources.txt
~~~~~
nudnik> cd /home/jbf/git/MarkdownDoclet
nudnik> find /home/jbf/project/autoplot/autoplot-code/ -name '*.java' | grep -v temp-src \
        | grep -v temp-classes | grep -v "/test/" | grep -v  "ProGAL" | grep -v "NetCdfDataSource" | grep -v VATesting > sources.txt
nudnuk> javadoc -docletpath dist/MarkdownDoclet.jar -doclet tips.DocletTip -sourcepath @sources.txt
