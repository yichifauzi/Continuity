package me.pepperbell.continuity.api.client;

public interface CTMLoader<T extends CTMProperties> {
	CTMPropertiesFactory<T> getPropertiesFactory();

	QuadProcessorFactory<T> getProcessorFactory();

	CachingPredicatesFactory<T> getPredicatesFactory();
}
