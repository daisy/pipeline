# --- !Ups

CREATE TABLE "USERSETTING" (
  "ID"                        BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  "USER_ID"                   BIGINT,
  "NAME"                      VARCHAR(255) NOT NULL,
  "VALUE"                     VARCHAR(255),
  CONSTRAINT pk_usersetting PRIMARY KEY ("ID"))
;

# --- !Downs

DROP TABLE "USERSETTING";
