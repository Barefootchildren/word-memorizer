create table user
(
    id         int auto_increment
        primary key,
    username   varchar(50)                        not null,
    password   varchar(255)                       not null,
    created_at datetime default CURRENT_TIMESTAMP null,
    constraint username
        unique (username)
);

create table word_book
(
    id         int auto_increment comment '单词书ID'
        primary key,
    name       varchar(100)                       not null comment '单词书名称，例如：基础词汇1、TOPIK 高频、大学英语词汇等',
    lang       varchar(10)                        not null comment '语言类型：EN 表示英语单词书；KO 表示韩语单词书',
    sort_order int      default 0                 null comment '排序字段，同一语言下的单词书按照 sort_order 升序排列',
    created_at datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '单词书表，用于区分不同系列的单词集';

create table word
(
    id         int auto_increment
        primary key,
    word       varchar(100)                          not null,
    meaning    text                                  not null,
    day        int                                   not null,
    created_at datetime    default CURRENT_TIMESTAMP null,
    lang       varchar(10) default 'EN'              not null,
    book_id    int                                   null comment '所属单词书ID，关联 word_book.id',
    constraint fk_word_book
        foreign key (book_id) references word_book (id)
            on update cascade
);

create table hard_word
(
    id         int auto_increment
        primary key,
    user_id    int                                  not null,
    word_id    int                                  not null,
    created_at datetime   default CURRENT_TIMESTAMP null,
    is_star    tinyint(1) default 0                 not null comment '0=未星标；1=星标（超难单词）',
    constraint uniq_user_word
        unique (user_id, word_id),
    constraint hard_word_ibfk_1
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint hard_word_ibfk_2
        foreign key (word_id) references word (id)
            on delete cascade
);

create index word_id
    on hard_word (word_id);

create table review_record
(
    id           int auto_increment
        primary key,
    user_id      int                                   not null,
    word_id      int                                   not null,
    status       varchar(20) default 'unknown'         null,
    review_times int         default 0                 null,
    last_review  datetime                              null,
    created_at   datetime    default CURRENT_TIMESTAMP null,
    constraint uniq_user_word_review
        unique (user_id, word_id),
    constraint review_record_ibfk_1
        foreign key (user_id) references user (id)
            on delete cascade,
    constraint review_record_ibfk_2
        foreign key (word_id) references word (id)
            on delete cascade
);

create index word_id
    on review_record (word_id);

create index idx_word_book_id
    on word (book_id);

create table word_extension
(
    id         int auto_increment
        primary key,
    word_id    int                                not null,
    type       varchar(20)                        not null comment 'SIMILAR=近义词；RELATED=关联词；IDIOM=惯用语；ANTONYM=反义词；SENTENCE=例句',
    text_kor   varchar(255)                       not null,
    text_cn    varchar(255)                       not null,
    sort_order int      default 0                 null,
    created_at datetime default CURRENT_TIMESTAMP null,
    constraint fk_word_extension_word
        foreign key (word_id) references word (id)
            on delete cascade
);

create index idx_word_extension_word_id
    on word_extension (word_id);

