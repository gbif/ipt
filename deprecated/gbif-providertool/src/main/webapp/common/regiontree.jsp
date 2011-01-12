<%@ include file="/common/taglibs.jsp"%>

<div id="regionTreeBox"></div>
<script type="text/javascript">
	// http://wwwendt.de/tech/dynatree/doc/dynatree-doc.html#h5.3
	$("#regionTreeBox").dynatree({
		title: "Geography",
		rootVisible: false,
		autoFocus: false,
		selectMode: 1,
		persist: true,
		cookieId: "ipt-geotree",
		idPrefix: "geonode",
		fx: { height: "toggle", duration: 100 },
		initAjax: {	url: '<c:url value="/ajax/regionTree.do"/>?id=<s:property value="region_id"/>',
					data: {resourceId:<s:property value="resourceId"/>}
		},
		onLazyRead: function(dtnode){
			$.getJSON('<c:url value="/ajax/regionSubTree.do"/>',
					{resourceId:<s:property value="resourceId"/>, id:dtnode.data.key},
			        function(data){
						dtnode.setLazyNodeStatus(DTNodeStatus_Ok);
						dtnode.append(data);
			       	}
			);
		},			
		onActivate: function(dtnode) {
			var action = '<c:url value="/occRegion.html"/>';
			var url = action + '?resourceId=<s:property value="resourceId"/>&id='+dtnode.data.key;
			window.location.href=url;
		}
	});
</script>