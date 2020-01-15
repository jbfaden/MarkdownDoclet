
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