<catalog >
  <datastores >
    <!--
      a datastore configuration element serves as a common data source connection
      parameters repository for all featuretypes it holds.
    -->
    <datastore namespace = "gbif" enabled = "true" id = "ipt" >
      <connectionParams >
        <parameter value = "http://gbif.org" name = "namespace" />
        <parameter value = "${cfg.getDataDir()}" name = "datadir" />
      </connectionParams>
    </datastore>
  </datastores>
  <formats >
    <!--
      a format configuration element serves as a common data source
      parameters repository for all coverages it holds.
    -->
  </formats>
  <!--
    Defines namespaces to be used by the datastores.
  -->
  <namespaces >
    <namespace uri = "http://gbif.org" prefix = "gbif" default = "true" />
  </namespaces>
  <styles >
    <!--
      Defines the style ids and file name to be used by the wms.
    -->
    <style filename = "occurrencePoint.sld" id = "occurrencePoint" />
  </styles>
</catalog>
