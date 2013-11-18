package eu.trentorise.smartcampus.domain.universiadi13.converter;

import it.sayservice.platform.core.domain.actions.DataConverter;
import it.sayservice.platform.core.domain.ext.Tuple;
import it.sayservice.services.universiadi2013.data.message.Data.Address;
import it.sayservice.services.universiadi2013.data.message.Data.KeyValue;
import it.sayservice.services.universiadi2013.data.message.Data.Venue;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.protobuf.ByteString;

import eu.trentorise.smartcampus.domain.discovertrento.GenericPOI;
import eu.trentorise.smartcampus.domain.discovertrento.POIData;

public class VenuesDataConverter implements DataConverter {

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
				Venue org = Venue.parseFrom(bs);
				GenericPOI gp = extractGenericPOI(org);
				list.add(gp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		res.put("data", list.toArray(new GenericPOI[list.size()]));
		return res;
	}

	private GenericPOI extractGenericPOI(Venue venue) throws ParseException {
		GenericPOI gp = new GenericPOI();
		gp.setType(venue.getCategory());
		
		gp.setSource("Universiadi 2013");
		
		Map<String,String> titleMap = new HashMap<String, String>();
		for (KeyValue kv : venue.getNameList()) {
			titleMap.put(kv.getKey(), kv.getValue());
		}
		if (titleMap.containsKey("EN")) gp.setTitle(titleMap.get("EN"));
		else if (titleMap.size() > 0) gp.setTitle(titleMap.values().iterator().next());

		Map<String,String> descrMap = new HashMap<String, String>();
		for (KeyValue kv : venue.getDescriptionList()) {
			descrMap.put(kv.getKey(), kv.getValue());
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
		gp.setDescription(createDescription(venue));
		
		POIData pd = new POIData();
		pd.setCity(address.getCity());
		pd.setCountry("Italy");
		pd.setPoiId(gp.getId());
		pd.setLatitude(venue.getLocation().getCoordinate().getLatitude());
		pd.setLongitude(venue.getLocation().getCoordinate().getLongitude());
		pd.setPostalCode(address.getPostalCode());
		pd.setStreet(address.getStreet());
		if (venue.getTagCount() > 0) {
			pd.setTags(venue.getTagList().toArray(new String[venue.getTagCount()]));
		}
		gp.setPoiData(pd);
		
		Map<String,Object> map = new TreeMap<String, Object>();
		map.put("category", venue.getCategory());
		map.put("imageUrl", venue.getImageUrl());
		map.put("title", titleMap);
		map.put("description", descrMap);
		try {
			gp.setCustomData(new ObjectMapper().writeValueAsString(map));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return gp;
	}
	
	private String createDescription(Venue p) {
		StringBuilder descr = new StringBuilder();
		for (KeyValue kv : p.getDescriptionList()) {
			if ("IT".equals(kv.getKey())) {
				descr.append(kv.getValue());
			}
		}
		if (descr.length() == 0 && p.getDescriptionCount() > 0) {
			descr.append(p.getDescription(0).getValue());
		}
		
		String s = descr.toString();
		s = s.replace("\n", " ");
		s = s.replace("\t", " ");
		return s;
	}

}
