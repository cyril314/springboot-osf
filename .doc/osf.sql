/*
SQLyog v10.2 
MySQL - 5.7.9 : Database - osf
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`osf` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci */;

USE `osf`;

/*Table structure for table `osf_albums` */

DROP TABLE IF EXISTS `osf_albums`;

CREATE TABLE `osf_albums` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `create_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `album_title` text,
  `album_desc` text COMMENT '描述',
  `last_add_ts` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `photos_count` int(11) NOT NULL DEFAULT '0',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '状态',
  `cover` varchar(45) DEFAULT NULL,
  `album_tags` text,
  PRIMARY KEY (`id`),
  KEY `fk_osf_albums_album_author_idx` (`user_id`),
  CONSTRAINT `fk_osf_albums_album_author` FOREIGN KEY (`user_id`) REFERENCES `osf_users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `osf_albums` */

/*Table structure for table `osf_comments` */

DROP TABLE IF EXISTS `osf_comments`;

CREATE TABLE `osf_comments` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `comment_object_type` int(11) NOT NULL COMMENT 'post, album,...',
  `comment_object_id` int(11) NOT NULL,
  `comment_author` int(11) NOT NULL,
  `comment_author_name` varchar(100) NOT NULL,
  `comment_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `comment_content` text NOT NULL,
  `comment_parent` int(11) NOT NULL DEFAULT '0',
  `comment_parent_author_name` varchar(100) DEFAULT NULL,
  `comment_parent_author` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_osf_comments_comment_author_idx` (`comment_author`),
  CONSTRAINT `fk_osf_comments_comment_author` FOREIGN KEY (`comment_author`) REFERENCES `osf_users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `osf_comments` */

/*Table structure for table `osf_events` */

DROP TABLE IF EXISTS `osf_events`;

CREATE TABLE `osf_events` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `object_type` int(11) NOT NULL,
  `object_id` int(11) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` int(11) NOT NULL,
  `user_name` varchar(50) DEFAULT NULL,
  `user_avatar` varchar(100) DEFAULT NULL,
  `like_count` int(11) NOT NULL,
  `share_count` int(11) NOT NULL,
  `comment_count` int(11) NOT NULL,
  `title` text,
  `summary` text,
  `content` text,
  `tags` text,
  `following_user_id` int(11) DEFAULT NULL,
  `following_user_name` varchar(50) DEFAULT NULL,
  `follower_user_id` int(11) DEFAULT NULL,
  `follower_user_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `osf_events` */

/*Table structure for table `osf_followers` */

DROP TABLE IF EXISTS `osf_followers`;

CREATE TABLE `osf_followers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `user_name` varchar(50) DEFAULT NULL,
  `follower_user_id` int(11) NOT NULL,
  `follower_user_name` varchar(50) DEFAULT NULL,
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`follower_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `osf_followers` */

/*Table structure for table `osf_followings` */

DROP TABLE IF EXISTS `osf_followings`;

CREATE TABLE `osf_followings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `user_name` varchar(50) DEFAULT NULL,
  `following_user_id` int(11) NOT NULL,
  `following_user_name` varchar(50) DEFAULT NULL,
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`following_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `osf_followings` */

/*Table structure for table `osf_interests` */

DROP TABLE IF EXISTS `osf_interests`;

CREATE TABLE `osf_interests` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `tag_id` int(11) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `osf_interests` */

/*Table structure for table `osf_likes` */

DROP TABLE IF EXISTS `osf_likes`;

CREATE TABLE `osf_likes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `object_type` int(11) NOT NULL,
  `object_id` int(11) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`object_type`,`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `osf_likes` */

/*Table structure for table `osf_notifications` */

DROP TABLE IF EXISTS `osf_notifications`;

CREATE TABLE `osf_notifications` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `notify_type` int(11) NOT NULL COMMENT '通知类型 Dic里有定义',
  `notify_id` int(11) NOT NULL COMMENT '通告对象ID,如评论的ID',
  `object_type` int(11) NOT NULL COMMENT '被通告的对象类型 Dic里有定义',
  `object_id` int(11) NOT NULL COMMENT '被通告对象的ID',
  `notified_user` int(11) NOT NULL COMMENT '被通告的用户',
  `notifier` int(11) NOT NULL COMMENT '通告者',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间戳',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '状态 0-未读 1-已读',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='通知';

/*Data for the table `osf_notifications` */

/*Table structure for table `osf_photos` */

DROP TABLE IF EXISTS `osf_photos`;

CREATE TABLE `osf_photos` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(45) NOT NULL,
  `album_id` int(11) NOT NULL,
  `ts` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `desc` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `osf_photos` */

/*Table structure for table `osf_posts` */

DROP TABLE IF EXISTS `osf_posts`;

CREATE TABLE `osf_posts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `post_author` int(11) NOT NULL COMMENT '作者ID',
  `post_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `post_content` longtext NOT NULL,
  `post_title` text,
  `post_excerpt` text COMMENT '摘要',
  `post_status` int(11) NOT NULL DEFAULT '0',
  `comment_status` int(11) NOT NULL DEFAULT '0',
  `post_pwd` varchar(60) DEFAULT NULL,
  `post_lasts` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `comment_count` int(11) NOT NULL DEFAULT '0',
  `like_count` int(11) NOT NULL DEFAULT '0',
  `share_count` int(11) NOT NULL DEFAULT '0',
  `post_url` varchar(45) DEFAULT NULL,
  `post_tags` text,
  `post_album` int(11) NOT NULL DEFAULT '0',
  `post_cover` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_osf_users_post_author_idx` (`post_author`),
  CONSTRAINT `fk_osf_users_post_author` FOREIGN KEY (`post_author`) REFERENCES `osf_users` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `osf_posts` */

