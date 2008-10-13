<%@ include file="/common/taglibs.jsp"%>
<s:set name="eml" value="eml" scope="request"/>
<fieldset class="metadata" id="taxonomicCoverage">
	<legend><s:text name="eml.dataset.coverage.taxonomicCoverage"/></legend>
	<input type="hidden" name="eml.dataset.coverage.taxonomicCoverage.scope" value="document"/>
	<fieldset id="classificationSystem" class="classificationSystem">
		<legend><s:text name="eml.dataset.coverage.taxonomicCoverage.taxonomicSystem.classificationSystem.classificationSystemCitation"/></legend>	
		<s:textfield key="eml.dataset.coverage.taxonomicCoverage.taxonomicSystem.classificationSystem.classificationSystemCitation.title" required="true" cssClass="text large"/>
		<s:textfield key="eml.dataset.coverage.taxonomicCoverage.taxonomicSystem.classificationSystem.classificationSystemCitation.creator" required="false" cssClass="text large"/>
		<s:textfield key="eml.dataset.coverage.taxonomicCoverage.taxonomicSystem.classificationSystem.classificationSystemCitation.book" required="false" cssClass="text large"/>
	</fieldset>

	<s:textfield key="eml.dataset.coverage.taxonomicCoverage.taxonomicSystem.identificationReference" required="false" cssClass="text large"/>
	<s:textfield key="eml.dataset.coverage.taxonomicCoverage.taxonomicSystem.identifierName" required="false" cssClass="text large"/>		
	<s:textfield key="eml.dataset.coverage.taxonomicCoverage.taxonomicSystem.taxonomicProcedures" required="false" cssClass="text large"/>		
	<s:textfield key="eml.dataset.coverage.taxonomicCoverage.taxonomicSystem.taxonomicCompleteness" required="false" cssClass="text large"/>		
	<script type="text/javascript">
		function copyClassification(theClassification, removeButtonValue){
			//top level = TBODY
			var topLevel = theClassification.parentNode.parentNode.parentNode;
			var ps = topLevel.getElementsByTagName("TR");
			var toCopy = ps[0];
			var clone = toCopy.cloneNode(true);
			var cells = clone.getElementsByTagName("TD");
			var removeButton = cells[cells.length-1].getElementsByTagName("INPUT")[0];
			removeButton.style.visibility = "visible";
			toCopy.parentNode.appendChild(clone);
		}
		
		function removeClassification(node) {
			var tr = node.parentNode;
			tr.removeChild(node);
		
		}
	</script>
	<li id="wwgrp_editCoverageForm_eml_dataset_taxonomicCoverage" class="wwgrp">
		<div id="wwlbl_editCoverageForm_eml_dataset_taxonomicCoverage" class="wwlbl">
			<label for="editCoverageForm_eml_dataset_taxonomicCoverage" class="desc">
				<s:text name="eml.dataset.coverage.taxonomicCoverage.taxonomicClassification"/><span class="req">*</span>
			</label>        
		</div> 
		<div id="wwctrl_editCoverageForm_eml_dataset_taxonomicCoverage">
			<s:set name="taxonomicCoverage" value="eml.dataset.coverage.taxonomicCoverage"/>
			<%			
				org.gbif.metadata.eml.TaxonomicCoverage tc = (org.gbif.metadata.eml.TaxonomicCoverage) pageContext.getAttribute("taxonomicCoverage");
				if(tc!=null){
					pageContext.setAttribute("taxaSize", tc.sizeTaxonomicClassificationList());
				} else{
					pageContext.setAttribute("taxaSize", new Integer(0));
				}
			%>	

		<table id="classificationTable">
			<thead>
				<tr>
					<th>Rank</th>
					<th>Taxon</th>
					<th>Common name</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
				</tr>
			</thead>
			<tbody>
		<c:choose>
			<c:when test="${taxaSize>0}">
			<c:forEach begin="0" end="${taxaSize-1}" varStatus="taxaStatus">
				<c:set var="taxaIndex" value="${taxaStatus.index}"/>
				<%
					Integer taxaIndex = (Integer) pageContext.getAttribute("taxaIndex");
					pageContext.setAttribute("taxonomicClassification", tc.getTaxonomicClassification(taxaIndex));
				%>	
				<tr>
					<td style="vertical-align:top;">
					<s:set name="taxonRanks" value="taxonRanks"/>
					<select id="selectedRank" name="selectedRank" class="text medium">
						<s:iterator value="majorTaxonRanks" status="taxonRank">
							<s:set name="rank"><s:property/></s:set>
							<option<c:if test="${taxonomicClassification.taxonRankName==rank}"> selected="true"</c:if>>
								<s:text name="%{rank}"/>
							</option>
						</s:iterator>
						<optgroup label="Other ranks">
						<s:iterator value="otherTaxonRanks" status="taxonRank">
							<s:set name="rank"><s:property/></s:set>
							<option<c:if test="${taxonomicClassification.taxonRankName==rank}"> selected="true"</c:if>>
								<s:text name="%{rank}"/>
							</option>
						</s:iterator>
						</optgroup>
					</select>
					</td>
					<td>
						<input id="selectedTaxon" type="text" name="selectedTaxon" value="${taxonomicClassification.taxonRankValue}" class="text medium"/>
					</td>
					<td>
						<input id="selectedCommonName" type="text" name="selectedCommonName" value="${taxonomicClassification.commonName}" class="text medium"/>
						<input id="classification" type="hidden" name="classification" value=""/>
					</td>
					<td>
						<input type="button" value="<s:text name="button.add"/>" onclick="javascript:copyClassification(this, '<s:text name="button.remove"/>');"/>
					</td>
					<td>
						<input type="button" value="<s:text name="button.remove"/>"<c:if test="${tclStatus.index>0}"> style="visibility:hidden" </c:if>onclick="javascript:removeClassification(this.parentNode.parentNode);"/>
					</td>
				</tr>	
			</c:forEach>
			</c:when>
			<c:otherwise>
				<tr class="coveredTaxon" style="vertical-align:top;">
					<td style="vertical-align:top;">
						<s:set name="taxonRanks" value="taxonRanks"/>
						<select id="selectedRank" name="selectedRank" class="text medium">
							<s:iterator value="majorTaxonRanks" status="taxonRank">
								<s:set name="rank"><s:property/></s:set>
								<option><s:text name="%{rank}"/></option>
							</s:iterator>
							<optgroup label="Other ranks">
							<s:iterator value="otherTaxonRanks" status="taxonRank">
								<s:set name="rank"><s:property/></s:set>
								<option><s:text name="%{rank}"/></option>
							</s:iterator>
							</optgroup>
						</select>
					</td>
					<td style="vertical-align:top;">
						<input id="ysearchinput" type="text" name="selectedTaxon" value="" class="text medium"/>
					</td> 
					<td style="vertical-align:top;">
						<input id="selectedCommonName" type="text" name="selectedCommonName" value="" class="text medium"/>
						<input id="classification" type="hidden" name="classification" value=""/>
					</td>
					<td style="vertical-align:top;">
						<input type="button" value="<s:text name="button.add"/>" onclick="javascript:copyClassification(this, '<s:text name="button.remove"/>');"/>
					</td>
					<td style="vertical-align:top;">
						<input type="button" style="visibility:hidden;" value="<s:text name="button.remove"/>" onclick="javascript:removeClassification(this.parentNode.parentNode);"/>
					</td>
				</tr>	
			</c:otherwise>
		</c:choose>		
			</tbody>	
			</table>
		</div>
	</li>
</fieldset>

<script type="text/javascript">
/* Sets the hidden INPUT value */
function prepareForm(){
	var taxonCoverage = document.getElementById('classificationTable');
	var coveredTaxa = taxonCoverage.getElementsByTagName("TR");
	for(var i=0; i<coveredTaxa.length; i++){
		var classification="";
		var cells = coveredTaxa[i].getElementsByTagName('TD');
		if(cells!=null && cells.length>0 && cells[0]!=null && cells[0].getElementsByTagName('SELECT')!=null){
			var rank = cells[0].getElementsByTagName('SELECT')[0].value;
			var taxon = cells[1].getElementsByTagName('INPUT')[0].value;
			var commonName = cells[2].getElementsByTagName('INPUT')[0].value;
			cells[2].getElementsByTagName('INPUT')[1].value = rank +'|'+taxon+"|"+commonName;
		}
	}
}
</script>