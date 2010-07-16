/**
 * 
 */
package org.gbif.ipt.action.admin;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.action.FormAction;
import org.gbif.ipt.model.User;
import org.gbif.ipt.service.AlreadyExistingException;
import org.gbif.ipt.service.DeletionNotAllowedException;
import org.gbif.ipt.service.DeletionNotAllowedException.Reason;
import org.gbif.ipt.service.admin.UserAccountManager;

import com.google.inject.Inject;

/**
 * The Action responsible for all user input relating to the user accounts in the IPT
 * @author tim
 */
public class UserAccountsAction extends FormAction {
	private static final long serialVersionUID = 8892204508303815998L;
	@Inject private UserAccountManager userManager;
	
	private User user;
	private List<User> users;
	
	
	public String list(){
		users=userManager.list();
		return SUCCESS;
	}

	@Override
	public void prepare() throws Exception{
		super.prepare();
		if (id==null){
			// create new user
			user=new User();
		}else{
			// modify existing user
			user=userManager.get(id);
			if (user==null){
				// set notFound flag to true so FormAction will return a NOT_FOUND 404 result name
				notFound=true;
			}
		}
	}
	
	@Override
	public String save(){
		try {
			if (id==null){
				userManager.add(user);
				addActionMessage(getText("admin.user.added"));
			}else{
				addActionMessage(getText("admin.user.changed"));
			}
			userManager.save();
			return SUCCESS;
		} catch (IOException e) {
			log.error("The user change couldnt be saved: "+e.getMessage(), e);
			addActionError(getText("admin.user.saveError"));
			addActionError(e.getMessage());
			return INPUT;
		} catch (AlreadyExistingException e) {
			addActionError(getText("admin.user.exists", new String[]{id,}));
			return INPUT;
		}
	}

	@Override
	public String delete(){
		try {
			User removedUser = userManager.delete(id);
			if (removedUser==null){
				return NOT_FOUND;
			}
			userManager.save();
			addActionMessage(getText("admin.user.deleted"));
		} catch (DeletionNotAllowedException e) {
			if (Reason.LAST_ADMIN==e.getReason()){
				addActionError(getText("admin.user.deleted.lastadmin"));
			}else if (Reason.LAST_RESOURCE_MANAGER==e.getReason()){
				addActionError(getText("admin.user.deleted.lastmanager"));
			}			
		} catch (IOException e) {
			addActionError("cant save user file: "+e.getMessage());
		}
		return SUCCESS;
	}

	
	// Getters / Setters follow
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
}
