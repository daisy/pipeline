package utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Namespaces {
	
	public static final Map<String, String> ns;
	
	static {
    	Map<String, String> nsMap = new HashMap<String, String>();
        
    	// Pipeline 2 namespaces
    	nsMap.put("d", "http://www.daisy.org/ns/pipeline/data");
    	
    	// A bunch of other namespaces
    	nsMap.put("dc", "http://purl.org/dc/elements/1.1/");
    	nsMap.put("dcterms", "http://purl.org/dc/terms/");
		nsMap.put("dctypes", "http://purl.org/dc/dcmitype/");
		nsMap.put("oebpackage", "http://openebook.org/namespaces/oeb-package/1.0/");
		nsMap.put("owl", "http://www.w3.org/2002/07/owl#");
		nsMap.put("owl2", "http://www.w3.org/2002/07/owl#");
		nsMap.put("doap", "http://usefulinc.com/ns/doap#");
		nsMap.put("earl", "http://www.w3.org/ns/earl#");
		nsMap.put("foaf", "http://xmlns.com/foaf/0.1/");
		nsMap.put("xsd", "http://www.w3.org/2001/XMLSchema");
    	nsMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		nsMap.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    	nsMap.put("bibo", "http://purl.org/ontology/bibo/");
    	nsMap.put("geo", "http://www.geonames.org/ontology#");
    	nsMap.put("lvont", "http://lexvo.org/ontology#");
    	nsMap.put("movie", "http://data.linkedmdb.org/resource/movie/");
    	nsMap.put("pode", "http://www.bibpode.no/vocabulary#");
    	nsMap.put("skos", "http://www.w3.org/2004/02/skos/core#");
    	nsMap.put("sub", "http://xmlns.computas.com/sublima#");
    	nsMap.put("xfoaf", "http://www.foafrealm.org/xfoaf/0.1/");
    	nsMap.put("ao", "http://purl.org/ontology/ao/core#");
    	nsMap.put("marcrel", "http://www.loc.gov/loc.terms/relators/");
    	nsMap.put("mods", "http://www.loc.gov/mods/");
    	nsMap.put("mods3", "http://www.loc.gov/mods/v3");
    	nsMap.put("media", "http://purl.org/media#");
    	nsMap.put("bio", "http://purl.org/vocab/bio/0.1/");
    	nsMap.put("schema", "http://schema.org/");
    	nsMap.put("org", "http://www.w3.org/ns/org#");
    	nsMap.put("frbr", "http://purl.org/vocab/frbr/core#");
    	nsMap.put("frbre", "http://purl.org/vocab/frbr/extended#");
    	nsMap.put("lifecycle", "http://purl.org/vocab/lifecycle/schema#");
    	nsMap.put("sumo", "http://www.ontologyportal.org/SUMO.owl#");
    	nsMap.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    	nsMap.put("OAI-PMH", "http://www.openarchives.org/OAI/2.0/");
    	nsMap.put("DIAG", "http://www.loc.gov/zing/sru/diagnostics/");
    	nsMap.put("SRU", "http://www.loc.gov/zing/sru/");
    	nsMap.put("SRU_dc", "info:sru/schema/1/dc-v1.1");
    	nsMap.put("normarc", "info:lc/xmlns/marcxchange-v1");
    	nsMap.put("marcxchange", "info:lc/xmlns/marcxchange-v1");
    	
        ns = Collections.unmodifiableMap(nsMap);
    }
	
	public String htmlReference(String property) {
		String[] split = property.split(":", 2);
		if ("nlb".equals(split[0])) return "http://128.39.251.177:9000/vocabulary/#"+split[1];
        if ("dc".equals(split[0])) return "http://dublincore.org/documents/dces/#"+split[1];
    	if ("dcterms".equals(split[0])) return "http://dublincore.org/documents/dcmi-terms/#terms-"+split[1].split("\\.",2)[0];
		if ("dctypes".equals(split[0])) return "http://dublincore.org/documents/dcmi-type-vocabulary/#"+split[1]+"-003";
		if ("owl".equals(split[0])) return "http://www.w3.org/TR/owl-ref/";
		if ("owl2".equals(split[0])) return "http://www.w3.org/TR/owl2-overview/";
		if ("doap".equals(split[0])) return "http://usefulinc.com/";
		if ("earl".equals(split[0])) return "http://www.w3.org/TR/EARL10-Schema/#"+split[1];
		if ("foaf".equals(split[0])) return "http://xmlns.com/foaf/spec/#term_"+split[1];
		if ("xsd".equals(split[0])) return "http://www.w3.org/XML/Schema";
		if ("rdf".equals(split[0])) return "http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_"+split[1].toLowerCase();
		if ("rdfs".equals(split[0])) return "http://www.w3.org/TR/2004/REC-rdf-schema-20040210/#ch_"+split[1].toLowerCase();
		if ("bibo".equals(split[0])) return "http://bibotools.googlecode.com/svn/bibo-ontology/trunk/doc/index.html";
		if ("geo".equals(split[0])) return "http://www.geonames.org/ontology/documentation.html#";
		if ("lvont".equals(split[0])) return "http://lexvo.org/ontology#";
		//if ("movie".equals(split[0])) return "";
		if ("pode".equals(split[0])) return "http://www.bibpode.no/blogg/?p=1573";
		if ("skos".equals(split[0])) return "http://www.w3.org/2009/08/skos-reference/skos.html#"+split[1];
		if ("sub".equals(split[0])) return "http://www.bibpode.no/blogg/?p=1573";
		if ("xfoaf".equals(split[0])) return "http://semdl.info/books/2/appendices/H";
		if ("ao".equals(split[0])) return "http://smiy.sourceforge.net/ao/spec/associationontology.html#"+split[1];
		if ("marcrel".equals(split[0])) return "http://id.loc.gov/vocabulary/relators/"+split[1].toLowerCase()+".html";
		if ("mods".equals(split[0])) return "http://www.loc.gov/standards/mods/v2/mods-outline-v2.html#"+split[1];
		if ("mods3".equals(split[0])) return "http://www.loc.gov/standards/mods/mods-outline.html#"+split[1];
		if ("media".equals(split[0])) return "http://purl.org/media#"+split[1];
		if ("bio".equals(split[0])) return "http://vocab.org/bio/0.1/.html#"+split[1];
		if ("schema".equals(split[0])) return "http://schema.org/docs/schemas.html";
		if ("org".equals(split[0])) return "http://www.epimorphics.com/public/vocabulary/org.html";
		if ("frbr".equals(split[0])) return "http://purl.org/vocab/frbr/core#"+split[1];
		if ("frbre".equals(split[0])) return "http://purl.org/vocab/frbr/extended#"+split[1];
		if ("lifecycle".equals(split[0])) return "http://vocab.org/lifecycle/schema#term-"+split[1];
		if ("sumo".equals(split[0])) return "http://www.ontologyportal.org/";
		if ("xsi".equals(split[0])) return "http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/datatypes.html#"+split[1];
		
		if ("OAI-PMH".equals(split[0])) return "http://www.openarchives.org/OAI/openarchivesprotocol.html#"+split[1];
		if ("DIAG".equals(split[0])) return "http://www.loc.gov/standards/sru/specs/diagnostics.html";
		if ("SRU".equals(split[0])) return "http://www.loc.gov/standards/sru/";
		if ("SRU_dc".equals(split[0])) return "http://www.loc.gov/standards/sru/resources/dc-schema.html";
		if ("normarc".equals(split[0])) return "http://www.loc.gov/standards/iso25577/ISO_DIS_25577__E_.pdf";
		if ("marcxchange".equals(split[0])) return "http://www.loc.gov/standards/iso25577/ISO_DIS_25577__E_.pdf";
		
		if (Namespaces.ns.containsKey(split[0])) return Namespaces.ns.get(split[0]);
		return "#";
	}

}
