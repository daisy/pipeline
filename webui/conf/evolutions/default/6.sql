# --- !Ups

DROP TABLE "UPLOAD";
DROP TABLE "JOB";
CREATE TABLE "JOB" (
  "ID"                        BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "ENGINE_ID"                 VARCHAR(255),
  "NICENAME"                  VARCHAR(255),
  "STATUS"                    VARCHAR(255),
  "CREATED"                   TIMESTAMP,
  "STARTED"                   TIMESTAMP,
  "FINISHED"                  TIMESTAMP,
  "USER_ID"                   BIGINT,
  "GUEST_EMAIL"               VARCHAR(255),
  "NOTIFIED_CREATED"          BOOLEAN,
  "NOTIFIED_COMPLETE"         BOOLEAN,
  "LOCAL_DIR_NAME"            VARCHAR(255),
  "SCRIPT_ID"                 VARCHAR(255),
  "SCRIPT_NAME"               VARCHAR(255),
  CONSTRAINT pk_job PRIMARY KEY ("ID"))
;

# --- !Downs

DROP TABLE "JOB";
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
  "LOCAL_DIR_NAME"            VARCHAR(255),
  "SCRIPT_ID"                 VARCHAR(255),
  "SCRIPT_NAME"               VARCHAR(255),
  CONSTRAINT pk_job PRIMARY KEY ("ID"))
;
CREATE TABLE "UPLOAD" (
  "ID"                        BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "ABSOLUTE_PATH"             VARCHAR(255),
  "CONTENT_TYPE"              VARCHAR(255),
  "UPLOADED"                  TIMESTAMP,
  "USER_ID"                   BIGINT,
  "JOB"                       VARCHAR(255),
  "BROWSER_ID"                BIGINT;
  CONSTRAINT pk_upload PRIMARY KEY ("ID"))
;
