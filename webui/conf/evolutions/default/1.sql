/*
To create the initial PLAY_EVOLUTIONS database (has to be done manually with ij):
CREATE TABLE PLAY_EVOLUTIONS (
  ID INT NOT NULL,
  HASH VARCHAR(255) NOT NULL,
  APPLIED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  APPLY_SCRIPT VARCHAR(32000),
  REVERT_SCRIPT VARCHAR(32000),
  STATE VARCHAR(255) DEFAULT NULL,
  LAST_PROBLEM VARCHAR(32000),
  PRIMARY KEY (ID)
);
*/

# --- !Ups

CREATE TABLE "JOB" (
  "ID"                        VARCHAR(255) NOT NULL,
  "NICENAME"                  VARCHAR(255),
  "CREATED"                   TIMESTAMP,
  "STARTED"                   TIMESTAMP,
  "FINISHED"                  TIMESTAMP,
  "USER_ID"                   BIGINT,
  "GUEST_EMAIL"               VARCHAR(255),
  "NOTIFIED_CREATED"          BOOLEAN,
  "NOTIFIED_COMPLETE"         BOOLEAN,
  CONSTRAINT pk_job PRIMARY KEY ("ID"))
;

CREATE TABLE "SETTING" (
  "NAME"                      VARCHAR(255) NOT NULL,
  "VALUE"                     VARCHAR(255),
  CONSTRAINT pk_setting PRIMARY KEY ("NAME"))
;

CREATE TABLE "UPLOAD" (
  "ID"                        BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "ABSOLUTE_PATH"             VARCHAR(255),
  "CONTENT_TYPE"              VARCHAR(255),
  "UPLOADED"                  TIMESTAMP,
  "USER_ID"                   BIGINT,
  "JOB"                       VARCHAR(255),
  CONSTRAINT pk_upload PRIMARY KEY ("ID"))
;

CREATE TABLE "USERS" (
  "ID"                        BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "EMAIL"                     VARCHAR(255),
  "NAME"                      VARCHAR(255),
  "PASSWORD"                  VARCHAR(255),
  "ADMIN"                     BOOLEAN,
  "ACTIVE"                    BOOLEAN,
  "PASSWORD_LINK_SENT"        TIMESTAMP,
  CONSTRAINT pk_user PRIMARY KEY ("ID"))
;

# --- !Downs

DROP TABLE "JOB";

DROP TABLE "SETTING";

DROP TABLE "UPLOAD";

DROP TABLE "USER";

