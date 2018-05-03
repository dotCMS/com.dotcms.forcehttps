package com.dotcms.tuckey;

import com.dotcms.repackage.org.apache.logging.log4j.LogManager;
import com.dotcms.repackage.org.apache.logging.log4j.core.LoggerContext;
import com.dotcms.repackage.org.tuckey.web.filters.urlrewrite.Condition;
import com.dotcms.repackage.org.tuckey.web.filters.urlrewrite.NormalRule;


import com.dotmarketing.loggers.Log4jUtil;
import com.dotmarketing.osgi.GenericBundleActivator;

import org.osgi.framework.BundleContext;


public class Activator extends GenericBundleActivator {

    private LoggerContext pluginLoggerContext;

    @SuppressWarnings ("unchecked")
    public void start ( BundleContext context ) throws Exception {

        //Initializing log4j...
        LoggerContext dotcmsLoggerContext = Log4jUtil.getLoggerContext();
        //Initialing the log4j context of this plugin based on the dotCMS logger context
        pluginLoggerContext = (LoggerContext) LogManager
                .getContext(this.getClass().getClassLoader(),
                        false,
                        dotcmsLoggerContext,
                        dotcmsLoggerContext.getConfigLocation());

        //Initializing services...
        initializeServices( context );
        
        //Creating a conditional rule
        NormalRule rule = new NormalRule();
        
        Condition condition = new Condition();
        condition.setType("header");
        condition.setOperator("notequal");
        condition.setName("x-forwarded-proto");
        condition.setValue("https");
        condition.initialise();
        rule.addCondition(condition);
        
        Condition scheme = new Condition();
        scheme.setType("scheme");
        scheme.setOperator("equal");
        scheme.setName("http");
        scheme.setValue("^http$");
        scheme.initialise();
        rule.addCondition(scheme);
        
        rule.setFrom("^.*$");
        rule.setToLast("true");
        rule.setToType("permanent-redirect");
        rule.setTo("https://%{server-name}%{request-uri}");


        addRewriteRule( rule );
    }

    public void stop ( BundleContext context ) throws Exception {

        //Unregister all the bundle services
        unregisterServices( context );

        //Shutting down log4j in order to avoid memory leaks
        Log4jUtil.shutdown(pluginLoggerContext);
    }

}