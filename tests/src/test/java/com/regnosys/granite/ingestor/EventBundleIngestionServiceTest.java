package com.regnosys.granite.ingestor;

import cdm.event.common.EventTestBundle;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.regnosys.granite.ingestor.service.IngestionFactory;
import com.regnosys.granite.ingestor.service.IngestionService;
import org.isda.cdm.CdmRuntimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;

import java.net.URL;
import java.util.stream.Stream;

class EventBundleIngestionServiceTest extends IngestionTest<EventTestBundle>{

	private static final String SAMPLE_FILES_DIR = "cdm-sample-files/bundles/";

	private static ImmutableList<URL> EXPECTATION_FILES = ImmutableList.<URL>builder()
			.add(Resources.getResource(SAMPLE_FILES_DIR + "expectations.json"))
			.build();
	
	private static IngestionService ingestionService;

	@BeforeAll
	static void setup() {
		CdmTestInitialisationUtil cdmTestInitialisationUtil = new CdmTestInitialisationUtil();
		initialiseIngestionFactory(new CdmRuntimeModule(), cdmTestInitialisationUtil.getPostProcessors());
		ingestionService = IngestionFactory.getInstance().getFpml510EventsAndBundles();
	}
	
	@Override
	protected Class<EventTestBundle> getClazz() {
		return EventTestBundle.class;
	}

	@Override
	protected IngestionService ingestionService() {
		return ingestionService;
	}
	
	@Override
	protected void assertEventEffect(EventTestBundle c) {
	}
	
	@SuppressWarnings("unused")//used by the junit parameterized test
	private static Stream<Arguments> fpMLFiles() {
		return readExpectationsFrom(EXPECTATION_FILES);
	}
}