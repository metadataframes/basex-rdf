xquery version "3.1";

import module namespace 
  basex-rdf = "https://metadatafram.es/basex/modules/rdf/graphs/" 
  at "modules/basex-rdf.xqm";
import module namespace graphs = "http://basex.org/modules/rdf/Graphs";

let $triples as xs:string :=
  ``[
    @prefix bf: <http://id.loc.gov/ontologies/bibframe/> .
    @prefix bflc: <http://id.loc.gov/ontologies/bflc/> .
    @prefix madsrdf: <http://www.loc.gov/mads/rdf/v1#> .
    @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
    @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
    @prefix xml: <http://www.w3.org/XML/1998/namespace> .
    @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
    
    <http://id.loc.gov/authorities/names/n00020514> a bf:Title ;
        rdfs:label "Bible. Vulgate. 1454." ;
        bflc:title30MarcKey """1300
                  $aBible.$lLatin.$sVulgate.$f1454.""" ;
        bflc:title30MatchKey """Bible. Vulgate.
                  1454.""" ;
        bflc:titleSortKey """Bible. Vulgate.
                  1454.""" ;
        bf:mainTitle "Bible" .
    
    <http://id.loc.gov/authorities/names/n50034916> a bf:Agent,
            bf:Person ;
        rdfs:label "Gutenberg, Johann, 1397?-1468,"@en ;
        bflc:name00MarcKey """7001 $aGutenberg,
                  Johann,$d1397?-1468,$eprinter.""" ;
        bflc:name00MatchKey """Gutenberg, Johann,
                  1397?-1468,""" .
    
    <http://id.loc.gov/vocabulary/countries/gw> a bf:Place .
    
    <http://id.loc.gov/vocabulary/issuance/mono> a bf:Issuance .
    
    <http://id.loc.gov/vocabulary/languages/lat> a bf:Language .
    
    <http://id.loc.gov/vocabulary/organizations/dlc> a bf:Source .
    
    <http://id.loc.gov/vocabulary/relators/prt> a bf:Role ;
        rdfs:label "printer." ;
        bflc:relatorMatchKey "printer" .
    
    <https://metadatafram.es/3391099#Instance> a bf:Instance ;
        rdfs:label "Biblia latina." ;
        bflc:indexedIn [ a bf:Instance ;
                bf:note [ a bf:Note ;
                        rdfs:label "4201" ;
                        bf:noteType "Location" ] ;
                bf:title [ a bf:Title ;
                        rdfs:label "GW" ] ],
            [ a bf:Instance ;
                bf:note [ a bf:Note ;
                        rdfs:label "ib00526000" ;
                        bf:noteType "Location" ] ;
                bf:title [ a bf:Title ;
                        rdfs:label "ISTC (CD-ROM, 1997 ed.)" ] ],
            [ a bf:Instance ;
                bf:note [ a bf:Note ;
                        rdfs:label "I, 17 (IC.55)" ;
                        bf:noteType "Location" ] ;
                bf:title [ a bf:Title ;
                        rdfs:label "BM 15th cent." ] ],
            [ a bf:Instance ;
                bf:note [ a bf:Note ;
                        rdfs:label "3031" ;
                        bf:noteType "Location" ] ;
                bf:title [ a bf:Title ;
                        rdfs:label "Hain" ] ],
            [ a bf:Instance ;
                bf:note [ a bf:Note ;
                        rdfs:label "B-526" ;
                        bf:noteType "Location" ] ;
                bf:title [ a bf:Title ;
                        rdfs:label "Goff" ] ],
            [ a bf:Instance ;
                bf:note [ a bf:Note ;
                        rdfs:label "56" ;
                        bf:noteType "Location" ] ;
                bf:title [ a bf:Title ;
                        rdfs:label "Procter" ] ] ;
        bf:dimensions "(fol.)" ;
        bf:extent [ a bf:Extent ;
                rdfs:label "2 v." ] ;
        bf:identifiedBy [ a bf:Local ;
                bf:source [ a bf:Source ;
                        rdfs:label "OCoLC" ] ;
                rdf:value "ocn685392767" ],
            [ a bf:Local ;
                rdf:value "3391099" ] ;
        bf:instanceOf <https://metadatafram.es/3391099#Work> ;
        bf:issuance <http://id.loc.gov/vocabulary/issuance/mono> ;
        bf:note [ a bf:Note ;
                rdfs:label """Commonly known as the Gutenberg
              Bible.""" ] ;
        bf:provisionActivity [ a bf:ProvisionActivity,
                    bf:Publication ;
                bf:date "1454"^^<http://id.loc.gov/datatypes/edtf> ;
                bf:place <http://id.loc.gov/vocabulary/countries/gw> ],
            [ a bf:ProvisionActivity,
                    bf:Publication ;
                bf:agent [ a bf:Agent ;
                        rdfs:label "Johann Gutenberg" ] ;
                bf:date "ca. 1454" ;
                bf:place [ a bf:Place ;
                        rdfs:label "Mainz" ] ] ;
        bf:provisionActivityStatement """[Mainz : Johann Gutenberg, ca.
          1454]""" ;
        bf:title [ a bf:Title ;
                rdfs:label "Biblia latina." ;
                bflc:titleSortKey "Biblia latina." ;
                bf:mainTitle "Biblia latina" ] .
    
    <https://metadatafram.es/3391099#Item856-33> a bf:Item ;
        bf:electronicLocator [ a rdfs:Resource ;
                bflc:locator <http://beinecke.library.yale.edu/dl_crosscollex/callnumSRCHXC.asp?WC=N&SS=N&CN=ZZi_56> ;
                bf:note [ a bf:Note ;
                        rdfs:label """View a selection of digital images in
                          the Beinecke Library's Digital Images Online
                          database""" ] ] ;
        bf:itemOf <https://metadatafram.es/3391099#Instance856-33> .
    
    <https://metadatafram.es/3391099#Work130-10> a bf:Work ;
        rdfs:label "Bible. Vulgate. 1454." ;
        bf:originDate "1454" ;
        bf:title <http://id.loc.gov/authorities/names/n00020514> ;
        bf:version "Vulgate" .
    
    <https://metadatafram.es/3391099#Work740-31> a bf:Work ;
        rdfs:label "Gutenberg Bible." ;
        bf:title <http://id.loc.gov/authorities/names/n82109028> .
    
    <http://id.loc.gov/authorities/names/n82109028> a bf:Title ;
        rdfs:label "Bible. Latin. Vulgate. 1454.",
            "Gutenberg Bible." ;
        bflc:title30MarcKey """1300
              $aBible.$lLatin.$sVulgate.$f1454.""" ;
        bflc:title30MatchKey """Bible. Latin. Vulgate.
              1454.""" ;
        bflc:titleSortKey """Bible. Latin. Vulgate.
              1454.""",
            "Gutenberg Bible." ;
        bf:mainTitle "Bible",
            "Gutenberg Bible" .
    
    <https://metadatafram.es/3391099#Instance856-33> a bf:Electronic,
            bf:Instance ;
        bf:hasItem <https://metadatafram.es/3391099#Item856-33> ;
        bf:instanceOf <https://metadatafram.es/3391099#Work> ;
        bf:title [ a bf:Title ;
                rdfs:label "Biblia latina." ;
                bflc:titleSortKey "Biblia latina." ;
                bf:mainTitle "Biblia latina" ] .
    
    <https://metadatafram.es/3391099#Work> a bf:Text,
            bf:Work ;
        bf:test """<html><head><title>Test</title></head><body>Test</body></html>"""^^rdf:HTML ;
        rdfs:label "Bible. Latin. Vulgate. 1454." ;
        bf:adminMetadata [ a bf:AdminMetadata ;
                bflc:encodingLevel [ a bflc:EncodingLevel ;
                        bf:code "1" ] ;
                bf:changeDate "2011-03-10T19:28:45"^^xsd:dateTime ;
                bf:creationDate "1992-08-12"^^xsd:date ;
                bf:descriptionConventions [ a bf:DescriptionConventions ;
                        bf:code "unknown" ] ;
                bf:generationProcess [ a bf:GenerationProcess ;
                        rdfs:label """DLC marc2bibframe2 v1.4.0-SNAPSHOT:
                  2017-10-10T01:00:51Z""" ] ;
                bf:identifiedBy [ a bf:Local ;
                        bf:source <http://id.loc.gov/vocabulary/organizations/dlc> ;
                        rdf:value "3391099" ] ;
                bf:source [ a bf:Agent,
                            bf:Source ;
                        rdfs:label "CtY-BR" ],
                    [ a bf:Agent,
                            bf:Source ;
                        rdfs:label "CtY-BR" ] ;
                bf:status [ a bf:Status ;
                        bf:code "c" ] ] ;
        bf:contribution [ a bf:Contribution ;
                bf:agent <http://id.loc.gov/authorities/names/n50034916> ;
                bf:role <http://id.loc.gov/vocabulary/relators/prt> ] ;
        bf:hasInstance <https://metadatafram.es/3391099#Instance>,
            <https://metadatafram.es/3391099#Instance856-33> ;
        bf:hasPart <https://metadatafram.es/3391099#Work740-31> ;
        bf:language [ a bf:Language ;
                rdfs:label "Latin" ],
            <http://id.loc.gov/vocabulary/languages/lat> ;
        bf:originDate "1454" ;
        bf:title <http://id.loc.gov/authorities/names/n82109028> ;
        bf:translationOf <https://metadatafram.es/3391099#Work130-10> ;
        bf:version "Vulgate" .
  ]``
let $options :=
  <options>
    <subject>http://id.loc.gov/authorities/names/n50034916</subject>
    <verb></verb>
    <object></object>
  </options>
return (
  (: <results>{    
    basex-rdf:query(    
      basex-rdf:transform($triples),
      basex-rdf:pass-options($options)    
    )
  }</results> :)
  basex-rdf:transform($triples)
)
  
    