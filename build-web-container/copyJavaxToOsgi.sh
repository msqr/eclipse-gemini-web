# copies the results of a web container build (ant clean collect) to the relevant directories of the given OSGi trunk
REPO=$1/licensed/repo
SRC=target/bundles
cp $SRC/com.springsource.javax.activation-1.1.0.jar $REPO/com.springsource.javax.activation/com.springsource.javax.activation-1.1.0.jar
cp $SRC/javax.annotation-1.1.0.v201105051105.jar $REPO/javax.annotation/javax.annotation-1.1.0.v201105051105.jar
cp $SRC/com.springsource.javax.ejb-3.0.0.jar $REPO/com.springsource.javax.ejb/com.springsource.javax.ejb-3.0.0.jar
cp $SRC/javax.el-2.2.0.v201105051105.jar $REPO/javax.el/javax.el-2.2.0.v201105051105.jar
cp $SRC/com.springsource.javax.persistence-1.0.0.jar $REPO/com.springsource.javax.persistence/com.springsource.javax.persistence-1.0.0.jar
cp $SRC/javax.servlet-3.0.0.v201103241009.jar $REPO/javax.servlet/javax.servlet-3.0.0.v201103241009.jar
cp $SRC/javax.servlet.jsp-2.2.0.v201103241009.jar $REPO/javax.servlet.jsp/javax.servlet.jsp-2.2.0.v201103241009.jar
cp $SRC/com.springsource.javax.transaction-1.1.0.jar $REPO/com.springsource.javax.transaction/com.springsource.javax.transaction-1.1.0.jar
cp $SRC/com.springsource.javax.xml.bind-2.1.7.jar $REPO/com.springsource.javax.xml.bind/com.springsource.javax.xml.bind-2.1.7.jar
cp $SRC/com.springsource.javax.xml.rpc-1.1.0.v20110517.jar $REPO/com.springsource.javax.xml.rpc/com.springsource.javax.xml.rpc-1.1.0.v20110517.jar
cp $SRC/com.springsource.javax.xml.soap-1.3.0.jar $REPO/com.springsource.javax.xml.soap/com.springsource.javax.xml.soap-1.3.0.jar
cp $SRC/com.springsource.javax.xml.stream-1.0.1.jar $REPO/com.springsource.javax.xml.stream/com.springsource.javax.xml.stream-1.0.1.jar