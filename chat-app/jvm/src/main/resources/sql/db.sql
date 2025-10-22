create table player(id text primary key, name text, email text, xp integer, game_type text);

insert into player (id, name, email, xp, game_type) values
  ('1', 'Alice Smith', 'alice@example.com', 1200, 'adventure'),
  ('2', 'Bob Jones', 'bob@example.com', 800, 'strategy'),
  ('3', 'Charlie Lee', 'charlie@example.com', 1500, 'adventure'),
  ('4', 'Dana White', 'dana@example.com', 950, 'puzzle'),
  ('5', 'Eve Black', 'eve@example.com', 1100, 'strategy');

create table game(id text primary key, name text, game_type text);

insert into game (id, name, game_type) values
  ('g1', 'Lost Island', 'adventure'),
  ('g2', 'Chess Master', 'strategy'),
  ('g3', 'Puzzle Quest', 'puzzle'),
  ('g4', 'Space Adventure', 'adventure'),
  ('g5', 'Empire Builder', 'strategy');

create table message(id text primary key, sender text, content text, parentid text, timestamp bigint);
