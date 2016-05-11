# Feedbacks Schema`

# --- !Ups

create table person (
  id                        serial,
  name 		    	        varchar(255) not null,
  role     		            varchar(255) not null,
  email                     varchar(255) not null,
  pass_hash                 varchar(255) not null,
  user_status				varchar(20) not null,
  manager_email				varchar(255) not null,
  constraint ch_person_status check (user_status in ('Active', 'Inactive', 'Restricted')),
  constraint uq_person_email unique (email),
  constraint pk_person primary key (id)
);

create unique index idx_person_email on person (email);

create table activations (
	token					varchar(255) not null,
	email 					varchar(255) not null,
	expires					timestamp not null,
	used					boolean not null,
	created					timestamp not null,
	constraint pk_activations primary key (token)
);

create index idx_activation_email on activations (email);

create table nominations (
	id 						serial,
	from_id					bigint not null,
	to_email				varchar(255) not null,
	status					varchar(20) not null,
	lastUpdated				timestamp,
	cycle_id 				bigint not null,
	shared 					boolean,
	constraint ck_nominations_status check (status in ('Pending','Submitted','Closed')),
	constraint pk_nominations primary key (id)
);

create index idx_nominations_from on nominations (from_id);
create index idx_nominations_to on nominations (to_email);

create table cycle (
	id						serial,
	label					varchar(50) not null,
	start_date 				date not null,
	end_date 				date not null,
	active 					boolean not null default false,
	constraint pk_cycle primary key (id)
);

create index cycle_label_idx on cycle (label);

create table question_templates (
	id 						serial,
	text					varchar(4096) not null,
	response_options		varchar(1024) not null,
	cycle_id				bigint not null,
	constraint pk_question_templates primary key (id)
);

create table question_response (
	id 						serial,
	nomination_id			bigint not null,
	text					varchar(4096) not null,
	response_options		varchar(1024) not null,
	response 				varchar(255),
	comments 				varchar(8192),
	constraint pk_question_response primary key (id)
);

alter table nominations add constraint fk_nominations_from foreign key (from_id) references person (id);
alter table nominations add constraint fk_nominations_to foreign key (to_email) references person (email);
alter table nominations add constraint fk_nominations_cycle foreign key (cycle_id) references cycle (id);
alter table question_response add constraint fk_question_response_nominations foreign key (nomination_id) references nominations (id);
alter table question_templates add constraint fk_question_template_cycle foreign key (cycle_id) references cycle (id);

# --- !Downs

drop table if exists person cascade;
drop table if exists activations cascade;
drop table if exists nominations cascade;
drop table if exists cycle cascade;
drop table if exists question_templates cascade;