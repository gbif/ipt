package org.gbif.scheduler;

import java.util.Map;

import org.gbif.scheduler.scheduler.Launchable;

public class MockJob implements Launchable{
	private MockInstance mockInstance;
	
	private MockJob(MockInstance mockInstance) {
		super();
		this.mockInstance = mockInstance;
	}

	public void launch(Map<String, Object> seed) throws Exception {
		System.out.println("MockJob launched successfully with DI MockInstance "+mockInstance.toString() + " and seed "+seed.toString());		
	}

}
