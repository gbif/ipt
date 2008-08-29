<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
	<link rel="STYLESHEET" type="text/css" href="/scripts/dhtmlxtree/dhtmlxtree.css">
	<script  src="/scripts/dhtmlxtree/dhtmlxcommon.js"></script>
	<script  src="/scripts/dhtmlxtree/dhtmlxtree.js"></script>
</head>
<body>

<div id="taxonTreeBox" style="width:200;height:200"></div>
<script>
	var justOpened = 1;
	
	tree=new dhtmlXTreeObject($('taxonTreeBox'),"100%","100%",0);
	tree.setImagePath("/scripts/dhtmlxtree/imgs/");
	tree.enableCheckBoxes(false);
	tree.enableDragAndDrop(false);
	tree.enableTreeImages(false);
	tree.enableHighlighting(true);
	tree.enableTreeLines(true);
	tree.attachEvent("onClick",onNodeSelect); //set function object to call on node select
	tree.attachEvent("onOpenStart",onNodeOpen); // onOpenStart 
	tree.setXMLAutoLoading("/ajax/taxonTreeNodes.html?resource_id=<s:property value="resource_id"/>");
	tree.loadXML("/ajax/taxonTreeNodes.html?resource_id=<s:property value="resource_id"/>"); //load root level from xml
	
	function onNodeSelect(nodeId){
		if (justOpened>0){
			//auto click when opening a new node. prevend this 
			justOpened=0;
		}else{
			alert("Real opening of node " + nodeId);
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

</body>
</html>