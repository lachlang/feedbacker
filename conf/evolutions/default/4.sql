# --- Extend schema to support ad-hoc feedback functionality

# --- !Ups

alter table person add column is_admin boolean default false;

create table ad_hoc_feedback (
  id 						      bigserial,
  from_email		      varchar(255) not null,
  from_name           varchar(255) not null,
  from_role           varchar(255) not null,
  to_email			      varchar(255) not null,
  to_name 			      varchar(255) not null,
  to_role 			      varchar(255) not null,
  message             varchar(8192) not null,
  created             timestamp not null,
  recipient_visible   boolean not null default FALSE,
  constraint pk_ad_hoc_feedback primary key (id)
);

alter table ad_hoc_feedback add constraint fk_feedback_from foreign key (from_email) references person (email);
alter table ad_hoc_feedback add constraint fk_feedback_to foreign key (to_email) references person (email);

# --- !Downs

alter table person drop column if exists is_admin;

drop table if exists ad_hoc_feedback cascade;
