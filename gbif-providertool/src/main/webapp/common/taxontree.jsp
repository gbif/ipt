<%@ include file="/common/taglibs.jsp"%>

<div id="taxonTreeBox"></div>
<script>
	var justOpened = 1;
	
	taxtree=new dhtmlXTreeObject($('taxonTreeBox'),"100%","100%",0);
	taxtree.setImagePath("/scripts/dhtmlxtree/imgs/");
	taxtree.enableCheckBoxes(false);
	taxtree.enableDragAndDrop(false);
	taxtree.enableTreeImages(false);
	taxtree.enableHighlighting(true);
	taxtree.enableTreeLines(true);
	taxtree.attachEvent("onClick",onNodeSelect); //set function object to call on node select
	taxtree.attachEvent("onOpenStart",onNodeOpen); // onOpenStart 
	taxtree.setXMLAutoLoading("/ajax/taxonTreeNodes.html?resource_id=<s:property value="resource_id"/>");
	taxtree.loadXML("/ajax/taxonTreeNodes.html?resource_id=<s:property value="resource_id"/>"); //load root level from xml
	
	function onNodeSelect(nodeId){
		if (justOpened>0){
			//auto click when opening a new node. prevend this 
			justOpened=0;
		}else{
		
			// DO SOMETHING HERE
			
		}
	}
	
	function onNodeOpen(nodeId, state){
		// state: Current open state of tree item. 0 - item has not childs, -1 - item closed, 1 - item opened.
		if (state>-1){
			justOpened=1;
		}
		return true;
	}
</script>
