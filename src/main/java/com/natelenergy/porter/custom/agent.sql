
DROP TABLE IF EXISTS fault_events;
CREATE TABLE fault_events (
  id int8 PRIMARY KEY,
  manager varchar NOT NULL,
  fault varchar NOT NULL,
  endpoint varchar NOT NULL,

  condition_hit_time timestamptz NOT NULL,
  faulted_time      timestamptz DEFAULT NULL,
  release_time      timestamptz DEFAULT NULL,
  ack_time          timestamptz DEFAULT NULL,

  value REAL
);
