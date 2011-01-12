/*************************************************************************
	(c) 2008-2009 Martin Wendt
 *************************************************************************/

$(function(){
	// Log to Google Analytics, when not running locally
	if ( document.urchinTracker && document.URL.toLowerCase().indexOf('wwwendt.de/')>=0 ) {
		_uacct = "UA-316028-1";
		urchinTracker();
	}

	// Show some elements only, if (not) inside the Example Browser
	if (top.location == self.location) 
		$(".hideOutsideFS").hide();
	else
		$(".hideInsideFS").hide();
});
