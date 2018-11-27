--liquibase formatted sql

--changeset ryan:1
DROP TABLE IF EXISTS fault_events;
CREATE TABLE fault_events (
  id int8 PRIMARY KEY,
  root int8 DEFAULT 0,
  is_root BOOL DEFAULT FALSE,
  manager varchar NOT NULL,
  fault varchar NOT NULL,
  endpoint varchar NOT NULL,

  condition_hit_time timestamptz NOT NULL,
  faulted_time      timestamptz DEFAULT NULL,
  release_time      timestamptz DEFAULT NULL,
  ack_time          timestamptz DEFAULT NULL,

  value REAL
);

create index idx_fault_root               on fault_events (root);
create index idx_fault_manager            on fault_events (manager);
create index idx_fault_fault              on fault_events (fault);
create index idx_fault_endpoint           on fault_events (endpoint);
create index idx_fault_condition_hit_time on fault_events (condition_hit_time);
create index idx_fault_faulted_time       on fault_events (faulted_time);