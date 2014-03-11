package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.admin.VocabulariesManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import com.google.inject.Inject;

/**
 * The Action responsible for all user input relating to the vocabularies in use within the IPT.
 */
public class VocabulariesAction extends BaseAction {

  private static final long serialVersionUID = 7277675384287096912L;

  private final VocabulariesManager vocabManager;
  private Vocabulary vocabulary;

  @Inject
  public VocabulariesAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager,
    VocabulariesManager vocabManager) {
    super(textProvider, cfg, registrationManager);
    this.vocabManager = vocabManager;
  }

  @Override
  public String execute() throws Exception {
    if (id != null) {
      vocabulary = vocabManager.get(id);
      if (vocabulary == null) {
        return NOT_FOUND;
      }
    }
    return SUCCESS;
  }

  public Vocabulary getVocabulary() {
    return vocabulary;
  }

}
