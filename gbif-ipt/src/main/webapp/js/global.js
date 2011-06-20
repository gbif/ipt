var timeout    = 500;
var closetimer = 0;
var ddmenuitem = 0;
$(document).ready(function(){
	$("#showMore").live("click", function(e) {
		e.preventDefault();
		var $parentTarget = $(e.target).parent();
		var showLess = $parentTarget.html();
		$parentTarget.html($parentTarget.next("#hiddenContent").html());	
		$parentTarget.next("#hiddenContent").html(showLess);	
	});
	$("#showLess").live("click", function(e) {
		e.preventDefault();
		var $parentTarget = $(e.target).parent();
		var showMore = $parentTarget.html();
		$parentTarget.html($parentTarget.next("#hiddenContent").html());
		$parentTarget.next("#hiddenContent").html(showMore);		
	});	
});

	
function str(x) {
	if(x!=null && x.length > 0) return x;
	return '';
}
function isTrueOrNull(x){
	if (x==null || x==true){
		return true;
	}
	return false;
}

function initForm(){
    $("input.form-reset").one("click", function () {
      $(this).val("");
    });  
}
/**
 * This method initialises the "help utility" of the forms
 * @param {String} context - is used to add events only to a specific context. 
 * 	-remember to add the (#) symbol in the parameter-. (this parameter can be omitted).
 */
function initHelp(context){
	if(context == undefined) context = "";
	
	$(context+" .calendarInfo").each(function(i) {
		if (context != "") {
			$(this).find("input").removeAttr("class");
			$(this).find(".ui-datepicker-trigger").remove();
		}
		$(this).find("input").datepicker({
			showOn: "button",
			buttonImage: "../images/calendar.png",
			buttonImageOnly: true,
			changeMonth: true,
			changeYear: true,
			constrainInput: false,
			dateFormat: 'yy-mm-dd'
		});		
	});
	$(context+" .infoImg").click(function(e) {
        var show = $(this).next().is(":hidden");
	    $("div.info:visible").hide("fast");
	    if (show){
	        $(this).next().show("fast");
	    };
    });
	$("div.info:visible").hide();
	$(context+" div.info").click(function(e) {
        $(this).hide("fast");
    });
    $(document).keyup(function(e) {
    	// pressing the escape key
  		if (e.keyCode == 27) { 
	        $("div.info:visible").hide("fast");
  		}   
	});
    $(context+" div.info ol a").click(function(e) {
    	e.preventDefault(); 
        $(this).parent().parent().parent().next().val($(this).attr("val"));
        $(this).parent().parent().parent().hide("fast");
    });    
}
function jsddm_open() {  
	jsddm_canceltimer();
	jsddm_close();
	ddmenuitem = $(this).find('ul').css('visibility', 'visible');
}
function jsddm_close() { 
	if(ddmenuitem) ddmenuitem.css('visibility', 'hidden');
}
function jsddm_timer() {
	closetimer = window.setTimeout(jsddm_close, timeout);
}
function jsddm_canceltimer() {  
	if(closetimer) {
		window.clearTimeout(closetimer);
      closetimer = null;
    }
}
function initMenu() {  
	// Simple Drop-Down Menu
	// taken from http://javascript-array.com/scripts/jquery_simple_drop_down_menu/
	$('#topmenu > li').bind('mouseover', jsddm_open);
	$('#topmenu > li').bind('mouseout',  jsddm_timer);	
}
