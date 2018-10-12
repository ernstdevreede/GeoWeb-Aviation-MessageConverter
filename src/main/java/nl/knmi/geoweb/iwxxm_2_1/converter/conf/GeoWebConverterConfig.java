package nl.knmi.geoweb.iwxxm_2_1.converter.conf;

import nl.knmi.geoweb.iwxxm_2_1.converter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fi.fmi.avi.converter.AviMessageSpecificConverter;
import fi.fmi.avi.converter.ConversionSpecification;
import fi.fmi.avi.model.sigmet.SIGMET;
import fi.fmi.avi.model.taf.TAF;
import nl.knmi.geoweb.backend.product.sigmet.Sigmet;
import nl.knmi.geoweb.backend.product.taf.Taf;

@Configuration
public class GeoWebConverterConfig {

    public static final ConversionSpecification<Taf, TAF> GEOWEBTAF_TO_TAF_POJO = new ConversionSpecification<>(Taf.class, TAF.class, "ICAO Annex 3 TAC",
            null);

    public static final ConversionSpecification<Sigmet, SIGMET> GEOWEBSIGMET_TO_SIGMET_POJO = new ConversionSpecification<>(Sigmet.class, SIGMET.class, "ICAO Annex 3 SIGMET",
            null);

    public static final ConversionSpecification<TAF, Taf> TAF_TO_GEOWEBTAF_POJO = new ConversionSpecification<>(TAF.class, Taf.class, "ICAO Annex 3 SIGMET",
            null);

    @Bean
    AviMessageSpecificConverter<Taf, TAF> geowebTafConverter() {
        GeoWebConverter<TAF> p = new GeoWebTAFConverter();
        return p;
    }

    @Bean
    AviMessageSpecificConverter<TAF, Taf> geowebTafInConverter() {
        GeoWebTafInConverter p = new GeoWebTafInConverter();
        return p;
    }

    @Bean
    AviMessageSpecificConverter<Sigmet, SIGMET> geowebSIGMETConverter() {
        GeoWebSigmetConverter<SIGMET> p = new GeoWebSIGMETConverter();
        return p;
    }

}
