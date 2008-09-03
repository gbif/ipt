<%@ include file="/common/taglibs.jsp"%>

<div id="taxonTreeBox"></div>
<script>
	var resourceId = <s:property value="resource_id"/>;
	var taxJustOpened = 1;
	
	taxtree=new dhtmlXTreeObject($('taxonTreeBox'),"100%","100%",0);
	taxtree.setImagePath("/scripts/dhtmlxtree/imgs/");
	taxtree.enableCheckBoxes(false);
	taxtree.enableDragAndDrop(false);
	taxtree.enableTreeImages(false);
	taxtree.enableHighlighting(true);
	taxtree.enableTreeLines(true);
	taxtree.attachEvent("onClick",onTaxNodeSelect); //set function object to call on node select
	taxtree.attachEvent("onOpenStart",onTaxNodeOpen); // onOpenStart 
	taxtree.setXMLAutoLoading("/ajax/taxonTreeNodes.html?resource_id=<s:property value="resource_id"/>");
	taxtree.loadXML("/ajax/taxonTreeNodes.html?resource_id=<s:property value="resource_id"/>"); //load root level from xml
	
	function onTaxNodeSelect(nodeId){
		if (taxJustOpened>0){
			//auto click when opening a new node. prevend this 
			taxJustOpened=0;
		}else{
			// only open terminal nodes
			if (taxtree.hasChildren(nodeId)<1){
				var taxonUrl = '/occTaxon.html?resource_id='+resourceId+'&id='+nodeId;
				window.location.href=taxonUrl;
			}
		}
	}
	
	function onTaxNodeOpen(nodeId, state){
		// state: Current open state of tree item. 0 - item has not childs, -1 - item closed, 1 - item opened.
		if (state>-1){
			taxJustOpened=1;
		}
		return true;
	}
</script>
