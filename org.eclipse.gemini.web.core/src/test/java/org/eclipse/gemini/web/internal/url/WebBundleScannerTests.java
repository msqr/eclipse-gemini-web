/*******************************************************************************
 * Copyright (c) 2009, 2010 VMware Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at 
 *   http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.  
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.gemini.web.internal.url;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;

import org.easymock.EasyMock;
import org.junit.Test;

import org.eclipse.gemini.web.internal.url.WebBundleScanner;
import org.eclipse.gemini.web.internal.url.WebBundleScannerCallback;
import org.eclipse.virgo.util.io.JarUtils;
import org.eclipse.virgo.util.io.PathReference;

public class WebBundleScannerTests {

	private static final File WAR_FILE = new File("src/test/resources/simple-war.war");

    @Test
    public void testScanLib() throws IOException {
        WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);
        
        setExpectations(callback);
                
        replay(callback);
        
        WebBundleScanner scanner = new WebBundleScanner(WAR_FILE.toURI().toURL(), callback);
        scanner.scanWar();
        
        verify(callback);        
    }
    
    private void setExpectations(WebBundleScannerCallback callback) {
        callback.jarFound("WEB-INF/lib/com.springsource.ch.qos.logback.classic-0.9.18.jar");
        callback.jarFound("WEB-INF/lib/com.springsource.ch.qos.logback.core-0.9.18.jar");
        callback.jarFound("WEB-INF/lib/com.springsource.slf4j.api-1.5.10.jar");
        
        callback.classFound("foo/A.class");
        callback.classFound("foo/bar/C.class");
        callback.classFound("goo/B.class");
        callback.classFound("D.class");
    }
    
    private void setExpectationsIncludingNestedJars(WebBundleScannerCallback callback) {
        setExpectations(callback);
        
        callback.classFound("ch/qos/logback/classic/BasicConfigurator.class");
        callback.classFound("ch/qos/logback/classic/boolex/JaninoEventEvaluator.class");
        callback.classFound("ch/qos/logback/classic/boolex/OnErrorEvaluator.class");
        callback.classFound("ch/qos/logback/classic/ClassicConstants.class");
        callback.classFound("ch/qos/logback/classic/db/DBAppender.class");
        callback.classFound("ch/qos/logback/classic/db/DBHelper.class");
        callback.classFound("ch/qos/logback/classic/filter/LevelFilter.class");
        callback.classFound("ch/qos/logback/classic/filter/ThresholdFilter.class");
        callback.classFound("ch/qos/logback/classic/html/DefaultCssBuilder.class");
        callback.classFound("ch/qos/logback/classic/html/DefaultThrowableRenderer.class");
        callback.classFound("ch/qos/logback/classic/html/HTMLLayout.class");
        callback.classFound("ch/qos/logback/classic/html/UrlCssBuilder.class");
        callback.classFound("ch/qos/logback/classic/jmx/JMXConfigurator.class");
        callback.classFound("ch/qos/logback/classic/jmx/JMXConfiguratorMBean.class");
        callback.classFound("ch/qos/logback/classic/jmx/MBeanUtil.class");
        callback.classFound("ch/qos/logback/classic/joran/action/ConfigurationAction.class");
        callback.classFound("ch/qos/logback/classic/joran/action/ConsolePluginAction.class");
        callback.classFound("ch/qos/logback/classic/joran/action/ContextNameAction.class");
        callback.classFound("ch/qos/logback/classic/joran/action/EvaluatorAction.class");
        callback.classFound("ch/qos/logback/classic/joran/action/InsertFromJNDIAction.class");
        callback.classFound("ch/qos/logback/classic/joran/action/JMXConfiguratorAction.class");
        callback.classFound("ch/qos/logback/classic/joran/action/LevelAction.class");
        callback.classFound("ch/qos/logback/classic/joran/action/LoggerAction.class");
        callback.classFound("ch/qos/logback/classic/joran/action/RootLoggerAction.class");
        callback.classFound("ch/qos/logback/classic/joran/JoranConfigurator.class");
        callback.classFound("ch/qos/logback/classic/Level.class");
        callback.classFound("ch/qos/logback/classic/log4j/XMLLayout.class");
        callback.classFound("ch/qos/logback/classic/Logger.class");
        callback.classFound("ch/qos/logback/classic/LoggerContext.class");
        callback.classFound("ch/qos/logback/classic/net/JMSQueueAppender.class");
        callback.classFound("ch/qos/logback/classic/net/JMSQueueSink.class");
        callback.classFound("ch/qos/logback/classic/net/JMSTopicAppender.class");
        callback.classFound("ch/qos/logback/classic/net/JMSTopicSink.class");
        callback.classFound("ch/qos/logback/classic/net/LoggingEventPreSerializationTransformer.class");
        callback.classFound("ch/qos/logback/classic/net/SimpleSocketServer.class");
        callback.classFound("ch/qos/logback/classic/net/SMTPAppender.class");
        callback.classFound("ch/qos/logback/classic/net/SocketAcceptor.class");
        callback.classFound("ch/qos/logback/classic/net/SocketAppender.class");
        callback.classFound("ch/qos/logback/classic/net/SocketNode.class");
        callback.classFound("ch/qos/logback/classic/net/SyslogAppender.class");
        callback.classFound("ch/qos/logback/classic/pattern/Abbreviator.class");
        callback.classFound("ch/qos/logback/classic/pattern/CallerDataConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/ClassicConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/ClassNameOnlyAbbreviator.class");
        callback.classFound("ch/qos/logback/classic/pattern/ClassOfCallerConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/ContextNameConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/DateConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/EnsureExceptionHandling.class");
        callback.classFound("ch/qos/logback/classic/pattern/ExtendedThrowableProxyConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/FileOfCallerConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/LevelConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/LineOfCallerConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/LineSeparatorConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/LoggerConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/LRUCache.class");
        callback.classFound("ch/qos/logback/classic/pattern/MarkerConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/MDCConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/MessageConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/MethodOfCallerConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/NamedConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/NopThrowableInformationConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/PropertyConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/RelativeTimeConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/SyslogStartConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/TargetLengthBasedClassNameAbbreviator.class");
        callback.classFound("ch/qos/logback/classic/pattern/ThreadConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/ThrowableHandlingConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/ThrowableProxyConverter.class");
        callback.classFound("ch/qos/logback/classic/pattern/Util.class");
        callback.classFound("ch/qos/logback/classic/PatternLayout.class");
        callback.classFound("ch/qos/logback/classic/selector/ContextJNDISelector.class");
        callback.classFound("ch/qos/logback/classic/selector/ContextSelector.class");
        callback.classFound("ch/qos/logback/classic/selector/DefaultContextSelector.class");
        callback.classFound("ch/qos/logback/classic/selector/servlet/ContextDetachingSCL.class");
        callback.classFound("ch/qos/logback/classic/selector/servlet/LoggerContextFilter.class");
        callback.classFound("ch/qos/logback/classic/sift/AppenderFactory.class");
        callback.classFound("ch/qos/logback/classic/sift/ContextBasedDiscriminator.class");
        callback.classFound("ch/qos/logback/classic/sift/MDCBasedDiscriminator.class");
        callback.classFound("ch/qos/logback/classic/sift/SiftAction.class");
        callback.classFound("ch/qos/logback/classic/sift/SiftingAppender.class");
        callback.classFound("ch/qos/logback/classic/sift/SiftingJoranConfigurator.class");
        callback.classFound("ch/qos/logback/classic/spi/CallerData.class");
        callback.classFound("ch/qos/logback/classic/spi/ClassPackagingData.class");
        callback.classFound("ch/qos/logback/classic/spi/ILoggingEvent.class");
        callback.classFound("ch/qos/logback/classic/spi/IThrowableProxy.class");
        callback.classFound("ch/qos/logback/classic/spi/LoggerComparator.class");
        callback.classFound("ch/qos/logback/classic/spi/LoggerContextAware.class");
        callback.classFound("ch/qos/logback/classic/spi/LoggerContextAwareBase.class");
        callback.classFound("ch/qos/logback/classic/spi/LoggerContextListener.class");
        callback.classFound("ch/qos/logback/classic/spi/LoggerContextVO.class");
        callback.classFound("ch/qos/logback/classic/spi/LoggerRemoteView.class");
        callback.classFound("ch/qos/logback/classic/spi/LoggingEvent.class");
        callback.classFound("ch/qos/logback/classic/spi/LoggingEventVO.class");
        callback.classFound("ch/qos/logback/classic/spi/PackagingDataCalculator.class");
        callback.classFound("ch/qos/logback/classic/spi/PlatformInfo.class");
        callback.classFound("ch/qos/logback/classic/spi/StackTraceElementProxy.class");
        callback.classFound("ch/qos/logback/classic/spi/STEUtil.class");
        callback.classFound("ch/qos/logback/classic/spi/ThrowableProxy.class");
        callback.classFound("ch/qos/logback/classic/spi/ThrowableProxyUtil.class");
        callback.classFound("ch/qos/logback/classic/spi/ThrowableProxyVO.class");
        callback.classFound("ch/qos/logback/classic/spi/TurboFilterList.class");
        callback.classFound("ch/qos/logback/classic/turbo/DuplicateMessageFilter.class");
        callback.classFound("ch/qos/logback/classic/turbo/DynamicThresholdFilter.class");
        callback.classFound("ch/qos/logback/classic/turbo/LRUMessageCache.class");
        callback.classFound("ch/qos/logback/classic/turbo/MarkerFilter.class");
        callback.classFound("ch/qos/logback/classic/turbo/MatchingFilter.class");
        callback.classFound("ch/qos/logback/classic/turbo/MDCFilter.class");
        callback.classFound("ch/qos/logback/classic/turbo/MDCValueLevelPair.class");
        callback.classFound("ch/qos/logback/classic/turbo/ReconfigureOnChangeFilter$ReconfiguringThread.class");
        callback.classFound("ch/qos/logback/classic/turbo/ReconfigureOnChangeFilter.class");
        callback.classFound("ch/qos/logback/classic/turbo/TurboFilter.class");
        callback.classFound("ch/qos/logback/classic/util/ContextInitializer.class");
        callback.classFound("ch/qos/logback/classic/util/DefaultNestedComponentRules.class");
        callback.classFound("ch/qos/logback/classic/util/JNDIUtil.class");
        callback.classFound("ch/qos/logback/classic/util/LevelToSyslogSeverity.class");
        callback.classFound("ch/qos/logback/classic/util/LoggerStatusPrinter.class");
        callback.classFound("ch/qos/logback/classic/util/StatusListenerConfigHelper.class");
        callback.classFound("ch/qos/logback/classic/ViewStatusMessagesServlet.class");
        callback.classFound("org/slf4j/impl/CopyOnInheritThreadLocal.class");
        callback.classFound("org/slf4j/impl/LogbackMDCAdapter.class");
        callback.classFound("org/slf4j/impl/StaticLoggerBinder.class");
        callback.classFound("org/slf4j/impl/StaticMarkerBinder.class");
        callback.classFound("org/slf4j/impl/StaticMDCBinder.class");
        callback.classFound("ch/qos/logback/core/Appender.class");
        callback.classFound("ch/qos/logback/core/AppenderBase.class");
        callback.classFound("ch/qos/logback/core/BasicStatusManager.class");
        callback.classFound("ch/qos/logback/core/boolex/EvaluationException.class");
        callback.classFound("ch/qos/logback/core/boolex/EventEvaluator.class");
        callback.classFound("ch/qos/logback/core/boolex/EventEvaluatorBase.class");
        callback.classFound("ch/qos/logback/core/boolex/JaninoEventEvaluatorBase.class");
        callback.classFound("ch/qos/logback/core/boolex/Matcher.class");
        callback.classFound("ch/qos/logback/core/ConsoleAppender.class");
        callback.classFound("ch/qos/logback/core/Context.class");
        callback.classFound("ch/qos/logback/core/ContextBase.class");
        callback.classFound("ch/qos/logback/core/CoreConstants.class");
        callback.classFound("ch/qos/logback/core/db/BindDataSourceToJNDIAction.class");
        callback.classFound("ch/qos/logback/core/db/ConnectionSource.class");
        callback.classFound("ch/qos/logback/core/db/ConnectionSourceBase.class");
        callback.classFound("ch/qos/logback/core/db/DataSourceConnectionSource.class");
        callback.classFound("ch/qos/logback/core/db/DBAppenderBase.class");
        callback.classFound("ch/qos/logback/core/db/DBHelper.class");
        callback.classFound("ch/qos/logback/core/db/dialect/DBUtil$1.class");
        callback.classFound("ch/qos/logback/core/db/dialect/DBUtil.class");
        callback.classFound("ch/qos/logback/core/db/dialect/HSQLDBDialect.class");
        callback.classFound("ch/qos/logback/core/db/dialect/MsSQLDialect.class");
        callback.classFound("ch/qos/logback/core/db/dialect/MySQLDialect.class");
        callback.classFound("ch/qos/logback/core/db/dialect/OracleDialect.class");
        callback.classFound("ch/qos/logback/core/db/dialect/PostgreSQLDialect.class");
        callback.classFound("ch/qos/logback/core/db/dialect/SQLDialect.class");
        callback.classFound("ch/qos/logback/core/db/dialect/SQLDialectCode.class");
        callback.classFound("ch/qos/logback/core/db/DriverManagerConnectionSource.class");
        callback.classFound("ch/qos/logback/core/db/JNDIConnectionSource.class");
        callback.classFound("ch/qos/logback/core/FileAppender.class");
        callback.classFound("ch/qos/logback/core/filter/AbstractMatcherFilter.class");
        callback.classFound("ch/qos/logback/core/filter/EvaluatorFilter.class");
        callback.classFound("ch/qos/logback/core/filter/Filter.class");
        callback.classFound("ch/qos/logback/core/helpers/CyclicBuffer.class");
        callback.classFound("ch/qos/logback/core/helpers/ThrowableToStringArray.class");
        callback.classFound("ch/qos/logback/core/helpers/Transform.class");
        callback.classFound("ch/qos/logback/core/html/CssBuilder.class");
        callback.classFound("ch/qos/logback/core/html/HTMLLayoutBase.class");
        callback.classFound("ch/qos/logback/core/html/IThrowableRenderer.class");
        callback.classFound("ch/qos/logback/core/html/NOPThrowableRenderer.class");
        callback.classFound("ch/qos/logback/core/joran/action/AbstractEventEvaluatorAction.class");
        callback.classFound("ch/qos/logback/core/joran/action/Action.class");
        callback.classFound("ch/qos/logback/core/joran/action/ActionConst.class");
        callback.classFound("ch/qos/logback/core/joran/action/AppenderAction.class");
        callback.classFound("ch/qos/logback/core/joran/action/AppenderRefAction.class");
        callback.classFound("ch/qos/logback/core/joran/action/ContextPropertyAction.class");
        callback.classFound("ch/qos/logback/core/joran/action/ConversionRuleAction.class");
        callback.classFound("ch/qos/logback/core/joran/action/IADataForBasicProperty.class");
        callback.classFound("ch/qos/logback/core/joran/action/IADataForComplexProperty.class");
        callback.classFound("ch/qos/logback/core/joran/action/ImplicitAction.class");
        callback.classFound("ch/qos/logback/core/joran/action/IncludeAction.class");
        callback.classFound("ch/qos/logback/core/joran/action/NestedBasicPropertyIA$1.class");
        callback.classFound("ch/qos/logback/core/joran/action/NestedBasicPropertyIA.class");
        callback.classFound("ch/qos/logback/core/joran/action/NestedComplexPropertyIA$1.class");
        callback.classFound("ch/qos/logback/core/joran/action/NestedComplexPropertyIA.class");
        callback.classFound("ch/qos/logback/core/joran/action/NewRuleAction.class");
        callback.classFound("ch/qos/logback/core/joran/action/NOPAction.class");
        callback.classFound("ch/qos/logback/core/joran/action/ParamAction.class");
        callback.classFound("ch/qos/logback/core/joran/action/PropertyAction.class");
        callback.classFound("ch/qos/logback/core/joran/action/StatusListenerAction.class");
        callback.classFound("ch/qos/logback/core/joran/action/TimestampAction.class");
        callback.classFound("ch/qos/logback/core/joran/event/BodyEvent.class");
        callback.classFound("ch/qos/logback/core/joran/event/EndEvent.class");
        callback.classFound("ch/qos/logback/core/joran/event/InPlayListener.class");
        callback.classFound("ch/qos/logback/core/joran/event/SaxEvent.class");
        callback.classFound("ch/qos/logback/core/joran/event/SaxEventRecorder.class");
        callback.classFound("ch/qos/logback/core/joran/event/StartEvent.class");
        callback.classFound("ch/qos/logback/core/joran/GenericConfigurator.class");
        callback.classFound("ch/qos/logback/core/joran/JoranConfiguratorBase.class");
        callback.classFound("ch/qos/logback/core/joran/spi/ActionException.class");
        callback.classFound("ch/qos/logback/core/joran/spi/CAI_WithLocatorSupport.class");
        callback.classFound("ch/qos/logback/core/joran/spi/DefaultClass.class");
        callback.classFound("ch/qos/logback/core/joran/spi/DefaultNestedComponentRegistry.class");
        callback.classFound("ch/qos/logback/core/joran/spi/EventPlayer.class");
        callback.classFound("ch/qos/logback/core/joran/spi/HostClassAndPropertyDouble.class");
        callback.classFound("ch/qos/logback/core/joran/spi/InterpretationContext.class");
        callback.classFound("ch/qos/logback/core/joran/spi/Interpreter.class");
        callback.classFound("ch/qos/logback/core/joran/spi/JoranException.class");
        callback.classFound("ch/qos/logback/core/joran/spi/NoAutoStart.class");
        callback.classFound("ch/qos/logback/core/joran/spi/NoAutoStartUtil.class");
        callback.classFound("ch/qos/logback/core/joran/spi/Pattern.class");
        callback.classFound("ch/qos/logback/core/joran/spi/PropertySetter$1.class");
        callback.classFound("ch/qos/logback/core/joran/spi/PropertySetter.class");
        callback.classFound("ch/qos/logback/core/joran/spi/RuleStore.class");
        callback.classFound("ch/qos/logback/core/joran/spi/SimpleRuleStore.class");
        callback.classFound("ch/qos/logback/core/joran/spi/XMLUtil.class");
        callback.classFound("ch/qos/logback/core/layout/EchoLayout.class");
        callback.classFound("ch/qos/logback/core/Layout.class");
        callback.classFound("ch/qos/logback/core/LayoutBase.class");
        callback.classFound("ch/qos/logback/core/LogbackException.class");
        callback.classFound("ch/qos/logback/core/net/JMSAppenderBase.class");
        callback.classFound("ch/qos/logback/core/net/LoginAuthenticator.class");
        callback.classFound("ch/qos/logback/core/net/SMTPAppenderBase.class");
        callback.classFound("ch/qos/logback/core/net/SocketAppenderBase$Connector.class");
        callback.classFound("ch/qos/logback/core/net/SocketAppenderBase.class");
        callback.classFound("ch/qos/logback/core/net/SyslogAppenderBase.class");
        callback.classFound("ch/qos/logback/core/net/SyslogConstants.class");
        callback.classFound("ch/qos/logback/core/net/SyslogWriter.class");
        callback.classFound("ch/qos/logback/core/net/TelnetAppender.class");
        callback.classFound("ch/qos/logback/core/pattern/CompositeConverter.class");
        callback.classFound("ch/qos/logback/core/pattern/Converter.class");
        callback.classFound("ch/qos/logback/core/pattern/ConverterUtil.class");
        callback.classFound("ch/qos/logback/core/pattern/DynamicConverter.class");
        callback.classFound("ch/qos/logback/core/pattern/FormatInfo.class");
        callback.classFound("ch/qos/logback/core/pattern/FormattingConverter.class");
        callback.classFound("ch/qos/logback/core/pattern/LiteralConverter.class");
        callback.classFound("ch/qos/logback/core/pattern/parser/Compiler.class");
        callback.classFound("ch/qos/logback/core/pattern/parser/CompositeNode.class");
        callback.classFound("ch/qos/logback/core/pattern/parser/FormattingNode.class");
        callback.classFound("ch/qos/logback/core/pattern/parser/KeywordNode.class");
        callback.classFound("ch/qos/logback/core/pattern/parser/Node.class");
        callback.classFound("ch/qos/logback/core/pattern/parser/OptionTokenizer.class");
        callback.classFound("ch/qos/logback/core/pattern/parser/Parser.class");
        callback.classFound("ch/qos/logback/core/pattern/parser/ScanException.class");
        callback.classFound("ch/qos/logback/core/pattern/parser/Token.class");
        callback.classFound("ch/qos/logback/core/pattern/parser/TokenStream.class");
        callback.classFound("ch/qos/logback/core/pattern/PatternLayoutBase.class");
        callback.classFound("ch/qos/logback/core/pattern/PostCompileProcessor.class");
        callback.classFound("ch/qos/logback/core/pattern/SpacePadder.class");
        callback.classFound("ch/qos/logback/core/pattern/util/AlmostAsIsEscapeUtil.class");
        callback.classFound("ch/qos/logback/core/pattern/util/IEscapeUtil.class");
        callback.classFound("ch/qos/logback/core/pattern/util/RegularEscapeUtil.class");
        callback.classFound("ch/qos/logback/core/read/CyclicBufferAppender.class");
        callback.classFound("ch/qos/logback/core/read/ListAppender.class");
        callback.classFound("ch/qos/logback/core/rolling/DefaultTimeBasedFileNamingAndTriggeringPolicy.class");
        callback.classFound("ch/qos/logback/core/rolling/FixedWindowRollingPolicy$1.class");
        callback.classFound("ch/qos/logback/core/rolling/FixedWindowRollingPolicy.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/ArchiveRemover.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/AsynchronousCompressor.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/CompressionMode.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/CompressionRunnable.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/Compressor$1.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/Compressor.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/DatePatternToRegexUtil.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/DateTokenConverter.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/DefaultArchiveRemover.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/FileFilterUtil$1.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/FileFilterUtil$2.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/FileFilterUtil$3.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/FileFilterUtil.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/FileNamePattern.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/IntegerTokenConverter.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/MonoTypedConverter.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/PeriodicityType.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/RenameUtil.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/RollingCalendar$1.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/RollingCalendar.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/SequenceToRegex4SDF.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/SizeAndTimeBasedArchiveRemover.class");
        callback.classFound("ch/qos/logback/core/rolling/helper/TokenConverter.class");
        callback.classFound("ch/qos/logback/core/rolling/RollingFileAppender.class");
        callback.classFound("ch/qos/logback/core/rolling/RollingPolicy.class");
        callback.classFound("ch/qos/logback/core/rolling/RollingPolicyBase.class");
        callback.classFound("ch/qos/logback/core/rolling/RolloverFailure.class");
        callback.classFound("ch/qos/logback/core/rolling/SizeAndTimeBasedFNATP.class");
        callback.classFound("ch/qos/logback/core/rolling/SizeBasedTriggeringPolicy.class");
        callback.classFound("ch/qos/logback/core/rolling/TimeBasedFileNamingAndTriggeringPolicy.class");
        callback.classFound("ch/qos/logback/core/rolling/TimeBasedFileNamingAndTriggeringPolicyBase.class");
        callback.classFound("ch/qos/logback/core/rolling/TimeBasedRollingPolicy$1.class");
        callback.classFound("ch/qos/logback/core/rolling/TimeBasedRollingPolicy.class");
        callback.classFound("ch/qos/logback/core/rolling/TriggeringPolicy.class");
        callback.classFound("ch/qos/logback/core/rolling/TriggeringPolicyBase.class");
        callback.classFound("ch/qos/logback/core/sift/AppenderFactoryBase.class");
        callback.classFound("ch/qos/logback/core/sift/AppenderTracker.class");
        callback.classFound("ch/qos/logback/core/sift/AppenderTrackerImpl$Entry.class");
        callback.classFound("ch/qos/logback/core/sift/AppenderTrackerImpl.class");
        callback.classFound("ch/qos/logback/core/sift/Discriminator.class");
        callback.classFound("ch/qos/logback/core/sift/SiftingAppenderBase.class");
        callback.classFound("ch/qos/logback/core/sift/SiftingJoranConfiguratorBase.class");
        callback.classFound("ch/qos/logback/core/spi/AppenderAttachable.class");
        callback.classFound("ch/qos/logback/core/spi/AppenderAttachableImpl.class");
        callback.classFound("ch/qos/logback/core/spi/ContextAware.class");
        callback.classFound("ch/qos/logback/core/spi/ContextAwareBase.class");
        callback.classFound("ch/qos/logback/core/spi/ContextAwareImpl.class");
        callback.classFound("ch/qos/logback/core/spi/FilterAttachable.class");
        callback.classFound("ch/qos/logback/core/spi/FilterAttachableImpl.class");
        callback.classFound("ch/qos/logback/core/spi/FilterReply.class");
        callback.classFound("ch/qos/logback/core/spi/LifeCycle.class");
        callback.classFound("ch/qos/logback/core/spi/PreSerializationTransformer.class");
        callback.classFound("ch/qos/logback/core/spi/PropertyContainer.class");
        callback.classFound("ch/qos/logback/core/status/ErrorStatus.class");
        callback.classFound("ch/qos/logback/core/status/InfoStatus.class");
        callback.classFound("ch/qos/logback/core/status/OnConsoleStatusListener.class");
        callback.classFound("ch/qos/logback/core/status/Status.class");
        callback.classFound("ch/qos/logback/core/status/StatusBase.class");
        callback.classFound("ch/qos/logback/core/status/StatusListener.class");
        callback.classFound("ch/qos/logback/core/status/StatusListenerAsList.class");
        callback.classFound("ch/qos/logback/core/status/StatusManager.class");
        callback.classFound("ch/qos/logback/core/status/StatusUtil.class");
        callback.classFound("ch/qos/logback/core/status/ViewStatusMessagesServletBase.class");
        callback.classFound("ch/qos/logback/core/status/WarnStatus.class");
        callback.classFound("ch/qos/logback/core/UnsynchronizedAppenderBase$1.class");
        callback.classFound("ch/qos/logback/core/UnsynchronizedAppenderBase.class");
        callback.classFound("ch/qos/logback/core/util/AggregationType.class");
        callback.classFound("ch/qos/logback/core/util/ContentTypeUtil.class");
        callback.classFound("ch/qos/logback/core/util/Duration.class");
        callback.classFound("ch/qos/logback/core/util/DynamicClassLoadingException.class");
        callback.classFound("ch/qos/logback/core/util/FileSize.class");
        callback.classFound("ch/qos/logback/core/util/FileUtil.class");
        callback.classFound("ch/qos/logback/core/util/IncompatibleClassException.class");
        callback.classFound("ch/qos/logback/core/util/Loader.class");
        callback.classFound("ch/qos/logback/core/util/OptionHelper.class");
        callback.classFound("ch/qos/logback/core/util/PropertySetterException.class");
        callback.classFound("ch/qos/logback/core/util/StatusPrinter.class");
        callback.classFound("ch/qos/logback/core/util/SystemInfo.class");
        callback.classFound("ch/qos/logback/core/util/TimeUtil.class");
        callback.classFound("ch/qos/logback/core/WriterAppender.class");
        callback.classFound("org/slf4j/helpers/BasicMarker.class");
        callback.classFound("org/slf4j/helpers/BasicMarkerFactory.class");
        callback.classFound("org/slf4j/helpers/BasicMDCAdapter.class");
        callback.classFound("org/slf4j/helpers/MarkerIgnoringBase.class");
        callback.classFound("org/slf4j/helpers/MessageFormatter.class");
        callback.classFound("org/slf4j/helpers/NamedLoggerBase.class");
        callback.classFound("org/slf4j/helpers/NOPLogger.class");
        callback.classFound("org/slf4j/helpers/NOPMakerAdapter.class");
        callback.classFound("org/slf4j/helpers/SubstituteLoggerFactory.class");
        callback.classFound("org/slf4j/helpers/Util.class");
        callback.classFound("org/slf4j/ILoggerFactory.class");
        callback.classFound("org/slf4j/IMarkerFactory.class");
        callback.classFound("org/slf4j/Logger.class");
        callback.classFound("org/slf4j/LoggerFactory.class");
        callback.classFound("org/slf4j/Marker.class");
        callback.classFound("org/slf4j/MarkerFactory.class");
        callback.classFound("org/slf4j/MDC.class");
        callback.classFound("org/slf4j/spi/LocationAwareLogger.class");
        callback.classFound("org/slf4j/spi/LoggerFactoryBinder.class");
        callback.classFound("org/slf4j/spi/MarkerFactoryBinder.class");
        callback.classFound("org/slf4j/spi/MDCAdapter.class");
       

    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testScanLibIncludingNestedJars() throws IOException {
        
        WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);
        
        setExpectationsIncludingNestedJars(callback);
        
        replay(callback);
        
        WebBundleScanner scanner = new WebBundleScanner(WAR_FILE.toURL(), callback, true);
        scanner.scanWar();
        
        verify(callback);        
    }
    
    @Test
    public void testScanDir() throws Exception {
        PathReference pr = unpackToDir();
        try {
            WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);
            
            setExpectations(callback);
            
            replay(callback);
            
            WebBundleScanner scanner = new WebBundleScanner(pr.toURI().toURL(), callback);            
            scanner.scanWar();
            
            verify(callback);
        } finally {
            pr.delete(true);
        }
    }
    
    @Test
    public void testScanDirIncludingNestedJars() throws Exception {
        PathReference pr = unpackToDir();
        try {
            WebBundleScannerCallback callback = EasyMock.createMock(WebBundleScannerCallback.class);
            
            setExpectationsIncludingNestedJars(callback);
            
            replay(callback);
            
            WebBundleScanner scanner = new WebBundleScanner(pr.toURI().toURL(), callback, true);            
            scanner.scanWar();
            
            verify(callback);
        } finally {
            pr.delete(true);
        }
    }
    
    private PathReference unpackToDir() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        PathReference dest = new PathReference(new File(tmpDir, "unpack-" + System.currentTimeMillis()));
        PathReference src = new PathReference(WAR_FILE);
        JarUtils.unpackTo(src, dest);
        return dest;
    }
}
