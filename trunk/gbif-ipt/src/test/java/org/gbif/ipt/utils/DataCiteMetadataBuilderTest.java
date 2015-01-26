package org.gbif.ipt.utils;

import org.gbif.api.model.common.DOI;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.service.InvalidMetadataException;
import org.gbif.ipt.model.Organisation;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.model.voc.IdentifierStatus;
import org.gbif.metadata.eml.Agent;
import org.gbif.metadata.eml.Eml;
import org.gbif.metadata.eml.UserId;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DataCiteMetadataBuilderTest {

  @Test
  public void testBuilder() throws InvalidMetadataException {
    Resource resource = new Resource();
    DOI doi = new DOI("10.5072/ipt12");
    resource.setDoi(doi);
    resource.setIdentifierStatus(IdentifierStatus.PUBLIC);

    Organisation organisation = new Organisation();
    organisation.setName("Natural History Museum");
    resource.setOrganisation(organisation);

    Eml eml = new Eml();
    Calendar cal = Calendar.getInstance();
    cal.set(2013, Calendar.JANUARY, 9);
    Date date = cal.getTime();
    eml.setDateStamp(date);

    resource.setEml(eml);

    eml.setTitle("Ants of New York State");
    eml.setMetadataLanguage("en");
    Agent creator1 = new Agent();
    creator1.setLastName("Smith");
    creator1.setFirstName("Jim");
    List<UserId> userIds = Lists.newArrayList();
    UserId userId1 = new UserId("http://orcid.org", "0000-0099-6824-9999");
    userIds.add(userId1);
    creator1.setUserIds(userIds);
    eml.addCreator(creator1);

    Agent creator2 = new Agent();
    creator2.setLastName("GBIF");
    List<UserId> userIds2 = Lists.newArrayList();
    creator2.setUserIds(userIds2);
    eml.addCreator(creator2);

    DataCiteMetadata dataCiteMetadata = DataCiteMetadataBuilder.createDataCiteMetadata(doi, resource);
    assertEquals("10.5072/ipt12", dataCiteMetadata.getIdentifier().getValue());
    assertEquals(DataCiteMetadataBuilder.DOI_IDENTIFIER_TYPE, dataCiteMetadata.getIdentifier().getIdentifierType());
    assertEquals("Ants of New York State", dataCiteMetadata.getTitles().getTitle().get(0).getValue());
    assertEquals("Natural History Museum", dataCiteMetadata.getPublisher());
    assertEquals("2013", dataCiteMetadata.getPublicationYear());
    assertEquals("en", dataCiteMetadata.getTitles().getTitle().get(0).getLang());
    assertNull(dataCiteMetadata.getTitles().getTitle().get(0).getTitleType());
    assertEquals("Jim Smith", dataCiteMetadata.getCreators().getCreator().get(0).getCreatorName());
    assertEquals("0000-0099-6824-9999",
      dataCiteMetadata.getCreators().getCreator().get(0).getNameIdentifier().getValue());
    assertEquals("http://orcid.org", dataCiteMetadata.getCreators().getCreator().get(0).getNameIdentifier().getSchemeURI());
    assertEquals(DataCiteMetadataBuilder.ORCID_NAME_IDENTIFIER_SCHEME, dataCiteMetadata.getCreators().getCreator().get(0).getNameIdentifier().getNameIdentifierScheme());
    assertEquals("GBIF", dataCiteMetadata.getCreators().getCreator().get(1).getCreatorName());
  }

  /**
   * Publisher name is an empty string.
   */
  @Test(expected=InvalidMetadataException.class)
  public void testGetPublisherWithEmptyName() throws InvalidMetadataException {
    Resource resource = new Resource();
    Organisation organisation = new Organisation();
    organisation.setName("");
    resource.setOrganisation(organisation);
    DataCiteMetadataBuilder.getPublisher(resource);
  }

  /**
   * Publisher does not exist.
   */
  @Test(expected=InvalidMetadataException.class)
  public void testGetPublisherNotExisting() throws InvalidMetadataException {
    Resource resource = new Resource();
    DataCiteMetadataBuilder.getPublisher(resource);
  }

  /**
   * Title is an empty string.
   */
  @Test(expected=InvalidMetadataException.class)
  public void testConvertEmlTitlesWithEmptyTitle() throws InvalidMetadataException {
    Eml eml = new Eml();
    eml.setTitle("");
    DataCiteMetadataBuilder.convertEmlTitles(eml);
  }


  /**
   * Scheme not recognized - only ORCID and ResearcherId are supported.
   */
  @Test
  public void testConvertEmlUserIdWithUnrecognizedScheme() throws InvalidMetadataException {
    UserId userId1 = new UserId("http://unrecognized.org", "0000-0099-6824-9999");
    DataCiteMetadata.Creators.Creator.NameIdentifier id = DataCiteMetadataBuilder.convertEmlUserId(userId1);
    assertNull(id);
  }

  /**
   * SchemeURI (UserId.directory) not provided.
   */
  @Test(expected=InvalidMetadataException.class)
  public void testConvertEmlUserIdWithMissingSchemeURI() throws InvalidMetadataException {
    UserId userId1 = new UserId("", "0000-0099-6824-9999");
    DataCiteMetadataBuilder.convertEmlUserId(userId1);
  }

  /**
   * First agent in list had an empty name.
   */
  @Test(expected=InvalidMetadataException.class)
  public void testConvertEmlCreatorsWithEmptyName() throws InvalidMetadataException {
    Agent creator1 = new Agent();
    creator1.setLastName("");
    creator1.setFirstName("");
    List<Agent> creators = Lists.newArrayList();
    creators.add(creator1);
    DataCiteMetadataBuilder.convertEmlCreators(creators);
  }

  /**
   * Agents list was empty.
   */
  @Test(expected=InvalidMetadataException.class)
  public void testConvertEmlCreatorsWithEmptyList() throws InvalidMetadataException {
    List<Agent> creators = Lists.newArrayList();
    DataCiteMetadataBuilder.convertEmlCreators(creators);
  }

}
