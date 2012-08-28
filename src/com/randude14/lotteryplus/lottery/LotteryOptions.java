package com.randude14.lotteryplus.lottery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.randude14.lotteryplus.configuration.Config;
import com.randude14.lotteryplus.configuration.Property;

public class LotteryOptions {
	private final Map<String, Object> options = new HashMap<String, Object>();
	
	public LotteryOptions(Map<String, Object> map) {
		this.options.putAll(map);
	}
	
	public boolean contains(String key) {
		return options.containsKey(key);
	}
	
	public void remove(String key) {
		options.remove(key);
	}
	
	public <T> T get(Property<T> property, T value) {
		return get(property.getName(), value);
	}
	
	public <T> T get(Property<T> property) {
		return get(property.getName(), Config.getProperty(property));
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String path, T def) {
		return get(path, (Class<T>)def.getClass(), def);
	}
	
	private <T> T get(String path, Class<T> clazz, T def) {
		Object value = options.get(path);
		if(value != null) {
			try {
				if(clazz == Long.class) {
					value = ((Number) value).longValue();
				}
			    return clazz.cast(value);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return def;
	}
	
	public void set(Property<?> property, Object value) {
		set(property.getName(), value);
	}
	
	public void set(String path, Object value) {
		options.put(path, value);
	}
	
	public Set<String> keySet() {
		return options.keySet();
	}
	
	public Map<String, Object> getValues() {
		return options;
	}
}
