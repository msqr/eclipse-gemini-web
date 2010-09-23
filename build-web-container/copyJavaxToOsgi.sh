# copies the results of a web container build (ant clean collect) to the relevant directories of the given OSGi trunk
REPO=$1/licensed/repo
SRC=target/bundles
cp $SRC/com.springsource.javax.activation-1.1.0.jar $REPO/com.springsource.javax.activation/com.springsource.javax.activation-1.1.0.jar
cp $SRC/com.springsource.javax.annotation-1.0.0.jar $REPO/com.springsource.javax.annotation/com.springsource.javax.annotation-1.0.0.jar
cp $SRC/com.springsource.javax.ejb-3.0.0.jar $REPO/com.springsource.javax.ejb/com.springsource.javax.ejb-3.0.0.jar
cp $SRC/com.springsource.javax.el-1.0.0.jar $REPO/com.springsource.javax.el/com.springsource.javax.el-1.0.0.jar
cp $SRC/com.springsource.javax.persistence-1.0.0.jar $REPO/com.springsource.javax.persistence/com.springsource.javax.persistence-1.0.0.jar
cp $SRC/com.springsource.javax.servlet-2.5.0.jar $REPO/com.springsource.javax.servlet/com.springsource.javax.servlet-2.5.0.jar
cp $SRC/com.springsource.javax.servlet.jsp-2.1.0.jar $REPO/com.springsource.javax.servlet.jsp/com.springsource.javax.servlet.jsp-2.1.0.jar
cp $SRC/com.springsource.javax.transaction-1.1.0.jar $REPO/com.springsource.javax.transaction/com.springsource.javax.transaction-1.1.0.jar
cp $SRC/com.springsource.javax.xml.bind-2.1.7.jar $REPO/com.springsource.javax.xml.bind/com.springsource.javax.xml.bind-2.1.7.jar
cp $SRC/com.springsource.javax.xml.rpc-1.1.0.jar $REPO/com.springsource.javax.xml.rpc/com.springsource.javax.xml.rpc-1.1.0.jar
cp $SRC/com.springsource.javax.xml.soap-1.3.0.jar $REPO/com.springsource.javax.xml.soap/com.springsource.javax.xml.soap-1.3.0.jar
cp $SRC/com.springsource.javax.xml.stream-1.0.1.jar $REPO/com.springsource.javax.xml.stream/com.springsource.javax.xml.stream-1.0.1.jar