# copies the results of a web container build (ant clean collect) to the relevant directories of the given OSGi trunk
REPO=$1/licensed/repo
SRC=target/bundles
cp $SRC/org.eclipse.virgo.util.osgi-2.1.0.*.jar $REPO/org.eclipse.virgo.util.osgi/org.eclipse.virgo.util.osgi-2.1.0.jar
cp $SRC/org.eclipse.virgo.util.common-2.1.0.*.jar $REPO/org.eclipse.virgo.util.common/org.eclipse.virgo.util.common-2.1.0.jar
cp $SRC/org.eclipse.virgo.util.io-2.1.0.*.jar $REPO/org.eclipse.virgo.util.io/org.eclipse.virgo.util.io-2.1.0.jar
cp $SRC/org.eclipse.virgo.util.math-2.1.0.*.jar $REPO/org.eclipse.virgo.util.math/org.eclipse.virgo.util.math-2.1.0.jar
cp $SRC/org.eclipse.virgo.util.parser.manifest-2.1.0.*.jar $REPO/org.eclipse.virgo.util.parser.manifest/org.eclipse.virgo.util.parser.manifest-2.1.0.jar
cp $SRC/org.springframework.aop-2.5.6.*.jar $REPO/org.springframework.aop/org.springframework.aop-2.5.6.jar
cp $SRC/org.springframework.beans-2.5.6.*.jar $REPO/org.springframework.beans/org.springframework.beans-2.5.6.jar
cp $SRC/org.springframework.context-2.5.6.*.jar $REPO/org.springframework.context/org.springframework.context-2.5.6.jar
cp $SRC/org.springframework.core-2.5.6.*.jar $REPO/org.springframework.core/org.springframework.core-2.5.6.jar
cp $SRC/org.springframework.osgi.core-1.2.0.jar $REPO/org.springframework.osgi.core/org.springframework.osgi.core-1.2.0.jar
cp $SRC/org.springframework.osgi.io-1.2.0.jar $REPO/org.springframework.osgi.io/org.springframework.osgi.io-1.2.0.jar
cp $SRC/com.springsource.org.apache.catalina.ha.springsource-6.0.20.*.jar $REPO/com.springsource.org.apache.catalina.ha.springsource/com.springsource.org.apache.catalina.ha.springsource-6.0.20.jar
cp $SRC/com.springsource.org.apache.catalina.springsource-6.0.20.*.jar $REPO/com.springsource.org.apache.catalina.springsource/com.springsource.org.apache.catalina.springsource-6.0.20.jar
cp $SRC/com.springsource.org.apache.catalina.tribes.springsource-6.0.20.*.jar $REPO/com.springsource.org.apache.catalina.tribes.springsource/com.springsource.org.apache.catalina.tribes.springsource-6.0.20.jar
cp $SRC/com.springsource.org.apache.coyote.springsource-6.0.20.*.jar $REPO/com.springsource.org.apache.coyote.springsource/com.springsource.org.apache.coyote.springsource-6.0.20.jar
cp $SRC/com.springsource.org.apache.el.springsource-6.0.20.*.jar $REPO/com.springsource.org.apache.el.springsource/com.springsource.org.apache.el.springsource-6.0.20.jar
cp $SRC/com.springsource.org.apache.jasper.org.eclipse.jdt.springsource-6.0.20.*.jar $REPO/com.springsource.org.apache.jasper.org.eclipse.jdt.springsource/com.springsource.org.apache.jasper.org.eclipse.jdt.springsource-6.0.20.jar
cp $SRC/com.springsource.org.apache.jasper.springsource-6.0.20.*.jar $REPO/com.springsource.org.apache.jasper.springsource/com.springsource.org.apache.jasper.springsource-6.0.20.jar
cp $SRC/com.springsource.org.apache.juli.extras.springsource-6.0.20.*.jar $REPO/com.springsource.org.apache.juli.extras.springsource/com.springsource.org.apache.juli.extras.springsource-6.0.20.jar
cp $SRC/com.springsource.org.apache.commons.logging-1.1.1.jar $REPO/com.springsource.org.apache.commons.logging/com.springsource.org.apache.commons.logging-1.1.1.jar
cp $SRC/org.eclipse.gemini.web.core-1.0.0.*.jar $REPO/org.eclipse.gemini.web.core/org.eclipse.gemini.web.core-1.0.0.jar
cp $SRC/org.eclipse.gemini.web.extender-1.0.0.*.jar $REPO/org.eclipse.gemini.web.extender/org.eclipse.gemini.web.extender-1.0.0.jar
cp $SRC/org.eclipse.gemini.web.tomcat-1.0.0.*.jar $REPO/org.eclipse.gemini.web.tomcat/org.eclipse.gemini.web.tomcat-1.0.0.jar