/*Table structure for table `osf_relations` */

DROP TABLE IF EXISTS `osf_relations`;

CREATE TABLE `osf_relations` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `object_type` int(11) NOT NULL,
  `object_id` int(11) NOT NULL,
  `tag_id` int(11) NOT NULL COMMENT '标签ID',
  `add_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`),
  KEY `fk_tag_id_idx` (`tag_id`),
  CONSTRAINT `fk_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `osf_tags` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `osf_relations` */

/*Table structure for table `osf_tags` */

DROP TABLE IF EXISTS `osf_tags`;

CREATE TABLE `osf_tags` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tag` varchar(30) NOT NULL COMMENT '标签',
  `add_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `cover` varchar(45) DEFAULT NULL COMMENT '封面',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Data for the table `osf_tags` */

/*Table structure for table `osf_users` */

DROP TABLE IF EXISTS `osf_users`;

CREATE TABLE `osf_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '用户名',
  `user_email` varchar(100) NOT NULL COMMENT '登录名，邮箱',
  `user_pwd` varchar(100) NOT NULL COMMENT '用户密码',
  `user_registered_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `user_status` int(11) DEFAULT NULL COMMENT '用户状态',
  `user_activationKey` varchar(24) DEFAULT NULL COMMENT '关键字',
  `user_avatar` varchar(100) DEFAULT NULL COMMENT '用户头像',
  `user_desc` text COMMENT '排序',
  `resetpwd_key` varchar(100) DEFAULT NULL COMMENT '保留的',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_name` (`user_name`,`user_email`),
  UNIQUE KEY `user_name_2` (`user_name`,`user_email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

/*Data for the table `osf_users` */

insert  into `osf_users`(`id`,`user_name`,`user_email`,`user_pwd`,`user_registered_date`,`user_status`,`user_activationKey`,`user_avatar`,`user_desc`,`resetpwd_key`) values (1,'admin','admin@admin.com','E10ADC3949BA59ABBE56E057F20F883E','2015-10-29 13:47:37',0,NULL,'default-avatar.jpg',NULL,NULL),(2,'test','test@foxmail.com','E10ADC3949BA59ABBE56E057F20F883E','2015-10-29 14:28:08',0,'n19jbzAw0MpoTheC0Fh6Bg==','default-avatar.jpg',NULL,NULL);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
