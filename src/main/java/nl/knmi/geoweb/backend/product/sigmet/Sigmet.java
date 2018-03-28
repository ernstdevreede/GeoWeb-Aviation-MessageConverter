package nl.knmi.geoweb.backend.product.sigmet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.UUID;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.Duration;

import org.geojson.GeoJsonObject;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Getter;
import lombok.Setter;
import nl.knmi.adaguc.tools.Debug;
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
public class Sigmet {
	
	public static final Duration WSVALIDTIME = Duration.ofHours(4); //4*3600*1000;
	public static final Duration WVVALIDTIME = Duration.ofHours(6); //6*3600*1000;

	private GeoJsonObject geojson;
	private Phenomenon phenomenon;
	private ObsFc obs_or_forecast;
	private SigmetLevel level;
	private SigmetMovement movement;
	private SigmetChange change;

	private String forecast_position;
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private OffsetDateTime issuedate;
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private OffsetDateTime validdate;
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private OffsetDateTime validdate_end;
	private String firname;
	private String location_indicator_icao;
	private String location_indicator_mwo;
	private String uuid;
	private SigmetStatus status;
	private int sequence;
	
	@JsonInclude(Include.NON_NULL)
	private Integer cancels;
	
	public static final String DATEFORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	
	@Bean(name = "objectMapper")
	public static ObjectMapper getSigmetObjectMapperBean() {
		ObjectMapper om = new ObjectMapper();
		om.registerModule(new JavaTimeModule());
		om.setTimeZone(TimeZone.getTimeZone("UTC"));
		om.setDateFormat(new SimpleDateFormat(DATEFORMAT_ISO8601));
		om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		om.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		return om;

	}

	
	@Getter
	public enum Phenomenon {
		OBSC_TS("OBSC TS", "Obscured Thunderstorms"),OBSC_TSGR("OBSC TSGR", "Obscured Thunderstorms with hail"),
		EMBD_TS("EMBD TS", "Embedded Thunderstorms"),EMBD_TSGR("EMBD TSGR", "Embedded Thunderstorms with hail"),
		FRQ_TS("FRQ TS", "Frequent Thunderstorms"),FRQ_TSGR("FRQ TSGR", "Frequent Thunderstorms with hail"),
		SQL_TS("SQL TS", "Squall line"),SQL_TSGR("SQL TSGR", "Squall line with hail"),
		SEV_TURB("SEV TURB", "Severe Turbulence"),
		SEV_ICE("SEV ICE", "Severe Icing"), SEV_ICE_FRZA("SEV ICE (FRZA)", "Severe Icing with Freezing Rain"),
		SEV_MTW("SEV MTW", "Severe Mountain Wave"),
		HVY_DS("HVY DS", "Heavy Duststorm"),HVY_SS("HVY SS", "Heavy Sandstorm"),
		RDOACT_CLD("RDOACT CLD", "Radioactive Cloud")
		;
		private String description;
		private String shortDescription;

		public static Phenomenon getRandomPhenomenon() {
			int i=(int)(Math.random()*Phenomenon.values().length);
			System.err.println("rand "+i+ " "+Phenomenon.values().length);
			return Phenomenon.valueOf(Phenomenon.values()[i].toString());
		}

		private Phenomenon(String shrt, String description) {
			this.shortDescription=shrt;
			this.description=description;
		}
		public static Phenomenon getPhenomenon(String desc) {
			for (Phenomenon phen: Phenomenon.values()) {
				if (desc.equals(phen.toString())){
					return phen;
				}
			}
			return null;
			//throw new Exception("You NOOB: Non existing pheonomenon!!!" + desc);
		}
	}
	@JsonInclude(Include.NON_NULL)
	@Getter
	public static class ObsFc {
		private boolean obs=true ;
		OffsetDateTime obsFcTime;
		public ObsFc(){};
		public ObsFc(boolean obs){
			this.obs=obs;
			this.obsFcTime=null;
		}
		public ObsFc(boolean obs, OffsetDateTime obsTime) {
			this.obs=obs;
			this.obsFcTime=obsTime;
		}
	}

	public enum SigmetLevelUnit {
		FT, FL, SFC, M, TOP, TOP_ABV;
	}

