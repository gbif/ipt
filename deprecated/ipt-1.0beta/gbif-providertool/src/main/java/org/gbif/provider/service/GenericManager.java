package org.gbif.provider.service;

import java.util.Collection;
import java.util.List;


public interface GenericManager<T>{
    List<Long> getAllIds();
    List<T> getAll();
	/**
	 * Gets all records without duplicates.
	 * @See GenericDao.getAllDistinct()
	 * @return
	 */
	List<T> getAllDistinct();
    T get(Long id);
    boolean exists(Long id);

    T save(T object);
	void saveAll(Collection<T> objs);
    void remove(Long id);
	void remove(T obj);

	void flush();
	public void debugSession();
}
