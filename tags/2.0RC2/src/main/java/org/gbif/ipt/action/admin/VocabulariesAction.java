/**
 * 
 */
package org.gbif.ipt.action.admin;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Vocabulary;
import org.gbif.ipt.service.admin.VocabulariesManager;

import com.google.inject.Inject;

/**
 * The Action responsible for all user input relating to the vocabularies in use within the IPT
 * 
 * @author tim
 */
public class VocabulariesAction extends BaseAction {
  private static final long serialVersionUID = 7277675384287096912L;
  @Inject
  private VocabulariesManager vocabManager;
  private Vocabulary vocabulary;

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
