# --- !Ups

create table job (
  id                            bigint auto_increment not null,
  engine_id                     varchar(255),
  nicename                      varchar(255),
  created                       datetime(6),
  started                       datetime(6),
  finished                      datetime(6),
  user_id                       bigint,
  guest_email                   varchar(255),
  script_id                     varchar(255),
  script_name                   varchar(255),
  status                        varchar(255),
  notified_created              tinyint(1) default 0,
  notified_complete             tinyint(1) default 0,
  constraint pk_job primary key (id)
);

create table setting (
  name                          varchar(255) not null,
  value                         varchar(255),
  constraint pk_setting primary key (name)
);

create table users (
  id                            bigint auto_increment not null,
  email                         varchar(255),
  name                          varchar(255),
  password                      varchar(255),
  admin                         tinyint(1) default 0,
  active                        tinyint(1) default 0,
  password_link_sent            datetime(6),
  constraint pk_users primary key (id)
);

create table usersetting (
  id                            varchar(255) not null,
  user_id                       bigint,
  name                          varchar(255),
  value                         varchar(255),
  constraint pk_usersetting primary key (id)
);


# --- !Downs

drop table if exists job;

drop table if exists setting;

drop table if exists users;

drop table if exists usersetting;

