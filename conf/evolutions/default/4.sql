# --- Extend schema to support ad-hoc feedback functionality

# --- !Ups

alter table person add column is_admin boolean default false;

# --- !Downs

alter table person drop column if exists is_admin;

