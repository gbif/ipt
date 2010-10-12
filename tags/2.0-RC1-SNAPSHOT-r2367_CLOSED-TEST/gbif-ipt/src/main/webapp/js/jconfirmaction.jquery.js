/*
 * jQuery Plugin : jConfirmAction
 * 
 * by Hidayat Sagita
 * http://www.webstuffshare.com
 * Licensed Under GPL version 2 license.
 *
 * Updated by Markus Döring to also work with submit buttons and forms in addition to simple links
 *
 */
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
					if($(this).next('.question').length <= 0)
						$(this).after('<div class="question">'+theOptions.question+'<br/> <span class="yes">'+theOptions.yesAnswer+'</span><span class="cancel">'+theOptions.cancelAnswer+'</span></div>');
					
					$(this).next('.question').animate({opacity: 1}, 300);
					
					$('.yes').bind('click', function(){
						if (thisHref!=null){
							window.location = thisHref;
						}else{
							submitBtn.attr("jconfirmed", true);
							submitBtn.click();
						}
					});
			
					$('.cancel').bind('click', function(){
						$(this).parents('.question').fadeOut(300, function() {
							$(this).remove();
						});
					});
				};
			});
			
		});
	}
	
})(jQuery);