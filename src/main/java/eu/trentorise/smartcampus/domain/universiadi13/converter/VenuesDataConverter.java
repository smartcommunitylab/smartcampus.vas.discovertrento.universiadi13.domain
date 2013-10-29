package eu.trentorise.smartcampus.domain.universiadi13.converter;

import it.sayservice.platform.core.domain.actions.DataConverter;
import it.sayservice.platform.core.domain.ext.Tuple;
import it.sayservice.services.universiadi2013.data.message.Data.Address;
import it.sayservice.services.universiadi2013.data.message.Data.KeyValue;
import it.sayservice.services.universiadi2013.data.message.Data.Venue;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
		
		gp.setType(TYPE_UNIVERSIADI + " - Venues");
		
		gp.setSource("Universiadi 2013");
		
		for (KeyValue kv : venue.getNameList()) {
			if ("IT".equals(kv.getKey())) {
				gp.setTitle(kv.getValue());
			}
		}
		if (gp.getTitle() == null && venue.getNameCount() > 0) {
			gp.setTitle(venue.getName(0).getValue());
		}

		gp.setId(venue.getId()+"@universiadi13");
		
		Address address = null;
		for (Address a : venue.getLocation().getAddressList()) {
			if ("IT".equals(a.getLang())) {
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
		if (venue.getTagCount() > 0) {
			pd.setTags(venue.getTagList().toArray(new String[venue.getTagCount()]));
		}
		gp.setPoiData(pd);
		
		Map<String,Object> map = new TreeMap<String, Object>();
		map.put("category", venue.getCategory());
		map.put("imageUrl", venue.getImageUrl());
		try {
			gp.setCustomData(new ObjectMapper().writeValueAsString(map));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return gp;
	}
}
