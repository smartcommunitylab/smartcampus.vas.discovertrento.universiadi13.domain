/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package smartcampus.domain.universiadi.test;

import it.sayservice.platform.client.ServiceBusAdminClient;
import it.sayservice.platform.client.ServiceBusClient;
import it.sayservice.platform.client.jms.JMSServiceBusAdminClient;
import it.sayservice.platform.client.jms.JMSServiceBusClient;
import it.sayservice.platform.core.domain.DomainObject;
import it.sayservice.platform.core.message.Core.DODataRequest;
import it.sayservice.platform.core.message.Core.DomainEvent;
import it.sayservice.platform.domain.test.DomainListener;
import it.sayservice.platform.domain.test.DomainTestHelper;

import java.util.List;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.jms.client.HornetQJMSConnectionFactory;

import eu.trentorise.smartcampus.domain.discovertrento.EventObjectDOEngine;
import eu.trentorise.smartcampus.domain.discovertrento.EventServiceDOEngine;
import eu.trentorise.smartcampus.domain.discovertrento.POIObjectDOEngine;
import eu.trentorise.smartcampus.domain.discovertrento.POIServiceDOEngine;
import eu.trentorise.smartcampus.domain.universiadi13.FeratelCommPOIServiceDOEngine;
import eu.trentorise.smartcampus.domain.universiadi13.FeratelPubPOIServiceDOEngine;
import eu.trentorise.smartcampus.domain.universiadi13.FeratelRicServiceDOEngine;
import eu.trentorise.smartcampus.domain.universiadi13.FeratelRistServiceDOEngine;
import eu.trentorise.smartcampus.domain.universiadi13.FeratelSportServiceDOEngine;
import eu.trentorise.smartcampus.domain.universiadi13.UniversiadiEventsServiceDOEngine;
import eu.trentorise.smartcampus.domain.universiadi13.UniversiadiPOIServiceDOEngine;
import eu.trentorise.smartcampus.domain.universiadi13.UniversiadiScheduleServiceDOEngine;
import eu.trentorise.smartcampus.domain.universiadi13.UniversiadiVenuesServiceDOEngine;


public class TestDomain {

	public static void main(String[] args) throws Exception {
		HornetQJMSConnectionFactory cf = 
			     new HornetQJMSConnectionFactory(false,
			                  new TransportConfiguration(
			                    "org.hornetq.core.remoting.impl.netty.NettyConnectorFactory"));
			  ServiceBusClient client = new JMSServiceBusClient(cf);

			  DomainTestHelper helper = new DomainTestHelper(client,new DomainListener() {
			    public void onDomainEvents(List<DomainEvent> events) {
			    	System.err.println(events);
			    	System.err.println();
			      // DO someth...
			    }

			    public void onDataRequest(DODataRequest req) {
			      // DO someth...
			    }
			  });

			  
			  eventsCreation(cf, helper);
			  System.err.println();
			  
	}

	private static void initDomain(DomainTestHelper helper) {
		helper.start(
				  new EventObjectDOEngine(),
				  new EventServiceDOEngine(),

				  new POIObjectDOEngine(),
				  new POIServiceDOEngine(),
				  
				  new UniversiadiEventsServiceDOEngine(),
				  new UniversiadiScheduleServiceDOEngine(),
				  new UniversiadiPOIServiceDOEngine(),
				  new UniversiadiVenuesServiceDOEngine(),
				  new FeratelCommPOIServiceDOEngine(),
				  new FeratelPubPOIServiceDOEngine(),
				  new FeratelRicServiceDOEngine(),
				  new FeratelRistServiceDOEngine(),
				  new FeratelSportServiceDOEngine()
				  );
	}

	private static void eventsCreation(HornetQJMSConnectionFactory cf,
			DomainTestHelper helper) throws Exception {
		  helper.cleanDomainData();
		  initDomain(helper);
		  ServiceBusAdminClient admin = new JMSServiceBusAdminClient(cf);
		  
//		  admin.restartService("it.sayservice.ext.universiadi2013", "GetEvents");
//		  admin.restartService("it.sayservice.ext.universiadi2013", "GetCompetitionSchedule");
//		  admin.restartService("it.sayservice.ext.universiadi2013", "GetPoiFromCsv");
//		  admin.restartService("it.sayservice.ext.universiadi2013", "GetVenues");
		  
		  admin.restartService("it.sayservice.ext.feratel", "GetFeratelCommerciale");
		  admin.restartService("it.sayservice.ext.feratel", "GetFeratelPubbliche");
		  admin.restartService("it.sayservice.ext.feratel", "GetFeratelRicettive");
		  admin.restartService("it.sayservice.ext.feratel", "GetFeratelRistoro");
		  admin.restartService("it.sayservice.ext.feratel", "GetFeratelSportive");
		  System.err.println();
}
}
