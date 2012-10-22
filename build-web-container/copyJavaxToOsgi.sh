# copies the results of a web container build (ant clean collect) to the relevant directories of the given OSGi trunk
REPO=$1/licensed/repo
SRC=target/bundles
cp $SRC/com.springsource.javax.activation-1.1.0.jar $REPO/com.springsource.javax.activation/com.springsource.javax.activation-1.1.0.jar
cp $SRC/javax.annotation-1.1.0.v201108011116.jar $REPO/javax.annotation/javax.annotation-1.1.0.v201108011116.jar
cp $SRC/javax.ejb-3.1.1.v201204261316.jar $REPO/javax.ejb/javax.ejb-3.1.1.v201204261316.jar
cp $SRC/javax.el-2.2.0.v201108011116.jar $REPO/javax.el/javax.el-2.2.0.v201108011116.jar
cp $SRC/javax.mail-1.4.0.v201005080615.jar $REPO/javax.mail/javax.mail-1.4.0.v201005080615.jar
cp $SRC/javax.persistence-2.0.3.v201010191057.jar $REPO/javax.persistence/javax.persistence-2.0.3.v201010191057.jar
cp $SRC/javax.servlet-3.0.0.v201103241009.jar $REPO/javax.servlet/javax.servlet-3.0.0.v201103241009.jar
cp $SRC/javax.servlet.jsp-2.2.0.v201112011158.jar $REPO/javax.servlet.jsp/javax.servlet.jsp-2.2.0.v201112011158.jar
cp $SRC/javax.transaction-1.1.0.v201205091237.jar $REPO/javax.transaction/javax.transaction-1.1.0.v201205091237.jar
cp $SRC/javax.xml.rpc-1.1.0.v201005080400.jar $REPO/javax.xml.rpc/javax.xml.rpc-1.1.0.v201005080400.jar
cp $SRC/javax.xml.soap-1.3.0.v201105210645.jar $REPO/javax.xml.soap/javax.xml.soap-1.3.0.v201105210645.jar
