<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  exclude-result-prefixes="xs trip"
  version="3.0"
  xmlns:basex-rdf="https://metadatafram.es/basex/modules/rdf/triples/"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output
    indent="no"
    method="xml"/>

  <xsl:param
    name="subject"/>
  <xsl:param
    name="verb"/>
  <xsl:param
    name="object"/>
  <xsl:param
    name="type"/>
  <xsl:param
    name="bnode"/>
  <xsl:param
    name="datatype"/>
  <xsl:param
    name="lang"/>

  <xsl:key
    match="iriref"
    name="prefix-key"
    use="@prefix"/>

  <xsl:key
    match="subject"
    name="subject-verb-key"
    use="following-sibling::predicate/verb"/>

  <xsl:key
    match="subject"
    name="subject-object-key"
    use="following-sibling::predicate/object | following-sibling::predicate/literal"/>

  <xsl:key
    match="object | literal"
    name="object-subject-key"
    use="../../subject"/>

  <xsl:key
    match="object | literal"
    name="object-verb-key"
    use="../verb"/>

  <xsl:key
    match="verb"
    name="verb-subject-key"
    use="../../subject"/>

  <xsl:key
    match="verb"
    name="verb-object-key"
    use="../object | ../literal"/>

  <xsl:key
    match="object[subject]"
    name="bnode-type-key"
    use="predicate/object"/>

  <xsl:key
    match="literal"
    name="datatype-literal-key"
    use="@datatype"/>

  <xsl:key
    match="literal[@xml:lang]"
    name="lang-literal-key"
    use="@xml:lang"/>

  <xsl:template
    match="/">
    <results>
      <xsl:choose>
        <xsl:when
          test="normalize-space($verb[. ne 'true']) and normalize-space($object[. ne 'true']) and $subject eq 'true'">
          <xsl:variable
            name="verb"
            select="basex-rdf:resolve-prefix($verb, *)"/>
          <xsl:variable
            name="object"
            select="basex-rdf:resolve-prefix($object, *)"/>
          <xsl:sequence
            select="
              for $s in (key('subject-verb-key', $verb) intersect key('subject-object-key', $object))
              return
                $s
              "/>
        </xsl:when>
        <xsl:when
          test="normalize-space($subject[. ne 'true']) and normalize-space($verb[. ne 'true']) and $object eq 'true'">
          <xsl:variable
            name="subject"
            select="basex-rdf:resolve-prefix($subject, *)"/>
          <xsl:variable
            name="verb"
            select="basex-rdf:resolve-prefix($verb, *)"/>
          <xsl:sequence
            select="
              for $o in (key('object-subject-key', $subject) intersect key('object-verb-key', $verb))
              return
                $o
              "/>
        </xsl:when>
        <xsl:when
          test="normalize-space($subject[. ne 'true']) and normalize-space($object[. ne 'true']) and $verb eq 'true'">
          <xsl:variable
            name="subject"
            select="basex-rdf:resolve-prefix($subject, *)"/>
          <xsl:variable
            name="object"
            select="basex-rdf:resolve-prefix($object, *)"/>
          <xsl:sequence
            select="
              for $v in (key('verb-subject-key', $subject) intersect key('verb-object-key', $object))
              return
                $v
              "/>
        </xsl:when>
        <xsl:when
          test="normalize-space($verb[. ne 'true']) and $subject eq 'true' and $object eq 'true'">
          <xsl:variable
            name="verb"
            select="basex-rdf:resolve-prefix($verb, *)"/>
          <xsl:sequence
            select="
              basex-rdf:subject-objects(
              for $s-o in (key('subject-verb-key', $verb) union key('object-verb-key', $verb))
              return
                $s-o
              )
              "/>
        </xsl:when>
        <xsl:when
          test="normalize-space($object[. ne 'true']) and $subject eq 'true' and $verb eq 'true'">
          <xsl:variable
            name="object"
            select="basex-rdf:resolve-prefix($object, *)"/>
          <xsl:sequence
            select="
              basex-rdf:subject-verbs(
              for $s-v in (key('subject-object-key', $object) union key('verb-object-key', $object))
              return
                $s-v
              )
              "/>
        </xsl:when>
        <xsl:when
          test="normalize-space($subject[. ne 'true']) and $verb eq 'true' and $object eq 'true'">
          <xsl:variable
            name="subject"
            select="basex-rdf:resolve-prefix($subject, *)"/>
          <xsl:sequence
            select="
              basex-rdf:predicate-objects(
              for $v-o in (key('verb-subject-key', $subject) union key('object-subject-key', $subject))
              return
                $v-o
              )
              "/>
        </xsl:when>
        <xsl:when
          test="$bnode eq 'true' and normalize-space($type)">
          <xsl:variable
            name="type"
            select="basex-rdf:resolve-prefix($type, *)"/>
          <xsl:sequence
            select="
              for $bn in key('bnode-type-key', $type)
              return
                $bn
              "/>
        </xsl:when>
        <xsl:when
          test="normalize-space($datatype)">
          <xsl:sequence
            select="
              for $dt in key('datatype-literal-key', $datatype)
              return
                $dt
              "/>
        </xsl:when>
        <xsl:when
          test="normalize-space($lang)">
          <xsl:sequence
            select="
              for $lang in key('lang-literal-key', $lang)
              return
                $lang
              "/>
        </xsl:when>       
      </xsl:choose>
    </results>
  </xsl:template>

  <xsl:function
    as="xs:anyURI"
    name="basex-rdf:resolve-prefix">
    <xsl:param
      as="xs:anyURI"
      name="ref"/>
    <xsl:param
      as="node()"
      name="nodes"/>
    <xsl:variable
      name="ref-token"
      select="tokenize($ref, ':')"/>
    <xsl:variable
      name="key-test"
      select="key('prefix-key', $ref-token[1], $nodes)"/>
    <xsl:sequence
      select="
        if (normalize-space($key-test))
        then
          xs:anyURI($key-test || $ref-token[2])
        else
          $ref
        "/>
  </xsl:function>

  <xsl:function
    as="element()"
    name="basex-rdf:subject-verbs">
    <xsl:param
      as="item()*"
      name="seq"/>
    <subject-verbs>
      <xsl:sequence
        select="$seq"/>
    </subject-verbs>
  </xsl:function>

  <xsl:function
    as="element()"
    name="basex-rdf:subject-objects">
    <xsl:param
      as="item()*"
      name="seq"/>
    <subject-objects>
      <xsl:sequence
        select="$seq"/>
    </subject-objects>
  </xsl:function>

  <xsl:function
    as="element()"
    name="basex-rdf:predicate-objects">
    <xsl:param
      as="item()*"
      name="seq"/>
    <predicates>
      <xsl:sequence
        select="$seq"/>
    </predicates>
  </xsl:function>

  <xsl:template
    match="@* | node()"/>

</xsl:stylesheet>
