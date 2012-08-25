package com.randude14.lotteryplus.configuration;

public class Property<T> {
	private final String path;
	private final T value;
	
	protected Property(String path, T t) {
		this.path = path;
		this.value = t;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getName() {
		int index = path.lastIndexOf('.');
		if(index < 0)
			return path;
		return path.substring(index+1);
	}
	
	public T getDefaultValue() {
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getValueClass() {
		return (Class<T>) value.getClass();
	}
}
