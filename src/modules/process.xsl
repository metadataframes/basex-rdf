<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  exclude-result-prefixes="xs basex-rdf"
  version="3.0"
  xmlns:basex-rdf="https://metadatafram.es/basex/modules/rdf/graphs/"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:strip-space
    elements="*"/>

  <xsl:output
    indent="yes"
    method="xml"/>

  <xsl:key
    match="IRIREF"
    name="ns-key"
    use="../PNAME_NS"/>

  <xsl:template
    match="/">
    <graph>
      <context>
        <xsl:apply-templates
          mode="directive"
          select="trigDoc/directive"/>
      </context>
      <xsl:apply-templates
        select="trigDoc/block"/>
      <xsl:sequence
        select="
          basex-rdf:group-bnodes(trigDoc/block/triplesOrGraph/predicateObjectList/objectList/object)"/>
    </graph>
  </xsl:template>
  
  <xsl:template
    match="directive"
    mode="directive">
    <iri
      prefix="{substring-before(prefixID/PNAME_NS, ':')}">
      <xsl:apply-templates
        mode="directive"/>
    </iri>
  </xsl:template>

  <xsl:function
    as="node()*"
    name="basex-rdf:group-bnodes">
    <xsl:param
      as="node()*"
      name="nodes"/>
    <xsl:for-each-group
      group-starting-with="descendant::*[1][self::TOKEN[. eq '[']]"
      select="$nodes//(collection union blankNodePropertyList)">
      <xsl:for-each
        select="current-group()">
        <bn
          xml:id="{concat('_:', generate-id(.))}">          
          <xsl:if
            test="ancestor::collection">
            <xsl:attribute
              name="n"
              select="count(../preceding-sibling::object) + 1"/>
          </xsl:if>
          <xsl:apply-templates
            mode="bnode"/>
        </bn>
      </xsl:for-each>
    </xsl:for-each-group>
  </xsl:function>

  <xsl:template
    match="blankNodePropertyList"/>

  <xsl:template
    match="collection union blankNodePropertyList"
    mode="bnode">
    <bn
      ref="{concat('_:', generate-id(.))}">
      <xsl:if
        test="ancestor::collection">
        <xsl:attribute
          name="n"
          select="count(../preceding-sibling::object) + 1"/>
      </xsl:if>
    </bn>
  </xsl:template>

  <xsl:template
    match="TOKEN[. eq '[']"
    mode="bnode">
    <s/>
  </xsl:template>

  <xsl:template
    match="block">
    <topic>
      <s>
        <xsl:apply-templates
          select="triplesOrGraph/labelOrSubject"/>
      </s>
      <xsl:apply-templates
        mode="bnode"
        select="triplesOrGraph/predicateObjectList"/>
    </topic>
  </xsl:template>

  <xsl:template
    match="predicateObjectList"/>

  <xsl:template
    match="predicateObjectList"
    mode="bnode">
    <predicates>
      <xsl:apply-templates
        mode="bnode"
        select="objectList"/>
    </predicates>
  </xsl:template>

  <xsl:template
    match="objectList/object"
    mode="bnode">
    <p>
      <v>
        <xsl:value-of
          select="basex-rdf:type(../preceding-sibling::*[1][self::verb])"/>
      </v>
      <o>
        <xsl:if
          test=".//ancestor::collection">
          <xsl:attribute
            name="parse-type">collection</xsl:attribute>
        </xsl:if>
        <xsl:apply-templates
          mode="bnode"/>
      </o>
    </p>
  </xsl:template>

  <xsl:template
    match="TOKEN"
    mode="#all">
    <xsl:sequence
      select="
        if (current() eq 'a')
        then
          basex-rdf:type(current())
        else
          ()
        "/>
  </xsl:template>

  <xsl:function
    as="xs:anyURI"
    name="basex-rdf:type">
    <xsl:param
      as="xs:string"
      name="v"/>
    <xsl:sequence
      select="
        if ($v eq 'a')
        then
          xs:anyURI('rdf:type')
        else
          xs:anyURI($v)
        "/>
  </xsl:function>



  <!--

  <xsl:template
    match="triplesOrGraph">
    <statements>
      <xsl:apply-templates/>
    </statements>
  </xsl:template>

  <xsl:template
    match="labelOrSubject">
    <subject>
      <xsl:apply-templates/>
    </subject>
  </xsl:template>

  <xsl:template
    match="verb">
    <predicate>
      <verb>
        <xsl:apply-templates/>
      </verb>
      <xsl:apply-templates
        mode="object"
        select="following-sibling::*[1][self::objectList]/object[not(blankNodePropertyList)]"/>
      <xsl:apply-templates
        mode="blank"
        select="following-sibling::*[1][self::objectList]/object[blankNodePropertyList]"/>
    </predicate>
  </xsl:template>

  <xsl:template
    match="object"
    mode="object">
    <xsl:choose>
      <xsl:when
        test="literal">
        <literal>
          <xsl:if
            test=".//LANGTAG">
            <xsl:attribute
              name="xml:lang"
              select="substring-after(.//LANGTAG, '@')"/>
          </xsl:if>
          <xsl:variable
            name="val">
            <xsl:analyze-string
              regex="(&apos;|&quot;)(.*?)\1"
              select="normalize-space(.)">
              <xsl:matching-substring>
                <xsl:value-of
                  select="normalize-space(regex-group(2))"/>
              </xsl:matching-substring>
            </xsl:analyze-string>
          </xsl:variable>
          <xsl:choose>
            <xsl:when
              test="literal/RDFLiteral[TOKEN = '^^']">
              <xsl:variable
                name="type"
                select="data(literal/RDFLiteral/iri)"/>
              <xsl:attribute
                name="datatype"
                select="
                  if (starts-with($type, '&lt;') and ends-with($type, '&gt;'))
                  then
                    substring-before(substring-after($type, '&lt;'), '&gt;')
                  else
                    (
                    if (contains($type, 'xsd:'))
                    then
                      'xs:' || substring-after($type, ':')
                    else
                      $type
                    )
                  "/>
              <xsl:value-of
                select="$val"/>
            </xsl:when>
            <xsl:when
              test="$val castable as xs:date">
              <xsl:attribute
                name="datatype">xs:date</xsl:attribute>
              <xsl:value-of
                select="$val"/>
            </xsl:when>
            <xsl:when
              test="$val castable as xs:dateTime">
              <xsl:attribute
                name="datatype">xs:dateTime</xsl:attribute>
              <xsl:value-of
                select="$val"/>
            </xsl:when>
            <xsl:when
              test="$val castable as xs:integer">
              <xsl:attribute
                name="datatype">xs:integer</xsl:attribute>
              <xsl:value-of
                select="$val"/>
            </xsl:when>
            <xsl:when
              test="$val castable as xs:double">
              <xsl:attribute
                name="datatype">xs:double</xsl:attribute>
              <xsl:value-of
                select="$val"/>
            </xsl:when>
            <xsl:when
              test="$val castable as xs:string">
              <xsl:attribute
                name="datatype">xs:string</xsl:attribute>
              <xsl:value-of
                select="$val"/>
            </xsl:when>
          </xsl:choose>
        </literal>
      </xsl:when>
      <xsl:when
        test="iri">
        <object>
          <xsl:apply-templates/>
        </object>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template
    match="object"
    mode="blank">
    <object>
      <subject/>
      <xsl:apply-templates/>
    </object>
  </xsl:template>

  <xsl:template
    match="PNAME_LN">
    <xsl:variable
      name="pname"
      select="key('ns-key', concat(substring-before(., ':'), ':'))"/>
    <xsl:value-of
      select="
        (if (starts-with($pname, '&lt;') and ends-with($pname, '&gt;'))
        then
          substring-before(substring-after($pname, '&lt;'), '&gt;')
        else
          $pname) || substring-after(., ':')
        "/>
  </xsl:template>

  <xsl:template
    match="object"/>

  <xsl:template
    match="prefixID/TOKEN"
    mode="directive"/>

  <xsl:template
    match="prefixID"/>

  <xsl:template
    match="PNAME_NS"
    mode="directive"/>

  <xsl:template
    match="IRIREF"
    mode="#all">
    <xsl:sequence
      select="
        substring-before(substring-after(., '&lt;'), '&gt;')"/>
  </xsl:template>

  <xsl:template
    match="TOKEN[. = 'a']">
    <xsl:text>http://www.w3.org/1999/02/22-rdf-syntax-ns#type</xsl:text>
  </xsl:template>

  <xsl:template
    match="TOKEN[. != 'a']"/>

  <xsl:template
    match="text()[preceding-sibling::* or following-sibling::*]">
    <xsl:analyze-string
      regex="(#.*?\n)"
      select=".">
      <xsl:matching-substring>
        <xsl:comment>
            <xsl:sequence select="normalize-space(regex-group(1))"/>
        </xsl:comment>
      </xsl:matching-substring>
    </xsl:analyze-string>
  </xsl:template>-->

</xsl:stylesheet>
