<#macro validate resource>
    <form action='validateResource.do' method='post'>
        <input name="r" type="hidden" value="${resource.shortname}"/>

        <div class="btn-group" role="group">
            <@s.submit id="publishButton" cssClass="btn btn-sm btn-outline-gbif-primary" name="validateResource" key="button.validate"/>
        </div>

    </form>
</#macro>