	@JsonInclude(Include.NON_NULL)
	@Getter
	public static class SigmetLevelPart{
		float value;
		SigmetLevelUnit unit;
		public SigmetLevelPart(){};
		public SigmetLevelPart(SigmetLevelUnit unit, float val) {
			this.unit=unit;
			this.value=val;
		}
	}

	@JsonInclude(Include.NON_NULL)
	@Getter
	public static class SigmetLevel {
		SigmetLevelPart lev1;
		SigmetLevelPart lev2;
		public SigmetLevel(){};
		public SigmetLevel(SigmetLevelPart lev1) {
			this.lev1=lev1;
		}
		public SigmetLevel(SigmetLevelPart lev1, SigmetLevelPart lev2) {
			this.lev1=lev1;
			this.lev2=lev2;
		}
	}

	public enum SigmetDirection {
		N,NNE,NE,ENE,E,ESE,SE,SSE,S,SSW,SW,WSW,W,WNW;
		public static SigmetDirection getSigmetDirection(String dir) {
			for (SigmetDirection v: SigmetDirection.values()) {
				if (dir.equals(v.toString())) return v;
			}
			return null;
		}
	}

	@JsonInclude(Include.NON_NULL)
	@Getter
	public static class SigmetMovement {
		private Integer speed;
		private SigmetDirection dir;
		private boolean stationary=true;
		public SigmetMovement(){};
		public SigmetMovement(boolean stationary) {
			this.stationary=stationary;
		}
		public SigmetMovement(String dir, int speed) {
			this.stationary=false;
			this.speed=speed;
			this.dir=SigmetDirection.getSigmetDirection(dir);
		}
	}

	@Getter
	public enum SigmetChange {
		INTSF("Intensifying"), WKN("Weakening"), NC("No change");
		private String description;
		private SigmetChange(String desc) {
			this.description=desc;
		}
	}
	
	@Getter
	public enum SigmetStatus {
		PRODUCTION("Production"), CANCELLED("Cancelled"), PUBLISHED("Published"), TEST("Test"); 
		private String status;
		private SigmetStatus (String status) {
			this.status = status;
		}
		public static SigmetStatus getSigmetStatus(String status){
			Debug.println("SIGMET status: " + status);

			for (SigmetStatus sstatus: SigmetStatus.values()) {
				if (status.equals(sstatus.toString())){
					return sstatus;
				}
			}
			return null;
		}

	}



	@Override
	public String toString() {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		PrintStream ps=new PrintStream(baos);
		ps.println(String.format("Sigmet: %s %s %s [%s]", this.firname, location_indicator_icao, location_indicator_mwo, uuid));
		ps.println(String.format("seq: %d issued at %s valid from %s",sequence, this.issuedate, this.validdate));
		ps.println(String.format("change: %s geo: %s", this.change, this.geojson));
		return baos.toString();
	}

	public Sigmet() {
		this.sequence=-1;
	}
	
	public Sigmet(Sigmet otherSigmet) {
		this.firname=otherSigmet.getFirname();
		this.location_indicator_icao=otherSigmet.getLocation_indicator_icao();
		this.location_indicator_mwo=otherSigmet.getLocation_indicator_mwo();
		this.sequence=-1;
		this.phenomenon = otherSigmet.getPhenomenon();
		this.change = otherSigmet.getChange();
		this.forecast_position = otherSigmet.getForecast_position();
		this.geojson = otherSigmet.getGeojson();
		this.level = otherSigmet.getLevel();
		this.movement = otherSigmet.getMovement();
		this.obs_or_forecast = otherSigmet.getObs_or_forecast();
		this.validdate = otherSigmet.getValiddate();
		this.validdate_end = otherSigmet.getValiddate_end();
		this.issuedate = otherSigmet.getIssuedate();
	}

	public Sigmet(String firname, String location, String issuing_mwo, String uuid) {
		this.firname=firname;
		this.location_indicator_icao=location;
		this.location_indicator_mwo=issuing_mwo;
		this.uuid=uuid;
		this.sequence=-1;
		this.phenomenon = null;
		// If a SIGMET is posted, this has no effect
		this.status=SigmetStatus.PRODUCTION;
	}

