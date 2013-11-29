package eu.trentorise.smartcampus.domain.universiadi13.converter;

import it.sayservice.platform.core.domain.actions.DataConverter;
import it.sayservice.platform.core.domain.ext.Tuple;
import it.sayservice.services.universiadi2013.data.message.Data.Event;
import it.sayservice.services.universiadi2013.data.message.Data.KeyValue;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.protobuf.ByteString;

import eu.trentorise.smartcampus.domain.discovertrento.GenericEvent;

public class EventsDataConverter implements DataConverter {

	private static final SimpleDateFormat[] sdf = new SimpleDateFormat[]{
		new SimpleDateFormat("yyyyMMdd'T'HH.mm"),
		new SimpleDateFormat("yyyyMMdd'T'HH:mm"),
		new SimpleDateFormat("yyyy-MM-dd'T'HH.mm"),
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"),
	};
	private static final SimpleDateFormat[] sdf_nt = new SimpleDateFormat[]{
		new SimpleDateFormat("yyyy-MM-dd'Tnull'"),
		new SimpleDateFormat("yyyyMMdd'Tnull'")
	};
	private static final String TYPE_UNIVERSIADI = "universiadi13";   
	private static final SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm");
	
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
		List<GenericEvent> list = new ArrayList<GenericEvent>();
		for (ByteString bs : data) {
			try {
				Event ev = Event.parseFrom(bs);
				GenericEvent ge = extractGenericEvent(ev);
				list.add(ge);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		res.put("data", list.toArray(new GenericEvent[list.size()]));
		return res;
	}

	private Date extractDateTime(String dt) {
		for (SimpleDateFormat f : sdf) {
			try {
				return f.parse(dt);
			} catch (ParseException e) {
				continue;
			}
		}
		throw new IllegalArgumentException();
	}

	private Date extractDate(String dt) {
		for (SimpleDateFormat f : sdf_nt) {
			try {
				return f.parse(dt);
			} catch (ParseException e) {
				continue;
			}
		}
		throw new IllegalArgumentException();
	}

	protected String getSource() {
		return "Universiadi 2013 - Events";
	}

	private GenericEvent extractGenericEvent(Event ev) throws ParseException {
		GenericEvent ge = new GenericEvent();
		
		
		ge.setSource(getSource());

		boolean hasFromTime = true, hasToTime = true;
		try {
			ge.setFromTime(extractDateTime(ev.getStartDate()).getTime());
		} catch (Exception e1) {
			hasFromTime = false;
			ge.setFromTime(extractDate(ev.getStartDate()).getTime());
		}
		try {
			ge.setToTime(extractDateTime(ev.getEndDate()).getTime());
		} catch (Exception e1) {
			hasToTime = false;
			ge.setToTime(extractDate(ev.getEndDate()).getTime());
		}
		if (hasFromTime) {
			String timing = sdf_time.format(new Date(ge.getFromTime()));
			if (hasToTime){
				timing += " - "+sdf_time.format(new Date(ge.getToTime()));
			}
			ge.setTiming(timing);
			
		}
		
		ge.setType(ev.getCategory());
		
		String s = ev.getId();
		ge.setId(s+"@universiadi13");
		if (ev.hasPoiId()) {
			ge.setPoiId(ev.getPoiId()+"@universiadi13");
		}

		Map<String,String> titleMap = new HashMap<String, String>();
		
		for (KeyValue kv : ev.getTitleList()) {
			titleMap.put(kv.getKey(), kv.getValue());
		}
		if (titleMap.containsKey("EN")) ge.setTitle(titleMap.get("EN"));
		else if (titleMap.size() > 0) ge.setTitle(titleMap.values().iterator().next());

		Map<String,String> descrMap = createDescription(ev);
		if (descrMap.containsKey("EN")) ge.setDescription(descrMap.get("EN"));
		else if (descrMap.size() > 0) ge.setDescription(descrMap.values().iterator().next());
		
		
		Map<String,Object> map = new TreeMap<String, Object>();
		map.put("imageUrl", ev.getImageUrl());
		map.put("category", ev.getCategory());
		map.put("title", titleMap);
		map.put("description", descrMap);
		if (ev.getSportsCount() > 0) {
			map.put("sports", ev.getSportsList());
		}
		try {
			ge.setCustomData(new ObjectMapper().writeValueAsString(map));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ge;
	}

	private Map<String,String> createDescription(Event ev) {
		Map<String,String> res = new HashMap<String, String>();
		
		for (KeyValue kv : ev.getLongDescList()) {
			res.put(kv.getKey(), kv.getValue());
		}
		for (KeyValue kv : ev.getShortDescList()) {
			if (!res.containsKey(kv.getKey()) || res.get(kv.getKey()).isEmpty()) {
				res.put(kv.getKey(), kv.getValue());	
			}
		}
		for (String key : res.keySet()) {
			String s = res.get(key);
			if (ev.getUrl() != null) {
				s += "<a href=\""+ev.getUrl()+"\">"+ev.getUrl()+"</a>";
			}
			s = s.replace("\n", " ");
			s = s.replace("\t", " ");
			res.put(key, s);
		}
		
		return res;
	}
}
