# --- Extend schema to support additional functionality

# --- !Ups

alter table cycle add column three_sixty_review boolean not null default false;
alter table cycle add column optional_sharing boolean not null default false;
alter table cycle add column forced_sharing boolean not null default false;
alter table cycle add column help_link_url VARCHAR(100);
alter table cycle add column help_link_text VARCHAR(100);

alter table person drop constraint if exists ch_person_status;
update person set user_status = 'Disabled' where user_status = 'Restricted';
alter table person add constraint ch_person_status check (user_status in ('Active', 'Nominated', 'Inactive', 'Disabled'));

GRANT DELETE ON TABLE question_templates TO "feedback-service";

# --- !Downs

alter table cycle drop column if exists three_sixty_review;
alter table cycle drop column if exists optional_sharing;
alter table cycle drop column if exists forced_sharing;
alter table cycle drop column if exists help_link_url;
alter table cycle drop column if exists help_link_text;

alter table person drop constraint if exists ch_person_status;
update person set user_status = 'Restricted' where user_status = 'Disabled';
alter table person add constraint ch_person_status check (user_status in ('Active', 'Nominated', 'Inactive', 'Restricted'));

