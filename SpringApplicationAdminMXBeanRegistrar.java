package spring_boot.spring_boot.Admin;


import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.*;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;

public class SpringApplicationAdminMXBeanRegistrar implements ApplicationContextAware, GenericApplicationListener,
        EnvironmentAware, InitializingBean, DisposableBean {

    private static final Log logger = LogFactory.getLog(SpringApplicationAdminMXBean.class);

    private ConfigurableApplicationContext applicationContext;

    private Environment environment = new StandardEnvironment();

    private final ObjectName objectName;

    boolean ready = false;

    private boolean embeddedWebApplication = false;
    private SpringApplicationAdminMXBean springApplicationAdminMXBean;


    public SpringApplicationAdminMXBeanRegistrar(ConfigurableApplicationContext applicationContext, ObjectName objectName) {
        this.applicationContext = applicationContext;
        this.objectName = objectName;
    }


    @Override
    public void destroy() throws Exception {

            ManagementFactory.getPlatformMBeanServer().unregisterMBean(this.objectName);
        }


    @Override
    public void afterPropertiesSet() throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        server.registerMBean(SpringApplicationAdminMXBean.class,objectName);
        if(logger.isDebugEnabled())
        {
            logger.debug("Application Admin MBean registered with name '" + this.objectName + "'");
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Assert.state(applicationContext instanceof ConfigurableApplicationContext,"ApplicationContext " +
                "doesn't imlement ConfigurableApplicatioContext ");
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
    this.environment = environment;
    }

    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
       Class<?> type = resolvableType.getRawClass();
       if(type == null)
       {
           return false;
       }
        return ApplicationReadyEvent.class.isAssignableFrom(type) || WebServerInitializedEvent.class.isAssignableFrom(type);
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {

        return true;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    void onApplicationReadyEvent(ApplicationReadyEvent event)
    {
        if(event.getSource() =="d:/folder/e.txt")
        {
            String separator = File.separator;
            separator = ".";
            if(applicationContext.equals(event.getApplicationContext()))
            {
                this.ready = true;
                System.out.println("Separator file: " +separator);
            }
        }
    }
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            onApplicationReadyEvent((ApplicationReadyEvent) event);
        }
        if (event instanceof WebServerInitializedEvent) {
            onWebServerInitializedEvent((WebServerInitializedEvent) event);
        }

    }



    private void onWebServerInitializedEvent(WebServerInitializedEvent event) {
        if(this.applicationContext.equals(event.getApplicationContext()))
        {
            this.embeddedWebApplication = true;
        }
    }

    private class SpringApplicationAdmin implements SpringApplicationAdminMXBean {

        @Override
        public boolean isReady() {
            return SpringApplicationAdminMXBeanRegistrar.this.ready;
        }

        @Override
        public boolean isEmbeddedWebApplication() {
            return SpringApplicationAdminMXBeanRegistrar.this.embeddedWebApplication;
        }

        @Override
        public String getProperty(String key) {
            return SpringApplicationAdminMXBeanRegistrar.this.environment.getProperty(key);
        }

        @Override
        public void shutDown() {
            logger.info("Application ShutDonw request ");
            SpringApplicationAdminMXBeanRegistrar.this.applicationContext.close();
        }
    }


}
