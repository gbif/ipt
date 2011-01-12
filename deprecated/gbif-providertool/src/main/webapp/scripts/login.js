<script type="text/javascript">
	$(document).ready(function(){
	    if (getCookie("username") != null) {
	        $("#j_username").val(getCookie("username"));
	        $("#j_password").focus();
	    } else {
	        $("#j_username").focus();
	    }
	});
    
    function saveUsername(theForm) {
        var expires = new Date();
        expires.setTime(expires.getTime() + 24 * 30 * 60 * 60 * 1000); // sets it for approx 30 days.
        setCookie("username",theForm.j_username.value,expires,"<c:url value="/"/>");
    }
    
    function validateForm(form) {                                                               
        return validateRequired(form); 
    } 
    
    function passwordHint() {
        if ($("#j_username").val().length == 0) {
            alert("<s:text name="errors.required"><s:param><s:text name="label.username"/></s:param></s:text>");
            $("#j_username").focus();
        } else {
            location.href="<c:url value="/passwordHint.html"/>?username=" + $("#j_username").val();     
        }
    }
    
    function required () { 
        this.aa = new Array("j_username", "<s:text name="errors.required"><s:param><s:text name="label.username"/></s:param></s:text>", new Function ("varName", " return this[varName];"));
        this.ab = new Array("j_password", "<s:text name="errors.required"><s:param><s:text name="label.password"/></s:param></s:text>", new Function ("varName", " return this[varName];"));
    } 
</script>