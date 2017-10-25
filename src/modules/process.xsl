<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
  exclude-result-prefixes="xs trip"
  version="3.0"
  xmlns:trip="https://metadatafram.es/basex/modules/rdf/triples/"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:strip-space
    elements="*"/>

  <xsl:output
    indent="no"
    method="xml"/>

  <xsl:key
    match="IRIREF"
    name="ns-key"
    use="../PNAME_NS"/>
  <xsl:template
    match="/">
    <triples
      xid="">
      <prefixes
        xid="">
        <xsl:apply-templates
          mode="directive"
          select="trigDoc/directive"/>
      </prefixes>
      <xsl:apply-templates
        select="trigDoc"/>
    </triples>
  </xsl:template>

  <xsl:template
    match="directive"
    mode="directive">
    <iriref
      prefix="{substring-before(prefixID/PNAME_NS, ':')}"
      xid="">
      <xsl:apply-templates
        mode="directive"/>
    </iriref>
  </xsl:template>

  <xsl:template
    match="triplesOrGraph">
    <statement
      xid="">
      <xsl:apply-templates/>
    </statement>
  </xsl:template>

  <xsl:template
    match="labelOrSubject">
    <subject
      xid="">
      <xsl:apply-templates/>
    </subject>
  </xsl:template>

  <xsl:template
    match="verb">
    <predicate
      xid="">
      <verb
        xid="">
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
        <literal
          xid="">
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
        <object
          xid="">
          <xsl:apply-templates/>
        </object>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template
    match="object"
    mode="blank">
    <object
      xid="">
      <subject
        xid=""/>
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
  </xsl:template>

</xsl:stylesheet>
