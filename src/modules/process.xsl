<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet exclude-result-prefixes="xs basex-rdf" version="3.0"
  xmlns:basex-rdf="https://metadatafram.es/basex/modules/rdf/graphs/"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:strip-space elements="*"/>

  <xsl:output indent="yes" method="xml"/>

  <xsl:key match="IRIREF" name="ns-key" use="../PNAME_NS"/>

  <xsl:template match="/">
    <g xml:id="{generate-id(.)}">
      <xsl:if test="trigDoc/directive">
        <c xml:id="{generate-id(trigDoc) || position()}">
          <xsl:apply-templates mode="directive" select="trigDoc/directive"/>
        </c>
      </xsl:if>
      <xsl:apply-templates select="trigDoc/block"/>
      <xsl:sequence
        select="
          basex-rdf:group-bnodes(trigDoc/block/triplesOrGraph/predicateObjectList/objectList/object)"
      />
    </g>
  </xsl:template>

  <xsl:template match="directive" mode="directive">
    <i p="{substring-before(prefixID/PNAME_NS, ':')}"
      xml:id="{generate-id(prefixID/IRIREF) || position()}">
      <xsl:apply-templates mode="directive"/>
    </i>
  </xsl:template>

  <xsl:function as="node()*" name="basex-rdf:group-bnodes">
    <xsl:param as="node()*" name="nodes"/>
    <xsl:for-each-group
      group-starting-with="descendant::*[1][self::TOKEN[. eq '[']]"
      select="$nodes//(collection union blankNodePropertyList)">
      <xsl:for-each select="current-group()">
        <b xml:id="{generate-id(.)}">
          <xsl:if test="ancestor::collection">
            <xsl:attribute name="n"
              select="count(../preceding-sibling::object) + 1"/>
          </xsl:if>
          <xsl:apply-templates mode="bnode"/>
        </b>
      </xsl:for-each>
    </xsl:for-each-group>
  </xsl:function>

  <xsl:template match="blankNodePropertyList"/>

  <xsl:template match="collection union blankNodePropertyList" mode="bnode">
    <b r="{generate-id(.)}">
      <xsl:if test="ancestor::collection">
        <xsl:attribute name="n"
          select="count(../preceding-sibling::object) + 1"/>
      </xsl:if>
    </b>
  </xsl:template>

  <xsl:template match="TOKEN[. eq '[']" mode="bnode">
    <s xml:id="{generate-id(.) || position()}"/>
  </xsl:template>

  <xsl:template match="block">
    <t xml:id="{generate-id(.) || position()}">
      <s xml:id="{generate-id(triplesOrGraph/labelOrSubject) || position()}">
        <xsl:apply-templates select="triplesOrGraph/labelOrSubject"/>
      </s>
      <xsl:apply-templates mode="bnode"
        select="triplesOrGraph/predicateObjectList"/>
    </t>
  </xsl:template>

  <xsl:template match="predicateObjectList"/>

  <xsl:template match="predicateObjectList" mode="bnode">
    <xsl:choose>
      <xsl:when test="count(descendant::objectList) gt 1">
        <p xml:id="{generate-id(.) || position()}">
          <xsl:apply-templates mode="bnode" select="objectList"/>
        </p>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="bnode" select="objectList"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="objectList/object" mode="bnode">
    <xsl:variable name="v" select="../preceding-sibling::*[1][self::verb]"/>
    <p xml:id="{generate-id(..) || position()}">
      <v xml:id="{generate-id($v) || position()}">
        <xsl:apply-templates select="$v"/>
      </v>
      <xsl:choose>
        <xsl:when test="literal">
          <l xml:id="{generate-id(.) || position()}">
            <xsl:if test=".//LANGTAG">
              <xsl:attribute name="xml:lang"
                select="substring-after(.//LANGTAG, '@')"/>
            </xsl:if>
            <xsl:variable name="val">
              <xsl:analyze-string regex="(&apos;|&quot;)(.*?)\1"
                select="normalize-space(.)">
                <xsl:matching-substring>
                  <xsl:value-of select="normalize-space(regex-group(2))"/>
                </xsl:matching-substring>
              </xsl:analyze-string>
            </xsl:variable>
            <xsl:choose>
              <xsl:when test="literal/RDFLiteral[TOKEN = '^^']">
                <xsl:variable name="type"
                  select="data(literal/RDFLiteral/iri)"/>
                <xsl:attribute name="d"
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
                <xsl:value-of select="$val"/>
              </xsl:when>
              <xsl:when test="$val castable as xs:date">
                <xsl:attribute name="d">xs:date</xsl:attribute>
                <xsl:value-of select="$val"/>
              </xsl:when>
              <xsl:when test="$val castable as xs:dateTime">
                <xsl:attribute name="d">xs:dateTime</xsl:attribute>
                <xsl:value-of select="$val"/>
              </xsl:when>
              <xsl:when test="$val castable as xs:integer">
                <xsl:attribute name="d">xs:integer</xsl:attribute>
                <xsl:value-of select="$val"/>
              </xsl:when>
              <xsl:when test="$val castable as xs:double">
                <xsl:attribute name="d">xs:double</xsl:attribute>
                <xsl:value-of select="$val"/>
              </xsl:when>
              <xsl:when test="$val castable as xs:string">
                <xsl:attribute name="d">xs:string</xsl:attribute>
                <xsl:value-of select="$val"/>
              </xsl:when>
            </xsl:choose>
          </l>
        </xsl:when>
        <xsl:otherwise>
          <o xml:id="{generate-id(.) || position()}">
            <xsl:apply-templates mode="bnode"/>
          </o>
        </xsl:otherwise>
      </xsl:choose>
    </p>
  </xsl:template>

  <xsl:template match="prefixID"/>

  <xsl:template match="PNAME_NS" mode="directive"/>

  <xsl:template match="IRIREF" mode="#all">
    <xsl:sequence
      select="
        substring-before(substring-after(., '&lt;'), '&gt;')"/>
  </xsl:template>

  <xsl:template match="PNAME_LN" mode="#all">
    <xsl:choose>
      <xsl:when test="not(matches(., 'http'))">
        <xsl:variable name="pname"
          select="key('ns-key', concat(substring-before(., ':'), ':'))"/>
        <xsl:sequence
          select="
            (if (starts-with($pname, '&lt;') and ends-with($pname, '&gt;'))
            then
              substring-before(substring-after($pname, '&lt;'), '&gt;')
            else
              $pname) || substring-after(., ':')"
        />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="TOKEN[. eq 'a']" mode="#all">
    <xsl:value-of>http://www.w3.org/1999/02/22-rdf-syntax-ns#type</xsl:value-of>
  </xsl:template>

  <xsl:template match="TOKEN[. ne 'a' and . ne '[']" mode="#all"/>

  <xsl:template match="text()[preceding-sibling::* or following-sibling::*]">
    <xsl:analyze-string regex="(#.*?\n)" select=".">
      <xsl:matching-substring>
        <xsl:comment>
            <xsl:sequence select="normalize-space(regex-group(1))"/>
        </xsl:comment>
      </xsl:matching-substring>
    </xsl:analyze-string>
  </xsl:template>

  <xsl:function as="xs:string" name="basex-rdf:type">
    <xsl:param as="xs:string" name="v"/>
    <xsl:choose>
      <xsl:when test="$v eq 'a'">
        <xsl:value-of>rdf:type</xsl:value-of>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="$v"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
