<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
	<link rel="STYLESHEET" type="text/css" href="../../codebase/dhtmlxtree.css">
	<script  src="/scripts/dhtmlxtree/dhtmlxcommon.js"></script>
	<script  src="/scripts/dhtmlxtree/dhtmlxtree.js"></script>
	<!-- script  src="/scripts/dhtmlxtree/ext/dhtmlxtree_ed.js"></script -->

 
</head>
<body>

<div id="taxonTreeBox" style="width:200;height:200"></div>
<script>
	tree=new dhtmlXTreeObject($('taxonTreeBox'),"100%","100%",0);
	tree.setImagePath("/scripts/dhtmlxtree/imgs/");
	tree.enableCheckBoxes(false);
	tree.enableDragAndDrop(false);
	tree.attachEvent("onClick",onNodeSelect); //set function object to call on node select
	tree.setXMLAutoLoading("/ajax/taxonTreeNodes.html?resource_id=<s:property value="resource_id"/>");
	tree.loadXML("/ajax/taxonTreeNodes.html?resource_id=<s:property value="resource_id"/>"); //load root level from xml
	
	function onNodeSelect(nodeId){
		alert("clicked node "+nodeId);
	}
</script>

</body>
</html>