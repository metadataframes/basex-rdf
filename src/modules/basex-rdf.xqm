xquery version "3.1";

module namespace 
  basex-rdf = "https://metadatafram.es/basex/modules/rdf/graphs/";
import module namespace 
  graphs = "http://basex.org/modules/rdf/Graphs";

declare variable $basex-rdf:XSL := doc("process.xsl");
declare variable $basex-rdf:XSL2 := doc("postprocess.xsl");

(:~ 
 :
 :)
declare
  %updating
function basex-rdf:transform(
  $rdf as xs:string
) {
  insert node 
    xslt:transform(graphs:parse($rdf), $basex-rdf:XSL)  
  as last into db:open("basex-rdf")/*
};

(:~ 
 :
 :)
declare function basex-rdf:pass-options(  
  $passed-options as element(options)+
) as element(options)+ {  
  let $options-to-query := (
    copy $options :=
      <options>
        <subject/>
        <verb/>
        <object/>
        <type/>
        <bnode/>
        <datatype/>
        <lang/>
      </options>
      modify (
        for $passed-option in $passed-options/*,
          $option in $options/*[name(.) eq name($passed-option)]
        return
          if (normalize-space($passed-option))
          then
            replace value of node $option
              with string($passed-option)
          else
            replace value of node $option
              with "true"
      )
    return
      $options
    )
  return $options-to-query             
};

(:~ 
 :
 :)
declare function basex-rdf:query(    
  $transform as item()*,  
  $options as element(options)+
) as element()+ {
  for $t in $transform/*/*
  let $query :=           
    xslt:transform(
      $t, 
      $basex-rdf:XSL2,
      map {
        "subject": $options/subject,
        "verb": $options/verb,
        "object": $options/object,
        "type": $options/type,
        "bnode": $options/bnode,
        "datatype": $options/datatype,
        "lang": $options/lang
      }
    )    
  return (
    if ($query//predicates)
    then
      <result>{
        for tumbling window $w in $query/*/*/*
          start $s when name($s) eq "v"
        return
          <predicate>{$w}</predicate>
      }</result>
    else
      if ($query//subject-objects)
      then
        <result>{
          for tumbling window $w in $query/*/*/*
            start $s when name($s) eq "s"
          return
            <subject-object>{$w}</subject-object>
        }</result>
      else
        if ($query//subject-verbs)
        then
          <result>{
            for tumbling window $w in $query/*/*/*
              start $s when name($s) eq "s"
            return
              <subject-verb>{$w}</subject-verb>
          }</result>
        else
          <result>{$query/*/*}</result>
   )
};