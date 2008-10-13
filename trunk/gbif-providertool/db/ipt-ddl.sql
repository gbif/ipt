
    drop table if exists app_user;

    drop table if exists dwcore;

    drop table if exists dwcore_ext;

    drop table if exists extension;

    drop table if exists extension_property;

    drop table if exists extension_property_terms;

    drop table if exists log_event;

    drop table if exists occ_stat_by_region_and_taxon;

    drop table if exists property_mapping;

    drop table if exists provider_cfg;

    drop table if exists region;

    drop table if exists resource;

    drop table if exists resource_keywords;

    drop table if exists role;

    drop table if exists taxon;

    drop table if exists upload_event;

    drop table if exists user_role;

    drop table if exists view_mapping;

    create table app_user (
        id bigint not null auto_increment,
        account_expired bit not null,
        account_locked bit not null,
        address_address varchar(150),
        address_city varchar(50) not null,
        address_country varchar(100),
        postal_code varchar(15) not null,
        address_province varchar(100),
        credentials_expired bit not null,
        email varchar(255) not null unique,
        account_enabled bit,
        first_name varchar(50) not null,
        last_name varchar(50) not null,
        password varchar(255) not null,
        password_hint varchar(255),
        phone_number varchar(255),
        username varchar(50) not null unique,
        version integer,
        website varchar(255),
        primary key (id)
    ) type=MyISAM;

    create table dwcore (
        id bigint not null auto_increment,
        attributes text,
        basis_of_record varchar(64),
        catalog_number varchar(128),
        collecting_method text,
        collection_code varchar(128),
        collector varchar(128),
        date_collected datetime,
        day_of_year varchar(32),
        deleted bit not null,
        earliest_date_collected varchar(64),
        guid varchar(255) not null,
        identification_qualifer varchar(64),
        image_url varchar(255),
        information_withheld text,
        institution_code varchar(128),
        latest_date_collected varchar(64),
        life_stage varchar(128),
        link varchar(255),
        local_id varchar(128) not null,
        lat float,
        lon float,
        maximum_depth_in_meters_as_integer integer,
        maximum_elevation_in_meters_as_integer integer,
        minimum_depth_in_meters_as_integer integer,
        minimum_elevation_in_meters_as_integer integer,
        modified datetime,
        problematic bit not null,
        related_information text,
        remarks text,
        scientific_name varchar(255),
        sex varchar(64),
        valid_distribution_flag varchar(16),
        region_fk bigint,
        resource_fk bigint not null,
        taxon_fk bigint,
        primary key (id),
        unique (local_id, resource_fk)
    ) type=MyISAM;

    create table dwcore_ext (
        id bigint not null,
        author_year_of_scientific_name varchar(128),
        classs varchar(64),
        continent varchar(64),
        country varchar(128),
        county varchar(255),
        family varchar(128),
        genus varchar(64),
        higher_geography text,
        higher_taxon text,
        infraspecific_epithet varchar(128),
        infraspecific_rank varchar(128),
        island varchar(255),
        island_group varchar(255),
        kingdom varchar(64),
        locality text,
        maximum_depth_in_meters varchar(32),
        maximum_elevation_in_meters varchar(32),
        minimum_depth_in_meters varchar(64),
        minimum_elevation_in_meters varchar(64),
        nomenclatural_code varchar(64),
        orderrr varchar(128),
        phylum varchar(64),
        specific_epithet varchar(128),
        state_province varchar(128),
        water_body varchar(255),
        resource_fk bigint,
        primary key (id)
    ) type=MyISAM;

    create table extension (
        id bigint not null auto_increment,
        installed bit not null,
        link varchar(255),
        name varchar(128) unique,
        type integer,
        primary key (id)
    ) type=MyISAM;

    create table extension_property (
        id bigint not null auto_increment,
        column_length integer not null,
        link varchar(255),
        name varchar(64),
        namespace varchar(128),
        qual_name varchar(255),
        required bit not null,
        extension_fk bigint not null,
        property_order integer not null,
        primary key (id)
    ) type=MyISAM;

    create table extension_property_terms (
        extension_property_fk bigint not null,
        terms_element varchar(255),
        terms_order integer not null,
        primary key (extension_property_fk, terms_order)
    ) type=MyISAM;

    create table log_event (
        id bigint not null auto_increment,
        group_id integer not null,
        info_as_json text,
        instance_id varchar(255),
        level integer not null,
        message varchar(255),
        message_params_as_json text,
        source_id integer not null,
        source_type integer not null,
        timestamp datetime,
        user_fk bigint,
        primary key (id)
    ) type=MyISAM;

    create table occ_stat_by_region_and_taxon (
        id bigint not null auto_increment,
        num_occ integer not null,
        region_fk bigint,
        resource_fk bigint not null,
        taxon_fk bigint,
        primary key (id)
    ) type=MyISAM;

    create table property_mapping (
        id bigint not null auto_increment,
        column_name varchar(255),
        value varchar(255),
        view_mapping_fk bigint not null,
        property_fk bigint,
        primary key (id)
    ) type=MyISAM;

    create table provider_cfg (
        id bigint not null auto_increment,
        base_url varchar(128),
        data_dir varchar(128),
        description_image varchar(255),
        geoserver_url varchar(128),
        google_maps_api_key varchar(128),
        meta_contact_email varchar(64),
        meta_contact_name varchar(128),
        meta_description text,
        meta_link varchar(255),
        meta_location_latitude float,
        meta_location_longitude float,
        meta_title varchar(128),
        primary key (id)
    ) type=MyISAM;

    create table region (
        id bigint not null auto_increment,
        bbox_max_latitude float,
        bbox_max_longitude float,
        bbox_min_latitude float,
        bbox_min_longitude float,
        label varchar(255),
        lft bigint,
        occ_total integer not null,
        rgt bigint,
        type integer,
        parent_fk bigint,
        resource_fk bigint not null,
        primary key (id)
    ) type=MyISAM;

    create table resource (
        dtype varchar(31) not null,
        id bigint not null auto_increment,
        created datetime,
        geo_coverage_max_latitude float,
        geo_coverage_max_longitude float,
        geo_coverage_min_latitude float,
        geo_coverage_min_longitude float,
        guid varchar(128),
        meta_contact_email varchar(64),
        meta_contact_name varchar(128),
        meta_description text,
        meta_link varchar(255),
        meta_location_latitude float,
        meta_location_longitude float,
        meta_title varchar(128),
        modified datetime,
        jdbc_driver_class varchar(64),
        jdbc_password varchar(64),
        jdbc_url varchar(128),
        jdbc_user varchar(64),
        bbox_max_latitude float,
        bbox_max_longitude float,
        bbox_min_latitude float,
        bbox_min_longitude float,
        num_classes integer,
        num_countries integer,
        num_families integer,
        num_genera integer,
        num_kingdoms integer,
        num_orders integer,
        num_phyla integer,
        num_regions integer,
        num_taxa integer,
        num_terminal_regions integer,
        num_terminal_taxa integer,
        rec_with_altitude integer,
        rec_with_coordinates integer,
        rec_with_country integer,
        rec_with_date integer,
        modifier_fk bigint,
        last_upload_event_fk bigint,
        creator_fk bigint,
        primary key (id)
    ) type=MyISAM;

    create table resource_keywords (
        resource_fk bigint not null,
        keywords_element varchar(255)
    ) type=MyISAM;

    create table role (
        id bigint not null auto_increment,
        description varchar(64),
        name varchar(20),
        primary key (id)
    ) type=MyISAM;

    create table taxon (
        id bigint not null auto_increment,
        authorship varchar(128),
        bbox_max_latitude float,
        bbox_max_longitude float,
        bbox_min_latitude float,
        bbox_min_longitude float,
        code varchar(255),
        dwc_rank integer,
        fullname varchar(255),
        lft bigint,
        name varchar(128),
        occ_total integer not null,
        rank varchar(128),
        rgt bigint,
        parent_fk bigint,
        resource_fk bigint not null,
        primary key (id)
    ) type=MyISAM;

    create table upload_event (
        id bigint not null auto_increment,
        execution_date datetime,
        job_source_id integer not null,
        job_source_type integer not null,
        records_added integer not null,
        records_changed integer not null,
        records_deleted integer not null,
        records_erroneous integer not null,
        records_uploaded integer not null,
        resource_fk bigint not null,
        primary key (id)
    ) type=MyISAM;

    create table user_role (
        user_id bigint not null,
        role_id bigint not null,
        primary key (user_id, role_id)
    ) type=MyISAM;

    create table view_mapping (
        mapping_type varchar(31) not null,
        id bigint not null auto_increment,
        localid_col varchar(64),
        rec_total integer not null,
        source_file varchar(255),
        source_sql text,
        guid_col varchar(64),
        link_col varchar(64),
        extension_fk bigint,
        resource_fk bigint not null,
        primary key (id)
    ) type=MyISAM;

    create index deleted on dwcore (deleted);

    create index longitude on dwcore (lon);

    create index inst_code on dwcore (institution_code);

    create index coll_code on dwcore (collection_code);

    create index sci_name on dwcore (scientific_name);

    create index cat_num on dwcore (catalog_number);

    create index record_basis on dwcore (basis_of_record);

    create index date_collected on dwcore (date_collected);

    create index guid on dwcore (guid);

    create index latitude on dwcore (lat);

    create index source_local_id on dwcore (local_id);

    alter table dwcore 
        add index FKB1603AB26A2EF90B (resource_fk), 
        add constraint FKB1603AB26A2EF90B 
        foreign key (resource_fk) 
        references resource (id);

    alter table dwcore 
        add index FKB1603AB286115B5A (region_fk), 
        add constraint FKB1603AB286115B5A 
        foreign key (region_fk) 
        references region (id);

    alter table dwcore 
        add index FKB1603AB2F4A31FEE (taxon_fk), 
        add constraint FKB1603AB2F4A31FEE 
        foreign key (taxon_fk) 
        references taxon (id);

    create index genus on dwcore_ext (genus);

    alter table dwcore_ext 
        add index FK3CAC4B146A2EF90B (resource_fk), 
        add constraint FK3CAC4B146A2EF90B 
        foreign key (resource_fk) 
        references resource (id);

    alter table dwcore_ext 
        add index FK3CAC4B14258F7CF7 (id), 
        add constraint FK3CAC4B14258F7CF7 
        foreign key (id) 
        references dwcore (id);

    alter table extension_property 
        add index FKB1C849D5E6B2BACE (extension_fk), 
        add constraint FKB1C849D5E6B2BACE 
        foreign key (extension_fk) 
        references extension (id);

    alter table extension_property_terms 
        add index FK8EEB829DB9EEC66D (extension_property_fk), 
        add constraint FK8EEB829DB9EEC66D 
        foreign key (extension_property_fk) 
        references extension_property (id);

    alter table log_event 
        add index FK8805F5DFF503D0FF (user_fk), 
        add constraint FK8805F5DFF503D0FF 
        foreign key (user_fk) 
        references app_user (id);

    alter table occ_stat_by_region_and_taxon 
        add index FK18B7A4842E6479A (resource_fk), 
        add constraint FK18B7A4842E6479A 
        foreign key (resource_fk) 
        references resource (id);

    alter table occ_stat_by_region_and_taxon 
        add index FK18B7A48486115B5A (region_fk), 
        add constraint FK18B7A48486115B5A 
        foreign key (region_fk) 
        references region (id);

    alter table occ_stat_by_region_and_taxon 
        add index FK18B7A484F4A31FEE (taxon_fk), 
        add constraint FK18B7A484F4A31FEE 
        foreign key (taxon_fk) 
        references taxon (id);

    alter table property_mapping 
        add index FKD70CF8645010B26D (property_fk), 
        add constraint FKD70CF8645010B26D 
        foreign key (property_fk) 
        references extension_property (id);

    alter table property_mapping 
        add index FKD70CF864A02FD6D4 (view_mapping_fk), 
        add constraint FKD70CF864A02FD6D4 
        foreign key (view_mapping_fk) 
        references view_mapping (id);

    create index title on provider_cfg (meta_title);

    create index reg_lft on region (lft);

    create index reg_rgt on region (rgt);

    create index region_name on region (label);

    alter table region 
        add index FKC84826F4FD0995E4 (parent_fk), 
        add constraint FKC84826F4FD0995E4 
        foreign key (parent_fk) 
        references region (id);

    alter table region 
        add index FKC84826F42E6479A (resource_fk), 
        add constraint FKC84826F42E6479A 
        foreign key (resource_fk) 
        references resource (id);

    create index title on resource (meta_title);

    alter table resource 
        add index FKEBABC40E4FFFD4FE (creator_fk), 
        add constraint FKEBABC40E4FFFD4FE 
        foreign key (creator_fk) 
        references app_user (id);

    alter table resource 
        add index FKEBABC40E497AF9C2 (last_upload_event_fk), 
        add constraint FKEBABC40E497AF9C2 
        foreign key (last_upload_event_fk) 
        references upload_event (id);

    alter table resource 
        add index FKEBABC40EA1C4CE73 (modifier_fk), 
        add constraint FKEBABC40EA1C4CE73 
        foreign key (modifier_fk) 
        references app_user (id);

    alter table resource_keywords 
        add index FKBFE8BCBB2E6479A (resource_fk), 
        add constraint FKBFE8BCBB2E6479A 
        foreign key (resource_fk) 
        references resource (id);

    create index tax_lft on taxon (lft);

    create index tax_rgt on taxon (rgt);

    create index taxon_fullname on taxon (fullname);

    alter table taxon 
        add index FK6908ECA7FAFDA0E (parent_fk), 
        add constraint FK6908ECA7FAFDA0E 
        foreign key (parent_fk) 
        references taxon (id);

    alter table taxon 
        add index FK6908ECA2E6479A (resource_fk), 
        add constraint FK6908ECA2E6479A 
        foreign key (resource_fk) 
        references resource (id);

    alter table upload_event 
        add index FKAF0A9EDC2E6479A (resource_fk), 
        add constraint FKAF0A9EDC2E6479A 
        foreign key (resource_fk) 
        references resource (id);

    alter table user_role 
        add index FK143BF46AF503D155 (user_id), 
        add constraint FK143BF46AF503D155 
        foreign key (user_id) 
        references app_user (id);

    alter table user_role 
        add index FK143BF46A4FD90D75 (role_id), 
        add constraint FK143BF46A4FD90D75 
        foreign key (role_id) 
        references role (id);

    alter table view_mapping 
        add index FKA8F48534D005777C (resource_fk), 
        add constraint FKA8F48534D005777C 
        foreign key (resource_fk) 
        references resource (id);

    alter table view_mapping 
        add index FKA8F48534E6B2BACE (extension_fk), 
        add constraint FKA8F48534E6B2BACE 
        foreign key (extension_fk) 
        references extension (id);



