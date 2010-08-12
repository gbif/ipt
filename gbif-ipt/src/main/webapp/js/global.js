var userPrefs;

function str(x) {
	if(x!=null && x.length > 0) return x;
	return '';
}
function initHelp(){
    $("img.infoImg").click(function(e) {
        var show = $(this).next().is(":hidden");
	    $("div.info:visible").hide("fast");
	    if (show){
//	    	var t = $(this).offset();
//	    	$(this).next().css({ top: off.top-24, left: off.left-170 });
	        $(this).next().show("fast");
	    };
    })
    $("div.info").click(function(e) {
        $(this).hide("fast");
    })
    $(document).keyup(function(e) {
    	// pressing the escape key
  		if (e.keyCode == 27) { 
	        $("div.info:visible").hide("fast");
  		}   
	});
    $("div.info ol a").click(function(e) {
    	e.preventDefault(); 
        $(this).parent().parent().parent().next().val($(this).attr("val"));
        $(this).parent().parent().parent().hide("fast");
    })
}
function initCollapsable(){
    $("div.collapsed ul").hide();
    $("div.collapsable").prepend('<div class="toggler"><span class="btn TGshow">show</span><span class="btn TGhide">hide</span></div>');
    $("span.TGshow").hide();
    $("div.collapsed span.TGhide").hide();
    $("div.collapsed span.TGshow").show();
    $(".TGshow").click(function(e) {
        $("ul",$(this).parent().parent()).show();
        $(this).hide();
        $(".TGhide",$(this).parent()).show();
    })
    $(".TGhide").click(function(e) {
        $("ul",$(this).parent().parent()).hide();
        $(this).hide();
        $(".TGshow",$(this).parent()).show();
    })
}
function isTrueOrNull(x){
	if (x==null || x==true){
		return true;
	}
	return false;
}

function readUserPrefCookie(){
	var cookieData = $.cookie("prefs");
	userPrefs = cookieData ? JSON.parse(cookieData) : {};
}
function setUserPrefCookie(){
	// expiration in days
    var options = { path: '/', expires: 1 }; 
	var cookieData = JSON.stringify(userPrefs);
	$.cookie("prefs", cookieData, options);
}

