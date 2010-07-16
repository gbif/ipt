var userPrefs;

function str(x) {
	if(x!=null && x.length > 0) return x;
	return '';
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

