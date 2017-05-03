# --- Extend schema to support addtional functionality

# --- !Ups

alter table nominations add column nomination_message varchar(4096);

alter table question_templates add column help_text varchar(4096);
alter table question_response add column help_text varchar(4096);

# --- !Downs

alter table nominations drop column if exists nomination_message;

alter table question_templates drop column if exists help_text;
alter table question_response drop column if exists help_text;
