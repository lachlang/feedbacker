# --- Extend schema to support additional functionality

# --- !Ups

alter table nominations add column nomination_message varchar(4096);

alter table question_templates add column help_text varchar(4096);
alter table question_response add column help_text varchar(4096);

update question_templates SET help_text = 'This text will help answer the question.' WHERE id = 1;
update question_templates SET help_text = 'Here is some context.  There is quite a lot of it and it is interesting to see how it displays given that there are so many words.  Honestly it just goes on and on and on and on and on and on and on and on and on and on and on and on and on and on and on without any sign that it will slow down.  One day, a change will come and we will reach the end of this description.  One day.' WHERE id = 3;
update question_templates SET help_text = 'The answer is ''c''.  It is <strong>always</strong> ''c''.' WHERE id = 7;
update question_templates SET help_text = 'We can add<em>emphasis</em> to certain words in the help text.' WHERE id = 11;
update question_templates SET help_text = 'Context is good for everybody :)' WHERE id = 12;

# --- !Downs

alter table nominations drop column if exists nomination_message;

alter table question_templates drop column if exists help_text;
alter table question_response drop column if exists help_text;
