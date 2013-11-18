package eu.trentorise.smartcampus.domain.universiadi13.converter;

import it.sayservice.platform.core.domain.actions.DataConverter;
import it.sayservice.platform.core.domain.ext.Tuple;
import it.sayservice.services.universiadi2013.data.message.Data.Address;
import it.sayservice.services.universiadi2013.data.message.Data.Event;
import it.sayservice.services.universiadi2013.data.message.Data.KeyValue;
import it.sayservice.services.universiadi2013.data.message.Data.Poi;
import it.sayservice.services.universiadi2013.data.message.Data.Venue;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.protobuf.ByteString;

import eu.trentorise.smartcampus.domain.discovertrento.GenericPOI;
import eu.trentorise.smartcampus.domain.discovertrento.POIData;

public class POIDataConverter implements DataConverter {

	private static final String TYPE_UNIVERSIADI = "universiadi13";   
	
//	private static final int DURATION = (24*60 - 1)*60*1000;
	
	@Override
	public Serializable toMessage(Map<String, Object> parameters) {
		if (parameters == null)
			return null;
		return new HashMap<String, Object>(parameters);
	}
	
	@Override
	public Object fromMessage(Serializable object) {
		List<ByteString> data = (List<ByteString>) object;
		Tuple res = new Tuple();
		List<GenericPOI> list = new ArrayList<GenericPOI>();
		for (ByteString bs : data) {
			try {
				Poi org = Poi.parseFrom(bs);
				GenericPOI gp = extractGenericPOI(org);
				list.add(gp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		res.put("data", list.toArray(new GenericPOI[list.size()]));
		return res;
	}

	private GenericPOI extractGenericPOI(Poi venue) throws ParseException {
		GenericPOI gp = new GenericPOI();
		
		gp.setType(venue.getCategory());
		
		gp.setSource("Universiadi 2013");
		
		Map<String,String> titleMap = new HashMap<String, String>();
		for (KeyValue kv : venue.getNameList()) {
			if (!kv.getValue().isEmpty()) {
				titleMap.put(kv.getKey(), kv.getValue());
			}
		}
		if (titleMap.containsKey("EN")) gp.setTitle(titleMap.get("EN"));
		else if (titleMap.size() > 0) gp.setTitle(titleMap.values().iterator().next());

		Map<String,String> descrMap = new HashMap<String, String>();
		for (KeyValue kv : venue.getDescriptionList()) {
			if (!kv.getValue().isEmpty()) {
				descrMap.put(kv.getKey(), kv.getValue());
			}
		}
		if (descrMap.containsKey("EN")) gp.setDescription(descrMap.get("EN"));
		else if (descrMap.size() > 0) gp.setDescription(descrMap.values().iterator().next());

		gp.setId(venue.getId()+"@universiadi13");
		
		Address address = null;
		for (Address a : venue.getLocation().getAddressList()) {
			if ("EN".equals(a.getLang())) {
				address = a;
			}
		}
		if (address == null) {
			address = venue.getLocation().getAddress(0);
		}
		
		POIData pd = new POIData();
		pd.setCity(address.getCity());
		pd.setCountry("Italy");
		pd.setPoiId(gp.getId());
		pd.setLatitude(venue.getLocation().getCoordinate().getLatitude());
		pd.setLongitude(venue.getLocation().getCoordinate().getLongitude());
		pd.setPostalCode(address.getPostalCode());
		pd.setStreet(address.getStreet());
		gp.setPoiData(pd);
		
		Map<String,Object> map = new TreeMap<String, Object>();
		map.put("category", venue.getCategory());
		if (venue.getImageCount() > 0) {
			map.put("imageUrl", venue.getImage(0).getImageUrl());
		}
		map.put("title", titleMap);
		map.put("description", descrMap);
		map.put("serviceDescription", createValue(venue.getServiceDescriptionList()));
		map.put("topic", venue.getTopicList());
		map.put("timetable", createValue(venue.getTimetableList()));
		map.put("price", createValue(venue.getPriceList()));
		map.put("seatingCapacity", createValue(venue.getSeatingCapacityList()));
		map.put("accessibility", createValue(venue.getAccessibilityList()));
		try {
			gp.setCustomData(new ObjectMapper().writeValueAsString(map));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return gp;
	}
	
	private Map<String,String> createValue(List<KeyValue> list) {
		if (list == null) return Collections.emptyMap();
		Map<String,String> res = new HashMap<String, String>(); 
		for (KeyValue kv : list) {
			res.put(kv.getKey(), kv.getValue().replace("\n", " ").replace("\t", " "));
		}
		
		return res;
	}

}