	static String testGeoJson="{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[4.44963571205923,52.75852934878266],[1.4462013467168233,52.00458561642831],[5.342222631879865,50.69927379063084],[7.754619712476178,50.59854892065259],[8.731640530117685,52.3196364467871],[8.695454573908739,53.50720041878871],[6.847813968390116,54.08633053026368],[3.086939481359807,53.90252679590722]]]},\"properties\":{\"prop0\":\"value0\",\"prop1\":{\"this\":\"that\"}}}]}";

	static int getRandomSequence=0;
	public static Sigmet getRandomSigmet() {
		Sigmet sm=new Sigmet("AMSTERDAM FIR", "EHAA", "EHDB", UUID.randomUUID().toString());
		sm.setPhenomenon(Phenomenon.getRandomPhenomenon());
		sm.setValiddate(OffsetDateTime.now(ZoneId.of("Z")));
		sm.setChange(SigmetChange.NC);
        sm.setGeoFromString(testGeoJson);
		sm.setSequence(getRandomSequence);
		getRandomSequence++;

		sm.setLevel(new SigmetLevel(new SigmetLevelPart(SigmetLevelUnit.FL, 100)));
		sm.setObs_or_forecast(new Sigmet.ObsFc(true));
		sm.setMovement(new Sigmet.SigmetMovement(true));

		return sm;
	}

	public static Sigmet getSigmetFromFile(File f) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper om = getSigmetObjectMapperBean();
		Sigmet sm=om.readValue(f, Sigmet.class);
		return sm;
	}
	
	public void setGeoFromString(String json) {
		Debug.println("setGeoFromString "+json);
		GeoJsonObject geo;	
		try {
			geo = getSigmetObjectMapperBean().readValue(testGeoJson.getBytes(), GeoJsonObject.class);
			this.setGeojson(geo);
			Debug.println("setGeoFromString ["+json+"] set");
			return;
		} catch (JsonParseException e) {
		} catch (JsonMappingException e) {
		} catch (IOException e) {
		}
		Debug.errprintln("setGeoFromString on ["+json+"] failed");
		this.setGeojson(null);
	}

	public void serializeSigmet(String fn) {
		Debug.println("serializeSigmet to "+fn);
		if(this.geojson == null || this.phenomenon == null) {
			throw new IllegalArgumentException("GeoJSON and Phenomenon are required");
		}
		// .... value from constructor is lost here, set it explicitly. (Why?)
		if(this.status == null) {
			this.status = SigmetStatus.PRODUCTION;
		}
		System.out.println(fn);
		ObjectMapper om=getSigmetObjectMapperBean();
		try {
			om.writeValue(new File(fn), this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String serializeSigmetToString() throws JsonProcessingException {
		ObjectMapper om=getSigmetObjectMapperBean();
		return om.writeValueAsString(this);
	}

//	public static void main(String args[]) throws IOException {
//		Sigmet sm=new Sigmet("AMSTERDAM FIR", "EHAA", "EHDB", "abcd");
//		sm.setPhenomenon(Phenomenon.getPhenomenon("OBSC_TS"));
//		sm.setValiddate(new Date(117,2,13,16,0));
//		Debug.println(sm.getValiddate().toString());
//		sm.setChange(SigmetChange.NC);
//		sm.setGeoFromString(testGeoJson);
//		Debug.println(sm.getPhenomenon().toString());
//		
//		SigmetStore store=new SigmetStore("/tmp");
////		store.storeSigmet(sm);
//		
//		ObjectMapper objectMapper = new ObjectMapper();
//		// DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//		// objectMapper.setDateFormat(df);
//		try{
//			String v = objectMapper.writeValueAsString(sm);
//			JSONObject j = (JSONObject) new JSONTokener(v).nextValue();
//			Debug.println(j.get("issuedate").toString());
//		}catch(JsonProcessingException e){
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
////		System.err.println(sm);
////		
////		for (int i=0; i<1; i++) {
////			sm=Sigmet.getRandomSigmet();
////			store.storeSigmet(sm);
////			System.err.println(i+": "+sm);
////		}
//	}
}
