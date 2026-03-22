use sentiment_dashboard;

create table posts(
id int auto_increment primary key,
keyword varchar(200) not null,
content text not null,
sentiment enum ('positive','neutral','negative'),
sent_score int,
created_at timestamp default current_timestamp,
index index_keyword(keyword),
index index_created_at(created_at)
);

create table hourly_stats(
id int auto_increment primary key,
keyword varchar(200) not null,
hour datetime not null,
pos_count int default 0,
neu_count int default 0,
neg_count int default 0,
tot_count int default 0,
unique key unique_keyword_hour(keyword,hour)
);
