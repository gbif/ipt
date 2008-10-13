<%@ include file="/common/taglibs.jsp"%>
<h1><s:text name="eml.dataset"/> - <s:text name="eml.dataset.coverage.geographicCoverage.title"/></h1>
<c:set var="northValue" value="${eml.dataset.coverage.geographicCoverage.boundingCoordinates.northBoundingCoordinate}" scope="request"/>			
<c:set var="southValue" value="${eml.dataset.coverage.geographicCoverage.boundingCoordinates.southBoundingCoordinate}" scope="request"/>
<c:set var="eastValue" value="${eml.dataset.coverage.geographicCoverage.boundingCoordinates.eastBoundingCoordinate}" scope="request"/>
<c:set var="westValue" value="${eml.dataset.coverage.geographicCoverage.boundingCoordinates.westBoundingCoordinate}" scope="request"/>												
<c:set var="polygonValue" value="${eml.dataset.coverage.geographicCoverage.datasetGPolygon.datasetGPolygonExclusionGRing.gRing}" scope="request"/>
<c:set var="largeMap" value="true" scope="request"/>
<jsp:include page="openLayers.jsp"></jsp:include>
<s:form id="editGeographicCoverageForm" action="initGeographicCoverage" method="post" validate="false">
<fieldset id="geographicCoverage" class="metadata">
<legend><s:text name="eml.dataset.coverage.geographicCoverage"/></legend>
<input type="hidden" name="eml.dataset.coverage.geographicCoverage.scope" value="document"/>
<s:textfield key="eml.dataset.coverage.geographicCoverage.geographicDescription" required="false" cssClass="text large"/>	
<s:textfield id="northCoordinateInput" key="eml.dataset.coverage.geographicCoverage.boundingCoordinates.northBoundingCoordinate" required="false" cssClass="text medium"/>
<s:textfield id="southCoordinateInput" key="eml.dataset.coverage.geographicCoverage.boundingCoordinates.southBoundingCoordinate" required="false" cssClass="text medium"/>
<s:textfield id="eastCoordinateInput" key="eml.dataset.coverage.geographicCoverage.boundingCoordinates.eastBoundingCoordinate" required="false" cssClass="text medium"/>
<s:textfield id="westCoordinateInput" key="eml.dataset.coverage.geographicCoverage.boundingCoordinates.westBoundingCoordinate" required="false" cssClass="text medium"/>
<s:textfield id="polygonInput" key="eml.dataset.coverage.geographicCoverage.datasetGPolygon.datasetGPolygonExclusionGRing.gRing" required="false" cssClass="text large"/>
</fieldset>
<li class="buttonBar bottom">
<s:submit cssClass="button" key="button.cancel" theme="simple" method="cancel"/>
<s:submit cssClass="button" key="button.back" theme="simple" method="back"/>
<s:submit cssClass="button" key="button.next" theme="simple"/>
</li>
</s:form>