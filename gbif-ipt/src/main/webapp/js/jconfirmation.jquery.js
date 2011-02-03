(function($){

	jQuery.fn.jConfirmAction = function (options) {
		
		// Some jConfirmAction options (limited to customize language) :
		// question : a text for your question.
		// yesAnswer : a text for Yes answer.
		// cancelAnswer : a text for Cancel/No answer.
		var theOptions = jQuery.extend ({
			question: "Are You Sure ?",
			yesAnswer: "Yes",
			cancelAnswer: "Cancel"
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

					$('#dialog-confirm').html('<p>'+theOptions.question+'</p>');
					$('#dialog-confirm').dialog({
						resizable: false,
						modal: true,
						buttons: btns
					});		
					
				};
			});
			
		});
	}
	
})(jQuery);