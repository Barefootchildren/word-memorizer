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

create table word
(
    id         int auto_increment
        primary key,
    word       varchar(100)                       not null,
    meaning    text                               not null,
    day        int                                not null,
    created_at datetime default CURRENT_TIMESTAMP null
);

create table hard_word
(
    id         int auto_increment
        primary key,
    user_id    int                                not null,
    word_id    int                                not null,
    created_at datetime default CURRENT_TIMESTAMP null,
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

-- 插入测试单词数据（10天 × 10个）
INSERT INTO word (word, meaning, day) VALUES
-- Day 1
('apple', 'n. 苹果', 1),
('banana', 'n. 香蕉', 1),
('cat', 'n. 猫', 1),
('dog', 'n. 狗', 1),
('egg', 'n. 鸡蛋', 1),
('fish', 'n. 鱼', 1),
('grape', 'n. 葡萄', 1),
('hat', 'n. 帽子', 1),
('ice', 'n. 冰', 1),
('juice', 'n. 果汁', 1),

-- Day 2
('king', 'n. 国王', 2),
('lion', 'n. 狮子', 2),
('monkey', 'n. 猴子', 2),
('nose', 'n. 鼻子', 2),
('orange', 'n. 橙子', 2),
('pig', 'n. 猪', 2),
('queen', 'n. 王后', 2),
('rose', 'n. 玫瑰', 2),
('sun', 'n. 太阳', 2),
('tree', 'n. 树', 2),

-- Day 3
('umbrella', 'n. 雨伞', 3),
('violin', 'n. 小提琴', 3),
('water', 'n. 水', 3),
('x-ray', 'n. X光', 3),
('yogurt', 'n. 酸奶', 3),
('zebra', 'n. 斑马', 3),
('ball', 'n. 球', 3),
('chair', 'n. 椅子', 3),
('desk', 'n. 书桌', 3),
('eraser', 'n. 橡皮', 3),

-- Day 4
('flower', 'n. 花', 4),
('garden', 'n. 花园', 4),
('house', 'n. 房子', 4),
('island', 'n. 岛', 4),
('jacket', 'n. 夹克', 4),
('key', 'n. 钥匙', 4),
('lamp', 'n. 灯', 4),
('milk', 'n. 牛奶', 4),
('notebook', 'n. 笔记本', 4),
('orange juice', 'n. 橙汁', 4),

-- Day 5
('pencil', 'n. 铅笔', 5),
('queen bee', 'n. 蜂王', 5),
('river', 'n. 河流', 5),
('stone', 'n. 石头', 5),
('tiger', 'n. 老虎', 5),
('umbrella stand', 'n. 伞架', 5),
('vase', 'n. 花瓶', 5),
('window', 'n. 窗户', 5),
('xylophone', 'n. 木琴', 5),
('yarn', 'n. 纱线', 5),

-- Day 6
('zoo', 'n. 动物园', 6),
('ant', 'n. 蚂蚁', 6),
('bread', 'n. 面包', 6),
('cloud', 'n. 云', 6),
('door', 'n. 门', 6),
('ear', 'n. 耳朵', 6),
('frog', 'n. 青蛙', 6),
('grass', 'n. 草', 6),
('hill', 'n. 小山', 6),
('ink', 'n. 墨水', 6),

-- Day 7
('juice box', 'n. 果汁盒', 7),
('kite', 'n. 风筝', 7),
('leaf', 'n. 树叶', 7),
('moon', 'n. 月亮', 7),
('nest', 'n. 鸟巢', 7),
('ocean', 'n. 海洋', 7),
('panda', 'n. 熊猫', 7),
('queen ant', 'n. 蚁后', 7),
('rain', 'n. 雨', 7),
('snow', 'n. 雪', 7),

-- Day 8
('train', 'n. 火车', 8),
('umbrella plant', 'n. 伞树', 8),
('village', 'n. 村庄', 8),
('windowpane', 'n. 窗玻璃', 8),
('xenon', 'n. 氙气', 8),
('yard', 'n. 院子', 8),
('zucchini', 'n. 西葫芦', 8),
('apple pie', 'n. 苹果派', 8),
('blueberry', 'n. 蓝莓', 8),
('camera', 'n. 相机', 8),

-- Day 9
('diamond', 'n. 钻石', 9),
('engine', 'n. 发动机', 9),
('feather', 'n. 羽毛', 9),
('guitar', 'n. 吉他', 9),
('hammer', 'n. 锤子', 9),
('ice cream', 'n. 冰淇淋', 9),
('jewel', 'n. 宝石', 9),
('kangaroo', 'n. 袋鼠', 9),
('ladder', 'n. 梯子', 9),
('mirror', 'n. 镜子', 9),

-- Day 10
('notebook computer', 'n. 笔记本电脑', 10),
('oyster', 'n. 牡蛎', 10),
('pizza', 'n. 披萨', 10),
('quilt', 'n. 被子', 10),
('robot', 'n. 机器人', 10),
('spoon', 'n. 勺子', 10),
('toothbrush', 'n. 牙刷', 10),
('umbrella hat', 'n. 雨帽', 10),
('vaccine', 'n. 疫苗', 10),
('wallet', 'n. 钱包', 10);

