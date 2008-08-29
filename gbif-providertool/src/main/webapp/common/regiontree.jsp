<%@ include file="/common/taglibs.jsp"%>

<div id="regionTreeBox"></div>
<script>
	var justOpened = 1;
	
	regiontree=new dhtmlXTreeObject($('regionTreeBox'),"100%","100%",0);
	regiontree.setImagePath("/scripts/dhtmlxtree/imgs/");
	regiontree.enableCheckBoxes(false);
	regiontree.enableDragAndDrop(false);
	regiontree.enableTreeImages(false);
	regiontree.enableHighlighting(true);
	regiontree.enableTreeLines(true);
	regiontree.attachEvent("onClick",onNodeSelect); //set function object to call on node select
	regiontree.attachEvent("onOpenStart",onNodeOpen); // onOpenStart 
	regiontree.setXMLAutoLoading("/ajax/regionTreeNodes.html?resource_id=<s:property value="resource_id"/>");
	regiontree.loadXML("/ajax/regionTreeNodes.html?resource_id=<s:property value="resource_id"/>"); //load root level from xml
	
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
