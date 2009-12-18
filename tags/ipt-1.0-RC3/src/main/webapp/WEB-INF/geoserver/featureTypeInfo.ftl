<featureType datastore = "ipt" >
  <name>resource${id}</name>
  <!--
    native wich EPGS code for the FeatureTypeInfoDTO
  -->
  <SRS>4326</SRS>
  <SRSHandling>0</SRSHandling>
  <title>${title}</title>
  <abstract>${description!}</abstract>
  <wmspath>/</wmspath>
  <numDecimals value = "0" />
  <keywords>IPT GBIF <#list keywords as k> ${k!""}</#list></keywords>
  <latLonBoundingBox dynamic = "false" miny = "${(bbox.min.y)!-90}" maxy = "${(bbox.max.y)!90}" maxx = "${(bbox.max.x)!180}" minx = "${(bbox.min.x)!-180}" />
  <nativeBBox dynamic = "false" miny = "-90.0" maxy = "90.0" maxx = "180.0" minx = "-180.0" />
  <!--
    the default style this FeatureTypeInfoDTO can be represented by.
    at least must contain the "default" attribute
  -->
  <styles default = "occurrencePoint" />
  <cacheinfo enabled = "false" maxage = "" />
  <searchable enabled = "false" />
  <regionateAttribute value = "" />
  <regionateStrategy value = "" />
  <regionateFeatureLimit value = "10" />
  <maxFeatures>0</maxFeatures>
</featureType>
