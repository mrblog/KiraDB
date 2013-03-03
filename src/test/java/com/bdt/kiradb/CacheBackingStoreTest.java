package com.bdt.kiradb;

import com.bdt.kiradb.mykdbapp.Person;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CacheBackingStoreTest {

	@Test
	public void testCachingDefault() throws KiraException {
		CacheBackingStore cache = new CacheBackingStore();
		
		assertNotNull("cache should not be null", cache);
		
		Person p = new Person();
		p.setAccount("12036");
		p.setName("Sam Spade");
		p.setCreatedAt(new Date());
		System.out.println("Writing person...");
		cache.storeObject(null, p);
		System.out.println("Reading person...");
		Person np = cache.retrieveObject(null, p, p.getAccount());
		System.out.println("Read object: " + np.getName());
		assertNotNull("The result should not be null", np);
		assertEquals("The person's name when read is not the same as when written", p.getName(), np.getName());

	}
}
