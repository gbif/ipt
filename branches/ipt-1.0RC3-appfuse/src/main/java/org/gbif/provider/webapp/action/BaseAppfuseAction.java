package org.gbif.provider.webapp.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.appfuse.Constants;
import org.appfuse.service.MailEngine;
import org.appfuse.service.RoleManager;
import org.appfuse.service.UserManager;
import org.gbif.provider.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;

import com.opensymphony.xwork2.ActionSupport;


/**
 * Implementation of <strong>ActionSupport</strong> that contains 
 * convenience methods for subclasses.  For example, getting the current
 * user and saving messages/errors. This class is intended to
 * be a base class for all Action classes.
 * 
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
public class BaseAppfuseAction extends ActionSupport {
    private static final long serialVersionUID = 3525445612504421307L;
    /**
     * Constant for cancel result String
     */
    public static final String CANCEL = "cancel";

    /**
     * Transient log to prevent session synchronization issues - children can use instance for logging.
     */
    protected transient final Log log = LogFactory.getLog(getClass());

    @Autowired
    protected UserManager userManager;
    @Autowired
    protected RoleManager roleManager;
    @Autowired
    protected MailEngine mailEngine;
    /**
     * A message pre-populated with default data
     */
    @Autowired
    protected SimpleMailMessage mailMessage;

    /**
     * Indicator if the user clicked cancel
     */
    protected String cancel;

    /**
     * Indicator for the page the user came from.
     */
    protected String from;

    /**
     * Set to "delete" when a "delete" request parameter is passed in
     */
    protected String delete;

    /**
     * Set to "save" when a "save" request parameter is passed in
     */
    protected String save;


    /**
     * Velocity template to use for e-mailing
     */
    protected String templateName;

    /**
     * Simple method that returns "cancel" result
     * @return "cancel"
     */
    public String cancel() {
        return CANCEL;
    }

    /**
     * Save the message in the session, appending if messages already exist
     * @param msg the message to put in the session
     */
    @SuppressWarnings("unchecked")
    protected void saveMessage(String msg) {
        List messages = (List) getRequest().getSession().getAttribute("messages");
        if (messages == null) {
            messages = new ArrayList();
        }
        messages.add(msg);
        getRequest().getSession().setAttribute("messages", messages);
    }

    /**
     * Convenience method to get the request
     * @return current request
     */
    protected HttpServletRequest getRequest() {
        return ServletActionContext.getRequest();
    }

    /**
     * Convenience method to get the response
     * @return current response
     */
    protected HttpServletResponse getResponse() {
        return ServletActionContext.getResponse();
    }

    /**
     * Convenience method to get the session. This will create a session if one doesn't exist.
     * @return the session from the request (request.getSession()).
     */
    protected HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * Convenience method to send e-mail to users
     * @param user the user to send to
     * @param msg the message to send
     * @param url the URL to the application (or where ever you'd like to send them)
     */
    protected void sendUserMessage(User user, String msg, String url) {
        if (log.isDebugEnabled()) {
            log.debug("sending e-mail to user [" + user.getEmail() + "]...");
        }

        mailMessage.setTo(user.getFullName() + "<" + user.getEmail() + ">");

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("user", user);
        // TODO: figure out how to get bundle specified in struts.xml
        // model.put("bundle", getTexts());
        model.put("message", msg);
        model.put("applicationURL", url);
        mailEngine.sendMessage(mailMessage, templateName, model);
    }

    public void setMailMessage(SimpleMailMessage mailMessage) {
        this.mailMessage = mailMessage;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Convenience method for setting a "from" parameter to indicate the previous page.
     * @param from indicator for the originating page
     */
    public void setFrom(String from) {
        this.from = from;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }

    public void setSave(String save) {
        this.save = save;
    }
}
