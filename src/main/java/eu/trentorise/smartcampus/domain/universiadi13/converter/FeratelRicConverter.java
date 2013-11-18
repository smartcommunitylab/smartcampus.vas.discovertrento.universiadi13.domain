package eu.trentorise.smartcampus.domain.universiadi13.converter;

import it.sayservice.platform.core.domain.actions.DataConverter;
import it.sayservice.platform.core.domain.ext.Tuple;
import it.sayservice.services.feratel.data.message.Data.Servizio;
import it.sayservice.services.feratel.data.message.Data.StrutturaPubblica;
import it.sayservice.services.feratel.data.message.Data.StrutturaRicettiva;

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

public class FeratelRicConverter implements DataConverter {

	private static final String TYPE_FERATEL = "feratel";   
	private static final ObjectMapper mapper = new ObjectMapper();
	
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
				StrutturaRicettiva org = StrutturaRicettiva.parseFrom(bs);
				GenericPOI gp = extractGenericPOI(org);
				list.add(gp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		res.put("data", list.toArray(new GenericPOI[list.size()]));
		return res;
	}

	private GenericPOI extractGenericPOI(StrutturaRicettiva venue) throws ParseException {
		GenericPOI gp = new GenericPOI();
		
		gp.setType(TYPE_FERATEL + " - Ricettiva");
		gp.setSource("Universiadi 2013");
		
		gp.setTitle(venue.getNome());
		if (venue.hasDescrizioneIt()) {
			gp.setDescription(venue.getDescrizioneIt());
		}
		gp.setId(venue.getId()+"@feratel");
		
		POIData pd = new POIData();
		pd.setCountry("Italy");
		pd.setPoiId(gp.getId());
		pd.setLatitude(venue.getPosizione().getLatitudine());
		pd.setLongitude(venue.getPosizione().getLongitudine());
		pd.setStreet(venue.getIndirizzo());
		gp.setPoiData(pd);
		
		Map<String,Object> map = new TreeMap<String, Object>();
		if (venue.getServizioCount() > 0) {
			List<Map<String,Object>> services = new ArrayList<Map<String,Object>>();
			for (Servizio s : venue.getServizioList()) {
				Map<String,Object> service = new HashMap<String, Object>();
				service.put("id", s.getId());
				service.put("numero", s.getNumero());
				if (s.hasDescIt()) {
					service.put("desc", s.getDescIt());
				}
				services.add(service);
			}
			map.put("services", services);
		}
		
		if (venue.getFotoCount() > 0) {
			map.put("imageUrl", venue.getFoto(0).getImageUrl());
		}
		if (venue.hasCategoriaIt()) {
			map.put("categoria", venue.getCategoriaIt());
		}
		if (venue.hasCamere()) {
			map.put("camere", venue.getCamere());
		}
		if (venue.hasLetti()) {
			map.put("letti", venue.getLetti());
		}

		try {
			gp.setCustomData(mapper.writeValueAsString(map));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return gp;
	}

}
