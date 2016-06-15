# --- Sample Dataset

# --- !Ups

insert into cycle (id, label, start_date, end_date, active) values (1, 'Round 1 - 2016', '2016-06-30 00:00:00', '2016-07-30 00:00:00', true);
insert into cycle (id, label, start_date, end_date, active) values (2, 'Round 2 - 2016', '2016-09-01 00:00:00', '2016-09-30 00:00:00', false);

insert into question_templates (id, render_format, text, response_options, cycle_id) values ( 1, 'RADIO','How awesome is feedbacker?', 'Unbelievable,Ok,Pass Conceded,Requires Development', 1);
insert into question_templates (id, render_format, text, response_options, cycle_id) values ( 2, 'RADIO','This is the second question.    It is really long and contains a lot of text.  Indeed it just cant quite seem to get the point and end up somewhere sensible.  If only it would ask something that was worth asking and end our suspense.  It isnt that is hasnt been a thrilling ride, but it would be *fantastic* if it would hit the triumphant conclusion.', '1,2,3,4,5', 1);
insert into question_templates (id, render_format, text, response_options, cycle_id) values ( 3, 'SELECT','This is the third question.', '1,2,3,4,5', 1);
insert into question_templates (id, render_format, text, response_options, cycle_id) values ( 4, 'RADIO','Here is a yes/no question.', 'Yes,No', 1);
insert into question_templates (id, render_format, text, response_options, cycle_id) values ( 5, 'RADIO','Here is a true/false question.', 'True,False', 1);
insert into question_templates (id, render_format, text, response_options, cycle_id) values ( 6, 'RADIO','Here is a question with a rating out of three.', '1,2,3', 1);
insert into question_templates (id, render_format, text, response_options, cycle_id) values ( 7, 'SELECT','Here is a question with a rating out of seven.', '1,2,3,4,5,6,7', 1);
insert into question_templates (id, render_format, text, response_options, cycle_id) values ( 8, 'RADIO','Here is the last question.', '1,2,3,4,5', 1);
insert into question_templates (id, render_format, text, response_options, cycle_id) values ( 9, 'RADIO','Here is the first question.', '1,2,3,4,5', 2);
insert into question_templates (id, render_format, text, response_options, cycle_id) values (10, 'RADIO','Here is the second question.', '1,2,3,4,5', 2);
insert into question_templates (id, render_format, text, response_options, cycle_id) values (11, 'SELECT','Here is the third question.', '1,2,3,4,5', 2);
insert into question_templates (id, render_format, text, response_options, cycle_id) values (12, 'SELECT','Here is the fourth question.', '1,2,3,4,5', 2);
insert into question_templates (id, render_format, text, response_options, cycle_id) values (13, 'SELECT','Here is the fifth question.', '1,2,3,4,5', 2);

# --- !Downs

delete from question_templates;
delete from cycle;
