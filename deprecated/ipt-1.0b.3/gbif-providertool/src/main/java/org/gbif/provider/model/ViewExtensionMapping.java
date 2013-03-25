package org.gbif.provider.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;


@Entity
@DiscriminatorValue("EXT")
public class ViewExtensionMapping extends ViewMappingBase {
		
}
