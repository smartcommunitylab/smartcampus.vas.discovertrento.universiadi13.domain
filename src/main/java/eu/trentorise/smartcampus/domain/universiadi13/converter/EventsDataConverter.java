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

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm");
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

	private GenericEvent extractGenericEvent(Event ev) throws ParseException {
		GenericEvent ge = new GenericEvent();
		
		ge.setDescription(createDescription(ev));
		
		ge.setSource("Universiadi 2013");

		ge.setFromTime(sdf.parse(ev.getStartDate()).getTime());
		ge.setToTime(sdf.parse(ev.getEndDate()).getTime());
		ge.setTiming(sdf_time.format(new Date(ge.getFromTime())) + " - "+sdf_time.format(new Date(ge.getToTime())));
		
		ge.setType(TYPE_UNIVERSIADI);
		
		String s = ev.getId();
		ge.setId(s+"@universiadi13");
		if (ev.hasPoiId()) {
			ge.setPoiId(ev.getPoiId()+"@universiadi13");
		}

		for (KeyValue kv : ev.getTitleList()) {
			if ("IT".equals(kv.getKey())) {
				ge.setTitle(kv.getValue());
			}
		}
		if (ge.getTitle() == null && ev.getTitleCount() > 0) {
			ge.setTitle(ev.getTitle(0).getValue());
		}
		
		Map<String,Object> map = new TreeMap<String, Object>();
		map.put("imageUrl", ev.getImageUrl());
		map.put("category", ev.getCategory());
		try {
			ge.setCustomData(new ObjectMapper().writeValueAsString(map));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ge;
	}

	private String createDescription(Event ev) {
		StringBuilder descr = new StringBuilder();
		for (KeyValue kv : ev.getLongDescList()) {
			if ("IT".equals(kv.getKey())) {
				descr.append(kv.getValue());
			}
		}
		if (descr.length() == 0) {
			for (KeyValue kv : ev.getShortDescList()) {
				if ("IT".equals(kv.getKey())) {
					descr.append(kv.getValue());
				}
			}
		}
		if (descr.length() == 0 && ev.getLongDescCount() > 0) {
			descr.append(ev.getLongDesc(0).getValue());
		} else if (descr.length() == 0 && ev.getShortDescCount() > 0) {
			descr.append(ev.getShortDesc(0).getValue());
		}
		
		if (ev.getUrl() != null) {
			descr.append("<a href=\""+ev.getUrl()+"\">"+ev.getUrl()+"</a>");
		}
		
		String s = descr.toString();
		s = s.replace("\n", " ");
		s = s.replace("\t", " ");
		return s;
	}
}
