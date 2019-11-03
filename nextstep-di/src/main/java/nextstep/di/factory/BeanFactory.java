package nextstep.di.factory;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BeanFactory {
    private static final Logger logger = LoggerFactory.getLogger(BeanFactory.class);

    private Set<Class<?>> preInstanticateBeans;

    private Map<Class<?>, Object> beans = Maps.newHashMap();

    public BeanFactory(Set<Class<?>> preInstanticateBeans) {
        this.preInstanticateBeans = preInstanticateBeans;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        return (T) beans.get(requiredType);
    }

    public void initialize() throws NoSuchMethodException {
        for (Class<?> bean : preInstanticateBeans) {
            beans.put(bean, createInstance(bean));
        }
    }

    private Object createInstance(Class<?> clazz) throws NoSuchMethodException {
        if (isBeanExists(clazz)) {
            return getBean(clazz);
        }
        Constructor constructor = getConstructor(clazz);
        List<Object> paramInstances = initParameters(constructor);
        return BeanUtils.instantiateClass(constructor, paramInstances.toArray());
    }

    private boolean isBeanExists(Class<?> clazz) {
        return getBean(clazz) != null;
    }

    private Constructor getConstructor(Class<?> clazz) throws NoSuchMethodException {
        Constructor injectedConstructor = BeanFactoryUtils.getInjectedConstructor(clazz);
        if (injectedConstructor == null) {
            return clazz.getDeclaredConstructor();
        }
        return injectedConstructor;

    }

    private List<Object> initParameters(Constructor constructor) throws NoSuchMethodException {
        Parameter[] parameters = constructor.getParameters();
        List<Object> paramInstances = new ArrayList<>();
        for (Parameter parameter : parameters) {
            Class<?> clazz = BeanFactoryUtils.findConcreteClass(parameter.getType(), preInstanticateBeans);
            paramInstances.add(createInstance(clazz));
        }
        return paramInstances;
    }
}
