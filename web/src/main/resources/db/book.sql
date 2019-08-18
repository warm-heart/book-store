
create database book;

use book;


DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
`user_id` varchar (64) NOT NULL comment '用户id',
`user_name` varchar(64) NOT NULL comment '用户姓名',
`user_password` varchar(64) NOT NULL comment '用户密码',
`user_address` varchar(64)   comment '用户地址',
`user_email` varchar (64)   comment '用户邮箱',
`user_phone` varchar(64)    comment '用户电话',
`create_time` timestamp NOT NULL default current_timestamp comment '创建时间',
`update_time` timestamp NOT NULL default current_timestamp on update current_timestamp comment '更新时间',
PRIMARY KEY (`user_id`)

)  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '用户表';



DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
`role_id` int NOT NULL AUTO_INCREMENT,
`user_id` varchar (64) NOT NULL comment '用户id',
`role_description` varchar(64) NOT NULL comment '角色描述',

`create_time` timestamp NOT NULL default current_timestamp comment '创建时间',
`update_time` timestamp NOT NULL default current_timestamp on update current_timestamp comment '更新时间',
PRIMARY KEY (`role_id`),
key `user_id` (`user_id`)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '角色表';




DROP TABLE IF EXISTS `book_info`;
CREATE TABLE `book_info` (
`book_id` varchar (64) NOT NULL comment '图书id',
`book_name` varchar(64) NOT NULL comment '图书名称',
`book_description` varchar(64) NOT NULL comment '图书描述',
`book_stock` int Not NULL comment '库存',
`book_icon` varchar(64) NOT NULL comment '图书图片',
`book_price` decimal (8,2) Not NULL comment '图书价格',
`category_type` int Not NULL comment '类目编号',

`create_time` timestamp NOT NULL default current_timestamp comment '创建时间',
`update_time` timestamp NOT NULL default current_timestamp on update current_timestamp comment '更新时间',
PRIMARY KEY (`book_id`)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment '图书详情表';




DROP TABLE IF EXISTS `book_category`;
CREATE TABLE `book_category` (
`category_id` int NOT NULL AUTO_INCREMENT,
`category_name` varchar(64) Not NULL comment '类目名称',
`category_type` int Not NULL comment '类目编号',
`create_time` timestamp NOT NULL default current_timestamp comment '创建时间',
`update_time` timestamp NOT NULL default current_timestamp on update current_timestamp comment '更新时间',
PRIMARY KEY (`category_id`),
unique key `uqe_category_type` (`category_type`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment'图书类目表';




DROP TABLE IF EXISTS `book_record`;
CREATE TABLE `book_borrow_record` (
`record_id` int NOT NULL AUTO_INCREMENT,
`user_id` varchar(64) Not NULL comment '用户id',
`book_id` varchar(64) Not NULL comment '图书id',
`lend_status`  tinyint(3) Not NULL default  '0' comment '借阅状态,默认0未还',
 `lend_date` date DEFAULT NULL,
`create_time` timestamp NOT NULL default current_timestamp comment '创建时间',
`update_time` timestamp NOT NULL default current_timestamp on update current_timestamp comment '更新时间',


PRIMARY KEY (`record_id`),
unique key `user_id` (`user_id`),
unique key `book_id` (`book_id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment'图书借阅记录表';



