<%@ include file="/common/taglibs.jsp"%>

<div id="taxonTreeBox"></div>
	<script type="text/javascript">
		// http://wwwendt.de/tech/dynatree/doc/dynatree-doc.html#h5.3
		$("#taxonTreeBox").dynatree({
			title: "Taxonomy",
			rootVisible: false,
			autoFocus: false,
			selectMode: 1,
			persist: true,
			cookieId: "ipt-taxtree",
			idPrefix: "taxnode",
			fx: { height: "toggle", duration: 100 },
			initAjax: {	url: '<c:url value="/ajax/taxonTree.do"/>?id=<s:property value="taxon_id"/>',
						data: {resource_id:"<s:property value='resource_id'/>"}
			},
			onLazyRead: function(dtnode){
				$.getJSON('<c:url value="/ajax/taxonSubTree.do"/>',
						{resource_id:<s:property value='resource_id'/>, id:dtnode.data.key},
				        function(data){
							dtnode.setLazyNodeStatus(DTNodeStatus_Ok);
							dtnode.append(data);
				       	}
				);
			},			
			onActivate: function(dtnode) {
				var action = '<c:url value="/${taxTreeAction}.html"/>';
				var taxonUrl = action + '?resource_id=<s:property value="resource_id"/>&id='+dtnode.data.key;
				window.location.href=taxonUrl;
			}
		});
	</script>
