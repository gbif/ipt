(function($){

	jQuery.fn.jConfirmAction = function (options) {
		
		// Some jConfirmAction options (limited to customize language) :
		// question : a text for your question.
		// yesAnswer : a text for Yes answer.
		// cancelAnswer : a text for Cancel/No answer.
		// checkboxText: a text for the checkbox needed to confirm for Yes answer (optional)
		var theOptions = jQuery.extend ({
			question: "Are You Sure ?",
			yesAnswer: "Yes",
			cancelAnswer: "Cancel",
			checkboxText: undefined
		}, options);
		return this.each (function () {
			
			$(this).bind('click', function(e) {
				var submitBtn = $(this); 
				if ($(this).attr("jconfirmed")){
					submitBtn.removeAttr("jconfirmed");
				}else{
					e.preventDefault();
					thisHref = $(this).attr('href');
					var btns = {};
					btns[theOptions.yesAnswer]=function() {								
						$( this ).dialog( "close" );					
						if (thisHref!=null){
							window.location = thisHref;
						}else{
							submitBtn.attr("jconfirmed", true);
							submitBtn.click();
						}
					}; 
					btns[theOptions.cancelAnswer]=function() {								
						$( this ).dialog( "close" );					
						submitBtn.removeAttr("jconfirmed");
					};
					
					var content='<p>'+theOptions.question+'</p>';
					if(theOptions.checkboxText!=undefined){
						content='<p>'+'<input type="checkbox" id="cbox">'+theOptions.checkboxText+'<br/><br/>'+theOptions.question+'</p>';
					}					
										
					$('#dialog-confirm').html(content);
					$('#cbox').click(function() {
						if($('#cbox').attr("checked")){
							$('.ui-dialog-buttonset button:first-child').show();
						}else{
							$('.ui-dialog-buttonset button:first-child').hide();
						}

					});
					$('#dialog-confirm').dialog({
						resizable: false,
						modal: true,
						buttons: btns
					});		
					
					if(theOptions.checkboxText!=undefined){
						$('.ui-dialog-buttonset button:first-child').hide();
					}
					
				};
			});
			
		});
	}
	
})(jQuery);