
package org.sparrow.proxy.registry;

import org.sparrow.common.annotation.SparrowReference;
import org.sparrow.proxy.SparrowProxy;
import org.sparrow.proxy.SparrowProxyFactory;
import org.sparrow.utils.NettyChannelLRUMap;
import org.sparrow.xsd.Reference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Set;


/**
 * @ClassName SpringBeanRegistry
 * @Author leo
 * @Description //TODO
 * @Date: 2019/1/5 17:03
 **/

@Component
public class SpringBeanRegistry implements ApplicationContextAware, BeanDefinitionRegistryPostProcessor {

    private static ApplicationContext ctx;
    private static final String SPARROW = "org.sparrow";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        /**
         *        需要被代理的接口
         *        支持两种模式
         *        1.sparrow命名空间
         *        2.SparrowReference 注解的类
         */
        Set<Class<?>> clazzs = NettyChannelLRUMap.ClassUtil.getClasses(SPARROW);
        clazzs.forEach(cls -> {
            SparrowReference reference = cls.getAnnotation(SparrowReference.class);
            if (reference != null) {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(cls);
                GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
                definition.getPropertyValues().add("interfaceClass", definition.getBeanClassName());
                definition.setBeanClass(SparrowProxy.class);
                definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
                beanDefinitionRegistry.registerBeanDefinition(reference.name(), definition);
            }
        });
        String[] names = ctx.getBeanNamesForType(Reference.class);
        for (String name : names) {
            Reference reference = (Reference) ctx.getBean(name);
            String serviceName = reference.getInterfaces();
            Class clazz = null;
            try {
                clazz = Class.forName(serviceName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
            GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
            definition.getPropertyValues().add("interfaceClass", definition.getBeanClassName());
            definition.setBeanClass(SparrowProxyFactory.class);
            definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
            // 注册bean名,一般为类名首字母小写
            beanDefinitionRegistry.registerBeanDefinition(name, definition);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;
    }

}

