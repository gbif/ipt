## Developer Guidelines ##

Developers new to the IPT codebase should follow the guidelines and best-practices outlines below:

### Git ###
Please don't check in unused files.

Code in the trunk should always compile and allow the startup of jetty via maven!

### Guice ###
IptModule contains wiring, but interfaces are annotated directly with @ImplementedBy(ConfigManagerImpl.class)
 
### Configuration ###
DataDir inside WEB-INF of a running IPT a single file datadir.location is created that points to the currently used datadir where all configuration and data is stored. If this file is deleted the setup interceptor will ask the user to assign a new (potentially existing) datadir.

The hidden file ".gbifreg" indicates whether the datadir is linked to the production or test registry. This cannot be changed again from the UI!

AppConfig.java contains all IPT wide configuration, in particular the baseURL for the application.
 
### Struts2 ###
BaseAction.java supplies the AppConfig, implements session aware and gives access to current user, implements a simpler TextProvider which is faster than the native struts2 one.

SetupAndCancelInterceptor.java checks if the datadir is configured and an admin user exists - otherwise redirects to the respective setup page.
 
For each package (root, portal, manage, admin) its own interceptor stack exists.

The "input" resultname is used to show the form. We can therefore use the standard validation interceptor which uses the input result when data doesn't validate.

The "success" resultname is called when the form submit succeeded. In many cases this should simply be a redirect to another, often the corresponding list, action.

The action implements preparable, request aware and the execute method.
* execute: FormAction determines if a POST or GET is used and calls save (POST), delete (POST + delete=true) or
   nothing (GET).
If any other action values need to be set you can override prepare() - but remember to call super.execute().
* prepare: the "id" parameter of the request object is stored in the action.

POSTAction.java simplifies working with forms. For modifying instance data always use POST, never GET.
Most full actions for modifying entities should override the following methods:    

* prepare(): load existing values based on "id" parameter and request object.
* save(): persist data AFTER the params interceptor did its job.
* delete(): this method is called when a POST with a "delete=anything_but_null" parameter is received.
If the "id" given does not exist you can set the "notFound" property to true in any of the above methods. The action will then return a 404 result name.

To do validation, implement the validate() method of an action (instead of using xml validation definitions). See SetupAction.java as an example. Validation requires an "input" result name that shows the form when the form was not valid. Using the simple theme we also need to declare where to render the validation feedback: http://struts.apache.org/docs/fielderror.html
  
### HTML Design ###
Links always use ${baseURL}/my/ipt/link.do, so please refrain from using struts or jsp url tags!
   
Forms use the forms.ftl macros instead of struts2 tags, such as the i18n @select drop downs.

Buttons use proper buttons or input@type=submit (forms) for add,delete,edit,create actions.

General best practice guides:
* http://htmldog.com/articles/formlayout/
* https://www.sherpaglobal.com/top-10-html-form-layout-best-practices/

### Javascript ###
Only use jQuery (https://jquery.com/) for custom code.

Use jconfirmation plugin (https://github.com/hdytsgt/jConfirmAction) when asking for confirmation, e.g. deletes.

Use jQuery dataTables (https://www.datatables.net/) plugin to enhance HTML tables with pagination, searching, etc.

### CSS ###
960 grid system (http://960.gs/) is used for page layouts.

Keep number of CSS classes to a minimum and consider using page specific CSS in <head><style> on that page.

### Managers ###
2 tier architecture only with interfaces + implementation.

### i18n ###
Templates, actions and also important service messages should be localised using a single ResourceBundle.

Translated vocabularies can be used to populate select drop downs easily by calling getI18nVocab(...).