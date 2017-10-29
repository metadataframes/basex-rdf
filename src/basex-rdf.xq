xquery version "3.1";

import module namespace 
  basex-rdf = "https://metadatafram.es/basex/modules/rdf/graphs/" 
  at "modules/basex-rdf.xqm";
import module namespace graphs = "http://basex.org/modules/rdf/Graphs";

let $triples as xs:string :=
  ``[
 @prefix pc: <http://id.loc.gov/ontologies/bibframe/> .
 @prefix c: <http://id.loc.gov/ontologies/bflc/> .
 @prefix ldp: <http://www.loc.gov/mads/rdf/v1#> .
 @prefix dcterms: <http://www.dcterms.org#> .
 @prefix foaf: <http://www.foaf.org#> .

    # This is our Product, which can be classified by topics...
    </catalog/SKU41-2510>
        a pc:Product;
        dcterms:title "Waterproof LED Flashlight";
        # The membership triples link to the real resource
        c:hasTopic
            <http://example.org/topics/handheld-flashlights>,
            <http://example.org/topics/camping-lights-and-lanterns>.
      
    # Indirect Container (controller for managing the topics on our Product)...
    </catalog/SKU41-2510/topics/>
        a                       ldp:IndirectContainer;
        ldp:membershipResource  </catalog/SKU41-2510>;
        ldp:hasMemberRelation   c:hasTopic;
        ldp:isMemberOfRelation  c:hasProduct;
        
        # The predicate of posted content from which to derive the object URI...
        ldp:insertedContentRelation     foaf:primaryTopic;
        # The containment triples keep a reference to the representation resource
        
        ldp:contains
            <topics/handheld-flashlights>,
            <topics/camping-lights-and-lanterns>.
     
    # Locally represents a resource outside of this app's domain of control...
    </topics/handheld-flashlights>
        a                   c:ProxyTopic;
        foaf:primaryTopic   <http://example.org/topics/handheld-flashlights>;
        c:hasProduct
            </catalog/SKU41-2510>.
  ]``
let $options :=
  <options>
    <subject></subject>
    <verb>rdf:type</verb>
    <object></object>
  </options>
let $x-triples := db:open("docs")
return (       
 
  (: db:create("rdf", ($x-triples
    => graphs:parse()), "rdf") :)  
  db:create(
    "basex-rdf2", 
    basex-rdf:transform($x-triples),
    "basex-rdf2"
  )
  (: basex-rdf:query(basex-rdf:pass-options($options) :)
  
  (: file:write("/home/tat2/Dropbox/basex-rdf/test/fixtures/docs.raw",
  <rdf>{
  for $line in $x-triples/*/*
  return
    try {
      graphs:parse($line)
    } catch * {
       <error
        xmlns="https://metadatafram.es/metaquery/mq/">
        {
          "Error ["
          || $err:code
          || "]: "
          || $err:line-number
          || "&#10;"
          || $err:additional
          || "&#10;"
          || $err:description
        }
      </error>
    }
    }</rdf>) :)
     
    (: file:write("/home/tat2/Dropbox/SHARE-VDE_yale/Phase2/Released_datasets/SAMPLE_YaleSubset/types.xml",
    <types>{
      let $types := db:text("basex-rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")/../..
      for $type in $types//o
      return (
        <subject-object>{
          $type/ancestor::t/s, $type  
        }</subject-object>
      )
    }</types>) :)
  

)
  

  
  
  
  

  
    