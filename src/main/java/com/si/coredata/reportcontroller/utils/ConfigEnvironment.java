package com.si.coredata.reportcontroller.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Service;

@Service
public class ConfigEnvironment {
 
	@Autowired
	static Environment springEnvironment;

	public static Map<String, Object> getAllKnownProperties() {
		Map<String, Object> rtn = new HashMap<>();
		if (springEnvironment instanceof ConfigurableEnvironment) {
			for (PropertySource<?> propertySource : ((ConfigurableEnvironment) springEnvironment)
					.getPropertySources()) {
				if (propertySource instanceof EnumerablePropertySource) {
					for (String key : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {
						rtn.put(key, propertySource.getProperty(key));
					}
				}
			}
		}
		return rtn;
	}

}
